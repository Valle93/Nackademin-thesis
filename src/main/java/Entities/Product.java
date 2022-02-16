package Entities;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
public class Product {

    private int mysql_id;
    private List<Location> locations;
    private String productid;
    private String name;
    private Integer products_sold;
    private String brand;
    private ProductCathegory cathegory;
    private double price;
    private static Random random = new Random();

    public Product(int mysql_id, String name, ProductCathegory cathegory, double price){

        this.products_sold = null;
        this.mysql_id = mysql_id;
        this.name = name;
        this.price = price;
        this.brand = Brand.values()[random.nextInt(5)].getName();

    }

    public Product(int mysql_id, String name, ProductCathegory cathegory, double price, String brand, int products_sold){

        this.mysql_id = mysql_id;
        this.name = name;
        this.cathegory = cathegory;
        this.price = price;
        this.brand = brand;
        this.locations = new ArrayList<>();
        this.products_sold = products_sold;

    }

    public Product(String productid, String name, ProductCathegory cathegory, double price) {

        this.productid = productid;
        this.name = name;
        this.cathegory = cathegory;
        this.price = price;
        this.locations = new ArrayList<>();
        this.brand = Brand.values()[random.nextInt(5)].getName();
        this.products_sold = random.nextInt(1000);


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

    public String getCathegoryString() {
        return cathegory.getString();
    }

}
