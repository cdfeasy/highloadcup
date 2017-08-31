package highloadcup;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import sun.nio.cs.Surrogate;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/**
 * Created by d.asadullin on 31.08.2017.
 */
public class StringTest {
    private void encodeBufferLoop(CharBuffer from, ByteBuffer to) {
        int var3;
        for(var3 = from.position(); from.hasRemaining(); ++var3) {
            char var4 = from.get();
            if(var4 < 128) {

                to.put((byte)var4);
            } else if(var4 < 2048) {

                to.put((byte)(192 | var4 >> 6));
                to.put((byte)(128 | var4 & 63));
            } else{
                to.put((byte)(224 | var4 >> 12));
                to.put((byte)(128 | var4 >> 6 & 63));
                to.put((byte)(128 | var4 & 63));
            }
        }
    }

    @Test
    public void testBuf(){
        ByteBuffer bb= ByteBuffer.allocate(100000);
        String str="ололо алала пыщь jdkdkd [] ! eee \n" ;
        char[] charArray = str.toCharArray();
        CharBuffer cb=CharBuffer.wrap(charArray);
        encodeBufferLoop(cb,bb);
        bb.flip();
        byte[] b=new byte[bb.remaining()];
        bb.get(b);
        Assert.assertEquals(str,new String(b,StandardCharsets.UTF_8));
        Assert.assertArrayEquals(str.getBytes(StandardCharsets.UTF_8),b);

        ByteBuf byteBuf= Unpooled.directBuffer(1000);

    }

}
