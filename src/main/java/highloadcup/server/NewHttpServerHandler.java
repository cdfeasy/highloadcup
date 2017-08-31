package highloadcup.server;

import highloadcup.service.ClientApi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by dmitry on 20.08.2017.
 */
public class NewHttpServerHandler extends SimpleChannelInboundHandler {
    private final Logger logger = LoggerFactory.getLogger(NewHttpServerHandler.class);
    private ClientApi api;

    private static DefaultFullHttpResponse defaultOkClose;
    private static DefaultFullHttpResponse defaultOkAlive;
    private static DefaultFullHttpResponse default400Close;
    private static DefaultFullHttpResponse default400Alive;
    private static DefaultFullHttpResponse default404Close;
    private static DefaultFullHttpResponse default404Alive;

    static {
        defaultOkClose = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(200), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        defaultOkClose.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        defaultOkClose.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, defaultOkClose.content().readableBytes());
        defaultOkClose.headers().set(CONNECTION, HttpHeaderValues.CLOSE);

        defaultOkAlive = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(200), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        defaultOkAlive.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        defaultOkAlive.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, defaultOkAlive.content().readableBytes());
        defaultOkAlive.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        default400Close = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(400), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        default400Close.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        default400Close.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, default400Close.content().readableBytes());
        default400Close.headers().set(CONNECTION, HttpHeaderValues.CLOSE);

        default400Alive = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(400), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        default400Alive.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        default400Alive.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, default400Alive.content().readableBytes());
        default400Alive.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        default404Close = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(404), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        default404Close.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        default404Close.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, default404Close.content().readableBytes());
        default404Close.headers().set(CONNECTION, HttpHeaderValues.CLOSE);

        default404Alive = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(404), Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8));
        default404Alive.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
        default404Alive.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, default404Alive.content().readableBytes());
        default404Alive.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    public NewHttpServerHandler(ClientApi api) {
        this.api = api;
    }
    public void   read(ChannelHandlerContext ctx, Object msg) throws Exception {
        channelRead0(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //logger.info("thread "+Thread.currentThread().getName());
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            try {
                boolean keepAlive = HttpUtil.isKeepAlive(req);
                ClientApi.Response resp = ApiHandler.transfer(api, ctx, msg);
                FullHttpResponse response = null;
                if (resp.getStatus() == 200 && resp.getResponse() != null && resp.getResponse().length!=2) {
                    response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(resp.getStatus()), Unpooled.wrappedBuffer(resp.getResponse()));
                    response.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
                    response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    if (keepAlive) {
                        response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    } else {
                        response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
                    }
                } else if (resp.getStatus() == 200) {
                    if (keepAlive) {
                        response = defaultOkAlive.duplicate().retain();
                    } else {
                        response = defaultOkClose.duplicate().retain();
                    }
                } else if (resp.getStatus() == 400) {
                    if (keepAlive) {
                        response = default400Alive.duplicate().retain();
                    } else {
                        response = default400Close.duplicate().retain();
                    }
                } else  {
                    if (keepAlive) {
                        response = default404Alive.duplicate().retain();
                    } else {
                        response = default404Close.duplicate().retain();
                    }
                }
                if (keepAlive) {
                    ctx.write(response);
                } else {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                }
            } finally {
                req.release();
            }
        }else {
            FullHttpResponse resp=defaultOkAlive.duplicate().retain();
            ctx.write(resp);
            //((ByteBuf)msg).release();
           // ctx.fireChannelRead(resp);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        logger.error("ctx close!",cause);
    }

}
