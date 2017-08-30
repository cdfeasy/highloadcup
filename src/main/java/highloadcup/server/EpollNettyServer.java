package highloadcup.server;

import highloadcup.AbstractServer;
import highloadcup.rapidoid.RapidoinHandler;
import highloadcup.service.ClientApi;
import org.rapidoid.net.Server;

/**
 * Created by dmitry on 27.08.2017.
 */
public class EpollNettyServer extends AbstractServer {
    EpollHttpServer server;

    public EpollNettyServer(int port, String[] args) {
        super(port, args);
    }

    @Override
    public void start() throws InterruptedException {
        server.start();
    }

    @Override
    public void close() {
        server.stop();
    }

    @Override
    public void join() throws InterruptedException {
        server.join();
    }

    @Override
    public void create(ClientApi api) {
        server = new EpollHttpServer(api, port);
    }
}
