package com.chaos.protection;

/**
 * 基于令牌桶算法的限流器
 * @author Chaos Wong
 */
public class TokenBucketRateLimiter {
    // 思考？令牌是个啥？令牌桶是个啥？
    // String? Object? list? map?

    // 代表令牌的数量，大于零说明有令牌，能放行，放行就减一，等于零，无令牌，阻拦
    private int tokens;

    // 限流的本质是，令牌数
    private final int capacity;

    // 令牌桶的令牌，如果没了要怎么办？按照一定的速率给令牌桶价令牌，如每秒加500个，不能超过总数
    // 可以用定时任务去加 ----> 启动一个定时任务，每秒执行一次，tokens+500 < capacity (不合理)
    // 单机版的限流器可以有更简单的操作，每一个有请求要发送的时候给他加一下
    private final int rate;

    // 上一次放令牌的时间
    private Long lastTokenTime;

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    /**
     * 判断请求是否可以放行
     * @return true 放行 false 拦截
     */
    public synchronized boolean allowRequest(){
        // 1.给令牌桶添加令牌
        // 计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        // 如果间隔时间超过1s，放令牌
        if(timeInterval >= 1000 / rate) {
            int needAddTokens = (int) (timeInterval * rate / 1000);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            // 标记最后一次放入令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }

        // 2.自己获取令牌，如果令牌桶中有令牌则放行，否则拦截
        if(tokens > 0) {
            tokens--;
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 10);
        for (int i = 0; i < 1000; i++) {
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}
