package com.chaosrpc;

import com.chaosrpc.ChaosrpcBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 提供Bootstrap单例
 * todo: 这里会有什么问题？
 */
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
                        channel.pipeline().addLast(null);
                    }
                });
    }

    private NettyBootstrapInitializer() {

    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
