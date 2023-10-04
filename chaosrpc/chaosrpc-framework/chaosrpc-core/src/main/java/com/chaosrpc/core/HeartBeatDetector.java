package com.chaosrpc.core;

import com.chaosrpc.ChaosrpcBootstrap;
import com.chaosrpc.NettyBootstrapInitializer;
import com.chaosrpc.compress.CompressFactory;
import com.chaosrpc.discovery.Registry;
import com.chaosrpc.enumeration.RequestType;
import com.chaosrpc.serialize.SerializerFactory;
import com.chaosrpc.transport.message.ChaosrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
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
                    completableFuture.get();
                    endTime = System.currentTimeMillis();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                Long time = endTime - start;
                // 使用treemap进行缓存
                ChaosrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                log.debug("和{}服务器的响应时间是{}ms", entry.getKey(), time);
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
