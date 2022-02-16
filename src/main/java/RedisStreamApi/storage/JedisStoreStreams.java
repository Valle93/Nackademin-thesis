package RedisStreamApi.storage;

import Entities.JedisStoreDB;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import RedisStreamApi.Enteties.Injectable;
import java.util.Set;

@Injectable
public class JedisStoreStreams {

    //ThreadLocal
    private final JedisPool pool;
    private String timeToSetPerson;

    @FunctionalInterface
    public interface Call<T> {
        T connect(Jedis jedis);
    }
    public <T> T call(JedisStoreDB.Call<T> call) {
        try (Jedis jedis = pool.getResource()) {
            return call.connect(jedis);
        }
    }

    public JedisStoreStreams() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(128);
        poolConfig.setMaxTotal(128);
        poolConfig.setJmxEnabled(false);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        pool = new JedisPool(poolConfig, "localhost", 6379, 10000, null, false);

        try (Jedis jedis = pool.getResource()){

            Set<String> keys = jedis.keys("*");

            if(keys.size() > 0) {

                jedis.ping("There are still old values so I will flush them");
                jedis.flushAll();
            }
        }

    }

    public JedisPool getPool() {
        return pool;
    }
}
