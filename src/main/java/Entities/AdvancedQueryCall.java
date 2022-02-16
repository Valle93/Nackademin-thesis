package Entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdvancedQueryCall {

    private int price_min;
    private int price_max;
    private Location[] locations;
    private Brand[] brands;
    private String id;

    public AdvancedQueryCall(int price_min, int price_max, Location[] locations, Brand[] brands){

        this.price_min = price_min;
        this.price_max = price_max;
        this.locations = locations;
        this.brands = brands;

    }

    public String[] getBrandsWithPlaceHolder(){

        String[] brandsString = new String[brands.length + 1];

        brandsString[0] = "placeHolder";

        for (int i = 1; i <= brands.length ; i++) {

            brandsString[i] = "brand:" + brands[i - 1].getName();
        }

        return brandsString;

    }

    public String[] getLocationsWithPlaceHolder(){

        String[] locationString = new String[locations.length + 1];

        locationString[0] = "placeHolder";

        for (int i = 1; i <= locations.length; i++) {


            locationString[i] = "location:" + locations[i - 1].getName();
        }

        return locationString;

    }

    public String getLuaScript(){

        String luaScript = "redis.call('zunionstore', 'query:locations', " +
                (this.getLocations().length + 1) + ", ";

        for (String s: this.getLocationsWithPlaceHolder()) {

            luaScript += "'" + s + "'" + ", ";
        }

        luaScript = luaScript.substring(0, luaScript.length() - 2) + ");\n\n";

        luaScript += "redis.call('zunionstore', 'query:brands', " + (this.getBrands().length + 1) + ", ";

        for (String s : this.getBrandsWithPlaceHolder()) {

            luaScript += "'" + s + "'" + ", ";
        }

        luaScript = luaScript.substring(0, luaScript.length() - 2) + ");\n\n";

        luaScript += "redis.call('zinterstore', 'query:brands_locations', 2, 'query:locations', 'query:brands');\n\n";

        luaScript += "redis.call('zinterstore', 'query:all-products', 2, 'SORTEDSET_products', 'query:brands_locations');\n\n";

        luaScript += "productids = redis.call('zrangeByScore', 'query:all-products', "
                        + this.getPrice_min() + ", " + this.getPrice_max() + ");\n\n";

        luaScript += "responses[#responses + 1] = '" + this.getId() + "';\n\n";

        luaScript += "for i = 1, table.maxn(productids), 1\n";

        luaScript += "do\n";

        luaScript += "responses[#responses + 1] = redis.call('GET', productids[i]);\n";

        luaScript += "end\n\n";

        return  luaScript;
    }


}
