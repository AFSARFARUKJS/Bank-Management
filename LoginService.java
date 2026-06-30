package login;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class LoginService {

    private ArrayList<User> users;

    public LoginService(ArrayList<User> users) {
        this.users = users;
    }

    public User login(String username, String password) {

        if (username == null || username.isEmpty()) {
            System.out.println("Username required");
            return null;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("Password required");
            return null;
        }

        for (User user : users) {
            if (user.getUsername().equals(username) &&
                user.getPassword().equals(password)) {

                user.setLastLogin(LocalDateTime.now());
                System.out.println("Login Successful");
                return user;
            }
        }

        System.out.println("Invalid credentials");
        LoggerService.log("Failed login attempt for username: " + username);
        return null;
    }

    public boolean resetPassword(User user, String oldPass,
                                 String newPass, String confirmPass) {

        if (!user.getPassword().equals(oldPass)) {
            System.out.println("Old password incorrect");
            return false;
        }

        if (oldPass.equals(newPass)) {
            System.out.println("New password must be different");
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            System.out.println("Confirm password mismatch");
            return false;
        }

        user.setPassword(newPass);
        LoggerService.log("Password changed for user: " + user.getUsername());
        System.out.println("Password changed successfully");
        return true;
    }

    public void validateRole(String role) {
        if (!(role.equals("CUSTOMER") ||
              role.equals("EMPLOYEE") ||
              role.equals("ADMIN"))) {

            LoggerService.log("Unauthorized access attempt: " + role);
        }
    }
}