package com.chaos.controller;

import com.chaos.service.HelloChaosrpc;
import com.chaos.annotation.ChaosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WongYut
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
