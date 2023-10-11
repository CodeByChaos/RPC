package com.chaos.channelHandler.handler;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.enumeration.ResponseCode;
import com.chaos.exceptions.ResponseCodeException;
import com.chaos.protection.CircuitBreaker;
import com.chaos.transport.message.ChaosrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试的类
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ChaosrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                ChaosrpcResponse chaosrpcResponse) throws Exception {
        // 从全局挂起中的请求中寻找与之匹配的待处理 completableFuture
        CompletableFuture<Object> completableFuture = ChaosrpcBootstrap
                .PENDING_REQUEST
                .get(chaosrpcResponse.getRequestId());

        SocketAddress address = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = ChaosrpcBootstrap
                .getInstance()
                .getConfiguration()
                .getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
        byte code = chaosrpcResponse.getCode();
        if(code == ResponseCode.FAIL.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id:{}的请求，返回错误的结果，响应码:{}.",
                    chaosrpcResponse.getRequestId(), chaosrpcResponse.getCode());
            throw new ResponseCodeException(code, ResponseCode.FAIL.getDescription());
        } else if(code == ResponseCode.RATE_LIMITER.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id:{}的请求，被限流，响应码:{}.",
                    chaosrpcResponse.getRequestId(), chaosrpcResponse.getCode());
            throw new ResponseCodeException(code, ResponseCode.RATE_LIMITER.getDescription());
        } else if(code == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id:{}的请求，未找到目标资源，响应码:{}.",
                    chaosrpcResponse.getRequestId(), chaosrpcResponse.getCode());
            throw new ResponseCodeException(code, ResponseCode.RESOURCE_NOT_FOUND.getDescription());
        } else if(code == ResponseCode.SUCCESS_HEARTBEAT.getCode()) {
            completableFuture.complete(null);
            if(log.isDebugEnabled()){
                log.debug("已经寻找到编号为{}的completableFuture，处理心跳检测.", chaosrpcResponse.getRequestId());
            }
        } else if(code == ResponseCode.SUCCESS.getCode()) {
            // 服务提供方，给予的结果
            Object returnValue = chaosrpcResponse.getBody();
            // todo 需要针对code做处理
            returnValue = returnValue == null ? new Object() : returnValue;

            completableFuture.complete(returnValue);
            if(log.isDebugEnabled()) {
                log.debug("已经寻找到编号为{}的completableFuture，处理响应结果.", chaosrpcResponse.getRequestId());
            }
        }
    }
}
