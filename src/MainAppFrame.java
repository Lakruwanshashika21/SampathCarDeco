import javax.swing.*;
import java.awt.*;
import java.sql.Connection;





public class MainAppFrame extends JFrame {
    private User loggedInUser;
    private Connection conn;
    
    


    public MainAppFrame(User user) {
        this.loggedInUser = user;


        // Basic frame setup
        setTitle("Sampath Car Deco Shop System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Top Panel: Logged-in User Info ---
        // --- Top Panel: User Info + Log Out Button ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel loggedInLabel = new JLabel("Logged in as: " + user.getEmail() + " (" + user.getRole() + ")");
        loggedInLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loggedInLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loggedInLabel.setForeground(new Color(0, 102, 204));

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame();
            }
        });

        topPanel.add(loggedInLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);


        // --- Center Panel: Buttons ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0); // vertical spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create Buttons
        JButton btnAddAdmin = createButton("Admin Management");
        JButton btnAddCategory = createButton("Category");
        JButton btnAddProduct = createButton("Items");
        JButton btnInventory = createButton("Inventory");
        JButton btnCustomer = createButton("Customer");
        JButton btnOrders = createButton("Orders");
        JButton btnJobs = createButton("Jobs");
        JButton btnReports = createButton("Reports");

        int row = 0;

        // Superadmin-only button
        if (user.isSuperAdmin()) {
            gbc.gridy = row++;
            centerPanel.add(btnAddAdmin, gbc);
        }

        gbc.gridy = row++; centerPanel.add(btnAddCategory, gbc);
        gbc.gridy = row++; centerPanel.add(btnAddProduct, gbc);
        gbc.gridy = row++; centerPanel.add(btnInventory, gbc);
        gbc.gridy = row++; centerPanel.add(btnCustomer, gbc);
        gbc.gridy = row++; centerPanel.add(btnOrders, gbc);
        gbc.gridy = row++; centerPanel.add(btnJobs, gbc);

        if (user.isSuperAdmin()) {
            gbc.gridy = row++; centerPanel.add(btnReports, gbc);
        }

        currentCenterPanel = centerPanel;
        add(currentCenterPanel, BorderLayout.CENTER);


        // --- Action Listeners (Example only) ---
        btnInventory.addActionListener(e -> {
            InventoryPanel inventoryPanel = new InventoryPanel(() -> setContentPanel(centerPanel));
            setContentPanel(inventoryPanel);
        });                                 

        btnCustomer.addActionListener(e -> {
            CustomerPanel customerPanel = new CustomerPanel(() -> setContentPanel(centerPanel));
            setContentPanel(customerPanel);
        });

        btnOrders.addActionListener(e -> {
            OrderPanel orderPanel = new OrderPanel(() -> setContentPanel(centerPanel));
            setContentPanel(orderPanel);
        });


        btnAddAdmin.addActionListener(e -> {
            // TODO: Replace with actual Add Admin UI
            AdminManagementPanel adminManagementPanel = new AdminManagementPanel(() -> setContentPanel(centerPanel));
            setContentPanel(adminManagementPanel);
    
        });

        btnAddCategory.addActionListener(e -> {
            CategoryPanel addCategoryPanel = new CategoryPanel(() -> setContentPanel(centerPanel));
            setContentPanel(addCategoryPanel);
        });


        btnAddProduct.addActionListener(e -> {
            ProductPanel addProductPanel = new ProductPanel(() -> setContentPanel(centerPanel));
            setContentPanel(addProductPanel);
        });


        btnJobs.addActionListener(e -> {
            Runnable showMain = () -> setContentPanel(centerPanel);

            final Runnable[] showCustomerPanel = new Runnable[1]; // placeholder

            Runnable showJobPanel = () -> {
                JobPanel jobPanel = new JobPanel(showMain, showCustomerPanel[0]);
                setContentPanel(jobPanel);
            };

            showCustomerPanel[0] = () -> {
                CustomerPanel customerPanel = new CustomerPanel(showJobPanel);
                setContentPanel(customerPanel);
            };

            showJobPanel.run(); // initial load
        });

        btnReports.addActionListener(e -> {
            ReportPanel reportsPanel = new ReportPanel(conn, () -> setContentPanel(centerPanel));
            setContentPanel(reportsPanel);
        });


        // Show window
        setVisible(true);

        
    }

    private JPanel currentCenterPanel;

private void setContentPanel(JPanel panel) {
    if (currentCenterPanel != null) {
        remove(currentCenterPanel);
    }
    currentCenterPanel = panel;
    add(currentCenterPanel, BorderLayout.CENTER);
    revalidate();
    repaint();
}


    

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setBackground(new Color(59, 89, 152));
        button.setForeground(Color.WHITE);
        return button;
    }

    
}


