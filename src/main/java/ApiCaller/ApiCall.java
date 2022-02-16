package ApiCaller;

import Entities.AdvancedQueryCall;
import Entities.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ApiCall {

    private Call callType;
    private long timeInMillis;
    List<Product> products;
    private AdvancedQueryCall advancedQueryCall;
    private String apiCall_url;

}
