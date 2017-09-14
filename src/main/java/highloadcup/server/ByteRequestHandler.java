package highloadcup.server;

import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ByteRequestHandler extends ChannelInboundHandlerAdapter {
    private static class ByteBufPool {
        static LinkedBlockingQueue<ByteBuf> queue = new LinkedBlockingQueue<>(2100);

        static {
            for (int i = 0; i < 2100; i++) {
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

    public ByteRequestHandler(ClientApi handler) {
        this.handler = handler;
    }


    //    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.flush();
//    }
    //ByteBuf copyBuf = Unpooled.buffer(1024 * 1024 * 10);
    ByteBuf copyBuf = null;
    private volatile boolean respSended = false;
    private volatile boolean lastSended = false;

    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        lastSended=true;
        close(ctx);
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        lastSended=true;
        close(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof ChannelInputShutdownEvent) {
            lastSended=true;
            // The decodeLast method is invoked when a channelInactive event is encountered.
            // This method is responsible for ending requests in some situations and must be called
            // when the input has been shutdown.
            close(ctx);
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        while (((ByteBuf) msg).isReadable()) {
            ByteBuf response = process((ByteBuf) msg);
            if (response != null) {
                respSended = true;
                ctx.write(response);
                break;
            } else {
                if (ctx.isRemoved()) {
                    break;
                }
            }
        }
        ((ByteBuf) msg).release();

    }

    private void close(ChannelHandlerContext ctx) {
        try {
            if (respSended) {
            } else {
                if (!lastSended) {
                    ByteBuf response = default400Alive.duplicate().retain();
                    ctx.write(response);
                }
            }
            if (copyBuf != null) {
                ByteBufPool.releaseBuf(copyBuf);
                copyBuf = null;
            }
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            respSended = false;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        close(ctx);
        super.channelReadComplete(ctx);
    }

    private ByteBuf process(ByteBuf msg) {
        if (respSended) {
            while (msg.isReadable()) {
                msg.readByte();
            }
        }
        lastSended = false;
        if (copyBuf == null) {
            try {
                copyBuf = ByteBufPool.getByteBuf();
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
        if (copyBuf != null) {
            ByteBufPool.releaseBuf(copyBuf);
            copyBuf = null;
        }
    }

}
