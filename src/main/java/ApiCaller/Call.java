package ApiCaller;

public enum Call {

    GETBYPAGE("getByPage"), GETPAGEBYCATHEGORY("getByCategory"), GETBYID("getById"), AdvancedQuery("AdvQuery");

    private String call;

    Call(String call){

        this.call = call;
    }

    public String getCall(){

        return call;
    }

}
