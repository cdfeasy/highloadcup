package highloadcup;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import highloadcup.entity.Location;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dmitry on 22.08.2017.
 */
public class SerializerTest {

    public static ByteBuffer toAddLocation(InputStream is) {
        try {
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createParser(is);
            JsonNode node = jp.readValueAsTree();

            JsonNode id = node.get("id");
            JsonNode place = node.get("place");
            JsonNode country = node.get("country");
            JsonNode city = node.get("city");
            JsonNode distance = node.get("distance");
            int _id;
            String _place;
            String _country;
            String _city;
            int _distance;

            if (id == null ||
                    place == null ||
                    country == null ||
                    city == null ||
                    distance == null) {
                return null;
            }
            if (id.isNumber() && id.asInt() >= 0) {
                _id = id.asInt();
            } else {
                return null;
            }
            if (place.isTextual()) {
                _place = place.asText();
            } else {
                return null;
            }
            if (country.isTextual() && country.asText().length() <= 50) {
                _country = country.asText();
            } else {
                return null;
            }
            if (city.isTextual() && city.asText().length() <= 50) {
                _city = city.asText();
            } else {
                return null;
            }
            if (distance.isNumber()) {
                _distance = distance.asInt();
            } else {
                return null;
            }
            ByteBuffer bb = ByteBuffer.allocate(4 + 4 + 4 + _place.length() + _country.length() + _city.length());
            bb.putInt(_id);
            bb.putInt(_distance);
            bb.putShort((short) _place.length());
            bb.putShort((short) _country.length());
            bb.putShort((short) _city.length());
            bb.put(_city.getBytes());
            bb.put(_place.getBytes());
            bb.put(_country.getBytes());
            return bb;
        } catch (Exception ex) {
            return null;
        }
    }

    private InputStream generateLocationStream(AtomicInteger id) {
        Random r = new Random();
        Integer _id = id.incrementAndGet();
        Location location = new Location();
        location.setId(_id);
        location.setCity(_id.toString() + "блабла1" + UUID.randomUUID().toString());
        location.setCountry(_id.toString() + "блабла2" + UUID.randomUUID().toString());
        location.setPlace(_id.toString() + "блабла3" + UUID.randomUUID().toString());
        location.setDistance(r.nextInt());
        return new ByteArrayInputStream(location.toString().getBytes());

    }

    @Test
    public void test() {
        AtomicInteger ai=new AtomicInteger();
        InputStream is=generateLocationStream(ai);
        ByteBuffer bb=toAddLocation(is);

    }
}
