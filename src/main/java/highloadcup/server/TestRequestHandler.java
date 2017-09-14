package highloadcup.server;

import highloadcup.server.RequestParser;
import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by d.asadullin on 31.08.2017.
 */
public class TestRequestHandler extends ByteToMessageDecoder {
    private static class ByteBufPool {
        static LinkedBlockingQueue<ByteBuf> queue = new LinkedBlockingQueue<>(1);

        static {
            for (int i = 0; i < 1; i++) {
                queue.add(Unpooled.buffer(1024 * 90, 1024 * 90));
            }
        }

        public static ByteBuf getByteBuf() throws InterruptedException {
            return queue.poll(100, TimeUnit.SECONDS);
        }

        public static void releaseBuf(ByteBuf buf) {
            buf.clear();
            queue.add(buf);
        }
    }


    private static Logger logger = LoggerFactory.getLogger(RequestParser.class);
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


    //    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.flush();
//    }
    //ByteBuf copyBuf = Unpooled.buffer(1024 * 1024 * 10);
    ByteBuf copyBuf =null;
    private long end = 0;
    private volatile boolean respSended = false;
    private volatile boolean lastSended = false;

//    @Override
//    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
//        if (copyBuf != null) {
//            ctx.write(default400Alive.duplicate().retain());
//            ctx.fireChannelReadComplete();
//            ctx.flush();
//        }
    //   }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            if (respSended) {
            } else {
                if(!lastSended) {
                    ByteBuf response = default400Alive.duplicate().retain();
                    ctx.write(response);
                }
            }
            if(copyBuf!=null) {
                ByteBufPool.releaseBuf(copyBuf);
                copyBuf=null;
            }
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            respSended = false;
        }
        super.channelReadComplete(ctx);
    }


    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        super.decodeLast(ctx, in, out);
        lastSended=true;
        try {
            if (respSended) {
            } else {
                ByteBuf response = default400Alive.duplicate().retain();
                ctx.write(response);
            }
            if(copyBuf!=null) {
                ByteBufPool.releaseBuf(copyBuf);
                copyBuf=null;
            }
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            respSended = false;
        }
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf response = process(msg, out);
        if (response != null) {
            respSended = true;
            out.add(response);
        }
    }

    private ByteBuf process(ByteBuf msg, List<Object> out) {
        if (respSended) {
            while (msg.isReadable()) {
                msg.readByte();
            }
        }
        lastSended=false;
        if (copyBuf == null) {
            try {
                copyBuf=ByteBufPool.getByteBuf();
            } catch (InterruptedException e) {
                //
            }
        }
        ByteBuf data = msg;
        boolean keepAlive = true;
        try {
            ClientApi.Response resp = waitResp;
            while (data.isReadable()) {
                copyBuf.writeBytes(data);
            }
            resp = RequestParser.parse(handler, copyBuf);
            if (resp.getStatus() == DataHolder.WAIT) {
                return null;
            }
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
                // logger.info("400 {}",printRequest(msg));
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
            }

            return response;
        } catch (Exception ex) {
            logger.error("error", ex);
            return default400Alive.duplicate().retain();
        } finally {

        }
    }

    public static String printRequest(ByteBuf data) {
        int readerIdx = data.readerIndex();
        data.readerIndex(0);
        String str = Thread.currentThread().getName() + "/" + data.toString(StandardCharsets.UTF_8);
        data.readerIndex(readerIdx);
        return str;
    }

    private static ClientApi.Response waitResp = new ClientApi.Response(DataHolder.WAIT);
    private static ClientApi.Response resp400 = new ClientApi.Response(DataHolder.INCORRECT_RESP);

    //@Override
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        logger.error("cannot process", cause);
        try {
            ctx.write(default400Alive.duplicate().retain());
            ctx.flush();
        } catch (Exception ex) {
            //
        }

        ctx.close();
        respSended = false;
        if(copyBuf!=null) {
            ByteBufPool.releaseBuf(copyBuf);
            copyBuf=null;
        }
    }
}
