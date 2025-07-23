

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnector {
    private static final String DB_PATH = "database/shop.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    // Actual working connection method
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("✅ Connected to SQLite at " + DB_PATH);
            return conn;
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
            return null;
        }
    }

    // Provide a consistent alias used throughout the app
    public static Connection getConnection() throws SQLException {
        return connect();
    }
}
