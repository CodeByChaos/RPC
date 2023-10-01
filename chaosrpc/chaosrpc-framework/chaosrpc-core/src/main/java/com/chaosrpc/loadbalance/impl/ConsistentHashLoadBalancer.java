package com.chaosrpc.loadbalance.impl;

import com.chaosrpc.ChaosrpcBootstrap;
import com.chaosrpc.loadbalance.AbstractLoadBalancer;
import com.chaosrpc.loadbalance.Selector;
import com.chaosrpc.transport.message.ChaosrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 轮询的负载均衡策略
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * 一致性hash的具体实现
     */
    private static class ConsistentHashSelector implements Selector {

        // hash环 用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            // 我们应该尝试将节点转换为虚拟节点进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(address);
            }

        }

        @Override
        public InetSocketAddress getNext() {
            // 1.hash环已经建立好了，接下来需要对请求的要素做处理，我们应该选择什么要素来进行hash运算
            // 有没有办法获取到具体的请求内容，chaosrpcRequest --> threadLocal
            ChaosrpcRequest chaosrpcRequest = ChaosrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            // 我们想根据请求的一些特征来选择服务器 id
            String requestId = Long.toString(chaosrpcRequest.getRequestId());
            // 对请求id做hash，字符串默认的hash不太行
            int hash = hash(requestId);
            // 判断该hash值是否能直接落在应该服务器上，和服务器的hash一样
            if(!circle.containsKey(hash)) {
                // 寻找最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }

        @Override
        public void reBalance() {

        }

        /**
         * 将每个节点挂载到hash环上
         * @param address 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress address) {
            // 为每个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                // 挂载到hash环上
                circle.put(hash, address);
                if(log.isDebugEnabled()) {
                    log.debug("hash为{}的节点已经到了挂载了hash环上.", hash);
                }
            }
        }

        private void removeNodeFromCircle(InetSocketAddress address) {
            // 为每个节点生成匹配的虚拟节点进行删除
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                circle.remove(hash);
            }
        }
        /**
         * 具体的hash算法
         * @return hash值
         */
        private int hash(String s) {
            MessageDigest md;
            try{
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // md5得到的结果是一个字节数组，但我们想要int 4个字节
            int res = 0;
            for (int i = 0; i < 4; i ++) {
                res = res << 8;
                if(digest[i] < 0) {
                    res = res | (digest[i] & 255);
                }else {
                    res = res | digest[i];
                }
            }
            return res;
        }
        private String toBinary(int i) {
            String s = Integer.toBinaryString(i);
            int index = 32 - s.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(s);
            return sb.toString();
        }
    }
}
