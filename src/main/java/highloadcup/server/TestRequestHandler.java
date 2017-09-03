package highloadcup.server;

import highloadcup.service.ClientApi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.rapidoid.commons.Str;

import java.nio.charset.Charset;

/**
 * Created by d.asadullin on 31.08.2017.
 */
public class TestRequestHandler extends ChannelInboundHandlerAdapter {
    private static ByteBuf defaultOkClose;
    private static ByteBuf defaultOkAlive;
    private static ByteBuf default400Close;
    private static ByteBuf default400Alive;
    private static ByteBuf default404Close;
    private static ByteBuf default404Alive;
    private static byte[] responseStart;

    static {
        defaultOkClose = Unpooled.wrappedBuffer(("HTTP/1.1 200 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "content-length: 2\r\n" +
                "connection: close\r\n" +
                "\r\n" +
                "{}").getBytes());

        defaultOkAlive = Unpooled.wrappedBuffer(("HTTP/1.1 200 OK\r\n" +
                "content-type: application/json; charset=utf-\r\n" +
                "content-length: 2\r\n" +
                "connection: keep-alive\r\n" +
                "\r\n" +
                "{}").getBytes());

        default400Close = Unpooled.wrappedBuffer(("HTTP/1.1 400 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "content-length: 2\r\n" +
                "connection: close\r\n" +
                "\r\n" +
                "{}").getBytes());

        default400Alive = Unpooled.wrappedBuffer(("HTTP/1.1 400 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "content-length: 2\r\n" +
                "connection: keep-alive\r\n" +
                "\r\n" +
                "{}").getBytes());

        default404Close = Unpooled.wrappedBuffer(("HTTP/1.1 404 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "content-length: 2\r\n" +
                "connection: close\r\n" +
                "\r\n" +
                "{}").getBytes());
        default404Alive = Unpooled.wrappedBuffer(("HTTP/1.1 404 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "content-length: 2\r\n" +
                "connection: keep-alive\r\n" +
                "\r\n" +
                "{}").getBytes());

        responseStart = ("HTTP/1.1 200 OK\r\n" +
                "content-type: application/json; charset=utf-8\r\n" +
                "connection: keep-alive\r\n" +
                "content-length: ").getBytes();
    }


    public ClientApi handler;

    public TestRequestHandler(ClientApi handler) {
        this.handler = handler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            try {
                boolean keepAlive = true;
                ClientApi.Response resp = RequestParser.parse(handler, (ByteBuf) msg);
                ByteBuf response = null;
                if (resp.getStatus() == 200 && resp.getResponse() != null) {
                    ByteBuf buf=Unpooled.buffer();
                    buf.writeBytes(responseStart);
                    buf.writeBytes(Integer.toString(resp.getResponse().length).getBytes());
                    buf.writeBytes(("\r\n\r\n").getBytes());
                    buf.writeBytes(resp.getResponse());
                    response = buf;
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
                } else {
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
                ((ByteBuf) msg).release();
//                ReferenceCountUtil.release(msg);
            }
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
