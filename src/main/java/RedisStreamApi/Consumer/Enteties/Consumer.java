package RedisStreamApi.Consumer.Enteties;

import RedisStreamApi.Consumer.CMain;
import RedisStreamApi.Consumer.storage.JedisStoreDB;
import RedisStreamApi.Consumer.storage.JedisStoreStream;
import RedisStreamApi.Consumer.storage.RedisKey;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamPendingEntry;

import java.util.ArrayList;
import java.util.List;

public class Consumer extends Thread{

    public static final String[] consumerNames = {"alice", "bob", "carol", "david", "eve", "frank", "gary", "harry",
            "irene", "janet"};
    private JedisStoreStream jedisStoreStream;
    private JedisStoreDB jedisStoreDB;
    private onMessage onMessage;
    private String name;
    private String belongsToGroup;
    private long minIdleTime;
    private String getJobLua;
    private boolean shouldLoopRun;

    public Consumer(JedisStoreStream jedisStoreStream, onMessage onMessage, String name, String belongsToGroup,
                    long minIdleTime, JedisStoreDB jedisStoreDB){

        this.jedisStoreDB = jedisStoreDB;
        this.jedisStoreStream = jedisStoreStream;
        this.onMessage = onMessage;
        this.name = name;
        this.belongsToGroup = belongsToGroup;
        this.minIdleTime = minIdleTime;
        this.shouldLoopRun = true;

        try( Jedis jedis = jedisStoreStream.getPool().getResource()){

            getJobLua = jedis.scriptLoad("""
                    
                    local resultOfCall = {};
                    
                    resultOfCall = redis.call('XAUTOCLAIM', KEYS[1], ARGV[1], ARGV[2], ARGV[3], 1);
                    
                    local tableJob = resultOfCall[2];
                    
                    if(table.maxn (tableJob) > 0)
                    then
                        resultOfCall[1] = 'xautoclaim';
                        return resultOfCall;
                    end
                    
                    return resultOfCall;
                    
                    """);

            /*resultOfCall = redis.call('XREADGROUP', 'group', ARGV[1], ARGV[2], 'count', 1, 'stream', KEYS[1]);

            tableJob = resultOfCall[2];

            if(table.maxn (tableJob) > 0)
                then
            resultOfCall[1] = 'xreadgroup';
            return resultOfCall;
            end

            resultOfCall[1] = 'nothing to read';*/

        }

    }

    private <T> T lua(String sha, int keys, String... args) {
        //noinspection unchecked
        return jedisStoreStream.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

    @FunctionalInterface
    public interface onMessage {
        void handleMessage(String consumerName, String groupName, String streamName,
                           List<StreamPendingEntry> pendingEntries, Long minIdleTime, Consumer consumer);
    }

    @Override
    public void run() {

        while(shouldLoopRun){

            List<StreamPendingEntry> pendingEntries = jedisStoreStream.call(jedis ->
                    jedis.xpending(RedisKey.redisStream, this.belongsToGroup,
                            XPendingParams.xPendingParams().count(20)));

            onMessage.handleMessage(this.name, this.belongsToGroup, RedisKey.redisStream, pendingEntries,
                    this.minIdleTime ,this);

        }

    }

    public void doJob(StreamEntry entry, String groupName){

        CartJob cartJob = CMain.gson.fromJson(entry.getFields().get(RedisKey.streamJobKey), CartJob.class);

        cartJob.getProductsAndQty().forEach((s, integer) -> {

            jedisStoreDB.call(jedis -> jedis.zincrby(RedisKey.listOfAllProductids, integer, s));
        });

        Transaction transaction = new Transaction(cartJob.getUserid(), cartJob);

        String transactionJSON = CMain.gson.toJson(transaction);

        String transactionUUID = CMain.key(RedisKey.TRANSACTION.singular(), CMain.generateId());

        jedisStoreStream.call(jedis -> jedis.set(transactionUUID, transactionJSON));

        jedisStoreStream.call(jedis -> jedis.sadd(CMain.key(cartJob.getUserid(), RedisKey.TRANSACTION.plural()),
                transactionUUID));

        jedisStoreStream.call(jedis -> jedis.sadd(RedisKey.allTransactions, transactionUUID));

        jedisStoreStream.call(jedis -> jedis.xack(RedisKey.redisStream, groupName, entry.getID()));

    }

    public List<StreamEntryID> sendInPendingGetIDS (List<StreamPendingEntry> entries) {

        List<StreamEntryID> streamEntryIDS = new ArrayList<>();

        entries.forEach(streamPendingEntry -> {

            //  if(streamPendingEntry.getDeliveredTimes() > 5){

            if (streamPendingEntry.getIdleTime() > minIdleTime) {

                streamEntryIDS.add(streamPendingEntry.getID());

                // Save the job in a failed streams and ack it from the order-jobs stream?
            }

        });

        return streamEntryIDS;

    }

    public void setShouldLoopRun(boolean shouldLoopRun) {
        this.shouldLoopRun = shouldLoopRun;
    }

    public void setConsumerName(String name) {
        this.name = name;
    }

    public String getConsumerNames() {
        return this.name;
    }

    public JedisStoreStream getJedisStore() {
        return jedisStoreStream;
    }

    public Consumer.onMessage getOnMessage() {
        return onMessage;
    }

    public String getBelongsToGroup() {
        return belongsToGroup;
    }

    public long getMinIdleTime() {
        return minIdleTime;
    }

    public String getGetJobLua() {
        return getJobLua;
    }

    public boolean isShouldLoopRun() {
        return shouldLoopRun;
    }
}
