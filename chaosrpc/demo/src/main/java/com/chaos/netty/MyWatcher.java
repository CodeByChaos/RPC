package com.chaos.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        // 判断事件类型，连接类型的事件
        if(watchedEvent.getType() == Event.EventType.None) {
            if(watchedEvent.getState().equals(Event.KeeperState.SyncConnected)) {
                System.out.println("zookeeper连接成功");
            } else if (watchedEvent.getState().equals(Event.KeeperState.AuthFailed)){
                System.out.println("zookeeper认证失败");
            } else if (watchedEvent.getState().equals(Event.KeeperState.Disconnected)) {
                System.out.println("zookeeper断开连接");
            }
        } else if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            System.out.println(watchedEvent.getPath() + "被创建了");
        } else if (watchedEvent.getType().equals(Event.EventType.NodeDeleted)) {
            System.out.println(watchedEvent.getPath() + "被删除了");
        } else if (watchedEvent.getType().equals(Event.EventType.NodeDataChanged)) {
            System.out.println(watchedEvent.getPath() + "数据发生了变化");
        } else if (watchedEvent.getType().equals(Event.EventType.NodeChildrenChanged)) {
            System.out.println(watchedEvent.getPath() + "子节点发生了变化");
        }
    }
}
