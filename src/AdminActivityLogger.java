// AdminActivityLogger.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminActivityLogger {
    private Connection conn;

    public AdminActivityLogger(Connection conn) {
        this.conn = conn;
    }

    public void logActivity(String username, String action, String description) {
        String sql = "INSERT INTO admin_logs (username, action, description) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, action);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
