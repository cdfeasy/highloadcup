package highloadcup.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import highloadcup.entity.Location;
import highloadcup.entity.User;
import highloadcup.entity.Visit;
import highloadcup.service.DataHolder;
import highloadcup.service.Deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by dmitry on 26.08.2017.
 */
public class Init {
    private static JsonFactory f = new MappingJsonFactory();

    public static void close(){
    }

    public static void process(DataHolder dataHolder, String zipFile) {
        try {
            ZipFile zis =
                    new ZipFile(zipFile);
            List<ZipEntry> locations = new ArrayList<>();
            List<ZipEntry> users = new ArrayList<>();
            List<ZipEntry> visits = new ArrayList<>();
            for (Enumeration<? extends ZipEntry> e = zis.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                String fileName = entry.getName();
                if (fileName.startsWith("users")) {
                    users.add(entry);
                }
                if (fileName.startsWith("locations")) {
                    locations.add(entry);
                }
                if (fileName.startsWith("visits")) {
                    visits.add(entry);
                }
            }
            for (ZipEntry entry : users) {
                initUsers(zis.getInputStream(entry), dataHolder);
                // logger.info("inited {}", entry.getName());
            }
            for (ZipEntry entry : locations) {
                initLocations(zis.getInputStream(entry), dataHolder);
                // logger.info("inited {}", entry.getName());
            }
            for (ZipEntry entry : visits) {
                initVisits(zis.getInputStream(entry), dataHolder);
                // logger.info("inited {}", entry.getName());
            }
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static void unZipIt(DataHolder dataHolder, String zipFile) throws Exception {
        process(dataHolder, zipFile);
    }

    public static String getOptions(String optionsFile) throws Exception {
        List<String> lines = Files.lines(Paths.get(optionsFile)).collect(Collectors.toList());
        Long date = Long.valueOf(lines.get(0));
        User.now = Instant.ofEpochSecond(date);
        String str = lines.get(1);
        return str;
    }


    public static void initVisits(InputStream is, DataHolder holder) throws IOException {
        JsonParser jp = f.createParser(is);
        JsonToken current = jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            current = jp.nextToken();
            if (fieldName.equals("visits")) {
                if (current == JsonToken.START_ARRAY) {
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode node = jp.readValueAsTree();
                        Visit visit = new Visit();
                        JsonNode id = node.get("id");
                        JsonNode location = node.get("location");
                        JsonNode user = node.get("user");
                        JsonNode visited_at = node.get("visited_at");
                        JsonNode mark = node.get("mark");
                        if ((id != null && id.isNull()) ||
                                (location != null && location.isNull()) ||
                                (user != null && user.isNull()) ||
                                (visited_at != null && visited_at.isNull()) ||
                                (mark != null && mark.isNull())) {
                            continue;
                        }
                        if (id.isNumber()) {
                            visit.setId(id.asInt());
                        }
                        if (location.isNumber()) {
                            visit.setLocation(location.asInt());
                        }
                        if (user.isNumber()) {
                            visit.setUser(user.asInt());
                        }
                        if (visited_at.isNumber()) {
                            visit.setVisited_at(visited_at.asLong());
                        }
                        if (mark.isNumber()) {
                            visit.setMark((byte) mark.asInt());
                        }
                        holder.addVisit(visit);
                    }
                } else {
                    jp.skipChildren();
                }
            }
        }
        jp.close();
    }

    public static void initUsers(InputStream is, DataHolder holder) throws IOException {
        JsonParser jp = f.createParser(is);
        JsonToken current = jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            current = jp.nextToken();
            if (fieldName.equals("users")) {
                if (current == JsonToken.START_ARRAY) {
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode node = jp.readValueAsTree();
                        User user = new User();
                        JsonNode id = node.get("id");
                        JsonNode email = node.get("email");
                        JsonNode first_name = node.get("first_name");
                        JsonNode last_name = node.get("last_name");
                        JsonNode gender = node.get("gender");
                        JsonNode birth_date = node.get("birth_date");
                        if ((id != null && id.isNull()) ||
                                (email != null && email.isNull()) ||
                                (first_name != null && first_name.isNull()) ||
                                (last_name != null && last_name.isNull()) ||
                                (gender != null && gender.isNull()) ||
                                (birth_date != null && birth_date.isNull())) {
                            continue;
                        }
                        if (id.isNumber()) {
                            user.setId(id.asInt());
                        }
                        if (email.isTextual()) {
                            user.setEmail(email.asText());
                        }
                        if (first_name.isTextual()) {
                            user.setFirst_name(first_name.asText());
                        }
                        if (last_name.isTextual()) {
                            user.setLast_name(last_name.asText());
                        }
                        if (gender.isTextual()) {
                            user.setGender("m".equals(gender.asText()));
                        }
                        if (birth_date.isNumber()) {
                            user.setBirth_date(birth_date.asLong());
                        }
                        holder.addUser(user);
                    }
                } else {
                    jp.skipChildren();
                }
            }
        }
        jp.close();
    }

    public static void initLocations(InputStream is, DataHolder holder) throws IOException {
        JsonParser jp = f.createParser(is);
        JsonToken current = jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            current = jp.nextToken();
            if (fieldName.equals("locations")) {
                if (current == JsonToken.START_ARRAY) {
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode node = jp.readValueAsTree();
                        Location location = new Location();
                        JsonNode id = node.get("id");
                        JsonNode place = node.get("place");
                        JsonNode country = node.get("country");
                        JsonNode city = node.get("city");
                        JsonNode distance = node.get("distance");

                        if ((id != null && id.isNull()) ||
                                (place != null && place.isNull()) ||
                                (country != null && country.isNull()) ||
                                (city != null && city.isNull()) ||
                                (distance != null && distance.isNull())) {
                            continue;
                        }
                        if (id.isNumber()) {
                            location.setId(id.asInt());
                        }
                        if (place.isTextual()) {
                            location.setPlace(place.asText());
                        }
                        if (country.isTextual()) {
                            location.setCountry(country.asText());
                        }
                        if (city.isTextual()) {
                            location.setCity(city.asText());
                        }
                        if (distance.isNumber()) {
                            location.setDistance(distance.asInt());
                        }
                        holder.addLocation(location);
                    }
                } else {
                    jp.skipChildren();
                }
            }
        }
        jp.close();
    }
}
