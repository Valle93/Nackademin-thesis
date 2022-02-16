package Entities;

public enum Brand {

    PONY("Pony", 1), TAMTUNG("Tamtung", 2), JOHANSSON("Johansson", 3),
    GN("GN", 4), TOSHINBA("Toshinba", 5);

    String name;
    int id;

    Brand(String name, int id){

        this.name = name;
        this.id = id;
    }

    public static Brand brandFromStringInt(String id){

        int int_id = Integer.valueOf(id);

        switch(int_id){

            case 1:

                return Brand.PONY;

            case 2:

                return Brand.TAMTUNG;

            case 3:

                return Brand.JOHANSSON;

            case 4:

                return Brand.GN;

            case 5:

                return Brand.TOSHINBA;

        }

        return Brand.PONY;

    }

    public int getId(){

        return id;
    }

    public String getName() {
        return name;
    }

}
