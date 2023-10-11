package com.chaos.discovery.impl;

import com.chaos.Constant;
import com.chaos.exceptions.DiscoveryException;
import com.chaos.utils.NetUtils;
import com.chaos.utils.zookeeper.ZookeeperNode;
import com.chaos.utils.zookeeper.ZookeeperUtils;
import com.chaos.ChaosrpcBootstrap;
import com.chaos.ServiceConfig;
import com.chaos.discovery.AbstractRegistry;
import com.chaos.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    // 维护一个zookeeper实例
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry(){
        this.zooKeeper = ZookeeperUtils.createZooKeeper();
    }


    public ZookeeperRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZooKeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {

        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        // 建立服务节点，这个节点应该是一个持久节点
        if(!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode node = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        // 建立分组节点
        parentNode = parentNode + "/" + service.getGroup();
        if(!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode node = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 创建本机的临时节点，ip:port
        // 服务提供方的端口一般直接设定，我们还需要一个获取ip地址的方法。
        // ip通常是需要一个局域网ip，不是127.0.0.1，也不是ipv6
        // 192.168.121.121
        String temporaryNode = parentNode + "/" + NetUtils.getIpAddress() + ":" + ChaosrpcBootstrap.getInstance().getConfiguration().getPort();
        if(!ZookeeperUtils.exists(zooKeeper, temporaryNode, null)) {
            ZookeeperNode node = new ZookeeperNode(temporaryNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }

        if(log.isDebugEnabled()){
            log.debug("服务{}，已经被注册.", service.getInterface().getName());
        }
    }

    /**
     * 注册中心的核心目的是什么？拉取合适的服务列表
     * @param serviceName 服务的名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        // 1.找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName + "/" + group;

        // 2.从zookeeper中获取他的子节点 192.168.12.123:2151
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();
        if (inetSocketAddresses.size() == 0) {
            throw new DiscoveryException("未发现任何可用的服务主机.");
        }
        //  q:我们每次调用相关方法的时候都需要去注册中心拉取服务列表吗？ 本地缓存 + watcher
        //  q:我们如何合理选择一个可用的服务，而不是只获取第一个？ 负载均衡策略
        return inetSocketAddresses;
    }
}
