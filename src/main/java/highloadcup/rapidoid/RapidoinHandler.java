package highloadcup.rapidoid;

import highloadcup.server.ApiHandler;
import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import io.netty.handler.codec.http.HttpMethod;
import org.rapidoid.buffer.Buf;
import org.rapidoid.bytes.BytesUtil;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpResponseCodes;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.MediaType;
import org.rapidoid.net.Server;
import org.rapidoid.net.TCP;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by d.asadullin on 24.08.2017.
 */
public class RapidoinHandler extends AbstractHttpServer {
    private ClientApi api;

    public RapidoinHandler(ClientApi api) {
        this.api = api;

    }

    @Override
    public void process(Channel ctx) {
        if (ctx.isInitial()) {
            return;
        }
        Buf buf = ctx.input();
        RapidoidHelper data = ctx.helper();
        HTTP_PARSER.parse(buf, data);
        boolean keepAlive = data.isKeepAlive.value;
        _handle(ctx, buf, data);
        ctx.closeIf(!keepAlive);
    }

    @Override
    protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper data) {
        return null;
    }

    protected void startResponse(Channel ctx, boolean isKeepAlive, int status) {
        ctx.write(HttpResponseCodes.get(status));
        writeCommonHeaders(ctx, isKeepAlive);
    }


    protected void _handle(Channel ctx, Buf buf, RapidoidHelper req) {
        String protocol = BytesUtil.get(buf.bytes(), req.verb);
        String path = BytesUtil.get(buf.bytes(), req.path);
        String query = BytesUtil.get(buf.bytes(), req.query);
        Object[] uriToQueryArray = null;
        if ("GET".equals(protocol)) {
            if (query != null && query.length() != 0) {
                uriToQueryArray = ApiHandler.uriToQueryArray(query);
                if (uriToQueryArray != null && "error".equals(uriToQueryArray[0])) {
                    ClientApi.Response response = new ClientApi.Response(DataHolder.INCORRECT_RESP);
                    startResponse(ctx, req.isKeepAlive.value, response.getStatus());
                    writeBody(ctx, (response.getResponse() != null ? response.getResponse() : "{}".getBytes()), MediaType.APPLICATION_JSON);
                    return;
                }
            }
        }

        InputStream is = null;
        if ("POST".equals(protocol)) {
            byte[] body = BytesUtil.getBytes(buf.bytes(), req.body);
            is = new ByteArrayInputStream(body);
        }
        ClientApi.Response response = null;//api.request(path, protocol, uriToQueryArray, is);
        startResponse(ctx, req.isKeepAlive.value, response.getStatus());
        writeBody(ctx, (response.getResponse() != null ? response.getResponse() : "{}".getBytes()), MediaType.APPLICATION_JSON);
    }

    public Server listen(String address, int port) {
        return TCP.server()
                .protocol(this)
                .address(address)
                .port(port)
                .syncBufs(true)
                .build()
                .start();
    }
}
