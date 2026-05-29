package com.restaurant.view;

import com.restaurant.service.DatabasePollingService;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Màn hình chính sau đăng nhập - Navigation menu theo role + CardLayout switch giữa các panel
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel contentPanel;                                         // Container chứa các panel chức năng
    private CardLayout cardLayout;                                       // Layout để switch giữa các panel
    private JButton[] menuButtons;                                       // Array buttons để highlight active button
    private HomePanel homePanel;                                         // Reference để refresh data
    private StaffRevenuePanel staffRevenuePanel;                         // Panel doanh thu cá nhân (chỉ Staff)

    public MainFrame() {
        setTitle("Nhà hàng Hương Vị Việt - Hệ thống quản lý chuyên nghiệp"); // Tiêu đề cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                  // Đóng app khi tắt
        setSize(1400, 800);                                              // Kích thước cửa sổ
        setLocationRelativeTo(null);                                     // Hiển thị giữa màn hình
        setExtendedState(JFrame.MAXIMIZED_BOTH);                         // Maximize toàn màn hình

        setLayout(new BorderLayout());                                   // Layout chính
        getContentPane().setBackground(UIConstants.BG_PRIMARY);          // Màu nền

        add(createHeaderPanel(), BorderLayout.NORTH);                    // Thêm header ở trên
        add(createSidebarPanel(), BorderLayout.WEST);                    // Thêm sidebar bên trái

        // Content panel - CardLayout để switch giữa các panel
        cardLayout = new CardLayout();                                   // Khởi tạo CardLayout
        contentPanel = new JPanel(cardLayout);                           // Panel chứa các card
        contentPanel.setBackground(UIConstants.BG_PRIMARY);              // Màu nền
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Padding

        // Trang chủ - Tất cả roles
        homePanel = new HomePanel();                                     // Khởi tạo HomePanel
        contentPanel.add(homePanel, "home");                             // Thêm vào CardLayout với key "home"

        // Staff panels
        if (SessionManager.getInstance().isStaff()) {                    // Nếu user là Staff
            contentPanel.add(new TableManagementPanel(), "tables");      // Thêm panel quản lý bàn
        }

        if (SessionManager.getInstance().isStaff()) {                    // Nếu user là Staff
            staffRevenuePanel = new StaffRevenuePanel();                 // Khởi tạo panel doanh thu cá nhân
            contentPanel.add(staffRevenuePanel, "statistics");           // Thêm vào CardLayout
        }

        // Manager panels
        if (SessionManager.getInstance().isManager() ||                  // Nếu user là Manager
            SessionManager.getInstance().isAdmin()) {                    // hoặc Admin
            contentPanel.add(new MenuManagementPanel(), "menu");         // Thêm panel quản lý thực đơn
        }

        if (SessionManager.getInstance().isManager()) {                  // Nếu user là Manager
            contentPanel.add(new StatisticsPanel(), "statistics");       // Thêm panel thống kê
        }

        // Admin panels
        if (SessionManager.getInstance().isAdmin()) {                    // Nếu user là Admin
            contentPanel.add(new AdminDashboardPanel(), "tables");       // Thêm panel giám sát bàn
        }

        if (SessionManager.getInstance().isAdmin()) {                    // Nếu user là Admin
            contentPanel.add(new AdminStatisticsPanel(), "statistics");  // Thêm panel thống kê nâng cao
        }

        if (SessionManager.getInstance().isAdmin()) {                    // Nếu user là Admin
            contentPanel.add(new StaffManagementPanel(), "staff");       // Thêm panel quản lý nhân viên
        }

        contentPanel.add(new LeaderboardPanel(), "leaderboard");         // Bảng xếp hạng (Admin + Manager)

        add(contentPanel, BorderLayout.CENTER);                          // Thêm content panel vào giữa

        // Khởi động DatabasePollingService để tự động refresh data mỗi 7 giây
        DatabasePollingService.getInstance().addListener(() -> {
            // Callback này chạy trên background thread, cần chuyển sang EDT (Event Dispatch Thread)
            SwingUtilities.invokeLater(() -> {
                refreshHomePanel();                                      // Refresh HomePanel và StaffRevenuePanel
            });
        });
        DatabasePollingService.getInstance().start();                    // Bắt đầu polling

        cardLayout.show(contentPanel, "home");                           // Hiển thị trang chủ mặc định

        setVisible(true);                                                // Hiển thị frame
    }

    // Tạo header panel: Logo + User info + Logout
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));             // Panel với BorderLayout
        panel.setBackground(UIConstants.PRIMARY_COLOR);                  // Màu nền primary
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding

        // Left: App title
        JLabel lblTitle = new JLabel("NHÀ HÀNG HƯƠNG VỊ VIỆT");         // Label tiêu đề
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));          // Font size 22
        lblTitle.setForeground(UIConstants.TEXT_WHITE);                  // Màu chữ trắng

        // Right: User info + Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Panel bên phải
        rightPanel.setBackground(UIConstants.PRIMARY_COLOR);             // Màu nền primary

        String userName = SessionManager.getInstance().getCurrentUser().getHoTenNV(); // Lấy tên user
        String role = SessionManager.getInstance().isAdmin() ? "Admin" : // Xác định role
                     SessionManager.getInstance().isManager() ? "Manager" : "Staff";

        JLabel lblUser = new JLabel(userName + " (" + role + ")");      // Label hiển thị user + role
        lblUser.setFont(UIConstants.FONT_NORMAL);                        // Font normal
        lblUser.setForeground(UIConstants.TEXT_WHITE);                   // Màu chữ trắng

        JButton btnLogout = new JButton("Đăng xuất");                   // Nút đăng xuất
        btnLogout.setFont(UIConstants.FONT_BUTTON);                      // Font button
        btnLogout.setBackground(UIConstants.DANGER_COLOR);               // Màu nền đỏ
        btnLogout.setForeground(UIConstants.TEXT_WHITE);                 // Màu chữ trắng
        btnLogout.setFocusPainted(false);                                // Không vẽ focus
        btnLogout.setBorderPainted(false);                               // Không vẽ border
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));            // Con trỏ tay khi hover
        btnLogout.addActionListener(e -> handleLogout());                // Xử lý click

        rightPanel.add(lblUser);                                         // Thêm label user
        rightPanel.add(btnLogout);                                       // Thêm nút logout

        panel.add(lblTitle, BorderLayout.WEST);                         // Thêm title bên trái
        panel.add(rightPanel, BorderLayout.EAST);                       // Thêm right panel bên phải

        return panel;                                                    // Trả về panel
    }

    // Tạo sidebar panel: Navigation menu theo role
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();                                     // Panel sidebar
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));        // Layout dọc
        panel.setBackground(UIConstants.BG_DARK);                        // Màu nền tối
        panel.setPreferredSize(new Dimension(250, 0));                  // Chiều rộng 250px
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Padding

        // Menu items theo role
        String[] menuItems;                                              // Array tên menu
        String[] cardNames;                                              // Array key của card

        if (SessionManager.getInstance().isAdmin()) {                    // Nếu user là Admin
            menuItems = new String[]{"Trang chủ", "Giám sát bàn", "Thống kê", "Quản lý nhân viên", "Bảng xếp hạng"};
            cardNames = new String[]{"home", "tables", "statistics", "staff", "leaderboard"};
        } else if (SessionManager.getInstance().isManager()) {           // Nếu user là Manager
            menuItems = new String[]{"Trang chủ", "Quản lý thực đơn", "Thống kê", "Bảng xếp hạng"};
            cardNames = new String[]{"home", "menu", "statistics", "leaderboard"};
        } else {                                                         // Nếu user là Staff
            menuItems = new String[]{"Trang chủ", "Quản lý bàn", "Doanh thu cá nhân"};
            cardNames = new String[]{"home", "tables", "statistics"};
        }

        menuButtons = new JButton[menuItems.length];                    // Khởi tạo array buttons

        for (int i = 0; i < menuItems.length; i++) {                    // Duyệt qua từng menu item
            final int index = i;                                         // Final variable cho lambda
            final String cardName = cardNames[i];                        // Final variable cho lambda

            JButton btn = createMenuButton(menuItems[i]);                // Tạo button
            btn.addActionListener(e -> {                                 // Xử lý click
                selectMenu(index);                                       // Highlight button
                cardLayout.show(contentPanel, cardName);                 // Show card tương ứng
            });

            menuButtons[i] = btn;                                        // Lưu vào array
            panel.add(btn);                                              // Thêm button vào panel
            panel.add(Box.createRigidArea(new Dimension(0, 10)));       // Khoảng cách 10px
        }

        selectMenu(0);                                                   // Select menu đầu tiên mặc định

        return panel;                                                    // Trả về panel
    }

    // Tạo menu button với style nhất quán
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);                                // Tạo button với text
        btn.setFont(UIConstants.FONT_NORMAL);                            // Font normal
        btn.setForeground(UIConstants.TEXT_WHITE);                       // Màu chữ trắng
        btn.setBackground(UIConstants.BG_DARK);                          // Màu nền tối
        btn.setMaximumSize(new Dimension(230, 50));                     // Kích thước tối đa
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);                   // Căn giữa
        btn.setFocusPainted(false);                                      // Không vẽ focus
        btn.setBorderPainted(false);                                     // Không vẽ border
        btn.setHorizontalAlignment(SwingConstants.LEFT);                 // Text căn trái
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));                  // Con trỏ tay khi hover
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        return btn;                                                      // Trả về button
    }

    // Highlight menu button đang active
    private void selectMenu(int index) {
        for (JButton btn : menuButtons) {                                // Duyệt qua tất cả buttons
            btn.setBackground(UIConstants.BG_DARK);                      // Đặt màu nền tối (deselect)
        }

        menuButtons[index].setBackground(UIConstants.PRIMARY_COLOR);     // Đặt màu primary cho button active
    }

    // Xử lý logout: Confirm → Clear session → Quay về WelcomeFrame
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,               // Hiển thị dialog xác nhận
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {                         // Nếu user chọn Yes
            SessionManager.getInstance().logout();                       // Clear session
            dispose();                                                   // Đóng MainFrame
            new WelcomeFrame();                                          // Mở WelcomeFrame
        }
    }

    // Refresh dữ liệu trong HomePanel và StaffRevenuePanel
    public void refreshHomePanel() {
        if (homePanel != null) {                                         // Nếu homePanel tồn tại
            homePanel.refreshData();                                     // Refresh data
        }
        if (staffRevenuePanel != null) {                                 // Nếu staffRevenuePanel tồn tại
            staffRevenuePanel.loadStatistics();                          // Reload statistics
        }
    }

    /**
     * Override dispose để dừng DatabasePollingService khi đóng frame
     */
    @Override
    public void dispose() {
        DatabasePollingService.getInstance().stop();                     // Dừng polling service
        super.dispose();                                                 // Gọi dispose của JFrame
    }
}
