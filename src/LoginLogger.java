import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginLogger {
    public static void log(int userId, String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("UserID: " + userId + " | Action: " + action + " | Time: " + timestamp);
    }
}
