package com.restaurant.view;

import com.restaurant.dao.BanAnDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.dao.ChiTietDonDatDAO;
import com.restaurant.dao.MonDAO;
import com.restaurant.model.BanAnDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.ChiTietDonDatDTO;
import com.restaurant.model.MonDTO;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel thanh toán đơn hàng
 * Giao diện: Thông tin đơn + Danh sách món + Tổng tiền + Buttons thanh toán
 */
public class PaymentPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private DonDatDAO donDatDAO;
    private ChiTietDonDatDAO chiTietDonDatDAO;
    private BanAnDAO banAnDAO;
    private MonDAO monDAO;
    private DonDatDTO currentOrder;
    private JTable tableItems;
    private DefaultTableModel itemTableModel;
    private JLabel lblOrderId;
    private JLabel lblTableName;
    private JLabel lblStaff;
    private JLabel lblOrderDate;
    private JLabel lblTotalAmount;

    /**
     * Constructor: Khởi tạo panel thanh toán
     */
    public PaymentPanel() {
        donDatDAO = new DonDatDAO();
        chiTietDonDatDAO = new ChiTietDonDatDAO();
        banAnDAO = new BanAnDAO();
        monDAO = new MonDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER) + Buttons (SOUTH)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("💳 THANH TOÁN");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));     // BorderLayout: Info (NORTH) + Table (CENTER) + Total (SOUTH)
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel infoPanel = createOrderInfoPanel();                      // Panel thông tin đơn hàng
        contentPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel tablePanel = createItemsTablePanel();                    // Panel danh sách món
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        JPanel totalPanel = createTotalPanel();                         // Panel tổng tiền
        contentPanel.add(totalPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // FlowLayout ngang cho 3 buttons
        btnPanel.setBackground(UIConstants.BG_PRIMARY);

        JButton btnSelectOrder = UIConstants.createPrimaryButton("🔍 Chọn đơn hàng");
        btnSelectOrder.addActionListener(e -> handleSelectOrder());     // Mở dialog chọn đơn

        JButton btnPayment = UIConstants.createSuccessButton("💰 Thanh toán");
        btnPayment.addActionListener(e -> handlePayment());             // Xử lý thanh toán

        JButton btnCancel = UIConstants.createSecondaryButton("❌ Hủy");
        btnCancel.addActionListener(e -> clearPayment());               // Xóa thông tin thanh toán

        btnPanel.add(btnSelectOrder);
        btnPanel.add(btnPayment);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Tạo panel hiển thị thông tin đơn hàng
     */
    private JPanel createOrderInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));       // Grid 4 hàng 2 cột
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblOrderIdLabel = new JLabel("Mã đơn:");
        lblOrderIdLabel.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblOrderIdLabel);

        lblOrderId = new JLabel("-");
        lblOrderId.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblOrderId);

        JLabel lblTableLabel = new JLabel("Bàn:");
        lblTableLabel.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblTableLabel);

        lblTableName = new JLabel("-");
        lblTableName.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblTableName);

        JLabel lblStaffLabel = new JLabel("Nhân viên:");
        lblStaffLabel.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblStaffLabel);

        lblStaff = new JLabel("-");
        lblStaff.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblStaff);

        JLabel lblDateLabel = new JLabel("Ngày đặt:");
        lblDateLabel.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblDateLabel);

        lblOrderDate = new JLabel("-");
        lblOrderDate.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblOrderDate);

        return panel;
    }

    /**
     * Tạo panel hiển thị danh sách món trong đơn
     */
    private JPanel createItemsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());                  // BorderLayout: Title (NORTH) + Table (CENTER)
        panel.setBackground(UIConstants.BG_PRIMARY);

        JLabel lblTitle = new JLabel("Chi tiết món:");
        lblTitle.setFont(UIConstants.FONT_SUBHEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Tên món", "Số lượng", "Đơn giá", "Thành tiền"};
        itemTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        tableItems = new JTable(itemTableModel);
        tableItems.setFont(UIConstants.FONT_NORMAL);
        tableItems.setRowHeight(40);
        tableItems.getTableHeader().setFont(UIConstants.FONT_SUBHEADER);
        tableItems.getTableHeader().setBackground(UIConstants.PRIMARY_COLOR);
        tableItems.getTableHeader().setForeground(UIConstants.TEXT_WHITE);
        tableItems.getTableHeader().setOpaque(true);
        tableItems.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableItems.setSelectionForeground(UIConstants.TEXT_WHITE);

        JScrollPane scrollPane = new JScrollPane(tableItems);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo panel hiển thị tổng tiền
     */
    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // FlowLayout ngang, căn phải
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTotalLabel = new JLabel("TỔNG TIỀN:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalLabel.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lblTotalLabel);

        lblTotalAmount = new JLabel("0 VNĐ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalAmount.setForeground(UIConstants.DANGER_COLOR);
        panel.add(lblTotalAmount);

        return panel;
    }

    /**
     * Xử lý chọn đơn hàng cần thanh toán
     */
    private void handleSelectOrder() {
        List<DonDatDTO> pendingOrders = donDatDAO.getAllDonDat().stream()
            // Filter chỉ lấy đơn với tình trạng = "pending"
            .filter(order -> "pending".equals(order.getTinhTrang()))
            // Chuyển stream thành list
            .toList();

        // Kiểm tra danh sách có đơn nào chưa
        if (pendingOrders.isEmpty()) {
            // Hiển thị thông báo không có đơn cần thanh toán
            JOptionPane.showMessageDialog(this, "Không có đơn hàng nào cần thanh toán!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tạo danh sách options (display strings) từ danh sách đơn
        String[] orderOptions = pendingOrders.stream()
            // Map từng đơn sang string hiển thị
            .map(order -> {
                // Lấy thông tin bàn từ database
                BanAnDTO ban = banAnDAO.getBanAnById(order.getMaBan());
                // Lấy tên bàn, nếu không tìm thấy thì dùng "N/A"
                String tenBan = ban != null ? ban.getTenBan() : "N/A";
                // Return string format: "Đơn #1 - Bàn A1 - 150,000 VNĐ"
                return String.format("Đơn #%d - Bàn %s - %,.0f VNĐ",
                    order.getMaDonDat(), tenBan, order.getTongTien());
            })
            // Chuyển stream thành array
            .toArray(String[]::new);

        // Hiển thị dialog chọn đơn hàng
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Chọn đơn hàng cần thanh toán:",
            "Chọn đơn hàng",
            JOptionPane.QUESTION_MESSAGE,
            null,
            // Danh sách các option để chọn
            orderOptions,
            // Option mặc định (item đầu tiên)
            orderOptions[0]
        );

        // Kiểm tra user đã chọn (không cancel)
        if (selected != null) {
            // Tìm index của đơn đã chọn trong danh sách options
            int index = java.util.Arrays.asList(orderOptions).indexOf(selected);
            // Load thông tin đơn vào panel thanh toán
            loadOrderForPayment(pendingOrders.get(index));
        }
    }

    /**
     * Load thông tin đơn hàng để thanh toán
     */
    private void loadOrderForPayment(DonDatDTO order) {
        this.currentOrder = order;

        // Cập nhật thông tin đơn hàng
        lblOrderId.setText(String.valueOf(order.getMaDonDat()));

        BanAnDTO ban = banAnDAO.getBanAnById(order.getMaBan());         // Lấy thông tin bàn
        lblTableName.setText(ban != null ? ban.getTenBan() : "N/A");

        lblStaff.setText(String.valueOf(order.getMaNV()));
        lblOrderDate.setText(order.getNgayDat());

        // Load danh sách món trong đơn
        itemTableModel.setRowCount(0);                                   // Xóa tất cả rows cũ
        List<ChiTietDonDatDTO> chiTietList = chiTietDonDatDAO.getChiTietByDonDat(order.getMaDonDat()); // Lấy chi tiết đơn

        for (ChiTietDonDatDTO ct : chiTietList) {                        // Duyệt qua từng món
            MonDTO mon = monDAO.getMonById(ct.getMaMon());               // Lấy thông tin món
            if (mon != null) {
                double thanhTien = mon.getGiaTien() * ct.getSoLuong();  // Tính thành tiền = giá × số lượng
                itemTableModel.addRow(new Object[]{                      // Thêm row vào table
                    mon.getTenMon(),
                    ct.getSoLuong(),
                    String.format("%,.0f VNĐ", mon.getGiaTien()),       // Format giá: 50,000 VNĐ
                    String.format("%,.0f VNĐ", thanhTien)
                });
            }
        }

        // Cập nhật tổng tiền
        lblTotalAmount.setText(String.format("%,.0f VNĐ", order.getTongTien()));
    }

    /**
     * Xử lý thanh toán đơn hàng
     */
    private void handlePayment() {
        // Kiểm tra nếu chưa chọn đơn hàng
        if (currentOrder == null) {
            // Hiển thị thông báo yêu cầu chọn đơn
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng cần thanh toán!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiển thị dialog xác nhận thanh toán
        int confirm = JOptionPane.showConfirmDialog(this,
                // Message hiển thị mã đơn và tổng tiền
                String.format("Xác nhận thanh toán đơn hàng #%d?\nTổng tiền: %,.0f VNĐ",
                    currentOrder.getMaDonDat(), currentOrder.getTongTien()),
                "Xác nhận thanh toán",
                // 2 options: Yes/No
                JOptionPane.YES_NO_OPTION,
                // Loại dialog: Question
                JOptionPane.QUESTION_MESSAGE);

        // Kiểm tra nếu user confirm thanh toán
        if (confirm == JOptionPane.YES_OPTION) {
            // Cập nhật trạng thái đơn hàng thành "completed" (đã thanh toán)
            currentOrder.setTinhTrang("completed");
            // Cập nhật vào database
            if (donDatDAO.updateDonDat(currentOrder)) {
                // Lấy thông tin bàn từ database
                BanAnDTO ban = banAnDAO.getBanAnById(currentOrder.getMaBan());
                // Kiểm tra nếu tìm thấy bàn
                if (ban != null) {
                    // Đặt trạng thái bàn thành "false" (trống)
                    ban.setTinhTrang("false");
                    // Cập nhật vào database
                    banAnDAO.updateBanAn(ban);
                }

                // Hiển thị thông báo thành công
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Xóa thông tin thanh toán hiện tại
                clearPayment();
                // Refresh dashboard để cập nhật dữ liệu
                refreshMainFrameHome();
            } else {
                // Hiển thị thông báo lỗi nếu update thất bại
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Refresh HomePanel trong MainFrame sau khi thanh toán
     */
    private void refreshMainFrameHome() {
        Window window = SwingUtilities.getWindowAncestor(this);         // Lấy window cha
        if (window instanceof MainFrame) {
            ((MainFrame) window).refreshHomePanel();                     // Gọi method refresh
        }
    }

    /**
     * Xóa thông tin thanh toán hiện tại
     */
    private void clearPayment() {
        currentOrder = null;
        lblOrderId.setText("-");
        lblTableName.setText("-");
        lblStaff.setText("-");
        lblOrderDate.setText("-");
        lblTotalAmount.setText("0 VNĐ");
        itemTableModel.setRowCount(0);                                   // Xóa tất cả rows trong table
    }

    /**
     * Load đơn hàng theo mã đơn (gọi từ bên ngoài)
     */
    public void loadOrderById(int maDonDat) {
        DonDatDTO order = donDatDAO.getDonDatById(maDonDat);            // Lấy đơn hàng từ database
        if (order != null && "pending".equals(order.getTinhTrang())) {  // Chỉ load nếu đơn đang pending
            loadOrderForPayment(order);                                  // Load thông tin đơn
        }
    }
}
