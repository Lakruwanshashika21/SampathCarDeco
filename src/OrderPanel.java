import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class OrderPanel extends JPanel {
    private JTextField customerPhoneField, itemIdField, discountField;
    private JLabel customerNameLabel;
    private JButton loadCustomerBtn, addItemBtn, printBtn, backBtn, removeItemBtn, customerPanelBtn, setPathBtn, refreshBtn;
    private JTable orderTable;
    private DefaultTableModel orderModel;
    private final Runnable onBack;



    private int customerId = -1;
    private double totalAmount = 0.0;

    private JPopupMenu suggestionPopup;
    private List<String[]> productList; // [product_id, name]

    public OrderPanel(Runnable onBack) {

        CopyPasteUtil.enableFor(this);

        this.onBack = onBack;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        customerPhoneField = new JTextField();
        loadCustomerBtn = new JButton("Load Customer");
        customerNameLabel = new JLabel("Customer Name: -");
        itemIdField = new JTextField();
        addItemBtn = new JButton("Add Item");
        discountField = new JTextField("0");
        printBtn = new JButton("Print Bill");
        refreshBtn = new JButton("🔄 Refresh");
        backBtn = new JButton("⬅ Back");
        setPathBtn = new JButton("📁 Set Save Path");
        removeItemBtn = new JButton("Remove Selected Item");  // New Remove button
        customerPanelBtn = new JButton("Open Customer Panel");
        JButton billSearchBtn = new JButton("📄 Search Bill");
        


        topPanel.add(new JLabel("Customer Phone:"));
        topPanel.add(customerPhoneField);
        topPanel.add(loadCustomerBtn);

        topPanel.add(customerNameLabel);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));

        topPanel.add(new JLabel("Item ID / Name / Barcode:"));
        topPanel.add(itemIdField);
        topPanel.add(addItemBtn);

        add(topPanel, BorderLayout.NORTH);

        orderModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Quantity", "Sell Price", "Subtotal"}, 0);
        orderTable = new JTable(orderModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(customerPanelBtn);
        bottomPanel.add(billSearchBtn);
        bottomPanel.add(refreshBtn);
        bottomPanel.add(new JLabel("Discount:"));
        bottomPanel.add(discountField);
        bottomPanel.add(removeItemBtn);  // Add remove button here
        bottomPanel.add(printBtn);
        bottomPanel.add(setPathBtn);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        


        suggestionPopup = new JPopupMenu();
        productList = new ArrayList<>();
        loadProductList();

        customerPanelBtn.addActionListener(e -> {
            CustomerPanel customerPanel = new CustomerPanel(onBack);
            JFrame customerFrame = new JFrame("Customer Panel");
            customerFrame.setContentPane(customerPanel);
            customerFrame.setSize(800, 600);
            customerFrame.setVisible(true);
        });

        loadCustomerBtn.addActionListener(e -> loadCustomer());
        addItemBtn.addActionListener(e -> addItemToOrder());
        printBtn.addActionListener(e -> printBill());
        backBtn.addActionListener(e -> onBack.run());
        removeItemBtn.addActionListener(e -> removeSelectedItem());  // Remove button action
        setPathBtn.addActionListener(e -> chooseSavePath());
        refreshBtn.addActionListener(e -> clearForm());
        billSearchBtn.addActionListener(e -> showBillSearchPanel());



        itemIdField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = itemIdField.getText();
                if (text.isEmpty()) {
                    suggestionPopup.setVisible(false);
                    return;
                }
                showSuggestions(text);
            }
        });
    }

    private void showBillSearchPanel() {
    JFrame frame = new JFrame("Bill Search");
    File folder = new File(BillService.getSavePath());
    BillSearchPanel panel = new BillSearchPanel(folder);
    frame.setContentPane(panel);
    frame.setSize(400, 150);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}



    private void loadProductList() {
        productList.clear();
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT product_id, name FROM products")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new String[]{rs.getString("product_id"), rs.getString("name")});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        customerPhoneField.setText("");
        customerNameLabel.setText("Customer Name: -");
        itemIdField.setText("");
        orderModel.setRowCount(0);
        totalAmount = 0.0;
        
    }

    private Preferences prefs = Preferences.userRoot().node("OrderPanelPrefs");

    private void chooseSavePath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose Folder to Save Bills");

        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            // Use BillService's prefs and key for consistency
            Preferences prefs = Preferences.userRoot().node("OrderPanelPrefs");
            prefs.put("savePath", selectedFolder.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Save path set to:\n" + selectedFolder.getAbsolutePath());
        }
    }




    private String getSavePath() {
        return prefs.get("savePath", "Bill/Order Bill");
    }


    private void showSuggestions(String text) {
        suggestionPopup.removeAll();
        String lowerText = text.toLowerCase();
        int count = 0;
        for (String[] prod : productList) {
            String productId = prod[0];
            String name = prod[1];
            if (productId.toLowerCase().contains(lowerText) || name.toLowerCase().contains(lowerText)) {
                JMenuItem item = new JMenuItem(productId + " - " + name);
                item.addActionListener(e -> {
                    itemIdField.setText(productId);
                    suggestionPopup.setVisible(false);
                });
                suggestionPopup.add(item);
                count++;
                if (count >= 10) break;
            }
        }
        if (count > 0) {
            suggestionPopup.show(itemIdField, 0, itemIdField.getHeight());
        } else {
            suggestionPopup.setVisible(false);
        }
    }

    private void loadCustomer() {
        String phone = customerPhoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer phone number.");
            return;
        }
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM customers WHERE phone = ?")) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("id");
                customerNameLabel.setText("Customer Name: " + rs.getString("name"));
            } else {
                int confirm = JOptionPane.showConfirmDialog(this, "Customer not found. Register new customer?", "New Customer", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    registerNewCustomer(phone);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customer: " + ex.getMessage());
        }
    }

    private void registerNewCustomer(String phone) {
        String name = JOptionPane.showInputDialog(this, "Enter customer name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return;
        }
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO customers (name, phone) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                customerId = keys.getInt(1);
                customerNameLabel.setText("Customer Name: " + name);
                JOptionPane.showMessageDialog(this, "Customer registered.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering customer: " + ex.getMessage());
        }
    }

    private void addItemToOrder() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter item ID or barcode.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.product_id, p.name, ci.sell_price, ci.quantity as stock_qty " +
                             "FROM products p JOIN current_inventory ci ON p.product_id = ci.item_id " +
                             "WHERE p.product_id = ?")) {
            stmt.setString(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String pid = rs.getString("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("sell_price");
                int stockQty = rs.getInt("stock_qty");

                // Ask quantity from user
                String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity (Available: " + stockQty + "):", "1");
                if (qtyStr == null) return; // cancel
                int requestedQty;
                try {
                    requestedQty = Integer.parseInt(qtyStr);
                    if (requestedQty <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantity must be positive.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity.");
                    return;
                }

                if (requestedQty > stockQty) {
                    JOptionPane.showMessageDialog(this, "Requested quantity exceeds available stock. Auto-adjusting to " + stockQty);
                    requestedQty = stockQty;
                    if (requestedQty == 0) {
                        JOptionPane.showMessageDialog(this, "Item out of stock.");
                        return;
                    }
                }

                // Check if item already in order; increase quantity if yes
                for (int i = 0; i < orderModel.getRowCount(); i++) {
                    if (orderModel.getValueAt(i, 0).equals(pid)) {
                        int currentQty = (int) orderModel.getValueAt(i, 2);
                        int newQty = currentQty + requestedQty;
                        if (newQty > stockQty) {
                            JOptionPane.showMessageDialog(this, "Total quantity in order exceeds stock. Adjusting to max available.");
                            newQty = stockQty;
                        }
                        orderModel.setValueAt(newQty, i, 2);
                        orderModel.setValueAt(newQty * price, i, 4);
                        updateTotalAmount();
                        return;
                    }
                }

                // Add new row
                orderModel.addRow(new Object[]{pid, name, requestedQty, price, requestedQty * price});
                updateTotalAmount();

            } else {
                JOptionPane.showMessageDialog(this, "Item not found or out of stock.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding item: " + ex.getMessage());
        }
    }

    private void removeSelectedItem() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove selected item from the order?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            orderModel.removeRow(selectedRow);
            updateTotalAmount();
        }
    }

    private void updateTotalAmount() {
        totalAmount = 0;
        for (int i = 0; i < orderModel.getRowCount(); i++) {
            totalAmount += (double) orderModel.getValueAt(i, 4);
        }
        double discount = 0;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (Exception ignored) {
        }

        double finalAmount = Math.max(totalAmount - discount, 0);
        printBtn.setText("Print Bill (Total: " + String.format("%.2f", finalAmount) + ")");
    }

    

    private void printBill() {
    if (customerId == -1) {
        JOptionPane.showMessageDialog(this, "Load or register a customer first.");
        return;
    }
    if (orderModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Add at least one item to the order.");
        return;
    }

    double discount = 0;
    try {
        discount = Double.parseDouble(discountField.getText().trim());
    } catch (Exception ignored) {}

    double finalAmount = Math.max(totalAmount - discount, 0);

    try (Connection conn = DatabaseConnector.connect()) {
        conn.setAutoCommit(false);

        // Insert bill
        String insertBillSQL = "INSERT INTO bills (customer_id, date, total_amount, discount) VALUES (?, ?, ?, ?)";
        PreparedStatement billStmt = conn.prepareStatement(insertBillSQL, Statement.RETURN_GENERATED_KEYS);
        billStmt.setInt(1, customerId);
        billStmt.setString(2, LocalDate.now().toString());
        billStmt.setDouble(3, finalAmount);
        billStmt.setDouble(4, discount);
        billStmt.executeUpdate();

        ResultSet keys = billStmt.getGeneratedKeys();
        int billId = -1;
        if (keys.next()) {
            billId = keys.getInt(1);
        }

        // Insert bill items and update inventory
        String insertDetailSQL = "INSERT INTO bill_items (bill_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStockSQL = "UPDATE current_inventory SET quantity = quantity - ? WHERE item_id = ?";
        PreparedStatement detailStmt = conn.prepareStatement(insertDetailSQL);
        PreparedStatement updateStmt = conn.prepareStatement(updateStockSQL);

        List<String[]> pdfItems = new ArrayList<>();

        for (int i = 0; i < orderModel.getRowCount(); i++) {
            String pid = orderModel.getValueAt(i, 0).toString();
            String name = orderModel.getValueAt(i, 1).toString();
            int qty = (int) orderModel.getValueAt(i, 2);
            double price = (double) orderModel.getValueAt(i, 3);
            double subtotal = (double) orderModel.getValueAt(i, 4);

            detailStmt.setInt(1, billId);
            detailStmt.setString(2, pid);
            detailStmt.setInt(3, qty);
            detailStmt.setDouble(4, price);
            detailStmt.addBatch();

            updateStmt.setInt(1, qty);
            updateStmt.setString(2, pid);
            updateStmt.addBatch();

            pdfItems.add(new String[]{name, String.valueOf(qty), String.valueOf(price)});
        }

        detailStmt.executeBatch();
        updateStmt.executeBatch();
        conn.commit();

        JOptionPane.showMessageDialog(this, "Bill saved successfully! Total: " + finalAmount);

        int option = JOptionPane.showConfirmDialog(this, "Generate and Print Bill PDF?", "Generate PDF", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            String customerName = customerNameLabel.getText().replace("Customer Name: ", "");
            String customerPhone = customerPhoneField.getText().trim();

            String[][] itemArray = pdfItems.toArray(new String[0][]);
            String savePath = BillService.getSavePath();

            BillService.generateAndPrintBill(
                    "ORDER",
                    customerName + " (" + customerPhone + ")",
                    itemArray,
                    finalAmount,
                    discount,
                    savePath
            );

            JOptionPane.showMessageDialog(this, "✅ Bill PDF saved and sent to printer.");
        }

        clearOrder();

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error saving bill: " + ex.getMessage());
    }
}


private void clearOrder() {
    orderModel.setRowCount(0);
    discountField.setText("0");
    customerNameLabel.setText("Customer Name: -");
    customerPhoneField.setText("");
    customerId = -1;
    totalAmount = 0;
    printBtn.setText("Print Bill");
}

}