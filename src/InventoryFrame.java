import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbCategory;
    private JTextField txtName, txtPrice, txtQuantity;
    private JButton btnAdd;

    public InventoryFrame() {
        setTitle("Inventory Management");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[] { "ID", "Name", "Category", "Price", "Quantity" }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // disable editing directly in table
            }
        };

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        // Input Panel
        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        txtName = new JTextField();
        String[] categories = { "Stickers", "Parts", "Designs", "Others" };
        cmbCategory = new JComboBox<>(categories);
        txtPrice = new JTextField();
        txtQuantity = new JTextField();
        btnAdd = new JButton("Add Item");

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(txtName);

        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(cmbCategory);

        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(txtPrice);

        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(txtQuantity);

        inputPanel.add(new JLabel("")); // spacing
        inputPanel.add(btnAdd);

        add(scroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        loadItems();

        btnAdd.addActionListener(e -> addItem());
    }

    private void loadItems() {
        model.setRowCount(0); // clear table

        List<Item> items = ItemDAO.getAllItems();
        for (Item item : items) {
            model.addRow(new Object[] {
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getPrice(),
                    item.getQuantity()
            });
        }
    }

    private void addItem() {
        String name = txtName.getText().trim();
        String category = cmbCategory.getSelectedItem().toString();
        String priceText = txtPrice.getText().trim();
        String quantityText = txtQuantity.getText().trim();

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a number and quantity must be an integer.");
            return;
        }

        Item item = new Item(name, category, price, quantity);
        boolean success = ItemDAO.addItem(item);
        if (success) {
            JOptionPane.showMessageDialog(this, "Item added successfully.");
            loadItems();
            txtName.setText("");
            txtPrice.setText("");
            txtQuantity.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add item.");
        }
    }
}
