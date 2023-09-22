package com.chaosrpc;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class ChaosrpcBootstrap {

    // ChaosrpcBootstrap 是个单例，我们希望每个应用程序只有一个实例
    private static ChaosrpcBootstrap bootstrap = new ChaosrpcBootstrap();

    private ChaosrpcBootstrap() {
        // 构造启动引导程序时需要做一些什么初始化的事

    }

    public static ChaosrpcBootstrap getInstance() {
        return bootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param applicationName 当前应用的名字
     * @return this 当前实例
     */
    public ChaosrpcBootstrap application(String applicationName) {
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig 注册中心
     * @return this 当前实例
     */
    public ChaosrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用协议
     * @param protocolConfig 协议的封装
     * @return null
     */
    public ChaosrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     *  ----- 服务提供方相关的api -----
     */

    /**
     * 发布服务
     * @param service 封装需要发布的服务
     * @return this 当前实例
     */
    public ChaosrpcBootstrap publish(ServiceConfig<?> service) {
        if(log.isDebugEnabled()){
            log.debug("服务{}，已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布 将接口实现，注册到服务中心
     * @param service 封装需要发布的服务集合
     * @return this 当前实例
     */
    public ChaosrpcBootstrap publish(List<?> service) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }

    /**
     *  ----- 服务调用方相关的api -----
     */
    public ChaosrpcBootstrap reference(ReferenceConfig<?> reference){
        // 在这个方法里我们是否可以拿到相关的配置项--注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        return this;
    }
}
