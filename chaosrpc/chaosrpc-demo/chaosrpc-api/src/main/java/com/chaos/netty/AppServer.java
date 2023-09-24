package com.chaos.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class AppServer {

    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() {
        // 1.创建eventLoop，老板只负责处理请求，之后会将请求分发至worker
        // 官方默认boss:worker 1:5
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            // 2.需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3.配置服务器
            serverBootstrap.group(boss, worker)
                    // 通过工厂方法设计模式实例化一个channel
                    .channel(NioServerSocketChannel.class)
                    // 设置监听端口
                    .localAddress(new InetSocketAddress(port))
                    // ChannelInitializer是一个特殊的处理类，
                    // 他的目的是帮助使用者配置一个新的Channel，
                    // 用于把许多自定义的处理类增加到pipeline上来
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 配置childHandler来通知一个关于消息处理的InfoServerHandler实例
                            socketChannel.pipeline().addLast(new MyChannelHandler());
                        }
                    });

            // 4.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("在" + channelFuture.channel().localAddress() + "上开启监听");
            // 阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            // closeFuture().sync()会阻塞当前线程，直到通道关闭操作完成。这可以用于确保在关闭通道之前，程序不会提前退出。
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppServer(8080).start();
    }
}
