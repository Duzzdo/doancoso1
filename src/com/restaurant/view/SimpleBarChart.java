package com.restaurant.view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Biểu đồ cột đơn giản - Vẽ bar chart bằng pure Swing Graphics2D
 */
public class SimpleBarChart extends JPanel {
    private static final long serialVersionUID = 1L;

    private Map<String, Double> data;
    private String title;
    private Color barColor;

    public SimpleBarChart(String title, Map<String, Double> data, Color barColor) {
        this.title = title;
        this.data = data;
        this.barColor = barColor;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiển thị message nếu không có dữ liệu
        if (data == null || data.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.setColor(Color.GRAY);
            String msg = "Không có dữ liệu";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(msg, x, y);
            return;
        }

        int padding = 40;
        int labelPadding = 20;
        int width = getWidth();
        int height = getHeight();

        // Vẽ title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.setColor(new Color(44, 62, 80));
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleX = (width - titleFm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, padding / 2);

        // Tìm giá trị max để tính tỷ lệ chiều cao cột
        double maxValue = data.values().stream().max(Double::compare).orElse(1.0);
        if (maxValue == 0) maxValue = 1.0;                               // Tránh chia cho 0

        // Tính kích thước vùng vẽ và chiều rộng mỗi cột
        int chartHeight = height - padding * 2 - labelPadding;
        int chartWidth = width - padding * 2;
        int barWidth = chartWidth / data.size() - 10;                    // Trừ spacing 10px giữa các cột

        // Vẽ từng cột
        int x = padding;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String label = entry.getKey();
            double value = entry.getValue();

            // Tính chiều cao cột theo tỷ lệ với maxValue
            int barHeight = (int) ((value / maxValue) * chartHeight);
            int barY = height - padding - labelPadding - barHeight;

            // Vẽ cột với gradient (màu nhạt trên → đậm dưới)
            GradientPaint gradient = new GradientPaint(
                x, barY, barColor,
                x, barY + barHeight, barColor.darker()
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x, barY, barWidth, barHeight);

            // Vẽ border cột
            g2d.setColor(barColor.darker());
            g2d.drawRect(x, barY, barWidth, barHeight);

            // Vẽ giá trị trên đỉnh cột (format: 150K)
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2d.setColor(new Color(44, 62, 80));
            String valueStr = String.format("%.0fK", value / 1000);
            FontMetrics fm = g2d.getFontMetrics();
            int valueX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
            g2d.drawString(valueStr, valueX, barY - 5);

            // Vẽ label dưới cột
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.setColor(Color.GRAY);
            int labelX = x + (barWidth - fm.stringWidth(label)) / 2;
            g2d.drawString(label, labelX, height - padding + 15);

            x += barWidth + 10;                                          // Di chuyển sang cột tiếp theo
        }

        // Vẽ trục Y và X
        g2d.setColor(Color.GRAY);
        g2d.drawLine(padding, padding, padding, height - padding - labelPadding);
        g2d.drawLine(padding, height - padding - labelPadding,
                     width - padding, height - padding - labelPadding);
    }
}
