package roadwatch;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

 //UITheme – Centralized GUI styling constants and helper methods.
 //DPWH-inspired color palette: deep blue + amber yellow.
 
public class UITheme {

    // ── Colors ─────────────────────────────────────────────────────────────
    public static final Color DPWH_BLUE      = new Color(0x1B4F8A);
    public static final Color DPWH_BLUE_DARK = new Color(0x123666);
    public static final Color DPWH_YELLOW    = new Color(0xFDB913);
    public static final Color BG_LIGHT       = new Color(0xF4F6FA);
    public static final Color CARD_WHITE     = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(0x1C2B3A);
    public static final Color TEXT_MUTED     = new Color(0x6B7A8D);
    public static final Color SUCCESS_GREEN  = new Color(0x27AE60);
    public static final Color WARNING_ORANGE = new Color(0xE67E22);
    public static final Color DANGER_RED     = new Color(0xE74C3C);
    public static final Color PENDING_COLOR  = new Color(0xE67E22);
    public static final Color INPROG_COLOR   = new Color(0x2980B9);
    public static final Color RESOLVED_COLOR = new Color(0x27AE60);

    // ── Fonts ──────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TABLE  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,  13);

    // ── Borders ────────────────────────────────────────────────────────────
    public static Border panelBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(DPWH_BLUE, 1, true),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_BOLD, DPWH_BLUE
        );
    }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCD5E0), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        );
    }

    // ── Button factory ─────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DPWH_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    public static JButton accentButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DPWH_YELLOW);
        btn.setForeground(DPWH_BLUE_DARK);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER_RED);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    /** Styled text field */
    public static JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_LABEL);
        tf.setBorder(fieldBorder());
        return tf;
    }

    /** Styled password field */
    public static JPasswordField styledPassword(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(FONT_LABEL);
        pf.setBorder(fieldBorder());
        return pf;
    }

    /** Styled combo box */
    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_LABEL);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    /** Styled text area */
    public static JTextArea styledArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_LABEL);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return ta;
    }

    /** Colored status badge label */
    public static Color statusColor(String status) {
        return switch (status) {
            case "Pending"     -> PENDING_COLOR;
            case "In Progress" -> INPROG_COLOR;
            case "Resolved"    -> RESOLVED_COLOR;
            default            -> TEXT_MUTED;
        };
    }

    /** Apply global Nimbus UI theme tweaks */
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase",           DPWH_BLUE);
            UIManager.put("nimbusBlueGrey",       new Color(0xA8B8CC));
            UIManager.put("control",              BG_LIGHT);
            UIManager.put("Table.alternateRowColor", new Color(0xEBF0F7));
        } catch (Exception e) {
            // Fallback to default L&F
        }
    }
}
