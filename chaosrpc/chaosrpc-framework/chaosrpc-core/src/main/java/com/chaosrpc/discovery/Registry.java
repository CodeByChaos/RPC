package com.chaosrpc.discovery;

import com.chaosrpc.ServiceConfig;

/**
 * 注册中心，应该具有什么样的能力
 */
public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

}
