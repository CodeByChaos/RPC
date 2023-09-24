package com.chaosrpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 提供Bootstrap单例
 * todo: 这里会有什么问题？
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
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
                        });
                    }
                });
    }

    private NettyBootstrapInitializer() {

    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
