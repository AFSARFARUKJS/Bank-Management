package login;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoggerService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<String> auditLogs = new ArrayList<>();
    private long nextLogId = 1;

    public void audit(User user, String action, String tableName, int recordId) {
        String line = "Log ID: " + nextLogId++ + " | User ID: " + (user == null ? 0 : user.getUserId())
                + " | Action: " + action + " | Table: " + tableName + " | Record ID: " + recordId
                + " | Modified By: " + (user == null ? "SYSTEM" : user.getUsername())
                + " | Timestamp: " + LocalDateTime.now().format(FMT);
        auditLogs.add(line);
        append("audit_logs.txt", line);
    }

    public void unauthorized(String message) {
        String line = "UNAUTHORIZED | " + message + " | Timestamp: " + LocalDateTime.now().format(FMT);
        System.out.println(line);
        append("unauthorized_access.txt", line);
    }

    public List<String> getAuditLogs() { return auditLogs; }

    public void printAuditLogs() {
        if (auditLogs.isEmpty()) System.out.println("No audit logs found.");
        else auditLogs.forEach(System.out::println);
    }

    private void append(String fileName, String line) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Could not write log file: " + e.getMessage());
        }
    }
}
