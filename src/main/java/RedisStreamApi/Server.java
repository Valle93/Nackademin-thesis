package RedisStreamApi;

import Entities.JedisStoreDB;
import RedisStreamApi.Enteties.Product;
import Entities.RedisKey;
import RedisStreamApi.Enteties.Cart;
import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Session;
import RedisStreamApi.Enteties.User;
import RedisStreamApi.Services.*;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.UnsupportedEncodingException;

import static spark.Spark.*;

@Injectable
public class Server {

    private static final String COOKIE_SESSION = "sessionCookie";
    public static final String requestReceived = "Request received successfully";
    public static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    public static final String noCurrentSession = "No current session, please log in";
    public static final String tooManyRequests = "too many requests, slow down";
    public static final String wrongCredentials = "Wrong email or password";
    public static final String correctCredentials = "Credentials accepted";

    private ProductService productService;
    private SessionService sessionService;
    private UserService userService;
    private JedisStoreDB jedisStoreDB;
    private UserServiceLua SSL;
    private ProductServiceLua PSL;

    public Server(ProductService productService, SessionService sessionService,
                  UserService userService, UserServiceLua userServiceLua,
                  ProductServiceLua productServiceLua, JedisStoreDB jedisStoreDB) {

        this.sessionService = sessionService;
        this.productService = productService;
        this.userService = userService;
        this.SSL = userServiceLua;
        this.PSL = productServiceLua;
        this.jedisStoreDB = jedisStoreDB;

        port(8080);
        Gson gson = new Gson();

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(e.getMessage());
            e.printStackTrace();
            System.out.println("Request Failed");
        });

        Spark.post("/login", this::authenticate);

        Spark.post("/logout", (req, res) -> logout(req.cookie(COOKIE_SESSION), res));

        Spark.get("testSpeedDoNothing", (req, res) -> productServiceLua.testSpeedBetweenJavaAndRedis());

        Spark.post("/add-product/:productid/:qty", (req,res) -> addProduct(
                req, res, req.params("productid"), Integer.parseInt(req.params("qty"))), gson::toJson);

        Spark.get("/page/:number", (req,res) -> productService.getProductsByPage(Integer.valueOf(req.params("number"))), gson::toJson);

        path("/api", () -> {

            afterAfter("/*", (req, res) -> {currentUser.remove();}) ;

            Spark.get("/getCart", (req, res) -> userService.getCart(req.cookie(COOKIE_SESSION)), gson::toJson);

            Spark.post("/orderCart", (req, res) -> userService.orderCart(req.cookie(COOKIE_SESSION)));

            path("/user", () -> {

                before("/*", this::beforeUsersApiMethod);

               // post("/order-products", (req, res) -> userService.orderCart(req.headers("Authorization").substring(6)), gson::toJson);

                post("/", (req, res) -> registerUser(gson.fromJson(req.body(), User.class),
                        req.cookie(COOKIE_SESSION), res), gson::toJson);

                Spark.get("/", (req,res) -> userService.getAllUsers(), gson::toJson);

                Spark.get("/getAllProducts/:userid", (req, res) ->
                        userService.getAllProductsFromUserid(req.params(("userid"))), gson::toJson);

                Spark.get("/getuser", (req, res) ->
                        userService.getUser(req.headers("userid")), gson::toJson);

                delete("/delete/:email", (req,res) -> userService.deleteUser(req.params("email")));

                post("/update", (req, res) ->
                        userService.updateUser(req.headers("userid"), gson.fromJson(req.body(), User.class)), gson::toJson);

            });

            path("/userLua", () -> {

                delete("/deleteViaLua/:email", (req,res) -> userServiceLua.deleteSeller(req.params("email")));

                Spark.get("/getAllProductsLua/:userid", (req, res) ->
                        userServiceLua.getAllProductsFromSellerid(req.params(("userid"))), gson::toJson);

                Spark.get("/", (req,res) -> userServiceLua.getAllSellers(), gson::toJson);

            });


            path("/products", () -> {

            /*    Spark.get("/products/:page", (req,res) -> {

                    productService.getProductsByPage(req.params("page"));
                }, gson::toJson);*/

                Spark.get("/:search", (req, res) -> productService.Search(req.params("search")), gson::toJson);

                post("/", (req, res) ->

                productService.postProductTransactional(gson.fromJson(req.body(), Product.class), req.headers("sellerid")), gson::toJson);

                Spark.get("/", (req,res) -> productService.getAllProducts(), gson::toJson);

                Spark.get("/Lua", (req,res) -> productServiceLua.getAllProducts(), gson::toJson);

                post("/updateProduct/:productid", (req,res) ->

                productService.updateProduct(gson.fromJson(req.body(), Product.class), req.params("productid")), gson::toJson);

                Spark.get("/getProduct/:productid", (req, res) ->

                productService.getProduct(req.params("productid")), gson::toJson);

                delete("/delete/:productid/:sellerid", (req, res) ->

                productService.deleteProduct(req.params("sellerid"), req.params("productid")));

            });

        });

    }

    public String logout(String sessionid, Response res){

        if(sessionid == null){

            return "No current session";
        }

        sessionService.removeSessionIfExists(sessionid);

        res.removeCookie("/", COOKIE_SESSION);

        return "Logged out";

    }

    public String registerUser(User userGSON, String sessionid, Response res){

        User user = new User(userGSON.getEmail(), userGSON.getUsername(), userGSON.getPassword());

        // Check session and if session then transferCart;

        // Only get from a noAccountUser

        System.out.println("server: registerUser: userid: " + user.getid());

        if(user.getid() == null){

            return "Server Error";
        }

        userService.registerUser(user);

        if (sessionid != null) {

            String validity = sessionService.checkValidity(sessionid);

            if(validity.equals(SessionService.validSession) &&
                    sessionid.startsWith(RedisKey.enumNoAccountSession.singular())){

                String sessionJSON = jedisStoreDB.call(jedis -> jedis.get(sessionid));

                Session session = PMain.gson.fromJson(sessionJSON, Session.class);

                transferCart(session, user.getid());

                sessionService.removeSessionIfExists(sessionid);

            }

        }

        res.removeCookie("/", COOKIE_SESSION);

        return user.getid();

    }

    public void beforeUsersApiMethod(Request req, Response res){

        String sessionUUID = req.cookie(COOKIE_SESSION);

        System.out.println("BeforeUsersApiMethod: We are in here");

        if(sessionUUID == null){

            halt(401, noCurrentSession);

        }

        String sessionJSON = jedisStoreDB.call(jedis -> jedis.get(sessionUUID));

        Session session = PMain.gson.fromJson(sessionJSON, Session.class);

        try {
            currentUser.set(session.getSessionData().get("userid"));

        } catch (Exception e) {

            halt(403, noCurrentSession);
        }

        if(sessionService.checkValidity(sessionUUID).equals(SessionService.invalidSession)){

            res.removeCookie(COOKIE_SESSION);

            halt(401, noCurrentSession);
        }

        // add session:uuid:requests   uuid something

        if(sessionService.limitRequests(sessionUUID).equals(tooManyRequests)){

            halt(401, tooManyRequests);
        }

        currentUser.get();

    }

    public String addProductToCart(String productid, int amount, Product product, Cart cart){

        if(cart.getProductidAndProduct().containsValue(productid)){

            int amountInCart = cart.getProductidAndProduct().get(productid).getQty();

            product.setQty(amountInCart + amount);

            cart.getProductidAndProduct().put(productid, product);

            return "There was already an instance of that item";
        }

        product.setQty(amount);

        cart.getProductidAndProduct().put(productid, product);

        return "No current instance, so we added one";

    }

    public String transferCart(Session session, String userid){

        String userJSON = jedisStoreDB.call(jedis -> jedis.get(userid));

        User user = PMain.gson.fromJson(userJSON, User.class);

        session.getCart().getProductidAndProduct().forEach((s, product) -> {

            addProductToCart(s, product.getQty(), product, user.getCart());
        });

        jedisStoreDB.call(jedis -> jedis.set(userid, PMain.gson.toJson(user)));

        return "Products transfered to cart";
    }

    public String addProduct(Request req, Response res, String productid, int qty){

       boolean doesTheProductExist = sessionService.isTheProductidValid(productid);

       if(!doesTheProductExist)
           return SessionService.noSuchProduct;

        String sessionid = req.cookie(COOKIE_SESSION);

        //Check if the session is valid, if not: remove it

        String sessionValidity = sessionService.isTheSessionValid(sessionid);

        if(sessionValidity.equals(SessionService.noSession)){

            res.removeCookie(COOKIE_SESSION);

            String noAccountSession = sessionService.intialiseNoAccountSession();

            res.cookie("/", COOKIE_SESSION, noAccountSession, -1, false);

            System.out.println("We are in the no-account-session !");

            return userService.addProduct(noAccountSession, productid, qty);
        }

        if(sessionid.startsWith(RedisKey.enumNoAccountSession.singular()))
            sessionService.extendNoAccountUserTime(sessionid);
        else {

            sessionid = sessionService.getUseridFromSession(sessionid);
        }
        // if we have a normal session we need to send in the userid

        return userService.addProduct(sessionid, productid, qty);
    }

    public String authenticate(Request req, Response resp) throws UnsupportedEncodingException {

        String authorization = req.headers("Authorization").substring(6);

        String email = jedisStoreDB.getEmailFromAuthorization(authorization);

        String userid = userService.getIdByEmail(email);

        String loginResult = userService.login(email, authorization);

        if(userid == null || !loginResult.equals(Server.correctCredentials))
            return Server.wrongCredentials;


        // What if we log in while they already have a session? If they removed cookies

        if(sessionService.checkValidity(userid + ":session").equals(SessionService.validSession)){

            // This happens when we log in, but we already have a session in the database
            // Maybe because the user removed all cookies from their browser

            resp.cookie(COOKIE_SESSION, userid + ":session");
            return "Logged in";
        }

        String sessionUUID = sessionService.initialiseSession(userid);

        String sessionType = req.cookie(COOKIE_SESSION);

        resp.cookie(COOKIE_SESSION, sessionUUID);

        boolean willWeTransferCart = false;

        if(sessionType != null && sessionType.startsWith(RedisKey.enumNoAccountSession.singular())){

            willWeTransferCart = true;
        }

        String sessionJSON = sessionService.removeSessionIfExists(sessionType);

        if(sessionJSON == null){

            //The user had a sessioncookie but the cookie did not exist in the database
        }

        if(!sessionJSON.equals(SessionService.noSession) && willWeTransferCart){

            Session session = PMain.gson.fromJson(sessionJSON, Session.class);

            transferCart(session, userid);

        }


        return userid;

    }

}
