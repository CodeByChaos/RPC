package com.chaos.proxy;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.ReferenceConfig;
import com.chaos.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chaos Wong
 */
public class ChaosrpcProxyFactory {

    public static Map<Class<?>, Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = cache.get(clazz);
        if(bean != null) {
            return (T)bean;
        }
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);
        ChaosrpcBootstrap.getInstance()
                .application("first-chaosrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        cache.put(clazz, reference.get());
        return reference.get();
    }
}
