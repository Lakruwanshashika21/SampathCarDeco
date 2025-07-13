import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public static boolean addItem(Item item) {
        String sql = "INSERT INTO items (name, category, price, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setInt(4, item.getQuantity());

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.out.println("Add item failed: " + e.getMessage());
            return false;
        }
    }

    public static List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnector.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"));
                list.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Get all items failed: " + e.getMessage());
        }
        return list;
    }
}
