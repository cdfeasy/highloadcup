package highloadcup.server;
import highloadcup.service.ClientApi;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * Created by dmitry on 20.08.2017.
 */
public class NewHttpServerInit extends ChannelInitializer<SocketChannel> {
    private ClientApi api;

    public NewHttpServerInit(ClientApi api){
        this.api=api;
    }
    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // HttpServerCodec is a combination of HttpRequestDecoder and HttpResponseEncoder
//        p.addLast(new HttpServerCodec());
      //  NewHttpServerHandler newHttpServerHandler = new NewHttpServerHandler(api);;

      //  new HttpRequestDecoder();
    //    p.addLast(new HttpRequestDecoder());
       // p.addLast(new HttpResponseEncoder());
        p.addLast(new ByteRequestHandler(api));
        p.addLast(new WrapperHandler());
      //  new HttpRequestDecoder();
     //   new HttpObjectAggregator(Integer.MAX_VALUE);
      //  new HttpResponseEncoder();
       // p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
       // p.addLast(new ChannelTrafficShapingHandler());


        // add gizp compressor for http response content
      //  p.addLast(new HttpContentCompressor());


        //p.addLast(new HttpResponseEncoder());
      //  p.addLast(new ChunkedWriteHandler());

      //  p.addLast(newHttpServerHandler);
    }
}