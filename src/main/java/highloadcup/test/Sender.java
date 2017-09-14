package highloadcup.test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by d.asadullin on 17.08.2017.
 */
public class Sender {
    AsyncHttpClientConfig config;
    AsyncHttpClient httpClient;
    Logger logger = LoggerFactory.getLogger(Sender.class);
    private AtomicBoolean working=new AtomicBoolean(true);
    public Sender(){
        config = new AsyncHttpClientConfig.Builder().
                setConnectTimeout(3000000).
                setRequestTimeout(3000000)
                .build();
        httpClient = new AsyncHttpClient(config);
    }
     public void close(){
        try {
            httpClient.close();
        }catch (Exception ex){

        }
    }
    public void stop(){
        working.set(false);
    }

    public String get(String url)  {
        if(working.get()) {
            long start=System.currentTimeMillis();
            try {
                AsyncHttpClient.BoundRequestBuilder boundRequestBuilder;
                boundRequestBuilder = httpClient.prepareGet(url);
                boundRequestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
                boundRequestBuilder.addHeader("Connection", "keep-alive");
                Future<Response> future = boundRequestBuilder.execute();
                com.ning.http.client.Response response = future.get();

                return response.getStatusCode()+"/"+response.getResponseBody()+"/"+response.getHeaders();
            } catch (Exception ex) {
                long end=System.currentTimeMillis();
                logger.error("execution time: "+Long.toString(end-start), ex.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //
                }
                return null;
            }
        }else {
            return null;
        }
    }

    public String post(String url,String request) throws ExecutionException, InterruptedException, IOException {
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder;
        boundRequestBuilder = httpClient.preparePost(url);
        boundRequestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
        boundRequestBuilder.addHeader("Connection","keep-alive");
        boundRequestBuilder.setBody(request);
        Future<Response> future = boundRequestBuilder.execute();
        com.ning.http.client.Response response = future.get();
        return response.getResponseBody(StandardCharsets.UTF_8.name());
    }
}
