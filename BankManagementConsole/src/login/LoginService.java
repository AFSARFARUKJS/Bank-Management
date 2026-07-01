package login;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoginService {
    private static final String SECRET = "ConsoleBankSecret";
    private final List<User> users;
    private final LoggerService logger;
    private int nextUserId = 1;

    public LoginService(List<User> users, LoggerService logger) {
        this.users = users;
        this.logger = logger;
    }

    public User createUser(String username, String password, Role role, String firstName, String lastName, String email, String phone) {
        String salt = newSalt();
        String hash = hash(password, salt);
        User user = role == Role.ADMIN ? new Admin(nextUserId++, username, hash, salt, firstName, lastName, email, phone)
                : role == Role.EMPLOYEE ? new Employee(nextUserId++, username, hash, salt, firstName, lastName, email, phone)
                : new User(nextUserId++, username, hash, salt, Role.CUSTOMER, firstName, lastName, email, phone);
        users.add(user);
        return user;
    }

    public User login(String username, String password) {
        String result = validateLoginApi(username, password);
        System.out.println(result);
        if (!result.equals("Login successful.")) return null;
        return findByUsername(username).orElse(null);
    }

    public String validateLoginApi(String username, String password) {
        if (username == null || username.isBlank()) return "Login failed: username is required.";
        if (password == null || password.isBlank()) return "Login failed: password is required.";
        Optional<User> user = findByUsername(username);
        if (user.isEmpty()) return "Login failed: user does not exist.";
        if (!user.get().isActive()) return "Login failed: user is inactive.";
        if (!hash(password, user.get().getSalt()).equals(user.get().getPasswordHash())) return "Login failed: invalid password.";
        user.get().setLastLogin(LocalDateTime.now());
        user.get().touch();
        return "Login successful.";
    }

    public String resetPassword(String username, String oldPassword, String newPassword, String confirmPassword) {
        Optional<User> user = findByUsername(username);
        if (user.isEmpty()) return "Password reset failed: user not found.";
        if (!hash(oldPassword, user.get().getSalt()).equals(user.get().getPasswordHash())) return "Password reset failed: old password is incorrect.";
        if (oldPassword.equals(newPassword)) return "Password reset failed: new password must be different from old password.";
        if (!newPassword.equals(confirmPassword)) return "Password reset failed: confirm password must match.";
        if (!validPassword(newPassword)) return "Password reset failed: password needs 8 chars, 1 number, and 1 special character.";
        String salt = newSalt();
        user.get().setSalt(salt);
        user.get().setPasswordHash(hash(newPassword, salt));
        user.get().touch();
        logger.audit(user.get(), "UPDATE", "users", user.get().getUserId());
        return "Password reset successful.";
    }

    public boolean hasRole(User user, Role role) {
        boolean ok = user != null && user.getRole() == role;
        if (!ok) logger.unauthorized("Invalid role access attempt.");
        return ok;
    }

    public String roleBasedAccessApi(Role role) {
        if (role == null) {
            logger.unauthorized("Invalid role requested.");
            return "Access denied: invalid role.";
        }
        if (role == Role.ADMIN) return "ADMIN permissions: manage users, roles, audit logs.";
        if (role == Role.EMPLOYEE) return "EMPLOYEE permissions: manage customers and accounts.";
        return "CUSTOMER permissions: view own accounts, transactions, transfer funds.";
    }

    public Optional<User> findById(int userId) { return users.stream().filter(u -> u.getUserId() == userId).findFirst(); }
    public Optional<User> findByUsername(String username) { return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst(); }
    public boolean duplicateUserContact(String username, String email, String phone) {
        return users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username) || u.getEmail().equalsIgnoreCase(email) || u.getPhone().equals(phone));
    }
    public void printUsers() {
        users.forEach(u -> System.out.println("ID: " + u.getUserId() + " | Username: " + u.getUsername() + " | Role: " + u.getRole()
                + " | Name: " + u.getFullName() + " | Email: " + u.getEmail() + " | Active: " + u.isActive()));
    }

    public static boolean validUsername(String username) { return username != null && username.length() <= 50 && username.matches(".*[A-Za-z].*") && username.matches("[A-Za-z0-9_]+"); }
    public static boolean validEmail(String email) { return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"); }
    public static boolean validMobile(String mobile) { return mobile != null && mobile.matches("\\d{10}"); }
    public static boolean validPassword(String password) { return password != null && password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[^A-Za-z0-9].*"); }
    public static boolean validSsn(String ssn) { return ssn != null && ssn.matches("\\d{12}"); }

    public static String encrypt(String text) {
        byte[] input = text.getBytes(StandardCharsets.UTF_8), key = SECRET.getBytes(StandardCharsets.UTF_8), output = new byte[input.length];
        for (int i = 0; i < input.length; i++) output[i] = (byte) (input[i] ^ key[i % key.length]);
        return Base64.getEncoder().encodeToString(output);
    }

    public static String decrypt(String encrypted) {
        byte[] input = Base64.getDecoder().decode(encrypted), key = SECRET.getBytes(StandardCharsets.UTF_8), output = new byte[input.length];
        for (int i = 0; i < input.length; i++) output[i] = (byte) (input[i] ^ key[i % key.length]);
        return new String(output, StandardCharsets.UTF_8);
    }

    private static String newSalt() { return UUID.randomUUID().toString().replace("-", ""); }
    private static String hash(String value, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) builder.append(String.format("%02x", b));
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
