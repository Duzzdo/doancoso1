package com.restaurant.view;

import com.restaurant.dao.BanAnDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.dao.NhanVienDAO;
import com.restaurant.model.BanAnDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý bàn ăn - Hiển thị grid các bàn với trạng thái và thao tác
 *
 * Layout: Grid 5 cột, mỗi bàn là 1 button với màu theo trạng thái
 * - Xanh lá: Bàn trống
 * - Đỏ: Bàn đang sử dụng (hiển thị tên nhân viên phục vụ)
 *
 * Thao tác:
 * - Bàn trống: Tạo đơn mới, Sửa bàn, Xóa bàn
 * - Bàn đang dùng: Xem đơn hàng (chỉ nhân viên phục vụ bàn đó)
 *
 * Security: Staff chỉ được thao tác với bàn của mình
 */
public class TableManagementPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private BanAnDAO banAnDAO;
    private DonDatDAO donDatDAO;
    private JPanel tablesPanel;

    public TableManagementPanel() {
        banAnDAO = new BanAnDAO();
        donDatDAO = new DonDatDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel: Title + buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(UIConstants.BG_PRIMARY);

        JLabel title = new JLabel("QUẢN LÝ BÀN ĂN");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIConstants.BG_PRIMARY);

        JButton btnAdd = UIConstants.createSuccessButton("+ Thêm bàn");
        btnAdd.addActionListener(e -> handleAddTable());

        JButton btnRefresh = UIConstants.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadTables());

        btnPanel.add(btnAdd);
        btnPanel.add(btnRefresh);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center panel: Grid 5 cột
        tablesPanel = new JPanel(new GridLayout(0, 5, 15, 15));
        tablesPanel.setBackground(UIConstants.BG_PRIMARY);
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(tablesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIConstants.BG_PRIMARY);
        add(scrollPane, BorderLayout.CENTER);

        loadTables();
    }

    /**
     * Load tất cả bàn từ database và hiển thị trong grid
     */
    private void loadTables() {
        // Xóa tất cả buttons bàn cũ trước khi load lại
        tablesPanel.removeAll();
        // Lấy danh sách tất cả bàn từ database
        List<BanAnDTO> tables = banAnDAO.getAllBanAn();

        // Duyệt qua từng bàn trong danh sách
        for (BanAnDTO ban : tables) {
            // Tạo button với thông tin của bàn
            JButton btnTable = createTableButton(ban);
            // Thêm button vào grid panel
            tablesPanel.add(btnTable);
        }

        // Refresh layout để hiển thị các button mới
        tablesPanel.revalidate();
        // Vẽ lại panel
        tablesPanel.repaint();
    }

    /**
     * Tạo button cho mỗi bàn với màu theo trạng thái
     * - Xanh lá: Trống
     * - Đỏ: Đang dùng (hiển thị tên nhân viên phục vụ)
     */
    private JButton createTableButton(BanAnDTO ban) {
        String buttonText = "<html><center><b>" + ban.getTenBan() + "</b><br><br>";

        if (ban.isOccupied()) {
            buttonText += "<font size='4'>Đang dùng bàn</font>";

            // Hiển thị tên nhân viên đang phục vụ
            DonDatDTO pendingOrder = donDatDAO.getPendingDonDatByBan(ban.getMaBan());
            if (pendingOrder != null) {
                NhanVienDAO nhanVienDAO = new NhanVienDAO();
                NhanVienDTO staff = nhanVienDAO.getNhanVienById(pendingOrder.getMaNV());
                if (staff != null) {
                    buttonText += "<br><font size='3'>(" + staff.getHoTenNV() + " đang phục vụ)</font>";
                }
            }
        } else {
            buttonText += "<font size='4'>Trống</font>";
        }

        buttonText += "</center></html>";
        JButton btnTable = new JButton(buttonText);

        btnTable.setPreferredSize(UIConstants.TABLE_BUTTON_SIZE);
        btnTable.setFont(UIConstants.FONT_NORMAL);
        btnTable.setFocusPainted(false);
        btnTable.setBorderPainted(false);
        btnTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color bgColor = ban.isOccupied() ? UIConstants.TABLE_OCCUPIED : UIConstants.TABLE_AVAILABLE;
        btnTable.setBackground(bgColor);
        btnTable.setForeground(UIConstants.TEXT_WHITE);

        // Hover effect
        btnTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTable.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTable.setBackground(bgColor);
            }
        });

        btnTable.addActionListener(e -> handleTableClick(ban));

        return btnTable;
    }

    /**
     * Xử lý khi click vào bàn
     * - Bàn trống: Tạo đơn mới, Sửa bàn, Xóa bàn
     * - Bàn đang dùng: Xem đơn hàng (chỉ nhân viên phục vụ bàn đó)
     */
    private void handleTableClick(BanAnDTO ban) {
        // Kiểm tra bàn có đang được sử dụng
        if (ban.isOccupied()) {
            // Lấy order đang chờ của bàn này
            DonDatDTO pendingOrder = donDatDAO.getPendingDonDatByBan(ban.getMaBan());

            if (pendingOrder != null) {
                // Lấy ID nhân viên hiện tại đang đăng nhập
                int currentStaffId = SessionManager.getInstance().getCurrentUser().getMaNV();

                // Security check: Chỉ nhân viên phục vụ bàn này mới được xem order
                if (pendingOrder.getMaNV() != currentStaffId) {
                    // Hiển thị thông báo lỗi nếu không phải nhân viên phục vụ
                    JOptionPane.showMessageDialog(this,
                            "Bàn này đang có nhân viên khác phục vụ!",
                            "Không có quyền truy cập",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Hiển thị dialog chọn hành động cho bàn đang sử dụng
            String[] options = new String[]{"Xem đơn hàng", "Hủy"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Chọn thao tác cho " + ban.getTenBan(),
                    "Quản lý bàn",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            // Nếu chọn "Xem đơn hàng"
            if (choice == 0) viewOrder(ban);
        } else {
            // Bàn trống - cho phép tất cả staff thao tác
            // Hiển thị dialog chọn hành động
            String[] options = new String[]{"Tạo đơn mới", "Sửa bàn", "Xóa bàn", "Hủy"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Chọn thao tác cho " + ban.getTenBan(),
                    "Quản lý bàn",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            // Xử lý theo lựa chọn
            if (choice == 0) createOrder(ban);
            else if (choice == 1) editTable(ban);
            else if (choice == 2) deleteTable(ban);
        }
    }

    private void handleAddTable() {
        String tenBan = JOptionPane.showInputDialog(this, "Nhập tên bàn:");
        if (tenBan != null && !tenBan.trim().isEmpty()) {
            if (banAnDAO.insertBanAn(tenBan.trim())) {
                JOptionPane.showMessageDialog(this, "Thêm bàn thành công!");
                loadTables();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm bàn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTable(BanAnDTO ban) {
        String newName = JOptionPane.showInputDialog(this, "Nhập tên mới:", ban.getTenBan());
        if (newName != null && !newName.trim().isEmpty()) {
            if (banAnDAO.updateTenBan(ban.getMaBan(), newName.trim())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadTables();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTable(BanAnDTO ban) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa " + ban.getTenBan() + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (banAnDAO.deleteBanAn(ban.getMaBan())) {
                JOptionPane.showMessageDialog(this, "Xóa bàn thành công!");
                loadTables();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa bàn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Tạo đơn hàng mới cho bàn trống
     * Flow: Insert DonDat → Update trạng thái bàn → Mở OrderDialog
     */
    private void createOrder(BanAnDTO ban) {
        System.out.println("=== CREATE ORDER START ===");

        if (SessionManager.getInstance().getCurrentUser() == null) {
            System.err.println("ERROR: No user logged in!");
            JOptionPane.showMessageDialog(this, "Lỗi: Chưa đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maNV = SessionManager.getInstance().getCurrentUser().getMaNV();
        String ngayDat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("User info - MaNV: " + maNV);
        System.out.println("Table info - MaBan: " + ban.getMaBan());
        System.out.println("Date: " + ngayDat);

        DonDatDTO donDat = new DonDatDTO();
        donDat.setMaBan(ban.getMaBan());
        donDat.setMaNV(maNV);
        donDat.setNgayDat(ngayDat);
        donDat.setTongTien(0);
        donDat.setTinhTrang("pending");

        System.out.println("Calling insertDonDat...");
        int maDonDat = donDatDAO.insertDonDat(donDat);
        System.out.println("Insert result - MaDonDat: " + maDonDat);

        if (maDonDat > 0) {
            System.out.println("Updating table status...");
            boolean updateResult = banAnDAO.updateTinhTrangBan(ban.getMaBan(), "true");
            System.out.println("Update table status result: " + updateResult);

            SwingUtilities.invokeLater(() -> {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                new OrderDialog(parentFrame, maDonDat);
                loadTables();
                refreshMainFrameHome();
            });
        } else {
            System.err.println("ERROR: Failed to insert order!");
            JOptionPane.showMessageDialog(this, "Tạo đơn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        System.out.println("=== CREATE ORDER END ===");
    }

    /**
     * Xem chi tiết đơn hàng của bàn đang sử dụng
     * Options: Chỉnh sửa đơn, Thanh toán, Hủy đơn
     */
    private void viewOrder(BanAnDTO ban) {
        DonDatDTO donDat = donDatDAO.getPendingDonDatByBan(ban.getMaBan());
        if (donDat != null) {
            String[] options = {"Chỉnh sửa đơn", "Thanh toán", "Hủy đơn", "Đóng"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Mã đơn: " + donDat.getMaDonDat() + "\n" +
                    "Bàn: " + ban.getTenBan() + "\n" +
                    "Ngày đặt: " + donDat.getNgayDat() + "\n" +
                    "Tổng tiền: " + String.format("%,.0f VNĐ", donDat.getTongTien()),
                    "Chi tiết đơn hàng",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) {
                try {
                    Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    System.out.println("Opening OrderDialog for order: " + donDat.getMaDonDat());
                    new OrderDialog(parentFrame, donDat.getMaDonDat());
                    loadTables();
                    refreshMainFrameHome();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Lỗi khi mở dialog: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 1) {
                payOrder(donDat, ban);
            } else if (choice == 2) {
                cancelOrder(donDat, ban);
            }
        }
    }

    /**
     * Thanh toán đơn hàng
     * Update: DonDat.tinhTrang = "completed", BanAn.tinhTrang = "false"
     */
    private void payOrder(DonDatDTO donDat, BanAnDTO ban) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán " + String.format("%,.0f VNĐ", donDat.getTongTien()) + "?",
                "Thanh toán",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            donDatDAO.updateTinhTrangDonDat(donDat.getMaDonDat(), "completed");
            banAnDAO.updateTinhTrangBan(ban.getMaBan(), "false");
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
            loadTables();
            refreshMainFrameHome();
        }
    }

    /**
     * Hủy đơn hàng
     * Update: DonDat.tinhTrang = "cancelled", BanAn.tinhTrang = "false"
     */
    private void cancelOrder(DonDatDTO donDat, BanAnDTO ban) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy đơn hàng này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            donDatDAO.updateTinhTrangDonDat(donDat.getMaDonDat(), "cancelled");
            banAnDAO.updateTinhTrangBan(ban.getMaBan(), "false");
            JOptionPane.showMessageDialog(this, "Đã hủy đơn hàng!");
            loadTables();
            refreshMainFrameHome();
        }
    }

    private void refreshMainFrameHome() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainFrame) {
            ((MainFrame) window).refreshHomePanel();
        }
    }
}
