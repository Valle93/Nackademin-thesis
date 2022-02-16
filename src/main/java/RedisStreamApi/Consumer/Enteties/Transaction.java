package RedisStreamApi.Consumer.Enteties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transaction {

    private String userid;
    private List<SellerProductAndQuantity> purchases;
    private Date dateOfTransaction;
    private double totalAmount;

    public Transaction(String userid, CartJob cartJob){

        this.userid = userid;

        this.dateOfTransaction = Date.from(Instant.now());

        List<SellerProductAndQuantity> tempPurchases = new ArrayList<>();

        this.totalAmount = 0.0;

        cartJob.getProductsAndQty().forEach((productid, integer) -> {

            Product product = cartJob.getProductIDProduct().get(productid);

            SellerProductAndQuantity sellerProductAndQuantity = new SellerProductAndQuantity(
                    productid , product, integer);

            tempPurchases.add(sellerProductAndQuantity);

            this.totalAmount += sellerProductAndQuantity.getTotal();
        });

        this.purchases = tempPurchases;

    }

    class SellerProductAndQuantity{

        private String seller;
        private Product product;
        private int qty;
        private double total;

        public SellerProductAndQuantity(String seller, Product product, int qty){

            this.seller = seller;
            this.product = product;
            this.qty = qty;

            this.total = qty * product.getPrice();

        }

        public double getTotal() {
            return total;
        }
    }

}
