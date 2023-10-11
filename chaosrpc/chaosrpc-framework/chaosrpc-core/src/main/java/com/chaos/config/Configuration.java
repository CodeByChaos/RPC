package com.chaos.config;

import com.chaos.IdGenerator;
import com.chaos.compress.Compressor;
import com.chaos.compress.impl.GZIPCompressor;
import com.chaos.discovery.RegistryConfig;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.loadbalance.impl.RoundRobinLoadBalancer;
import com.chaos.protection.CircuitBreaker;
import com.chaos.protection.RateLimiter;
import com.chaos.serialize.Serializer;
import com.chaos.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类，代码配置 ----> xml配置 ----> 默认项
 * @author Chaos Wong
 */
@Data
@Slf4j
public class Configuration {

    // 配置信息 ----> 端口号
    private int port = 8088;

    // 配置信息 ----> 应用程序名字
    private String applicationName = "default";

    // 配置信息 ----> 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息 ----> 序列化使用的协议
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();

    // 配置信息 ----> 压缩使用的协议
    private String compressType = "gzip";
    private Compressor compressor = new GZIPCompressor();

    // 配置信息 ----> Id生成器
    private IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息 ----> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // 配置信息 ----> 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>();
    // 配置信息 ----> 为每一个ip配置一个断路器
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>();

    // 读xml，dom4j
    public Configuration() {
        // 1.成员变量的默认配置项

        // 2.spi机制发现相关配置项
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        // 3.读取xml获取上边的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        // 4.编程配置项 chaosrpcBootStrap提供
    }

}
