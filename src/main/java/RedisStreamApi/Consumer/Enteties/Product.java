package RedisStreamApi.Consumer.Enteties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import RedisStreamApi.Consumer.CMain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {

    private String productid;
    private String sellerid;
    private String name;
    private String description;
    private String cathegory;
    private int qty;
    private double price;

    public Product(String sellerid, String name, String description, String cathegory, int qty, double price) {

        String productid = CMain.key(CMain.enumProduct.singular(), CMain.generateId());

        this.productid = productid;
        this.sellerid = sellerid;
        this.name = name;
        this.description = description;
        this.cathegory = cathegory;
        this.qty = qty;
        this.price = price;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public Product(String errorMessage){
        this.name = errorMessage;
    }

    public String getSellerid() {
        return sellerid;
    }

    public void setSellerid(String sellerid) {
        this.sellerid = sellerid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCathegory() {
        return cathegory;
    }

    public void setCathegory(String cathegory) {
        this.cathegory = cathegory;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static Map<String,String> productAsHashMap(Product product, String... args){

        Map<String,String> mapOfProduct = new HashMap<>();

        for (int i = 0; i < args.length - 1; i++) {

            mapOfProduct.put(args[i], args[i + 1]);
        }

        mapOfProduct.put("seller", product.getSellerid());
        mapOfProduct.put("name", product.getName());
        mapOfProduct.put("description", product.getDescription());
        mapOfProduct.put("cathegory", product.getCathegory());
        mapOfProduct.put("qty", String.valueOf(product.getQty()));
        mapOfProduct.put("price", String.valueOf(product.getPrice()));

        return mapOfProduct;
    }

    public static Product hashMapAsProduct(Map<String, String> mapOfProduct){

        Product product = new Product(mapOfProduct.get("seller"), mapOfProduct.get("name"),
                mapOfProduct.get("description"), mapOfProduct.get("cathegory"),
                Integer.parseInt(mapOfProduct.get("qty")),
                Double.parseDouble(mapOfProduct.get("price")));

        return product;
    }

    public static <T> List<T> getJsonFromObject(List<List<String>> list, Class<T> classT){



        Type classtype = Product.class;

        Class classtype2 = Product.class;

        List<T> list2 = new ArrayList<>();

        return list2;

    }

    public static List<Product> getJsonProductsFromListObject(List<List<String>> list, Gson gson) {

        List<Product> products = new ArrayList<>();


        for (List l: list){

            String asjson = "{\n";

            for (int i = 0; i < l.size(); i+=2) {

                String s = l.get(i).toString();

                if(s.equals("price") || s.equals("qty")){

                    asjson += "\"" + s + "\":" + l.get(i + 1) + ",\n";
                }
                else {

                    asjson += "\"" + s + "\":" + "\"" + l.get(i + 1) + "\"" + ",\n";
                }
            }

            asjson = asjson.substring(0, asjson.length() - 2);

            asjson += "\n}";

            System.out.println(asjson);
            System.out.println();
            System.out.println(l);

            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(asjson);
            JsonElement jelem = gson.fromJson(jo, JsonElement.class);
            JsonObject jobj = jelem.getAsJsonObject();

            products.add(gson.fromJson(jobj, Product.class));
        }

        return products;

    }

}
