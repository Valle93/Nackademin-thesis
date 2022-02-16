package RedisStreamApi.storage;

import RedisStreamApi.Enteties.Product;
import RedisStreamApi.Enteties.ProductCathegory;
import RedisStreamApi.Enteties.User;
import java.util.ArrayList;
import java.util.List;

public class ManyProducts {

    private String[] listOfBooks = {"Great Gatsby", "Jaws", "Shawshank Redemption", "The Shining", "Jurassic Park",
                                    "Crime and Punishment", "War and Peace", "Bible", "Tintin", "The Process",
                                    "1984", "Oliver Twist", "Charlie and the Chocolate Factory", "Winnie the Puh",
                                    "The Jungle Book", "Pride and Prejudice", "King Arthur", "Brave new World"};

    private String[] listOfSportItems = {"football", "soccerball", "baseball-glove", "tennisracket", "ping-pong-balls",
                                        "Tennis-balls", "basketball", "Dumbell", "Barbell", "5kg weight", "10kg weight",
                                        "15kg weight", "20kg weight", "25kg weight", "headband", "waterbottle"};

    private String[] listOfElectronicItems = {"USB-cord", "phone-case", "keyboard", "mouse", "Starcraft", "HD-TV",
                                              "Halo 5", "Dota 3", "Laptop", "GraphicsCard", "RAM", "Battery",
                                                "Half-life 3", "Silent Hills", "Metal Gear Solid 5", "Headphones"};

    private String[] listOfClothingItems = {"T-Shirt", "Long-sleeve t-shirt", "Cool Hoodie", "Cute Skirt", "blue jacket",
                                            "Wacky Shoes", "Hat", "Cap", "Socks", "Red Socks", "White Panties",
                                            "Grey Boxers", "brown Glasses", "Gold Necklace", "Yellow Coat"};

    private List<List<Product>> products;

    private User user;

    public ManyProducts(){

        this.user = new User("JohnDoe@website.com", "John", "Doe");

        this.products = new ArrayList<>();

        List<Product> clothingProducts = new ArrayList<>();

        List<Product> sportProducts = new ArrayList<>();

        List<Product> electronicProducts = new ArrayList<>();

        List<Product> literatureProducts = new ArrayList<>();

        for(String s : listOfBooks){

            Product product = new Product(Product.generateID(), s, "used",
                    ProductCathegory.LITERATURE, 5000, 300);

            literatureProducts.add(product);
        }

        for(String s : listOfSportItems){

            Product product = new Product(Product.generateID(), s, "used",
                    ProductCathegory.SPORTS, 5000, 300);

            sportProducts.add(product);
        }

        for(String s : listOfElectronicItems){

            Product product = new Product(Product.generateID(), s, "used",
                    ProductCathegory.ELECTRONICS, 5000, 300);

            electronicProducts.add(product);
        }

        for(String s : listOfClothingItems){

            Product product = new Product(Product.generateID(), s, "used",
                    ProductCathegory.CLOTHES, 5000, 300);

            clothingProducts.add(product);
        }

        products.add(electronicProducts);

        products.add(clothingProducts);

        products.add(sportProducts);

        products.add(literatureProducts);

    }

    public List<List<Product>> getProducts() {
        return products;
    }

    public User getUser() {
        return user;
    }
}
