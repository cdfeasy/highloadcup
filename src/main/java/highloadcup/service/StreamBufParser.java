package highloadcup.service;

import highloadcup.entity.Location;
import highloadcup.entity.User;
import highloadcup.entity.Visit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static highloadcup.server.ApiHandler.isNumeric;
import static highloadcup.server.ApiHandler.decodeComponent;

public class StreamBufParser {
    private static Logger logger = LoggerFactory.getLogger(StreamBufParser.class);
    private static byte[] first_name_bytes = "first_name".getBytes();
    private static byte[] last_name_bytes = "last_name".getBytes();
    private static byte[] gender_bytes = "gender".getBytes();
    private static byte[] id_bytes = "id".getBytes();
    private static byte[] birth_date_bytes = "birth_date".getBytes();
    private static byte[] email_bytes = "email".getBytes();

    private static byte[] place_bytes = "place".getBytes();
    private static byte[] country_bytes = "country".getBytes();
    private static byte[] city_bytes = "city".getBytes();
    private static byte[] distance_bytes = "distance".getBytes();

    private static byte[] location_bytes = "location".getBytes();
    private static byte[] user_bytes = "user".getBytes();
    private static byte[] visited_at_bytes = "visited_at".getBytes();
    private static byte[] mark_bytes = "mark".getBytes();

    private static byte eol = '\n';
    private static byte sep = ':';
    private static byte comma = ',';
    private static byte space = ' ';
    private static byte quote = '\"';
    private static byte start = '{';
    private static byte end = '}';

    public static short[] parse(ByteBuf buf, int size, int readerIdx) {
        try {
            short[] addresses = new short[size];
            buf.readerIndex(readerIdx);
            int startIdx = buf.indexOf(readerIdx,buf.capacity(),start);
            int endIdx = buf.indexOf(readerIdx,buf.capacity(),end);
            if (endIdx == -1 ||startIdx==-1 || startIdx==endIdx) {
                addresses[0] = -1;
                return addresses;
            }
            int i = startIdx + 1;
            int paramIdx = 1;
            while (i < endIdx) {
                int startName = buf.indexOf(i, endIdx, quote);
                int endName = buf.indexOf(startName + 1, endIdx, quote);
                if (startName == -1 || endName == -1) {
                    addresses[0] = -1;
                    return addresses;
                }
                startName++;
                int sepIdx = buf.indexOf(endName, endIdx, sep);
                if (sepIdx == -1) {
                    addresses[0] = -1;
                    return addresses;
                }
                int commaIdx = buf.indexOf(sepIdx, endIdx, comma);
                if (commaIdx == -1) {
                    commaIdx = endIdx;
                }
                int startValue = buf.indexOf(sepIdx, commaIdx, quote);
                int endValue;
                if (startValue != -1) {
                    startValue++;
                    endValue = buf.indexOf(startValue, commaIdx, quote);
                    if (endValue == -1) {
                        addresses[0] = -1;
                        return addresses;
                    }
                } else {
                    startValue = sepIdx + 1;
                    while (buf.getByte(startValue) == space) {
                        startValue++;
                    }
                    if (buf.getByte(startValue) == 'n') {
                        endValue = -1;
                    } else {
                        endValue = commaIdx;
                        while (buf.getByte(endValue - 1) == space && endValue > startValue) {
                            endValue--;
                        }
                        if (endValue == startValue) {
                            addresses[0] = -1;
                            return addresses;
                        }
                    }

                }
                addresses[paramIdx] = (short) startName;
                addresses[paramIdx + 1] = (short) (endName - startName);
                addresses[paramIdx + 2] = (short) startValue;
                if (endValue != -1) {
                    addresses[paramIdx + 3] = (short) (endValue - startValue);
                } else {
                    addresses[paramIdx + 3] = -1;
                }
                paramIdx += 4;
                addresses[0] = (short) (addresses[0] + 1);
                i = commaIdx;
            }
            return addresses;
        }catch (Exception ex){
            logger.info("cannot parse "+buf.toString(StandardCharsets.UTF_8),ex);
            short[] addresses = new short[1];
            addresses[0]=-1;
            return addresses;
        }
    }

    private static boolean arraysEquals(ByteBuf data, byte[] array, short from, short length) {
        if (array.length != length) {
            return false;
        }
        int idx = 0;
        int cursor = from;
        while (data.readableBytes() > 0 && idx < length && data.getByte(cursor++) == array[idx++]) ;
        return idx == length;
    }


    public static void main(String[] args) {
        StreamBufParser parser = new StreamBufParser();
        ByteBuf buf0 = Unpooled.wrappedBuffer(("").getBytes());
        System.out.println(toUpdateUser(buf0));


        ByteBuf buf = Unpooled.wrappedBuffer(("{\"first_name\": \"\\u0412\\u0438\\u043a\\u0442\\u043e\\u0440\\u0438\\u044f\", \"last_name\": \"\\u041a\\u0438\\u0441\\u0443\\u0448\\u0443\\u0447\\u0430\\u043d\", \"gender\": \"f\", \"id\": 10094, \"birth_date\": 803347200, \"email\": \"abputew@ya.ru\"}").getBytes());
        System.out.println(toAddUser(buf));
        ByteBuf buf1 = Unpooled.wrappedBuffer(("{\n" +
                "  \"first_name\": \"Людмила\",\n" +
                "  \"last_name\": \"Лукыкако\",\n" +
                "  \"gender\": \"f\",\n" +
                "  \"id\": 1002943,\n" +
                "  \"birth_date\": 160790400,\n" +
                "  \"email\": \"veimtinotpa@mail.ru\"\n" +
                "}").getBytes());
        System.out.println(toAddUser(buf1));

        ByteBuf buf2 = Unpooled.wrappedBuffer(("{\n" +
                "  \"country\": \"Белоруссия\"\n" +
                "}").getBytes());
        System.out.println(toUpdateLocation(buf2));




        ByteBuf buf3 = Unpooled.wrappedBuffer(("{\n" +
                "            \"id\": 7765,\n" +
                "                \"distance\": 95,\n" +
                "                \"place\": \"Море\",\n" +
                "                \"city\": \"Новобирск\",\n" +
                "                \"country\": \"Австралия\"\n" +
                "        }").getBytes());
        System.out.println(toAddLocation(buf3));


    }


    public static Visit toAddVisit(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String location = null;
        String user = null;
        String visited_at = null;
        String mark = null;

        for (int i = 0; i < parse[0]; i++) {
            if (location == null && arraysEquals(buf, location_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                location = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (user == null && arraysEquals(buf, user_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                user = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (visited_at == null && arraysEquals(buf, visited_at_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                visited_at = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (mark == null && arraysEquals(buf, mark_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                mark = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (id == null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }

        if (id == null ||
                location == null ||
                user == null ||
                visited_at == null ||
                mark == null) {
            return null;
        }
        int _id;
        if (isNumeric(id)) {
            _id = Integer.valueOf(id);
        } else {
            return null;
        }
        int _location;
        if (isNumeric(location)) {
            _location = Integer.valueOf(location);
        } else {
            return null;
        }
        int _user;
        if (isNumeric(user)) {
            _user = Integer.valueOf(user);
        } else {
            return null;
        }
        long _visited_at;
        if (isNumeric(visited_at)) {
            _visited_at = Long.valueOf(visited_at);
        } else {
            return null;
        }
        byte _mark;
        if (isNumeric(mark)) {
            _mark = Byte.valueOf(mark);
            if (_mark < 0 || _mark > 5) {
                return null;
            }
        } else {
            return null;
        }
        return new Visit(_id, _location, _user, _visited_at, _mark);
    }

    public static Visit toUpdateVisit(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String location = null;
        String user = null;
        String visited_at = null;
        String mark = null;

        for (int i = 0; i < parse[0]; i++) {
            if (location == null && arraysEquals(buf, location_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                location = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(location==null){
                    return null;
                }
            } else if (user== null && arraysEquals(buf, user_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                user = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(user==null){
                    return null;
                }
            } else if (visited_at == null && arraysEquals(buf, visited_at_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                visited_at = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(visited_at==null){
                    return null;
                }
            } else if (mark== null && arraysEquals(buf, mark_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                mark = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(mark==null){
                    return null;
                }
            } else if (id== null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }
        if (id != null) {
            return null;
        }

        if (
                location == null &&
                user == null &&
                visited_at == null &&
                mark == null) {
            return null;
        }
        int _location = -1;
        if (location != null && isNumeric(location)) {
            _location = Integer.valueOf(location);
        } else {
            if (location != null)
                return null;
        }
        int _user = -1;
        if (user != null && isNumeric(user)) {
            _user = Integer.valueOf(user);
        } else {
            if (user != null)
                return null;
        }
        Long _visited_at=null;
        if (visited_at != null && isNumeric(visited_at)) {
            _visited_at = Long.valueOf(visited_at);
        } else {
            if (visited_at != null)
                return null;
        }
        byte _mark = -1;
        if (mark != null && isNumeric(mark)) {
            _mark = Byte.valueOf(mark);
            if (_mark < 0 || _mark > 5) {
                return null;
            }
        } else {
            if (mark != null)
                return null;
        }
        return new Visit(0, _location, _user, _visited_at, _mark);
    }


    public static Location toAddLocation(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String place = null;
        String country = null;
        String city = null;
        String distance = null;

        for (int i = 0; i < parse[0]; i++) {
            if (place == null && arraysEquals(buf, place_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                place = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (country == null && arraysEquals(buf, country_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                country = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (city == null && arraysEquals(buf, city_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                city = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (distance == null && arraysEquals(buf, distance_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                distance = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (id == null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }
        if (id == null ||
                place == null ||
                country == null ||
                city == null ||
                distance == null) {
            return null;
        }
        int _id;
        if (isNumeric(id)) {
            _id = Integer.valueOf(id);
        } else {
            return null;
        }
        if (country.length() > 50) {
            return null;
        }
        if (city.length() > 50) {
            return null;
        }
        int _distance;
        if (isNumeric(distance)) {
            _distance = Integer.valueOf(distance);
        } else {
            return null;
        }
        return new Location(_id, place, country, city, _distance);
    }
    private static String getValue(ByteBuf buf, short from, short length,boolean decode){
        if(length==-1){
            return null;
        }
        byte[] value = new byte[length];
        buf.getBytes(from, value);
        if(decode) {
                return org.apache.commons.text.StringEscapeUtils.unescapeJava(new String(value));
            //     return decodeComponent(new String(value, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }else {
            return new String(value, StandardCharsets.UTF_8);
        }
    }

    

    public static Location toUpdateLocation(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String place = null;
        String country = null;
        String city = null;
        String distance = null;

        for (int i = 0; i < parse[0]; i++) {
            if (place == null && arraysEquals(buf, place_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                place = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(place==null){
                    return null;
                }
            } else if (country == null && arraysEquals(buf, country_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                country = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(country==null){
                    return null;
                }
            } else if (city == null && arraysEquals(buf, city_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                city = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(city==null){
                    return null;
                }
            } else if (distance == null && arraysEquals(buf, distance_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                distance = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(distance==null){
                    return null;
                }
            } else if (id == null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }
        if (id != null) {
            return null;
        }

        if (
                place == null &&
                country == null &&
                city == null &&
                distance == null) {
            return null;
        }

        if (country != null && country.length() > 50) {
            return null;
        }
        if (city != null && city.length() > 50) {
            return null;
        }
        int _distance = -1;
        if (distance != null && isNumeric(distance)) {
            _distance = Integer.valueOf(distance);
        } else {
            if (distance != null) {
                return null;
            }
        }
        return new Location(0, place, country, city, _distance);
    }


    public static User toAddUser(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String email = null;
        String first_name = null;
        String last_name = null;
        String gender = null;
        String birth_date = null;

        for (int i = 0; i < parse[0]; i++) {
            if (first_name == null && arraysEquals(buf, first_name_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                first_name = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (last_name == null && arraysEquals(buf, last_name_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                last_name = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (email == null && arraysEquals(buf, email_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                email = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (gender == null && arraysEquals(buf, gender_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                gender = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
            } else if (birth_date == null && arraysEquals(buf, birth_date_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                birth_date = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            } else if (id == null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }
        if (id == null ||
                email == null ||
                first_name == null ||
                last_name == null ||
                gender == null ||
                birth_date == null) {
            return null;
        }
        int _id;
        if (isNumeric(id)) {
            _id = Integer.valueOf(id);
            if (_id <= 0) {
                return null;
            }
        } else {
            return null;
        }
        if (email.length() >= 100) {
            return null;
        }
        if (first_name.length() > 50) {
            return null;
        }
        if (last_name.length() > 50) {
            return null;
        }
        boolean _gender;
        if ("f".equals(gender) || "m".equals(gender)) {
            _gender = "m".equals(gender);
        } else {
            return null;
        }
        long _birth_date;
        if (isNumeric(birth_date)) {
            _birth_date = Long.valueOf(birth_date);
        } else {
            return null;
        }

        return new User(_id, email, first_name, last_name, _gender, _birth_date);
    }

    public static User toUpdateUser(ByteBuf buf) {
        short[] parse = parse(buf, 33, buf.readerIndex());
        if (parse[0] == -1) {
            return null;
        }
        String id = null;
        String email = null;
        String first_name = null;
        String last_name = null;
        String gender = null;
        String birth_date = null;

        for (int i = 0; i < parse[0]; i++) {
            if (first_name == null && arraysEquals(buf, first_name_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                first_name = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(first_name==null){
                    return null;
                }
            } else if (last_name == null && arraysEquals(buf, last_name_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                last_name = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(last_name==null){
                    return null;
                }
            } else if (email == null && arraysEquals(buf, email_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                email = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(email==null){
                    return null;
                }
            } else if (gender == null && arraysEquals(buf, gender_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                gender = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],true);
                if(gender==null){
                    return null;
                }
            } else if (birth_date == null && arraysEquals(buf, birth_date_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                birth_date = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
                if(birth_date==null){
                    return null;
                }
            } else if (id == null && arraysEquals(buf, id_bytes, parse[i * 4 + 1], parse[i * 4 + 2])) {
                id = getValue(buf,parse[i * 4 + 3],parse[i * 4 + 4],false);
            }
        }
        if (id != null) {
            return null;
        }
        if (
                email == null &&
                first_name == null &&
                last_name == null &&
                gender == null &&
                birth_date == null) {
            return null;
        }


        if (email != null && email.length() > 100) {
            return null;
        }
        if (first_name != null && first_name.length() > 50) {
            return null;
        }
        if (last_name != null && last_name.length() > 50) {
            return null;
        }
        Boolean _gender = null;
        if (gender != null && ("f".equals(gender) || "m".equals(gender))) {
            _gender = "m".equals(gender);
        } else {
            if (gender != null) {
                return null;
            }
        }
        Long _birth_date = null;
        if (birth_date != null && isNumeric(birth_date)) {
            _birth_date = Long.valueOf(birth_date);
        } else {
            if (birth_date != null) {
                return null;
            }
        }
        return new User(0, email, first_name, last_name, _gender, _birth_date);
    }


}
