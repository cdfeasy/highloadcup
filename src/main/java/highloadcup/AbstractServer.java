package highloadcup;

import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import highloadcup.service.Heater;
import highloadcup.service.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dmitry on 27.08.2017.
 */
public abstract class AbstractServer {
    protected static Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    protected int port;
    protected DataHolder holder;
    protected ClientApi api;
    protected String[] args;

    public AbstractServer(int port, String[] args) {
        this.port = port;
        this.args = args;
    }

    public abstract void start() throws InterruptedException;

    public abstract void close();

    public abstract void join() throws InterruptedException;

    public abstract void create(ClientApi api);

    public void init() throws Exception {
        long start = System.currentTimeMillis();
        holder = new DataHolder();
        Init.unZipIt(holder, args[0]);
        String battle = Init.getOptions(args[1]);
        System.gc();
        logger.info("inited {}", battle);
        api = new ClientApi(holder);
        create(api);
        start();

        if ("0".equals(battle)) {
            Heater.connectionHeater(holder, port, 10000, 10l);
            Heater.heatingHolder(api, start, 40);
            Heater.heating(holder, port, start, 55);
        }
        if ("1".equals(battle)) {
            Heater.connectionHeater(holder, port, 10000, 10l);
            Heater.heatingHolder(api, start, 450);
            Heater.heating(holder, port, start, 560);
          //  System.gc();
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

}
