// Note: Fully updated JobPanel.java
// - Auto-suggest item names
// - Add multiple items to a job with quantity
// - Save job and item data to database
// - Generate PDF bill on job completion
// - View last 10 done jobs

// Required imports
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.prefs.Preferences;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.util.ArrayList;
import java.util.List;

// At top of JobPanel.java




public class JobPanel extends JPanel {
    private JTable jobTable, itemTable;
    private DefaultTableModel jobTableModel, itemTableModel;
    private JTextField phoneField, vehicleField, nameField, timeField, noteField, jobFeeField, itemNameField, qtyField;
    private JLabel totalAmountLabel;
    private JComboBox<String> jobTypeCombo;
    private JButton addJobBtn, deleteJobBtn, completeJobBtn, getCustomerBtn, addItemBtn, changePathBtn, viewDoneBtn, backBtn, refreshBtn;
    private final Runnable onBack;
    private Preferences prefs = Preferences.userRoot().node("JobPanelPrefs");
    private JPopupMenu suggestionPopup;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;
    private final Runnable onRedirectToCustomer;

    public JobPanel(Runnable onBack, Runnable onRedirectToCustomer) {
        this.onBack = onBack;
        this.onRedirectToCustomer = onRedirectToCustomer;
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(12, 2, 10, 10));
        nameField = new JTextField();
        phoneField = new JTextField();
        vehicleField = new JTextField();
        timeField = new JTextField();
        noteField = new JTextField();
        jobFeeField = new JTextField();
        totalAmountLabel = new JLabel("0.00");
        itemNameField = new JTextField();
        qtyField = new JTextField();

        jobTypeCombo = new JComboBox<>(new String[]{"Cleaning", "Wrapping", "Tinting", "Other"});
        getCustomerBtn = new JButton("🔍 Get Customer");
        addJobBtn = new JButton("➕ Add Appointment");
        deleteJobBtn = new JButton("🗑 Delete Job");
        completeJobBtn = new JButton("✔ Complete Job");
        changePathBtn = new JButton("📁 Set Save Path");
        refreshBtn = new JButton("🔄 Refresh");
        backBtn = new JButton("⬅ Back");
        viewDoneBtn = new JButton("📜 View Done Jobs");
        addItemBtn = new JButton("➕ Add Item");

        formPanel.add(new JLabel("Phone Number:")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Customer Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Vehicle No:")); formPanel.add(vehicleField);
        formPanel.add(new JLabel("Job Type:")); formPanel.add(jobTypeCombo);
        formPanel.add(new JLabel("Appointment Time:")); formPanel.add(timeField);
        formPanel.add(new JLabel("Note:")); formPanel.add(noteField);
        formPanel.add(new JLabel("Item Name:")); formPanel.add(itemNameField);
        formPanel.add(new JLabel("Qty:")); formPanel.add(qtyField);
        formPanel.add(new JLabel("Job Fee (Rs.):")); formPanel.add(jobFeeField);
        formPanel.add(new JLabel("Total Amount:")); formPanel.add(totalAmountLabel);
        formPanel.add(getCustomerBtn); formPanel.add(addItemBtn);
        formPanel.add(addJobBtn);

        add(new JScrollPane(formPanel), BorderLayout.NORTH);


        itemTableModel = new DefaultTableModel(new Object[]{"Item ID", "Name", "Qty", "Price", "Total"}, 0);
        itemTable = new JTable(itemTableModel);

        // Auto Suggest
        suggestionPopup = new JPopupMenu();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                itemNameField.setText(suggestionList.getSelectedValue());
                suggestionPopup.setVisible(false);
            }
        });
        suggestionList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    itemNameField.setText(suggestionList.getSelectedValue());
                    suggestionPopup.setVisible(false);
                }
            }
        });
        suggestionPopup.add(new JScrollPane(suggestionList));

        itemNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { suggestItems(); }
            public void removeUpdate(DocumentEvent e) { suggestItems(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        itemNameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.setSelectedIndex(0);
                }
            }
        });

        addItemBtn.addActionListener(e -> {
            String name = itemNameField.getText();
            if (name.isEmpty()) return;
            String id = "";
            double price = 0;
            int qty = 0;
            int qty1 = 0;
            try {
                qty = Integer.parseInt(qtyField.getText());
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT item_id, sell_price, quantity FROM current_inventory WHERE item_name = ? LIMIT 1")) {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    qty1 = rs.getInt("quantity");
                    if (qty1<qty) {
                        JOptionPane.showMessageDialog(null, "Available Quantity is " + qty1 + ".", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                        
                    }
                    else {
                        if (rs.next()) {
                        id = rs.getString("item_id");
                        price = rs.getDouble("sell_price");
                    }
                        
                    }
                    
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            double total = qty * price;
            itemTableModel.addRow(new Object[]{id, name, qty, price, total});
            itemNameField.setText("");
            qtyField.setText("");
            updateItemPrices();
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        add(formPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(deleteJobBtn);
        bottomPanel.add(refreshBtn);
        bottomPanel.add(completeJobBtn);
        bottomPanel.add(viewDoneBtn);
        bottomPanel.add(changePathBtn);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        

        jobTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Vehicle", "Type", "Time", "Note", "Fee", "Status", "Total" }, 0);
        jobTable = new JTable(jobTableModel);

        JScrollPane itemScroll = new JScrollPane(itemTable);
        itemScroll.setPreferredSize(new Dimension(200, 200)); // ⬅ Reduce width of itemTable
        centerPanel.add(itemScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JScrollPane jobScroll = new JScrollPane(jobTable);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        tablesPanel.add(jobScroll);
        tablesPanel.add(itemScroll);

        add(tablesPanel, BorderLayout.CENTER);

        jobTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = jobTable.getSelectedRow() != -1;
            deleteJobBtn.setEnabled(selected);
            completeJobBtn.setEnabled(selected);
        });




        // Button listeners
        phoneField.addActionListener(e -> loadCustomerDetails());
        getCustomerBtn.addActionListener(e -> loadCustomerDetails());
        addJobBtn.addActionListener(e -> addJob());
        completeJobBtn.addActionListener(e -> completeJob());
        deleteJobBtn.addActionListener(e -> deleteJob());
        backBtn.addActionListener(e -> onBack.run());
        changePathBtn.addActionListener(e -> chooseSavePath());
        viewDoneBtn.addActionListener(e -> viewDoneJobs());
        refreshBtn.addActionListener(e -> clearForm());


        jobFeeField.getDocument().addDocumentListener(new SimpleDoc(this::updateItemPrices));

        refreshTable();
    }

    private void suggestItems() {
        String text = itemNameField.getText().trim();
        if (text.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }
        suggestionModel.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT item_name FROM current_inventory WHERE item_name LIKE ? LIMIT 10")) {
            ps.setString(1, text + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                suggestionModel.addElement(rs.getString("item_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!suggestionModel.isEmpty()) {
            suggestionList.setSelectedIndex(0);
            suggestionPopup.setPopupSize(itemNameField.getWidth(), 150);
            suggestionPopup.show(itemNameField, 0, itemNameField.getHeight());
        } else {
            suggestionPopup.setVisible(false);
        }
    }

    private void clearForm() {
        nameField.setText("");
        phoneField.setText("");
        vehicleField.setText("");
        timeField.setText("");
        noteField.setText("");
        jobFeeField.setText("");
        itemNameField.setText("");
        qtyField.setText("");
        totalAmountLabel.setText("0.00");
        itemTableModel.setRowCount(0);
    }


    private void updateItemPrices() {
        double total = 0;
        try {
            for (int i = 0; i < itemTableModel.getRowCount(); i++) {
                int qty = Integer.parseInt(itemTableModel.getValueAt(i, 2).toString());
                double price = Double.parseDouble(itemTableModel.getValueAt(i, 3).toString());
                double subTotal = qty * price;
                total += subTotal;
                itemTableModel.setValueAt(subTotal, i, 4);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        double jobFee = jobFeeField.getText().isEmpty() ? 0.0 : Double.parseDouble(jobFeeField.getText());
        totalAmountLabel.setText(String.format("%.2f", jobFee + total));
    }

    // ... Other methods remain the same as previous version (addJob, completeJob, deleteJob, generateBillPDF, chooseSavePath, getSavePath, loadCustomerDetails, refreshTable, viewDoneJobs)

   


    // Other methods like chooseSavePath(), getSavePath(), addJob(), completeJob(), generateBillPDF(), deleteJob(), loadCustomerDetails(), refreshTable(), SimpleDoc class remain unchanged.
    Preferences jobPrefs = Preferences.userRoot().node("JobPanelPrefs");

    private void chooseSavePath() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    // Set last-used path if exists
    String lastPath = prefs.get("savePath", null);
    if (lastPath != null) {
        chooser.setCurrentDirectory(new File(lastPath));
    }

    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File dir = chooser.getSelectedFile();
        prefs.put("savePath", dir.getAbsolutePath()); // ✅ save to memory
        JOptionPane.showMessageDialog(this, "✅ Path set to: " + dir.getAbsolutePath());
    }
}


    private String getSavePath() {
        return prefs.get("savePath", "Bill/Job Bill");
    }

    private void addJob() {
    if (nameField.getText().isEmpty() || phoneField.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in customer name and phone.");
        return;
    }

    try (Connection conn = DatabaseConnector.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO jobs (customer_name, mobile, vehicle_number, job_type, time, note, fee, status, date) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', DATE('now'))",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setString(1, nameField.getText());
        stmt.setString(2, phoneField.getText());
        stmt.setString(3, vehicleField.getText());
        stmt.setString(4, jobTypeCombo.getSelectedItem().toString());
        stmt.setString(5, timeField.getText());
        stmt.setString(6, noteField.getText());
        stmt.setDouble(7, Double.parseDouble(jobFeeField.getText()));
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            int jobId = keys.getInt(1);
            for (int i = 0; i < itemTableModel.getRowCount(); i++) {
                String itemId = itemTableModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(itemTableModel.getValueAt(i, 2).toString());
                double price = Double.parseDouble(itemTableModel.getValueAt(i, 3).toString());
                double total = Double.parseDouble(itemTableModel.getValueAt(i, 4).toString());

                PreparedStatement itemStmt = conn.prepareStatement("INSERT INTO job_items (job_id, item_id, quantity, price, total) VALUES (?, ?, ?, ?, ?)");
                itemStmt.setInt(1, jobId);
                itemStmt.setString(2, itemId);
                itemStmt.setInt(3, qty);
                itemStmt.setDouble(4, price);
                itemStmt.setDouble(5, total);
                itemStmt.executeUpdate();
            }
        }

        JOptionPane.showMessageDialog(this, "✅ Job added");
        refreshTable();
        clearForm();

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "❌ Failed to add job: " + ex.getMessage());
    }
}


    private void viewDoneJobs() {
        try (Connection conn = DatabaseConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT id, customer_name, mobile, vehicle_number, job_type, time, fee, time_closed FROM jobs WHERE status = 'Done' ORDER BY id")) {
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new String[]{"ID", "Name", "Phone", "Vehicle", "Type", "Time", "Fee", "Completed"});
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("customer_name"),
                    rs.getString("mobile"),
                    rs.getString("vehicle_number"),
                    rs.getString("job_type"),
                    rs.getString("time"),
                    rs.getDouble("fee"),
                    rs.getString("time_closed")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "Completed Jobs", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to load done jobs.");
        }
    }

    private double calculateTotalItems(String[][] items) {
    double total = 0;
    for (String[] row : items) {
        int qty = Integer.parseInt(row[1]);
        double price = Double.parseDouble(row[2]);
        total += qty * price;
    }
    return total;
}



    private void completeJob() {
        int row = jobTable.getSelectedRow();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to complete this job?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (row == -1) return;
        int id = Integer.parseInt(jobTable.getValueAt(row, 0).toString());
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement done = conn.prepareStatement("UPDATE jobs SET status = 'Done', time_closed = ? WHERE id = ?");
            done.setString(1, LocalDateTime.now().toString());
            done.setInt(2, id);
            done.executeUpdate();

            PreparedStatement items = conn.prepareStatement("SELECT item_id, quantity FROM job_items WHERE job_id = ?");
            items.setInt(1, id);
            ResultSet rs = items.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item_id");
                int qty = rs.getInt("quantity");
                PreparedStatement upd = conn.prepareStatement("UPDATE current_inventory SET quantity = quantity - ? WHERE item_id = ?");
                upd.setInt(1, qty);
                upd.setString(2, item);
                upd.executeUpdate();
            }

            PreparedStatement info = conn.prepareStatement("SELECT customer_name, fee FROM jobs WHERE id = ?");
            info.setInt(1, id);
            ResultSet infoRs = info.executeQuery();

            if (infoRs.next()) {
                String customerName = infoRs.getString("customer_name");
                double jobFee = infoRs.getDouble("fee");

                PreparedStatement itemsStmt = conn.prepareStatement("SELECT p.item_name, ji.quantity, ji.price FROM job_items ji JOIN current_inventory p ON ji.item_id = p.item_id WHERE ji.job_id = ?");
                itemsStmt.setInt(1, id);
                ResultSet itemRs = itemsStmt.executeQuery();

                List<String[]> itemList = new ArrayList<>();
                while (itemRs.next()) {
                    String name = itemRs.getString("item_name");
                    String qty = itemRs.getString("quantity");
                    String price = itemRs.getString("price");
                    itemList.add(new String[]{name, qty, price});
                }

                String[][] itemArray = itemList.toArray(new String[0][0]);
                String savePath = getSavePath();
                BillService.generateAndPrintBill("Service", customerName, itemArray, jobTypeCombo.getSelectedItem().toString() ,jobFee, jobFee + calculateTotalItems(itemArray), savePath, null);

            }

            JOptionPane.showMessageDialog(this, "✅ PDF Saved to " + getSavePath());
            refreshTable();
        } catch (Exception ex) {
            ex.printStackTrace();
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
                int customerId = keys.getInt(1);
                nameField.setText(name); // ✅ This is correct
                JOptionPane.showMessageDialog(this, "Customer registered.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering customer: " + ex.getMessage());
        }
    }


    private void deleteJob() {
        int row = jobTable.getSelectedRow();
        if (row == -1) return;
        int id = Integer.parseInt(jobTable.getValueAt(row, 0).toString());
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM jobs WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            refreshTable();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCustomerDetails() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) return;

        try (Connection conn = DatabaseConnector.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT name, vehicle_number FROM customers WHERE phone = ?")) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                vehicleField.setText(rs.getString("vehicle_number"));
            } else {
                int option = JOptionPane.showConfirmDialog(this, 
                    "Customer not found.\nDo you want to register this customer?", 
                    "Customer Not Found", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    if (onRedirectToCustomer != null) onRedirectToCustomer.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customer.");
        }
    }


    private void refreshTable() {

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT \r\n" + //
                                  "  j.id, j.customer_name, j.mobile, j.vehicle_number, j.job_type, \r\n" + //
                                  "  j.time, j.note, j.fee, j.status,\r\n" + //
                                  "  (j.fee + IFNULL(SUM(i.total), 0)) AS total\r\n" + //
                                  "FROM jobs j\r\n" + //
                                  "LEFT JOIN job_items i ON j.id = i.job_id\r\n" + //
                                  "WHERE j.status = 'Pending'\r\n" + //
                                  "GROUP BY j.id\r\n" + //
                                  "ORDER BY j.id DESC\r\n" + //
                                  "")) {
            
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new String[]{ "ID", "Name", "Phone", "Vehicle", "Type", "Time", "Note", "Fee", "Status", "Total"});

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("customer_name"),
                    rs.getString("mobile"),
                    rs.getString("vehicle_number"),
                    rs.getString("job_type"),
                    rs.getString("time"),
                    rs.getString("note"),
                    rs.getDouble("fee"),
                    rs.getString("status"),
                    rs.getDouble("total")
                });
            }
            
            jobTable.setModel(model);
            jobTable.getColumnModel().getColumn(9).setPreferredWidth(100); // index 9 = "Total"
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SimpleDoc implements DocumentListener {
    private final Runnable onChange;

    public SimpleDoc(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onChange.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onChange.run();
    }
    }

    
}