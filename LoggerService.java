package login;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoggerService {

    public static void log(String message) {
        String finalMessage = LocalDateTime.now() + " - " + message;

        System.out.println(finalMessage);

        try {
            FileWriter fw = new FileWriter("logs.txt", true);
            fw.write(finalMessage + "\n");
            fw.close();
        } catch (IOException e) {
            System.out.println("Error writing log file");
        }
    }
}