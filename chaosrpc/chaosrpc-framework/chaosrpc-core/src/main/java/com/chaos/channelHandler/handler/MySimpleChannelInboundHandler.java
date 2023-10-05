package com.chaos.channelHandler.handler;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.transport.message.ChaosrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试的类
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ChaosrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                ChaosrpcResponse chaosrpcResponse) throws Exception {
        // 服务提供方，给予的结果
        Object returnValue = chaosrpcResponse.getBody();
        // todo 需要针对code做处理
        returnValue = returnValue == null ? new Object() : returnValue;
        // 从全局挂起中的请求中寻找与之匹配的待处理 completableFuture
        CompletableFuture<Object> completableFuture = ChaosrpcBootstrap
                .PENDING_REQUEST
                .get(chaosrpcResponse.getRequestId());
        completableFuture.complete(returnValue);
        if(log.isDebugEnabled()) {
            log.debug("已经寻找到编号为{}的completableFuture，处理响应结果.", chaosrpcResponse.getRequestId());
        }
    }
}
