package login;

import java.util.ArrayList;

public class BankService {

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public void viewBalance(Customer customer) {
        System.out.println("Current Balance: " + customer.getBalance());
    }

    public void deposit(Customer customer, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        customer.deposit(amount);
        transactions.add(new Transaction(
                customer.getUsername(),
                "DEPOSIT",
                amount
        ));

        System.out.println("Deposit successful");
    }

    public void withdraw(Customer customer, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        boolean success = customer.withdraw(amount);

        if (success) {
            transactions.add(new Transaction(
                    customer.getUsername(),
                    "WITHDRAW",
                    amount
            ));
            System.out.println("Withdraw successful");
        } else {
            System.out.println("Insufficient balance");
        }
    }

    public void transfer(Customer sender, Customer receiver, double amount) {

        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        if (sender.withdraw(amount)) {
            receiver.deposit(amount);

            transactions.add(new Transaction(
                    sender.getUsername(),
                    "TRANSFER",
                    amount
            ));

            System.out.println("Transfer successful");
        } else {
            System.out.println("Insufficient balance");
        }
    }

    public void showTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found");
            return;
        }

        for (Transaction t : transactions) {
            t.display();
        }
    }
}