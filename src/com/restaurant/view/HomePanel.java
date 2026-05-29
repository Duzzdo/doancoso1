package com.restaurant.view;

import com.restaurant.dao.BanAnDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.dao.StaffRankingDAO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.StaffRankingDTO;
import com.restaurant.utils.RankUtil;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Home panel - Dashboard tổng quan với statistics cards và ranking
 *
 * Layout:
 * - Admin/Manager: 4 cards (2x2 grid) + welcome message
 * - Staff: 6 cards (2x3 grid) + motivation panel (bonus + progress bar)
 *
 * Auto-refresh: Staff có timer refresh mỗi 5 giây
 */
public class HomePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private BanAnDAO banAnDAO;
    private DonDatDAO donDatDAO;
    private StaffRankingDAO staffRankingDAO;

    private static final Color GRADIENT_START = new Color(240, 248, 255);
    private static final Color GRADIENT_END = new Color(230, 240, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color DANGER_RED = new Color(231, 76, 60);
    private static final Color INFO_BLUE = new Color(52, 152, 219);
    private static final Color WARNING_ORANGE = new Color(243, 156, 18);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);

    private JPanel statsPanel;
    private JLabel lblAvailableTables;
    private JLabel lblOccupiedTables;
    private JLabel lblTodayOrders;
    private JLabel lblTodayRevenue;
    private JLabel lblRanking;
    private JLabel lblPosition;
    private JLabel lblBonus;
    private JLabel lblNextRank;
    private JProgressBar progressBar;
    private Timer refreshTimer;

    public HomePanel() {
        banAnDAO = new BanAnDAO();
        donDatDAO = new DonDatDAO();
        staffRankingDAO = new StaffRankingDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(GRADIENT_START);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Dashboard quản lý nhà hàng");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_LIGHT);

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        add(titlePanel, BorderLayout.NORTH);

        // Statistics cards: Admin/Manager (2x2), Staff (2x3 với ranking)
        boolean isStaff = SessionManager.getInstance().isStaff();
        if (isStaff) {
            statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        } else {
            statsPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        }
        statsPanel.setOpaque(false);

        // Get data từ database
        int availableTables = banAnDAO.getAvailableTablesCount();
        int occupiedTables = banAnDAO.getOccupiedTablesCount();
        String today = LocalDate.now().toString();

        // Tính orders và revenue theo role
        int currentStaffId = SessionManager.getInstance().getCurrentUser().getMaNV();
        boolean isStaffRole = SessionManager.getInstance().isStaff();
        List<DonDatDTO> todayOrders = donDatDAO.getDonDatByDate(today);
        int displayTodayOrders = 0;
        double displayTodayRevenue = 0;

        if (isStaffRole) {
            // Staff: Chỉ đếm orders và revenue của mình
            for (DonDatDTO order : todayOrders) {
                if (order.getMaNV() == currentStaffId && "completed".equals(order.getTinhTrang())) {
                    displayTodayOrders++;
                    displayTodayRevenue += order.getTongTien();
                }
            }
        } else {
            // Admin/Manager: Tổng của tất cả staff
            for (DonDatDTO order : todayOrders) {
                if ("completed".equals(order.getTinhTrang())) {
                    displayTodayOrders++;
                    displayTodayRevenue += order.getTongTien();
                }
            }
        }

        // Tạo 4 cards chính
        JPanel card1 = createModernCard("Bàn trống", String.valueOf(availableTables),
            SUCCESS_GREEN, "✅");
        lblAvailableTables = findValueLabel(card1);
        statsPanel.add(card1);

        JPanel card2 = createModernCard("Bàn đang sử dụng", String.valueOf(occupiedTables),
            DANGER_RED, "🪑");
        lblOccupiedTables = findValueLabel(card2);
        statsPanel.add(card2);

        JPanel card3 = createModernCard("Đơn hàng hôm nay", String.valueOf(displayTodayOrders),
            INFO_BLUE, "📋");
        lblTodayOrders = findValueLabel(card3);
        statsPanel.add(card3);

        JPanel card4 = createModernCard("Doanh thu hôm nay",
            String.format("%,.0f VNĐ", displayTodayRevenue), WARNING_ORANGE, "💰");
        lblTodayRevenue = findValueLabel(card4);
        statsPanel.add(card4);

        // Staff: Thêm 2 cards ranking
        if (isStaffRole) {
            String currentMonth = LocalDate.now().toString().substring(0, 7); // yyyy-MM
            StaffRankingDTO staffRanking = staffRankingDAO.getStaffRankingById(currentStaffId, currentMonth);

            JPanel card5 = createModernCard("Xếp hạng hiện tại",
                staffRanking.getRank(),
                new Color(155, 89, 182), "🏆");
            lblRanking = findValueLabel(card5);
            statsPanel.add(card5);

            JPanel card6 = createModernCard("Vị trí",
                "#" + staffRanking.getXepHang(),
                new Color(26, 188, 156), "📊");
            lblPosition = findValueLabel(card6);
            statsPanel.add(card6);
        }

        add(statsPanel, BorderLayout.CENTER);

        // Bottom panel: Staff (motivation + progress bar), Admin/Manager (welcome card)
        if (isStaffRole) {
            JPanel motivationPanel = createMotivationPanel(currentStaffId);
            add(motivationPanel, BorderLayout.SOUTH);
        } else {
            JPanel welcomeCard = createWelcomeCard();
            add(welcomeCard, BorderLayout.SOUTH);
        }

        // Auto-refresh timer: Chỉ cho Staff, refresh mỗi 5 giây
        if (isStaffRole) {
            refreshTimer = new Timer(5000, e -> refreshData());
            refreshTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Vẽ gradient background từ trên xuống dưới
        GradientPaint gradient = new GradientPaint(
            0, 0, GRADIENT_START,
            0, getHeight(), GRADIENT_END
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Tạo modern card với shadow, rounded corners, emoji icon
     *
     * @param label Tiêu đề card (VD: "Bàn trống")
     * @param value Giá trị hiển thị (VD: "5", "150,000 VNĐ")
     * @param accentColor Màu accent (border trái + icon)
     * @param iconType Emoji icon
     * @return JPanel card đã được style
     */
    private JPanel createModernCard(String label, String value, Color accentColor, String iconType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect (offset 4px)
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);

                // Card background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                // Accent border bên trái
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, 6, getHeight() - 4, 20, 20);

                g2d.dispose();
            }
        };

        card.setLayout(new BorderLayout(15, 15));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(248, 249, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_BG);
            }
        });

        // Icon panel: Emoji với font fallback
        JLabel iconLabel = new JLabel(iconType);
        Font emojiFont = null;
        String[] fontNames = {"Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji", "Android Emoji", "EmojiOne Color", "Symbola"};
        for (String fontName : fontNames) {
            Font testFont = new Font(fontName, Font.PLAIN, 48);
            if (testFont.getFamily().equals(fontName)) {
                emojiFont = testFont;
                break;
            }
        }
        if (emojiFont == null) {
            emojiFont = new Font(Font.SANS_SERIF, Font.PLAIN, 48);
        }
        iconLabel.setFont(emojiFont);
        iconLabel.setForeground(accentColor);
        iconLabel.setPreferredSize(new Dimension(90, 80));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Content panel: Value + Label
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(TEXT_DARK);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTitle.setForeground(TEXT_LIGHT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        contentPanel.add(lblValue);
        contentPanel.add(lblTitle);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Tìm JLabel value trong card (để update sau này)
     * Value label có font size 32
     */
    private JLabel findValueLabel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component innerComp : panel.getComponents()) {
                    if (innerComp instanceof JLabel) {
                        JLabel label = (JLabel) innerComp;
                        if (label.getFont().getSize() == 32) {
                            return label;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Refresh tất cả dữ liệu trong panel
     * Được gọi từ MainFrame hoặc Timer (auto-refresh mỗi 5 giây cho Staff)
     */
    public void refreshData() {
        // Lấy số bàn trống từ database
        int availableTables = banAnDAO.getAvailableTablesCount();
        // Lấy số bàn đang sử dụng
        int occupiedTables = banAnDAO.getOccupiedTablesCount();
        // Lấy ngày hôm nay cho việc tính toán doanh thu/đơn
        String today = LocalDate.now().toString();

        // Lấy ID và kiểm tra role của nhân viên hiện tại
        int currentStaffId = SessionManager.getInstance().getCurrentUser().getMaNV();
        // Kiểm tra user là Staff hay Admin/Manager
        boolean isStaff = SessionManager.getInstance().isStaff();
        // Lấy danh sách tất cả đơn hôm nay
        List<DonDatDTO> todayOrders = donDatDAO.getDonDatByDate(today);
        // Khởi tạo biến để lưu số đơn hôm nay (sau khi filter theo role)
        int displayTodayOrders = 0;
        // Khởi tạo biến để lưu doanh thu hôm nay (sau khi filter theo role)
        double displayTodayRevenue = 0;

        // Kiểm tra nếu người dùng là Staff
        if (isStaff) {
            // Duyệt qua tất cả đơn hôm nay
            for (DonDatDTO order : todayOrders) {
                // Chỉ đếm nếu là order của nhân viên này và đã completed
                if (order.getMaNV() == currentStaffId && "completed".equals(order.getTinhTrang())) {
                    // Tăng số đơn của nhân viên
                    displayTodayOrders++;
                    // Cộng doanh thu vào tổng của nhân viên
                    displayTodayRevenue += order.getTongTien();
                }
            }
        } else {
            // Duyệt qua tất cả đơn hôm nay
            for (DonDatDTO order : todayOrders) {
                // Chỉ đếm nếu đơn đã completed
                if ("completed".equals(order.getTinhTrang())) {
                    // Tăng số đơn tổng
                    displayTodayOrders++;
                    // Cộng doanh thu vào tổng
                    displayTodayRevenue += order.getTongTien();
                }
            }
        }

        // Cập nhật nhãn số bàn trống nếu component còn tồn tại
        if (lblAvailableTables != null) {
            lblAvailableTables.setText(String.valueOf(availableTables));
        }
        // Cập nhật nhãn số bàn đang sử dụng
        if (lblOccupiedTables != null) {
            lblOccupiedTables.setText(String.valueOf(occupiedTables));
        }
        // Cập nhật nhãn số đơn hôm nay
        if (lblTodayOrders != null) {
            lblTodayOrders.setText(String.valueOf(displayTodayOrders));
        }
        // Cập nhật nhãn doanh thu hôm nay (format tiền)
        if (lblTodayRevenue != null) {
            lblTodayRevenue.setText(String.format("%,.0f VNĐ", displayTodayRevenue));
        }

        // Cập nhật các cards ranking chỉ cho Staff
        if (SessionManager.getInstance().isStaff()) {
            // Lấy tháng hiện tại (yyyy-MM format)
            String currentMonth = LocalDate.now().toString().substring(0, 7);
            // Lấy thông tin ranking của nhân viên trong tháng này
            StaffRankingDTO staffRanking = staffRankingDAO.getStaffRankingById(currentStaffId, currentMonth);

            // Cập nhật nhãn xếp hạng hiện tại nếu tồn tại
            if (lblRanking != null) {
                lblRanking.setText(staffRanking.getRank());
            }
            // Cập nhật nhãn vị trí xếp hạng nếu tồn tại
            if (lblPosition != null) {
                lblPosition.setText("#" + staffRanking.getXepHang());
            }

            // Cập nhật motivation panel (thưởng + thanh tiến độ)
            // Lấy tổng doanh thu tháng này của nhân viên
            double monthRevenue = donDatDAO.getRevenueByStaffAndMonth(currentStaffId, currentMonth);
            // Tính xếp hạng dựa vào doanh thu
            String currentRank = RankUtil.calculateRank(monthRevenue);
            // Tính phần trăm bonus dựa vào doanh thu
            double bonusPercent = RankUtil.getBonusPercentageFromRevenue(monthRevenue) * 100;
            // Tính doanh thu còn lại để lên rank tiếp theo
            double revenueToNext = RankUtil.getRevenueToNextRank(monthRevenue);
            // Lấy tên rank tiếp theo
            String nextRank = RankUtil.getNextRank(monthRevenue);
            // Tính tiến độ từ 0-1 để lên rank tiếp theo
            double progress = RankUtil.getProgressToNextRank(monthRevenue);

            // Cập nhật nhãn thưởng nếu tồn tại
            if (lblBonus != null) {
                lblBonus.setText(String.format("Thưởng hạng %s: +%.0f%% tiền lương", currentRank, bonusPercent));
            }

            // Cập nhật nhãn rank tiếp theo nếu tồn tại
            if (lblNextRank != null) {
                // Kiểm tra có rank tiếp theo chưa
                if (!nextRank.isEmpty()) {
                    // Hiển thị thông tin doanh thu còn lại
                    lblNextRank.setText(String.format("Còn %,.0f VNĐ để lên hạng %s", revenueToNext, nextRank));
                    lblNextRank.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                } else {
                    // Hiển thị message nếu đã ở rank cao nhất
                    lblNextRank.setText("Bạn đã đạt hạng cao nhất! Tiếp tục phát huy!");
                    lblNextRank.setFont(new Font("Segoe UI", Font.BOLD, 15));
                }
            }

            // Cập nhật progress bar nếu tồn tại
            if (progressBar != null) {
                // Set giá trị tiến độ (0-100%)
                progressBar.setValue((int) (progress * 100));
                // Set text hiển thị phần trăm
                progressBar.setString(String.format("%.0f%%", progress * 100));
            }
        }

        // Refresh layout để hiển thị dữ liệu mới
        revalidate();
        // Vẽ lại panel
        repaint();
    }

    /**
     * Tạo welcome card cho Admin/Manager (bottom panel)
     */
    private JPanel createWelcomeCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(52, 152, 219, 10),
                    getWidth(), 0, new Color(155, 89, 182, 10)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 20, 20);

                // Border
                g2d.setColor(new Color(52, 152, 219, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                g2d.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));

        JLabel lblWelcome = new JLabel("<html><div style='text-align: center;'>" +
                "<h2 style='color: #2c3e50; margin: 0; font-size: 24px;'>Chào mừng đến với Nhà hàng Hương Vị Việt</h2>" +
                "<p style='color: #7f8c8d; margin-top: 10px; font-size: 15px;'>Hệ thống quản lý nhà hàng chuyên nghiệp</p>" +
                "<p style='color: #95a5a6; margin-top: 5px; font-size: 14px;'>Chọn menu bên trái để bắt đầu sử dụng</p>" +
                "</div></html>", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        card.add(lblWelcome, BorderLayout.CENTER);

        return card;
    }

    /**
     * Tạo motivation panel cho Staff (bonus info + progress bar)
     * Hiển thị thông tin thưởng hiện tại và tiến độ lên rank tiếp theo
     */
    private JPanel createMotivationPanel(int staffId) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(46, 204, 113, 10),
                    getWidth(), 0, new Color(52, 152, 219, 10)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 20, 20);

                // Border
                g2d.setColor(new Color(46, 204, 113, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                g2d.dispose();
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        // Get doanh thu tháng hiện tại và tính bonus
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        double monthRevenue = donDatDAO.getRevenueByStaffAndMonth(staffId, currentMonth);

        String currentRank = RankUtil.calculateRank(monthRevenue);
        double bonusPercent = RankUtil.getBonusPercentageFromRevenue(monthRevenue) * 100;
        double revenueToNext = RankUtil.getRevenueToNextRank(monthRevenue);
        String nextRank = RankUtil.getNextRank(monthRevenue);
        double progress = RankUtil.getProgressToNextRank(monthRevenue);

        JLabel lblTitle = new JLabel("Thưởng & Tiến độ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblBonus = new JLabel(String.format("Thưởng hạng %s: +%.0f%% tiền lương", currentRank, bonusPercent));
        lblBonus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblBonus.setForeground(new Color(46, 204, 113));
        lblBonus.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBonus.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panel.add(lblTitle);
        panel.add(lblBonus);

        // Next rank info (nếu chưa đạt rank cao nhất)
        if (!nextRank.isEmpty()) {
            lblNextRank = new JLabel(String.format("Còn %,.0f VNĐ để lên hạng %s", revenueToNext, nextRank));
            lblNextRank.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            lblNextRank.setForeground(TEXT_LIGHT);
            lblNextRank.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblNextRank.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(lblNextRank);

            // Progress bar (0-100%)
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue((int) (progress * 100));
            progressBar.setStringPainted(true);
            progressBar.setString(String.format("%.0f%%", progress * 100));
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            progressBar.setForeground(new Color(46, 204, 113));
            progressBar.setBackground(new Color(236, 240, 241));
            progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            progressBar.setPreferredSize(new Dimension(0, 30));
            progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(progressBar);
        } else {
            // Đã đạt rank cao nhất (Diamond)
            lblNextRank = new JLabel("Bạn đã đạt hạng cao nhất! Tiếp tục phát huy!");
            lblNextRank.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblNextRank.setForeground(new Color(243, 156, 18));
            lblNextRank.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lblNextRank);
        }

        return panel;
    }
}
