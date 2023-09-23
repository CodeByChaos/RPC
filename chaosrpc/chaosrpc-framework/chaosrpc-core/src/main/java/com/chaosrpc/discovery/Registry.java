package com.chaosrpc.discovery;

import com.chaosrpc.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心，应该具有什么样的能力
 */
public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务的名称
     * @return 服务的ip:port
     */
    InetSocketAddress lookup(String serviceName);
}
