package com.chaos.config;

import com.chaos.compress.CompressFactory;
import com.chaos.compress.Compressor;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.serialize.Serializer;
import com.chaos.serialize.SerializerFactory;
import com.chaos.spi.SpiHandler;

import java.util.List;

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
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if(loadBalancerWrappers.size() > 0) {
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }
        List<ObjectWrapper<Compressor>> compressorWrappers = SpiHandler.getList(Compressor.class);
        if(compressorWrappers != null) {
            compressorWrappers.forEach(CompressFactory::addCompressor);
        }
        List<ObjectWrapper<Serializer>> serializerWrappers = SpiHandler.getList(Serializer.class);
        if(serializerWrappers != null) {
            serializerWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
