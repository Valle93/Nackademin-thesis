package RedisStreamApi.Enteties;

import java.util.HashMap;
import java.util.Map;

public class Session implements Orderable {

    private Map<String, String> sessionData;
    private Cart cart;
    private final String sessionStarted = "session started";
    private final String userid = "userid";

    public Session(String userid){

        Map<String, String> mapOfData = new HashMap<>();

        mapOfData.put(this.sessionStarted, String.valueOf(System.currentTimeMillis()));
        mapOfData.put(this.userid, userid);
        this.sessionData = mapOfData;

        this.cart = new Cart(userid);

    }

    public Cart getCart() {
        return this.cart;
    }

    public void flushCart(){}

    public Map<String, String> getSessionData() {
        return sessionData;
    }

    public String getSessionStarted() {
        return sessionStarted;
    }

    public String getid() {
        return sessionData.get(userid);
    }
}
