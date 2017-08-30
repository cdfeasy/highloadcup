package highloadcup.service;

import highloadcup.test.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by d.asadullin on 24.08.2017.
 */
public class Heater {
    private static Logger logger = LoggerFactory.getLogger(Heater.class);



    public static void connectionHeater(DataHolder holder, int port, int count, long sleepTime) {
        ExecutorService es = Executors.newFixedThreadPool(20);
        holder.printLastLocation();
        holder.printLastUser();
        holder.printLastVisit();
        final AtomicInteger cnt = new AtomicInteger();
        final AtomicInteger success = new AtomicInteger();
        final String str = "{\n" +
                "  \"first_name\": \"Данила\",\n" +
                "  \"last_name\": \"Стыкушувич\",\n" +
                "  \"gender\": \"m\",\n" +
                "  \"id\": 100174,\n" +
                "  \"birth_date\": 843091200,\n" +
                "  \"email\": \"idsornawsotne@me.com\"\n" +
                "}";
        HttpClient sender=new HttpClient(port,cnt,success);
        for (int j = 0; j < count; j++) {
            es.submit(() -> {
                try {
                   // cnt.getAndAdd(2);
                    try {
                        if (sender.get("http://127.0.0.1:" + Integer.toString(port) + "/users/" + holder.getRandomUserId()) != null) {
                  //          success.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        //
                    }
                    try {
                        if (sender.post("http://127.0.0.1:" + Integer.toString(port) + "/users/"+holder.getRandomUserId(), str) != null) {
                           // success.incrementAndGet();
                        }
                    } catch (Exception e) {
                        //
                    }
                } finally {
                   // sender.stop();
                   // sender.close();
                }
            });
            es.submit(() -> {
             //   Sender sender = new Sender();
                try {
                    try {
                     //   cnt.incrementAndGet();
                        if (sender.post("http://127.0.0.1:" + Integer.toString(port) + "/locations/"+holder.getRandomLocationId(), str) != null) {
                          //  success.incrementAndGet();
                        }
                    } catch (Exception e) {
                        //
                    }
                } finally {
                 //   sender.stop();
                  //  sender.close();
                }
            });
            es.submit(() -> {
               // Sender sender = new Sender();
                try {
                    try {
                     //   cnt.incrementAndGet();
                        if (sender.post("http://127.0.0.1:" + Integer.toString(port) + "/visits/"+holder.getRandomUserId(), str) != null) {
                       //     success.incrementAndGet();
                        }
                    } catch (Exception e) {
                        //
                    }
                } finally {
                   // sender.stop();
                   // sender.close();
                }
            });
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            //
        }
        es.shutdown();
        try {
            try {
                es.awaitTermination(30, TimeUnit.SECONDS);
            } catch (Exception ex) {
                //
            }
            if (!es.isShutdown()) {
                es.shutdownNow();
                es.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
        }
        sender.close();
        logger.info("ConnectionHeater complete {}/{}", success.get(), cnt.get());
    }


    public static void heating(DataHolder holder, int port, long start, int time) {
        long end = start + time * 1000;
        long start1 = System.currentTimeMillis();
        ScheduledExecutorService es = Executors.newScheduledThreadPool(4);
        Random r = new Random();
        final AtomicInteger cnt = new AtomicInteger();
        final AtomicInteger success = new AtomicInteger();
        HttpClient sender=new HttpClient(port,cnt,success);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 20000; j++) {
                int userId = holder.getRandomUserId();
                es.submit(() -> {
                    String s = sender.get("http://127.0.0.1:" + Integer.toString(port) + "/users/" + userId);
                });
            }
            for (int j = 0; j < 20000; j++) {
                int locationId = holder.getRandomLocationId();
                es.submit(() -> {
                    String s = sender.get("http://127.0.0.1:" + Integer.toString(port) + "/locations/" + locationId);
                });
            }
            for (int j = 0; j < 20000; j++) {
                int visitId = holder.getRandomVisitId();
                es.submit(() -> {
                    String s = sender.get("http://127.0.0.1:" + Integer.toString(port) + "/visits/" + visitId);
                });
            }
        }

        es.scheduleWithFixedDelay(() -> {
            try {
                Integer locationId = holder.getRandomLocationId();
                String s = sender.get(String.format("http://127.0.0.1:%d/locations/%d/avg?fromAge=%d&toAge=%d&gender=%s", port, locationId, r.nextInt(10), r.nextInt(80), (r.nextInt(1) == 0 ? "m" : "f")));
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                Integer locationId = holder.getRandomLocationId();
                String s = sender.get(String.format("http://127.0.0.1:%d/locations/%d/avg?fromAge=%d&toAge=%d&gender=%s&fromDate=%d", port, locationId, r.nextInt(10), r.nextInt(80), (r.nextInt(1) == 0 ? "m" : "f"), r.nextInt()));
            } catch (Exception ex) {
            }
        }, 5, 10, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                Integer locationId = holder.getRandomLocationId();
                String s = sender.get(String.format("http://127.0.0.1:%d/locations/%d/avg?fromAge=%d&toAge=%d&gender=%s&toDate=%d", port, locationId, r.nextInt(10), r.nextInt(80), (r.nextInt(1) == 0 ? "m" : "f"), r.nextInt()));
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                Integer userId = holder.getRandomUserId();
                String s = sender.get(String.format("http://127.0.0.1:%d/users/%d/visits?toDistance=%d", port, userId, r.nextInt(50)));
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                Integer userId = holder.getRandomUserId();
                String s = sender.get(String.format("http://127.0.0.1:%d/users/%d/visits?toDistance=%d&toDate=%d", port, userId, r.nextInt(50), r.nextInt()));
            } catch (Exception ex) {
            }
        }, 5, 10, TimeUnit.MILLISECONDS);

        es.scheduleWithFixedDelay(() -> {
            try {
                Integer userId = holder.getRandomUserId();
                String s = sender.get(String.format("http://127.0.0.1:%d/users/%d", port, userId));
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);

        es.scheduleWithFixedDelay(() -> {
            try {
                Integer location = holder.getRandomLocationId();
                String s = sender.get(String.format("http://127.0.0.1:%d/locations/%d", port, location));
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);

        if (end - System.currentTimeMillis() > 0) {
            try {
                Thread.sleep(end - System.currentTimeMillis());
                sender.close();
                es.shutdown();
            } catch (Exception ex) {

            }
        } else {
            sender.close();
            es.shutdown();
        }
        try {
            es.awaitTermination(1, TimeUnit.SECONDS);
            if (!es.isShutdown()) {
                es.shutdownNow();
                es.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            //
        }
        long end1 = System.currentTimeMillis();
        logger.info("highHeating complete {}/{} time {}", success.get(), cnt.get(), end1 - start1);
    }

    public static void heatingHolder(ClientApi api, long start, int time) {
        long end = start + time * 1000;
        long start1 = System.currentTimeMillis();
        ScheduledExecutorService es = Executors.newScheduledThreadPool(4);
        Random r = new Random();
        final AtomicInteger cnt = new AtomicInteger();
        final AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 30000; j++) {
                int userId = api.getDataHolder().getRandomUserId();
                es.submit(() -> {
                    cnt.incrementAndGet();
                    ClientApi.Response get = api.request("/users/" + userId, "GET", null, null);
                    if (get != null && get.getStatus()==200) {
                        success.incrementAndGet();
                    }
                });
            }
            for (int j = 0; j < 30000; j++) {
                int locationId = api.getDataHolder().getRandomLocationId();
                es.submit(() -> {
                    cnt.incrementAndGet();
                    ClientApi.Response s = api.request("/locations/" + locationId, "GET", null, null);
                    if (s != null&& s.getStatus()==200) {
                        success.incrementAndGet();
                    }
                });
            }
            for (int j = 0; j < 30000; j++) {
                int visitId = api.getDataHolder().getRandomVisitId();
                es.submit(() -> {
                    cnt.incrementAndGet();
                    ClientApi.Response s = api.request("/visits/" + visitId, "GET", null, null);
                    if (s != null&& s.getStatus()==200) {
                        success.incrementAndGet();
                    }
                });
            }
        }

        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer locationId = api.getDataHolder().getRandomLocationId();
                String request=String.format("/locations/%d/avg", locationId);
                Object[] req=new Object[7];
                req[4]=r.nextInt(20);
                req[5]=r.nextInt(80);
                req[6]=new Boolean(r.nextInt(1) == 0);
                ClientApi.Response s = api.request(request, "GET", req, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {

            }
        }, 5, 5, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer locationId = api.getDataHolder().getRandomLocationId();
                String request=String.format("/locations/%d/avg", locationId);
                Object[] req=new Object[7];
                req[4]=r.nextInt(20);
                req[0]=Long.valueOf(r.nextInt());
                req[6]=new Boolean(r.nextInt(1) == 0);
                ClientApi.Response s = api.request(request, "GET", req, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {
            } finally {
            }
        }, 5, 5, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer locationId = api.getDataHolder().getRandomLocationId();
                String request=String.format("/locations/%d/avg", locationId);
                Object[] req=new Object[7];
                req[4]=r.nextInt(20);
                req[1]=Long.valueOf(r.nextInt());
                req[6]=new Boolean(r.nextInt(1) == 0);
                ClientApi.Response s = api.request(request, "GET", req, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {

            }
        }, 5, 5, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer userId = api.getDataHolder().getRandomUserId();
                String request=String.format("/users/%d/visits", userId);
                Object[] req=new Object[7];
                req[1]=Long.valueOf(r.nextInt());
                req[3]=r.nextInt(20);
                ClientApi.Response s = api.request(request, "GET", req, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {

            }
        }, 5, 5, TimeUnit.MILLISECONDS);
        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer userId = api.getDataHolder().getRandomUserId();
                String request=String.format("/users/%d/visits", userId);
                ClientApi.Response s = api.request(request, "GET", null, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {
            }
        }, 5, 10, TimeUnit.MILLISECONDS);

        es.scheduleWithFixedDelay(() -> {
            try {
                cnt.incrementAndGet();
                Integer userId = api.getDataHolder().getRandomUserId();
                String request=String.format("/users/%d", userId);
                Object[] req=new Object[7];
                req[0]=Long.valueOf(r.nextInt());
                req[3]=r.nextInt(20);
                ClientApi.Response s = api.request(request, "GET", req, null);
                if (s != null &&  s.getStatus()==200) {
                    success.incrementAndGet();
                }
            } catch (Exception ex) {

            }
        }, 5, 10, TimeUnit.MILLISECONDS);

        if (end - System.currentTimeMillis() > 0) {
            try {
                Thread.sleep(end - System.currentTimeMillis());
                es.shutdown();
            } catch (Exception ex) {

            }
        } else {
            es.shutdown();
        }
        try {
            es.awaitTermination(1, TimeUnit.SECONDS);
            if (!es.isShutdown()) {
                es.shutdownNow();
                es.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            //
        }
        long end1 = System.currentTimeMillis();
        logger.info("innerHeating complete {}/{} time {}", success.get(), cnt.get(), end1 - start1);
    }


}
