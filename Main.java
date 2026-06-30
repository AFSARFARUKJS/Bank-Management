package login;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ArrayList<User> users = new ArrayList<>();

        Customer customer1 = new Customer("cust1", "1234", 5000);
        Customer customer2 = new Customer("cust2", "5678", 3000);
        Employee employee = new Employee("emp1", "1111");
        Admin admin = new Admin("admin", "9999");

        users.add(customer1);
        users.add(customer2);
        users.add(employee);
        users.add(admin);

        LoginService loginService = new LoginService(users);
        BankService bankService = new BankService();

        while (true) {
            System.out.println("\n===== ONLINE BANKING MANAGEMENT SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 2) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Password: ");
            String password = sc.nextLine();

            User loggedUser = loginService.login(username, password);

            if (loggedUser == null) {
                continue;
            }

            if (loggedUser.getRole().equals("CUSTOMER")) {
                Customer customer = (Customer) loggedUser;

                while (true) {
                    System.out.println("\n--- CUSTOMER DASHBOARD ---");
                    System.out.println("1. View Balance");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Transfer");
                    System.out.println("5. Transactions");
                    System.out.println("6. Reset Password");
                    System.out.println("7. Logout");
                    System.out.print("Choice: ");

                    int c = sc.nextInt();

                    switch (c) {
                        case 1:
                            bankService.viewBalance(customer);
                            break;

                        case 2:
                            System.out.print("Enter amount: ");
                            double dep = sc.nextDouble();
                            bankService.deposit(customer, dep);
                            break;

                        case 3:
                            System.out.print("Enter amount: ");
                            double wd = sc.nextDouble();
                            bankService.withdraw(customer, wd);
                            break;

                        case 4:
                            System.out.print("Enter amount: ");
                            double amt = sc.nextDouble();

                            Customer receiver = customer1;
                            if (customer == customer1) {
                                receiver = customer2;
                            }

                            bankService.transfer(customer, receiver, amt);
                            break;

                        case 5:
                            bankService.showTransactions();
                            break;

                        case 6:
                            sc.nextLine();
                            System.out.print("Old password: ");
                            String oldPass = sc.nextLine();

                            System.out.print("New password: ");
                            String newPass = sc.nextLine();

                            System.out.print("Confirm password: ");
                            String confirm = sc.nextLine();

                            loginService.resetPassword(
                                    customer,
                                    oldPass,
                                    newPass,
                                    confirm
                            );
                            break;

                        case 7:
                            System.out.println("Logged out");
                            break;

                        default:
                            System.out.println("Invalid choice");
                    }

                    if (c == 7)
                        break;
                }
            }

            else if (loggedUser.getRole().equals("EMPLOYEE")) {
                System.out.println("\n--- EMPLOYEE DASHBOARD ---");
                System.out.println("Can manage customers/accounts");
            }

            else if (loggedUser.getRole().equals("ADMIN")) {
                System.out.println("\n--- ADMIN DASHBOARD ---");
                System.out.println("Can manage users/roles/settings");
            }

            else {
                loginService.validateRole(loggedUser.getRole());
            }
        }

        sc.close();
    }
}