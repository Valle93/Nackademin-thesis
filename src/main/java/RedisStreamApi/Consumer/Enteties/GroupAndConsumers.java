package RedisStreamApi.Consumer.Enteties;

import RedisStreamApi.Consumer.storage.JedisStoreDB;
import RedisStreamApi.Consumer.storage.JedisStoreStream;
import RedisStreamApi.Consumer.storage.RedisKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupAndConsumers {

    private String groupname;
    private List<Consumer> consumers;

    public GroupAndConsumers(String group, Map<Consumer, Integer> consumersAndAmount,
                             JedisStoreStream jedisStoreStream, JedisStoreDB jedisStoreDB){

        this.groupname = group;

        try {
            jedisStoreStream.call(jedis -> jedis.xgroupCreate(RedisKey.redisStream, this.groupname, null, false));

        } catch (Exception ignore) {

            System.out.println("Group :" + this.groupname + " already existed");
        }

        List<Consumer> consumers = new ArrayList<>();

        consumersAndAmount.forEach((consumer, integer) -> {

            for (int i = 0; i < integer; i++) {

                Consumer consumerCopy = new Consumer(consumer.getJedisStore(), consumer.getOnMessage(),
                        consumer.getConsumerNames(), consumer.getBelongsToGroup(), consumer.getMinIdleTime(),
                        jedisStoreDB);

                consumers.add(consumerCopy);
            }
        });

        for (int i = 0; i < consumers.size(); i++) {

            consumers.get(i).setConsumerName(Consumer.consumerNames[i]);
        }

        this.consumers = consumers;
    }

    public void printAllNames(){

        for (Consumer consumer : this.consumers) {

            System.out.println(consumer.getName());
        }
    }

    public void startEveryConsumer(){

        for (Consumer consumer : this.consumers){

            consumer.start();
        }
    }

    public void stopEveryConsumer(){

        for (Consumer consumer : this.consumers){

            consumer.setShouldLoopRun(false);
        }
    }



}
