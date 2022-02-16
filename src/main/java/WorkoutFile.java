import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class WorkoutFile {

    public static void main(String[] args) {

        Product product = new Product("Pencil", 1, "Pyrus", new int[]{1,2});

        Gson gson = new Gson();

        System.out.println("product json : " + gson.toJson(product));

    }

    @Getter
    @Setter
    @AllArgsConstructor
    static
    class Product {

        String name;
        int price;
        String brand;
        int[] locations;
    }

}
