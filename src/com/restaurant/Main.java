package com.restaurant;

import com.restaurant.database.DatabaseSetup;
import com.restaurant.view.SplashScreen;

import javax.swing.*;

/**
 * Main entry point của ứng dụng Quản lý Nhà hàng Hương Vị Việt
 *
 * Chức năng:
 * - Khởi tạo database (tạo bảng, insert dữ liệu mẫu nếu chưa có)
 * - Thiết lập giao diện theo hệ điều hành (Windows/Mac/Linux)
 * - Hiển thị màn hình splash screen khi khởi động
 *
 * Luồng khởi động:
 * 1. DatabaseSetup.initialize() - Tạo/kiểm tra database
 * 2. UIManager.setLookAndFeel() - Thiết lập giao diện native
 * 3. SplashScreen - Hiển thị logo 2-3 giây
 * 4. WelcomeFrame - Màn hình đăng nhập/đăng ký
 *
 * @author Restaurant Management Team
 * @version 1.0.0
 */
public class Main {
    /**
     * Entry point của ứng dụng
     *
     * @param args Command line arguments (không sử dụng)
     */
    public static void main(String[] args) {
        // Bước 1: Khởi tạo database
        // - Tạo file restaurant.db nếu chưa tồn tại
        // - Tạo các bảng: QUYEN, NHANVIEN, BAN, LOAIMON, MON, DONDAT, CHITIETDONDAT
        // - Insert dữ liệu mẫu: 3 quyền (Admin/Manager/Staff), 3 tài khoản mặc định
        DatabaseSetup.initialize();

        // Bước 2: Thiết lập Look and Feel theo hệ điều hành
        // - Windows: Windows Look and Feel
        // - macOS: Aqua Look and Feel
        // - Linux: GTK+ Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Nếu lỗi, sử dụng Metal Look and Feel mặc định
            e.printStackTrace();
        }

        // Bước 3: Khởi động ứng dụng với Splash Screen
        // SwingUtilities.invokeLater() đảm bảo UI được tạo trên Event Dispatch Thread (EDT)
        // Điều này quan trọng để tránh deadlock và đảm bảo thread-safety cho Swing
        SwingUtilities.invokeLater(() -> {
            // Hiển thị SplashScreen (logo + loading bar) trong 2-3 giây
            // Sau đó tự động chuyển sang WelcomeFrame
            new SplashScreen();
        });
    }
}
