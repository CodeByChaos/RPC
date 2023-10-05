package com.chaos;

import com.chaos.discovery.Registry;
import com.chaos.proxy.handler.RPCComsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
        Class<T>[] classes = new Class[]{interfaceConsumer};
        InvocationHandler handler = new RPCComsumerInvocationHandler(registry, interfaceConsumer);

        // 使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T)helloProxy;
    }
}
