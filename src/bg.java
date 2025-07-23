import javax.swing.*;
import java.awt.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
        setLayout(new GridBagLayout()); // Allow GridBagLayout inside
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw background image scaled
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
