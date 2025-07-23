import models.Category;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductPanel extends JPanel {
    private JComboBox<Category> categoryComboBox = new JComboBox<>();
    private JTextField nameField = new JTextField(20);
    private JLabel generatedIdLabel = new JLabel();
    private JButton addBtn = new JButton("Add Product");
    private JButton deleteBtn = new JButton("Delete Selected");
    private JButton updateBtn = new JButton("Update Selected");
    private JTable productTable;
    private DefaultTableModel productTableModel;

    private Runnable onBack;  // callback to main frame or controller

    public ProductPanel(Runnable onBack) {
        this.onBack = onBack;  // store callback


    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

    // ✅ Correct Top panel with Back button and title
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);

    JButton backButton = new JButton("⬅ Back");
    backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    backButton.setFocusPainted(false);
    backButton.setBackground(new Color(220, 220, 220));
    backButton.addActionListener(e -> onBack.run());  // call back action
    topPanel.add(backButton, BorderLayout.WEST);

    JLabel titleLabel = new JLabel("Manage Products", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
    topPanel.add(titleLabel, BorderLayout.CENTER);

    // ✅ Add top panel to NORTH
    add(topPanel, BorderLayout.NORTH);




        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        formPanel.add(categoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Generated ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(generatedIdLabel, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addBtn, gbc);

        add(formPanel, BorderLayout.WEST);

        // Table for products
        productTableModel = new DefaultTableModel(new String[]{"DB ID", "Product ID", "Name", "Category"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // readonly table
            }
        };
        productTable = new JTable(productTableModel);
        productTable.removeColumn(productTable.getColumnModel().getColumn(0)); // hide DB ID column

        JScrollPane tableScroll = new JScrollPane(productTable);
        add(tableScroll, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCategories();
        loadProducts();
        generateUniqueProductId();

        // Event handlers
        addBtn.addActionListener(e -> addProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        updateBtn.addActionListener(e -> updateSelectedProduct());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                int dbId = (int) productTableModel.getValueAt(selectedRow, 0);
                String prodId = (String) productTableModel.getValueAt(selectedRow, 1);
                String prodName = (String) productTableModel.getValueAt(selectedRow, 2);
                String catName = (String) productTableModel.getValueAt(selectedRow, 3);

                nameField.setText(prodName);

                // Set category in combo
                for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                    if (categoryComboBox.getItemAt(i).getName().equals(catName)) {
                        categoryComboBox.setSelectedIndex(i);
                        break;
                    }
                }

                generatedIdLabel.setText(prodId);
            }
        });
    }

    private void generateUniqueProductId() {
        // Generate a simple UUID without dashes, truncated to 8 chars
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        generatedIdLabel.setText(id);
    }

    private void loadCategories() {
        categoryComboBox.removeAllItems();
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryComboBox.addItem(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id, p.product_id, p.name, c.name AS category_name " +
                             "FROM products p INNER JOIN categories c ON p.category_id = c.id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getString("category_name")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage());
        }
    }

    private void addProduct() {
        String name = nameField.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        String productId = generatedIdLabel.getText();

        if (name.isEmpty() || selectedCategory == null || productId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "INSERT INTO products (product_id, name, category_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setString(2, name);
            stmt.setInt(3, selectedCategory.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                nameField.setText("");
                generateUniqueProductId();
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage());
        }
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }
        int dbId = (int) productTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "DELETE FROM products WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, dbId);
            stmt.executeUpdate();
            loadProducts();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
        }
    }

    private void updateSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to update.");
            return;
        }
        int dbId = (int) productTableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();

        if (name.isEmpty() || selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "UPDATE products SET name = ?, category_id = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, selectedCategory.getId());
            stmt.setInt(3, dbId);
            stmt.executeUpdate();
            loadProducts();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage());
        }
    }


}
