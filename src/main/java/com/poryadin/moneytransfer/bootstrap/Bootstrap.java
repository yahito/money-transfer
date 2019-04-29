package com.poryadin.moneytransfer.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import com.poryadin.moneytransfer.routing.Router;

import java.net.InetSocketAddress;

public class Bootstrap {
    private EventLoopGroup loopGroup;
    private Channel channel;

    public void start(Router router, int port) throws Exception {
        Class<? extends ServerChannel> serverChannelClass;
        if (Epoll.isAvailable()) {
            loopGroup = new EpollEventLoopGroup();
            serverChannelClass = EpollServerSocketChannel.class;
        } else {
            loopGroup = new NioEventLoopGroup();
            serverChannelClass = NioServerSocketChannel.class;
        }
        start(loopGroup, serverChannelClass, router, port);
    }

    private void start(
            final EventLoopGroup loopGroup,
            final Class<? extends ServerChannel> serverChannelClass,
            Router router,
            int port)
            throws InterruptedException {

        try {
            final InetSocketAddress address = new InetSocketAddress(port);

            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(loopGroup).channel(serverChannelClass).childHandler(new WebServerInitializer(router));
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
            channel = bootstrap.bind(address).sync().channel();
            channel.closeFuture().sync();
        } finally {
            loopGroup.shutdownGracefully().sync();
        }
    }


    private class WebServerInitializer extends ChannelInitializer<SocketChannel> {

        private Router router;

        public WebServerInitializer(Router router) {
            this.router = router;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline p = ch.pipeline();
            p.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
            p.addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
            p.addLast("encoder", new HttpResponseEncoder());
            p.addLast("handler", new HttpRequestHandler(router));
        }

    }

    public void stop() {
        try {
            loopGroup.shutdownGracefully().sync();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
