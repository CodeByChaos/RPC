package com.chaosrpc.channelHandler.handler;

import com.chaosrpc.ChaosrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试的类
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                ByteBuf msg) throws Exception {
        // 服务提供方，给予的结果
        String result = msg.toString(Charset.defaultCharset());
        log.info("msg---->{}", msg.toString(Charset.defaultCharset()));
        // 从全局挂起中的请求中寻找与之匹配的待处理 completableFuture
        CompletableFuture<Object> completableFuture = ChaosrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
