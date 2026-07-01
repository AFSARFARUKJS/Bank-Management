package login;

public class Employee extends User {
    public Employee(int userId, String username, String passwordHash, String salt,
                    String firstName, String lastName, String email, String phone) {
        super(userId, username, passwordHash, salt, Role.EMPLOYEE, firstName, lastName, email, phone);
    }
}
