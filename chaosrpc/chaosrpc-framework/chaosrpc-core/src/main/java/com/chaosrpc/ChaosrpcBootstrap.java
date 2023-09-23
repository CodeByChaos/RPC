package com.chaosrpc;

import com.chaosrpc.discovery.Registry;
import com.chaosrpc.discovery.RegistryConfig;
import com.chaosrpc.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChaosrpcBootstrap {

    // ChaosrpcBootstrap 是个单例，我们希望每个应用程序只有一个实例
    private static final ChaosrpcBootstrap bootstrap = new ChaosrpcBootstrap();

    // 定义一些相关的基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;

    // 注册中心
    private Registry registry;

    // 维护已经发布且暴露的服务列表 key->interface的全限定名 value->ServiceConfig
    private static final Map<String, ServiceConfig<?>> SERVICE_LISTS = new ConcurrentHashMap<>();

    // 维护一个ZooKeeper实例
//    private ZooKeeper zooKeeper;

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
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig 注册中心
     * @return this 当前实例
     */
    public ChaosrpcBootstrap registry(RegistryConfig registryConfig) {

        // 尝试使用 registryConfig 获取一个注册中心，有点工厂设计模式
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前暴露的服务使用协议
     * @param protocolConfig 协议的封装
     * @return null
     */
    public ChaosrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     *  ----- 服务提供方相关的api -----
     */

    /**
     * 发布服务 将接口 ----> 实现，注册到服务中心
     * @param service 封装需要发布的服务
     * @return this 当前实例
     */
    public ChaosrpcBootstrap publish(ServiceConfig<?> service) {
        // 我们抽象了注册中心的概念，使用注册中心的一个实现完成注册
        registry.register(service);

        // 1.当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，提供怎么知道使用哪一个实现
        //  (1)new 一个   (2)spring beanFactory.getBean(Class)    (3)自己维护映射关系
        SERVICE_LISTS.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布 将接口实现，注册到服务中心
     * @param services 封装需要发布的服务集合
     * @return this 当前实例
     */
    public ChaosrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
