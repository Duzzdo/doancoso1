package com.restaurant.utils;

import java.awt.*;
import javax.swing.*;

/**
 * Utility class chứa các constants cho UI styling nhất quán
 *
 * Chức năng:
 * - Định nghĩa color palette chuyên nghiệp cho ứng dụng nhà hàng
 * - Cung cấp fonts chuẩn cho các loại text (title, header, normal)
 * - Định nghĩa dimensions cho buttons, panels
 * - Cung cấp spacing constants (padding, margin)
 * - Factory methods tạo styled components (buttons, panels, textfields)
 *
 * Tại sao cần UIConstants?
 * - Đảm bảo UI nhất quán trên toàn bộ ứng dụng
 * - Dễ dàng thay đổi theme (chỉ sửa 1 chỗ)
 * - Code dễ đọc hơn (PRIMARY_COLOR thay vì new Color(41, 128, 185))
 * - Tuân thủ Material Design / Modern UI principles
 *
 * Color Palette Design:
 * - PRIMARY: Blue (#2980b9) - Màu chủ đạo, dùng cho buttons, headers
 * - SUCCESS: Green (#2ecc71) - Thành công, bàn trống
 * - DANGER: Red (#e74c3c) - Lỗi, xóa, bàn đang dùng
 * - WARNING: Yellow (#f1c40f) - Cảnh báo, bàn đặt trước
 * - INFO: Light Blue (#3498db) - Thông tin
 *
 * Font Family:
 * - Segoe UI: Font mặc định của Windows, dễ đọc, chuyên nghiệp
 * - Fallback: Hệ thống tự chọn font tương tự nếu không có Segoe UI
 *
 * Sử dụng trong:
 * - Tất cả View classes (LoginFrame, MainFrame, các Panels)
 * - Tạo buttons với màu sắc nhất quán
 * - Tạo panels với border và padding chuẩn
 */
public class UIConstants {

    // ==================== COLOR PALETTE ====================

    /**
     * Màu chủ đạo - Blue (#2980b9)
     * Dùng cho: Primary buttons, headers, borders, active states
     */
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);

    /**
     * Màu primary tối - Dark Blue (#1f618d)
     * Dùng cho: Hover effects, shadows, darker variants
     */
    public static final Color PRIMARY_DARK = new Color(31, 97, 141);

    /**
     * Màu phụ - Light Blue (#3498db)
     * Dùng cho: Secondary buttons, info messages, links
     */
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);

    /**
     * Màu thành công - Green (#2ecc71)
     * Dùng cho: Success buttons, bàn trống (available), positive messages
     */
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);

    /**
     * Màu cảnh báo - Yellow (#f1c40f)
     * Dùng cho: Warning buttons, bàn đặt trước (reserved), caution messages
     */
    public static final Color WARNING_COLOR = new Color(241, 196, 15);

    /**
     * Màu nguy hiểm - Red (#e74c3c)
     * Dùng cho: Delete buttons, bàn đang dùng (occupied), error messages
     */
    public static final Color DANGER_COLOR = new Color(231, 76, 60);

    /**
     * Màu thông tin - Light Blue (#3498db)
     * Dùng cho: Info buttons, tooltips, help messages
     */
    public static final Color INFO_COLOR = new Color(52, 152, 219);

    // ==================== BACKGROUND COLORS ====================

    /**
     * Background chính - Light Gray (#ecf0f1)
     * Dùng cho: Main content area, panel backgrounds
     */
    public static final Color BG_PRIMARY = new Color(236, 240, 241);

    /**
     * Background phụ - White
     * Dùng cho: Cards, dialogs, input fields
     */
    public static final Color BG_SECONDARY = Color.WHITE;

    /**
     * Background tối - Dark Gray (#2c3e50)
     * Dùng cho: Sidebar, footer, dark mode elements
     */
    public static final Color BG_DARK = new Color(44, 62, 80);

    // ==================== TEXT COLORS ====================

    /**
     * Text chính - Dark Gray (#2c3e50)
     * Dùng cho: Body text, labels, normal content
     */
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);

    /**
     * Text phụ - Gray (#7f8c8d)
     * Dùng cho: Secondary text, hints, placeholders
     */
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);

    /**
     * Text trắng - White
     * Dùng cho: Text trên background tối, button text
     */
    public static final Color TEXT_WHITE = Color.WHITE;

    // ==================== TABLE STATUS COLORS ====================

    /**
     * Màu bàn trống - Green (#2ecc71)
     * Trạng thái: TINHTRANG = 'false' (available)
     */
    public static final Color TABLE_AVAILABLE = new Color(46, 204, 113);

    /**
     * Màu bàn đang dùng - Red (#e74c3c)
     * Trạng thái: TINHTRANG = 'true' (occupied)
     */
    public static final Color TABLE_OCCUPIED = new Color(231, 76, 60);

    /**
     * Màu bàn đặt trước - Yellow (#f1c40f)
     * Trạng thái: Reserved (future feature)
     */
    public static final Color TABLE_RESERVED = new Color(241, 196, 15);

    // ==================== FONTS ====================

    /**
     * Font tiêu đề lớn - Segoe UI Bold 24pt
     * Dùng cho: Page titles, main headings
     */
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);

    /**
     * Font header - Segoe UI Bold 18pt
     * Dùng cho: Section headers, panel titles
     */
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);

    /**
     * Font subheader - Segoe UI Bold 16pt
     * Dùng cho: Subsection headers, card titles
     */
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);

    /**
     * Font thường - Segoe UI Plain 14pt
     * Dùng cho: Body text, labels, input fields
     */
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    /**
     * Font nhỏ - Segoe UI Plain 12pt
     * Dùng cho: Hints, footnotes, secondary info
     */
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Font button - Segoe UI Bold 14pt
     * Dùng cho: Button text
     */
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // ==================== DIMENSIONS ====================

    /**
     * Kích thước button chuẩn - 120x40
     * Dùng cho: Các button thông thường (OK, Cancel, Save)
     */
    public static final Dimension BUTTON_SIZE = new Dimension(120, 40);

    /**
     * Kích thước button lớn - 150x45
     * Dùng cho: Primary actions, important buttons
     */
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(150, 45);

    /**
     * Kích thước button nhỏ - 100x35
     * Dùng cho: Secondary actions, toolbar buttons
     */
    public static final Dimension SMALL_BUTTON_SIZE = new Dimension(100, 35);

    /**
     * Kích thước button bàn ăn - 150x120
     * Dùng cho: Hiển thị bàn trong TableManagementPanel
     */
    public static final Dimension TABLE_BUTTON_SIZE = new Dimension(150, 120);

    // ==================== SPACING ====================

    /**
     * Padding nhỏ - 5px
     * Dùng cho: Tight spacing, compact layouts
     */
    public static final int PADDING_SMALL = 5;

    /**
     * Padding trung bình - 10px
     * Dùng cho: Normal spacing, default padding
     */
    public static final int PADDING_MEDIUM = 10;

    /**
     * Padding lớn - 20px
     * Dùng cho: Section spacing, panel margins
     */
    public static final int PADDING_LARGE = 20;

    // ==================== BORDER ====================

    /**
     * Border radius - 8px
     * Dùng cho: Rounded corners (buttons, panels, cards)
     */
    public static final int BORDER_RADIUS = 8;

    // ==================== FACTORY METHODS ====================

    /**
     * Tạo button với style tùy chỉnh
     *
     * Features:
     * - Font: FONT_BUTTON (Segoe UI Bold 14pt)
     * - Foreground: TEXT_WHITE (white text)
     * - Size: BUTTON_SIZE (120x40)
     * - Cursor: Hand cursor khi hover
     * - Hover effect: Background tối hơn khi hover
     * - No focus border: setFocusPainted(false)
     * - No border: setBorderPainted(false)
     *
     * Sử dụng khi: Cần tạo button với màu custom
     *
     * @param text Text hiển thị trên button
     * @param bgColor Màu background của button
     * @return JButton đã được style
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(TEXT_WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(BUTTON_SIZE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect: Tối màu background khi hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    /**
     * Tạo primary button (màu xanh dương)
     *
     * Sử dụng cho: Main actions (Lưu, Xác nhận, Đăng nhập)
     *
     * @param text Text hiển thị trên button
     * @return JButton với PRIMARY_COLOR background
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }

    /**
     * Tạo success button (màu xanh lá)
     *
     * Sử dụng cho: Positive actions (Thêm, Tạo mới, Hoàn thành)
     *
     * @param text Text hiển thị trên button
     * @return JButton với SUCCESS_COLOR background
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR);
    }

    /**
     * Tạo danger button (màu đỏ)
     *
     * Sử dụng cho: Destructive actions (Xóa, Hủy, Từ chối)
     *
     * @param text Text hiển thị trên button
     * @return JButton với DANGER_COLOR background
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR);
    }

    /**
     * Tạo warning button (màu vàng)
     *
     * Sử dụng cho: Caution actions (Cảnh báo, Đặt trước)
     *
     * @param text Text hiển thị trên button
     * @return JButton với WARNING_COLOR background
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING_COLOR);
    }

    /**
     * Tạo secondary button (màu xanh nhạt)
     *
     * Sử dụng cho: Secondary actions (Hủy, Quay lại)
     *
     * @param text Text hiển thị trên button
     * @return JButton với SECONDARY_COLOR background
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR);
    }

    /**
     * Tạo info button (màu xanh nhạt)
     *
     * Sử dụng cho: Info actions (Xem chi tiết, Trợ giúp)
     *
     * @param text Text hiển thị trên button
     * @return JButton với INFO_COLOR background
     */
    public static JButton createInfoButton(String text) {
        return createStyledButton(text, INFO_COLOR);
    }

    /**
     * Tạo panel với title và border
     *
     * Features:
     * - Layout: BorderLayout
     * - Background: BG_SECONDARY (white)
     * - Border: PRIMARY_COLOR line border (2px) + padding
     * - Title: FONT_HEADER, PRIMARY_COLOR, ở vị trí NORTH
     * - Padding: PADDING_MEDIUM (10px) all sides
     *
     * Sử dụng khi: Tạo section panel với title rõ ràng
     *
     * @param title Tiêu đề panel (null hoặc empty = không có title)
     * @return JPanel đã được style với title
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(FONT_HEADER);
            lblTitle.setForeground(PRIMARY_COLOR);
            lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING_MEDIUM, 0));
            panel.add(lblTitle, BorderLayout.NORTH);
        }

        return panel;
    }

    /**
     * Tạo text field với style nhất quán
     *
     * Features:
     * - Font: FONT_NORMAL (Segoe UI Plain 14pt)
     * - Border: PRIMARY_COLOR line border (1px) + padding
     * - Padding: PADDING_SMALL top/bottom, PADDING_MEDIUM left/right
     *
     * Sử dụng khi: Tạo input field trong forms
     *
     * @return JTextField đã được style
     */
    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(FONT_NORMAL);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        return textField;
    }

    /**
     * Tạo label với font và màu tùy chỉnh
     *
     * Sử dụng khi: Cần tạo label với style cụ thể
     *
     * @param text Text hiển thị
     * @param font Font của label (VD: FONT_HEADER, FONT_NORMAL)
     * @param color Màu text (VD: TEXT_PRIMARY, PRIMARY_COLOR)
     * @return JLabel đã được style
     */
    public static JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
}
