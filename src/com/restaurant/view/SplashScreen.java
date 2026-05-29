package com.restaurant.view;

import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình splash screen - Hiển thị logo và loading animation khi khởi động app
 */
public class SplashScreen extends JWindow {
    private static final long serialVersionUID = 1L;

    private JProgressBar progressBar;
    private JLabel lblStatus;

    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(UIConstants.BG_SECONDARY);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 3),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        // ==================== TITLE PANEL ====================
        JPanel titlePanel = new JPanel(new GridLayout(3, 1, 0, 10));
        titlePanel.setBackground(UIConstants.BG_SECONDARY);

        JLabel lblIcon = new JLabel("HVV", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 60));

        JLabel lblTitle = new JLabel("NHÀ HÀNG HƯƠNG VỊ VIỆT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);

        JLabel lblSubtitle = new JLabel("Hệ thống quản lý chuyên nghiệp", SwingConstants.CENTER);
        lblSubtitle.setFont(UIConstants.FONT_NORMAL);
        lblSubtitle.setForeground(UIConstants.TEXT_SECONDARY);

        titlePanel.add(lblIcon);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);

        // ==================== PROGRESS PANEL ====================
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setBackground(UIConstants.BG_SECONDARY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);                              // Hiển thị % text trên bar
        progressBar.setForeground(UIConstants.PRIMARY_COLOR);
        progressBar.setPreferredSize(new Dimension(400, 25));

        lblStatus = new JLabel("Đang khởi động...", SwingConstants.CENTER);
        lblStatus.setFont(UIConstants.FONT_NORMAL);
        lblStatus.setForeground(UIConstants.TEXT_SECONDARY);

        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(lblStatus, BorderLayout.SOUTH);

        // ==================== FOOTER ====================
        JLabel lblVersion = new JLabel("Version 1.0.0 - © 2026", SwingConstants.CENTER);
        lblVersion.setFont(UIConstants.FONT_SMALL);
        lblVersion.setForeground(UIConstants.TEXT_SECONDARY);

        content.add(titlePanel, BorderLayout.NORTH);
        content.add(progressPanel, BorderLayout.CENTER);
        content.add(lblVersion, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(600, 400);
        setLocationRelativeTo(null);                                     // Center màn hình
        setVisible(true);

        startLoading();
    }

    private void startLoading() {
        Timer timer = new Timer(30, null);                               // 30ms interval = 33 FPS smooth animation

        timer.addActionListener(e -> {
            int value = progressBar.getValue();

            if (value < 100) {
                progressBar.setValue(value + 2);                         // Tăng 2% mỗi lần → 1.5 giây để đạt 100%

                // Cập nhật status text theo progress
                if (value < 30) {
                    lblStatus.setText("Đang kết nối database...");
                } else if (value < 60) {
                    lblStatus.setText("Đang tải dữ liệu...");
                } else if (value < 90) {
                    lblStatus.setText("Đang khởi tạo giao diện...");
                } else {
                    lblStatus.setText("Hoàn tất!");
                }
            } else {
                timer.stop();
                dispose();
                SwingUtilities.invokeLater(() -> new WelcomeFrame());    // Mở WelcomeFrame trên EDT
            }
        });

        timer.start();
    }
}
