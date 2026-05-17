package roadwatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;


 //MainLanding – Home screen.
 //Three entry points: Citizen Portal (login/register), Guest Submit, Admin Login.
 
public class MainLanding extends JFrame {

    public MainLanding() {
        setTitle("ROADWATCH — DPWH Infrastructure Damage Reporting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(UITheme.BG_LIGHT);
                g2.fillRect(340, 0, getWidth() - 340, getHeight());
                GradientPaint gp = new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                                     0, getHeight(), UITheme.DPWH_BLUE_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, 340, getHeight());
            }
        };

        //  LEFT — branding 
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(44, 30, 30, 30));
        left.setPreferredSize(new Dimension(340, 600));

        JLabel logoIcon = new JLabel(createRoadIcon(), SwingConstants.CENTER);
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("ROADWATCH");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        appName.setForeground(UITheme.DPWH_YELLOW);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel(
            "<html><center>DPWH Public Infrastructure<br>Damage Reporting System</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(0xB8CCE0));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x3A6FA0));
        sep.setMaximumSize(new Dimension(240, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("v2.0  |  Academic Project");
        version.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        version.setForeground(new Color(0x7A9CC0));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(logoIcon);
        left.add(Box.createVerticalStrut(14));
        left.add(appName);
        left.add(Box.createVerticalStrut(8));
        left.add(tagline);
        left.add(Box.createVerticalStrut(18));
        left.add(sep);
        left.add(Box.createVerticalStrut(10));
        left.add(version);
        left.add(Box.createVerticalGlue());

        String[] members = {
            "Narco D. Villando Jr",
            "John Jefferson E. Lacson",
            "Jimuel B. Mupra"
        };
        JLabel membersTitle = new JLabel("GROUP MEMBERS");
        membersTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        membersTitle.setForeground(new Color(0x7A9CC0));
        membersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(membersTitle);
        left.add(Box.createVerticalStrut(6));
        for (String m : members) {
            JLabel ml = new JLabel("• " + m);
            ml.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            ml.setForeground(new Color(0xA0C0D8));
            ml.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(ml);
            left.add(Box.createVerticalStrut(3));
        }

        //  RIGHT  cards 
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(40, 44, 30, 44));

        JLabel welcome = new JLabel("Welcome to ROADWATCH");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(UITheme.TEXT_DARK);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("How would you like to proceed?");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        right.add(welcome);
        right.add(Box.createVerticalStrut(6));
        right.add(sub);
        right.add(Box.createVerticalStrut(30));

        // Card 1: Citizen Portal (login/register)
        right.add(buildCard(
            "👤  Citizen Portal",
            "Sign in or create a free account to file reports\nand track their status anytime.",
            UITheme.DPWH_BLUE,
            e -> new CitizenAuthFrame(this).setVisible(true)
        ));
        right.add(Box.createVerticalStrut(14));

        // Card 2: Guest Submit
        right.add(buildCard(
            "📋  Submit as Guest",
            "Report an infrastructure hazard without an account.\nYou won't be able to track it later.",
            new Color(0x5D7A8A),
            e -> new CitizenReportFrame(null, null).setVisible(true)
        ));
        right.add(Box.createVerticalStrut(14));

        // Card 3: Admin Login
        right.add(buildCard(
            "🔐  Admin Login",
            "DPWH administrators can review, update,\nand manage all submitted reports.",
            UITheme.DPWH_BLUE_DARK,
            e -> new LoginFrame(this).setVisible(true)
        ));

        right.add(Box.createVerticalGlue());

        JLabel footer = new JLabel(
            "Department of Public Works and Highways (DPWH) — Academic Simulation");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footer.setForeground(UITheme.TEXT_MUTED);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(footer);

        root.add(left,  BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
    }

    // Card builder 

    private JPanel buildCard(String title, String body, Color accent, ActionListener action) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Double(0, 0, 6, getHeight(), 6, 6));
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(UITheme.TEXT_DARK);

        JLabel bodyLbl = new JLabel("<html>" + body.replace("\n", "<br>") + "</html>");
        bodyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bodyLbl.setForeground(UITheme.TEXT_MUTED);

        JButton btn = new JButton("Open →");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(accent);
        btn.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(bodyLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(btn);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 1, true),
                    BorderFactory.createEmptyBorder(13, 19, 13, 15)));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 16));
            }
        });
        return card;
    }

    // Logo icon 

    private ImageIcon createRoadIcon() {
        int size = 90;
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UITheme.DPWH_YELLOW);
        g2.fillOval(5, 5, size - 10, size - 10);
        g2.setColor(UITheme.DPWH_BLUE);
        g2.fillOval(12, 12, size - 24, size - 24);
        g2.setColor(UITheme.DPWH_YELLOW);
        int[] rx = { 35, 55, 65, 25 };
        int[] ry = { 30, 30, 65, 65 };
        g2.fillPolygon(rx, ry, 4);
        g2.setColor(UITheme.DPWH_BLUE);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     10, new float[]{5, 4}, 0));
        g2.drawLine(45, 33, 45, 63);
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(Color.WHITE);
        int[] tx = { 45, 62, 28 }; int[] ty = { 18, 45, 45 };
        g2.drawPolygon(tx, ty, 3);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString("!", 41, 42);
        g2.dispose();
        return new ImageIcon(img);
    }
}
