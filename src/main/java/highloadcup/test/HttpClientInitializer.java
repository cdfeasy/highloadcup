package highloadcup.test;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
    private AtomicInteger success;

    public HttpClientInitializer(AtomicInteger success) {
        this.success=success;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpClientCodec());

        p.addLast(new HttpContentDecompressor());
        p.addLast(new HttpObjectAggregator(1048576));

        p.addLast(new HttpClientHandler(success));
    }
}