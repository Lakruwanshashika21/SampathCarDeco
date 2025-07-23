import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class InventoryPanel extends JPanel {

    
    private JTextField itemIdField, quantityField, buyPriceField, sellPriceField;
    private JButton loadBtn, addStockBtn, backBtn, barcodeBtn, editBtn, deleteBtn;
    private JLabel itemNameLabel, categoryLabel, profitLabel, barcodePreviewLabel;
    private JTable logTable, currentTable;

    private final Runnable onBack;

    String itemId;
    File barcodeFile;

    

    // For autocomplete:
    private JPopupMenu suggestionPopup;
    private List<String[]> productList; // Each: [product_id, name, barcode]

    public InventoryPanel(Runnable onBack) {
        CopyPasteUtil.enableFor(this);

        this.onBack = onBack;
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        itemIdField = new JTextField();
        quantityField = new JTextField();
        buyPriceField = new JTextField();
        sellPriceField = new JTextField();

        itemNameLabel = new JLabel("-");
        categoryLabel = new JLabel("-");
        profitLabel = new JLabel("0.0");
        JButton previewBarcodeBtn = new JButton("🔍 Preview Barcode");


        loadBtn = new JButton("Load Item");
        addStockBtn = new JButton("Add to Inventory");
        barcodeBtn = new JButton("Generate Barcode");
        backBtn = new JButton("⬅ Back");
        editBtn = new JButton("✏ Edit Selected");
        deleteBtn = new JButton("🗑 Delete Selected");

        formPanel.add(new JLabel("Item ID or Barcode:"));
        formPanel.add(itemIdField);

        formPanel.add(new JLabel(""));
        formPanel.add(loadBtn);

        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(itemNameLabel);

        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryLabel);

        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);

        formPanel.add(new JLabel("Buy Price:"));
        formPanel.add(buyPriceField);

        formPanel.add(new JLabel("Sell Price:"));
        formPanel.add(sellPriceField);

        formPanel.add(new JLabel("Profit per item:"));
        formPanel.add(profitLabel);

        formPanel.add(new JLabel("")); // empty label
        formPanel.add(previewBarcodeBtn);


        formPanel.add(barcodeBtn);
        formPanel.add(addStockBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        // topPanel.add(backBtn, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Tables panel
        JPanel tablePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        logTable = new JTable();
        currentTable = new JTable();

        JScrollPane logScroll = new JScrollPane(logTable);
        JScrollPane currentScroll = new JScrollPane(currentTable);
        logScroll.setBorder(BorderFactory.createTitledBorder("Inventory Diary (Logs)"));
        currentScroll.setBorder(BorderFactory.createTitledBorder("Current Inventory"));

        tablePanel.add(logScroll);
        tablePanel.add(currentScroll);
        add(tablePanel, BorderLayout.CENTER);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(backBtn, BorderLayout.SOUTH);

        add(actionPanel, BorderLayout.SOUTH);

        // Setup autocomplete popup and load product list
        suggestionPopup = new JPopupMenu();
        productList = new ArrayList<>();
        loadProductList();

        // Listeners
        loadBtn.addActionListener(e -> loadItem());
        addStockBtn.addActionListener(e -> addInventory());
        barcodeBtn.addActionListener(e -> generateBarcode());
        backBtn.addActionListener(e -> onBack.run());
        editBtn.addActionListener(e -> editSelectedInventory());
        deleteBtn.addActionListener(e -> deleteSelectedInventory());
        previewBarcodeBtn.addActionListener(e -> {
            itemId = itemIdField.getText().trim();
            if (itemId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Load or enter an Item ID to preview barcode.");
                return;
            }

            File barcodeFile = new File("barcodes/" + itemId + "_" + itemNameLabel.getText() + ".png");
            if (barcodeFile.exists()) {
                ImageIcon icon = new ImageIcon(barcodeFile.getAbsolutePath());
                JLabel imgLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(300, 100, Image.SCALE_SMOOTH)));

                JOptionPane.showMessageDialog(this, imgLabel, "Barcode Preview", JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No barcode image found for this item.");
            }
        });


        // Autocomplete on itemIdField:
        itemIdField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = itemIdField.getText().trim();
                if (text.isEmpty()) {
                    suggestionPopup.setVisible(false);
                    return;
                }
                showSuggestions(text);
            }
        });

        // Hide popup on focus lost
        itemIdField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> suggestionPopup.setVisible(false));
            }
        });

        refreshTables();
    }

    private void loadProductList() {
        productList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT product_id, name, barcode FROM products")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new String[]{
                    rs.getString("product_id"),
                    rs.getString("name"),
                    rs.getString("barcode") != null ? rs.getString("barcode") : ""
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showSuggestions(String text) {
        suggestionPopup.removeAll();
        String lowerText = text.toLowerCase();
        int count = 0;

        for (String[] prod : productList) {
            String productId = prod[0];
            String name = prod[1];
            String barcode = prod[2];

            if (productId.toLowerCase().contains(lowerText) ||
                name.toLowerCase().contains(lowerText) ||
                barcode.toLowerCase().contains(lowerText)) {

                JMenuItem item = new JMenuItem(productId + " - " + name + (barcode.isEmpty() ? "" : " (Barcode: " + barcode + ")"));
                item.addActionListener(e -> {
                    itemIdField.setText(productId);
                    suggestionPopup.setVisible(false);
                    loadItem(); // Auto load when suggestion selected
                });
                suggestionPopup.add(item);
                count++;
                if (count >= 10) break; // Limit suggestions
            }
        }

        if (count > 0) {
            suggestionPopup.show(itemIdField, 0, itemIdField.getHeight());
        } else {
            suggestionPopup.setVisible(false);
        }
    }

    // The rest of your methods unchanged below...

    private void loadItem() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Item ID");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = """
                SELECT p.name AS item_name, c.name AS category
                FROM products p
                JOIN categories c ON p.category_id = c.id
                WHERE p.product_id = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                itemNameLabel.setText(rs.getString("item_name"));
                categoryLabel.setText(rs.getString("category"));

                // Load barcode preview if exists
                File barcodeFile = new File("barcodes/" + itemId + ".png");
                if (barcodeFile.exists()) {
                   itemId = itemIdField.getText().trim();
                    if (itemId.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Load or enter an Item ID to preview barcode.");
                        return;
                    }

                    barcodeFile = new File("barcodes/" + itemId + "_" + itemNameLabel.getText() + ".png");
                    if (barcodeFile.exists()) {
                        ImageIcon icon = new ImageIcon(barcodeFile.getAbsolutePath());
                        JLabel imgLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(300, 100, Image.SCALE_SMOOTH)));

                        JOptionPane.showMessageDialog(this, imgLabel, "Barcode Preview", JOptionPane.PLAIN_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No barcode image found for this item.");
                    }
                } else {
                    barcodePreviewLabel.setIcon(null);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Item not found.");
                itemNameLabel.setText("-");
                categoryLabel.setText("-");
                barcodePreviewLabel.setIcon(null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading item: " + ex.getMessage());
        }
    }

    private void addInventory() {
        String itemId = itemIdField.getText().trim();
        String itemName = itemNameLabel.getText();
        String category = categoryLabel.getText();

        if (itemId.isEmpty() || itemName.equals("-") || category.equals("-")) {
            JOptionPane.showMessageDialog(this, "Load a valid item first.");
            return;
        }

        int quantity;
        double buyPrice, sellPrice;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            buyPrice = Double.parseDouble(buyPriceField.getText().trim());
            sellPrice = Double.parseDouble(sellPriceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter valid quantity and prices.");
            return;
        }

        String date = LocalDate.now().toString();
        double profit = sellPrice - buyPrice;
        profitLabel.setText(String.format("%.2f", profit));

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement logStmt = conn.prepareStatement("""
                INSERT INTO inventory_logs (item_id, category, item_name, quantity, buy_price, sell_price, date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """);
            logStmt.setString(1, itemId);
            logStmt.setString(2, category);
            logStmt.setString(3, itemName);
            logStmt.setInt(4, quantity);
            logStmt.setDouble(5, buyPrice);
            logStmt.setDouble(6, sellPrice);
            logStmt.setString(7, date);
            logStmt.executeUpdate();

            PreparedStatement selectStmt = conn.prepareStatement("SELECT quantity FROM current_inventory WHERE item_id = ?");
            selectStmt.setString(1, itemId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int newQty = rs.getInt("quantity") + quantity;
                PreparedStatement updateStmt = conn.prepareStatement("""
                    UPDATE current_inventory SET quantity = ?, buy_price = ?, sell_price = ? WHERE item_id = ?
                """);
                updateStmt.setInt(1, newQty);
                updateStmt.setDouble(2, buyPrice);
                updateStmt.setDouble(3, sellPrice);
                updateStmt.setString(4, itemId);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement("""
                    INSERT INTO current_inventory (item_id, category, item_name, quantity, buy_price, sell_price)
                    VALUES (?, ?, ?, ?, ?, ?)
                """);
                insertStmt.setString(1, itemId);
                insertStmt.setString(2, category);
                insertStmt.setString(3, itemName);
                insertStmt.setInt(4, quantity);
                insertStmt.setDouble(5, buyPrice);
                insertStmt.setDouble(6, sellPrice);
                insertStmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "✅ Inventory updated.");
            refreshTables();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating inventory: " + ex.getMessage());
        }
    }

    private void generateBarcode() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Item ID to generate barcode");
            return;
        }

        try {
            File barcodeFile = new File("barcodes/" + itemId + "_" + itemNameLabel.getText() + ".png");
            barcodeFile.getParentFile().mkdirs(); // Ensure folder exists

            // Create Barcode Bean
            Code39Bean bean = new Code39Bean();
            final int dpi = 150;

            // Configure the barcode generator
            bean.setModuleWidth(0.2); // narrow bar width
            bean.setWideFactor(3);
            bean.doQuietZone(true);

            // Create the canvas provider to generate PNG
            BufferedImage image = new BufferedImage(200, 60, BufferedImage.TYPE_BYTE_BINARY);
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            bean.generateBarcode(canvas, itemId);
            canvas.finish();

            // Write the barcode to file
            ImageIO.write(canvas.getBufferedImage(), "png", barcodeFile);

            JOptionPane.showMessageDialog(this, "✅ Barcode saved to:\n" + barcodeFile.getAbsolutePath());

            // Preview
             barcodeFile = new File("barcodes/" + itemId + "_" + itemNameLabel.getText() + ".png");
                if (barcodeFile.exists()) {
                   itemId = itemIdField.getText().trim();
                    if (itemId.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Load or enter an Item ID to preview barcode.");
                        return;
                    }

                    barcodeFile = new File("barcodes/" + itemId + "_" + itemNameLabel.getText() + ".png");
                    if (barcodeFile.exists()) {
                        ImageIcon icon = new ImageIcon(barcodeFile.getAbsolutePath());
                        JLabel imgLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(300, 100, Image.SCALE_SMOOTH)));

                        JOptionPane.showMessageDialog(this, imgLabel, "Barcode Preview", JOptionPane.PLAIN_MESSAGE);
                    } 
                } else {
                    barcodePreviewLabel.setIcon(null);
                }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error generating barcode: " + ex.getMessage());
        }
        
    }


    private void editSelectedInventory() {
        int row = currentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        String itemId = currentTable.getValueAt(row, 0).toString();
        String itemName = currentTable.getValueAt(row, 2).toString();
        String quantity = JOptionPane.showInputDialog("Quantity:", currentTable.getValueAt(row, 3));
        String buyPrice = JOptionPane.showInputDialog("Buy Price:", currentTable.getValueAt(row, 4));
        String sellPrice = JOptionPane.showInputDialog("Sell Price:", currentTable.getValueAt(row, 5));

        if (quantity == null || buyPrice == null || sellPrice == null) return; // Cancelled dialog

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "UPDATE current_inventory SET quantity = ?, buy_price = ?, sell_price = ? WHERE item_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(quantity));
            stmt.setDouble(2, Double.parseDouble(buyPrice));
            stmt.setDouble(3, Double.parseDouble(sellPrice));
            stmt.setString(4, itemId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Inventory updated for: " + itemName);
            refreshTables();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating inventory: " + e.getMessage());
        }
    }

    private void deleteSelectedInventory() {
        int row = currentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }
        String itemId = currentTable.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete inventory for item ID: " + itemId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnector.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM current_inventory WHERE item_id = ?");
                stmt.setString(1, itemId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Deleted.");
                refreshTables();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting inventory: " + e.getMessage());
            }
        }
    }

    private void refreshTables() {
        loadTableData("SELECT * FROM inventory_logs ORDER BY date DESC", logTable);
        loadTableData("SELECT * FROM current_inventory ORDER BY item_id ASC", currentTable);
    }

    private void loadTableData(String sql, JTable table) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                model.addColumn(meta.getColumnName(i));
            }
            while (rs.next()) {
                Object[] row = new Object[meta.getColumnCount()];
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading table: " + ex.getMessage());
        }
    }
}
