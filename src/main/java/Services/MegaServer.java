package Services;

import ApiCaller.ApiTestResults;
import Entities.*;
import Services.DynamicLua.LuaScriptRunner;
import Services.MySQLservice.MysqlConnector;
import Services.MySQLservice.ProductServiceSQL;
import com.google.gson.Gson;
import spark.Spark;

import java.sql.SQLException;
import java.util.HashMap;

import static spark.Spark.path;
import static spark.Spark.port;

public class MegaServer {

    private HashMap<String, Service> services = new HashMap<>();

    public MegaServer(boolean addManyProducts) {

        Gson gson = new Gson();

        JedisStoreDB jedisStoreDB = new JedisStoreDB();

        MysqlConnector mysqlConnector = new MysqlConnector();

        RedisBasicNoLua productService = new RedisBasicNoLua(jedisStoreDB);

        ProductServiceSQL productServiceSQL = null;

        try {

            productServiceSQL = new ProductServiceSQL(mysqlConnector.getConnection());

        } catch (SQLException throwables) {
            throwables.printStackTrace();

        }


        // ProductServiceLua productServiceLua = new ProductServiceLua(jedisStoreDB);

        LuaScriptRunner luaScriptRunner = new LuaScriptRunner(jedisStoreDB);

        // luaScriptRunner.start();

        try {
            assert productServiceSQL != null;
            InitData initData = new InitData(productServiceSQL, productService, addManyProducts);
        } catch (SQLException throwables) {

            throwables.printStackTrace();
        }


        services.put("Redis", productService);
        // services.put("Services/RedisLua", productServiceLua);
        services.put("dynamicLua",luaScriptRunner);
        services.put("SQL", productServiceSQL);

        port(8081);

        ProductServiceSQL finalProductServiceSQL = productServiceSQL;
        ProductServiceSQL finalProductServiceSQL1 = productServiceSQL;
        ProductServiceSQL finalProductServiceSQL2 = productServiceSQL;

        path("/api", () -> {

           path("/redis", () -> {

               Spark.get("/test",(req,res) -> "Pong");

               Spark.get("/getProduct/:productid", (req, res) ->
                       productService.getProduct(req.params("productid")));

               Spark.get("/productsByPageJSON/:page", (req,res) ->
                       productService.getProductsByPageJSON(Integer.parseInt(req.params("page"))));

               Spark.get("/productsByPage/:page", (req,res) ->
                       productService.getProductsByPage(Integer.parseInt(req.params("page"))),gson::toJson);

               Spark.get("/AdvQuery/:price_min/:price_max/:locations/:brands", (req,res)->

                    productService.advancedQuery(advQueryFromParameters(req.params("price_min"),
                            req.params("price_max"), req.params("locations"), req.params("brands"))));

                    });

           /*path("/redisLua", () -> {

               Spark.get("/getProduct/:productid", (req, res) ->
                       productServiceLua.getProduct(req.params("productid")), gson::toJson);

               Spark.get("/getProductJSON/:productid", (req, res) ->
                       productServiceLua.getProductJSON(req.params("productid")));

               Spark.get("/productsByPage/:page", (req,res) ->
                       productServiceLua.getProductsByPage(Integer.parseInt(req.params("page"))),gson::toJson);

           });*/

           path("/dynamicLua", () -> {

               Spark.get("/AdvQuery/:price_min/:price_max/:locations/:brands", (req,res) ->

                               luaScriptRunner.putApiCall(

                                       advQueryFromParameters(req.params("price_min"), req.params("price_max"),
                                               req.params("locations"), req.params("brands"))));

           });

           path("/sql", () -> {

               Spark.get("/AdvQuery/:price_min/:price_max/:locations/:brands", (req,res) ->

                               finalProductServiceSQL2.advancedQuerySearch(

                                       advQueryFromParameters(req.params("price_min"), req.params("price_max"),
                                               req.params("locations"), req.params("brands"))),

                       gson::toJson);

               Spark.post("/postResults", (req,res) -> finalProductServiceSQL.postResults(
                       gson.fromJson(req.body(), ApiTestResults.class)
               ));

               Spark.get("/getString", (req,res) ->


                       finalProductServiceSQL.testThisService()

               );

               Spark.get("/getFromID/:product_id", (req,res) ->

                       finalProductServiceSQL1.getProductFromId(req.params("product_id")), gson::toJson

                   );



               Spark.get("/ping", (req,res) -> "pong");



           });

        });

        // SELECT product.productid, product_name, location.location_name, product.price,product.brand, product.cathegory
        // FROM product JOIN product_to_location ON product.productid = product_to_location.product_id
        // JOIN location ON location.id = product_to_location.location_id
        // WHERE price BETWEEN ? AND ? AND brand IN (?,?) AND location_name IN (?,?)

    }

    AdvancedQueryCall advQueryFromParameters(String min_price, String max_price, String locations, String brands){


        String[] locationIds = locations.split("&");

        Location[] locationRes = new Location[locationIds.length];

        String[] brandsIds = brands.split("&");

        Brand[] brandRes = new Brand[brandsIds.length];

        for(int i = 0; i < locationIds.length; i++){

            locationRes[i] = Location.locationFromStringInt(locationIds[i]);
        }

        for (int i = 0; i < brandsIds.length; i++) {

            brandRes[i] = Brand.brandFromStringInt(brandsIds[i]);
        }

        return new AdvancedQueryCall(Integer.parseInt(min_price),Integer.parseInt(max_price), locationRes, brandRes);

    }

}
