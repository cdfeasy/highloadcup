package highloadcup;

import highloadcup.entity.Visit;
import highloadcup.server.HttpServer;
import highloadcup.service.ClientApi;
import highloadcup.service.DataHolder;
import highloadcup.service.Deserializer;
import highloadcup.service.Init;
import highloadcup.test.HttpClient;
import highloadcup.test.Sender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by d.asadullin on 17.08.2017.
 */
public class ServiceTest {
    private void init(DataHolder holder) throws Exception {
        Init.initUsers(ServiceTest.class.getResourceAsStream("/users_1.json"), holder);
        Init.initLocations(ServiceTest.class.getResourceAsStream("/locations_1.json"), holder);
        Init.initVisits(ServiceTest.class.getResourceAsStream("/visits_1.json"), holder);
        Init.getOptions("d:\\work\\highloadcup\\data\\options.txt");
    }

    @Test
    public void testServer() throws Exception {
        DataHolder holder = new DataHolder();
        init(holder);
        ClientApi api = new ClientApi(holder);

        HttpServer server = new HttpServer(api, 8080);
        server.start();
        AtomicInteger cnt=new AtomicInteger();
        AtomicInteger success=new AtomicInteger();
        HttpClient client=new HttpClient(8080,cnt,success);
        // . /locations/114/avg?toDate=1462838400&toAge=52&fromAge=7
        String str = "{\n" +
                "  \"first_name\": \"Данила\",\n" +
                "  \"last_name\": \"Стыкушувич\",\n" +
                "  \"gender\": \"m\",\n" +
                "  \"id\": 100174,\n" +
                "  \"birth_date\": 843091200,\n" +
                "  \"email\": \"idsornawsotne@me.com\"\n" +
                "}";
        String str1 = "{\n" +
                "  \"first_name\": \"Данила\",\n" +
                "  \"last_name\": \"Стыкушувич\",\n" +
                "  \"gender\": \"m\",\n" +
                "  \"id\": 100175,\n" +
                "  \"birth_date\": 843091200,\n" +
                "  \"email\": \"idsornawsotne@me.com\"\n" +
                "}";
        String loca11 = "{\"distance\": 15, \"city\": \"Ньюлёв\", \"place\": \"Ручей1\", \"id\": 100001, \"country\": \"Италия\"}";
        String loca12 = "{\"distance\": 20, \"city\": \"Ньюлёв\", \"place\": \"Ручей2\", \"id\": 100002, \"country\": \"Италия\"}";
        String visit1 = "{\"user\": 100174, \"location\": 100001, \"visited_at\": 957879823, \"id\": 100001, \"mark\": 4}";
        String visit1_5 = "{\"user\": 100174, \"location\": 100001, \"visited_at\": 957879823, \"id\": 100002, \"mark\": 1}";
        String visit2 = "{\"user\": 100174, \"location\": 100002, \"visited_at\": 957879823, \"mark\": 1}";
//        System.out.println(client.post("http://127.0.0.1:8080/users/new?ololo=alala", str));
        // System.out.println(client.get("http://127.0.0.1:8080/users/8728/visits?toDistance=cedbedfceaeebcbacdfcbdeeffaebeda"));
//        System.out.println(client.get("http://127.0.0.1:8080/locations/112/avg?toDate=1215043200&fromDate=1497052800&toAge=54&fromAge=21&gender=m"));
//        System.out.println(client.get("http://127.0.0.1:8080/users/100174"));
//        System.out.println(client.get("http://127.0.0.1:8080/locations/112/avg?fromAge=10&country=Италия"));
//
//        System.out.println(client.post("http://127.0.0.1:8080/users/new?ololo=alala", str));
//       // System.out.println(client.get("http://127.0.0.1:8080/users/8728/visits?toDistance=cedbedfceaeebcbacdfcbdeeffaebeda"));
//        System.out.println(client.get("http://127.0.0.1:8080/locations/112/avg?toDate=1215043200&fromDate=1497052800&toAge=54&fromAge=21&gender=m"));
//        System.out.println(client.get("http://127.0.0.1:8080/users/100174"));
//        System.out.println(client.get("http://127.0.0.1:8080/locations/112/avg?fromAge=10&country=Италия"));
        client.post("http://127.0.0.1:8080/users/new", str);
        client.post("http://127.0.0.1:8080/users/new", str1);
        client.post("http://127.0.0.1:8080/locations/new", loca11);
        client.post("http://127.0.0.1:8080/locations/new", loca12);
        client.post("http://127.0.0.1:8080/visits/new", visit1);
        client.post("http://127.0.0.1:8080/visits/new", visit1_5);

        System.out.println(client.get("http://127.0.0.1:8080/users/100174"));
        System.out.println(client.get("http://127.0.0.1:8080/locations/100001/avg"));
        System.out.println(client.get("http://127.0.0.1:8080/locations/100002/avg"));
        client.post("http://127.0.0.1:8080/visits/100001", visit2);
        System.out.println(client.get("http://127.0.0.1:8080/locations/100001/avg"));
        System.out.println(client.get("http://127.0.0.1:8080/locations/100002/avg"));
        Thread.sleep(1000);
        client.close();
        //  Thread.sleep(1000000);
     //   System.out.println("bla"+"/"+cnt.get()+"/"+success.get());
        server.stop();
    }

//    @Test
//    public void testServer1() throws Exception {
//        DataHolder holder = new DataHolder();
//        init(holder);
//        ClientApi api = new ClientApi(holder);
//
//        HttpServer server = new HttpServer(api, 8080);
//        server.start();
//        AtomicInteger cnt=new AtomicInteger();
//        AtomicInteger success=new AtomicInteger();
//        HttpClient client=new HttpClient(8080,cnt,success);
//        Sender sender = new Sender();
//        // . /locations/114/avg?toDate=1462838400&toAge=52&fromAge=7
//        Heater.heating(holder,8080,System.currentTimeMillis(),100);
//        server.stop();
//    }


    @Test
   // @Ignore
    public void otherTest() throws Exception {
//        String str = "{\n" +
//                "  \"first_name\": \"Данила\",\n" +
//                "  \"last_name\": \"Стыкушувич\",\n" +
//                "  \"gender\": \"m\",\n" +
//                "  \"id\": 1100000,\n" +
//                "  \"birth_date\": 843091200,\n" +
//                "  \"email\": \"idsornawsotne@me.com\"\n" +
//                "}";
//        String str1="{\n" +
//                "  \"birth_date\": 47779200,\n" +
//                "  \"first_name\": \"Аркадий\"\n" +
//                "}";
//        DataHolder holder = new DataHolder();
//        init(holder);
//        ClientApi api = new ClientApi(holder);
//        System.out.println(api.addUser(new ByteArrayInputStream(str.getBytes())));
//        System.out.println(api.updateUser("1100000",new ByteArrayInputStream(str1.getBytes())));
//        System.out.println(api.getUser("1100000").toString());
//        "fdsf".getBytes();
//        ByteBuf b= Unpooled.directBuffer(100000);
//        Unpooled.copiedBuffer(new char[]{}, StandardCharsets.UTF_8);


//        String _user="{\"user\": 84, \"location\": 54, \"visited_at\": 957879823, \"id\": 1, \"mark\": 2";
//        Any deserialize = JsonIterator.deserialize(_user);
//        System.out.println(deserialize.as(User.class));

        ByteBuffer sb= ByteBuffer.allocate(100000);
        sb.put("ololo".getBytes());
        sb.put("ololo".getBytes());
        sb.put("alala".getBytes());
        sb.flip();
        byte[] bytes=new byte[sb.remaining()];
        sb.get(bytes);
        System.out.println(""+new String(bytes));



    }




//
//    @Test
//    public void TestJson1() throws IOException {
//        Integer i1=new Integer(1000);
//        Integer i2=Integer.valueOf(1000);
//        System.out.println(i1==i2);
//
//    }
//
//
    @Test
    public void TestJson2() throws IOException {
        long l=System.currentTimeMillis();
        long start=l;
        Visit[] array=new Visit[10];
        List<Visit> lst=new ArrayList<>(2);
        for (int i=0;i<10;i++){
            String visit1 = "{ \"id\": 100001,\"user\": 100174, \"location\": 100002, \"visited_at\": "+Long.toString(l)+", \"mark\": 1}";
            l++;
            Visit v1= Deserializer.toAddVisit(new ByteArrayInputStream(visit1.getBytes()));
            array[i]=v1;

        }

        lst.add(array[5]);
        lst.add(array[1]);
        lst.add(array[3]);
        lst.add(array[4]);
        lst.add(array[6]);
        lst.add(array[8]);
        lst.add(array[7]);
        lst.add(array[9]);
        lst.add(array[0]);
        Collections.sort(lst,DataHolder.vComp);
        lst.remove(array[1]);
        lst.remove(array[9]);
        lst.add(array[2]);
        Collections.sort(lst,DataHolder.vComp);
        System.out.println("----------"+Long.toString(l)+"---------------");
        for(Visit v:lst){
            System.out.println(v.toFullString());
        }
        List<Visit> newList=new ArrayList<>();
        Random r=new Random();
        for(Visit v:lst){
            if(r.nextBoolean()){
                newList.add(v);
            }
        }

        System.out.println("-------------------------");
        for(Visit v:newList){
            System.out.println(v.toFullString());
        }

    }


    @Test
    public void dateTest() {
        long time = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(10).toInstant().toEpochMilli();
        long d2 = 872085644 * 100000;
        System.out.println(new Date(d2));
        System.out.println(new Date(time));
        System.out.println(ZonedDateTime.now(ZoneId.systemDefault()).minusYears(49).toInstant());
        long time1 = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(49).toInstant().toEpochMilli();
        // -1022457600
        //-43068674126
        System.out.println(time1);
        System.out.println(new Date(time1));
        System.out.println(new Date(-1022457600));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(829612800);

        System.out.println(Year.now().getValue());
        System.out.println(calendar.get(Calendar.YEAR));
        System.out.println(new Integer(2017 - (new Date(829612800)).getYear()).byteValue());

        System.out.println(new Date(-1613433600));
        System.out.println(new Date(0));
        Date d = new Date(0);
        Date d1 = new Date(-345081600);
        System.out.println(d1.getTime());

        System.out.println(345081600d / (1000 * 60 * 60 * 24d));
        System.out.println(new Date(-345081600));
        System.out.println(-345081600d / (1000 * 60 * 60 * 24d));

        System.out.println(new Date(-1613433600).after(new Date(829612800)));
    }


}
