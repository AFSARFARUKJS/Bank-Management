package login;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BankService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<User> users = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final LoggerService logger = new LoggerService();
    private final LoginService loginService = new LoginService(users, logger);
    private int nextCustomerId = 1001;
    private int nextAccountNumber = 10000001;
    private int nextTransactionId = 1;

    public BankService() { seedData(); }
    public LoginService getLoginService() { return loginService; }
    public LoggerService getLogger() { return logger; }
    public List<Transaction> getTransactions() { return transactions; }

    private void seedData() {
        User admin = loginService.createUser("admin", "Admin@123", Role.ADMIN, "System", "Admin", "admin@bank.com", "9000000001");
        User employee = loginService.createUser("employee1", "Emp@1234", Role.EMPLOYEE, "Bank", "Employee", "employee@bank.com", "9000000002");
        createCustomer(employee, "johncustomer", "John@123", "John", "Mathew", "john@example.com", "9876543210",
                "123456789012", "12 Lake Street", "Chennai", "Tamil Nadu", "600001", true);
        createAccount(employee, 1001, AccountType.SAVINGS, new BigDecimal("5000"), BigDecimal.ZERO);
        logger.audit(admin, "CREATE", "seed_data", 1);
    }

    public String createCustomer(User actor, String username, String password, String firstName, String lastName,
                                 String email, String mobile, String ssn, String address, String city,
                                 String state, String postalCode, boolean verified) {
        if (!LoginService.validUsername(username)) return "Username must contain alphabets with 50 char max.";
        if (!LoginService.validEmail(email)) return "Invalid email format.";
        if (!LoginService.validMobile(mobile)) return "Mobile must be a 10-digit number.";
        if (!LoginService.validPassword(password)) return "Password must be 8 chars with 1 number and 1 special character.";
        if (!LoginService.validSsn(ssn)) return "SSN ID must be a 12-digit number.";
        if (loginService.duplicateUserContact(username, email, mobile)) return "Duplicate username, email, or mobile.";
        User user = loginService.createUser(username, password, Role.CUSTOMER, firstName, lastName, email, mobile);
        Customer customer = new Customer(nextCustomerId++, user.getUserId(), LoginService.encrypt(ssn), address, city, state, postalCode, verified);
        customers.add(customer);
        logger.audit(actor, "CREATE", "customers", customer.getCustomerId());
        return "Customer created successfully. Customer ID: " + customer.getCustomerId();
    }

    public String updateCustomer(User actor, int customerId, String firstName, String lastName, String email,
                                 String mobile, String address, String city, String state, String postalCode, boolean verified) {
        Optional<Customer> customer = findCustomer(customerId);
        if (customer.isEmpty()) return "Customer not found.";
        Optional<User> user = loginService.findById(customer.get().getUserId());
        if (user.isEmpty()) return "Linked user not found.";
        if (!LoginService.validEmail(email) || !LoginService.validMobile(mobile)) return "Invalid email or mobile.";
        boolean duplicate = users.stream().anyMatch(u -> u.getUserId() != user.get().getUserId() && (u.getEmail().equalsIgnoreCase(email) || u.getPhone().equals(mobile)));
        if (duplicate) return "Email or mobile already exists.";
        user.get().setFirstName(firstName);
        user.get().setLastName(lastName);
        user.get().setEmail(email);
        user.get().setPhone(mobile);
        user.get().touch();
        customer.get().setAddress(address);
        customer.get().setCity(city);
        customer.get().setState(state);
        customer.get().setPostalCode(postalCode);
        customer.get().setVerified(verified);
        logger.audit(actor, "UPDATE", "customers", customerId);
        return "Customer updated. Username, SSN, Customer ID and User ID were not modified.";
    }

    public String softDeleteCustomer(User actor, int customerId) {
        Optional<Customer> customer = findCustomer(customerId);
        if (customer.isEmpty()) return "Customer not found.";
        if (!customer.get().isActive()) return "Only active customers can be soft-deleted.";
        customer.get().setActive(false);
        loginService.findById(customer.get().getUserId()).ifPresent(u -> { u.setActive(false); u.touch(); });
        logger.audit(actor, "DELETE", "customers", customerId);
        return "Customer soft deleted. Status changed active -> inactive.";
    }

    public void searchCustomers(String name, String ssn, String status, String city) {
        List<Customer> result = new ArrayList<>(customers);
        if (!name.isBlank()) result = result.stream().filter(c -> loginService.findById(c.getUserId()).map(u -> u.getFullName().toLowerCase().contains(name.toLowerCase())).orElse(false)).collect(Collectors.toList());
        if (!ssn.isBlank()) result = result.stream().filter(c -> LoginService.decrypt(c.getEncryptedSsn()).equals(ssn)).collect(Collectors.toList());
        if (!status.isBlank() && !"all".equalsIgnoreCase(status)) {
            boolean active = "active".equalsIgnoreCase(status);
            result = result.stream().filter(c -> c.isActive() == active).collect(Collectors.toList());
        }
        if (!city.isBlank()) result = result.stream().filter(c -> c.getCity().equalsIgnoreCase(city)).collect(Collectors.toList());
        result.sort(Comparator.comparing(Customer::getCreatedAt).reversed());
        if (result.isEmpty()) System.out.println("No records found.");
        else result.forEach(this::printCustomer);
    }

    public String createAccount(User actor, int customerId, AccountType type, BigDecimal initialBalance, BigDecimal interestRate) {
        Optional<Customer> customer = findCustomer(customerId);
        if (customer.isEmpty() || !customer.get().isActive()) return "Active customer not found.";
        BigDecimal min = minimumBalance(type);
        if (initialBalance.compareTo(min) < 0) return "Minimum balance required: " + money(min);
        Account account = new Account(nextAccountNumber++, customerId, type, scale(initialBalance), min, scale(interestRate));
        accounts.add(account);
        logger.audit(actor, "CREATE", "accounts", account.getAccountNumber());
        return "Account created successfully. Account Number: " + account.getAccountNumber();
    }

    public String updateAccountStatus(User actor, int accountNumber, AccountStatus status) {
        Optional<Account> account = findAccount(accountNumber);
        if (account.isEmpty()) return "Account not found.";
        if (actor.getRole() == Role.CUSTOMER && !ownsAccount(actor, accountNumber)) {
            logger.unauthorized("Customer tried to update another account: " + actor.getUsername());
            return "Customers can update only their own accounts.";
        }
        if (status == AccountStatus.INACTIVE && hasPendingTransaction(accountNumber)) return "Cannot close account because pending transactions exist.";
        if (account.get().getBalance().compareTo(BigDecimal.ZERO) != 0) return "Balance must be zero before freezing/closing.";
        account.get().setStatus(status);
        logger.audit(actor, "UPDATE", "accounts", accountNumber);
        return "Account status updated to " + status + ".";
    }

    public String deposit(User actor, int toAccount, BigDecimal amount) {
        Optional<Account> account = findActiveAccount(toAccount);
        if (account.isEmpty()) return saveFailed(actor, null, toAccount, amount, TransactionType.DEPOSIT, "Deposit failed: account inactive/not found.");
        account.get().setBalance(scale(account.get().getBalance().add(amount)));
        Transaction t = addTransaction(actor, null, toAccount, amount, TransactionType.DEPOSIT, TransactionStatus.COMPLETED, "Deposit");
        logger.audit(actor, "CREATE", "transactions", t.getTransactionId());
        return "Deposit successful. Transaction ID: " + t.getTransactionId();
    }

    public String withdraw(User actor, int fromAccount, BigDecimal amount) {
        Optional<Account> account = findActiveAccount(fromAccount);
        if (account.isEmpty()) return saveFailed(actor, fromAccount, null, amount, TransactionType.WITHDRAWAL, "Withdrawal failed: account inactive/not found.");
        BigDecimal after = account.get().getBalance().subtract(amount);
        if (after.compareTo(account.get().getMinBalance()) < 0) return saveFailed(actor, fromAccount, null, amount, TransactionType.WITHDRAWAL, "Withdrawal failed: minimum balance not maintained.");
        account.get().setBalance(scale(after));
        Transaction t = addTransaction(actor, fromAccount, null, amount, TransactionType.WITHDRAWAL, TransactionStatus.COMPLETED, "Withdrawal");
        logger.audit(actor, "CREATE", "transactions", t.getTransactionId());
        return "Withdrawal successful. Transaction ID: " + t.getTransactionId();
    }

    public String transfer(User actor, int fromAccount, int toAccount, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("50000")) > 0) return saveFailed(actor, fromAccount, toAccount, amount, TransactionType.TRANSFER, "Transfer failed: limit is 50,000.");
        if (actor.getRole() == Role.CUSTOMER && !ownsAccount(actor, fromAccount)) {
            logger.unauthorized("Customer tried transfer from another account: " + actor.getUsername());
            return "Customers can transfer only from their own account.";
        }
        Optional<Account> from = findActiveAccount(fromAccount), to = findActiveAccount(toAccount);
        if (from.isEmpty() || to.isEmpty()) return saveFailed(actor, fromAccount, toAccount, amount, TransactionType.TRANSFER, "Transfer failed: active from/to account not found.");
        BigDecimal after = from.get().getBalance().subtract(amount);
        if (after.compareTo(from.get().getMinBalance()) < 0) return saveFailed(actor, fromAccount, toAccount, amount, TransactionType.TRANSFER, "Transfer failed: insufficient balance or minimum balance not maintained.");
        from.get().setBalance(scale(after));
        to.get().setBalance(scale(to.get().getBalance().add(amount)));
        Transaction t = addTransaction(actor, fromAccount, toAccount, amount, TransactionType.TRANSFER, TransactionStatus.COMPLETED, "Fund transfer");
        logger.audit(actor, "CREATE", "transactions", t.getTransactionId());
        return "Transfer successful. Transaction ID: " + t.getTransactionId();
    }

    public void printCustomerAccounts(User user) {
        List<Account> mine = visibleAccounts(user).stream().map(this::findAccount).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        if (mine.isEmpty()) System.out.println("No accounts found.");
        else mine.forEach(a -> System.out.println("Account: " + maskAccount(a.getAccountNumber()) + " | Type: " + a.getAccountType() + " | Balance: " + money(a.getBalance()) + " | Status: " + a.getStatus()));
    }

    public void printAllAccounts() {
        if (accounts.isEmpty()) System.out.println("No accounts found.");
        else accounts.forEach(a -> System.out.println("Account: " + a.getAccountNumber() + " | Customer ID: " + a.getCustomerId() + " | Type: " + a.getAccountType() + " | Balance: " + money(a.getBalance()) + " | Status: " + a.getStatus()));
    }

    public void transactionHistory(User user, LocalDate date, BigDecimal minAmount, BigDecimal maxAmount) {
        List<Integer> visible = visibleAccounts(user);
        List<Transaction> result = transactions.stream()
                .filter(t -> user.getRole() != Role.CUSTOMER || visible.contains(t.getFromAccount()) || visible.contains(t.getToAccount()))
                .filter(t -> date == null || t.getCreatedAt().toLocalDate().equals(date))
                .filter(t -> minAmount == null || t.getAmount().compareTo(minAmount) >= 0)
                .filter(t -> maxAmount == null || t.getAmount().compareTo(maxAmount) <= 0)
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed()).collect(Collectors.toList());
        if (result.isEmpty()) System.out.println("No transaction records found.");
        else result.forEach(t -> System.out.println("Txn ID: " + t.getTransactionId() + " | Type: " + t.getTransactionType()
                + " | From: " + (t.getFromAccount() == null ? "-" : maskAccount(t.getFromAccount()))
                + " | To: " + (t.getToAccount() == null ? "-" : maskAccount(t.getToAccount()))
                + " | Amount: " + money(t.getAmount()) + " | Status: " + t.getStatus()
                + " | Date: " + t.getCreatedAt().format(FMT)));
    }

    public void printCustomer(Customer c) {
        Optional<User> u = loginService.findById(c.getUserId());
        if (u.isEmpty()) return;
        String ssn = LoginService.decrypt(c.getEncryptedSsn());
        System.out.println("Customer ID: " + c.getCustomerId() + " | Username: " + u.get().getUsername()
                + " | Name: " + u.get().getFullName() + " | Email: " + u.get().getEmail()
                + " | Mobile: " + u.get().getPhone() + " | SSN: ********" + ssn.substring(8)
                + " | City: " + c.getCity() + " | Active: " + c.isActive());
    }

    public Optional<Customer> findCustomer(int customerId) { return customers.stream().filter(c -> c.getCustomerId() == customerId).findFirst(); }
    public boolean ownsAccount(User user, int accountNumber) {
        Optional<Customer> c = customers.stream().filter(x -> x.getUserId() == user.getUserId()).findFirst();
        return c.isPresent() && accounts.stream().anyMatch(a -> a.getCustomerId() == c.get().getCustomerId() && a.getAccountNumber() == accountNumber);
    }
    public static BigDecimal scale(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    public static String money(BigDecimal value) { return scale(value).toPlainString(); }
    public static String maskAccount(int accountNumber) { String s = String.valueOf(accountNumber); return "****" + s.substring(s.length() - 4); }
    public static BigDecimal minimumBalance(AccountType type) {
        if (type == AccountType.CURRENT) return new BigDecimal("5000.00");
        if (type == AccountType.SAVINGS) return new BigDecimal("500.00");
        return BigDecimal.ZERO;
    }
    private Optional<Account> findAccount(int accountNumber) { return accounts.stream().filter(a -> a.getAccountNumber() == accountNumber).findFirst(); }
    private Optional<Account> findActiveAccount(int accountNumber) { return accounts.stream().filter(a -> a.getAccountNumber() == accountNumber && a.getStatus() == AccountStatus.ACTIVE).findFirst(); }
    private boolean hasPendingTransaction(int accountNumber) {
        return transactions.stream().anyMatch(t -> t.getStatus() == TransactionStatus.PENDING && (Integer.valueOf(accountNumber).equals(t.getFromAccount()) || Integer.valueOf(accountNumber).equals(t.getToAccount())));
    }
    private String saveFailed(User actor, Integer from, Integer to, BigDecimal amount, TransactionType type, String message) {
        Transaction t = addTransaction(actor, from, to, amount, type, TransactionStatus.FAILED, message);
        logger.audit(actor, "CREATE", "transactions", t.getTransactionId());
        return message + " Failed Transaction ID: " + t.getTransactionId();
    }
    private Transaction addTransaction(User actor, Integer from, Integer to, BigDecimal amount, TransactionType type, TransactionStatus status, String description) {
        Transaction t = new Transaction(nextTransactionId++, from, to, scale(amount), type, status, description, actor.getUserId());
        transactions.add(t);
        return t;
    }
    private List<Integer> visibleAccounts(User user) {
        if (user.getRole() != Role.CUSTOMER) return accounts.stream().map(Account::getAccountNumber).collect(Collectors.toList());
        Optional<Customer> c = customers.stream().filter(x -> x.getUserId() == user.getUserId()).findFirst();
        if (c.isEmpty()) return new ArrayList<>();
        return accounts.stream().filter(a -> a.getCustomerId() == c.get().getCustomerId()).map(Account::getAccountNumber).collect(Collectors.toList());
    }
}
