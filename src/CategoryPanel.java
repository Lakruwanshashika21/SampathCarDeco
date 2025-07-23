import models.Category;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CategoryPanel extends JPanel {
    private DefaultListModel<Category> categoryModel = new DefaultListModel<>();
    private JList<Category> categoryList = new JList<>(categoryModel);
    private JTextField nameField = new JTextField(20);

    private Runnable onBack;  // callback to main frame or controller

    public CategoryPanel(Runnable onBack) {
        this.onBack = onBack;  // store callback

        CopyPasteUtil.enableFor(this);


        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Top panel with Back button and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("⬅ Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(220, 220, 220));
        backButton.addActionListener(e -> onBack.run());  // call back action
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel title = new JLabel("Manage Categories", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(title, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Form panel (Category input + Add button)
        JPanel form = new JPanel();
        form.add(new JLabel("Category Name:"));
        form.add(nameField);
        JButton addBtn = new JButton("Add");
        form.add(addBtn);
        add(form, BorderLayout.CENTER);

        // List panel (JList + update/delete buttons)
        JPanel listPanel = new JPanel(new BorderLayout());
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete");
        JButton updateBtn = new JButton("Update");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(listPanel, BorderLayout.SOUTH);

        loadCategories();

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                try (Connection conn = DatabaseConnector.connect()) {
                    String sql = "INSERT INTO categories(name) VALUES (?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.executeUpdate();
                    nameField.setText("");
                    loadCategories();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            Category selected = categoryList.getSelectedValue();
            if (selected != null) {
                try (Connection conn = DatabaseConnector.connect()) {
                    String sql = "DELETE FROM categories WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, selected.getId());
                    stmt.executeUpdate();
                    loadCategories();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        updateBtn.addActionListener(e -> {
            Category selected = categoryList.getSelectedValue();
            String newName = nameField.getText().trim();
            if (selected != null && !newName.isEmpty()) {
                try (Connection conn = DatabaseConnector.connect()) {
                    String sql = "UPDATE categories SET name = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, newName);
                    stmt.setInt(2, selected.getId());
                    stmt.executeUpdate();
                    loadCategories();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Category selected = categoryList.getSelectedValue();
                if (selected != null) {
                    nameField.setText(selected.getName());
                }
            }
        });
    }

    private void loadCategories() {
        categoryModel.clear();
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryModel.addElement(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
