package highloadcup.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Created by d.asadullin on 31.08.2017.
 */
public class TestRequestHandler extends ChannelInboundHandlerAdapter  {
    public NewHttpServerHandler handler;
    public TestRequestHandler(NewHttpServerHandler handler){
        this.handler=handler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {

            handler.read(ctx,msg);
            ctx.flush();
            ReferenceCountUtil.release(msg);
        //    ctx.flush();
         //   ((ByteBuf) msg).release();
          //  ctx.read();
           // ctx.fireChannelReadComplete();
         //   ctx.flush();
         //   ctx.flush();
//            ctx.fireChannelRead(msg);
//            ctx.read();
//            ctx.fireChannelReadComplete();
//            ctx.fireChannelActive();
          //  ctx.flush();
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
