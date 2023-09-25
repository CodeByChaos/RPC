package com.chaosrpc.channelHandler.handler;

import com.chaosrpc.ChaosrpcBootstrap;
import com.chaosrpc.ServiceConfig;
import com.chaosrpc.transport.message.ChaosrpcRequest;
import com.chaosrpc.transport.message.RequestPlayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<ChaosrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                ChaosrpcRequest chaosrpcRequest) throws Exception {
        // 1.获取负载内容
        RequestPlayload requestPlayload = chaosrpcRequest.getRequestPlayload();
        // 2.根据负载内容进行方法调用
        Object object = callTargetMethod(requestPlayload);
        // 3.封装响应

        // 4.写出响应
        channelHandlerContext.channel().writeAndFlush(object);

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
