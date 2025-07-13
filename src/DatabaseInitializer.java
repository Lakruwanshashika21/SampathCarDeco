import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String createItemsTable = """
                    CREATE TABLE IF NOT EXISTS items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        category TEXT,
                        price REAL NOT NULL,
                        quantity INTEGER NOT NULL
                    );
                """;

        try (Connection conn = DatabaseConnector.connect();
                Statement stmt = conn.createStatement()) {

            stmt.execute(createItemsTable);
            System.out.println("Tables initialized.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
