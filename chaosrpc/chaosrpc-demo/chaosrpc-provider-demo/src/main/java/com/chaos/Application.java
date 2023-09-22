package com.chaos;

import com.chaos.impl.HelloChaosrpcImpl;
import com.chaosrpc.ChaosrpcBootstrap;
import com.chaosrpc.ProtocolConfig;
import com.chaosrpc.RegistryConfig;
import com.chaosrpc.ServiceConfig;

public class Application {
    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1.封装要发布的服务
        ServiceConfig<HelloChaosrpc> service = new ServiceConfig<>();
        service.setInterface(HelloChaosrpc.class);
        service.setRef(new HelloChaosrpcImpl());
        // 2.定义注册中心

        // 3.通过启动引导程序，启动服务提供方
        //  1).配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        //  2).发布服务
        ChaosrpcBootstrap.getInstance()
                .application("first-chaosrpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                // 发布服务
                .publish(service)
                // 启动服务
                .start();
    }
}
