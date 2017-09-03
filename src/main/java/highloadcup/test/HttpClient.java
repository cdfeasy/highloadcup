package highloadcup.test;
import highloadcup.server.HttpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.rapidoid.commons.Str;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpServer}.
 */
public final class HttpClient {
    String host = "127.0.0.1";
    EventLoopGroup group;
    Bootstrap b;
    int port;
    AtomicInteger cnt, success;
    public HttpClient(int port, AtomicInteger cnt,AtomicInteger success){
        this.port=port;
        this.cnt=cnt;
        this.success=success;
        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpClientInitializer(success))
                    .option(ChannelOption.SO_KEEPALIVE, true);

        } finally {
        }

    }
    public void close(){
        try{
            group.shutdownGracefully();
        }  catch (Exception ex){
            try {
                group.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public String get(String url) {
        try {
            cnt.incrementAndGet();
            String URL = url.replace("http://127.0.0.1:" + port, "");
            Channel ch = b.connect(host, port).sync().channel();
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, URL, Unpooled.copiedBuffer("".getBytes()));
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
            ChannelFuture channelFuture = ch.writeAndFlush(request);
            channelFuture.await(1000);
            return "";
        }catch (Exception ex){
            //
            return "";
        }
    }
    public String post(String url, String body) throws InterruptedException {
        cnt.incrementAndGet();
        String URL=url.replace("http://127.0.0.1:"+port,"");
        Channel ch = b.connect(host, port).sync().channel();
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, URL, Unpooled.copiedBuffer(body.getBytes()));
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        ChannelFuture channelFuture = ch.writeAndFlush(request);
        channelFuture.await(1000);
        Thread.sleep(100);
        return "";
    }

}