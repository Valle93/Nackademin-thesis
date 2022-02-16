package RedisStreamApi.Consumer.Enteties;

import java.util.HashMap;
import java.util.Map;

public class CartJob {

    private String userid;
    private Map<String, Integer> productsAndQty = new HashMap<>();
    private Map<String, Product> productIDProduct = new HashMap<>();
    private Map<String, String> properties = new HashMap<>();

    public CartJob(){}

    public CartJob(String userid, Map<String, Integer> productsAndQty, Map<String, Product> productIDProduct) {

        this.userid = userid;
        this.productsAndQty = productsAndQty;
        this.productIDProduct = productIDProduct;

    }

    public Map<String, Product> getProductIDProduct() {
        return productIDProduct;
    }

    public void setProductIDProduct(Map<String, Product> productIDProduct) {
        this.productIDProduct = productIDProduct;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Map<String, Integer> getProductsAndQty() {
        return productsAndQty;
    }

    public void setProductsAndQty(Map<String, Integer> productsAndQty) {
        this.productsAndQty = productsAndQty;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
