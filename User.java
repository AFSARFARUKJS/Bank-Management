package login;

import java.time.LocalDateTime;

public class User {
    protected String username;
    protected String password;
    protected String role;
    protected LocalDateTime lastLogin;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
}