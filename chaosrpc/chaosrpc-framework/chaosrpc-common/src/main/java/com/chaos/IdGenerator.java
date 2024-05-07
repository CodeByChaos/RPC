package com.chaos;

import com.chaos.utils.DateUtils;

import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

/**
 * 请求id生成器
 * @author WongYut
 */
public class IdGenerator {

// 这是单机版本的线程安全的id发号器，一旦变成集群状态，就失效了
/*    private static LongAdder longAdder = new LongAdder();

    public static long getId() {
        longAdder.increment();
        return longAdder.sum();
    }*/

    // 雪花算法 -- 世界上没有一片雪花是一样的
    // 机房号(数据中心)  5bit 32
    // 机器号          5bit 32
    // 时间戳(long 1970-1-1 00:00:00) 原本64位表示时间，必须减少（64），自由选择一个比较近的时间
    // 如公司成立时间
    // 同一个机房的同一个机器号的同一个时间可能因为并发量很大需要多个id
    // 序列号 12bit 5+5+42+12 = 64

    // 起始时间戳
    public static final long START_STAMP = DateUtils.get("2022-1-1").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

    // 最大值
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    // 时间戳(42) 机房号(5) 机器号(5) 序列号(12)
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
//    private long sequenceId = 0L;

    // 解决线程安全问题
    private LongAdder sequenceId = new LongAdder();

    // 时钟回拨问题
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        // 判断传入的参数是否合法
        if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("您传入的数据中心编号或机器编号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        // 1.处理时间戳的问题
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;
        // 判断时间回拨
        if(timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时钟回拨");
        }
        // sequenceId需要做一些处理，如果是同一个时间节点，必须自增
        if(timeStamp == lastTimeStamp) {
//            sequenceId++;
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        // 执行结束后将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT
                | dataCenterId << DATA_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    private long getNextTimeStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        // 如果一样就一直循环，直到下一个时间戳
        while(current == lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

}
