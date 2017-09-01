package highloadcup.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class StreamBufParser {

    private static byte eol='\n';
    private static byte sep=':';
    private static byte comma=',';
    private static byte space=' ';
    private static byte quote='\"';
    private static byte start='{';
    private static byte end='}';
    public short[] parse(ByteBuf buf,int size){
        short[] addresses=new short[size];
        int startIdx=buf.bytesBefore(start);
        int endIdx=buf.bytesBefore(end);
        if(endIdx==-1){
            addresses[0]=-1;
            return addresses;
        }
        int i=startIdx+1;
        int paramIdx=1;
        while (i<endIdx){
            int startName=buf.indexOf(i,endIdx,quote);
            int endName=buf.indexOf(startName+1,endIdx,quote);
            if(startName==-1||endName==-1){
                addresses[0]=-1;
                return addresses;
            }
            startName++;
            int sepIdx=buf.indexOf(endName,endIdx,sep);
            if(sepIdx==-1){
                addresses[0]=-1;
                return addresses;
            }
            int commaIdx=buf.indexOf(sepIdx,endIdx,comma);
            if(commaIdx==-1){
               commaIdx=endIdx;
            }
            int startValue=buf.indexOf(sepIdx,commaIdx,quote);
            int endValue;
            if(startValue!=-1){
                endValue=buf.indexOf(startValue+1,commaIdx,quote);
                if(endValue==-1){
                    addresses[0]=-1;
                    return addresses;
                }
            }else{
                startValue=sepIdx+1;
                while (buf.getByte(startValue)==space){
                    startValue++;
                }
                endValue=commaIdx-1;
            }
            startValue++;
            addresses[paramIdx]=(short) startName;
            addresses[paramIdx+1]=(short) (endName-startName);
            addresses[paramIdx+2]=(short) startValue;
            addresses[paramIdx+3]=(short) (endValue-startValue);
            paramIdx+=4;
            addresses[0]=(short)(addresses[0]+ 1);
            i=commaIdx;
        }
        return addresses;
    }

    public static void main(String[] args){
        StreamBufParser parser=new StreamBufParser();
        ByteBuf buf= Unpooled.wrappedBuffer(("        {\n" +
                "        \"first_name\": \"Данила\",\n" +
                "            \"last_name\": \"Стыкушувич\",\n" +
                "            \"gender\" : \"m\",\n" +
                "            \"id\": 100174,\n" +
                "            \"birth_date\": 843091200,\n" +
                "            \"email\": \"idsornawsotne@me.com\"\n" +
                "    }").getBytes());
        short[] parse = parser.parse(buf, 40);
        if(parse[0]==-1){
            System.out.println("беда");
        }else {
            for(int i=0;i<parse[0];i++){
                byte[] name=new byte[parse[i*4+2]];
                byte[] value=new byte[parse[i*4+4]];
                buf.getBytes(parse[i*4+1],name);
                buf.getBytes(parse[i*4+3],value);
                System.out.println("/"+new String(name)+"/");
                System.out.println("/"+new String(value)+"/");
            }
        }
    }
}
