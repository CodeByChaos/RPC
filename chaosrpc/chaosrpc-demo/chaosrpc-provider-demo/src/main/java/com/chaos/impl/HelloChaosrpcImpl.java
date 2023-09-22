package com.chaos.impl;

import com.chaos.HelloChaosrpc;

public class HelloChaosrpcImpl implements HelloChaosrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
