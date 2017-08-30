/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package highloadcup.server;

import highloadcup.service.ClientApi;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * An HTTP internal that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HttpServer {

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    private ClientApi api;
    private Channel ch;
    private int port;

    public HttpServer(ClientApi api, int port) {
        this.api = api;
        this.port = port;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();


        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new NewHttpServerInit(api));

        ch = b.bind(port).sync().channel();

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
    }
}
