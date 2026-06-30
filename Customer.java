package login;

public class Customer extends User {
    private double balance;

    public Customer(String username, String password, double balance) {
        super(username, password, "CUSTOMER");
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}