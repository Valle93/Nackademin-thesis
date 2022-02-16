package Entities;

import Services.MySQLservice.ProductServiceSQL;
import Services.RedisBasicNoLua;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class InitData {

    private ProductServiceSQL productServiceSQL;
    private RedisBasicNoLua productService;
    private boolean addLotsOfProductsInCart;
    private boolean orderCarts;


    public InitData(ProductServiceSQL productServiceSQL, RedisBasicNoLua productService, boolean addManyProducts) throws SQLException {

        this.productServiceSQL = productServiceSQL;
        this.productService = productService;

        addLotsOfProductsInCart = true;
        orderCarts = true;

        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("------------------------------------------------");
        System.out.println("-Here Starts Init Data : Here Starts Init Data -");
        System.out.println("------------------------------------------------");
        System.out.println("------------------------------------------------");
        System.out.println();

        productServiceSQL.removeDataFromSystem();

        productServiceSQL.addLocationsInTable();

        productService.flushSystem();

        ManyProducts manyProducts = new ManyProducts(addManyProducts);

        manyProducts.getProducts().forEach(product -> {

                try {
                    productServiceSQL.postProduct(product);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            });


        List<Product> products = productServiceSQL.getAllProducts();

        Random random = new Random();

        System.out.println("MySQL-product-id : " + products.get(0).getMysql_id());

        products.forEach(product -> {

            int ran = random.nextInt(5) + 1;

            for (int i = 0; i < ran; i++) {

                Location location = Location.values()[i];

                product.getLocations().add(location);

            }

        });

        productServiceSQL.setLocationIDsInLocationEnum();

        // Now we can fill the product_to_location table

        productServiceSQL.fillProductToLocationTable(products);

        // Here We Fill the Redis Database

        products.forEach(product -> {

            product.setProductid(Product.generateID());

            productService.postProductTransactional(product, "123");

        });

    }
}
