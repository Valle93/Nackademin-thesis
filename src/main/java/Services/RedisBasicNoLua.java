package Services;

import Entities.*;
import RedisStreamApi.PMain;
import com.google.gson.Gson;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisBasicNoLua implements Service {

    public static final String postProduct_successful = "Product Added";

    private JedisStoreDB jedcon;

    public RedisBasicNoLua(JedisStoreDB jedcon){

        this.jedcon = jedcon;
        Gson gson = new Gson();
    }

    public List<Product> getAllProducts(){

        Set<String> stringProducts = (Set<String>) jedcon.call(jedis -> jedis.zrange(RedisKey.listOfAllProductids, 0, -1));

        List<Product> products = new ArrayList<>();

        for(String s: stringProducts){

            products.add(PMain.gson.fromJson(getProduct(s), Product.class));

        }

        return products;
    }

    public List<String> advancedQuery(AdvancedQueryCall call){

        List<Response<String>> productsJSONresponse = new ArrayList<>();

        List<String> productsJSON = new ArrayList<>();

        List<String> productids = new ArrayList<>();

        // Först en zrange mellan alla produkter price_min och price_max?

            // zunionstore

        List<Response> responses = new ArrayList<>();

        try {
            jedcon.call(jedis -> {

            Pipeline pipeline = jedis.pipelined();
            pipeline.zunionstore("query:locations", call.getLocationsWithPlaceHolder());
            pipeline.zunionstore("query:brands", call.getBrandsWithPlaceHolder());
            pipeline.zinterstore("query:brands_locations", "query:locations", "query:brands");
            pipeline.zinterstore("query:all-products", RedisKey.listOfAllProductids,
                    "query:brands_locations");
            Response<List<String>> response = pipeline.zrangeByScore("query:all-products", call.getPrice_min(),
                    call.getPrice_max());
            pipeline.sync();
            for(String s : response.get()){
                productsJSONresponse.add(pipeline.get(s));
            }
            pipeline.sync();

            pipeline.close();

                return null;

            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        productsJSONresponse.forEach(stringResponse -> productsJSON.add(stringResponse.get()));

        return productsJSON;

    }

    public Set<String> getAllProductIDS(){

        Set<String> productIDS = (Set<String>) jedcon.call(jedis -> jedis.zrange(RedisKey.listOfAllProductids, 0, -1));

        return productIDS;
    }

    public void flushSystem(){

        jedcon.flushRedisDatabase();
    }

    public String updateProduct(Product product, String productid){

        String productJSON = PMain.gson.toJson(product);

        try{

            jedcon.call(jedis -> jedis.set(productid, productJSON));
        }catch (Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return productJSON;
    }

/*
    public List<Product> getProductsByPage(int pageNumber){

        jedcon.call(jedis -> jedis.zrange(RedisKey.listOfAllProductids, ))
    }
*/

    public Set<String> getProductsByPageJSON(int page){

        int pageNr = page - 1;

        Set<String> productIDs = (Set<String>) jedcon.call(jedis ->
                jedis.zrange(RedisKey.listOfAllProductids, pageNr, pageNr + 10));

        Set<String> productsJSON = new HashSet<>();

        for(String s : productIDs){

            String productJSON = getProduct(s);

            productsJSON.add(productJSON);

        }

        return productsJSON;

    }

    public List<Product> getProductsByPage(int page){

        int pageNr = page - 1;

        Set<String> productIDs = (Set<String>) jedcon.call(jedis ->
                jedis.zrange(RedisKey.listOfAllProductids, pageNr, pageNr + 10));

        List<Product> products = new ArrayList<>();

        for(String s : productIDs){

            Product product = PMain.gson.fromJson(getProduct(s), Product.class);

            products.add(product);

        }

        return products;

    }

    public long getMaxPageNumber(){

        Long amountOfProducts = jedcon.call(jedis ->
                jedis.zcount(RedisKey.listOfAllProductids, "-inf", "+inf"));

        double amountOfProductsdouble = (double) amountOfProducts /10;

        long amountPages = (long)amountOfProductsdouble;

        long withoutRemains = (long)Math.floor(amountOfProductsdouble);

        if(amountOfProductsdouble > withoutRemains){

            amountPages++;
        }

        return amountPages;
    }

    public String getProduct(String productid){

        try {
            return jedcon.call(jedis -> jedis.get(productid));

        }catch (Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

    }

    public String postProduct(Product product, String sellerid){

        // Here we can have many jedis calls back and forth and compare with transactional

        return "hello";
    }

    public String postProductTransactional(Product product, String sellerid){

        String productid = product.getProductid();

        String productJSON = PMain.gson.toJson(product);

        try{

            jedcon.call(jedis -> {

                Pipeline pipelined = jedis.pipelined();
                pipelined.set(productid, productJSON);
                pipelined.zadd(RedisKey.listOfAllProductids,product.getPrice(), productid);
                pipelined.sadd(PMain.keySellerToSellerProducts(sellerid), productid);
                pipelined.zadd(PMain.key(RedisKey.productsByCathegory, product.getCathegoryString()), 0, productJSON);
                pipelined.hset(RedisKey.HASH_productsNameToId, product.getName().toLowerCase(), productJSON);

                for(Location l : product.getLocations()){

                    pipelined.zadd(PMain.key("location", l.getName()), 0, productid);
                }

                pipelined.zadd(PMain.key("brand", product.getBrand()), 0, productid);



                pipelined.sync();

                return null;
            });

        }catch (Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return postProduct_successful;

    }

    public String deleteProduct(String productid, String sellerid){

        //The deleteProduct method also needs to delete its instance in the productsofuser list

        try{

            jedcon.call(jedis -> jedis.del(productid));
            jedcon.call(jedis -> jedis.srem(PMain.key(sellerid, RedisKey.listOfAllProductids), productid));
            jedcon.call(jedis -> jedis.zrem(RedisKey.listOfAllProductids, productid));

        }catch (Exception e){
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return "Product Deleted";
    }



}
