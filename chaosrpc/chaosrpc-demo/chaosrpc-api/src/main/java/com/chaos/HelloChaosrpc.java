package com.chaos;

public interface HelloChaosrpc {

    /**
     * 通用接口，server和client都需要依赖
     * @param msg 发送具体的消息
     * @return 返回的结果
     */
    String sayHi(String msg);
}
