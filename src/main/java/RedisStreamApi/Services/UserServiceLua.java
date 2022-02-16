package RedisStreamApi.Services;

import Entities.RedisKey;
import redis.clients.jedis.Jedis;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Product;
import RedisStreamApi.Enteties.User;
import RedisStreamApi.PMain;
import Entities.JedisStoreDB;
import java.util.ArrayList;
import java.util.List;

@Injectable
public class UserServiceLua {

    public static final String addProduct_soldOut = "Product sold out";
    public static final String userDataDeleted = "Data deleted successfully";
    public static final String emailAvailable = "Email Available";
    public static final String emailInUse = "Email in use";
    public static final String wrongCredentials = "Wrong email or password";
    public static final String correctCredentials = "Credentials accepted";
    public static final String emailAlreadyRegistered = "This email is already registered";
    public static final String usernameAlreadyExist = "Username is already in use";

    public static String addProduct_productsAdded(int qty){
        if(qty == 1)
            return "product added";
        return "products added";
    }

    public static String addProduct_notEnoughLeft(int qty){
        return "There were only " + qty + " products left, they have been added to your cart";
    }

    // ------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------

    private JedisStoreDB jedcon;
    private ProductService productService;
    private String login;
    private String getAllUsers;
    private String deleteUser;
    private String getAllProductsFromUserid;
    private String postUser;
    private String updateUser;
    private String testCallOnARGV;

    // ------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------

    // TODO: Gotta do a login with Lua with several returns depending on if-exist etc

    public UserServiceLua(JedisStoreDB jedcon) {

        this.jedcon = jedcon;

        try (
                Jedis jedis = jedcon.getPool().getResource()) {

            deleteUser = jedis.scriptLoad("""
                                                            
                    local userPassEncoded = KEYS[1];
                                        
                    local userid = redis.call('HGET', 'users-email-password-base64-hash', userPassEncoded);
                                        
                    local userProductsKey = userid .. KEYS[2];                              
                                        
                    local products = {};
                                        
                    products = redis.call('SMEMBERS', userProductsKey);
                                        
                    redis.call('DEL', userProductsKey);
                      
                      for i = 1, table.maxn (products),1
                      do
                          redis.call('DEL', products[i]);
                          redis.call('SREM', 'products', products[i]);
                      end
                      
                      redis.call('SREM', 'users', userid);
                      redis.call('DEL', userid);
                      redis.call('HDEL', 'userpass:id', userPassEncoded);
                                        
                    """);

            testCallOnARGV = jedis.scriptLoad("""
                    
                        local table = {};
                        
                        table = redis.call('hgetall', ARGV[1])
                        
                        return table;
                    
                    """);

            getAllUsers = jedis.scriptLoad("""
                                        
                      local sellerids = {};
                                       
                      sellerids = redis.call('SMEMBERS', 'users');                                                         
                                                                              
                      local sellersJSON = {};         
                             
                      for i = 1,table.maxn (sellerids),1
                      do
                         local userid = sellerids[i];
                         sellersJSON[i] = redis.call('GET', userid .. ':json');
                      end
                      
                      return sellersJSON;         

                    """);

            getAllProductsFromUserid = jedis.scriptLoad("""
                    
                    local sellerid = KEYS[1];
                    
                    local productids = {};
                    
                    productids = redis.call('SMEMBERS', sellerid);
                    
                    local productHashes = {};
                    
                    for i = 1,table.maxn (productids),1
                      do
                         local productWithJson = productids[i] .. ':json';
                         productHashes[i] = redis.call('GET', productWithJson);
                      end
                      
                    return productHashes;
                    
                    """);

            postUser = jedis.scriptLoad("""
                    
                    redis.call('HMSET', KEYS[1], 'email', KEYS[2], 'password', KEYS[3]);
                    
                    redis.call('SET', KEYS[5], KEYS[4]);
                    
                    redis.call('HSET', KEYS[7], KEYS[8], KEYS[1]);
                    
                    redis.call('SADD', KEYS[6], KEYS[1]);
                   
                    """);

            updateUser = jedis.scriptLoad("""
                    
                    """);

            login = jedis.scriptLoad("""
                    
                   
                    -- if we continue then the user exists and the password was correct
                    
                       
                    
                    return sellerid;
                    """);

        } catch (Exception e) {

            e.printStackTrace();
            //Should not occur
        }

    }

    public String getKeyByEmail(String userPassEncoded){

        try{

            return jedcon.call(jedis -> jedis.hget(RedisKey.emailToUserid, userPassEncoded));
        }catch (Exception E){

            return JedisStoreDB.databaseError;
        }
    }

    public String login(String userPassEncoded){

        return lua(login, 2, userPassEncoded, RedisKey.emailToUserid);
    }

    public String updateSellerLua(String sellerid, String jsonbody){



        return "successfully deleted";
    };

    public List<User> getAllSellers(){

        long timeStart = System.currentTimeMillis();

        List<User> users = new ArrayList<>();

        List<String> sellersJSON = lua(getAllUsers, 0);

        for(String s: sellersJSON){

            User user = PMain.gson.fromJson(s, User.class);

            users.add(user);
        }

        Long timeStop = System.currentTimeMillis() - timeStart;

        System.out.println("getAllSellersViaLua ms :" + timeStop);

        return users;
    }

    public String deleteSeller(String userPassEncoded){

        long start = System.currentTimeMillis();

        String keyPart = ":" + RedisKey.enumProduct.plural();

        lua(deleteUser, 2, userPassEncoded, keyPart);

        long stop = System.currentTimeMillis() - start;

        System.out.println("deleteSellerViaLua ms; " + stop);

        return "Sellerdata deleted";

    }

    public List<Product> getAllProductsFromSellerid(String sellerid){

        List<String> productsFromLua = lua(getAllProductsFromUserid,
                1, PMain.keySellerToSellerProducts(sellerid));

        List<Product> products = new ArrayList<>();

        for(String s: productsFromLua){

            products.add(PMain.gson.fromJson(s, Product.class));
        }

        return products;
    }




    public String postSeller(User user){

        String sellerid = PMain.key(RedisKey.enumSeller.singular(), PMain.generateId());

        String userPassEncodedHash = JedisStoreDB.sha256(jedcon.getUserPassAsBase64(user.getEmail(), user.getPassword()));

        user.setPassword(JedisStoreDB.sha256(user.getPassword()));

        String sellerAsJson = PMain.gson.toJson(user);

        try {

            String sellerIdIfExists = jedcon.call(jedis -> jedis.hget(RedisKey.emailToUserid, userPassEncodedHash));

            if(sellerIdIfExists != null){

                return emailAlreadyRegistered;
            }

        }catch (Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        String result = lua(postUser, 8, sellerid, user.getEmail(), user.getPassword(), sellerAsJson,
                sellerid, RedisKey.listOfAlluserids, RedisKey.emailToUserid,
                userPassEncodedHash);

       // redis.call('HSET', KEYS[7], KEYS[8], KEYS[1]);

        return sellerAsJson;
    }

/*    public String addProduct(String sessionuuid, String productid, int qty){

        String sessionJSON = jedcon.call(jedis -> jedis.get(sessionuuid));

        Session session = Main.gson.fromJson(sessionJSON, Session.class);

    }*/


    private <T> T lua(String sha, int keys, String... args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

    private <T> T lua3(String sha, int keys, List<String> args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, String.valueOf(args)));
    }

    private <T> T lua2(String sha, List<String> keys, List<String> args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

    public JedisStoreDB getJedcon() {
        return jedcon;
    }

}
