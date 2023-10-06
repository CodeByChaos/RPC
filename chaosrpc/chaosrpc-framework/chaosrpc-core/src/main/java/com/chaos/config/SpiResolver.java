package com.chaos.config;

import com.chaos.compress.Compressor;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.serialize.Serializer;
import com.chaos.spi.SpiHandler;

/**
 * @author Chaos Wong
 */
public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {
        // 1.
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        if(loadBalancer != null) {
            configuration.setLoadBalancer(loadBalancer);
        }
        Compressor compressor = SpiHandler.get(Compressor.class);
        if(compressor != null) {
            configuration.setCompressor(compressor);
        }
        Serializer serializer = SpiHandler.get(Serializer.class);
        if(serializer != null) {
            configuration.setSerializer(serializer);
        }
    }
}
