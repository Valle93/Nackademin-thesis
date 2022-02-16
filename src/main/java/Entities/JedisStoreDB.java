package Entities;

import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.PMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

@Injectable
public class JedisStoreDB {

    public static final String databaseError = "Server error, try again later";

    private JedisPool pool;
    private JedisPoolConfig jedisPoolConfig;

    @FunctionalInterface
    public interface Call<T> {
        T connect(Jedis jedis);
    }
    public <T> T call(Call<T> call) {
        try (Jedis jedis = pool.getResource()) {
            return call.connect(jedis);
        }
    }

    public JedisStoreDB() {

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(128);
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setJmxEnabled(false);
        jedisPoolConfig.setTestOnBorrow(false);
        jedisPoolConfig.setTestOnReturn(false);
        pool = new JedisPool(jedisPoolConfig, "localhost", 6380, 10000, null, false);

        flushRedisDatabase();

        /*JedisMonitor jedisMonitor = new JedisMonitor() {
            @Override
            public void onCommand(String command) {
                System.out.println(command);
            }
        };
        new Thread(() -> call(jedis -> {
            jedis.monitor(jedisMonitor);
            return null;
        })).start();*/

    }
    public void flushRedisDatabase() {

        try (Jedis jedis = pool.getResource()) {

            Set<String> keys = jedis.keys("*");

            if (keys.size() > 0) {

                jedis.ping("There are still old values so I will flush them");
                jedis.flushAll();
            }

        } catch (Exception e) {

            e.printStackTrace();
            //Should not occur
        }finally {

            pool.close();
            pool = new JedisPool(jedisPoolConfig, "localhost", 6380, 10000, null, false);
        }

    }

    public String getEmailFromAuthorization(String auth) throws UnsupportedEncodingException {

        byte[] decodedValue = Base64.getDecoder().decode(auth);
        String decodeAuthorization =  new String(decodedValue, StandardCharsets.UTF_8.toString());
        return decodeAuthorization.substring(0, decodeAuthorization.indexOf(":"));
    }

    public String getUserPassAsBase64(String email, String password){

        return Base64.getEncoder().encodeToString(PMain.key(email, password).getBytes());

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

    public JedisPool getPool() {
        return pool;
    }
}
