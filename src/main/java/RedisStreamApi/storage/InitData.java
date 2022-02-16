package RedisStreamApi.storage;

import RedisStreamApi.Enteties.Injectable;
import RedisStreamApi.Enteties.Product;
import RedisStreamApi.Enteties.ProductCathegory;
import RedisStreamApi.Enteties.User;
import RedisStreamApi.Services.*;
import RedisStreamApi.Enteties.ManyProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Injectable
public class InitData {

    private ProductService productService;
    private UserService userService;
    private UserServiceLua SSL;
    private ProductServiceLua PSL;
    private SessionService sessionService;
    private boolean addManyProducts;
    private boolean addLotsOfProductsInCart;
    private boolean orderCarts;


    public InitData(ProductService productService, UserService userService,
                    UserServiceLua SSL, ProductServiceLua PSL, SessionService sessionService){

        this.sessionService = sessionService;
        this.productService = productService;
        this.userService = userService;
        this.SSL = SSL;
        this.PSL = PSL;

        addManyProducts = false;
        addLotsOfProductsInCart = true;
        orderCarts = true;

        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("------------------------------------------------");
        System.out.println("-Here Starts Init Data : Here Starts Init Data -");
        System.out.println("------------------------------------------------");
        System.out.println("------------------------------------------------");
        System.out.println();


        User user1 = new User("Shania@website.com", "Shania", "123");

        User user2 = new User("Frank@website.com", "Frank", "123");

        User user3 = new User("Monday@website.com", "Monday", "1234");

        userService.registerUser(user1);
        userService.registerUser(user2);
        userService.registerUser(user3);

        Product product1 = new Product(Product.generateID(), "Phone", "new condition",
                ProductCathegory.ELECTRONICS, 5000, 100);

        Product product2 = new Product(Product.generateID(), "Dragonball", "new condition",
                ProductCathegory.LITERATURE, 5000, 300);

        Product product3 = new Product(Product.generateID(), "football", "slightly damaged",
                ProductCathegory.SPORTS, 5000, 5);

        Product product4 = new Product(Product.generateID(), "golf", "slightly damaged",
                ProductCathegory.SPORTS, 5000, 5);

        long begin = System.nanoTime();

        productService.postProductTransactional(product1, user1.getid());

        System.out.println("Time in nano : " + (System.nanoTime() - begin));

        productService.postProductTransactional(product2, user1.getid());

        productService.postProductTransactional(product3, user1.getid());

        productService.postProductTransactional(product4, user1.getid());

        String userid = userService.getIdByEmail("Shania@website.com");

        System.out.println(product1.getProductid());

        System.out.println(product2.getProductid());

        System.out.println(product3.getProductid());

        System.out.println(product4.getProductid());

        System.out.println("Shania@website.com");

        System.out.println(userid);

        //-------------
        //-------------
        //Here we add tons of Products


        if (addManyProducts) {
            ManyProducts manyProducts = new ManyProducts();

            manyProducts.getProducts().forEach(listProduct -> {

                listProduct.forEach(product -> {

                    productService.postProductTransactional(product, manyProducts.getUser().getid());
                });
            });
        }


        //------------
        //------------
        //------------

        // Down Here We should have the userids add products to a cart and then order their cart

        Set<String> productIDS = productService.getAllProductIDS();

        List<String> userIDS = new ArrayList<>();

        userIDS.add(userService.getIdByEmail("Shania@website.com"));

        userIDS.add(userService.getIdByEmail("Frank@website.com"));

        userIDS.add(userService.getIdByEmail("Monday@website.com"));

        // BELOW WE ADDLOTSOFPRODUCTS
        // BELOW WE ADDLOTSOFPRODUCTS
        // BELOW WE ADDLOTSOFPRODUCTS

        if (addLotsOfProductsInCart) {


            userIDS.forEach(s -> {

                sessionService.initialiseSession(s);

                HashMap<String, Integer> cart = new HashMap<>();

                productIDS.forEach(s1 -> {

                    double amount = Math.random() * 200;

                    cart.put(s1, (int) amount );

                });

                cart.forEach((s1, integer) -> {

                    userService.addProduct(s, s1, integer);

                });

            });
        }


        // --------- BELOW WE ORDER THE CARTS
        // --------- BELOW WE ORDER THE CARTS
        // --------- BELOW WE ORDER THE CARTS
        // --------- BELOW WE ORDER THE CARTS

        if(orderCarts && !addLotsOfProductsInCart){

            userIDS.forEach(s -> {

                userService.orderCart(s);
            });
        }



    }
}
