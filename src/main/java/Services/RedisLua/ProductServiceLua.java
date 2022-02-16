package Services.RedisLua;


import Entities.JedisStoreDB;
import Entities.Product;
import Entities.RedisKey;
import Entities.Service;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProductServiceLua implements Service {

    private JedisStoreDB jedcon;
    private String getAllProducts;
    private String postProduct;
    private String getProduct;
    private String updateProduct;
    private String deleteProduct;
    private String printTestSpeed;
    private String doNothing;
    private String getProductsFromPriceRange;
    private String getProductsByPage;
    private Gson gson;

    public ProductServiceLua(JedisStoreDB jedcon){

        this.jedcon = jedcon;

        this.gson = new Gson();

        try(Jedis jedis = jedcon.getPool().getResource()) {

            doNothing = jedis.scriptLoad("""
                    """);

            printTestSpeed = jedis.scriptLoad("""
                    print("x")
                    """);

            getProduct = jedis.scriptLoad("""
                    
                    local productJSON = redis.call('get', KEYS[1]);
                    
                    return productJSON;
                    
                    """);


            getAllProducts = jedis.scriptLoad("""
                    
                    local productids = {};
                    
                    productids = redis.call('zrange', KEYS[1], 0, -1);
                    
                    local productsJSON = {};
                    
                    for i = 1, table.maxn(productids),1
                        do
                        productsJSON[i] = redis.call('GET', productids[i]);
                        end
                    
                    return productsJSON;
                    """);


            deleteProduct = jedis.scriptLoad("""
                        
                        redis.call('DEL', KEYS[1]);
                        
                        redis.call('ZREM', KEYS[2], KEYS[1]);
                    
                    """);

            postProduct = jedis.scriptLoad("""
                                   
                        redis.call('SET', KEYS[1], ARGV[1]);
                        
                        redis.call('zadd', KEYS[2], ARGV[2], KEYS[1]);
                    
                    """);

            getProductsFromPriceRange = jedis.scriptLoad("""
                    
                    local productids = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2]);
                    
                    local productsJSON = {};
                    
                    for i = 1, table.maxn(productids), 1
                        do
                        productsJSON[i] = redis.call('GET', productids[i]);
                        end
                        
                    return productsJSON;
                        
                    """);

            getProductsByPage = jedis.scriptLoad("""
                    
                    local prouctids = redis.call('zrange', KEYS[1], ARGV[1], ARGV[2]);
                    
                    local productsJSON = {};
                    
                    for i = 1, table.maxn(productids), 1
                        do
                        productsJSON[i] = redis.call('GET', productids[i]);
                        end
                    
                    return productsJSON;
                    """);

        }
    }

    public List<Product> getAllProducts(){

        List<Product> products = new ArrayList<>();

        List<String> productsJSON = lua(getAllProducts, 1, RedisKey.listOfAllProductids);

        for(String s: productsJSON){

            Product product = gson.fromJson(s, Product.class);

            products.add(product);
        }

        return products;
    }

    public List<Product> getProductsFromPriceRange(int start, int end){

        List<Product> products = new ArrayList<>();

        List<String> productsJSON = lua(getProductsFromPriceRange, 1, RedisKey.listOfAllProductids,
                String.valueOf(start), String.valueOf(end));

        for(String s : productsJSON){

            products.add(gson.fromJson(s, Product.class));
        }

        return products;

    }

    public Set<String> getProductsByPage(int number){

        number--;

        return lua(getProductsByPage, 1, RedisKey.listOfAllProductids,
                String.valueOf(number), String.valueOf(number + 10));

    }

    public Product postProduct(Product product, String sellerid){

        String productid = key(RedisKey.enumProduct.singular(), generateID());

        String price = String.valueOf(product.getPrice());

        String productJSON = gson.toJson(product);

        lua(postProduct, 2, productid, RedisKey.listOfAllProductids,
                productJSON, price);

        return product;

    }

    public String deleteProduct(String productid){

        lua(deleteProduct, 2, productid, RedisKey.listOfAllProductids);

        return "Product Deleted";
    }

    public Product getProduct(String productid){

        String productJSON = lua(getProduct, 1, productid);

        return gson.fromJson(productJSON, Product.class);

    }

    public String getProductJSON(String productid){

        return lua(getProduct, 1, productid);
    }

    private <T> T lua(String sha, int keys, String... args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

    static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateID() {

        return (key(RedisKey.enumProduct.singular(), randomUUID()));
    }

    static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
