package highloadcup.server;

import highloadcup.server.RequestParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class WrapperHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(RequestParser.class);
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private static void printResponse(ByteBuf data) {
        int readerIdx = data.readerIndex();
        data.readerIndex(0);
        String resp=data.toString(StandardCharsets.UTF_8);
        if(resp.contains("HTTP/1.1 400 OK")) {
            logger.info("response:{}", data.toString(StandardCharsets.UTF_8));
        }
        data.readerIndex(readerIdx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       // printResponse((ByteBuf) msg);
       // System.out.println(((ByteBuf)msg).toString(Charset.defaultCharset()));
        ctx.writeAndFlush(msg,ctx.voidPromise());
       // ((ByteBuf) msg).release();
       // ctx.flush();
     //   ctx.fireChannelReadComplete();
       // ctx.flush();
       //ctx.flush();
    }


}
