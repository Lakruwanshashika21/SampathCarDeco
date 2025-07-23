import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerPanel extends JPanel {
    private JTextField nameField, phoneField, emailField, addressField, vehicleField, searchField;
    private JTable customerTable;
    private JButton addBtn, editBtn, deleteBtn, backBtn;
    private Runnable onBack;

    public CustomerPanel(Runnable onBack) {
        CopyPasteUtil.enableFor(this);

        this.onBack = onBack;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        nameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        vehicleField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Phone (Unique):"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Vehicle No:"));
        formPanel.add(vehicleField);

        addBtn = new JButton("➕ Add Customer");
        editBtn = new JButton("✏ Edit Selected");
        deleteBtn = new JButton("🗑 Delete Selected");
        backBtn = new JButton("⬅ Back");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Table Panel
        customerTable = new JTable();
        JScrollPane tableScroll = new JScrollPane(customerTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Customer List"));
        add(tableScroll, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("🔍 Search");
        searchPanel.add(new JLabel("Search by Name/Phone:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        bottomPanel.add(searchPanel, BorderLayout.WEST);

        // Back button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backBtn.setBackground(new Color(220, 53, 69));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        actionPanel.add(backBtn);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addBtn.addActionListener(e -> addCustomer());
        editBtn.addActionListener(e -> editCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        searchBtn.addActionListener(e -> searchCustomers());
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        loadCustomers();
    }

    private void addCustomer() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String vehicle = vehicleField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Phone are required.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT * FROM customers WHERE phone = ?");
            check.setString(1, phone);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Phone already exists.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO customers (name, phone, email, address, vehicle_number)
                VALUES (?, ?, ?, ?, ?)
            """);
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, vehicle);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Customer added.");
            clearForm();
            loadCustomers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void editCustomer() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        String phone = customerTable.getValueAt(row, 2).toString(); // phone
        String name = JOptionPane.showInputDialog("Name:", customerTable.getValueAt(row, 1));
        String email = JOptionPane.showInputDialog("Email:", customerTable.getValueAt(row, 3));
        String address = JOptionPane.showInputDialog("Address:", customerTable.getValueAt(row, 4));
        String vehicle = JOptionPane.showInputDialog("Vehicle No:", customerTable.getValueAt(row, 5));

        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                UPDATE customers SET name = ?, email = ?, address = ?, vehicle_number = ?
                WHERE phone = ?
            """);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, address);
            stmt.setString(4, vehicle);
            stmt.setString(5, phone);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Customer updated.");
            loadCustomers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteCustomer() {
        int row = customerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        String phone = customerTable.getValueAt(row, 2).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete customer with phone: " + phone + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnector.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM customers WHERE phone = ?");
                stmt.setString(1, phone);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Customer deleted.");
                loadCustomers();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        String sql = "SELECT id, name, phone, email, address, vehicle_number FROM customers " +
                     "WHERE name LIKE ? OR phone LIKE ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Email", "Address", "Vehicle"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("vehicle_number")
                });
            }
            customerTable.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone, email, address, vehicle_number FROM customers")) {

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Email", "Address", "Vehicle"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("vehicle_number")
                });
            }
            customerTable.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        vehicleField.setText("");
    }
}
