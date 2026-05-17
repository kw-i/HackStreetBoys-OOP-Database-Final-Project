package roadwatch;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;


 //AdminDashboardFrame – Admin panel showing all reports, with filter, status update, delete, and summary.
 
public class AdminDashboardFrame extends JFrame {

    private final Admin admin;
    private final DamageReportDAO reportDAO = new DamageReportDAO();

    private JTable  table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbFilter;
    private JLabel lblCount;

    private static final String[] COLUMNS = {
        "ID", "Report Code", "Reporter", "Damage Type", "Location", "Severity", "Status", "Date Submitted"
    };

    public AdminDashboardFrame(Admin admin) {
        this.admin = admin;
        setTitle("ROADWATCH — Admin Dashboard  |  Logged in as: " + admin.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        buildUI();
        loadReports("ALL");
    }

    //UI Construction

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildToolbar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                                     getWidth(), 0, UITheme.DPWH_BLUE_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.DPWH_YELLOW);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setPreferredSize(new Dimension(1100, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel title = new JLabel("🛣  ROADWATCH — Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Department of Public Works and Highways  |  Infrastructure Damage Management");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xB8CCE0));

        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.setOpaque(false);

        JLabel adminLabel = new JLabel("👤  " + admin.getFullName());
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminLabel.setForeground(UITheme.DPWH_YELLOW);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(0xC0392B));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            dispose();
            new MainLanding().setVisible(true);
        });

        right.add(adminLabel);
        right.add(btnLogout);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(UITheme.BG_LIGHT);
        center.setBorder(BorderFactory.createEmptyBorder(14, 18, 8, 18));

        // Filter bar 
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setBackground(UITheme.BG_LIGHT);

        JLabel filterLbl = new JLabel("Filter by Status:");
        filterLbl.setFont(UITheme.FONT_BOLD);
        filterLbl.setForeground(UITheme.TEXT_DARK);

        cbFilter = UITheme.styledCombo(new String[]{"ALL", "Pending", "In Progress", "Resolved"});
        cbFilter.setPreferredSize(new Dimension(160, 32));
        cbFilter.addActionListener(e ->
            loadReports((String) cbFilter.getSelectedItem()));

        JButton btnRefresh = UITheme.primaryButton("⟳  Refresh");
        btnRefresh.setPreferredSize(new Dimension(110, 32));
        btnRefresh.addActionListener(e -> loadReports((String) cbFilter.getSelectedItem()));

        lblCount = new JLabel("0 report(s) found");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(UITheme.TEXT_MUTED);

        filterBar.add(filterLbl);
        filterBar.add(cbFilter);
        filterBar.add(btnRefresh);
        filterBar.add(Box.createHorizontalStrut(20));
        filterBar.add(lblCount);

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
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column used internally
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Column widths
        int[] widths = { 0, 160, 130, 120, 170, 70, 100, 130 };
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] > 0)
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Status cell renderer colored badges
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        // Re-apply status renderer after setting default
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xCCD5E0)));
        scroll.getViewport().setBackground(Color.WHITE);

        center.add(filterBar, BorderLayout.NORTH);
        center.add(scroll,    BorderLayout.CENTER);
        return center;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE3EC)));

        JButton btnView    = UITheme.primaryButton("👁  View / Edit");
        JButton btnPending = new JButton("⏳  Set Pending");
        JButton btnInProg  = new JButton("🔧  Set In Progress");
        JButton btnResolve = UITheme.successButton("✔  Set Resolved");
        JButton btnDelete  = UITheme.dangerButton("🗑  Delete");
        JButton btnSummary = UITheme.accentButton("📊  Summary");

        styleToolBtn(btnPending, UITheme.PENDING_COLOR);
        styleToolBtn(btnInProg,  UITheme.INPROG_COLOR);

        btnView.addActionListener(e -> viewSelectedReport());
        btnPending.addActionListener(e -> quickUpdateStatus("Pending"));
        btnInProg.addActionListener(e -> quickUpdateStatus("In Progress"));
        btnResolve.addActionListener(e -> quickUpdateStatus("Resolved"));
        btnDelete.addActionListener(e -> deleteSelectedReport());
        btnSummary.addActionListener(e -> new SummaryFrame(reportDAO).setVisible(true));

        bar.add(btnView);
        bar.add(btnPending);
        bar.add(btnInProg);
        bar.add(btnResolve);
        bar.add(btnDelete);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(btnSummary);
        return bar;
    }

    //  Data loading 

    public void loadReports(String filter) {
        tableModel.setRowCount(0);
        List<DamageReport> reports = reportDAO.getByFilter(filter);
        for (DamageReport r : reports) {
            tableModel.addRow(new Object[]{
                r.getId(),
                r.getReportCode(),
                r.getReporterName(),
                r.getDamageType(),
                r.getLocation(),
                r.getSeverity(),
                r.getStatus(),
                r.getSubmittedAt() != null
                    ? r.getSubmittedAt().toString().substring(0, 16)
                    : ""
            });
        }
        lblCount.setText(reports.size() + " report(s) found");
    }

    //  Actions 

    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    private void viewSelectedReport() {
        int id = getSelectedId();
        if (id < 0) return;
        DamageReport r = reportDAO.getById(id);
        if (r != null) new ReportDetailFrame(r, reportDAO, this).setVisible(true);
    }

    private void quickUpdateStatus(String newStatus) {
        int id = getSelectedId();
        if (id < 0) return;

        String remarks = JOptionPane.showInputDialog(this,
            "Admin remarks for this status update (optional):",
            "Update Status to: " + newStatus,
            JOptionPane.PLAIN_MESSAGE);
        if (remarks == null) return; // cancelled

        if (reportDAO.updateStatus(id, newStatus, remarks)) {
            JOptionPane.showMessageDialog(this,
                "Status updated to: " + newStatus,
                "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadReports((String) cbFilter.getSelectedItem());
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedReport() {
        int id = getSelectedId();
        if (id < 0) return;
        int row = table.getSelectedRow();
        String code = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Are you sure you want to delete report <b>" + code + "</b>?<br>" +
            "This action cannot be undone.</html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (reportDAO.delete(id)) {
                JOptionPane.showMessageDialog(this, "Report deleted successfully.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadReports((String) cbFilter.getSelectedItem());
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //  Helpers 

    private void styleToolBtn(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 36));
    }

    //  Custom cell renderers 

    // Renders the Status column as a colored badge.
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String status = v != null ? v.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            Color c = UITheme.statusColor(status);
            setForeground(sel ? Color.WHITE : c);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3),
                BorderFactory.createLineBorder(sel ? c.darker() : c, 1, true)
            ));
            setBackground(sel ? c.darker() : new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            return this;
        }
    }

    // Alternating row colors for readability.
    static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
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
