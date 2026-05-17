package roadwatch;

import java.awt.*;
import java.util.List;
import javax.swing.*;

//SummaryFrame – Displays report statistics and damage-type breakdown.
 
public class SummaryFrame extends JFrame {

    private final DamageReportDAO reportDAO;

    public SummaryFrame(DamageReportDAO reportDAO) {
        this.reportDAO = reportDAO;
        setTitle("ROADWATCH — Summary Report");
        setSize(580, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // Header 
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UITheme.DPWH_BLUE,
                                                     0, getHeight(), UITheme.DPWH_BLUE_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.DPWH_YELLOW);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
        };
        header.setPreferredSize(new Dimension(580, 80));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));

        JLabel title = new JLabel("📊  Summary Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Infrastructure damage statistics — DPWH ROADWATCH");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xB8CCE0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // Body 
        JPanel body = new JPanel();
        body.setBackground(UITheme.BG_LIGHT);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        int[] counts = reportDAO.getSummaryCounts();
        int total    = counts[0];
        int pending  = counts[1];
        int inProg   = counts[2];
        int resolved = counts[3];

        // Stat cards row 
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setBackground(UITheme.BG_LIGHT);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        statsRow.add(statCard("Total Reports", total, UITheme.DPWH_BLUE));
        statsRow.add(statCard("Pending",        pending, UITheme.PENDING_COLOR));
        statsRow.add(statCard("In Progress",    inProg,  UITheme.INPROG_COLOR));
        statsRow.add(statCard("Resolved",       resolved,UITheme.RESOLVED_COLOR));

        // Bar chart manual
        JPanel chartPanel = new BarChartPanel(counts);
        chartPanel.setBorder(UITheme.panelBorder("Report Status Distribution"));
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        chartPanel.setPreferredSize(new Dimension(520, 160));

        // By type table 
        List<String[]> byType = reportDAO.getSummaryByType();
        String[] cols = { "Damage Type", "No. of Reports" };
        Object[][] data = new Object[byType.size()][2];
        for (int i = 0; i < byType.size(); i++) {
            data[i][0] = byType.get(i)[0];
            data[i][1] = byType.get(i)[1];
        }

        JTable typeTable = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        typeTable.setFont(UITheme.FONT_TABLE);
        typeTable.setRowHeight(28);
        typeTable.setGridColor(new Color(0xE0E8F0));
        typeTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        typeTable.getTableHeader().setBackground(UITheme.DPWH_BLUE);
        typeTable.getTableHeader().setForeground(Color.WHITE);
        typeTable.setShowVerticalLines(true);

        JScrollPane typeScroll = new JScrollPane(typeTable);
        typeScroll.setBorder(UITheme.panelBorder("Reports by Damage Type"));
        typeScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Close button 
        JButton btnClose = UITheme.primaryButton("Close");
        btnClose.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnClose.addActionListener(e -> dispose());

        body.add(statsRow);
        body.add(Box.createVerticalStrut(16));
        body.add(chartPanel);
        body.add(Box.createVerticalStrut(16));
        body.add(typeScroll);
        body.add(Box.createVerticalStrut(16));
        body.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(body) {{ setBorder(BorderFactory.createEmptyBorder()); }},
                 BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel statCard(String label, int value, Color color) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), 6, 4, 4);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        card.setOpaque(false);

        JLabel numLbl = new JLabel(String.valueOf(value));
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        numLbl.setForeground(color);
        numLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txtLbl = new JLabel(label);
        txtLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        txtLbl.setForeground(UITheme.TEXT_MUTED);
        txtLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(numLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(txtLbl);
        return card;
    }

    // ── Simple bar chart ──────────────────────────────────────────────────

    static class BarChartPanel extends JPanel {
        private final int[] counts;
        private static final String[] LABELS = { "Total", "Pending", "In Progress", "Resolved" };
        private static final Color[] COLORS  = {
            UITheme.DPWH_BLUE, UITheme.PENDING_COLOR, UITheme.INPROG_COLOR, UITheme.RESOLVED_COLOR
        };

        BarChartPanel(int[] counts) {
            this.counts = counts;
            setBackground(UITheme.CARD_WHITE);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 50, padR = 20, padT = 20, padB = 50;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            int max = 1;
            for (int c : counts) if (c > max) max = c;

            int barCount = counts.length;
            int barW = chartW / barCount - 20;

            // Grid lines
            g2.setColor(new Color(0xE8EDF4));
            for (int i = 0; i <= 4; i++) {
                int y = padT + chartH - (int)((double) i / 4 * chartH);
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(String.valueOf((int)((double) i / 4 * max)), 4, y + 4);
                g2.setColor(new Color(0xE8EDF4));
            }

            // Bars
            for (int i = 0; i < barCount; i++) {
                int x = padL + i * (chartW / barCount) + 10;
                int barH = counts[i] == 0 ? 2 : (int)((double) counts[i] / max * chartH);
                int y = padT + chartH - barH;

                g2.setColor(COLORS[i]);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                // Value label
                g2.setColor(COLORS[i]);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String val = String.valueOf(counts[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 5);

                // Category label
                g2.setColor(UITheme.TEXT_DARK);
                g2.setFont(UITheme.FONT_SMALL);
                FontMetrics fm2 = g2.getFontMetrics();
                String lbl = LABELS[i];
                g2.drawString(lbl, x + (barW - fm2.stringWidth(lbl)) / 2,
                              padT + chartH + 18);
            }

            // Axes
            g2.setColor(new Color(0xCCD5E0));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);
        }
    }
}
