package login;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final int transactionId;
    private final Integer fromAccount;
    private final Integer toAccount;
    private final BigDecimal amount;
    private final TransactionType transactionType;
    private final TransactionStatus status;
    private final String description;
    private final int initiatedBy;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Transaction(int transactionId, Integer fromAccount, Integer toAccount, BigDecimal amount,
                       TransactionType transactionType, TransactionStatus status, String description, int initiatedBy) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.description = description;
        this.initiatedBy = initiatedBy;
    }

    public int getTransactionId() { return transactionId; }
    public Integer getFromAccount() { return fromAccount; }
    public Integer getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public int getInitiatedBy() { return initiatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
