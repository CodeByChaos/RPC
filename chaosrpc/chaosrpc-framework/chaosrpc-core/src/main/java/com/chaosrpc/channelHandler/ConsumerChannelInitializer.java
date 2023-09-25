package com.chaosrpc.channelHandler;

import com.chaosrpc.channelHandler.handler.ChaosrpcMessageEncoder;
import com.chaosrpc.channelHandler.handler.MySimpleChannelInboundHandler;
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
                .addLast(new ChaosrpcMessageEncoder())
                .addLast(new MySimpleChannelInboundHandler());
    }

}
