package roadwatch;

import javax.swing.*;
import java.awt.*;


public class CitizenAuthFrame extends JDialog {

    private final CitizenDAO citizenDAO = new CitizenDAO();

    // ── Login fields
    private JTextField     tfLoginEmail;
    private JPasswordField pfLoginPass;
    private JLabel         lblLoginErr;

    // ── Register fields
    private JTextField     tfRegName, tfRegEmail, tfRegContact;
    private JPasswordField pfRegPass, pfRegConfirm;
    private JLabel         lblRegErr;

    public CitizenAuthFrame(JFrame parent) {
        super(parent, "ROADWATCH — Citizen Portal", true);
        setSize(460, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        //  Header 
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                              0, getHeight(), UITheme.DPWH_BLUE_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.DPWH_YELLOW);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setPreferredSize(new Dimension(460, 80));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));

        JLabel title = new JLabel(" Citizen Portal ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in or create an account to track your reports.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xB8CCE0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // Tabs 
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(UITheme.BG_LIGHT);
        tabs.addTab("  Sign In  ",  buildLoginTab());
        tabs.addTab("  Register  ", buildRegisterTab());

        root.add(header, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        setContentPane(root);
    }

    // LOGIN TAB 

    private JPanel buildLoginTab() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_LIGHT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(28, 40, 20, 40));

        tfLoginEmail = UITheme.styledField(22);
        pfLoginPass  = UITheme.styledPassword(22);
        lblLoginErr  = new JLabel(" ");
        lblLoginErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoginErr.setForeground(UITheme.DANGER_RED);

        JButton btnLogin = UITheme.primaryButton("Sign In");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.addActionListener(e -> attemptLogin());

        JButton btnGuest = new JButton("Continue as Guest (no account)");
        btnGuest.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGuest.setForeground(UITheme.TEXT_MUTED);
        btnGuest.setBackground(UITheme.BG_LIGHT);
        btnGuest.setBorderPainted(false);
        btnGuest.setFocusPainted(false);
        btnGuest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGuest.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuest.addActionListener(e -> {
            dispose();
            new CitizenReportFrame(null, null).setVisible(true);
        });

        addFormRow(p, "Email Address", tfLoginEmail);
        p.add(Box.createVerticalStrut(16));
        addFormRow(p, "Password", pfLoginPass);
        p.add(Box.createVerticalStrut(8));
        p.add(lblLoginErr);
        p.add(Box.createVerticalStrut(16));
        p.add(btnLogin);
        p.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sep);
        p.add(Box.createVerticalStrut(12));

        JLabel orLabel = new JLabel("Don't have an account? Switch to the Register tab.");
        orLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        orLabel.setForeground(UITheme.TEXT_MUTED);
        orLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(orLabel);
        p.add(Box.createVerticalStrut(8));
        p.add(btnGuest);
        return p;
    }

    //  REGISTER TAB 

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_LIGHT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        tfRegName    = UITheme.styledField(22);
        tfRegEmail   = UITheme.styledField(22);
        tfRegContact = UITheme.styledField(22);
        pfRegPass    = UITheme.styledPassword(22);
        pfRegConfirm = UITheme.styledPassword(22);
        lblRegErr    = new JLabel(" ");
        lblRegErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRegErr.setForeground(UITheme.DANGER_RED);

        JButton btnReg = UITheme.successButton("Create Account");
        btnReg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnReg.addActionListener(e -> attemptRegister());

        addFormRow(p, "Full Name *",       tfRegName);    p.add(Box.createVerticalStrut(12));
        addFormRow(p, "Email Address *",   tfRegEmail);   p.add(Box.createVerticalStrut(12));
        addFormRow(p, "Contact Number *",  tfRegContact); p.add(Box.createVerticalStrut(12));
        addFormRow(p, "Password *",        pfRegPass);    p.add(Box.createVerticalStrut(12));
        addFormRow(p, "Confirm Password *",pfRegConfirm); p.add(Box.createVerticalStrut(8));
        p.add(lblRegErr);
        p.add(Box.createVerticalStrut(16));
        p.add(btnReg);
        return p;
    }

    // LOGIC 

    private void attemptLogin() {
        String email = tfLoginEmail.getText().trim();
        String pass  = new String(pfLoginPass.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            lblLoginErr.setText("Please enter email and password."); return;
        }
        Citizen c = citizenDAO.login(email, pass);
        if (c != null) {
            dispose();
            new CitizenDashboardFrame(c).setVisible(true);
        } else {
            lblLoginErr.setText("Invalid email or password.");
            pfLoginPass.setText("");
        }
    }

    private void attemptRegister() {
        String name    = tfRegName.getText().trim();
        String email   = tfRegEmail.getText().trim();
        String contact = tfRegContact.getText().trim();
        String pass    = new String(pfRegPass.getPassword());
        String confirm = new String(pfRegConfirm.getPassword());

        if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || pass.isEmpty()) {
            lblRegErr.setText("All fields are required."); return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            lblRegErr.setText("Please enter a valid email address."); return;
        }
        if (!contact.matches("[0-9+\\-\\s]{7,15}")) {
            lblRegErr.setText("Contact number appears invalid."); return;
        }
        if (pass.length() < 6) {
            lblRegErr.setText("Password must be at least 6 characters."); return;
        }
        if (!pass.equals(confirm)) {
            lblRegErr.setText("Passwords do not match."); return;
        }

        boolean ok = citizenDAO.register(name, email, contact, pass);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "<html><b style='color:#27AE60'>Account created!</b><br><br>"
                + "You can now sign in with:<br>"
                + "Email: <b>" + email + "</b></html>",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            // Auto-login
            Citizen c = citizenDAO.login(email, pass);
            if (c != null) { dispose(); new CitizenDashboardFrame(c).setVisible(true); }
        } else {
            lblRegErr.setText("Email already registered. Please sign in.");
        }
    }

    // Helper 

    private void addFormRow(JPanel p, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
    }
}
