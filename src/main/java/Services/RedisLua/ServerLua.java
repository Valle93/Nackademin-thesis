package Services.RedisLua;

import Entities.*;
import com.google.gson.Gson;
import spark.Spark;

import static spark.Spark.*;

public class ServerLua {

    private static final String COOKIE_SESSION = "sessionCookie";
    public static final String requestReceived = "Request received successfully";
    public static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    public static final String noCurrentSession = "No current session, please log in";
    public static final String tooManyRequests = "too many requests, slow down";
    public static final String wrongCredentials = "Wrong email or password";
    public static final String correctCredentials = "Credentials accepted";

    private ProductServiceLua productService;
    private JedisStoreDB jedisStoreDB;
    private ProductServiceLua PSL;

    public ServerLua(ProductServiceLua productServiceLua, JedisStoreDB jedisStoreDB) {

        this.PSL = productServiceLua;
        this.jedisStoreDB = jedisStoreDB;

        port(8081);
        Gson gson = new Gson();

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(e.getMessage());
            e.printStackTrace();
            System.out.println("Request Failed");
        });

        path("/lua", () -> {

            post("/", (req, res) ->

            productService.postProduct(gson.fromJson(req.body(), Product.class), req.headers("sellerid")), gson::toJson);

            Spark.get("/", (req,res) -> productServiceLua.getAllProducts(), gson::toJson);

            Spark.post( "/update", (req,res) -> productService.postProduct(gson.fromJson(req.body(),
                    Product.class), req.params("productid")), gson::toJson);

            Spark.get("/getProduct/:productid", (req, res) ->
                    productServiceLua.getProduct(req.params("productid")));

            Spark.get("/getProducts/:start/:end", (req,res) ->
                    productServiceLua.getProductsFromPriceRange(
                            Integer.parseInt(req.params("start")),Integer.parseInt(req.params("end"))), gson::toJson);


            Spark.delete("/delete/:productid", (req,res) ->

            productService.deleteProduct(req.params("productid")));

        });

    }

}
