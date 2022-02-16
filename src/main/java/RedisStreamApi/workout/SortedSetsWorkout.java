package RedisStreamApi.workout;

import Entities.JedisStoreDB;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.resps.Tuple;

import java.util.Set;


class SortedSetsWorkout {

    public static void main(String[] args) {

        JedisStoreDB jedisStoreDB = new JedisStoreDB();

        jedisStoreDB.call(jedis -> jedis.flushAll());

        jedisStoreDB.call(jedis -> {

            Pipeline pipelined = jedis.pipelined();

            pipelined.zadd("user:1:cart", 1, "Phone");
            pipelined.zadd("user:1:cart", 1, "Football");
            pipelined.zadd("user:1:cart", 1, "Basketball");

            pipelined.sync();
            pipelined.close();

            return null;
        });


        Set<Tuple> call = (Set<Tuple>) jedisStoreDB.call(jedis -> jedis.zrangeWithScores("user:1:cart", 0, -1));

        call.forEach(tuple -> {
            System.out.println("Here should be name: " + tuple.getElement());
            System.out.println("This is the score: " + tuple.getScore());
            System.out.println();
        });

        double number = 69;

        number = number /10;

        long amountPages = (long)number;

        long withoutRemains = (long)Math.floor(number);

        if(number > withoutRemains){

            amountPages++;
        }

        System.out.println(number);
    }
}