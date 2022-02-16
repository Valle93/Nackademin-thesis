package RedisStreamApi.Services;

import Entities.JedisStoreDB;
import Entities.JedisStoreStreams;
import RedisStreamApi.Enteties.Product;
import Entities.RedisKey;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Orderable;
import RedisStreamApi.Enteties.Session;
import RedisStreamApi.Enteties.User;
import RedisStreamApi.PMain;
import RedisStreamApi.Server;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.StreamEntryID;

import java.util.*;

@Injectable
public class UserService {

    public static final String addProduct_soldOut = "Product sold out";
    public static final String userDataDeleted = "Data deleted successfully";
    public static final String emailNotAvailable = "This email is already registered";
    public static final String usernameNotAvailable = "Username is already in use";
    public static final String bothUnavailable = "email and username are both unavailable";
    public static final String emailAndUsernameAvailable = "Both are available";
    public static final String successfulRegistration = "Registration successful";

    public static String addProduct_productsAdded(int qty){
        if(qty == 1)
            return "product added";
        return "products added";
    }

    public static String addProduct_notEnoughLeft(int qty){
        return "There were only " + qty + " products left, they have been added to your cart";
    }

    private JedisStoreDB jedcon;
    private JedisStoreStreams jedisStoreStreams;
    private ProductService productService;


    public UserService(JedisStoreDB jedcon, ProductService productService,
                       JedisStoreStreams jedisStoreStreams){

         this.jedcon = jedcon;
         this.jedisStoreStreams = jedisStoreStreams;
         this.productService = productService;

    }

    public Map<String, Integer> getCart(String sessionid){

        if(sessionid == null){

            return Map.of("Cart is empty", 0);
        }

        if(sessionid != null && sessionid.startsWith("user:")){

            sessionid = sessionid.substring(0, 37);
        }


        String finalSessionid = sessionid;

        String sessionJSON = jedcon.call(jedis -> jedis.get(finalSessionid));

        if(sessionJSON == null){

            return Map.of("Cart is empty", 0);
        }

        Session session = PMain.gson.fromJson(sessionJSON, Session.class);

        // TODO: Here needs work

        Map<String, Integer> productNameAndAmount = new HashMap<>();

        session.getCart().getProductidAndProduct().forEach((s, product) -> {

                productNameAndAmount.put(product.getName(), product.getQty());
        });

        return productNameAndAmount;

    }

    public CustomerInfo areYouShoppingWithAnAccout(String orderableid){

        String orderableJSON = jedcon.call(jedis -> jedis.get(orderableid));

        Orderable orderable;

        CustomerInfo customerInfo;

        if(orderableid.substring(0 , 16).equals(RedisKey.enumNoAccountSession.singular())){

            orderable = PMain.gson.fromJson(orderableJSON, Session.class);

            customerInfo = new CustomerInfo(orderable, true);

        }else{

            orderable = PMain.gson.fromJson(orderableJSON, User.class);

            customerInfo = new CustomerInfo(orderable, false);
        }

        return customerInfo;
    }

    public String addProduct(String orderableid, String productid, int qty){

        boolean lessThanOrdered = false;

        String productJSON = jedcon.call(jedis -> jedis.get(productid));

        Product product = PMain.gson.fromJson(productJSON, Product.class);

        Orderable orderable = areYouShoppingWithAnAccout(orderableid).getOrderable();

        int qtyLeft = product.getQty();

        if (qty > qtyLeft){

            qty = qtyLeft;

            lessThanOrdered = true;
        }

        if(qtyLeft == 0){

            return addProduct_soldOut;
        }

        product.setQty(qtyLeft - qty);

        jedcon.call(jedis -> jedis.set(productid, PMain.gson.toJson(product)));

        int qtyInCart;

        if(orderable.getCart().getProductidAndProduct().get(productid) == null)
            qtyInCart = 0;
        else
            qtyInCart = orderable.getCart().getProductidAndProduct().get(productid).getQty();

        if (qtyInCart == 0){

            product.setQty(qty);

            orderable.getCart().getProductidAndProduct().put(productid, product);

            jedcon.call(jedis -> jedis.set(orderableid, PMain.gson.toJson(orderable)));
        }
        else{

            int quantities = qty + qtyInCart;

            product.setQty(quantities);

            orderable.getCart().getProductidAndProduct().put(productid, product);

            jedcon.call(jedis -> jedis.set(orderableid, PMain.gson.toJson(orderable)));
        }

        if(lessThanOrdered){

            return addProduct_notEnoughLeft(qty);
        }

        return addProduct_productsAdded(qty);

    }

    public String orderCart(String orderableid){

        CustomerInfo customerInfo = areYouShoppingWithAnAccout(orderableid);

        Orderable orderable = customerInfo.getOrderable();

        String cartJSON = PMain.gson.toJson(orderable.getCart());

        jedisStoreStreams.call(jedis -> jedis.xadd(RedisKey.redisStream, (StreamEntryID) null, Map.of("cart", cartJSON)));

        // The xadd is done on the other Redis Instance

        if(customerInfo.isItNoAccount) {
            jedcon.call(jedis -> jedis.del(PMain.key(orderable.getid(), RedisKey.enumSession.singular())));

        }else{

            orderable.flushCart();
            jedcon.call(jedis -> jedis.set(orderableid, PMain.gson.toJson(orderable)));
        }

        return cartJSON;

    }

    public String deleteUser(String userPassEncoded){

        PMain.stopWatch.startTimer("UserService, method : deleteSeller");

        String userid = getIdByEmail(userPassEncoded);

        try{

            Set<String> productids = jedcon.call(jedis -> jedis.smembers(PMain.keySellerToSellerProducts(userid)));

            for(String x: productids){
                productService.deleteProduct(userid, x);
            }

        }catch (Exception e){
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        try{

            jedcon.call(jedis -> jedis.srem(RedisKey.listOfAlluserids, userid));
            jedcon.call(jedis -> jedis.del(userid));
            jedcon.call(jedis -> jedis.hdel(RedisKey.encodedUserPassHashKey, userPassEncoded));

        }catch (Exception e){
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        PMain.stopWatch.stopTimer();

        return userDataDeleted;
    }

    public JedisStoreDB getJedcon() {
        return jedcon;
    }

    public List<User> getAllUsers(){

        PMain.stopWatch.startTimer("UserService, method: getAllSellers");

        Set<String> userids = jedcon.call(jedis -> jedis.smembers(RedisKey.listOfAlluserids));

        List<User> listOfUsers = new ArrayList<>();

        for(String s : userids){

            listOfUsers.add(PMain.gson.fromJson(getUser(s), User.class));
        }

        PMain.stopWatch.stopTimer();

        return listOfUsers;
    }

    public List<Product> getAllProductsFromUserid(String userid){

        List<Product> products = new ArrayList<>();

        try{
            Set<String> ids = jedcon.call(jedis ->
                    jedis.smembers(PMain.keySellerToSellerProducts(userid)));

            for(String s: ids){

                products.add(PMain.gson.fromJson(productService.getProduct(s), Product.class));
            }

        }catch (Exception e){
            e.printStackTrace();

        }

        return products;

    }

    public String login(String email, String credentials){

        // First get the id with the email

        String userid = getIdByEmail(email);

        if(userid == null)
            return Server.wrongCredentials;

        String loginResult = checkCorrectPassword(userid, credentials);

        if(loginResult.equals(Server.wrongCredentials))
            return Server.wrongCredentials;

        return Server.correctCredentials;

        // Then start a session
    }

    public String checkIfEmailAndUserNameAvailable(String email, String username){

        try {

            Boolean isTheEmailInUse = jedcon.call(jedis -> jedis.sismember(RedisKey.listOfallEmails, email));
            Boolean isTheUsernameInUse = jedcon.call(jedis ->
                    jedis.sismember(RedisKey.listOfAllUsernames, username));

            if(isTheEmailInUse && isTheUsernameInUse)
                return bothUnavailable;
            if(isTheEmailInUse)
                return emailNotAvailable;
            if(isTheUsernameInUse)
                return usernameNotAvailable;


        }catch (Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;

        }

        return emailAndUsernameAvailable;

    }

    public String checkCorrectPassword(String sellerid, String emailPassEncoded){

        String hashedCredentials = jedcon.call(jedis ->
                jedis.hget(RedisKey.HASH_selleridToHashedCredentials, sellerid));

        if(!JedisStoreDB.sha256(emailPassEncoded).equals(hashedCredentials))
            return Server.wrongCredentials;

        return Server.correctCredentials;
    }

    public String getIdByEmail(String email){

        try{

            return jedcon.call(jedis -> jedis.hget(RedisKey.emailToUserid, email));
        }catch (Exception E){

            return JedisStoreDB.databaseError;
        }
    }

    // Post Seller
    public String registerUser(User user){

        // Check the session for a valid session and transfer cart

        String areTheyAvailable = checkIfEmailAndUserNameAvailable(user.getEmail(), user.getUsername());

        if( areTheyAvailable.equals(bothUnavailable) ||
            areTheyAvailable.equals(emailNotAvailable) ||
            areTheyAvailable.equals(usernameNotAvailable))
            return areTheyAvailable;

        String userid = user.getid();

        String userPassEncoded = jedcon.getUserPassAsBase64(user.getEmail(), user.getPassword());

        user.setPassword("__HIDDEN__");

        String sellerAsJson = PMain.gson.toJson(user);

        try{

            jedcon.call(jedis -> {

                Pipeline pipelined = jedis.pipelined();

                pipelined.set(userid, sellerAsJson);
                pipelined.hset(RedisKey.emailToUserid, user.getEmail(), userid);
                pipelined.sadd(RedisKey.listOfAlluserids, userid);
                pipelined.sadd(RedisKey.listOfallEmails, user.getEmail());
                pipelined.hset(RedisKey.HASH_selleridToHashedCredentials, userid, JedisStoreDB.sha256(userPassEncoded));

                pipelined.sync();
                pipelined.close();


                return null;
            });

        }catch (Exception e){
            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        return  successfulRegistration;

    }

    public String getUser(String userid){

        return jedcon.call(jedis -> jedis.get(userid));
    }

    public String updateUser(String userid, User user){

        // If the email has been updated we need to remove it from the list all-emails
        // aswell as remove the old instance in the hash email-to-sellerid

        User oldUser = PMain.gson.fromJson(getUser(userid), User.class);

        if (!oldUser.getEmail().equals(user.getEmail())) {
            try{

                jedcon.call(jedis -> {

                    Pipeline pipelined = jedis.pipelined();

                    pipelined.hdel(RedisKey.emailToUserid, oldUser.getEmail());
                    pipelined.hset(RedisKey.emailToUserid, user.getEmail(), userid);
                    pipelined.srem(RedisKey.listOfallEmails, oldUser.getEmail());
                    pipelined.sadd(RedisKey.listOfallEmails, user.getEmail());
                    pipelined.sync();
                    pipelined.close();

                    return null;
                });


            }catch (Exception e){
                e.printStackTrace();
                return JedisStoreDB.databaseError;
            }
        }

        // 2nd: We update the user on the users: RedisKey

        String updatedUserJSON = PMain.gson.toJson(user);

        try{
            jedcon.call(jedis -> jedis.set(userid, updatedUserJSON));

        }catch(Exception e){

            e.printStackTrace();
            return JedisStoreDB.databaseError;
        }

        // 3rd: we Update the users products so they are sold by the new username

        // 3.1 get all the products

        List<Product> listOfUsersProducts = getAllProductsFromUserid(userid);

        for(Product p: listOfUsersProducts){

            p.setSellerid(user.getEmail());
            productService.updateProduct(p, p.getProductid());

            // ooops I actually need to have the productid in JAVA
        }

        return updatedUserJSON;
    }

    public class CustomerInfo{

        private Orderable orderable;
        private boolean isItNoAccount;

        public CustomerInfo(Orderable orderable, boolean isItNoAccount){

            this.orderable = orderable;
            this.isItNoAccount = isItNoAccount;
        }

        public Orderable getOrderable(){

            return this.orderable;
        }

    }

}
