package highloadcup;
import highloadcup.rapidoid.RapidoidServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by d.asadullin on 24.08.2017.
 */
public class RapidoidApplication {
    private static Logger logger = LoggerFactory.getLogger(RapidoidApplication.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("empty args");
        }
        int port = 80;
        RapidoidServer server=new RapidoidServer(port,args);
        server.init();
        server.join();
    }
}
