package com.chaosrpc;

import com.chaos.exceptions.NetworkException;
import com.chaosrpc.discovery.Registry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceConsumer;

    private Registry registry;

    public Class<T> getInterface() {
        return interfaceConsumer;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return 代理对象
     */
    public T get() {
        // 此处一定是使用动态代理完成了一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceConsumer};

        // 使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 我们调用sayHi方法，事实上会走进这个代码段中
                // 我们已经知道method(具体的方法)，args(参数列表)
                log.info("methods---->{}", method);
                log.info("args---->{}", args);
                // 1.发现服务，从注册中心，寻找一个可用的服务
                // 传入服务的名字，返回一个ip:port

                InetSocketAddress address = registry.lookup(interfaceConsumer.getName());
                if(log.isDebugEnabled()) {
                    log.debug("服务调用方，发现了服务{}的可用主机{}", interfaceConsumer.getName(), address);
                }
                // 2.使用netty连接服务器，发送调用的服务的名字+方法名字+参数列表，得到结果
                // 定义线程池，EventLoopGroup
                // q:整个连接过程放在此处是否可行？也就意味着每次调用都会产生一个新的netty连接。如何缓存我们的连接？
                // 每次在此处建立一个新的连接是不合适的
                // 解决方案？缓存channel，尝试从缓存中获取channel，如果未获取，则创建新的连接，并进行缓存
                // 1.尝试从全局缓存中获取一个channel
                Channel channel = ChaosrpcBootstrap.CHANNEL_CACHE.get(address);
                if(channel == null) {
                    // await()方法会阻塞，会等待连接成功再返回，netty还提供了异步处理的逻辑
                    // sync() 和 await() 都是阻塞当前线程，获取当前返回值（连接的过程是异步的，发送数据的过程是异步的）
                    // 如果发生了异常，sync()会主动在主线程抛出异常，await()不会，异常在子线程中处理需要使用future中处理

                    // 同步操作
/*                    channel = NettyBootstrapInitializer.getBootstrap()
                            .connect(address)
                            .await()
                            .channel();*/

                    // 使用addListener执行的异步操作
                    CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
                    NettyBootstrapInitializer.getBootstrap()
                            .connect(address)
                            .addListener((ChannelFutureListener)promise -> {
                               if(promise.isDone()) {
                                   // 异步的，我们已经完成
                                   if(log.isDebugEnabled()) {
                                       log.debug("已经和{}成功建立了连接。", address);
                                   }
                                   channelFuture.complete(promise.channel());
                               } else if (!promise.isSuccess()) {
                                   channelFuture.completeExceptionally(promise.cause());
                               }
                            });
                    // 阻塞获取channel
                    channel = channelFuture.get(3, TimeUnit.SECONDS);
                    // 如何等待

                    // 缓存channel
                    ChaosrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
                if(channel == null) {
                    throw new NetworkException("获取channel时发生了异常");
                }
                /*
                 * --------------同步策略--------------
                 */
/*                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
                // 需要学习channelFuture的api
                // get 阻塞获取当前结果，getNow 获取当前的结果，如果未处理完成，返回null
                if(channelFuture.isDone()) {
                    Object object = channelFuture.getNow();
                } else if(!channelFuture.isSuccess()) {
                    // 需要捕获异常，可以捕获异步任务中的异常
                    Throwable cause = channelFuture.cause();
                    throw new RuntimeException(cause);
                }*/

                /*
                 * --------------异步策略--------------
                 */
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                // todo 需要将 completableFuture 暴露出去
                channel.writeAndFlush(new Object())
                        .addListener((ChannelFutureListener)promise -> {
                            /*
                            当前的promise将来的返回结果是什么？writeAndFlush的返回结果
                            一旦数据被写出去，这个promise也就结束了
                            但我们想要什么？ 服务端给我们的返回值，所以这里处理completableFuture会有问题
                            是不是应该将 completableFuture 挂起并暴露，且得到服务提供方的响应的时候调用complete()方法
                            */
//                            if(promise.isDone()) {
//                                completableFuture.complete(promise.getNow());
//                            }
                            // 只需要处理以下异常就行了
                            if (!promise.isSuccess()) {
                                completableFuture.completeExceptionally(promise.cause());
                            }
                        });
                return completableFuture.get(3, TimeUnit.SECONDS);
            }
        });
        return (T)helloProxy;
    }
}
