package highloadcup.server;

import highloadcup.service.ClientApi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.rapidoid.commons.Str;

import java.nio.charset.Charset;

import static io.netty.handler.codec.ByteToMessageDecoder.MERGE_CUMULATOR;

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
    ByteBuf cumulation;
    private boolean first;private int numReads;
    private ByteToMessageDecoder.Cumulator cumulator = MERGE_CUMULATOR;

    public TestRequestHandler(ClientApi handler) {
        this.handler = handler;
    }


    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = cumulation;
        if (buf != null) {
            // Directly set this to null so we are sure we not access it in any other method here anymore.
            cumulation = null;

            int readable = buf.readableBytes();
            if (readable > 0) {
                ByteBuf bytes = buf.readBytes(readable);
                buf.release();
                ctx.fireChannelRead(bytes);
            } else {
                buf.release();
            }
            numReads = 0;
            ctx.fireChannelReadComplete();
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.flush();
        numReads = 0;
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            first = cumulation == null;
            if (first) {
                cumulation = data;
            } else {
                cumulation = cumulator.cumulate(ctx.alloc(), cumulation, data);
            }

            try {
                boolean keepAlive = true;
                ClientApi.Response resp = RequestParser.parse(handler, (ByteBuf) cumulation);
                ByteBuf response = null;
                if (resp.getStatus() == 200 && resp.getResponse() != null) {
                    ByteBuf buf = Unpooled.buffer();
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
                } else if (resp.getStatus() == 404) {
                    if (keepAlive) {
                        response = default404Alive.duplicate().retain();
                    } else {
                        response = default404Close.duplicate().retain();
                    }
                } else {
                    response = null;
                }
                if (response != null) {
                    if (keepAlive) {
                        ctx.write(response);
                        //((ByteBuf) msg).release();
                       // ctx.fireChannelReadComplete();
                    } else {
                        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                      //  ((ByteBuf) msg).release();
                       // ctx.fireChannelReadComplete();
                    }
                }
            } finally {
                if (cumulation != null && !cumulation.isReadable()) {
                    numReads = 0;
                    cumulation.release();
                    cumulation = null;
                }

                //((ByteBuf) msg).clear();
//                ReferenceCountUtil.release(msg);
            }
//            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
