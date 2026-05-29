package com.restaurant.view;

import com.restaurant.dao.DonDatDAO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel thống kê nâng cao cho Admin
 * Giao diện: Cards so sánh doanh thu + Biểu đồ cột (7 ngày hoặc theo tháng)
 */
public class AdminStatisticsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private DonDatDAO donDatDAO;
    private JLabel lblCurrentRevenue, lblLastMonthRevenue, lblComparison;
    private JPanel chartCanvas;
    private Map<String, Double> chartData;
    private boolean showDailyChart = true;                               // true = biểu đồ 7 ngày, false = biểu đồ theo tháng

    /**
     * Constructor: Khởi tạo panel thống kê Admin
     */
    public AdminStatisticsPanel() {
        donDatDAO = new DonDatDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("THỐNG KÊ NÂNG CAO (ADMIN)");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));     // BorderLayout: Comparison (NORTH) + Chart (CENTER)
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel comparisonPanel = createComparisonPanel();                // Panel 3 cards so sánh doanh thu
        contentPanel.add(comparisonPanel, BorderLayout.NORTH);

        JPanel chartPanel = createChartPanel();                          // Panel biểu đồ cột
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadStatistics();                                                // Load dữ liệu thống kê
    }

    /**
     * Tạo panel so sánh doanh thu (3 cards)
     */
    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));        // Grid 1 hàng 3 cột
        panel.setBackground(UIConstants.BG_PRIMARY);

        // Card 1: Doanh thu tháng này
        lblCurrentRevenue = new JLabel("0 VNĐ", SwingConstants.CENTER);
        panel.add(createStatCard("Doanh thu tháng này", lblCurrentRevenue, "💰", UIConstants.SUCCESS_COLOR));

        // Card 2: Doanh thu tháng trước
        lblLastMonthRevenue = new JLabel("0 VNĐ", SwingConstants.CENTER);
        panel.add(createStatCard("Doanh thu tháng trước", lblLastMonthRevenue, "📅", UIConstants.INFO_COLOR));

        // Card 3: So sánh % tăng/giảm
        lblComparison = new JLabel("0%", SwingConstants.CENTER);
        panel.add(createStatCard("So sánh", lblComparison, "📊", UIConstants.WARNING_COLOR));

        return panel;
    }

    /**
     * Tạo card thống kê (icon + label + value)
     */
    private JPanel createStatCard(String label, JLabel valueLabel, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));             // BorderLayout: Icon (NORTH) + Content (CENTER)
        card.setBackground(UIConstants.BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);       // Icon emoji
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // Grid 2 hàng: Title + Value
        contentPanel.setBackground(UIConstants.BG_SECONDARY);

        JLabel lblTitle = new JLabel(label, SwingConstants.CENTER);     // Tiêu đề card
        lblTitle.setFont(UIConstants.FONT_SUBHEADER);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));        // Giá trị (sẽ được set sau)
        valueLabel.setForeground(color);

        contentPanel.add(lblTitle);
        contentPanel.add(valueLabel);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title with chart type buttons and refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIConstants.BG_SECONDARY);

        // Left side: Chart type buttons
        JPanel chartTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chartTypePanel.setBackground(UIConstants.BG_SECONDARY);

        JButton btnDailyChart = UIConstants.createPrimaryButton("Biểu đồ doanh thu 7 ngày gần nhất");
        btnDailyChart.setPreferredSize(new Dimension(280, 40));
        btnDailyChart.addActionListener(e -> {
            showDailyChart = true;
            chartCanvas.repaint();
        });

        JButton btnMonthlyChart = UIConstants.createSecondaryButton("Biểu đồ doanh thu từng tháng");
        btnMonthlyChart.setPreferredSize(new Dimension(250, 40));
        btnMonthlyChart.addActionListener(e -> {
            showDailyChart = false;
            chartCanvas.repaint();
        });

        chartTypePanel.add(btnDailyChart);
        chartTypePanel.add(btnMonthlyChart);
        titlePanel.add(chartTypePanel, BorderLayout.WEST);

        // Right side: Refresh button
        JButton btnRefresh = UIConstants.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            loadStatistics();
            chartCanvas.repaint();
        });
        titlePanel.add(btnRefresh, BorderLayout.EAST);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Chart canvas
        chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (showDailyChart) {
                    drawDailyRevenueChart(g);
                } else {
                    drawMonthlyRevenueChart(g);
                }
            }
        };
        chartCanvas.setBackground(Color.WHITE);
        chartCanvas.setPreferredSize(new Dimension(0, 350));

        panel.add(chartCanvas, BorderLayout.CENTER);

        return panel;
    }

    private void loadStatistics() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        // Calculate current month revenue
        String currentMonthStart = currentMonth.atDay(1).toString();
        String currentMonthEnd = currentMonth.atEndOfMonth().toString();
        double currentRevenue = calculateRevenue(currentMonthStart, currentMonthEnd);

        // Calculate last month revenue
        String lastMonthStart = lastMonth.atDay(1).toString();
        String lastMonthEnd = lastMonth.atEndOfMonth().toString();
        double lastRevenue = calculateRevenue(lastMonthStart, lastMonthEnd);

        // Calculate comparison percentage
        double comparison = 0;
        String comparisonText = "0%";
        Color comparisonColor = UIConstants.WARNING_COLOR;

        if (lastRevenue > 0) {
            comparison = ((currentRevenue - lastRevenue) / lastRevenue) * 100;

            // Giới hạn phần trăm trong khoảng -100% đến +100%
            if (comparison > 100) {
                comparison = 100;
            } else if (comparison < -100) {
                comparison = -100;
            }

            if (comparison > 0) {
                comparisonText = String.format("↑ +%.1f%%", comparison);
                comparisonColor = UIConstants.SUCCESS_COLOR;
            } else if (comparison < 0) {
                comparisonText = String.format("↓ %.1f%%", Math.abs(comparison));
                comparisonColor = UIConstants.DANGER_COLOR;
            } else {
                comparisonText = "→ 0%";
            }
        } else if (currentRevenue > 0) {
            comparisonText = "↑ +100%";
            comparisonColor = UIConstants.SUCCESS_COLOR;
        }

        // Update cards
        lblCurrentRevenue.setText(String.format("%,.0f VNĐ", currentRevenue));
        lblLastMonthRevenue.setText(String.format("%,.0f VNĐ", lastRevenue));
        lblComparison.setText(comparisonText);
        lblComparison.setForeground(comparisonColor);

        // Load chart data (last 7 days)
        chartData = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            double revenue = donDatDAO.getTotalRevenueByDate(dateStr);

            // Format label as "MM-DD"
            String label = dateStr.substring(5); // Get MM-DD part
            chartData.put(label, revenue);
        }

        // Refresh chart
        if (chartCanvas != null) {
            chartCanvas.repaint();
        }
    }

    private double calculateRevenue(String fromDate, String toDate) {
        List<DonDatDTO> orders = donDatDAO.getDonDatByDateRange(fromDate, toDate);
        double total = 0;
        for (DonDatDTO order : orders) {
            if ("completed".equals(order.getTinhTrang())) {
                total += order.getTongTien();
            }
        }
        return total;
    }

    private void drawDailyRevenueChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = g2d.getClipBounds().width;
        int height = g2d.getClipBounds().height;

        if (chartData == null || chartData.isEmpty()) {
            g2d.setFont(UIConstants.FONT_NORMAL);
            g2d.setColor(Color.GRAY);
            String noData = "Không có dữ liệu";
            int textWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - textWidth) / 2, height / 2);
            return;
        }

        // Chart dimensions
        int leftMargin = 80;
        int rightMargin = 40;
        int topMargin = 40;
        int bottomMargin = 60;
        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        // Find max value for scaling
        double maxRevenue = chartData.values().stream().max(Double::compare).orElse(1.0);
        double scale = maxRevenue > 0 ? chartHeight / maxRevenue : 1;

        // Draw grid lines and Y-axis labels
        g2d.setColor(new Color(230, 230, 230));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            int y = topMargin + (chartHeight * i / 5);
            g2d.drawLine(leftMargin, y, leftMargin + chartWidth, y);

            // Y-axis label
            double value = maxRevenue * (5 - i) / 5;
            String label = formatRevenue(value);
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.drawString(label, 10, y + 5);
            g2d.setColor(new Color(230, 230, 230));
        }

        // Draw bars
        java.util.List<String> dates = new java.util.ArrayList<>(chartData.keySet());
        int barCount = dates.size();
        int barSpacing = 20;
        int barWidth = (chartWidth - (barCount + 1) * barSpacing) / barCount;

        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            double revenue = chartData.get(date);

            int barHeight = (int) (revenue * scale);
            int x = leftMargin + barSpacing + i * (barWidth + barSpacing);
            int y = topMargin + chartHeight - barHeight;

            // Draw bar with gradient
            Color barColor = new Color(26, 188, 156);
            GradientPaint gradient = new GradientPaint(
                x, y, barColor.brighter(),
                x, y + barHeight, barColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Draw value on top of bar
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valueText = formatRevenue(revenue);
            int textWidth = g2d.getFontMetrics().stringWidth(valueText);
            g2d.drawString(valueText, x + (barWidth - textWidth) / 2, y - 5);

            // Draw date label
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String dateLabel = date; // Already in MM-DD format
            int dateLabelWidth = g2d.getFontMetrics().stringWidth(dateLabel);
            g2d.drawString(dateLabel, x + (barWidth - dateLabelWidth) / 2, topMargin + chartHeight + 20);
        }
    }

    private void drawMonthlyRevenueChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = g2d.getClipBounds().width;
        int height = g2d.getClipBounds().height;

        // Get last 6 months revenue data
        Map<String, Double> revenueData = getLast6MonthsRevenue();
        if (revenueData.isEmpty()) {
            g2d.setFont(UIConstants.FONT_NORMAL);
            g2d.setColor(Color.GRAY);
            String noData = "Không có dữ liệu";
            int textWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - textWidth) / 2, height / 2);
            return;
        }

        // Chart dimensions
        int leftMargin = 80;
        int rightMargin = 40;
        int topMargin = 40;
        int bottomMargin = 60;
        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        // Find max value for scaling
        double maxRevenue = revenueData.values().stream().max(Double::compare).orElse(1.0);
        double scale = maxRevenue > 0 ? chartHeight / maxRevenue : 1;

        // Draw grid lines and Y-axis labels
        g2d.setColor(new Color(230, 230, 230));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            int y = topMargin + (chartHeight * i / 5);
            g2d.drawLine(leftMargin, y, leftMargin + chartWidth, y);

            // Y-axis label
            double value = maxRevenue * (5 - i) / 5;
            String label = formatRevenue(value);
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.drawString(label, 10, y + 5);
            g2d.setColor(new Color(230, 230, 230));
        }

        // Draw bars
        java.util.List<String> months = new java.util.ArrayList<>(revenueData.keySet());
        int barCount = months.size();
        int barSpacing = 20;
        int barWidth = (chartWidth - (barCount + 1) * barSpacing) / barCount;

        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            double revenue = revenueData.get(month);

            int barHeight = (int) (revenue * scale);
            int x = leftMargin + barSpacing + i * (barWidth + barSpacing);
            int y = topMargin + chartHeight - barHeight;

            // Draw bar with gradient (blue color for monthly)
            Color barColor = new Color(52, 152, 219);
            GradientPaint gradient = new GradientPaint(
                x, y, barColor.brighter(),
                x, y + barHeight, barColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Draw value on top of bar
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valueText = formatRevenue(revenue);
            int textWidth = g2d.getFontMetrics().stringWidth(valueText);
            g2d.drawString(valueText, x + (barWidth - textWidth) / 2, y - 5);

            // Draw month label (format: MM/YYYY)
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String monthLabel = month.substring(5) + "/" + month.substring(0, 4); // MM/YYYY
            int dateLabelWidth = g2d.getFontMetrics().stringWidth(monthLabel);
            g2d.drawString(monthLabel, x + (barWidth - dateLabelWidth) / 2, topMargin + chartHeight + 20);
        }
    }

    private Map<String, Double> getLast6MonthsRevenue() {
        Map<String, Double> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String monthStr = monthDate.toString().substring(0, 7); // YYYY-MM

            // Calculate revenue for the entire month
            LocalDate firstDay = LocalDate.of(monthDate.getYear(), monthDate.getMonth(), 1);
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

            List<DonDatDTO> orders = donDatDAO.getDonDatByDateRange(firstDay.toString(), lastDay.toString());
            double revenue = 0;
            for (DonDatDTO order : orders) {
                if ("completed".equals(order.getTinhTrang())) {
                    revenue += order.getTongTien();
                }
            }

            data.put(monthStr, revenue);
        }

        return data;
    }

    private String formatRevenue(double revenue) {
        if (revenue >= 1_000_000) {
            return String.format("%.1fM", revenue / 1_000_000);
        } else if (revenue >= 1_000) {
            return String.format("%.1fK", revenue / 1_000);
        } else {
            return String.format("%.0f", revenue);
        }
    }
}
