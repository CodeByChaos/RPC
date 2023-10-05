package com.chaos.impl;

import com.chaos.HelloChaosrpc;
import com.chaos.annotation.ChaosApi;

@ChaosApi
public class HelloChaosrpcImpl implements HelloChaosrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
