package login;

import java.time.LocalDateTime;

public class Customer {
    private final int customerId;
    private final int userId;
    private final String encryptedSsn;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private boolean verified;
    private boolean active = true;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime modifiedAt = LocalDateTime.now();

    public Customer(int customerId, int userId, String encryptedSsn, String address,
                    String city, String state, String postalCode, boolean verified) {
        this.customerId = customerId;
        this.userId = userId;
        this.encryptedSsn = encryptedSsn;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.verified = verified;
    }

    public int getCustomerId() { return customerId; }
    public int getUserId() { return userId; }
    public String getEncryptedSsn() { return encryptedSsn; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public boolean isVerified() { return verified; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setAddress(String address) { this.address = address; touch(); }
    public void setCity(String city) { this.city = city; touch(); }
    public void setState(String state) { this.state = state; touch(); }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; touch(); }
    public void setVerified(boolean verified) { this.verified = verified; touch(); }
    public void setActive(boolean active) { this.active = active; touch(); }
    public void touch() { this.modifiedAt = LocalDateTime.now(); }
}
