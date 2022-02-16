package Services.DynamicLua;

import Entities.AdvancedQueryCall;

import java.util.UUID;

public class ApiCallRequest {

    // CRUD

    private String callType;

    private String callid;

    private String productJSON;

    private String productid;

    private String luaScript;

    private int price;

    private int start;

    private int end;

    public ApiCallRequest(AdvancedQueryCall advQuery){


    }

    public ApiCallRequest(String callType, String productJSON, String productid, int start, int end, int price){

        this.callid = key("call", generateId());
        this.callType = callType;
        this.productJSON = productJSON;
        this.productid = productid;
        this.callid = callid;
        this.start = start;
        this.end = end;
        this.price = price;

        switch (callType) {

            case "AdvQuery":



                break;

            case "post":

            case "update":

                luaScript = "redis.call('SET', KEYS[keyvalue], ARGV[argvalue]); \n\n";

                luaScript += "redis.call('zadd', KEYS[1], ARGV[argvalue + 1], KEYS[keyvalue]);\n\n";

                luaScript += "keyvalue = keyvalue + 1;\n\n";

                luaScript += "argvalue = argvalue + 2;\n\n";

                this.callType = "post";

                this.productJSON = productJSON;

                break;

            case "productsPriceRange":

                // #responses = table.size

                luaScript = "responses[#responses + 1] = ARGV[argvalue + 2];\n\n";

                luaScript += "local productids = redis.call('zrangebyscore', KEYS[1], " +
                        "ARGV[argvalue], ARGV[argvalue + 1]);\n\n";

                luaScript += "for i = 1, table.maxn(productids), 1\n";

                luaScript += "do\n";

                luaScript += "responses[#responses + 1] = redis.call('GET', productids[i]);\n";

                luaScript += "end\n\n";

                luaScript += "argvalue = argvalue + 3;\n\n";

                this.callid = callid;

                this.callType = "productsPriceRange";

                this.start = start;

                this.end = end;

                break;

            case "delete":

                luaScript = "redis.call('DEL', KEYS[keyvalue]);\n\n";

                luaScript += "redis.call('ZREM', KEYS[1], KEYS[keyvalue]);\n\n";

                luaScript += "keyusage = keyusage + 1;\n\n";

                break;

            case "get":

                luaScript = "responses[#responses + 1] = ARGV[argvalue];;\n\n";

                luaScript += "local productJSON = redis.call('get', KEYS[keyvalue]);\n\n";

                luaScript += "responses[#responses + 1] = productJSON;\n\n";

                luaScript += "keyvalue = keyvalue + 1;\n\n";

                luaScript += "argvalue = argvalue + 1;\n\n";

                break;

        }

    }

    public String getLuaScript() {
        return luaScript;
    }

    public String getCallType() {
        return callType;
    }

    public String getProductid() {
        return productid;
    }

    public String getCallid() {
        return callid;
    }

    public String getProductJSON() {
        return productJSON;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getPrice() {
        return price;
    }

    public static String key(String... parts) {
        return String.join(":", parts);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }


}
