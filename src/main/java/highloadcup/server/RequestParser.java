package highloadcup.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;

/**
 * Created by d.asadullin on 01.09.2017.
 */
public class RequestParser {
//    POST /users/new?ololo=alala HTTP/1.1
//    host: 127.0.0.1
//    connection: close
//    content-length: 168
//
//    {
//        "first_name": "Данила",
//            "last_name": "Стыкушувич",
//            "gender": "m",
//            "id": 100174,
//            "birth_date": 843091200,
//            "email": "idsornawsotne@me.com"
//    }
//

    private static int getContentLength(ByteBuf data,byte[] array) {
        int idx = 0;
        byte b;
        int cursor=data.readerIndex();
        while (true) {
            while (data.readableBytes() > 0 && (b = data.getByte(cursor++)) != array[idx]) ;
            if (data.readableBytes()-cursor < array.length) {
                return -1;
            }
            for (idx = 1; idx < array.length; idx++) {
                b = data.getByte(cursor++);
                if (b != array[idx]) {
                    idx = 0;
                    break;
                }
            }
            if (idx != 0) {
                return data.readerIndex();
            }
        }
    }

    private static byte eol='\n';
    private static int httpSize=" HTTP/1.1\n".length();
    public void parse(ChannelHandlerContext ctx, ByteBuf data){
        boolean isPost=false;
        if(data.readByte()=='G'){
            data.skipBytes(3);
        }else {
            data.skipBytes(4);
            isPost=true;
        }
        int fistLineEnd=data.bytesBefore(eol);
        byte[] request1=new byte[fistLineEnd];
        data.readBytes(request1);
        System.out.println(new String(request1));
       // byte[] request=new byte[fistLineEnd-httpSize-data.readerIndex()];
//        data.readBytes(request);
//        System.out.println(new String(request));




    }
}
