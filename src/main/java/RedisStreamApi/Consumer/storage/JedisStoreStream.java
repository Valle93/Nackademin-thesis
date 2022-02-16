package RedisStreamApi.Consumer.storage;

import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class JedisStoreStream {

    //ThreadLocal

    public static final String encodedUserPassHashKey = "userpass:id";
    public static final String listAllUsersKey = "users";
    public static final String listALlProductsKey = "products";
    private final JedisPool pool;
    private String timeToSetPerson;

    @FunctionalInterface
    public interface Call<T> {
        T connect(Jedis jedis);
    }
    public <T> T call(Call<T> call) {
        try (Jedis jedis = pool.getResource()) {
            return call.connect(jedis);
        }
    }

    public JedisStoreStream() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(128);
        poolConfig.setMaxTotal(128);
        poolConfig.setJmxEnabled(false);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        pool = new JedisPool(poolConfig, "localhost", 6379, 10000, null, false);

    }

    public String errorMessage(){

        return "Something went wrong with the server, try again in a few moments";
    }

    public String[] getStringArrayFromRedisObjectArray(String array){

        array = array.substring(1);
        array = array.substring(0, array.length() - 1);
        array = array.replaceAll(",","");
        String[] productidsAsStrings = array.split(" ");

        return productidsAsStrings;
    }

    public List<String> scanAndGetStringids(String MATCH){

        List<String> listOfStringids = new ArrayList<>();

        ScanParams scanParams = new ScanParams().count(10).match("users:*");
        String cursor;

        do {

            ScanResult<String> scanResult = call(jedis -> jedis.scan("0", scanParams));
            // work with result jedis -> jedis.scan(cur, scanParams)

            for(String x: scanResult.getResult()){

                listOfStringids.add(x);
            }

            cursor = scanResult.getCursor();

        } while (!cursor.equals("0"));

        return listOfStringids;
    }

    public String getUserPassAsBase64(String email, String password){

        String userPassTogether = email + ":" + password;
        String userPassEncoded = Base64.getEncoder().encodeToString(userPassTogether.getBytes());
        return userPassEncoded;

    }

    public JedisPool getPool() {
        return pool;
    }

    public String getEncodedUserPassHashName() {
        return encodedUserPassHashKey;
    }
}
