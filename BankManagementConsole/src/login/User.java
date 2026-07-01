package login;

import java.time.LocalDateTime;

enum Role { ADMIN, EMPLOYEE, CUSTOMER }
enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }
enum AccountStatus { ACTIVE, INACTIVE, FROZEN }
enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }
enum TransactionStatus { PENDING, COMPLETED, FAILED }

public class User {
    private final int userId;
    private final String username;
    private String passwordHash;
    private String salt;
    private final Role role;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean active = true;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime modifiedAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    public User(int userId, String username, String passwordHash, String salt, Role role,
                String firstName, String lastName, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public String getFullName() { return firstName + " " + lastName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActive(boolean active) { this.active = active; }
    public void touch() { this.modifiedAt = LocalDateTime.now(); }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
