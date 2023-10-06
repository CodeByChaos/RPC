package com.chaos;

import com.chaos.annotation.ChaosApi;
import com.chaos.channelHandler.handler.ChaosrpcRequestDecoder;
import com.chaos.channelHandler.handler.ChaosrpcResponseEncoder;
import com.chaos.channelHandler.handler.MethodCallHandler;
import com.chaos.config.Configuration;
import com.chaos.core.HeartBeatDetector;
import com.chaos.discovery.RegistryConfig;
import com.chaos.loadbalance.LoadBalancer;
import com.chaos.transport.message.ChaosrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ChaosrpcBootstrap {

    // ChaosrpcBootstrap 是个单例，我们希望每个应用程序只有一个实例
    private static final ChaosrpcBootstrap bootstrap = new ChaosrpcBootstrap();

    // 全局的配置中心
    private Configuration configuration;

    // 保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<ChaosrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    // 连接的缓存，如果使用InetSocketAddress类做key，一定要看有没有重写equals()和toString()。
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    // 维护已经发布且暴露的服务列表 key->interface的全限定名 value->ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVICE_LISTS = new ConcurrentHashMap<>(16);

    // 定义全局的对外挂起的 completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();

    // 维护一个ZooKeeper实例
//    private ZooKeeper zooKeeper;

    private ChaosrpcBootstrap() {
        // 构造启动引导程序时需要做一些什么初始化的事
        configuration = new Configuration();
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
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig 注册中心
     * @return this 当前实例
     */
    public ChaosrpcBootstrap registry(RegistryConfig registryConfig) {
        // 尝试使用 registryConfig 获取一个注册中心，有点工厂设计模式
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 用来配置负载均衡策略
     * @param loadBalancer 负载均衡策略
     * @return this 当前实例
     */
    public ChaosrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 配置当前暴露的服务使用协议
     * @param protocolConfig 协议的封装
     * @return null
     */
    public ChaosrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
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
        configuration.getRegistryConfig().getRegistry().register(service);

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
        // 1.创建eventLoop，老板只负责处理请求，之后会将请求分发至worker
        // 官方默认boss:worker 1:5
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            // 2.需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3.配置服务器
            serverBootstrap.group(boss, worker)
                    // 通过工厂方法设计模式实例化一个channel
                    .channel(NioServerSocketChannel.class)
                    // 设置监听端口
                    .localAddress(new InetSocketAddress(configuration.getPort()))
                    // ChannelInitializer是一个特殊的处理类，
                    // 他的目的是帮助使用者配置一个新的Channel，
                    // 用于把许多自定义的处理类增加到pipeline上来
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 核心，我们需要添加很多入栈和出栈的handler
                            // 配置childHandler来通知一个关于消息处理的InfoServerHandler实例
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new ChaosrpcRequestDecoder())
                                    // 根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    .addLast(new ChaosrpcResponseEncoder());
                        }
                    });

            // 4.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
            // 阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            // closeFuture().sync()会阻塞当前线程，直到通道关闭操作完成。这可以用于确保在关闭通道之前，程序不会提前退出。
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  ----- 服务调用方相关的api -----
     */
    public ChaosrpcBootstrap reference(ReferenceConfig<?> reference){
        // 开启对这个服务的心跳检测
        HeartBeatDetector.detectHeartBeat(reference.getInterface().getName());
        // 在这个方法里我们是否可以拿到相关的配置项--注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        // 1.reference需要一个注册中心
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }

    /**
     * 配置序列化方式
     * @param serializeType 序列化的方式
     */
    public ChaosrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if(log.isDebugEnabled()) {
            log.error("配置了序列化的方式为{}.", serializeType);
        }
        return this;
    }

    public ChaosrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if(log.isDebugEnabled()) {
            log.error("配置了压缩的方式为{}.", compressType);
        }
        return this;
    }

    /**
     * 扫描包进行批量注册
     * @param packageName 包名
     * @return this
     */
    public ChaosrpcBootstrap scan(String packageName) {
        // 1.需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);
        // 2.通过反射获取接口，构建具体实现
        List<Class<?>> classes = classNames.stream().map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(ChaosApi.class) != null)
                .collect(Collectors.toList());
        for (Class<?> clazz : classes) {
            // 获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if(log.isDebugEnabled()) {
                    log.debug("---->已经通过包扫描，将服务{}发布.", anInterface);
                }
                // 3.发布
                publish(serviceConfig);
            }
        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1.通过packageName获得绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null) {
            throw new RuntimeException("包扫描时，发现路径可能不存在.");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<String>();
        classNames = recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if(file.isDirectory()) {
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null) {
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    // 文件 ----> 类权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }
        } else {
            // 文件 ----> 类权限定名称
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\")))
                .replaceAll("\\\\", ".");
        fileName = fileName.substring(0, fileName.indexOf(".class"));
        return fileName;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
