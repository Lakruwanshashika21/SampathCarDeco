import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Sampath Car Deco Shop System");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel logoLabel = new JLabel(new ImageIcon("image/logo.png")); // Put your image at image/logo.png
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(logoLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login");

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(emailLabel, gbc);
        gbc.gridy++;
        formPanel.add(emailField, gbc);
        gbc.gridy++;
        formPanel.add(passLabel, gbc);
        gbc.gridy++;
        formPanel.add(passwordField, gbc);
        gbc.gridy++;
        formPanel.add(loginBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    

    private void login() {
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();

    User user = Authenticator.authenticate(email, password);
    if (user != null) {
        System.out.println("✅ Login successful: " + user.getEmail());
        new MainAppFrame(user);
        dispose();
    } else {
        System.out.println("❌ Login failed for: " + email);
        JOptionPane.showMessageDialog(this, "Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}

}
