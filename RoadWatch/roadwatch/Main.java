package roadwatch;

import javax.swing.SwingUtilities;

//Main – Application entry point for ROADWATCH.

public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new MainLanding().setVisible(true));
    }
}
