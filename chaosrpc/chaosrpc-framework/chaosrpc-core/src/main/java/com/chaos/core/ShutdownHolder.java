package com.chaos.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Chaos Wong
 */
public class ShutdownHolder {

    // 用来标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
