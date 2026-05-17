package roadwatch;

import java.awt.*;
import java.io.File; // this is for the file import for the image
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


 //CitizenReportFrame – Damage report submission form.
 //Supports guest mode (citizen=null) and logged-in citizens.
 //Includes optional photo attachment (max 5 MB, stored as BLOB).
 
public class CitizenReportFrame extends JFrame {

    private final Citizen               citizen;       // null = guest
    private final CitizenDashboardFrame dashboardRef;  // refresh after submit; null if guest

    private JTextField        tfName, tfContact, tfLocation;
    private JComboBox<String> cbDamageType, cbSeverity;
    private JTextArea         taDescription;
    private JLabel            lblStatus, lblPhotoName;
    private byte[]            selectedPhotoBytes; // null = no photo

    private final DamageReportDAO reportDAO = new DamageReportDAO();

    private static final String[] DAMAGE_TYPES = {
        "Pothole", "Cracked Road", "Damaged Bridge", "Broken Guardrail",
        "Flooded Road", "Damaged Sidewalk", "Collapsed Road", "Missing Road Sign", "Other"
    };
    private static final String[] SEVERITY_LEVELS = { "Low", "Medium", "High" };
    private static final long     MAX_PHOTO_BYTES  = 5 * 1024 * 1024; // 5 MB

    /** Guest submission. */
    public CitizenReportFrame(Citizen citizen, CitizenDashboardFrame dashboard) {
        this.citizen      = citizen;
        this.dashboardRef = dashboard;

        setTitle(citizen != null
            ? "ROADWATCH — File Report  [" + citizen.getFullName() + "]"
            : "ROADWATCH — Submit Report (Guest)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 740);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.add(buildHeader(),  BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // Header 

    private JPanel buildHeader() {
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
        header.setPreferredSize(new Dimension(650, 90));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 14, 28));

        JLabel title = new JLabel("Submit Infrastructure Damage Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        String subTxt = citizen != null
            ? "Logged in as " + citizen.getFullName() + " — report will be linked to your account."
            : "Guest mode — sign in to track your report's status.";
        JLabel sub = new JLabel(subTxt);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(citizen != null ? UITheme.DPWH_YELLOW : new Color(0xB8CCE0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        return header;
    }

    // Form 

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(UITheme.BG_LIGHT);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 35, 10, 35));

        //  Reporter Info 
        JPanel secReporter = new JPanel(new GridBagLayout());
        secReporter.setBorder(UITheme.panelBorder("Reporter Information"));
        secReporter.setBackground(UITheme.CARD_WHITE);
        GridBagConstraints gbc = defaultGBC();

        tfName    = UITheme.styledField(25);
        tfContact = UITheme.styledField(25);

        if (citizen != null) {
            tfName.setText(citizen.getFullName());
            tfContact.setText(citizen.getContactNumber());
            tfName.setEditable(false);    tfName.setBackground(new Color(0xF0F5FB));
            tfContact.setEditable(false); tfContact.setBackground(new Color(0xF0F5FB));
        }

        gbc.gridy = 0; addRow(secReporter, gbc, "Full Name *",      tfName);
        gbc.gridy = 1; addRow(secReporter, gbc, "Contact Number *", tfContact);
        secReporter.setAlignmentX(Component.LEFT_ALIGNMENT);
        secReporter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Damage Details 
        JPanel secDamage = new JPanel(new GridBagLayout());
        secDamage.setBorder(UITheme.panelBorder("Damage Details"));
        secDamage.setBackground(UITheme.CARD_WHITE);
        gbc = defaultGBC();

        cbDamageType = UITheme.styledCombo(DAMAGE_TYPES);
        cbSeverity   = UITheme.styledCombo(SEVERITY_LEVELS);
        tfLocation   = UITheme.styledField(25);

        gbc.gridy = 0; addRow(secDamage, gbc, "Damage Type *",        cbDamageType);
        gbc.gridy = 1; addRow(secDamage, gbc, "Severity Level *",     cbSeverity);
        gbc.gridy = 2; addRow(secDamage, gbc, "Location / Address *", tfLocation);
        secDamage.setAlignmentX(Component.LEFT_ALIGNMENT);
        secDamage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        // Description 
        JPanel secDesc = new JPanel(new BorderLayout(0, 6));
        secDesc.setBorder(UITheme.panelBorder("Description (Optional)"));
        secDesc.setBackground(UITheme.CARD_WHITE);

        taDescription = UITheme.styledArea(4, 40);
        JScrollPane descScroll = new JScrollPane(taDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(0xCCD5E0)));
        descScroll.setPreferredSize(new Dimension(500, 90));

        JLabel descHint = new JLabel("Describe the damage — size, exact spot, any accidents caused, etc.");
        descHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        descHint.setForeground(UITheme.TEXT_MUTED);

        secDesc.add(descHint,   BorderLayout.NORTH);
        secDesc.add(descScroll, BorderLayout.CENTER);
        secDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        secDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Photo Attachment Optional
        JPanel secPhoto = new JPanel(new GridBagLayout());
        secPhoto.setBorder(UITheme.panelBorder("Photo Attachment  (Optional — max 5 MB)"));
        secPhoto.setBackground(UITheme.CARD_WHITE);
        secPhoto.setAlignmentX(Component.LEFT_ALIGNMENT);
        secPhoto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        GridBagConstraints pg = new GridBagConstraints();
        pg.insets = new Insets(6, 10, 6, 10);
        pg.anchor = GridBagConstraints.WEST;
        pg.fill   = GridBagConstraints.HORIZONTAL;
        pg.gridy  = 0;

        JLabel photoHint = new JLabel("Attach a photo of the damage to help DPWH assess the issue faster.");
        photoHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        photoHint.setForeground(UITheme.TEXT_MUTED);
        pg.gridx = 0; pg.gridwidth = 2; pg.weightx = 1;
        secPhoto.add(photoHint, pg);

        pg.gridy = 1; pg.gridwidth = 1; pg.weightx = 0;
        JButton btnChoose = UITheme.accentButton(" Choose Photo");
        btnChoose.setPreferredSize(new Dimension(150, 34));
        secPhoto.add(btnChoose, pg);

        pg.gridx = 1; pg.weightx = 1;
        lblPhotoName = new JLabel("No photo selected.");
        lblPhotoName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPhotoName.setForeground(UITheme.TEXT_MUTED);
        secPhoto.add(lblPhotoName, pg);

        pg.gridy = 2; pg.gridx = 0; pg.gridwidth = 2;
        JButton btnClearPhoto = new JButton(" Remove Photo");
        btnClearPhoto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClearPhoto.setForeground(UITheme.DANGER_RED);
        btnClearPhoto.setBackground(UITheme.CARD_WHITE);
        btnClearPhoto.setBorderPainted(false);
        btnClearPhoto.setFocusPainted(false);
        btnClearPhoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        secPhoto.add(btnClearPhoto, pg);

        // Choose photo action
        btnChoose.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Damage Photo");
            fc.setFileFilter(new FileNameExtensionFilter(
                "Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f.length() > MAX_PHOTO_BYTES) {
                    JOptionPane.showMessageDialog(this,
                        "Photo is too large (max 5 MB). Please choose a smaller image.",
                        "File Too Large", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    selectedPhotoBytes = Files.readAllBytes(f.toPath());
                    lblPhotoName.setText("📎  " + f.getName()
                        + "  (" + (f.length() / 1024) + " KB)");
                    lblPhotoName.setForeground(UITheme.SUCCESS_GREEN);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Could not read the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClearPhoto.addActionListener(e -> {
            selectedPhotoBytes = null;
            lblPhotoName.setText("No photo selected.");
            lblPhotoName.setForeground(UITheme.TEXT_MUTED);
        });

        // Status label 
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(secReporter);
        form.add(Box.createVerticalStrut(16));
        form.add(secDamage);
        form.add(Box.createVerticalStrut(16));
        form.add(secDesc);
        form.add(Box.createVerticalStrut(16));
        form.add(secPhoto);
        form.add(Box.createVerticalStrut(12));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(10));
        return form;
    }

    // Bottom bar

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE3EC)));

        JButton btnClear  = UITheme.accentButton("Clear Form");
        JButton btnSubmit = UITheme.primaryButton("Submit Report");

        btnClear.addActionListener(e  -> clearForm());
        btnSubmit.addActionListener(e -> submitReport());

        bar.add(btnClear);
        bar.add(btnSubmit);
        return bar;
    }

    // Logic

    private void submitReport() {
        String name     = tfName.getText().trim();
        String contact  = tfContact.getText().trim();
        String location = tfLocation.getText().trim();
        String type     = (String) cbDamageType.getSelectedItem();
        String severity = (String) cbSeverity.getSelectedItem();
        String desc     = taDescription.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || location.isEmpty()) {
            showStatus("Please fill in all required fields (*).", UITheme.DANGER_RED);
            return;
        }
        if (!contact.matches("[0-9+\\-\\s]{7,15}")) {
            showStatus("Contact number appears invalid.", UITheme.DANGER_RED);
            return;
        }

        // Duplicate detection
        if (reportDAO.isDuplicate(location, type)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "<html><b>Possible duplicate detected.</b><br>"
                + "A <i>" + type + "</i> report for <i>" + location + "</i><br>"
                + "was already submitted in the last 48 hours.<br><br>"
                + "Do you still want to submit?</html>",
                "Duplicate Detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        DamageReport r = new DamageReport(name, contact, type, location, severity, desc);
        Integer citizenId = citizen != null ? citizen.getId() : null;
        String code = reportDAO.insertFull(r, citizenId, selectedPhotoBytes);

        if (code != null) {
            String trackNote = citizen != null
                ? "<br>You can track this report from your <b>My Reports</b> dashboard."
                : "<br><i>Tip: Create a free account to track your report's progress anytime!</i>";
            JOptionPane.showMessageDialog(this,
                "<html><b style='color:#27AE60;font-size:14px;'>Report Submitted!</b><br><br>"
                + "Your report code: <b>" + code + "</b>" + trackNote + "</html>",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            showStatus("Submitted successfully. Code: " + code, UITheme.SUCCESS_GREEN);
            if (dashboardRef != null) dashboardRef.loadMyReports();
        } else {
            showStatus("Submission failed. Check your database connection.", UITheme.DANGER_RED);
        }
    }

    private void clearForm() {
        if (citizen == null) { tfName.setText(""); tfContact.setText(""); }
        tfLocation.setText("");
        taDescription.setText("");
        cbDamageType.setSelectedIndex(0);
        cbSeverity.setSelectedIndex(0);
        selectedPhotoBytes = null;
        lblPhotoName.setText("No photo selected.");
        lblPhotoName.setForeground(UITheme.TEXT_MUTED);
        lblStatus.setText(" ");
    }

    private void showStatus(String msg, Color color) {
        lblStatus.setText(msg);
        lblStatus.setForeground(color);
    }

    // GridBag helpers

    private GridBagConstraints defaultGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        lbl.setPreferredSize(new Dimension(160, 28));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        field.setMinimumSize(new Dimension(280, 32));
        panel.add(field, gbc);
    }
}
