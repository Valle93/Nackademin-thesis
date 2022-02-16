/*
package Producer.Legacy;

import Producer.RedisKey;
import com.google.gson.Gson;

import Producer.Server;
import Producer.Services.ProductService;
import Producer.Services.UserService;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import Producer.storage.InitData;
import Producer.storage.JedisStoreDB;
import Producer.storage.JedisStoreStreams;
import java.util.UUID;

public class MainNoDependencyInjection {

    public static final Gson gson = new Gson();
    public static RedisKey enumNoAccountUser = RedisKey.NoAccountUser;
    public static RedisKey enumUser = RedisKey.USER;
    public static RedisKey enumSeller = RedisKey.SELLER;
    public static RedisKey enumProduct = RedisKey.PRODUCT;
    public static RedisKey enumSession = RedisKey.SESSION;
    public static final String wrongEmailorPass = "Wrong Email or Password";
    public static final String databaseError = "Something went wrong with the database";
    public static final String userByThatNameAlreadyExists = "A user by that name already exists";

    public static void main(String[] args) {

        ApplicationRun applicationRun = new ApplicationRun();

        JedisStoreDB connection = new JedisStoreDB();

        JedisStoreStreams streamStore = new JedisStoreStreams();

        ProductService productService = new ProductService(connection);

        UserServiceLua SSL = new UserServiceLua(connection);

        SessionService sessionService = new SessionService(connection);

        UserService userService = new UserService(connection, productService, streamStore);

        ProductServiceLua PSL = new ProductServiceLua(connection);

        Server server = new Server(productService, sessionService, userService, SSL,
                PSL, connection);

        InitData initData = new InitData(productService, userService, SSL, PSL, sessionService);

    }

    public static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateProductid(){

        return key(enumProduct.singular(), generateId());
    }

    public static String keySellerToSellerProducts(String sellerid){

        return key(sellerid, enumProduct.plural());
    }

    public static String keyUserToUserCart(String userid){

        return key(userid, "cart");
    }

    public static String sha256(String userPassEncoded) {

        String userPassEncodedAsHash = "";


        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] HashPassAsBytes = md.digest(userPassEncoded.getBytes(StandardCharsets.UTF_8));

            // md.digest(input.getBytes(StandardCharsets.UTF_8));

            BigInteger number = new BigInteger(1, HashPassAsBytes);

            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 32)
            {
                hexString.insert(0, '0');
            }

            userPassEncodedAsHash = hexString.toString();

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return userPassEncodedAsHash;

    }

}*/
