package roadwatch;

import java.awt.*;
import javax.swing.*;


 //ReportDetailFrame – Full report detail for admin view/edit.
//v2: includes photo preview if attached.
 
public class ReportDetailFrame extends JDialog {

    private final DamageReport       report;
    private final DamageReportDAO    reportDAO;
    private final AdminDashboardFrame dashboard;

    private JComboBox<String> cbStatus;
    private JTextArea         taRemarks;

    public ReportDetailFrame(DamageReport report, DamageReportDAO reportDAO,
                             AdminDashboardFrame dashboard) {
        super(dashboard, "Report Detail — " + report.getReportCode(), true);
        this.report    = report;
        this.reportDAO = reportDAO;
        this.dashboard = dashboard;
        setSize(640, 720);
        setLocationRelativeTo(dashboard);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(new JScrollPane(buildBody()) {{
            setBorder(BorderFactory.createEmptyBorder());
            getVerticalScrollBar().setUnitIncrement(16);
        }}, BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                              0, getHeight(), UITheme.DPWH_BLUE_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.statusColor(report.getStatus()));
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setPreferredSize(new Dimension(640, 90));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));

        JLabel code = new JLabel(report.getReportCode());
        code.setFont(new Font("Segoe UI", Font.BOLD, 22));
        code.setForeground(UITheme.DPWH_YELLOW);
        code.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel info = new JLabel(report.getDamageType() + "  •  " + report.getLocation()
            + "  •  " + (report.getCitizenId() > 0 ? "Registered Citizen" : "Guest Submission"));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(new Color(0xB8CCE0));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(code);
        header.add(Box.createVerticalStrut(4));
        header.add(info);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setBackground(UITheme.BG_LIGHT);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        // Info grid 
        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(UITheme.panelBorder("Report Information"));
        info.setBackground(UITheme.CARD_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        String[][] fields = {
            { "Report Code",    report.getReportCode() },
            { "Submitted By",   report.getCitizenId() > 0 ? "Registered Citizen (ID: " + report.getCitizenId() + ")" : "Guest" },
            { "Reporter Name",  report.getReporterName() },
            { "Contact Number", report.getContactNumber() },
            { "Damage Type",    report.getDamageType() },
            { "Location",       report.getLocation() },
            { "Severity",       report.getSeverity() },
            { "Date Submitted", report.getSubmittedAt() != null
                                  ? report.getSubmittedAt().toString().substring(0, 16) : "N/A" },
            { "Last Updated",   report.getUpdatedAt() != null
                                  ? report.getUpdatedAt().toString().substring(0, 16) : "N/A" },
        };
        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = i; gbc.gridx = 0; gbc.weightx = 0;
            JLabel lbl = new JLabel(fields[i][0] + ":");
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.TEXT_DARK);
            lbl.setPreferredSize(new Dimension(140, 24));
            info.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            JLabel val = new JLabel(fields[i][1]);
            val.setFont(UITheme.FONT_LABEL);
            val.setForeground("Severity".equals(fields[i][0])
                ? severityColor(report.getSeverity()) : UITheme.TEXT_MUTED);
            if ("Severity".equals(fields[i][0])) val.setFont(UITheme.FONT_BOLD);
            info.add(val, gbc);
        }
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));

        //  Description 
        JPanel descP = new JPanel(new BorderLayout());
        descP.setBorder(UITheme.panelBorder("Citizen Description"));
        descP.setBackground(UITheme.CARD_WHITE);
        descP.setAlignmentX(Component.LEFT_ALIGNMENT);
        descP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JTextArea taDesc = new JTextArea(
            report.getDescription() != null && !report.getDescription().isEmpty()
            ? report.getDescription() : "(No description provided)");
        taDesc.setEditable(false); taDesc.setLineWrap(true); taDesc.setWrapStyleWord(true);
        taDesc.setBackground(UITheme.CARD_WHITE); taDesc.setFont(UITheme.FONT_LABEL);
        taDesc.setForeground(UITheme.TEXT_MUTED);
        taDesc.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        descP.add(new JScrollPane(taDesc) {{ setBorder(BorderFactory.createEmptyBorder()); }},
                  BorderLayout.CENTER);

        //  Photo preview 
        JPanel photoSection = null;
        if (report.getPhoto() != null && report.getPhoto().length > 0) {
            photoSection = new JPanel(new BorderLayout(0, 6));
            photoSection.setBorder(UITheme.panelBorder("Attached Photo"));
            photoSection.setBackground(UITheme.CARD_WHITE);
            photoSection.setAlignmentX(Component.LEFT_ALIGNMENT);
            photoSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            ImageIcon raw = new ImageIcon(report.getPhoto());
            Image scaled  = raw.getImage().getScaledInstance(560, 180, Image.SCALE_SMOOTH);
            JLabel photoLbl = new JLabel(new ImageIcon(scaled));
            photoLbl.setHorizontalAlignment(SwingConstants.CENTER);
            photoSection.add(photoLbl, BorderLayout.CENTER);

            JLabel sizeInfo = new JLabel(
                "  Photo size: " + (report.getPhoto().length / 1024) + " KB");
            sizeInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            sizeInfo.setForeground(UITheme.TEXT_MUTED);
            photoSection.add(sizeInfo, BorderLayout.SOUTH);
        }

        //  Admin update section 
        JPanel adminP = new JPanel(new GridBagLayout());
        adminP.setBorder(UITheme.panelBorder("Admin Update"));
        adminP.setBackground(UITheme.CARD_WHITE);
        adminP.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        GridBagConstraints ag = new GridBagConstraints();
        ag.insets = new Insets(6, 10, 6, 10);
        ag.fill   = GridBagConstraints.HORIZONTAL;
        ag.anchor = GridBagConstraints.WEST;

        ag.gridy = 0; ag.gridx = 0; ag.weightx = 0;
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BOLD); lblStatus.setForeground(UITheme.TEXT_DARK);
        lblStatus.setPreferredSize(new Dimension(120, 28));
        adminP.add(lblStatus, ag);

        ag.gridx = 1; ag.weightx = 1;
        cbStatus = UITheme.styledCombo(new String[]{"Pending", "In Progress", "Resolved"});
        cbStatus.setSelectedItem(report.getStatus());
        adminP.add(cbStatus, ag);

        ag.gridy = 1; ag.gridx = 0; ag.weightx = 0; ag.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblRemarks = new JLabel("Remarks:");
        lblRemarks.setFont(UITheme.FONT_BOLD); lblRemarks.setForeground(UITheme.TEXT_DARK);
        adminP.add(lblRemarks, ag);

        ag.gridx = 1; ag.weightx = 1;
        taRemarks = UITheme.styledArea(3, 30);
        taRemarks.setText(report.getAdminRemarks() != null ? report.getAdminRemarks() : "");
        JScrollPane remScroll = new JScrollPane(taRemarks);
        remScroll.setPreferredSize(new Dimension(360, 70));
        remScroll.setBorder(BorderFactory.createLineBorder(new Color(0xCCD5E0)));
        adminP.add(remScroll, ag);

        // Assemble body
        body.add(info);
        body.add(Box.createVerticalStrut(12));
        body.add(descP);
        if (photoSection != null) {
            body.add(Box.createVerticalStrut(12));
            body.add(photoSection);
        }
        body.add(Box.createVerticalStrut(12));
        body.add(adminP);
        return body;
    }

    private JPanel buildActions() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE3EC)));

        JButton btnCancel = new JButton("Close");
        btnCancel.setFont(UITheme.FONT_LABEL);
        btnCancel.setForeground(UITheme.TEXT_MUTED);
        btnCancel.setBackground(new Color(0xECEFF4));
        btnCancel.setBorderPainted(false); btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UITheme.primaryButton("💾  Save Changes");
        btnSave.addActionListener(e -> saveChanges());

        bar.add(btnCancel);
        bar.add(btnSave);
        return bar;
    }

    private void saveChanges() {
        String newStatus  = (String) cbStatus.getSelectedItem();
        String newRemarks = taRemarks.getText().trim();
        if (reportDAO.updateStatus(report.getId(), newStatus, newRemarks)) {
            JOptionPane.showMessageDialog(this, "Report updated successfully.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
            dashboard.loadReports("ALL");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save. Check DB connection.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color severityColor(String s) {
        return switch (s) {
            case "High"   -> UITheme.DANGER_RED;
            case "Medium" -> UITheme.WARNING_ORANGE;
            default       -> UITheme.SUCCESS_GREEN;
        };
    }
}
