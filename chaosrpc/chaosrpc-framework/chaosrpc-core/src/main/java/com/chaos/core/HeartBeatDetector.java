package com.chaos.core;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.NettyBootstrapInitializer;
import com.chaos.compress.CompressFactory;
import com.chaos.discovery.Registry;
import com.chaos.enumeration.RequestType;
import com.chaos.serialize.SerializerFactory;
import com.chaos.transport.message.ChaosrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测的核心目的是什么？探活，感知哪些服务器是正常的，哪些是不正常的
 * @author Chaos Wong
 */
@Slf4j
public class HeartBeatDetector {

    public static void detectHeartBeat(String serviceName) {
        // 1.从注册中心拉取服务列表并建立连接
        Registry registry = ChaosrpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);

        // 2.将连接进行缓存
        for (InetSocketAddress address : addresses) {
            try {
                if(!ChaosrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap()
                            .connect(address)
                            .sync().channel();
                    ChaosrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 3.任务，定期发送消息
        Thread thread = new Thread(() ->
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000)
        , "chaosrpc-HeartBeatDetector-thread");
        thread.setDaemon(true);
        thread.start();

    }

    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // 将响应时长的map清空
            ChaosrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = ChaosrpcBootstrap.CHANNEL_CACHE;
            for(Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                // 定义一个重试的次数
                int tryTimes = 3;
                while (tryTimes > 0) {
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();
                    // 构建一个心跳请求
                    ChaosrpcRequest chaosrpcRequest = ChaosrpcRequest.builder()
                            .requestId(ChaosrpcBootstrap.ID_GENERATOR.getId())
                            .compressType(CompressFactory.getCompress(ChaosrpcBootstrap.COMPRESS_TYPE).getCode())
                            .requestType(RequestType.HEARTBEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(ChaosrpcBootstrap.SERIALIZE_TYPE).getCode())
                            .timeStamp(start)
                            .build();

                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 需要将 completableFuture 暴露出去
                    ChaosrpcBootstrap.PENDING_REQUEST.put(chaosrpcRequest.getRequestId(), completableFuture);
                    channel.writeAndFlush(chaosrpcRequest)
                            .addListener((ChannelFutureListener) promise -> {
                                if (!promise.isSuccess()) {
                                    completableFuture.completeExceptionally(promise.cause());
                                }
                            });
                    long endTime = 0L;
                    try {
                        // 阻塞方法，get()方法如果得不到结果，就会一直阻塞
                        // 我们想不一直阻塞，可以添加参数
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        // 一旦发生问题，需要优先重试
                        tryTimes--;
                        log.error("和地址为{}的主机连接发生异常,正在进行第{}次重试.", channel.remoteAddress(), 3 - tryTimes);
                        // 将重试的机会用尽，将失效的地址移出服务列表
                        if(tryTimes == 0) {
                            ChaosrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        // 尝试等待一段时间后重试
                        try {
                            Thread.sleep(10 * new Random().nextInt(5));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    // 使用treemap进行缓存
                    ChaosrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("和{}服务器的响应时间是{}ms", entry.getKey(), time);
                    break;
                }
            }

            log.info("---------------------响应时间的TreeMap---------------------");
            for(Map.Entry<Long, Channel> entry : ChaosrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()){
                if(log.isDebugEnabled()) {
                    log.debug("{}---->channelId:{}.", entry.getKey(), entry.getValue().id());
                }
            }
        }
    }
}
