package com.chaos;

import com.chaos.utils.zookeeper.ZookeeperNode;
import com.chaos.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * 注册中心的管理页面
 */
@Slf4j
public class Application {
    public static void main(String[] args) {

        try {

            // 创建一个zookeeper实例
            ZooKeeper zooKeeper = ZookeeperUtils.createZooKeeper();
            // 定义节点和数据
            String basePath = "/chaos-metadata";
            String providerPath = basePath + "/providers";
            String consumerPath = basePath + "/consumers";
            ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
            ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
            ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);
            List.of(baseNode, consumerNode, providerNode).forEach(node -> {
                ZookeeperUtils.createNode(zooKeeper,
                        node, null,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            });
            ZookeeperUtils.close(zooKeeper);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
