package RedisStreamApi.Enteties;

import RedisStreamApi.PMain;
import Entities.RedisKey;

import java.util.UUID;

public class Product {

    private String productid;
    private String sellerid;
    private String name;
    private String description;
    private ProductCathegory cathegory;
    private int qty;
    private double price;

    public Product(String sellerid, String name, String description, ProductCathegory cathegory,
                   int qty, double price) {

        this.productid = PMain.key(RedisKey.enumProduct.singular(), PMain.generateId());
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

    public void generateNewId() {

        setProductid(PMain.key(RedisKey.enumProduct.singular(), PMain.generateId()));
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
        return cathegory.getString();
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

    public String getCathegoryString() {

        return this.cathegory.getString();
    }

    public static String generateID() {

        return (key(RedisKey.enumProduct.singular(), randomUUID()));
    }

    static String key(String... parts) {
        return String.join(":", parts);
    }

    static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
