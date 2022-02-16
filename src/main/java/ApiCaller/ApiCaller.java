package ApiCaller;

import Entities.AdvancedQueryCall;
import Entities.Brand;
import Entities.Location;
import Entities.Product;
import com.google.gson.Gson;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Getter

public class ApiCaller extends Thread {

    private int api_caller_id;
    private List<ApiCall> apiCalls;
    private int amountOfCalls;
    private StopWatch stopWatch;
    private Random random;
    private boolean doneWithCalls;
    private Call whatCallToMake;
    private String api_url;

    private Gson gson;
    private List<AdvancedQueryCall> advQueryPool;
    private String apiCall_url;

    public ApiCaller(int amountOfCalls, Call whatCallToMake, String api_url, List<AdvancedQueryCall> advQueryPool, int number){

        this.api_caller_id = number;
        this.amountOfCalls = amountOfCalls;
        this.stopWatch = new StopWatch();
        this.random = new Random();
        doneWithCalls = false;
        this.whatCallToMake = whatCallToMake;
        this.gson = new Gson();
        this.api_url = api_url;
        this.advQueryPool = advQueryPool;
        apiCalls = new ArrayList<>();

    }

    @Override
    public void run() {

        // When we Start the individual Thread we want to make as many calls as been provided in the Main

        int callsMade = 0;

        List<Product> products;

        while(callsMade < amountOfCalls){

            AdvancedQueryCall advQuery = advQueryPool.get(random.nextInt(1000));

            products = new ArrayList<>();

            stopWatch.startTimer();

            switch(whatCallToMake){

                case GETBYID -> {}

                case GETBYPAGE -> {}

                case GETPAGEBYCATHEGORY -> {}

                case AdvancedQuery -> {

                    products = advancedQuery(advQuery, api_url);

                }

            }

            callsMade++;

            apiCalls.add(new ApiCall(whatCallToMake, stopWatch.stopTimer(), products, advQuery, apiCall_url));

        }

        doneWithCalls = true;


    }

    public List<Product> advancedQuery(AdvancedQueryCall advQuery, String url){

        List<Product> products = new ArrayList<>();

        url += "/" + advQuery.getPrice_min() + "/" + advQuery.getPrice_max() + "/";

        for(Location l : advQuery.getLocations()){

            url += l.id + "&";
        }

        url = url.substring(0, url.length() - 1);

        url += "/";

        for(Brand b : advQuery.getBrands()){

            url += b.getId() + "&";
        }

        url = url.substring(0, url.length() - 1);

        this.apiCall_url = url;

        try{


            URL url_connect = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) url_connect.openConnection();

            conn.setRequestMethod("GET");

            conn.connect();

            int responseCode = conn.getResponseCode();

            if(responseCode != 200){

            }else{

                StringBuilder informationString = new StringBuilder();

                Scanner scanner = new Scanner(url_connect.openStream());

                while(scanner.hasNext()){

                    informationString.append(scanner.nextLine());
                }

                scanner.close();

                JSONParser parser = new JSONParser();

                JSONArray array = (JSONArray) parser.parse(informationString.toString());

                array.forEach(o -> {

                    Product product = gson.fromJson(o.toString(), Product.class);

                    products.add(product);

                });

            }

        }catch (Exception e){

            e.printStackTrace();
        }

        return products;

    }

}
