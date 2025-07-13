import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.out.println("Failed to initialize FlatLaf.");
        }

        // Initialize DB tables
        DatabaseInitializer.initialize();

        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("Sampath Car Deco Shop System");
            mainFrame.setSize(400, 200);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setLocationRelativeTo(null);

            // Create Inventory Button
            JButton btnInventory = new JButton("Open Inventory");

            btnInventory.setFont(new Font("Arial", Font.BOLD, 16));
            btnInventory.setFocusPainted(false);
            btnInventory.setPreferredSize(new Dimension(200, 40));

            btnInventory.addActionListener(e -> {
                InventoryFrame inventoryFrame = new InventoryFrame();
                inventoryFrame.setVisible(true);
            });

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.add(btnInventory);

            mainFrame.add(panel);
            mainFrame.setVisible(true);

        });
    }
}
