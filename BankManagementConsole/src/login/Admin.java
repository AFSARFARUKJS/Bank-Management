package login;

public class Admin extends User {
    public Admin(int userId, String username, String passwordHash, String salt,
                 String firstName, String lastName, String email, String phone) {
        super(userId, username, passwordHash, salt, Role.ADMIN, firstName, lastName, email, phone);
    }
}
