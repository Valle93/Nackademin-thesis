package RedisStreamApi.Services;

import Entities.JedisStoreDB;
import Entities.RedisKey;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Session;
import RedisStreamApi.PMain;
import RedisStreamApi.Server;
import redis.clients.jedis.Jedis;

@Injectable
public class SessionService {

    public static final String noSession = "no session";
    public static final String noSuchProduct = "Unknown Product";
    public static final String validSession = "valid session";
    public static final String invalidSession = "invalid session";
    public static final String acceptableAmountOfRequests = "Amount of requests acceptable";
    public static final int NoAccountUserSessionTime = 60 * 60 * 24 * 5;
    private JedisStoreDB jedcon;
    private String numberOfRequests;
    public static final int acceptableRequestsPerMinute = 20;

    public SessionService(JedisStoreDB jedcon){

        this.jedcon = jedcon;

        try (Jedis jedis = jedcon.getPool().getResource()) {

            numberOfRequests = jedis.scriptLoad("""
                    
                    """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Session getSession(String sessionid){

        String sessionJSON = jedcon.call(jedis -> jedis.get(sessionid));

        return PMain.gson.fromJson(sessionJSON, Session.class);

    }

    public String getUseridFromSession(String sessionid){

        String sessionJSON = jedcon.call(jedis -> jedis.get(sessionid));

        Session session = PMain.gson.fromJson(sessionJSON, Session.class);

        return session.getid();

    }

    public void extendNoAccountUserTime(String sessionid){

        jedcon.call(jedis -> jedis.expire(sessionid, NoAccountUserSessionTime));
    }

    public boolean isTheProductidValid (String productid){

        String productJSON = jedcon.call(jedis -> jedis.get(productid));

        return productJSON != null;
    }

    public String intialiseNoAccountSession() {

        String sessionid = PMain.key(RedisKey.enumNoAccountSession.singular(), PMain.generateId(),
                RedisKey.enumSession.singular());

        Session session = new Session("__NoAccountSession__");

        String sessionJSON = PMain.gson.toJson(session);

        try {

            jedcon.call(jedis -> {

                jedis.set(sessionid, sessionJSON);
                jedis.expire(sessionid, NoAccountUserSessionTime);

                return null;
            });


        } catch (Exception e) {
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return sessionid;

    }

    public String isTheSessionValid(String sessionid){

        if(sessionid == null){

            return noSession;
        }

        Boolean doesItExist = jedcon.call(jedis -> jedis.exists(sessionid));

        if(doesItExist){

            return validSession;
        }

        return noSession;

    }

    public String removeSessionIfExists(String sessionuuid){

        System.out.println(sessionuuid);

        if(sessionuuid == null){

            return noSession;
        }

        // But the sessionuuid might be corrupted

        // If sessionJSON == null Then that means that the users session didnt exist in the database

        final String[] sessionJSON = new String[1];

        jedcon.call(jedis -> {

            sessionJSON[0] = jedis.get(sessionuuid);
            jedis.del(sessionuuid);

            return sessionJSON[0];
        });

        return sessionJSON[0];

    }

    public String initialiseSession(String userid){

        String redisKey = PMain.key(userid, RedisKey.enumSession.singular());

        Session session = new Session(userid);

        String sessionJSON = PMain.gson.toJson(session);

        try {

            jedcon.call(jedis -> jedis.set(redisKey, sessionJSON));
            jedcon.call(jedis -> jedis.expire(redisKey, 600));

        } catch (Exception e) {
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return redisKey;

    }

    public String checkValidity(String sessionUUID){

        String call = jedcon.call(jedis -> jedis.get(sessionUUID));

        if(call == null){

            return invalidSession;
        }

        return validSession;
    }

    public String limitRequests(String sessionUUID){

        String sessionJSON = jedcon.call(jedis -> jedis.get(sessionUUID));

        Session session = PMain.gson.fromJson(sessionJSON, Session.class);

        String userid = session.getSessionData().get("userid");

        String redisKey = PMain.key(userid, RedisKey.requests);

        jedcon.call(jedis -> jedis.incr(redisKey));
        jedcon.call(jedis -> jedis.expire(redisKey, 60));
        jedcon.call(jedis -> jedis.expire(sessionUUID, 600));

        if(Integer.parseInt(jedcon.call(jedis -> jedis.get(redisKey))) > acceptableRequestsPerMinute){

            return Server.tooManyRequests;
        }

        return acceptableAmountOfRequests;

    }

    private <T> T lua(String sha, int keys, String... args) {
        //noinspection unchecked
        return jedcon.call(jedis -> (T) jedis.evalsha(sha, keys, args));
    }

}
