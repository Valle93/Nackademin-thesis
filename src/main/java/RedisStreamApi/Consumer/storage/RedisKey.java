package RedisStreamApi.Consumer.storage;

public enum RedisKey {

    USER("user"), PRODUCT("product"), SESSION("session"), TRANSACTION("transaction");

    public static final String redisStream = "order-jobs";
    public static final String redisStreamRemoved = "order-jobs-removed";
    public static final String streamJobKey = "stream-order-carts";
    public static final String allTransactions = "transactions";
    public static final String listOfAllProductids = "SORTEDSET_products";
    private final String singular;

    RedisKey(String sing){

        singular = sing;
    }

    public String singular(){

        return this.singular;
    }

    public String plural(){

        return this.singular + "s";
    }

}
