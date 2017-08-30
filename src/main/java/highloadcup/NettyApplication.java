package highloadcup;

import highloadcup.server.EpollNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by d.asadullin on 18.08.2017.
 */
public class NettyApplication {
    private static Logger logger = LoggerFactory.getLogger(NettyApplication.class);
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("empty args");
        }
        int port = 80;
        EpollNettyServer server=new EpollNettyServer(port,args);
        server.init();
        server.join();
    }
}
