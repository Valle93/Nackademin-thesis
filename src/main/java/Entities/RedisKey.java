package Entities;

public enum RedisKey {

    SELLER("seller"), PRODUCT("product"), SESSION("session"), USER("user")
    , NoAccountUser("noAccountSession"), streamJobKey("stream-jobs");

    public static String redisStreamRemoved = "redist-stream";
    private final String singular;

    public static final String productsByCathegory = "List_Products-by-cathegory";
    public static final String listOfAllUsernames = "LIST_usernames";
    public static final String redisStream = "STREAM_order-jobs";
    public static final String encodedUserPassHashKey = "HASH_users-email-password-base64-hash";
    public static final String emailToUserid = "HASH_email-to-userid";
    public static final String listOfAllProductids = "SORTEDSET_products";
    public static final String listOfAlluserids = "LIST_users";
    public static final String requests = "requests";
    public static final String userCart = "cart";
    public static final String streamOrderCarts = "stream-order-carts";
    public static final String listOfallEmails = "LIST_every-email";
    public static final String HASH_selleridToHashedCredentials = "HASH_userid-to-hashed-credentials";
    public static final String HASH_productsNameToId = "HASH_products-name-to-id";

    public static RedisKey enumNoAccountSession = RedisKey.NoAccountUser;
    public static RedisKey enumUser = RedisKey.USER;
    public static RedisKey enumSeller = RedisKey.SELLER;
    public static RedisKey enumProduct = RedisKey.PRODUCT;
    public static RedisKey enumSession = RedisKey.SESSION;

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
