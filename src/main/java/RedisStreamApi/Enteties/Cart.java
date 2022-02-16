package RedisStreamApi.Enteties;

import java.util.HashMap;
import java.util.Map;

public class Cart {

    private String userid;
    private Map<String, Product> productidAndProduct;

    public Cart(String userid){

        this.userid = userid;
        this.productidAndProduct = new HashMap<>();

    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Map<String, Product> getProductidAndProduct() {
        return productidAndProduct;
    }
}
