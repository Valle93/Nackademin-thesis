package RedisStreamApi.Enteties;

import RedisStreamApi.PMain;
import Entities.RedisKey;

public class User implements Orderable {

    private String username;
    private String email;
    private String password;
    private String id;
    private Cart cart;

    public User(String email, String username, String password) {

        this.email = email;
        this.username = username;
        this.password = password;
        this.id = PMain.key(RedisKey.enumUser.singular(), PMain.generateId());
        this.cart = new Cart(this.id);
    }

    public Cart getCart() {
        return cart;
    }

    public void flushCart() {

        this.cart = new Cart(id);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getid() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
