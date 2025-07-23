import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.out.println("Failed to initialize FlatLaf.");
        }

        // Optional: Create DB tables here
        DatabaseInitializer.initialize(); // Implement if needed

        SwingUtilities.invokeLater(() -> {
            new LoginFrame();  // Starts the login screen
        });
    }
}
