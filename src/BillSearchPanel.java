import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class BillSearchPanel extends JPanel {
    private JTextField searchField;
    private JButton openButton;
    private JLabel statusLabel;
    private File billFolder;

    // Constructor accepts the folder where bills are saved
    public BillSearchPanel(File billFolder) {
        this.billFolder = billFolder;

        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        openButton = new JButton("🔍 Open Bill");
        statusLabel = new JLabel("Enter or scan barcode ID (e.g., JOB_1721900290)");

        top.add(new JLabel("Bill ID:"));
        top.add(searchField);
        top.add(openButton);

        add(top, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        openButton.addActionListener(e -> openBillFile());
        searchField.addActionListener(e -> openBillFile()); // Handle Enter key
    }

    private void openBillFile() {
        String billId = searchField.getText().trim();
        if (billId.isEmpty()) {
            statusLabel.setText("❌ Bill ID required.");
            return;
        }

        if (billFolder == null || !billFolder.exists() || !billFolder.isDirectory()) {
            statusLabel.setText("❌ Bill folder not set or invalid.");
            return;
        }

        File billFile = new File(billFolder, billId + ".pdf");
        if (billFile.exists()) {
            try {
                Desktop.getDesktop().open(billFile);
                statusLabel.setText("✅ Opened: " + billFile.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Failed to open bill.");
            }
        } else {
            statusLabel.setText("❌ File not found: " + billFile.getAbsolutePath());
        }
    }
}
