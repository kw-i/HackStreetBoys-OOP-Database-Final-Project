package roadwatch;

import java.awt.*;
import javax.swing.*;

//LoginFrame – Admin authentication window.
 
public class LoginFrame extends JDialog {

    private final JFrame parent;
    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JLabel         lblError;

    private final AdminDAO adminDAO = new AdminDAO();

    public LoginFrame(JFrame parent) {
        super(parent, "ROADWATCH — Admin Login", true);
        this.parent = parent;
        setSize(420, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header bar 
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                                     0, getHeight(), UITheme.DPWH_BLUE_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(420, 110));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(22, 28, 18, 28));

        JLabel lockIcon = new JLabel("🔐", SwingConstants.CENTER);
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        lockIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("DPWH Admin Login");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Authorized personnel only");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(0xB8CCE0));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lockIcon);
        header.add(Box.createVerticalStrut(6));
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);

        //  Form panel 
        JPanel form = new JPanel();
        form.setBackground(UITheme.BG_LIGHT);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_DARK);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfUsername = UITheme.styledField(20);
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_DARK);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        pfPassword = UITheme.styledPassword(20);
        pfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error message
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(UITheme.DANGER_RED);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton btnLogin = UITheme.primaryButton("Login");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> attemptLogin());

        // Cancel
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(UITheme.FONT_LABEL);
        btnCancel.setForeground(UITheme.TEXT_MUTED);
        btnCancel.setBackground(UITheme.BG_LIGHT);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.addActionListener(e -> dispose());

        // Default button on Enter
        getRootPane().setDefaultButton(btnLogin);

        // Hint
        JLabel hint = new JLabel("admin  password: mupra");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lblUser);
        form.add(Box.createVerticalStrut(6));
        form.add(tfUsername);
        form.add(Box.createVerticalStrut(18));
        form.add(lblPass);
        form.add(Box.createVerticalStrut(6));
        form.add(pfPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(lblError);
        form.add(Box.createVerticalStrut(18));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(10));
        form.add(btnCancel);
        form.add(Box.createVerticalStrut(16));
        form.add(hint);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        setContentPane(root);
    }

    private void attemptLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        Admin admin = adminDAO.authenticate(username, password);
        if (admin != null) {
            dispose();
            new AdminDashboardFrame(admin).setVisible(true);
        } else {
            lblError.setText("Invalid username or password. Try again.");
            pfPassword.setText("");
        }
    }
}
