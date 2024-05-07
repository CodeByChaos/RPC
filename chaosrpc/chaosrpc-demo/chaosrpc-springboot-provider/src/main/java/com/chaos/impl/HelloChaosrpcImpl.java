package com.chaos.impl;

import com.chaos.service.HelloChaosrpc;
import com.chaos.annotation.ChaosApi;
import com.chaos.annotation.TryTimes;

/**
 * @author WongYut
 */
@ChaosApi(group = "primary")
public class HelloChaosrpcImpl implements HelloChaosrpc {
    @Override
    @TryTimes(tryTimes = 3, intervalTimes = 3000)
    public String sayHi(String msg) {
        return "hi consumer " + msg;
    }
}
