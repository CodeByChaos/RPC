package com.chaos;

import com.chaos.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // 想尽一切办法获取代理对象，使用ReferenceConfig进行封装
        // reference一定用代理的模板方法，get()
        ReferenceConfig<HelloChaosrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloChaosrpc.class);

        // 代理做了些什么
        // 1、连接注册中心
        // 2、拉取服务列表
        // 3、选择一个服务并建立连接
        // 4.发送请求，携带一些信息（接口名， 参数列表， 方法的名字），获得结果
        ChaosrpcBootstrap.getInstance()
                .application("first-chaosrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .reference(reference);

        // 获取一个代理对象
        for (int i = 0; i < 500; i++) {
            HelloChaosrpc helloChaosrpc = reference.get();
            String s = helloChaosrpc.sayHi("你好");
            log.info("say hi ----> {}", s);
        }
    }
}
