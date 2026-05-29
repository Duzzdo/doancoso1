package com.restaurant.view;

import com.restaurant.dao.BanAnDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.dao.NhanVienDAO;
import com.restaurant.model.BanAnDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel dashboard Admin - Giám sát bàn ăn và theo dõi nhân viên phục vụ
 * Giao diện: Title + Grid bàn ăn + Legend (chú thích màu)
 */
public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private BanAnDAO banAnDAO;
    private DonDatDAO donDatDAO;
    private NhanVienDAO nhanVienDAO;
    private JPanel tablesContainer;

    /**
     * Constructor: Khởi tạo panel dashboard Admin
     */
    public AdminDashboardPanel() {
        banAnDAO = new BanAnDAO();
        donDatDAO = new DonDatDAO();
        nhanVienDAO = new NhanVienDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Tables (CENTER) + Legend (SOUTH)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel titlePanel = new JPanel(new BorderLayout());             // BorderLayout: Title (WEST) + Button (EAST)
        titlePanel.setBackground(UIConstants.BG_PRIMARY);

        JLabel title = new JLabel("GIÁM SÁT BÀN ĂN - THEO DÕI NHÂN VIÊN");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);

        JButton btnRefresh = UIConstants.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadTables());                 // Refresh danh sách bàn

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(btnRefresh, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        JPanel legendPanel = createLegendPanel();     // Panel chú thích màu (xanh = trống, đỏ = đang phục vụ)
        add(legendPanel, BorderLayout.SOUTH);

        tablesContainer = new JPanel();
        tablesContainer.setLayout(new GridLayout(0, 4, 20, 20));   // Grid 4 cột, số hàng tự động
        tablesContainer.setBackground(UIConstants.BG_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(tablesContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UIConstants.BG_PRIMARY);
        scrollPane.getViewport().setBackground(UIConstants.BG_PRIMARY);

        add(scrollPane, BorderLayout.CENTER);

        loadTables();                                                    // Load danh sách bàn
    }

    /**
     * Tạo panel chú thích màu (legend)
     */
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10)); // FlowLayout ngang cho 2 items
        panel.setBackground(UIConstants.BG_PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Chú thích "Bàn trống" (màu xanh)
        JPanel availablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        availablePanel.setBackground(UIConstants.BG_PRIMARY);
        JLabel availableColor = new JLabel("   ");                      // Label nhỏ làm ô màu
        availableColor.setOpaque(true);
        availableColor.setBackground(new Color(46, 204, 113));          // Màu xanh lá
        availableColor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JLabel availableLabel = new JLabel("Bàn trống");
        availableLabel.setFont(UIConstants.FONT_NORMAL);
        availablePanel.add(availableColor);
        availablePanel.add(availableLabel);

        // Chú thích "Đang phục vụ" (màu đỏ)
        JPanel occupiedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        occupiedPanel.setBackground(UIConstants.BG_PRIMARY);
        JLabel occupiedColor = new JLabel("   ");
        occupiedColor.setOpaque(true);
        occupiedColor.setBackground(new Color(231, 76, 60));            // Màu đỏ
        occupiedColor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        JLabel occupiedLabel = new JLabel("Đang phục vụ");
        occupiedLabel.setFont(UIConstants.FONT_NORMAL);
        occupiedPanel.add(occupiedColor);
        occupiedPanel.add(occupiedLabel);

        panel.add(availablePanel);
        panel.add(occupiedPanel);

        return panel;
    }

    /**
     * Load danh sách bàn ăn từ database
     */
    private void loadTables() {
        tablesContainer.removeAll();                                     // Xóa tất cả cards cũ
        List<BanAnDTO> tables = banAnDAO.getAllBanAn();                 // Lấy tất cả bàn

        for (BanAnDTO table : tables) {                                  // Duyệt qua từng bàn
            tablesContainer.add(createTableCard(table));                 // Tạo card cho bàn và thêm vào grid
        }

        tablesContainer.revalidate();                                    // Refresh layout
        tablesContainer.repaint();
    }

    /**
     * Tạo card hiển thị thông tin bàn
     */
    private JPanel createTableCard(BanAnDTO table) {
        boolean isOccupied = "true".equals(table.getTinhTrang());       // true = đang phục vụ, false = trống
        Color bgColor = isOccupied ? new Color(231, 76, 60) : new Color(46, 204, 113); // Đỏ nếu đang phục vụ, xanh nếu trống

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));          // BoxLayout dọc
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 3),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        card.setPreferredSize(new Dimension(250, 200));

        JLabel lblTableName = new JLabel(table.getTenBan());            // Tên bàn (VD: "Bàn A1")
        lblTableName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTableName.setForeground(Color.WHITE);
        lblTableName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStatus = new JLabel(isOccupied ? "Đang phục vụ" : "Trống");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());                              // Căn giữa theo chiều dọc
        card.add(lblTableName);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(lblStatus);

        if (isOccupied) {                           // Nếu bàn đang phục vụ → hiển thị thêm thông tin đơn hàng
            DonDatDTO currentOrder = donDatDAO.getCurrentOrderByTable(table.getMaBan()); // Lấy đơn hàng hiện tại của bàn
            if (currentOrder != null) {
                NhanVienDTO staff = nhanVienDAO.getNhanVienById(currentOrder.getMaNV()); // Lấy thông tin nhân viên phục vụ
                String staffName = staff != null ? staff.getHoTenNV() : "Unknown";

                JLabel lblStaff = new JLabel(staffName);                // Tên nhân viên
                lblStaff.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblStaff.setForeground(Color.WHITE);
                lblStaff.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblTime = new JLabel(currentOrder.getNgayDat()); // Thời gian đặt
                lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblTime.setForeground(Color.WHITE);
                lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblAmount = new JLabel(String.format("%,.0f VNĐ", currentOrder.getTongTien())); // Tổng tiền
                lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblAmount.setForeground(Color.YELLOW);
                lblAmount.setAlignmentX(Component.CENTER_ALIGNMENT);

                card.add(Box.createRigidArea(new Dimension(0, 10)));
                card.add(new JSeparator());
                card.add(Box.createRigidArea(new Dimension(0, 10)));
                card.add(lblStaff);
                card.add(Box.createRigidArea(new Dimension(0, 5)));
                card.add(lblTime);
                card.add(Box.createRigidArea(new Dimension(0, 5)));
                card.add(lblAmount);
            }
        }

        card.add(Box.createVerticalGlue());

        return card;
    }
}
