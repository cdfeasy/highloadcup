package highloadcup.server;

import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * Created by d.asadullin on 01.09.2017.
 */
public class RequestParser {
    private static Logger logger = LoggerFactory.getLogger(RequestParser.class);
    static Method _getByte;

    static {
        try {
            _getByte = AbstractByteBuf.class.getDeclaredMethod("_getByte", int.class);
            _getByte.setAccessible(true);
        } catch (Exception ex) {

        }
    }

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
    public static byte[] contentLength = "content-length: ".getBytes();
    public static byte[] bodySeparator = "\r\n\r\n".getBytes();

    private static int findBytes(ByteBuf data, byte[] array) {
        int idx = 0;
        byte b;
        int cursor = data.readerIndex();
        while (true) {
            // ((AbstractByteBuf)data)._getByte();

            while (data.capacity() > cursor && (b = data.getByte(cursor++)) != array[idx]) ;
            if (data.capacity() - cursor < array.length) {
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
                return cursor;
            }
        }
    }

    private static byte eol = '\n';
    private static int httpSize = " HTTP/1.1\n".length();

    public static ClientApi.Response parse(ClientApi api, ByteBuf data) {
//        data= Unpooled.wrappedBuffer(("POST /visits/4900?query_id=1332 HTTP/1.1\n" +
//                "Host: travels.com\n" +
//                "User-Agent: Technolab/1.0 (Docker; CentOS) Highload/1.0\n" +
//                "Accept: */*\n" +
//                "Connection: close\n" +
//                "Content-Length: 0\n" +
//                "Content-Type: application/json").getBytes());


        boolean isPost = false;
        if (data.readByte() == 'G') {
            data.skipBytes(3);
        } else {
            data.skipBytes(4);
            isPost = true;
        }
        int fistLineEnd = data.bytesBefore(eol);
        if(fistLineEnd==-1){
            return new ClientApi.Response(DataHolder.INCORRECT_RESP);
        }
        byte[] request1 = new byte[fistLineEnd - httpSize];
        data.readBytes(request1);
        String uri = new String(request1);
//        if(uri.contains("/users/4048")){
//            data.readerIndex(0);
//            logger.info(data.toString(StandardCharsets.UTF_8));
//        }
//        if(uri.contains("/users/6136")){
//            data.readerIndex(0);
//            logger.info(data.toString(StandardCharsets.UTF_8));
//        }
//        if(uri.contains("/visits/4900")){
//            data.readerIndex(0);
//            logger.info(data.toString(StandardCharsets.UTF_8));
//        }
//        if(uri.contains("/locations/5893")){
//            data.readerIndex(0);
//            logger.info(data.toString(StandardCharsets.UTF_8));
//        }

        int bodySep = findBytes(data, bodySeparator);
        if (bodySep != -1) {
            data.readerIndex(bodySep);
        }
//        byte[] body = new byte[data.readableBytes()];
//        data.readBytes(body);
//        System.out.println("------------");
//        System.out.println(new String(body));
//        System.out.println("------------");

        ClientApi.Response transfer = ApiHandler.transfer(api, data, isPost, uri);
//        if (transfer.getStatus() == 400) {
//
//            if (isPost) {
//                data.readerIndex(0);
//                logger.info(data.toString(StandardCharsets.UTF_8));
//            }
//
//
//        }
        return transfer;

        // byte[] request=new byte[fistLineEnd-httpSize-data.readerIndex()];
//        data.readBytes(request);
//        System.out.println(new String(request));


    }
}
