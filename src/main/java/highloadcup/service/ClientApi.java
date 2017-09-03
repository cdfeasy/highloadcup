package highloadcup.service;

import highloadcup.entity.Location;
import highloadcup.entity.User;
import highloadcup.entity.Visit;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by d.asadullin on 18.08.2017.
 */
public class ClientApi {
    Logger logger = LoggerFactory.getLogger(ClientApi.class);
    private DataHolder dataHolder;

    public DataHolder getDataHolder() {
        return dataHolder;
    }

    public static class Response {
        private Integer status;
        private byte[] response;

        public Response(Integer status) {
            this.status = status;
        }

        public Response(Integer status, byte[] response) {
            this.status = status;
            this.response = response;
        }

        public Integer getStatus() {
            return status;
        }

        public byte[] getResponse() {
            return response;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (getStatus() != null) {
                sb.append("\"status\":" + "\"" + getStatus() + "\"" + ",");
            }
            if (getResponse() != null) {
                sb.append("\"response\":" + "\"" + getResponse() + "\"" + "");
            }
            sb.append("}");
            return sb.toString();
        }
    }


    public Response request(String url, boolean isPost, Object[] params, ByteBuf content) {
        try {
            String[] parts = url.length() > 0 ? url.substring(1).split("/") : new String[]{};
            if (!isPost) {
                Response x = processGet(parts, params);
                if (x != null) return x;
            } else {
                Response x = processPost(parts, content);
                if (x != null) return x;
            }
            return new Response(DataHolder.NOTFOUND_RESP);
        }catch (Exception ex){
            logger.error("Unexpected error",ex);
            return new Response(DataHolder.INCORRECT_RESP);
        }
    }

    private Response processPost(String[] parts, ByteBuf content) {
        if (parts.length == 2) {
            if ("users".equals(parts[0])) {
                if ("new".equals(parts[1])) {
                    return addUser(content);
                } else {
                    return updateUser(parts[1], content);
                }
            } else if ("visits".equals(parts[0])) {
                if ("new".equals(parts[1])) {
                    return addVisit(content);
                } else {
                    return updateVisit(parts[1], content);
                }
            } else if ("locations".equals(parts[0])) {
                if ("new".equals(parts[1])) {
                    return addLocation(content);
                } else {
                    return updateLocation(parts[1], content);
                }
            } else {
                return new Response(DataHolder.NOTFOUND_RESP);
            }
        } else {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    private Response processGet(String[] parts, Object[] params) {
        if (parts.length == 2) {
            if ("users".equals(parts[0])) {
                return getUser(parts[1]);
            } else if ("visits".equals(parts[0])) {
                return getVisit(parts[1]);
            } else if ("locations".equals(parts[0])) {
                return getLocation(parts[1]);
            }
        } else if (parts.length == 3) {
            if ("users".equals(parts[0]) && "visits".equals(parts[2])) {
                return getVisits(parts[1], params);
            } else if ("locations".equals(parts[0]) && "avg".equals(parts[2])) {
                return getAvgMark(parts[1], params);
            } else {
                return new Response(DataHolder.NOTFOUND_RESP);
            }
        } else {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
        return new Response(DataHolder.NOTFOUND_RESP);
    }


    public ClientApi(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }


    public Response getUser(String id) {
        try {
            Integer _id = Integer.valueOf(id);
            User user = dataHolder.getUser(_id);
            if (user == null) {
                return new Response(DataHolder.NOTFOUND_RESP);
            }
            return new Response(DataHolder.OK_RESP, user.toJson());
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response getLocation(String id) {
        try {
            Integer _id = Integer.valueOf(id);
            Location location = dataHolder.getLocation(_id);
            if (location == null) {
                return new Response(DataHolder.NOTFOUND_RESP);
            }
            return new Response(DataHolder.OK_RESP, location.toJson());
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response getVisit(String id) {
        try {
            Integer _id = Integer.valueOf(id);
            Visit visit = dataHolder.getVisit(_id);
            if (visit == null) {
                return new Response(DataHolder.NOTFOUND_RESP);
            }
            return new Response(DataHolder.OK_RESP, visit.toFullString().getBytes(StandardCharsets.UTF_8));
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response getVisits(String id, Object[] params) {
        Integer _id = null;
        try {
            _id = Integer.valueOf(id);
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.INCORRECT_RESP);
        }
        VisitResponse visits = dataHolder.getVisits(_id, params);
        return new Response(visits.getStatus(), visits.getVisits().getBytes(StandardCharsets.UTF_8));
    }

    public Response getAvgMark(String id, Object[] params) {
        Integer _id = null;
        try {
            _id = Integer.valueOf(id);
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.INCORRECT_RESP);
        }
        AvgResponse avgMark = dataHolder.getAvgMark(_id, params);
        return new Response(avgMark.getStatus(), getAvgMark(avgMark.getAvg()).getBytes(StandardCharsets.UTF_8));
    }

    public static String getAvgMark(Double avgMark) {
        if (avgMark == null) {
            return null;
        }
        return "{\"avg\":" + Double.toString(round(avgMark, 5)) + "}";

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public Response updateUser(String id, ByteBuf user) {
        try {
            Integer _id = Integer.valueOf(id);
            User _user = StreamBufParser.toUpdateUser(user);
            if (_user == null) {
                return new Response(DataHolder.INCORRECT_RESP);
            }
            Integer status = dataHolder.updateUser(_id, _user);
            return new Response(status);
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response updateVisit(String id, ByteBuf visit) {
        try {
            Integer _id = Integer.valueOf(id);
            Visit _visit = StreamBufParser.toUpdateVisit(visit);
            if (_visit == null) {
                return new Response(DataHolder.INCORRECT_RESP);
            }
            Integer status = dataHolder.updateVisit(_id, _visit);
            return new Response(status);
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response updateLocation(String id, ByteBuf location) {
        try {
            Integer _id = Integer.valueOf(id);
            Location _location = StreamBufParser.toUpdateLocation(location);
            if (_location == null) {
                return new Response(DataHolder.INCORRECT_RESP);
            }
            Integer status = dataHolder.updateLocation(_id, _location);
            return new Response(status);
        } catch (NumberFormatException ex) {
            return new Response(DataHolder.NOTFOUND_RESP);
        }
    }

    public Response addUser(ByteBuf user) {
        User _user = StreamBufParser.toAddUser(user);
        if (_user == null) {
            return new Response(DataHolder.INCORRECT_RESP);
        }
        Integer status = dataHolder.addUser(_user);
        return new Response(status);
    }

    public Response addVisit(ByteBuf visit) {
        Visit _visit = StreamBufParser.toAddVisit(visit);
        if (_visit == null) {
            return new Response(DataHolder.INCORRECT_RESP);
        }
        Integer status = dataHolder.addVisit(_visit);
        return new Response(status);
    }

    public Response addLocation(ByteBuf location) {
        Location _location = StreamBufParser.toAddLocation(location);
        if (_location == null) {
            return new Response(DataHolder.INCORRECT_RESP);
        }
        Integer status = dataHolder.addLocation(_location);
        return new Response(status);
    }
}
