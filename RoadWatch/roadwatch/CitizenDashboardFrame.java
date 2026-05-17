package roadwatch;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;


 //CitizenDashboardFrame – Personal dashboard for a logged-in citizen.
 //Shows all their reports with live status, and lets them file new ones.
 
public class CitizenDashboardFrame extends JFrame {

    private final Citizen citizen;
    private final DamageReportDAO reportDAO = new DamageReportDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            lblCount;

    private static final String[] COLUMNS = {
        "ID", "Report Code", "Damage Type", "Location", "Severity", "Status", "Date Submitted", "Admin Remarks"
    };

    public CitizenDashboardFrame(Citizen citizen) {
        this.citizen = citizen;
        setTitle("ROADWATCH — My Reports  |  " + citizen.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 580);
        setLocationRelativeTo(null);
        buildUI();
        loadMyReports();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildToolbar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // Header 

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                              getWidth(), 0, UITheme.DPWH_BLUE_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.DPWH_YELLOW);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setPreferredSize(new Dimension(960, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("📋  My Damage Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Track the status of all your submitted infrastructure reports");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xB8CCE0));
        left.add(title); left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.setOpaque(false);

        JLabel userLabel = new JLabel("👤  " + citizen.getFullName());
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(UITheme.DPWH_YELLOW);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(0xC0392B));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { dispose(); new MainLanding().setVisible(true); });

        right.add(userLabel); right.add(btnLogout);
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    //Center table

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(UITheme.BG_LIGHT);
        center.setBorder(BorderFactory.createEmptyBorder(14, 18, 8, 18));

        // Filter / count bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setBackground(UITheme.BG_LIGHT);

        lblCount = new JLabel("Loading...");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(UITheme.TEXT_MUTED);

        JButton btnRefresh = UITheme.primaryButton("⟳  Refresh");
        btnRefresh.setPreferredSize(new Dimension(110, 30));
        btnRefresh.addActionListener(e -> loadMyReports());

        topBar.add(lblCount);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(btnRefresh);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setBackground(UITheme.BG_LIGHT);
        legend.add(legendDot("Pending",     UITheme.PENDING_COLOR));
        legend.add(legendDot("In Progress", UITheme.INPROG_COLOR));
        legend.add(legendDot("Resolved",    UITheme.RESOLVED_COLOR));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UITheme.BG_LIGHT);
        headerBar.add(topBar,  BorderLayout.WEST);
        headerBar.add(legend,  BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_TABLE);
        table.setRowHeight(30);
        table.setGridColor(new Color(0xE0E8F0));
        table.setSelectionBackground(new Color(0xD0E4F5));
        table.setSelectionForeground(UITheme.TEXT_DARK);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.getTableHeader().setBackground(UITheme.DPWH_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Column widths
        int[] w = { 0, 160, 120, 170, 70, 100, 130, 180 };
        for (int i = 0; i < w.length; i++)
            if (w[i] > 0) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Custom renderers
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.setDefaultRenderer(Object.class, new AlternatingRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xCCD5E0)));
        scroll.getViewport().setBackground(Color.WHITE);

        center.add(headerBar, BorderLayout.NORTH);
        center.add(scroll,    BorderLayout.CENTER);
        return center;
    }

    // Toolbar 

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE3EC)));

        JButton btnNew    = UITheme.primaryButton(" File New Report");
        JButton btnView   = UITheme.accentButton("View Details");

        btnNew.addActionListener(e ->
            new CitizenReportFrame(citizen, this).setVisible(true));

        btnView.addActionListener(e -> viewSelectedReport());

        bar.add(btnNew);
        bar.add(btnView);
        return bar;
    }

    // Data loading 

    public void loadMyReports() {
        tableModel.setRowCount(0);
        List<DamageReport> reports = reportDAO.getByCitizenId(citizen.getId());
        for (DamageReport r : reports) {
            tableModel.addRow(new Object[]{
                r.getId(),
                r.getReportCode(),
                r.getDamageType(),
                r.getLocation(),
                r.getSeverity(),
                r.getStatus(),
                r.getSubmittedAt() != null
                    ? r.getSubmittedAt().toString().substring(0, 16) : "",
                r.getAdminRemarks() != null ? r.getAdminRemarks() : ""
            });
        }
        int total    = reports.size();
        long pending  = reports.stream().filter(r -> "Pending".equals(r.getStatus())).count();
        long inprog   = reports.stream().filter(r -> "In Progress".equals(r.getStatus())).count();
        long resolved = reports.stream().filter(r -> "Resolved".equals(r.getStatus())).count();
        lblCount.setText(total + " report(s)  —  "
            + pending + " Pending  |  " + inprog + " In Progress  |  " + resolved + " Resolved");
    }

    // View details 

    private void viewSelectedReport() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        DamageReport r = reportDAO.getById(id);
        if (r != null) showDetailDialog(r);
    }

    private void showDetailDialog(DamageReport r) {
        JDialog dlg = new JDialog(this, "Report Detail — " + r.getReportCode(), true);
        dlg.setSize(560, 620);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel hdr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                              0, getHeight(), UITheme.DPWH_BLUE_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.statusColor(r.getStatus()));
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        hdr.setPreferredSize(new Dimension(560, 80));
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setBorder(BorderFactory.createEmptyBorder(16, 22, 12, 22));

        JLabel codeL = new JLabel(r.getReportCode());
        codeL.setFont(new Font("Segoe UI", Font.BOLD, 20));
        codeL.setForeground(UITheme.DPWH_YELLOW);
        codeL.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Status badge
        JLabel statusL = new JLabel("  " + r.getStatus() + "  ");
        statusL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusL.setForeground(Color.WHITE);
        statusL.setOpaque(true);
        statusL.setBackground(UITheme.statusColor(r.getStatus()));
        statusL.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        statusL.setAlignmentX(Component.LEFT_ALIGNMENT);

        hdr.add(codeL);
        hdr.add(Box.createVerticalStrut(6));
        hdr.add(statusL);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_LIGHT);
        body.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // Info card
        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(UITheme.CARD_WHITE);
        info.setBorder(UITheme.panelBorder("Report Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        String[][] fields = {
            { "Damage Type",    r.getDamageType() },
            { "Location",       r.getLocation() },
            { "Severity",       r.getSeverity() },
            { "Date Submitted", r.getSubmittedAt() != null ? r.getSubmittedAt().toString().substring(0,16) : "N/A" },
            { "Last Updated",   r.getUpdatedAt()   != null ? r.getUpdatedAt().toString().substring(0,16)   : "N/A" },
        };
        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = i; gbc.gridx = 0; gbc.weightx = 0;
            JLabel lbl = new JLabel(fields[i][0] + ":"); lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.TEXT_DARK); lbl.setPreferredSize(new Dimension(130, 22));
            info.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            JLabel val = new JLabel(fields[i][1]); val.setFont(UITheme.FONT_LABEL);
            val.setForeground(UITheme.TEXT_MUTED);
            info.add(val, gbc);
        }
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Description
        JPanel descP = new JPanel(new BorderLayout());
        descP.setBorder(UITheme.panelBorder("Your Description"));
        descP.setBackground(UITheme.CARD_WHITE);
        descP.setAlignmentX(Component.LEFT_ALIGNMENT);
        descP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JTextArea taDesc = new JTextArea(r.getDescription() != null ? r.getDescription() : "(none)");
        taDesc.setEditable(false); taDesc.setLineWrap(true); taDesc.setWrapStyleWord(true);
        taDesc.setBackground(UITheme.CARD_WHITE); taDesc.setFont(UITheme.FONT_LABEL);
        taDesc.setForeground(UITheme.TEXT_MUTED); taDesc.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
        descP.add(taDesc, BorderLayout.CENTER);

        // Admin remarks
        JPanel remP = new JPanel(new BorderLayout());
        remP.setBorder(UITheme.panelBorder("DPWH Admin Remarks"));
        remP.setBackground(UITheme.CARD_WHITE);
        remP.setAlignmentX(Component.LEFT_ALIGNMENT);
        remP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        String remarksTxt = (r.getAdminRemarks() != null && !r.getAdminRemarks().isEmpty())
            ? r.getAdminRemarks() : "(No remarks yet — DPWH will update this when your report is reviewed.)";
        JTextArea taRem = new JTextArea(remarksTxt);
        taRem.setEditable(false); taRem.setLineWrap(true); taRem.setWrapStyleWord(true);
        taRem.setBackground(UITheme.CARD_WHITE); taRem.setFont(UITheme.FONT_LABEL);
        taRem.setForeground(r.getAdminRemarks() != null ? UITheme.TEXT_DARK : UITheme.TEXT_MUTED);
        taRem.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
        remP.add(taRem, BorderLayout.CENTER);

        // Photo preview
        if (r.getPhoto() != null && r.getPhoto().length > 0) {
            JPanel photoP = new JPanel(new BorderLayout(0, 6));
            photoP.setBorder(UITheme.panelBorder("Attached Photo"));
            photoP.setBackground(UITheme.CARD_WHITE);
            photoP.setAlignmentX(Component.LEFT_ALIGNMENT);
            photoP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
            ImageIcon raw = new ImageIcon(r.getPhoto());
            Image scaled  = raw.getImage().getScaledInstance(480, 150, Image.SCALE_SMOOTH);
            JLabel photoLbl = new JLabel(new ImageIcon(scaled));
            photoLbl.setHorizontalAlignment(SwingConstants.CENTER);
            photoP.add(photoLbl, BorderLayout.CENTER);
            body.add(info);
            body.add(Box.createVerticalStrut(10));
            body.add(descP);
            body.add(Box.createVerticalStrut(10));
            body.add(photoP);
            body.add(Box.createVerticalStrut(10));
            body.add(remP);
        } else {
            body.add(info);
            body.add(Box.createVerticalStrut(10));
            body.add(descP);
            body.add(Box.createVerticalStrut(10));
            body.add(remP);
        }

        JButton btnClose = UITheme.primaryButton("Close");
        btnClose.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnClose.addActionListener(e -> dlg.dispose());
        body.add(Box.createVerticalStrut(12));
        body.add(btnClose);

        root.add(hdr,  BorderLayout.NORTH);
        root.add(new JScrollPane(body) {{ setBorder(BorderFactory.createEmptyBorder()); }},
                 BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // Helpers 

    private JLabel legendDot(String label, Color color) {
        JLabel l = new JLabel("●  " + label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(color);
        return l;
    }

    //Cell renderers 

    static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String status = v != null ? v.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            Color c = UITheme.statusColor(status);
            setForeground(sel ? Color.WHITE : c);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3),
                BorderFactory.createLineBorder(sel ? c.darker() : c, 1, true)));
            setBackground(sel ? c.darker() : new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            return this;
        }
    }

    static class AlternatingRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF0F5FB));
                setForeground(UITheme.TEXT_DARK);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setFont(UITheme.FONT_TABLE);
            return this;
        }
    }
}
