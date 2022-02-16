package RedisStreamApi;

import RedisStreamApi.Services.*;
import com.google.gson.Gson;
import RedisStreamApi.Enteties.InitServer;
import RedisStreamApi.Enteties.StopWatch;
import Entities.JedisStoreDB;
import RedisStreamApi.storage.JedisStoreStreams;
import java.util.UUID;
import Entities.RedisKey;
import static RedisStreamApi.Services.ApplicationRun.run;

public class PMain {

    public static StopWatch stopWatch = new StopWatch();
    public static final Gson gson = new Gson();
    private ProductService productService;
    private SessionService sessionService;
    private UserService userService;
    private UserServiceLua SSL;
    private ProductServiceLua PSL;
    private JedisStoreDB jedisStoreDB;
    private JedisStoreStreams jedisStoreStreams;

    public PMain(ProductService productService, SessionService sessionService,
                 UserService userService, UserServiceLua SSL, ProductServiceLua PSL, JedisStoreDB jedisStoreDB,
                 JedisStoreStreams jedisStoreStreams){

        this.productService = productService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.SSL = SSL;
        this.PSL = PSL;
        this.jedisStoreDB = jedisStoreDB;
        this.jedisStoreStreams = jedisStoreStreams;

    }

    public static void main(String[] args) {
        run(ApplicationRun.class);
    }

    public static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String keySellerToSellerProducts(String sellerid){

            return key(sellerid, RedisKey.enumProduct.plural());
    }

    @InitServer
    public void startServer(){

        Server server = new Server(productService, sessionService, userService, SSL, PSL, jedisStoreDB);
    }

}