# Online Banking Management System - Console Java

Console-based Java banking project using collections only. No database is used.

## Run

```powershell
javac -d bin src\login\*.java
java -cp bin login.Main
```

## Demo Users

| Role | Username | Password |
| --- | --- | --- |
| Admin | admin | Admin@123 |
| Employee | employee1 | Emp@1234 |
| Customer | johncustomer | John@123 |

## Files

```text
src/login/Account.java
src/login/Admin.java
src/login/BankService.java
src/login/Customer.java
src/login/Employee.java
src/login/LoggerService.java
src/login/LoginService.java
src/login/Main.java
src/login/Transaction.java
src/login/User.java
```

The API requirements are implemented as Java service methods returning string responses, because the requested submission is console Java without database.
