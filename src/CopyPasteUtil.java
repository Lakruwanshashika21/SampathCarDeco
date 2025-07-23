import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class CopyPasteUtil {

    public static void enableFor(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof JTextComponent textComponent) {
                addPopupMenu(textComponent);
            } else if (component instanceof Container container) {
                enableFor(container); // recurse into child panels
            }
        }
    }

    private static void addPopupMenu(JTextComponent textComponent) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");

        cut.addActionListener(e -> textComponent.cut());
        copy.addActionListener(e -> textComponent.copy());
        paste.addActionListener(e -> textComponent.paste());

        popup.add(cut);
        popup.add(copy);
        popup.add(paste);

        textComponent.setComponentPopupMenu(popup);
    }
}
