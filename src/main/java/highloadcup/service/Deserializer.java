package highloadcup.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import highloadcup.entity.Location;
import highloadcup.entity.User;
import highloadcup.entity.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dmitry on 20.08.2017.
 */
public class Deserializer {
    private static JsonFactory f = new MappingJsonFactory();
    Logger logger = LoggerFactory.getLogger(Deserializer.class);

    public static Visit toAddVisit(InputStream is) {
        try(JsonParser jp = f.createParser(is)){
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode location = node.get("location");
            JsonNode user = node.get("user");
            JsonNode visited_at = node.get("visited_at");
            JsonNode mark = node.get("mark");
            if (id == null ||
                    location == null ||
                    user == null ||
                    visited_at == null ||
                    mark == null) {
                return null;
            }
            int _id;
            if (id.isNumber()) {
                _id = id.asInt();
            } else {
                return null;
            }
            int _location;
            if (location.isNumber() && location.asInt() >= 0) {
                _location = location.asInt();
            } else {
                return null;
            }
            int _user;
            if (user.isNumber() && user.asInt() >= 0) {
                _user = user.asInt();
            } else {
                return null;
            }
            long _visited_at;
            if (visited_at.isNumber()) {
                _visited_at = visited_at.asLong();
            } else {
                return null;
            }
            byte _mark;
            if (mark.isNumber() && mark.asInt() >= 0 && mark.asInt() <= 5) {
                _mark = (byte) mark.asInt();
            } else {
                return null;
            }
            return new Visit(_id, _location, _user, _visited_at, _mark);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Visit toUpdateVisit(InputStream is) {
        try(JsonParser jp = f.createParser(is)){
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode location = node.get("location");
            JsonNode user = node.get("user");
            JsonNode visited_at = node.get("visited_at");
            JsonNode mark = node.get("mark");
            if ((id != null) ||
                    (location != null && location.isNull()) ||
                    (user != null && user.isNull()) ||
                    (visited_at != null && visited_at.isNull()) ||
                    (mark != null && mark.isNull())) {
                return null;
            }
            int _location = -1;
            if (location != null && location.isNumber() && location.asInt() >= 0) {
                _location = location.asInt();
            } else {
                if (location != null) {
                    return null;
                }
            }
            int _user = -1;
            if (user != null && user.isNumber() && user.asInt() >= 0) {
                _user = user.asInt();
            } else {
                if (user != null) {
                    return null;
                }
            }
            Long _visited_at = null;
            if (visited_at != null && visited_at.isNumber()) {
                _visited_at = visited_at.asLong();
            } else {
                if (visited_at != null) {
                    return null;
                }
            }
            byte _mark = -1;
            if (mark != null && mark.isNumber() && mark.asInt() >= 0 && mark.asInt() <= 5) {
                _mark = (byte) mark.asInt();
            } else {
                if (mark != null) {
                    return null;
                }
            }
            return new Visit(0, _location, _user, _visited_at, _mark);
        } catch (Exception ex) {
            return null;
        }
    }


    public static Location toAddLocation(InputStream is) {
        try ( JsonParser jp = f.createParser(is)){
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode place = node.get("place");
            JsonNode country = node.get("country");
            JsonNode city = node.get("city");
            JsonNode distance = node.get("distance");
            if (id == null ||
                    place == null ||
                    country == null ||
                    city == null ||
                    distance == null) {
                return null;
            }
            int _id;
            if (id.isNumber() && id.asInt() >= 0) {
                _id = id.asInt();
            } else {
                return null;
            }
            String _place;
            if (place.isTextual()) {
                _place = place.asText();
            } else {
                return null;
            }
            String _country;
            if (country.isTextual() && country.asText().length() <= 50) {
                _country = country.asText();
            } else {
                return null;
            }
            String _city;
            if (city.isTextual() && city.asText().length() <= 50) {
                _city = city.asText();
            } else {
                return null;
            }
            int _distance;
            if (distance.isNumber()) {
                _distance = distance.asInt();
            } else {
                return null;
            }
            return new Location(_id, _place, _country, _city, _distance);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Location toUpdateLocation(InputStream is) {
        try( JsonParser jp = f.createParser(is)) {
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode place = node.get("place");
            JsonNode country = node.get("country");
            JsonNode city = node.get("city");
            JsonNode distance = node.get("distance");
            if ((id != null) ||
                    (place != null && place.isNull()) ||
                    (country != null && country.isNull()) ||
                    (city != null && city.isNull()) ||
                    (distance != null && distance.isNull())) {
                return null;
            }
            String _place = null;
            if (place != null && place.isTextual()) {
                _place = place.asText();
            } else {
                if (place != null) {
                    return null;
                }
            }
            String _country = null;
            if (country != null && country.isTextual() && country.asText().length() <= 50) {
                _country = country.asText();
            } else {
                if (country != null) {
                    return null;
                }
            }
            String _city = null;
            if (city != null && city.isTextual() && city.asText().length() <= 50) {
                _city = city.asText();
            } else {
                if (city != null) {
                    return null;
                }
            }
            int _distance = -1;
            if (distance != null && distance.isNumber()) {
                _distance = distance.asInt();
            } else {
                if (distance != null) {
                    return null;
                }
            }
            return new Location(0, _place, _country, _city, _distance);
        } catch (Exception ex) {
            return null;
        }
    }


    public static User toAddUser(InputStream is) {
        try(  JsonParser jp = f.createParser(is)) {
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode email = node.get("email");
            JsonNode first_name = node.get("first_name");
            JsonNode last_name = node.get("last_name");
            JsonNode gender = node.get("gender");
            JsonNode birth_date = node.get("birth_date");
            if (id == null ||
                    email == null ||
                    first_name == null ||
                    last_name == null ||
                    gender == null ||
                    birth_date == null) {
                return null;
            }
            int _id;
            if (id.isNumber() && id.asInt() >= 0) {
                _id = id.asInt();
            } else {
                return null;
            }
            String _email;
            if (email.isTextual() && email.asText().length() <= 100) {
                _email = email.asText();
            } else {
                return null;
            }
            String _first_name;
            if (first_name.isTextual() && first_name.asText().length() <= 50) {
                _first_name = first_name.asText();
            } else {
                return null;
            }
            String _last_name;
            if (last_name.isTextual() && last_name.asText().length() <= 50) {
                _last_name = last_name.asText();
            } else {
                return null;
            }
            boolean _gender;
            if (gender.isTextual() && ("f".equals(gender.asText()) || "m".equals(gender.asText()))) {
                _gender = "m".equals(gender.asText());
            } else {
                return null;
            }
            long _birth_date;
            if (birth_date.isNumber()) {
                _birth_date = birth_date.asLong();
            } else {
                return null;
            }

            return new User(_id, _email, _first_name, _last_name, _gender, _birth_date);
        } catch (Exception ex) {
            return null;
        }
    }

    public static User toUpdateUser(InputStream is) {
        try( JsonParser jp = f.createParser(is)) {
            JsonNode node = jp.readValueAsTree();
            JsonNode id = node.get("id");
            JsonNode email = node.get("email");
            JsonNode first_name = node.get("first_name");
            JsonNode last_name = node.get("last_name");
            JsonNode gender = node.get("gender");
            JsonNode birth_date = node.get("birth_date");
            if ((id != null) ||
                    (email != null && email.isNull()) ||
                    (first_name != null && first_name.isNull()) ||
                    (last_name != null && last_name.isNull()) ||
                    (gender != null && gender.isNull()) ||
                    (birth_date != null && birth_date.isNull())) {
                return null;
            }
            String _email = null;
            if (email != null && email.isTextual() && email.asText().length() <= 100) {
                _email = email.asText();
            } else {
                if (email != null) {
                    return null;
                }
            }
            String _first_name = null;
            if (first_name != null && first_name.isTextual() && first_name.asText().length() <= 50) {
                _first_name = first_name.asText();
            } else {
                if (first_name != null) {
                    return null;
                }
            }
            String _last_name = null;
            if (last_name != null && last_name.isTextual() && last_name.asText().length() <= 50) {
                _last_name = last_name.asText();
            } else {
                if (last_name != null) {
                    return null;
                }
            }
            Boolean _gender = null;
            if (gender != null && gender.isTextual() && ("f".equals(gender.asText()) || "m".equals(gender.asText()))) {
                _gender = "m".equals(gender.asText());
            } else {
                if (gender != null) {
                    return null;
                }
            }
            Long _birth_date = null;
            if (birth_date != null && birth_date.isNumber()) {
                _birth_date = birth_date.asLong();
            } else {
                if (birth_date != null) {
                    return null;
                }
            }
            return new User(0, _email, _first_name, _last_name, _gender, _birth_date);
        } catch (Exception ex) {
            return null;
        }
    }
}
