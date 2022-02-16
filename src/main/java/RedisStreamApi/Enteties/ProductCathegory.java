package RedisStreamApi.Enteties;

public enum ProductCathegory {

    ELECTRONICS("Electronics"), SPORTS("Sports"), CLOTHES("Clothes"),
    LITERATURE("Literature");

    private final String cathegory;

    public static ProductCathegory electronics = ProductCathegory.ELECTRONICS;
    public static ProductCathegory sports = ProductCathegory.SPORTS;
    public static ProductCathegory clothes = ProductCathegory.CLOTHES;

    ProductCathegory(String value){

        cathegory = value;
    }

    public String getString(){

        return cathegory;
    }
}
