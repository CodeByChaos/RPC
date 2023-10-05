package com.chaos.loadbalance.impl;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.loadbalance.AbstractLoadBalancer;
import com.chaos.loadbalance.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * 最短响应时间的负载均衡策略
 * @author Chaos Wong
 */
@Slf4j
public class MinResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinResponseTimeSelector(serviceList);
    }

    private static class MinResponseTimeSelector implements Selector {

        public MinResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = ChaosrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("选取了响应时间为{}ms的服务节点", entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            // 直接从缓存中获取一个可用的
            Channel channel = (Channel) ChaosrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }
    }
}
