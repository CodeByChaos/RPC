package com.chaosrpc;

import com.chaosrpc.discovery.Registry;
import com.chaosrpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

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

                System.out.println("hello proxy");
                return null;
            }
        });
        return (T)helloProxy;
    }
}
