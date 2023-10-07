package com.chaos.impl;

import com.chaos.HelloChaosrpc;
import com.chaos.annotation.ChaosApi;
import com.chaos.annotation.TryTimes;

@ChaosApi
public class HelloChaosrpcImpl implements HelloChaosrpc {
    @Override
    @TryTimes(tryTimes = 3, intervalTimes = 3000)
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
