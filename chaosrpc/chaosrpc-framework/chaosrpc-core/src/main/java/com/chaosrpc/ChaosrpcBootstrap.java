package com.chaosrpc;

import com.chaos.Constant;
import com.chaos.utils.NetUtils;
import com.chaos.utils.zookeeper.ZookeeperNode;
import com.chaos.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class ChaosrpcBootstrap {

    // ChaosrpcBootstrap 是个单例，我们希望每个应用程序只有一个实例
    private static final ChaosrpcBootstrap bootstrap = new ChaosrpcBootstrap();

    // 定义一些相关的基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;

    // 维护一个ZooKeeper实例
    private ZooKeeper zooKeeper;

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
        // 这里维护一个zookeeper实例，但是会与当前工程耦合
        // 其实更希望以后可以扩展更多种不同的实现
        zooKeeper = ZookeeperUtils.createZooKeeper();

        this.registryConfig = registryConfig;
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

        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        // 这个节点应该是一个持久节点
        if(!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 创建本机的临时节点，ip:port
        // 服务提供方的端口一般直接设定，我们还需要一个获取ip地址的方法。
        // ip通常是需要一个局域网ip，不是127.0.0.1，也不是ipv6
        // 192.168.121.121
        String temporaryNode = parentNode + "/" + NetUtils.getIpAddress() + ":" + port;
        if(!ZookeeperUtils.exists(zooKeeper, temporaryNode, null)) {
            ZookeeperNode node = new ZookeeperNode(temporaryNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }

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
