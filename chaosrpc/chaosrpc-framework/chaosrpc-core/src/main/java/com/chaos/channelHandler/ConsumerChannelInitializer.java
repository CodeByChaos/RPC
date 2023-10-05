package com.chaos.channelHandler;

import com.chaos.channelHandler.handler.ChaosrpcRequestEncoder;
import com.chaos.channelHandler.handler.ChaosrpcResponseDecoder;
import com.chaos.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new ChaosrpcRequestEncoder())
                // 入栈的解码器
                .addLast(new ChaosrpcResponseDecoder())
                // 处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }

}
