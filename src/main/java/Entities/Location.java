package Entities;

public enum Location {

    UPTOWN("Uptown", 1), WESTSIDE("Westside", 2), EASTTOWN("Easttown", 3),
    DOWNTOWN("Downtown", 4), MIDTOWN("Midtown", 5);

    private String name;
    public int id;

    Location(String name, int id) {

        this.name = name;
        this.id = id;
    }

    public static Location locationFromStringInt(String id){

        int int_id = Integer.valueOf(id);

        switch(int_id){

            case 1:

                return Location.UPTOWN;

            case 2:

                return Location.WESTSIDE;

            case 3:

                return Location.EASTTOWN;

            case 4:

                return Location.DOWNTOWN;

            case 5:

                return Location.MIDTOWN;

        }

        return Location.UPTOWN;

    }

    public void setID(int id){

        this.id = id;
    }

    public int getID(){

        return id;
    }

    public String getName() {
        return name;
    }

}