package Services.MySQLservice;


import ApiCaller.ApiTestResults;
import Entities.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductServiceSQL implements Service{

    private Connection connection;

    public ProductServiceSQL(Connection connection) throws SQLException {

        this.connection = connection;

        removeDataFromSystem();

    }

    public void setLocationIDsInLocationEnum() throws SQLException {

        String query = "SELECT * FROM location";

        PreparedStatement prep = connection.prepareStatement(query);

        ResultSet rs = prep.executeQuery();

        while (rs.next()){

            Location location = Location.valueOf(rs.getString("location_name").toUpperCase());

            location.setID(rs.getInt("location_id"));

        }

    }

    public List<Product> advancedQuerySearch(AdvancedQueryCall advQuery){

        // I want a products id, name, location, price, brand, and category

        List<Product> products = new ArrayList<>();

        int paramIndex = 1;

        String query = "SELECT product.product_id, product_name," +
                " location.location_name, product.price," +
                "product.brand, product.cathegory FROM product " +
                "JOIN product_to_location " +
                "ON product.product_id = product_to_location.product_table_id " +
                "JOIN location ON location.location_id = " +
                "product_to_location.location_table_id " +
                "WHERE price BETWEEN ? AND ? " +
                "AND brand IN (";
                for(Brand b : advQuery.getBrands()){
                    query += "?,";
                }
                query = query.substring(0, query.length() - 1);
                query += ") AND location_name IN (";
                for(Location l : advQuery.getLocations()){
                    query += "?,";
                }
                query = query.substring(0, query.length() - 1);
                query += ")";


        try {


            PreparedStatement prep = connection.prepareStatement(query);

            prep.setInt(paramIndex, advQuery.getPrice_min());

            prep.setInt(++paramIndex, advQuery.getPrice_max());

            for (++paramIndex; paramIndex <= advQuery.getBrands().length + 2; paramIndex++) {

                prep.setString(paramIndex, advQuery.getBrands()[paramIndex - 3].getName());
            }

            int paramPlaceholder = paramIndex;

            for (; paramIndex < paramPlaceholder + advQuery.getLocations().length; paramIndex++) {

                prep.setString(paramIndex, advQuery.getLocations()
                        [paramIndex - 3 - advQuery.getBrands().length].getName());

            }

            ResultSet rs = prep.executeQuery();

            while(rs.next()){

                Product product = new Product(rs.getInt("product_id"), rs.getString("product_name"),
                                                    ProductCathegory.valueOf(rs.getString("cathegory").toUpperCase()),
                        rs.getDouble("price"));

                products.add(product);

            }

            } catch (Exception e) {
                e.printStackTrace();
            }



        return products;

    }

    public void fillProductToLocationTable(List<Product> products) throws SQLException{

        products.forEach(product -> {

                String query = "INSERT INTO product_to_location (product_table_id, location_table_id) values ";

                for (Location l: product.getLocations()) {

                    query += "(?,?),";
                }

                query = query.substring(0, query.length() - 1);


            PreparedStatement prep = null;

            try {
                prep = connection.prepareStatement(query);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


            for (int i = 0; i < product.getLocations().size(); i++) {

                try {
                    prep.setInt(1 + (2 * i), product.getMysql_id());
                    prep.setInt(2 + (2 * i), product.getLocations().get(i).getID());

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }

            try {
                prep.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        });

    }

    public void addLocationsInTable() throws SQLException {


        String query = "INSERT INTO location (location_name) VALUE ";

        for(Location l : Location.values()){

            query += "(?),";

        }

        query = query.substring(0, query.length() - 1);

        PreparedStatement prep = connection.prepareStatement(query);

        for (int i = 1; i <= Location.values().length ; i++) {

            prep.setString(i, Location.values()[i - 1].getName());

        }

        prep.execute();

    }

    public Product getProductFromId (String productid) {

        try {

            String query = "SELECT * FROM product WHERE productid = ?";

            PreparedStatement prep = connection.prepareStatement(query);

            prep.setInt(1, Integer.valueOf(productid));

            ResultSet rs = prep.executeQuery();

            if(!rs.next()){

                 return new Product(1, "fail", ProductCathegory.ELECTRONICS, 1.1);

            }

            System.out.println("Here");

            return new Product(rs.getInt("productid"),
                    rs.getString("product_name"),
                    ProductCathegory.valueOf(rs.getString("cathegory").toUpperCase()),
                    rs.getInt("price"));


        } catch (Exception e) {

            e.printStackTrace();

        }

        return new Product(1, "fail", ProductCathegory.ELECTRONICS, 1.1);

    }

    public String testThisService(){

        return "just-a-random-string";
    }

    public List<Product> get10ProductsByPopularity(int pageNr){

        List<Product> products = new ArrayList<>();

        return products;

    };

    public List<Product> get10ProductsByCategory(int pageNr){

        List<Product> products = new ArrayList<>();

        return products;
    }

    public String update(Product product, String productid) throws SQLException {

        String query = "UPDATE product SET product_name = ?," +
                "cathegory = ?, price = ? WHERE productid = ?";

        PreparedStatement prep = connection.prepareStatement(query);

        prep.setString(1, product.getName());
        prep.setString(2, product.getCathegoryString());
        prep.setInt(3, (int) product.getPrice());
        prep.setString(4, productid);

        prep.execute();

        return "product updated";

    }

    public String deleteById(String productid) throws SQLException {

        String query = "DELETE FROM product WHERE productid = ?";

        PreparedStatement prep = connection.prepareStatement(query);

        prep.setString(1, productid);

        prep.execute();

        return "product deleted";

    }

    public void removeDataFromSystem() throws SQLException {

        String query1 = "DELETE FROM product";

        PreparedStatement prep = connection.prepareStatement(query1);

        prep.execute();

        String query2 = "DELETE FROM location";

        prep = connection.prepareStatement(query2);

        prep.execute();

        String query3 = "DELETE FROM product_to_location";

        prep = connection.prepareStatement(query3);

        prep.execute();

    }

    public String postResults(ApiTestResults apiTR) {

        System.out.println("We Are HEEEEEERE");

        String query = "INSERT INTO apicallstestresults (service, apicallers, calls_per_thread," +
                "tm0_10, tm10_20, tm20_30, tm30_40, tm40_50, tm0_50, tm50_100, tm100_150, tm150_200, tm200_beyond, total_time) " +
                "" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try {
            PreparedStatement prep = connection.prepareStatement(query);

            prep.setString(1, apiTR.getApi_url());
            prep.setInt(2, apiTR.getNumberOfThreads());
            prep.setInt(3, apiTR.getCallsPerThread());

            for (int i = 0; i < apiTR.getMillis0to50().length; i++) {

                prep.setInt(i + 4, apiTR.getMillis0to50()[i]);
            }

            for (int i = 0; i < apiTR.getMillis0to200().length; i++) {

                prep.setInt(9 + i, apiTR.getMillis0to200()[i]);
            }

            prep.setInt(14, apiTR.getTotaltime());

            prep.execute();

        } catch (SQLException throwables) {

            throwables.printStackTrace();
        }


        return "okeydokay";

    }

    public String postProduct(Product product) throws SQLException {

       String query = "INSERT INTO product (product_name, cathegory, brand, price, products_sold)" +
                "values (?, ?, ?, ?, ?)";

        PreparedStatement prep = connection.prepareStatement(query);

        prep.setString(1, product.getName());
        prep.setString(2, product.getCathegoryString());
        prep.setString(3, product.getBrand());
        prep.setInt(4, (int) product.getPrice());
        prep.setInt(5, product.getProducts_sold());

        prep.execute();

        return "Product posted";

    }

    public List<Product> getAllProducts() throws SQLException {

        List<Product> products = new ArrayList<>();

        ResultSet rs = connection.createStatement().executeQuery("select * from product");

        while(rs.next()){

            // id , name, category, price

            Product product = null;

            try {

                ProductCathegory cat = ProductCathegory.valueOf(rs.getString("cathegory").toUpperCase());

                product = new Product(rs.getInt("product_id"), rs.getString("product_name"),
                        cat, rs.getInt("price"), rs.getString("brand"),
                        rs.getInt("products_sold"));

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


            products.add(product);

        }

        return products;

    }

    public static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateProductId(){

        return key("product" , generateId());
    }

}
