// ReportPanel.java
// Updated with PDF export and Back button functionality

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.Vector;
import com.itextpdf.text.*;
import java.awt.Font;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;


public class ReportPanel extends JPanel {
    private JComboBox<String> reportTypeCombo;
    private JButton generateBtn, exportPdfBtn, backBtn;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private Connection conn;
    private String[] currentColumns;
    private String currentTitle;
    private Runnable onBack;

    public ReportPanel(Connection conn, Runnable onBack) {
        this.conn = conn;
        this.onBack = onBack;
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Reports", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        reportTypeCombo = new JComboBox<>(new String[]{
                "Inventory Report", "Sales Report", "Job Report",
                "Customer Report", "Admin Activity Report"
        });

        generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(new ReportButtonListener());

        exportPdfBtn = new JButton("Export PDF");
        exportPdfBtn.addActionListener(e -> exportReportToPDF());

        backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        topPanel.add(new JLabel("Select Report: "));
        topPanel.add(reportTypeCombo);
        topPanel.add(generateBtn);
        topPanel.add(exportPdfBtn);
        topPanel.add(backBtn);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    private class ReportButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String selectedReport = (String) reportTypeCombo.getSelectedItem();
            switch (selectedReport) {
                case "Inventory Report":
                    generateInventoryReport(); break;
                case "Sales Report":
                    generateSalesReport(); break;
                case "Job Report":
                    generateJobReport(); break;
                case "Customer Report":
                    generateCustomerReport(); break;
                case "Admin Activity Report":
                    generateAdminActivityReport(); break;
            }
        }
    }

    private void generateInventoryReport() {
        currentColumns = new String[]{"Item ID","Category", "Name", "Quantity", "Buy Price", "Sell Price"};
        currentTitle = "Inventory Report";
        String query = "SELECT item_id, category ,item_name, quantity, buy_price, sell_price FROM current_inventory ORDER BY name ASC";
        populateTable(query, currentColumns);
    }

    private void generateSalesReport() {
        currentColumns = new String[]{"Item ID", "Item Name", "Total Sold", "Total Earned"};
        currentTitle = "Sales Report";
        String query = "SELECT item_id, item_name, SUM(quantity) as total_sold, SUM(quantity * price) as total_earned " +
                "FROM order_items GROUP BY item_id, item_name ORDER BY total_sold DESC";
        populateTable(query, currentColumns);
    }

    private void generateJobReport() {
        currentColumns = new String[]{"Job ID", "Customer", "Phone", "Job Fee", "Item Cost", "Total", "Status", "Completed Date"};
        currentTitle = "Job Report";
        String query = "SELECT id, customer_name, mobile, fee, item_total, total_cost, status, completed_date " +
                "FROM jobs ORDER BY completed_date DESC";
        populateTable(query, currentColumns);
    }

    private void generateCustomerReport() {
        currentColumns = new String[]{"Customer ID", "Name", "Phone", "Jobs Done", "Total Spent"};
        currentTitle = "Customer Report";
        String query = "SELECT c.id, c.name, c.phone, COUNT(j.id) AS jobs_done, SUM(j.total_cost) AS total_spent " +
                "FROM customers c LEFT JOIN jobs j ON c.id = j.customer_id " +
                "GROUP BY c.id, c.name, c.phone ORDER BY total_spent DESC";
        populateTable(query, currentColumns);
    }

    private void generateAdminActivityReport() {
        currentColumns = new String[]{"Admin Username", "Action", "Description", "Timestamp"};
        currentTitle = "Admin Activity Report";
        String query = "SELECT username, action, description, timestamp FROM admin_logs ORDER BY timestamp DESC";
        populateTable(query, currentColumns);
    }

    private void populateTable(String query, String[] columnNames) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            for (String col : columnNames) {
                tableModel.addColumn(col);
            }

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnNames.length; i++) {
                    row.add(rs.getObject(i));
                }
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReportToPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Please generate a report first.");
            return;
        }

        try {
            File dir = new File("Reports");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "Reports/" + currentTitle.replace(" ", "_") + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph(currentTitle + "\n\n", titleFont));

            PdfPTable pdfTable = new PdfPTable(currentColumns.length);
            pdfTable.setWidthPercentage(100);

            for (String column : currentColumns) {
                PdfPCell header = new PdfPCell();
                header.setPhrase(new Phrase(column));
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(header);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    pdfTable.addCell(String.valueOf(tableModel.getValueAt(row, col)));
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this, "Report exported to: " + fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to export PDF: " + ex.getMessage());
        }
    }
} // End of ReportPanel
