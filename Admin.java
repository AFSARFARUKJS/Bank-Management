package login;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }
}