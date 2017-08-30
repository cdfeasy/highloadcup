package highloadcup.rapidoid;

import highloadcup.AbstractServer;
import highloadcup.service.ClientApi;
import org.rapidoid.net.Server;

/**
 * Created by dmitry on 27.08.2017.
 */
public class RapidoidServer extends AbstractServer {
    RapidoinHandler server;
    Server listen;

    public RapidoidServer(int port, String[] args) {
        super(port, args);
    }

    @Override
    public void start() {
        listen = server.listen(port);
    }

    @Override
    public void close() {
        listen.shutdown();
    }

    @Override
    public void join() {

    }

    @Override
    public void create(ClientApi api) {
        server = new RapidoinHandler(api);
    }
}
