package Entities;

public enum ProductCathegory {

    ELECTRONICS("Electronics"), SPORTS("Sports"), CLOTHES("Clothes"),
    LITERATURE("Literature");

    private final String cathegory;

    ProductCathegory(String value){

        cathegory = value;
    }

    public String getString(){

        return cathegory;
    }
}
