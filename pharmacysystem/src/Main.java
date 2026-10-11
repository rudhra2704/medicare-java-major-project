import javax.swing.SwingUtilities;

/** Start here:  java Main */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UI.install();
            PharmacySystem store = new PharmacySystem();
            new PharmacyGUI(store).setVisible(true);
        });
    }
}
