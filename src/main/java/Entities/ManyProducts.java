package Entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManyProducts {

    String[] listOfBooks = {"Great Gatsby", "Jaws", "Shawshank Redemption", "The Shining", "Jurassic Park",
                                    "Crime and Punishment", "War and Peace", "Bible", "Tintin", "The Process",
                                    "1984", "Oliver Twist", "Charlie and the Chocolate Factory", "Winnie the Puh",
                                    "The Jungle Book", "Pride and Prejudice", "King Arthur", "Brave new World",
                                    "The jungle fable", "Chuckleberry Grin"};

    String[] listOfSportItems = {"football", "soccerball", "baseball-glove", "tennisracket", "ping-pong-balls",
                                        "Tennis-balls", "basketball", "Dumbell", "Barbell", "5kg weight", "10kg weight",
                                        "15kg weight", "20kg weight", "25kg weight", "headband", "waterbottle",
                                        "Headband", "arm-pads", "knee-pads", "handball"};

    String[] listOfElectronicItems = {"USB-cord", "phone-case", "keyboard", "mouse", "Starcraft", "HD-TV",
                                              "Halo 5", "Dota 3", "Laptop", "GraphicsCard", "RAM", "Battery",
                                                "Half-life 3", "Silent Hills", "Metal Gear Solid 5", "Headphones",
                                                "Mash Candycoot", "Reloaded", "Terraforming Sun", "Donkey Bong"};

    String[] listOfClothingItems = {"T-Shirt", "Long-sleeve shirt", "Cool Hoodie", "Cute Skirt", "blue jacket",
                                            "Wacky Shoes", "Hat", "Cap", "Socks", "Red Socks", "White Panties",
                                            "Grey Boxers", "brown Glasses", "Gold Necklace", "Yellow Coat",
                                            "blue hat", "Anorak", "Jeans", "Adidis", "2022-glasses"};

    private List<Product> products;

    public ManyProducts(boolean addManyProducts){

        Random random = new Random();

        this.products = new ArrayList<>();

        for(String s : listOfBooks){

            Product product = new Product(Product.generateID(), s, ProductCathegory.LITERATURE, random.nextInt(10000));


            if (addManyProducts) {

                Product product2 = new Product(Product.generateID(), s + "-ver2", ProductCathegory.LITERATURE,
                                                            random.nextInt(10000));

                products.add(product2);

            }


            products.add(product);

        }

        for(String s : listOfSportItems){

            Product product = new Product(Product.generateID(), s, ProductCathegory.SPORTS, random.nextInt(10000));

            if (addManyProducts) {

                Product product2 = new Product(Product.generateID(), s + "-ver2", ProductCathegory.LITERATURE,
                        random.nextInt(10000));

                products.add(product2);

            }

            products.add(product);
        }

        for(String s : listOfElectronicItems){

            Product product = new Product(Product.generateID(), s, ProductCathegory.ELECTRONICS, random.nextInt(10000));

            if (addManyProducts) {

                Product product2 = new Product(Product.generateID(), s + "-ver2", ProductCathegory.LITERATURE,
                        random.nextInt(10000));

                products.add(product2);

            }

            products.add(product);

        }

        for(String s : listOfClothingItems){

            Product product = new Product(Product.generateID(), s, ProductCathegory.CLOTHES, random.nextInt(10000));

            if (addManyProducts) {

                Product product2 = new Product(Product.generateID(), s + "-ver2", ProductCathegory.LITERATURE,
                        random.nextInt(10000));

                products.add(product2);

            }

            products.add(product);

        }

        if(!addManyProducts){

            return;
        }

        List<Product> generatedGarbageProducts = new ArrayList<>();

        for(Product p : products){

            for (int i = 0; i < 10; i++) {

                Product product = new Product(Product.generateID(), p.getName() + "." + i,
                        ProductCathegory.CLOTHES, random.nextInt(10000));

                generatedGarbageProducts.add(product);

            }
        }

        products.addAll(generatedGarbageProducts);

    }

    public List<Product> getProducts() {
        return products;
    }

}
