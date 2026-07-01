package login;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private final int accountNumber;
    private final int customerId;
    private final AccountType accountType;
    private BigDecimal balance;
    private final BigDecimal minBalance;
    private final BigDecimal interestRate;
    private final LocalDateTime openingDate = LocalDateTime.now();
    private LocalDateTime modifiedAt = LocalDateTime.now();
    private AccountStatus status = AccountStatus.ACTIVE;

    public Account(int accountNumber, int customerId, AccountType accountType,
                   BigDecimal balance, BigDecimal minBalance, BigDecimal interestRate) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.minBalance = minBalance;
        this.interestRate = interestRate;
    }

    public int getAccountNumber() { return accountNumber; }
    public int getCustomerId() { return customerId; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getMinBalance() { return minBalance; }
    public BigDecimal getInterestRate() { return interestRate; }
    public LocalDateTime getOpeningDate() { return openingDate; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public AccountStatus getStatus() { return status; }
    public void setBalance(BigDecimal balance) { this.balance = balance; touch(); }
    public void setStatus(AccountStatus status) { this.status = status; touch(); }
    private void touch() { this.modifiedAt = LocalDateTime.now(); }
}
