package ApiCaller;

import Entities.AdvancedQueryCall;
import Entities.Brand;
import Entities.Location;
import Entities.Product;
import com.google.gson.Gson;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class ApiCallerStation {

    private long total_time;
    private String url;
    private int callsPerThread;
    private List<ApiCaller> threads;
    private List<AdvancedQueryCall> advQueryPool;



    @Getter
    private int[] millis0to50;
    @Getter
    private int[] millis0to200;
    private Random random;
    private Gson gson;
    @Getter
    private AtomicBoolean areTheThreadsDone;

    public ApiCallerStation(String api_url, int numberOfThreads, int callsPerThread, int offset, Call whatCallToMake)

            throws IOException {

        this.random = new Random();
        this.gson = new Gson();
        this.url = api_url;
        this.callsPerThread = callsPerThread + offset;
        whatCallToMake = whatCallToMake;
        advQueryPool = randomiseManyQueries();
        areTheThreadsDone = new AtomicBoolean();
        areTheThreadsDone.set(false);
        threads = new ArrayList<>();

        this.url = "http://localhost:8081/api/" + api_url + "/" + whatCallToMake.getCall();

        for (int i = 0; i < numberOfThreads; i++) {

            ApiCaller apiCT = new ApiCaller(this.callsPerThread, whatCallToMake, url, advQueryPool, i + 1);

            threads.add(apiCT);

        }

        long then = System.currentTimeMillis();

        threads.forEach(Thread::start);

        do{

            areTheThreadsDone.set(true);

            threads.forEach(apiCaller -> {

                if(!apiCaller.isDoneWithCalls()){

                    areTheThreadsDone.set(false);
                }

            });

        }while(!areTheThreadsDone.get());

        total_time = System.currentTimeMillis() - then;

        // Sedan här ska vi printa ut resultatet och helst samla det i en fin tabell

        // Kanske trycka in det i mySQL?

        for (int i = 0; i < offset; i++) {

            threads.forEach(apiCaller -> apiCaller.getApiCalls().remove(0));

        }

        List<ApiCall> allCalls = new ArrayList<>();

        threads.forEach(apiCaller -> allCalls.addAll(apiCaller.getApiCalls()));

        millis0to200 = new int[5];

        millis0to50 = new int[5];

        for(ApiCall c : allCalls){

            long millis = c.getTimeInMillis();

            if(millis <= 50){

                millis0to200[0]++;

                if(millis <= 10){

                    millis0to50[0]++;
                    continue;

                }

                if(millis < 20){

                    millis0to50[1]++;
                    continue;
                }

                if(millis <= 30){

                    millis0to50[2]++;
                    continue;
                }

                if(millis <= 40){

                    millis0to50[3]++;
                    continue;
                }

                millis0to50[4]++;
                continue;

            }
            if(millis <= 100){

                millis0to200[1]++;
                continue;
            }
            if(millis <= 150){

                millis0to200[2]++;
                continue;
            }

            if(millis <= 200){

                millis0to200[3]++;
                continue;
            }

            millis0to200[4]++;

        }

        sendResultsToMySQL(new ApiTestResults(millis0to50, millis0to200, api_url, numberOfThreads, callsPerThread, (int) total_time));

    }

    void sendResultsToMySQL(ApiTestResults apiTR) {

        String url = "http://localhost:8081/api/sql/postResults";


        try {

            URL UrlObj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) UrlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            String testResultsJSON = gson.toJson(apiTR);
            outputStream.writeBytes(testResultsJSON);

            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader inputReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = inputReader.readLine()) != null) {
                    response.append(inputLine);
                }
                inputReader.close();

                System.out.println(response.toString());
            }

        } catch (IOException e) {

            e.printStackTrace();
        }


        System.out.println("Send 'HTTP POST' request to : " + url);

    }

    List<AdvancedQueryCall> randomiseManyQueries(){

        List<AdvancedQueryCall> calls = new ArrayList<>();

        for(int i = 1; i <= 1000; i++){

            int startingPrice = random.nextInt(5000);

            int highestPrice = random.nextInt(5000) + startingPrice;

            Brand[] brands = new Brand[random.nextInt(4) + 1];

            int j = 0;

            int k = 0;

            do {

                brands[j] = Brand.values()[j];

                j++;

            }while( j < brands.length );

            Location[] locations = new Location[random.nextInt(4) + 1];

            do {

                locations[k] = Location.values()[k];

                k++;

            }while(k < locations.length);

            AdvancedQueryCall call = new AdvancedQueryCall(startingPrice, highestPrice, locations, brands);

            calls.add(call);

        }

        return calls;

    }

    public void post (Product product) {


       try{

           URL url = new URL("localhost:8080/lua/");

           HttpURLConnection conn = (HttpURLConnection) url.openConnection();

           conn.setRequestMethod("GET");

           conn.connect();

           int responseCode = conn.getResponseCode();

           if(responseCode != 200){

               System.out.println("Response Code : " + responseCode);
           }else{

               StringBuilder informationString = new StringBuilder();

               Scanner scanner = new Scanner(url.openStream());

               while(scanner.hasNext()){

                   informationString.append(scanner.nextLine());
               }

               scanner.close();

               System.out.println(informationString.toString());


           }

       }catch (Exception e){

       }

    }

    public List<ApiCall> getAllApiCallsDone(){

        List<ApiCall> apiCalls = new ArrayList<>();

        for(ApiCaller caller : threads) {

            for(ApiCall apiCall : caller.getApiCalls()){

                apiCalls.add(apiCall);
            }

        }

        return apiCalls;

    }

}
