package highloadcup.server;

import highloadcup.service.ClientApi;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.TimeUnit;

/**
 * Created by d.asadullin on 25.08.2017.
 */
public class EpollHttpServer {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ServerBootstrap b;
    private ClientApi api;
    private Channel ch;
    private int port;

    public EpollHttpServer(ClientApi api, int port) {
        this.api = api;
        this.port = port;
    }

    public void start() throws InterruptedException {
        bossGroup = new EpollEventLoopGroup(1);
        workerGroup = new EpollEventLoopGroup();

        b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.option(ChannelOption.SO_KEEPALIVE,true);
        b.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .childHandler(new NewHttpServerInit(api));

        ch = b.bind(port).sync().channel();
        final ChannelGroup channels =
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        System.err.println("Open your web browser and navigate to " +
                "http" + "://127.0.0.1:" + port + '/');

        // ch.closeFuture().sync();
    }

    public void join() throws InterruptedException {
        ch.closeFuture().sync();
    }

    public void stop() {
        ch.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        try {
            bossGroup.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //
        }
        try {
            workerGroup.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //
        }
    }
}
