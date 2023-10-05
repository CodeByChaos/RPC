package com.chaos.watcher;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.NettyBootstrapInitializer;
import com.chaos.discovery.Registry;
import com.chaos.loadbalance.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author Chaos Wong
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        // 当前的节点是否发生了变化
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            if(log.isDebugEnabled()) {
                log.debug("检测到服务{}有节点上/下线，将重新拉取服务列表.", watchedEvent.getPath());
            }
            String serviceName = getServiceName(watchedEvent.getPath());
            Registry registry = ChaosrpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses= registry.lookup(serviceName);
            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                // 新增的节点 会在address 不在CHANNEL_CACHE

                if(!ChaosrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer
                                .getBootstrap()
                                .connect(address)
                                .sync()
                                .channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    ChaosrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            }

            // 处理下线的节点 下线的节点 可能在CHANNEL_CACHE 不在address
            for(Map.Entry<InetSocketAddress, Channel> entry : ChaosrpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if(!addresses.contains(entry.getKey())) {
                    ChaosrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获得负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = ChaosrpcBootstrap.LOAD_BALANCER;
            loadBalancer.reLoadBalance(serviceName, addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
