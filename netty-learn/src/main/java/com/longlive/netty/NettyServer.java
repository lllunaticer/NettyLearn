package com.longlive.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Anything that can go wrong will go wrong
 *
 * @author Xingjian LONG <longxingjian@kuaishou.com>
 * @date 2023-05-11
 */
public class NettyServer {
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                // 用于指定在服务端启动过程中的逻辑
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) {
                        System.out.println("服务启动中");
                    }
                })
                // 用于指定处理新连接数据的读写处理逻辑
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ct, String msg) {
                                System.out.println("[" + Thread.currentThread().getName() + "]" + msg);
                            }
                        });
                    }
                })
                // 可以给服务端channel 指定一些自定义属性，然后可以通过channel.attr()取出属性
                .attr(AttributeKey.newInstance("serverName"), "nettyServer")
                // 可以给每一个连接都指定自定义属性
                .childAttr(AttributeKey.newInstance("clientKey"), "clientValue")
                // 可以给服务端channel 设置TCP参数，下面的设置表示系统用于临时存放已完成三次握手的请求的队列的最大长度
                // 如果连接建立频繁，服务器处理创建新连接较慢，则可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 可以给每个连接设置一些TCP参数
                // TCP心跳
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 开启Nagle算法
                // Nagle 算法收集小包集中发送
                .childOption(ChannelOption.TCP_NODELAY, true)
        ;
        bind(serverBootstrap, 8000);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]" + "绑定成功");
            } else {
                System.out.println("端口[" + port + "]" + "绑定失败");
                bind(serverBootstrap, port + 1);
            }
        });
    }
}
