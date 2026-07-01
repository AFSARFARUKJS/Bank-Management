package login;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final BankService BANK = new BankService();
    private static final LoginService LOGIN = BANK.getLoginService();

    public static void main(String[] args) {
        while (true) {
            title("ONLINE BANKING MANAGEMENT SYSTEM");
            System.out.println("1. Login");
            System.out.println("2. Reset Password");
            System.out.println("3. Exit");
            int choice = readInt("Choose option: ");
            if (choice == 1) login();
            else if (choice == 2) resetPassword();
            else if (choice == 3) { System.out.println("Thank you. Application closed."); return; }
            else System.out.println("Invalid option.");
            pause();
        }
    }

    private static void login() {
        User user = LOGIN.login(readRequired("Username: "), readRequired("Password: "));
        if (user == null) return;
        if (user.getRole() == Role.ADMIN) adminDashboard(user);
        else if (user.getRole() == Role.EMPLOYEE) employeeDashboard(user);
        else customerDashboard(user);
    }

    private static void resetPassword() {
        System.out.println(LOGIN.resetPassword(readRequired("Username: "), readRequired("Old password: "), readRequired("New password: "), readRequired("Confirm password: ")));
    }

    private static void adminDashboard(User user) {
        while (true) {
            title("ADMIN DASHBOARD");
            System.out.println("1. View all users");
            System.out.println("2. View audit logs");
            System.out.println("3. Create employee");
            System.out.println("4. Customer/account management");
            System.out.println("5. Logout");
            int c = readInt("Choose option: ");
            if (c == 1) LOGIN.printUsers();
            else if (c == 2) BANK.getLogger().printAuditLogs();
            else if (c == 3) createEmployee(user);
            else if (c == 4) employeeDashboard(user);
            else if (c == 5) return;
            else System.out.println("Invalid option.");
            pause();
        }
    }

    private static void employeeDashboard(User user) {
        while (true) {
            title(user.getRole() + " DASHBOARD");
            System.out.println("1. Register customer");
            System.out.println("2. View/Edit customer");
            System.out.println("3. Search/filter customers");
            System.out.println("4. Soft delete customer");
            System.out.println("5. Create bank account");
            System.out.println("6. Freeze/close account");
            System.out.println("7. Deposit");
            System.out.println("8. Withdraw");
            System.out.println("9. View all accounts");
            System.out.println("10. Back/Logout");
            int c = readInt("Choose option: ");
            if (c == 1) registerCustomer(user);
            else if (c == 2) viewEditCustomer(user);
            else if (c == 3) searchCustomers();
            else if (c == 4) System.out.println(BANK.softDeleteCustomer(user, readInt("Customer ID: ")));
            else if (c == 5) createAccount(user);
            else if (c == 6) updateStatus(user);
            else if (c == 7) System.out.println(BANK.deposit(user, readInt("To account: "), readAmount("Amount: ")));
            else if (c == 8) System.out.println(BANK.withdraw(user, readInt("From account: "), readAmount("Amount: ")));
            else if (c == 9) BANK.printAllAccounts();
            else if (c == 10) return;
            else System.out.println("Invalid option.");
            pause();
        }
    }

    private static void customerDashboard(User user) {
        while (true) {
            title("CUSTOMER DASHBOARD");
            System.out.println("1. View my accounts");
            System.out.println("2. Transfer funds");
            System.out.println("3. Transaction history");
            System.out.println("4. Request freeze/close account");
            System.out.println("5. Logout");
            int c = readInt("Choose option: ");
            if (c == 1) BANK.printCustomerAccounts(user);
            else if (c == 2) transfer(user);
            else if (c == 3) transactionHistory(user);
            else if (c == 4) updateStatus(user);
            else if (c == 5) return;
            else System.out.println("Invalid option.");
            pause();
        }
    }

    private static void createEmployee(User actor) {
        String username = readRequired("Username: "), password = readRequired("Password: "), first = readRequired("First name: "), last = readRequired("Last name: "), email = readRequired("Email: "), phone = readRequired("Mobile: ");
        if (!LoginService.validUsername(username) || !LoginService.validPassword(password) || !LoginService.validEmail(email) || !LoginService.validMobile(phone)) { System.out.println("Invalid employee details."); return; }
        if (LOGIN.duplicateUserContact(username, email, phone)) { System.out.println("Duplicate username/email/mobile."); return; }
        User employee = LOGIN.createUser(username, password, Role.EMPLOYEE, first, last, email, phone);
        BANK.getLogger().audit(actor, "CREATE", "users", employee.getUserId());
        System.out.println("Employee created. User ID: " + employee.getUserId());
    }

    private static void registerCustomer(User actor) {
        System.out.println(BANK.createCustomer(actor, readRequired("Username: "), readRequired("Password: "), readRequired("First name: "), readRequired("Last name: "), readRequired("Email: "), readRequired("Mobile: "), readRequired("SSN ID: "), readRequired("Address: "), readRequired("City: "), readRequired("State: "), readRequired("Postal code: "), readYesNo("KYC verified? y/n: ")));
    }

    private static void viewEditCustomer(User actor) {
        int id = readInt("Customer ID: ");
        Optional<Customer> customer = BANK.findCustomer(id);
        if (customer.isEmpty()) { System.out.println("No records found."); return; }
        BANK.printCustomer(customer.get());
        if (!readYesNo("Edit customer? y/n: ")) return;
        System.out.println(BANK.updateCustomer(actor, id, readRequired("First name: "), readRequired("Last name: "), readRequired("Email: "), readRequired("Mobile: "), readRequired("Address: "), readRequired("City: "), readRequired("State: "), readRequired("Postal code: "), readYesNo("KYC verified? y/n: ")));
    }

    private static void searchCustomers() { BANK.searchCustomers(readOptional("Name contains: "), readOptional("SSN exact: "), readOptional("Status active/inactive/all: "), readOptional("City: ")); }
    private static void createAccount(User actor) {
        int customerId = readInt("Customer ID: ");
        AccountType type = readAccountType();
        BigDecimal interest = type == AccountType.FIXED_DEPOSIT ? readAmount("Interest rate: ") : BigDecimal.ZERO;
        System.out.println(BANK.createAccount(actor, customerId, type, readAmount("Initial balance: "), interest));
    }
    private static void updateStatus(User actor) {
        int account = readInt("Account number: ");
        int c = readInt("1. Freeze  2. Close/Inactive: ");
        System.out.println(BANK.updateAccountStatus(actor, account, c == 1 ? AccountStatus.FROZEN : AccountStatus.INACTIVE));
    }
    private static void transfer(User actor) {
        int from = readInt("From account: "), to = readInt("To account: ");
        BigDecimal amount = readAmount("Amount: ");
        String type = readRequired("Transaction type (TRANSFER): ");
        if (!type.equalsIgnoreCase("TRANSFER")) { System.out.println("Only TRANSFER transaction type is allowed on this page."); return; }
        System.out.println(BANK.transfer(actor, from, to, amount));
    }
    private static void transactionHistory(User user) {
        LocalDate date = null;
        String input = readOptional("Date yyyy-MM-dd blank to skip: ");
        if (!input.isBlank()) {
            try { date = LocalDate.parse(input); } catch (DateTimeParseException e) { System.out.println("Invalid date."); return; }
        }
        BANK.transactionHistory(user, date, readOptionalAmount("Min amount blank to skip: "), readOptionalAmount("Max amount blank to skip: "));
    }
    private static AccountType readAccountType() {
        int c = readInt("1. Savings  2. Current  3. Fixed Deposit: ");
        if (c == 2) return AccountType.CURRENT;
        if (c == 3) return AccountType.FIXED_DEPOSIT;
        return AccountType.SAVINGS;
    }
    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = SCANNER.nextLine().trim();
            if (!v.isBlank()) return v;
            System.out.println("This field is required.");
        }
    }
    private static String readOptional(String prompt) { System.out.print(prompt); return SCANNER.nextLine().trim(); }
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(SCANNER.nextLine().trim()); } catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }
    private static BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal value = new BigDecimal(SCANNER.nextLine().trim());
                if (value.compareTo(BigDecimal.ZERO) > 0) return BankService.scale(value);
            } catch (NumberFormatException e) { System.out.println("Enter a valid amount."); }
        }
    }
    private static BigDecimal readOptionalAmount(String prompt) {
        String input = readOptional(prompt);
        if (input.isBlank()) return null;
        try { return BankService.scale(new BigDecimal(input)); } catch (NumberFormatException e) { System.out.println("Invalid amount ignored."); return null; }
    }
    private static boolean readYesNo(String prompt) {
        while (true) {
            String input = readRequired(prompt);
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) return true;
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) return false;
            System.out.println("Enter y or n.");
        }
    }
    private static void pause() { System.out.print("\nPress Enter to continue..."); SCANNER.nextLine(); }
    private static void title(String text) { System.out.println("\n=================================================="); System.out.println(text); System.out.println("=================================================="); }
}
