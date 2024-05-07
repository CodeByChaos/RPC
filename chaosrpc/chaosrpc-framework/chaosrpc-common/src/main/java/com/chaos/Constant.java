package com.chaos;

/**
 * @author WongYut
 */
public class Constant {

    // zookeeper默认连接地址
    public static final String DEFAULT_ZK_CONNECTION = "127.0.0.1:2181";
    // zookeeper默认超时时间
    public static final int TIME_OUT = 10000;

    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDER_PATH = "/chaos-metadata/providers";
    public static final String BASE_CONSUMER_PATH = "/chaos-metadata/consumers";
}
