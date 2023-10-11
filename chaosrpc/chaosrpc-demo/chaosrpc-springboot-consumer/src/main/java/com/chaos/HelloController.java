package com.chaos;

import com.chaos.annotation.ChaosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chaos Wong
 */
@RestController
public class HelloController {

    // 需要注入一个代理对象
    @ChaosService
    private HelloChaosrpc helloChaosrpc;

    @GetMapping("/hello")
    public String hello() {
        return helloChaosrpc.sayHi("provider");
    }
}
