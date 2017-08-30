package highloadcup.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    static Logger logger = LoggerFactory.getLogger(Sender.class);
    private AtomicInteger success;

    public HttpClientHandler(AtomicInteger success) {
        this.success = success;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        try {
            if (msg instanceof FullHttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                HttpContent content = (HttpContent) msg;
              //  logger.info("content=" + content.content().toString(CharsetUtil.UTF_8));
                success.incrementAndGet();
            }
        } finally {
           // ((FullHttpResponse) msg).release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}