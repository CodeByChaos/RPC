package com.chaos.channelHandler.handler;

import com.chaos.ChaosrpcBootstrap;
import com.chaos.ServiceConfig;
import com.chaos.enumeration.RequestType;
import com.chaos.enumeration.ResponseCode;
import com.chaos.protection.RateLimiter;
import com.chaos.protection.TokenBucketRateLimiter;
import com.chaos.transport.message.ChaosrpcRequest;
import com.chaos.transport.message.ChaosrpcResponse;
import com.chaos.transport.message.RequestPlayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<ChaosrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                ChaosrpcRequest chaosrpcRequest) throws Exception {
        // 1.先封装部分响应
        ChaosrpcResponse chaosrpcResponse = new ChaosrpcResponse();
        chaosrpcResponse.setRequestId(chaosrpcRequest.getRequestId());
        chaosrpcResponse.setCompressType(chaosrpcRequest.getCompressType());
        chaosrpcResponse.setSerializeType(chaosrpcRequest.getSerializeType());
        // 2.完成限流相关的操作
        Channel channel = channelHandlerContext.channel();
        SocketAddress address = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                ChaosrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(address);
        if(rateLimiter == null) {
            rateLimiter = new TokenBucketRateLimiter(20, 20);
            everyIpRateLimiter.put(address, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();
        // 限流
        if(!allowRequest) {
            // 需要封装响应并且返回了
            chaosrpcResponse.setCode(ResponseCode.RATE_LIMITER.getCode());
        }
        // 处理心跳
        else if(chaosrpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()){
            chaosrpcResponse.setCode(ResponseCode.SUCCESS_HEARTBEAT.getCode());
        }
        // 正常调用
        else {
            // 1.获取负载内容
            RequestPlayload requestPlayload = chaosrpcRequest.getRequestPlayload();
            // 2.根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPlayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求{}已经在服务端完成方法调用.", chaosrpcRequest.getRequestId());
                }

                // 3.封装响应 我们是否需要考虑另外一个问题，响应码，响应类型
                chaosrpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                chaosrpcResponse.setBody(result);
            } catch (Exception e) {
                log.error("请求id:{}在调用过程发生异常.", chaosrpcRequest.getRequestId() ,e);
                chaosrpcResponse.setCode(ResponseCode.FAIL.getCode());
            }
            // 4.写出响应

        }
        channel.writeAndFlush(chaosrpcResponse);
    }

    private Object callTargetMethod(RequestPlayload requestPlayload) {
        String interfaceName = requestPlayload.getInterfaceName();
        String methodName = requestPlayload.getMethodName();
        Class<?>[] parametersType = requestPlayload.getParametersType();
        Object[] parametersValue = requestPlayload.getParametersValue();

        // 寻找到暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = ChaosrpcBootstrap.SERVICE_LISTS.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 通过反射调用 1.获取方法对象 2.执行invoke方法
        Class<?> clazz = refImpl.getClass();
        Object returnValue;
        try {
            Method method = clazz.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("调用服务{}的方法{}发送了异常",interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
