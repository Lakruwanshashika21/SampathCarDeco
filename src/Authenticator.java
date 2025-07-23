import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Authenticator {
     public static User authenticate(String email, String password) {
        
       String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND active = 1";

    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, email);
        stmt.setString(2, password);

        System.out.println("Authenticating: " + email + " / " + password); // debug

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String role = rs.getString("role");
            System.out.println("Login successful for: " + email + ", role: " + role); // debug
            return new User(id, email, role);
        } else {
            System.out.println("Login failed: no user found."); // debug
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
    }
}

