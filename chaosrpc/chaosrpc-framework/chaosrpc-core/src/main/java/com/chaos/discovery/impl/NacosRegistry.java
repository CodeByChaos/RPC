package com.chaos.discovery.impl;

import com.chaos.Constant;
import com.chaos.utils.NetUtils;
import com.chaos.utils.zookeeper.ZookeeperNode;
import com.chaos.utils.zookeeper.ZookeeperUtils;
import com.chaos.ServiceConfig;
import com.chaos.discovery.AbstractRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosRegistry extends AbstractRegistry {

    // 维护一个zookeeper实例
    private ZooKeeper zooKeeper;

    public NacosRegistry(){
        this.zooKeeper = ZookeeperUtils.createZooKeeper();
    }


    public NacosRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZooKeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {

        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        // 这个节点应该是一个持久节点
        if(!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode node = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // 创建本机的临时节点，ip:port
        // 服务提供方的端口一般直接设定，我们还需要一个获取ip地址的方法。
        // ip通常是需要一个局域网ip，不是127.0.0.1，也不是ipv6
        // 192.168.121.121
        // todo: 后续处理端口的问题
        String temporaryNode = parentNode + "/" + NetUtils.getIpAddress() + ":" + 8088;
        if(!ZookeeperUtils.exists(zooKeeper, temporaryNode, null)) {
            ZookeeperNode node = new ZookeeperNode(temporaryNode, null);
            ZookeeperUtils.createNode(zooKeeper, node, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }

        if(log.isDebugEnabled()){
            log.debug("服务{}，已经被注册", service.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        return null;
    }
}
