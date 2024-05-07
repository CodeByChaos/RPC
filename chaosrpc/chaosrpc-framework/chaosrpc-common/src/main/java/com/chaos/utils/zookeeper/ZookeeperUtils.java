package com.chaos.utils.zookeeper;

import com.chaos.Constant;
import com.chaos.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author WongYut
 */
@Slf4j
public class ZookeeperUtils {

    /**
     * 使用默认配置创建zookeeper实例
     * @return zookeeper实例
     */
    public static ZooKeeper createZooKeeper() {
        // 定义连接参数
        String connectionString = Constant.DEFAULT_ZK_CONNECTION;
        // 定义超时时间
        int timeout = Constant.TIME_OUT;
        return createZooKeeper(connectionString, timeout);
    }

    /**
     * 使用自定义配置创建zookeeper实例
     * @param connectionString 连接参数
     * @param timeout 超时时间
     * @return zookeeper
     */
    public static ZooKeeper createZooKeeper(String connectionString, int timeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 创建zookeeper实例，建立连接
            final ZooKeeper zooKeeper = new ZooKeeper(connectionString, timeout, watchedEvent -> {
                // 只有连接成功才放行
                if(watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("客户端已经连接成功。");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zooKeeper zookeeper实例
     * @param node 节点实例 path data
     * @param watcher watcher实例
     * @param acl acl权限
     * @param createMode 节点的类型
     * @return true:success false:failed or exist exception:throwed
     */
    public static Boolean createNode(ZooKeeper zooKeeper,
                                       ZookeeperNode node,
                                       Watcher watcher,
                                       List<ACL> acl,
                                       CreateMode createMode) {
        try {
            if(zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(),
                        node.getData(),
                        acl,
                        createMode);
                log.info("{}节点创建完成", result);
                return true;
            } else {
                if(log.isDebugEnabled()) {
                    log.info("节点{}已经存在，无需创建", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录发生异常", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭zookeeper
     * @param zooKeeper zookeeper实例
     */
    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时出现问题", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     * @param zooKeeper zookeeper实例
     * @param node 节点路径
     * @param watcher watcher
     * @return true:exist false:not exist
     */
    public static boolean exists(ZooKeeper zooKeeper, String node, Watcher watcher) {
        try {
            return zooKeeper.exists(node, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点{}是否存在时发生异常", node, e);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 查询一个节点的子元素
     * @param zooKeeper zookeeper实例
     * @param serviceNode 服务节点
     * @return 资源数列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点{}的子元素发生异常",serviceNode, e);
            throw new ZookeeperException(e);
        }
    }
}
