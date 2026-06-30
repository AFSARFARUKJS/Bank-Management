package login;

import java.time.LocalDateTime;

public class Transaction {
    private String username;
    private String type;
    private double amount;
    private LocalDateTime timestamp;

    public Transaction(String username, String type, double amount) {
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public void display() {
        System.out.println(
                "User: " + username +
                " | Type: " + type +
                " | Amount: " + amount +
                " | Time: " + timestamp
        );
    }
}