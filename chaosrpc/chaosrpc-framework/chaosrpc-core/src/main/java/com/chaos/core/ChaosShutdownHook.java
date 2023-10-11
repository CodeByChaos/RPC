package com.chaos.core;

/**
 * @author Chaos Wong
 */
public class ChaosShutdownHook extends Thread{
    @Override
    public void run() {
        // 1.打开挡板（boolean 需要线程安全）
        ShutdownHolder.BAFFLE.set(true);
        // 2.等待计数器归零（正常的请求处理结束）
        // 等待归零，继续执行 countdownLatch 最多等待10s
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShutdownHolder.REQUEST_COUNTER.sum() == 0L
                    || System.currentTimeMillis() - start > 10000) {
                break;
            }
        }
        // 3.阻塞结束后，放行。执行其他操作，如释放资源

    }
}
