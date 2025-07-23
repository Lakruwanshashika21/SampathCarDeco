import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminManagementPanel extends JPanel {
    private List<User> userList = new ArrayList<>();

    public AdminManagementPanel(Runnable onBack) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setBackground(Color.WHITE);

        // --- Title & Back Button ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("⬅ Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(220, 220, 220));
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Admin Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JTextField passwordField = new JTextField(20);
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"superadmin", "admin"});
        JButton addBtn = new JButton("Add Admin");

        addBtn.setBackground(new Color(59, 89, 152));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(roleBox, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Add Admin Logic ---
        addBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String role = (String) roleBox.getSelectedItem();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter email and password.");
                return;
            }

            try (Connection conn = DatabaseConnector.connect()) {
                String sql = "INSERT INTO users (email, password, role, active) VALUES (?, ?, ?, 1)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, role);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, role + " account created for: " + email);
                    emailField.setText("");
                    passwordField.setText("");
                    roleBox.setSelectedIndex(0);
                    loadAdmins();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add admin.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            
        });

        // --- Manage Admins Button ---
        JButton manageBtn = new JButton("Manage Admins");
        manageBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        manageBtn.setFocusPainted(false);
        manageBtn.setBackground(new Color(200, 200, 200));
        add(manageBtn, BorderLayout.SOUTH);

        manageBtn.addActionListener(e -> showAdminList());

        // Load current admins
        loadAdmins();
    }

    private void loadAdmins() {
        userList.clear();
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT id, email, role, active FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String role = rs.getString("role");
                boolean active = rs.getInt("active") == 1;
                userList.add(new User(id, email, active ? role : role + "_inactive"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load admins from DB.");
        }
    }

    private void showAdminList() {
        JFrame listFrame = new JFrame("Admin List");
        listFrame.setSize(400, 300);
        listFrame.setLocationRelativeTo(null);
        listFrame.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (User u : userList) {
            listModel.addElement(u.getEmail() + " | " + u.getRole());
        }

        JList<String> userListView = new JList<>(listModel);
        listFrame.add(new JScrollPane(userListView), BorderLayout.CENTER);

        JButton toggleBtn = new JButton("Activate/Deactivate");
        listFrame.add(toggleBtn, BorderLayout.SOUTH);

        toggleBtn.addActionListener(e -> {
            int index = userListView.getSelectedIndex();
            if (index >= 0) {
                User u = userList.get(index);
                boolean nowActive = !u.getRole().endsWith("_inactive");
                String newRole = nowActive ? u.getRole().replace("_inactive", "") : u.getRole() + "_inactive";
                int newActive = nowActive ? 0 : 1;

                try (Connection conn = DatabaseConnector.connect()) {
                    String sql = "UPDATE users SET active = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, newActive);
                    stmt.setInt(2, u.getId());
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        userList.set(index, new User(u.getId(), u.getEmail(), newRole));
                        listModel.set(index, u.getEmail() + " | " + newRole);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update status.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                }
            }
        });

        listFrame.setVisible(true);
    }
}
