package com.longlive.netty;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;

/**
 * Anything that can go wrong will go wrong
 *
 * @author Xingjian LONG <longxingjian@kuaishou.com>
 * @date 2023-05-11
 */
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        //指定线程模型
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 指定IO模型
                .channel(NioSocketChannel.class)
                .attr(AttributeKey.newInstance("clientName"), "nettyClient")
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                // IO处理逻辑
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                });
        Channel channel = connect(bootstrap, "127.0.0.1", 8000, MAX_RETRY);
        // 建立连接
        while (true) {
            channel.writeAndFlush(new Date() + ": hello world!");
            Thread.sleep(2000);
        }
    }


    private static final int MAX_RETRY = 3;

    // 指数退避客户端重连
    private static Channel connect(final Bootstrap bootstrap, final String host, final int port, int retry) {
        return bootstrap.connect(host, port).addListener(
                future -> {
                    if (future.isSuccess()) {
                        System.out.println("连接成功!");
                    } else if (retry == 0) {
                        System.out.println("重试次数已用完，放弃连接!");
                    } else {
                        int order = MAX_RETRY - retry + 1;
                        int delay = 1 << order;
                        System.out.println(new Date() + ": 连接失败，第" + order + "次重连......");
                        bootstrap.config().group()
                                .schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
                    }
                }
        ).channel();
    }
}
