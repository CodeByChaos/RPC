package com.chaos;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ZookeeperTest {
    ZooKeeper zooKeeper;

    /**
     * 初始化zookeeper
     */
    @Before
    public void createZK(){
        // 定义连接参数
        String connectionString = "127.0.0.1:2181";
        // 定义超时时间
        int timeout = 10000;
        try {
            zooKeeper = new ZooKeeper(connectionString, timeout, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个持久节点
     */
    @Test
    public void testCreatePNode() {
        try{
            String result = zooKeeper.create("/chaos",
                    "hello".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除一个持久节点
     */
    @Test
    public void testDeletePNode() {
        try{
            // version: cas mysql 乐观锁, 也可以无视版本号 -1
            zooKeeper.delete("/chaos", -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取节点的版本号
     */
    @Test
    public void testExistsPNode() {
        try{
            // version: cas mysql 乐观锁, 也可以无视版本号 -1
            Stat stat = zooKeeper.exists("/chaos", null);
            zooKeeper.setData("/chaos", "hi".getBytes(), -1);
            // 当前节点的数据版本
            int version = stat.getVersion();
            System.out.println("version = " + version);
            // 当前节点的acl数据版本
            int aversion = stat.getAversion();
            System.out.println("aversion = " + aversion);
            // 当前子节点数据的版本
            int cversion = stat.getCversion();
            System.out.println("cversion = " + cversion);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
