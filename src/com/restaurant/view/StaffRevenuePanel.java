package com.restaurant.view;

import com.restaurant.dao.DonDatDAO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel doanh thu cá nhân của nhân viên
 * Chỉ hiển thị các đơn hàng do nhân viên này tạo
 * Giao diện: Filter ngày + Stats cards (Doanh thu, Số đơn, TB/đơn) + Table đơn hàng
 */
public class StaffRevenuePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private DonDatDAO donDatDAO;
    private JTable tableOrders;
    private DefaultTableModel orderModel;
    private JLabel lblRevenue, lblOrderCount, lblAvgOrder;
    private JTextField txtFromDate, txtToDate;

    /**
     * Constructor: Khởi tạo panel doanh thu cá nhân
     */
    public StaffRevenuePanel() {
        donDatDAO = new DonDatDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("DOANH THU CÁ NHÂN");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));     // BorderLayout: Top (NORTH) + Table (CENTER)
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel topSection = new JPanel(new BorderLayout(10, 10));       // BorderLayout: Filter (NORTH) + Stats (CENTER)
        topSection.setBackground(UIConstants.BG_PRIMARY);

        JPanel filterPanel = createFilterPanel();                        // Panel filter theo ngày
        topSection.add(filterPanel, BorderLayout.NORTH);

        JPanel statsPanel = createStatsPanel();                          // Panel 3 cards thống kê
        topSection.add(statsPanel, BorderLayout.CENTER);

        contentPanel.add(topSection, BorderLayout.NORTH);

        JPanel ordersPanel = createOrdersPanel();                        // Panel table danh sách đơn hàng
        contentPanel.add(ordersPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadStatistics();                                                // Load dữ liệu thống kê
    }

    /**
     * Tạo panel filter theo ngày (Từ - Đến)
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // FlowLayout ngang
        panel.setBackground(UIConstants.BG_PRIMARY);

        JLabel lblFilter = new JLabel("Lọc theo ngày:");
        lblFilter.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblFilter);

        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblFrom);

        txtFromDate = UIConstants.createStyledTextField();
        txtFromDate.setText(LocalDate.now().toString());                 // Mặc định là hôm nay
        txtFromDate.setPreferredSize(new Dimension(120, 35));
        panel.add(txtFromDate);

        JLabel lblTo = new JLabel("Đến:");
        lblTo.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblTo);

        txtToDate = UIConstants.createStyledTextField();
        txtToDate.setText(LocalDate.now().toString());
        txtToDate.setPreferredSize(new Dimension(120, 35));
        panel.add(txtToDate);

        JButton btnFilter = UIConstants.createPrimaryButton("Lọc");
        btnFilter.addActionListener(e -> loadStatistics());              // Lọc theo khoảng ngày đã chọn
        panel.add(btnFilter);

        JButton btnToday = UIConstants.createSecondaryButton("Hôm nay");
        btnToday.addActionListener(e -> {
            String today = LocalDate.now().toString();
            txtFromDate.setText(today);                                  // Set cả 2 field = hôm nay
            txtToDate.setText(today);
            loadStatistics();                                            // Load lại dữ liệu
        });
        panel.add(btnToday);

        JButton btnRefresh = UIConstants.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadStatistics());             // Refresh dữ liệu
        panel.add(btnRefresh);

        return panel;
    }

    /**
     * Tạo panel 3 cards thống kê (Doanh thu, Số đơn, TB/đơn)
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));        // Grid 1 hàng 3 cột
        panel.setBackground(UIConstants.BG_PRIMARY);

        // Card 1: Doanh thu của tôi
        lblRevenue = new JLabel("0 VNĐ", SwingConstants.CENTER);
        panel.add(createStatCard("Doanh thu của tôi", lblRevenue, "💰", UIConstants.SUCCESS_COLOR));

        // Order count card
        lblOrderCount = new JLabel("0", SwingConstants.CENTER);
        panel.add(createStatCard("Số đơn đã phục vụ", lblOrderCount, "📋", UIConstants.INFO_COLOR));

        // Average order card
        lblAvgOrder = new JLabel("0 VNĐ", SwingConstants.CENTER);
        panel.add(createStatCard("Trung bình/đơn", lblAvgOrder, "📊", UIConstants.WARNING_COLOR));

        return panel;
    }

    private JPanel createStatCard(String label, JLabel valueLabel, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UIConstants.BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Icon with padding to prevent clipping
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);

        // Content panel
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        contentPanel.setBackground(UIConstants.BG_SECONDARY);

        JLabel lblTitle = new JLabel(label, SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_SUBHEADER);
        lblTitle.setForeground(UIConstants.TEXT_PRIMARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        contentPanel.add(lblTitle);
        contentPanel.add(valueLabel);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIConstants.BG_PRIMARY);

        // Title
        JLabel lblTitle = new JLabel("Lịch sử đơn hàng của tôi");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Orders table
        String[] orderColumns = {"Mã đơn", "Bàn", "Ngày", "Tổng tiền", "Trạng thái"};
        orderModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableOrders = new JTable(orderModel);
        tableOrders.setFont(UIConstants.FONT_NORMAL);
        tableOrders.setRowHeight(35);
        tableOrders.setBackground(Color.WHITE);
        tableOrders.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableOrders.setSelectionForeground(UIConstants.TEXT_WHITE);
        tableOrders.setShowGrid(true);
        tableOrders.setGridColor(new Color(220, 225, 230));
        tableOrders.setIntercellSpacing(new Dimension(1, 1));

        // Header styling with custom renderer
        tableOrders.getTableHeader().setFont(UIConstants.FONT_SUBHEADER);
        tableOrders.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tableOrders.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setFont(UIConstants.FONT_SUBHEADER);
                label.setBackground(new Color(41, 128, 185));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(220, 225, 230)),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Double-click to view invoice for completed orders
        tableOrders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = tableOrders.getSelectedRow();
                    if (selectedRow != -1) {
                        int maDonDat = (int) orderModel.getValueAt(selectedRow, 0);
                        String tinhTrang = (String) orderModel.getValueAt(selectedRow, 4);

                        if ("completed".equals(tinhTrang)) {
                            showInvoice(maDonDat);
                        } else {
                            JOptionPane.showMessageDialog(StaffRevenuePanel.this,
                                "Chỉ có thể xem hóa đơn của đơn hàng đã hoàn thành!",
                                "Thông báo",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        // Column widths
        tableOrders.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableOrders.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableOrders.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableOrders.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableOrders.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tableOrders);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UIConstants.BG_PRIMARY);

        JButton btnPrintInvoice = UIConstants.createPrimaryButton("In hóa đơn");
        btnPrintInvoice.setPreferredSize(new Dimension(150, 38));
        btnPrintInvoice.addActionListener(e -> {
            int selectedRow = tableOrders.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đơn hàng cần in!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int maDonDat = (int) orderModel.getValueAt(selectedRow, 0);
            String tinhTrang = (String) orderModel.getValueAt(selectedRow, 4);

            if ("completed".equals(tinhTrang)) {
                showInvoice(maDonDat);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Chỉ có thể in hóa đơn của đơn hàng đã hoàn thành!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnPrintInvoice);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showInvoice(int maDonDat) {
        DonDatDTO donDat = donDatDAO.getDonDatById(maDonDat);
        if (donDat != null) {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new InvoiceDialog(parentFrame, donDat);
        }
    }

    public void loadStatistics() {
        String fromDate = txtFromDate.getText().trim();
        String toDate = txtToDate.getText().trim();

        // Get current staff ID
        int currentStaffId = SessionManager.getInstance().getCurrentUser().getMaNV();

        // Load revenue and order count for this staff only
        double totalRevenue = 0;
        int totalOrders = 0;

        // Get all orders in date range for this staff
        orderModel.setRowCount(0);
        List<DonDatDTO> orders = donDatDAO.getDonDatByDateRange(fromDate, toDate);

        for (DonDatDTO order : orders) {
            // Only show orders created by this staff
            if (order.getMaNV() == currentStaffId) {
                if ("completed".equals(order.getTinhTrang())) {
                    totalRevenue += order.getTongTien();
                    totalOrders++;
                }

                orderModel.addRow(new Object[]{
                    order.getMaDonDat(),
                    order.getMaBan(),
                    order.getNgayDat(),
                    String.format("%,.0f VNĐ", order.getTongTien()),
                    order.getTinhTrang()
                });
            }
        }

        // Calculate average order value
        double avgOrder = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Update stat cards
        lblRevenue.setText(String.format("%,.0f VNĐ", totalRevenue));
        lblOrderCount.setText(String.valueOf(totalOrders));
        lblAvgOrder.setText(String.format("%,.0f VNĐ", avgOrder));
    }
}
