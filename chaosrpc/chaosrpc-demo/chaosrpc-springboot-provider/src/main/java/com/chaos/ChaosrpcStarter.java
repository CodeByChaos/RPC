package com.chaos;

import com.chaos.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author WongYut
 */
@Component
@Slf4j
public class ChaosrpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.debug("chaosrpc开始启动.");
        ChaosrpcBootstrap.getInstance()
                .application("first-chaosrpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
//                // 发布服务
//                .publish(service)
                .scan("com.chaos")
                // 启动服务
                .start();

    }
}
