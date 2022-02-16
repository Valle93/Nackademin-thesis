package RedisStreamApi.Consumer;

import Entities.RedisKey;
import RedisStreamApi.Consumer.Enteties.Consumer;
import RedisStreamApi.Consumer.Enteties.GroupAndConsumers;
import RedisStreamApi.Consumer.storage.JedisStoreDB;
import RedisStreamApi.Consumer.storage.JedisStoreStream;
import com.google.gson.Gson;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XClaimParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamGroupInfo;
import redis.clients.jedis.resps.StreamPendingEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CMain {

    public static Gson gson = new Gson();
    public static RedisKey enumProduct = RedisKey.PRODUCT;


    public static void main(String[] args) {

        JedisStoreStream jedisStoreStream = new JedisStoreStream();

        JedisStoreDB jedisStoreDB = new JedisStoreDB();

        List<StreamGroupInfo> call = jedisStoreStream.call(jedis -> jedis.xinfoGroup(RedisKey.redisStream));

        for (StreamGroupInfo sgi : call){

            System.out.println(sgi.getName());
        }

        Consumer doJobsConsumer = new Consumer (jedisStoreStream, (String consumerName, String groupName, String streamName,
                                                                   List<StreamPendingEntry> pendingEntries, Long minIdleTime,
                                                                   Consumer consumer) -> {

            List<StreamEntryID> idleTimeLongerThan10Minutes = consumer.sendInPendingGetIDS(pendingEntries);

            StreamEntryID[] streamEntryIDS = idleTimeLongerThan10Minutes.toArray(StreamEntryID[]::new);

            List<StreamEntry> call2 = null;
            if (streamEntryIDS.length > 0) {

                call2 = jedisStoreStream.call(jedis -> jedis.xclaim(RedisKey.redisStream, groupName, consumerName,
                        minIdleTime, XClaimParams.xClaimParams(), streamEntryIDS));
            }

            if (call2 != null) {

                call2.forEach(streamEntry -> {

                   // consumer.doJob(streamEntry, groupName);

                });
            }

            List<Map.Entry<String, List<StreamEntry>>> readGroup = jedisStoreStream.call(jedis -> jedis.xreadGroup(
                    groupName, consumerName, XReadGroupParams.xReadGroupParams().count(5).block(500),
                    Map.of(RedisKey.redisStream, StreamEntryID.UNRECEIVED_ENTRY)));

            if(readGroup == null){

                return;
            }

            Map.Entry<String, List<StreamEntry>> stringListEntry = readGroup.get(0);

            List<StreamEntry> readGroupEntries = stringListEntry.getValue();

            readGroupEntries.forEach(streamEntry -> {

                //  consumer.doJob(streamEntry, groupName);
            });

        }, "consumer", "latestJobs", 1000, jedisStoreDB);

        //
        //   NEW CONSUMER
        //                  NEW CONSUMER
        //

        Consumer deleteJobsConsumer = new Consumer(jedisStoreStream, (String consumerName, String groupName, String streamName,
                                                                      List<StreamPendingEntry> pendingEntries, Long minIdleTime , Consumer consumer) ->

        {

            if (pendingEntries.size() > 0){

                System.out.println("it was !");
            }

            List<StreamEntryID> failedOver5Times = consumer.sendInPendingGetIDS(pendingEntries);

            StreamEntryID[] finalIds1 = failedOver5Times.toArray(StreamEntryID[]::new);

            List<StreamEntry> entryToRemove = null;

            if (finalIds1.length > 0) {

                entryToRemove = jedisStoreStream.call(jedis -> jedis.xclaim(RedisKey.redisStream,
                        groupName, consumerName, 1, XClaimParams.xClaimParams(), finalIds1));

            }


            if (entryToRemove != null && !entryToRemove.isEmpty()) {
                entryToRemove.forEach(streamEntry -> {

                    Map<String, String> fields = streamEntry.getFields();

                    jedisStoreStream.call(jedis -> jedis.xadd(RedisKey.redisStreamRemoved, streamEntry.getID(), Map.of(

                            "Removed at " + System.currentTimeMillis() + " SCTM", fields.get(RedisKey.streamJobKey)

                    )));

                    jedisStoreStream.call(jedis -> jedis.xack(RedisKey.redisStream, groupName, streamEntry.getID()));

                });

            }

        }, "delete-consumer", "latestJobs", 1000, jedisStoreDB);

        Map<Consumer, Integer> consumersAndAmount = new HashMap<>();

        consumersAndAmount.put(doJobsConsumer, 8);

        consumersAndAmount.put(deleteJobsConsumer, 2);

        GroupAndConsumers streamLatestJobs = new GroupAndConsumers("latestJobs", consumersAndAmount,
                jedisStoreStream, jedisStoreDB);

        streamLatestJobs.startEveryConsumer();

    }



    public static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateProductid(){

        return key(enumProduct.plural(), generateId());
    }

    public static String fromKeyUsersToKeyUser(String userid){

            return key(userid, enumProduct.plural());

    }

}