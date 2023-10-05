package com.chaos;

import com.chaos.discovery.RegistryConfig;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.loadbalance.impl.RoundRobinLoadBalancer;
import lombok.Data;

/**
 * 全局的配置类，代码配置 ----> xml配置 ----> 默认项
 * @author Chaos Wong
 */
@Data
public class Configuration {

    // 配置信息 ----> 端口号
    public int port = 8091;

    // 配置信息 ----> 应用程序名字
    private String applicationName = "default";

    // 配置信息 ----> 注册中心
    public RegistryConfig registryConfig;

    // 配置信息 ----> 序列化协议
    public ProtocolConfig protocolConfig;
    // 配置信息 ----> 序列化使用的协议
    public String serializeType = "jdk";
    // 配置信息 ----> 压缩使用的协议
    public String compressType = "gzip";

    // 配置信息 ----> Id生成器
    public IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息 ----> 负载均衡策略
    public LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // 读xml

    public Configuration() {
        // 读取xml获取上边的信息

    }


    // 代码配置有引导程序配置

}
