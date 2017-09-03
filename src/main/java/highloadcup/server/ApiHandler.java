package highloadcup.server;

import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.rapidoid.commons.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dmitry on 20.08.2017.
 */
public class ApiHandler {
    public final static int from_date_idx = 0;
    public final static int to_date_idx = 1;
    public final static int county_idx = 2;
    public final static int distance_idx = 3;
    public final static int from_age_idx = 4;
    public final static int to_age_idx = 5;
    public final static int gender_idx = 6;

    private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

    private static String parseEndpoint(String uri) {
        String endpoint = uri.split("\\?")[0];
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length());
        }
        return endpoint;
    }

    private static Map<String, String> queryStringHandler(String uri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        if (queryStringDecoder.parameters().size() > 0) {
            Map<String, String> res = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : queryStringDecoder.parameters().entrySet())
                if (entry.getValue().size() > 0) {
                    res.put(entry.getKey(), entry.getValue().get(0));
                }
            return res;
        }
        return null;
    }


    private static InputStream requestBodyHandler(Object msg) {
        if (msg instanceof HttpContent) {
            ByteBuf content = ((HttpContent) msg).content();
            return new ByteBufInputStream(content);
        }
        return null;
    }

    private static char decodeHexNibble(final char c) {
        if ('0' <= c && c <= '9') {
            return (char) (c - '0');
        } else if ('a' <= c && c <= 'f') {
            return (char) (c - 'a' + 10);
        } else if ('A' <= c && c <= 'F') {
            return (char) (c - 'A' + 10);
        } else {
            return Character.MAX_VALUE;
        }
    }

    public static String decodeComponent(final String s, final Charset charset) {
        if (s == null) {
            return "";
        }
        final int size = s.length();
        boolean modified = false;
        for (int i = 0; i < size; i++) {
            final char c = s.charAt(i);
            if (c == '%' || c == '+') {
                modified = true;
                break;
            }
        }
        if (!modified) {
            return s;
        }
        final byte[] buf = new byte[size];
        int pos = 0;  // position in `buf'.
        for (int i = 0; i < size; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    buf[pos++] = ' ';  // "+" -> " "
                    break;
                case '%':
                    if (i == size - 1) {
                        throw new IllegalArgumentException("unterminated escape"
                                + " sequence at end of string: " + s);
                    }
                    c = s.charAt(++i);
                    if (c == '%') {
                        buf[pos++] = '%';  // "%%" -> "%"
                        break;
                    }
                    if (i == size - 1) {
                        throw new IllegalArgumentException("partial escape"
                                + " sequence at end of string: " + s);
                    }
                    c = decodeHexNibble(c);
                    final char c2 = decodeHexNibble(s.charAt(++i));
                    if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE) {
                        throw new IllegalArgumentException(
                                "invalid escape sequence `%" + s.charAt(i - 1)
                                        + s.charAt(i) + "' at index " + (i - 2)
                                        + " of: " + s);
                    }
                    c = (char) (c * 16 + c2);
                    // Fall through.
                default:
                    buf[pos++] = (byte) c;
                    break;
            }
        }
        return new String(buf, 0, pos, charset);
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static Object[] urlToQueryArray(String uri) {
        String[] reqParts = uri.split("\\?");
        if (reqParts.length == 2 && reqParts[1] != null && reqParts[1].length() != 0) {
            return uriToQueryArray(reqParts[1]);
        }
        return null;
    }

    public static Object[] uriToQueryArray(String uri) {
        Object[] res = new Object[7];
        String query = uri;
        String[] parts = query.split("&");
        try {
            for (String part : parts) {
                if (part.length() > 0) {
                    String[] keyVal = part.split("=");
                    if (keyVal.length == 2) {
                        int length = keyVal[0].length();
                        if (length == 10 && "toDistance".equals(keyVal[0])) {
                            if (!isNumeric(keyVal[1])) {
                                res[0] = "error";
                                break;
                            }
                            res[distance_idx] = Integer.valueOf(keyVal[1]);
                        } else if (length == 8 && "fromDate".equals(keyVal[0])) {
                            if (!isNumeric(keyVal[1])) {
                                res[0] = "error";
                                break;
                            }
                            res[from_date_idx] = Long.valueOf(keyVal[1]);
                        } else if (length == 7 && "country".equals(keyVal[0])) {
                            res[county_idx] = decodeComponent(keyVal[1], StandardCharsets.UTF_8);
                        } else if (length == 7 && "fromAge".equals(keyVal[0])) {
                            if (!isNumeric(keyVal[1])) {
                                res[0] = "error";
                                break;
                            }
                            res[from_age_idx] = Integer.valueOf(keyVal[1]);
                        } else if (length == 6 && "toDate".equals(keyVal[0])) {
                            if (!isNumeric(keyVal[1])) {
                                res[0] = "error";
                                break;
                            }
                            res[to_date_idx] = Long.valueOf(keyVal[1]);
                        } else if (length == 6 && "gender".equals(keyVal[0])) {
                            if (!("m".equals(keyVal[1]) || "f".equals(keyVal[1]))) {
                                res[0] = "error";
                                break;
                            }
                            res[gender_idx] = new Boolean("m".equals(keyVal[1]));
                        } else if (length == 5 && "toAge".equals(keyVal[0])) {
                            if (!isNumeric(keyVal[1])) {
                                res[0] = "error";
                                break;
                            }
                            res[to_age_idx] = Integer.valueOf(keyVal[1]);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            parts[0] = "error";
        }
        return res;
    }

    public static ClientApi.Response transfer(ClientApi api, ByteBuf buf, boolean isPost, String uri) {
        //   ApiProtocol apiProtocol = new ApiProtocol(msg);
        try {
            String endpoint = parseEndpoint(uri);
            Object[] uriToQueryArray = null;
            if (!isPost) {
                uriToQueryArray = urlToQueryArray(uri);
                if (uriToQueryArray != null && "error".equals(uriToQueryArray[0])) {
                    return new ClientApi.Response(DataHolder.INCORRECT_RESP);
                }
            }
            ClientApi.Response response = api.request(endpoint, isPost, uriToQueryArray, buf);
            return response;
        }catch (Exception ex){
            logger.error("error",ex);
            return new ClientApi.Response(DataHolder.INCORRECT_RESP);
        }
    }


}
