package RedisStreamApi.Services;

import Entities.RedisKey;
import redis.clients.jedis.Jedis;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Product;
import RedisStreamApi.PMain;
import Entities.JedisStoreDB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Injectable
public class ProductServiceLua {

    private JedisStoreDB jedcon;
    private String getAllProducts;
    private String postProduct;
    private String getProduct;
    private String updateProduct;
    private String deleteProduct;
    private String searchForProduct;
    private String printTestSpeed;
    private String doNothing;

    public ProductServiceLua(JedisStoreDB jedcon){

        this.jedcon = jedcon;

        try(Jedis jedis = jedcon.getPool().getResource()) {

            doNothing = jedis.scriptLoad("""
                    """);

            printTestSpeed = jedis.scriptLoad("""
                    print("x")
                    """);

            searchForProduct = jedis.scriptLoad("""
                    
                    local string1 = "GolF"
                                        
                    local string2 = string.lower(string1)
                                        
                    print(string.len(string2))
                                        
                    local searches = {}
                                        
                    searches[1] = string2
                                        
                    for i = 0, string.len(string2), 1
                        do
                            
                             local start = string.sub(string2, 0, i)
                            
                             local finish = string.sub(string2, i + 1)
                            
                             local product = start .. "?" .. finish
                            
                             table.insert(searches, product)
                            
                        end
                       
                        print("")
                        print("")
                        print("")
                       
                    for i = 0, string.len(string2) - 1, 1
                        do
                            local start = string.sub(string2, 1, i)
                           
                            local finish = string.sub(string2, i + 2)
                           
                            local product = start .. "?" .. finish
                           
                            table.insert(searches, product)
                        end
                       
                    for i = 0, string.len(string2) - 2, 1
                        do
                            local start = string.sub(string2, 0 , i)
                           
                            local ende = string.sub(string2, i + 3)
                       
                            local result = start .. "??" .. ende
                       
                            table.insert(searches, result)
                        end
                       
                    for i = 1, table.maxn(searches), 1
                        do
                            print(searches[i])
                        end
                        
                        return searches
                    """);

            getAllProducts = jedis.scriptLoad("""
                    
                    local productids = {};
                    
                    productids = redis.call('smembers', 'products');
                    
                    local productsJSON = {};
                    
                    for i = 1, table.maxn(productids),1
                        do
                        local productid = productids[i];
                        productsJSON[i] = redis.call('GET', productid .. ':json');
                        end
                    
                    return productsJSON;
                    """);

            postProduct = jedis.scriptLoad("""
                        
                        redis.call('HMSET', KEYS[7], 'name', KEYS[1], 'user', KEYS[2], 
                                   'qty', KEYS[3], 'price', KEYS[4], 'description', KEYS[5],
                                   'cathegory', KEYS[6]);
                                   
                        redis.call('SET', KEYS[7] .. ':json', KEYS[8]);
                        
                        redis.call('SADD', KEYS[9], KEYS[7]);
                        
                        redis.call('SADD', KEYS[10], KEYS[7]); 
                    
                    """);

            deleteProduct = jedis.scriptLoad("""
                        
                        redis.call('DEL', KEYS[1]);
                        
                        redis.call('SREM', KEYS[2] .. KEYS[3], KEYS[1]);
                        
                        redis.call('SREM', KEYS[3], KEYS[1]);
                    
                    """);

            updateProduct = jedis.scriptLoad("""
                    
                         redis.call('HMSET', KEYS[7], 'name', KEYS[1], 'user', KEYS[2], 
                                   'qty', KEYS[3], 'price', KEYS[4], 'description', KEYS[5],
                                   'cathegory', KEYS[6]);
                                   
                         redis.call('SET', KEYS[7] .. ':json', KEYS[8]);
                                   
                    """);

            // jedcon.call(jedis -> jedis.hmset(productid, mapOfProduct));
           // jedcon.call(jedis -> jedis.set(Main.key(productid, RedisKey.json), productJSON));

        }
    }

    public Product updateProduct(Product product, String productid){

        String productJSON = PMain.gson.toJson(product);

        lua(postProduct, 8, product.getName(), product.getSellerid(),
                String.valueOf(product.getQty()), String.valueOf(product.getPrice()),
                product.getDescription(), product.getCathegory(), productid, productJSON);

        return product;
    }

    public String deleteProduct(String productid, String sellerid){

        lua(deleteProduct, 3, productid, sellerid, RedisKey.listOfAllProductids);

        return "Product Deleted";
    }

    public Product postProduct(Product product, String sellerid){

        String productid = PMain.key(RedisKey.enumProduct.singular(), PMain.generateId());

        String productAsJson = PMain.gson.toJson(product);

                lua(postProduct, 10, product.getName(), product.getSellerid(),
                String.valueOf(product.getQty()), String.valueOf(product.getPrice()),
                product.getDescription(), product.getCathegory(), productid, productAsJson,
                RedisKey.listOfAllProductids, PMain.key(sellerid, RedisKey.listOfAllProductids));

        return product;
    }

    public List<Product> getAllProducts(){

        List products = new ArrayList();

        List<String> productsJSON = lua(getAllProducts, 0);

        for(String s: productsJSON){

            Product product = PMain.gson.fromJson(s, Product.class);

            products.add(product);
        }

        return products;
    }

    private <T> T lua(String sha, int keys, String... args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

    public String testSpeedBetweenJavaAndRedis(){

        StringBuilder sb = new StringBuilder();

        Map<Integer, Long> map = new HashMap<>();

        sb.append("First the results for DoNothing: \n\n");

        for (int i = 1; i <= 100; i++) {

            long then = System.currentTimeMillis();

            lua(doNothing, 0, "");

            map.put(i, System.currentTimeMillis() - then);

        }

        map.forEach((integer, aLong) -> {

            sb.append(integer + " : " + aLong + "\n");
        });

        map = new HashMap<>();

        sb.append("And now with print: \n\n");

        for (int i = 1; i <= 100; i++) {

            long then = System.currentTimeMillis();

            lua(printTestSpeed, 0, "");

            map.put(i, System.currentTimeMillis() - then);

        }

        map.forEach((integer, aLong) -> {

            sb.append(integer + " : " + aLong + "\n");
        });

        return sb.toString();
    }

}
