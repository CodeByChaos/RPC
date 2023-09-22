package com.chaos;

public class ChaosrpcImpl implements HelloChaosrpc{
    @Override
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
