package RedisStreamApi.Services;

import Entities.JedisStoreDB;
import Entities.RedisKey;
import Entities.Service;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Product;
import RedisStreamApi.PMain;
import com.google.gson.Gson;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.stream.Collectors;

@Injectable
public class ProductService implements Service {

    public static final String postProduct_successful = "Product Added";

    private JedisStoreDB jedcon;

    public ProductService(JedisStoreDB jedcon){

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

    public List<Product> Search(String searchInput){

        searchInput = searchInput.toLowerCase();

        List<Product> products = new ArrayList<>();

        List<String> queries = getSearchProximities(searchInput);

        Set<String> productsAsJSONS = new HashSet<>();

        jedcon.call(jedis -> {

            for (String s : queries){

                String cursor = "0";

                do {

                    ScanParams scanParams = new ScanParams().match(s);

                    ScanResult<Map.Entry<String, String>> hscan = jedis.hscan(RedisKey.HASH_productsNameToId,
                            cursor, scanParams);

                    productsAsJSONS.addAll(hscan.getResult().stream().map(Map.Entry::getValue).collect(Collectors.toSet()));

                    cursor = hscan.getCursor();

                }while(!cursor.equals("0"));
            }

            return null;

        });

        for (String productsAsJSON : productsAsJSONS) {

            Product product = PMain.gson.fromJson(productsAsJSON, Product.class);

            products.add(product);

        }

        return products;

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
                pipelined.zadd(RedisKey.listOfAllProductids,0, productid);
                pipelined.sadd(PMain.keySellerToSellerProducts(sellerid), productid);
                pipelined.zadd(PMain.key(RedisKey.productsByCathegory, product.getCathegoryString()), 0, productJSON);
                pipelined.hset(RedisKey.HASH_productsNameToId, product.getName().toLowerCase(), productJSON);
                pipelined.sync();
                pipelined.close();

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

    public List<String> getSearchProximities(String searchInput){

            searchInput = searchInput.toLowerCase();

            List<String> queries = new ArrayList<>();

            queries.add(searchInput);


        for (int i = 0; i < searchInput.length() - 1; i++) {

            char[] charArray = new char[searchInput.length() - 1];

            byte skipChar = 0;

            for (int j = 0; j < charArray.length; j++) {

                if (j == i){

                    skipChar++;
                }

                charArray[j] = searchInput.charAt(j + skipChar);
            }

            queries.add(new String(charArray));

        }

            for (int i = 0; i < searchInput.length(); i++) {

                queries.add(searchInput.replace(searchInput.charAt(i), '?'));
            }

            for (int i = 0; i < searchInput.length() - 1; i++) {

                String tempString = searchInput;

                tempString = tempString.replace(searchInput.charAt(i), '?');
                tempString = tempString.replace(searchInput.charAt(i + 1), '?');

                queries.add(tempString);
            }

            for(int i = 0; i < searchInput.length() + 1; i++){

                char[] stringAsArray = new char[searchInput.length() + 1];

                byte doWeIndex = 0;

                for (int j = 0; j < stringAsArray.length; j++) {

                    if(j == i){
                        stringAsArray[j] = '?';
                        doWeIndex++;

                    }else{
                        stringAsArray[j] = searchInput.charAt(j - doWeIndex);
                    }
                }

                queries.add(new String(stringAsArray));

            }

            for(int i = 0; i < searchInput.length(); i++){

                char[] stringAsArray = new char[searchInput.length() + 1];

                byte doWeIndex = 0;

                for (int j = 0; j < stringAsArray.length; j++) {

                    if(j == i || j == i + 1){
                        stringAsArray[j] = '?';
                        doWeIndex++;

                    }else{
                        stringAsArray[j] = searchInput.charAt(j - doWeIndex);
                    }
                }

                queries.add(new String(stringAsArray));

            }

            for(int i = 0; i < searchInput.length() - 1; i++){

                char[] stringAsArray = new char[searchInput.length() + 1];

                byte doWeIndex = 0;

                for (int j = 0; j < stringAsArray.length; j++) {

                    if(j == i || j == i + 1 || j == i + 2){

                        stringAsArray[j] = '?';
                        doWeIndex++;

                    }else{
                        stringAsArray[j] = searchInput.charAt(j - doWeIndex);

                    }
                }

                queries.add(new String(stringAsArray));
            }

        System.out.println(queries);

        return queries;

    }

}
