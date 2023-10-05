package com.chaos.discovery;

import com.chaos.Constant;
import com.chaos.exceptions.DiscoveryException;
import com.chaos.discovery.impl.NacosRegistry;
import com.chaos.discovery.impl.ZookeeperRegistry;

public class RegistryConfig {

    // 定义连接的url zookeeper://127.0.0.1:2181
    private String connectString;

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 可以使用简单工厂完成
     * @return 具体注册中心实例
     */
    public Registry getRegistry() {
        String[] typeAndHost = getRegistryTypeAndHost(connectString);
        // 1.需要获取注册中心的类型
        String type = typeAndHost[0].toLowerCase().trim();
        String host = typeAndHost[1];
        if("zookeeper".equals(type)) {
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if ("nacos".equals(type)) {
            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    /**
     * 根据url获取注册中心类型及注册中心地址
     * @param connectString 注册中心url
     * @return 注册中心类型及注册中心地址
     */
    private String[] getRegistryTypeAndHost(String connectString) {
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2) {
            throw new RuntimeException("给定的注册中心的url不合法");
        }
        return typeAndHost;
    }
}
