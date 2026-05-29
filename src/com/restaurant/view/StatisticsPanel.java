package com.restaurant.view;

import com.restaurant.dao.ChiTietDonDatDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.dao.NhanVienDAO;
import com.restaurant.dao.StaffRankingDAO;
import com.restaurant.model.ChiTietDonDatDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.model.StaffRankingDTO;
import com.restaurant.utils.RankUtil;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel thống kê và báo cáo
 * Manager: Xem doanh thu tất cả nhân viên
 * Admin: Thống kê nâng cao với biểu đồ
 * Giao diện: Filter + Stats cards + Chart + Tables (Staff revenue, Orders, Best selling)
 */
public class StatisticsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private DonDatDAO donDatDAO;
    private ChiTietDonDatDAO chiTietDonDatDAO;
    private NhanVienDAO nhanVienDAO;
    private StaffRankingDAO staffRankingDAO;
    private JTable tableOrders, tableBestSelling, tableStaffRevenue;
    private DefaultTableModel orderModel, bestSellingModel, staffRevenueModel;
    private JLabel lblRevenue, lblOrderCount, lblAvgOrder;
    private JTextField txtFromDate, txtToDate;

    /**
     * Constructor: Khởi tạo panel thống kê
     */
    public StatisticsPanel() {
        donDatDAO = new DonDatDAO();
        chiTietDonDatDAO = new ChiTietDonDatDAO();
        nhanVienDAO = new NhanVienDAO();
        staffRankingDAO = new StaffRankingDAO();

        UIManager.put("TableHeader.background", new Color(41, 128, 185));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", UIConstants.FONT_SUBHEADER);

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title khác nhau cho Admin và Manager
        String titleText = SessionManager.getInstance().isAdmin() ?
            "THỐNG KÊ & BÁO CÁO (ADMIN)" : "THỐNG KÊ & BÁO CÁO (MANAGER)";
        JLabel title = new JLabel(titleText);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));     // BorderLayout: Top (NORTH) + Tables (CENTER)
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel topSection = new JPanel(new BorderLayout(10, 10));       // BorderLayout: Filter (NORTH) + Stats (CENTER)
        topSection.setBackground(UIConstants.BG_PRIMARY);

        JPanel filterPanel = createFilterPanel();                        // Panel filter theo ngày
        topSection.add(filterPanel, BorderLayout.NORTH);

        JPanel statsPanel = createStatsPanel();                          // Panel 3 cards: Doanh thu, Số đơn, Trung bình
        topSection.add(statsPanel, BorderLayout.CENTER);

        contentPanel.add(topSection, BorderLayout.NORTH);

        JPanel chartPanel = createRevenueChartPanel();                   // Panel biểu đồ doanh thu 7 ngày
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        chartPanel.setPreferredSize(new Dimension(0, 350));

        JPanel tablesSection = new JPanel();
        tablesSection.setLayout(new BoxLayout(tablesSection, BoxLayout.Y_AXIS)); // BoxLayout dọc cho các tables
        tablesSection.setBackground(UIConstants.BG_PRIMARY);

        tablesSection.add(chartPanel);
        tablesSection.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel staffRevenuePanel = createStaffRevenuePanel();            // Table doanh thu nhân viên
        staffRevenuePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        staffRevenuePanel.setPreferredSize(new Dimension(0, 250));
        tablesSection.add(staffRevenuePanel);
        tablesSection.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel ordersPanel = createOrdersPanel();                        // Table danh sách đơn hàng
        ordersPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        ordersPanel.setPreferredSize(new Dimension(0, 300));
        tablesSection.add(ordersPanel);
        tablesSection.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel bestSellingPanel = createBestSellingPanel();              // Table món bán chạy
        bestSellingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        bestSellingPanel.setPreferredSize(new Dimension(0, 250));
        tablesSection.add(bestSellingPanel);

        JScrollPane tablesScrollPane = new JScrollPane(tablesSection);
        tablesScrollPane.setBorder(null);
        tablesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(tablesScrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadStatistics();                                                // Load dữ liệu thống kê
    }

    /**
     * Tạo panel filter theo ngày (Từ ngày - Đến ngày)
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
        txtFromDate.setText(LocalDate.now().toString());                 // Mặc định là ngày hôm nay
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
        btnFilter.addActionListener(e -> loadStatistics());
        panel.add(btnFilter);

        JButton btnToday = UIConstants.createSecondaryButton("Hôm nay");
        btnToday.addActionListener(e -> {
            String today = LocalDate.now().toString();
            txtFromDate.setText(today);
            txtToDate.setText(today);
            loadStatistics();
        });
        panel.add(btnToday);

        JButton btnRefresh = UIConstants.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadStatistics());
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setBackground(UIConstants.BG_PRIMARY);

        // Revenue card
        lblRevenue = new JLabel("0 VNĐ", SwingConstants.CENTER);
        panel.add(createStatCard("Doanh thu", lblRevenue, "💰", UIConstants.SUCCESS_COLOR));

        // Order count card
        lblOrderCount = new JLabel("0", SwingConstants.CENTER);
        panel.add(createStatCard("Số đơn hàng", lblOrderCount, "📋", UIConstants.INFO_COLOR));

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
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
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
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title with count badge
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(UIConstants.BG_SECONDARY);

        JLabel lblTitle = new JLabel("Lịch sử đơn hàng");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        titlePanel.add(lblTitle);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Orders table
        String[] orderColumns = {"Mã đơn", "Bàn", "Ngày", "Tổng tiền", "Trạng thái"};
        orderModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableOrders = new JTable(orderModel) {
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setBackground(new Color(41, 128, 185));
                        setForeground(Color.WHITE);
                        setFont(UIConstants.FONT_SUBHEADER);
                    }
                };
            }
        };
        tableOrders.setFont(UIConstants.FONT_NORMAL);
        tableOrders.setRowHeight(40);
        tableOrders.setBackground(Color.WHITE);
        tableOrders.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableOrders.setSelectionForeground(UIConstants.TEXT_WHITE);
        tableOrders.setShowGrid(true);
        tableOrders.setGridColor(new Color(230, 235, 240));
        tableOrders.setIntercellSpacing(new Dimension(2, 2));

        // Center align all cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableOrders.getColumnCount(); i++) {
            tableOrders.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Force header styling with custom renderer
        javax.swing.table.JTableHeader header = tableOrders.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
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
                            JOptionPane.showMessageDialog(StatisticsPanel.this,
                                "Chỉ có thể xem hóa đơn của đơn hàng đã hoàn thành!",
                                "Thông báo",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        // Column widths
        tableOrders.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableOrders.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableOrders.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableOrders.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableOrders.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tableOrders);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showInvoice(int maDonDat) {
        DonDatDTO donDat = donDatDAO.getDonDatById(maDonDat);
        if (donDat != null) {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new InvoiceDialog(parentFrame, donDat);
        }
    }

    private JPanel createStaffRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel lblTitle = new JLabel("Doanh thu theo nhân viên");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Staff revenue table
        String[] staffRevenueColumns = {"STT", "Tên nhân viên", "Số đơn", "Tổng doanh thu", "Trung bình/đơn", "Xếp hạng"};
        staffRevenueModel = new DefaultTableModel(staffRevenueColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableStaffRevenue = new JTable(staffRevenueModel) {
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setBackground(new Color(41, 128, 185));
                        setForeground(Color.WHITE);
                        setFont(UIConstants.FONT_SUBHEADER);
                    }
                };
            }
        };
        tableStaffRevenue.setFont(UIConstants.FONT_NORMAL);
        tableStaffRevenue.setRowHeight(40);
        tableStaffRevenue.setBackground(Color.WHITE);
        tableStaffRevenue.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableStaffRevenue.setSelectionForeground(UIConstants.TEXT_WHITE);
        tableStaffRevenue.setShowGrid(true);
        tableStaffRevenue.setGridColor(new Color(230, 235, 240));
        tableStaffRevenue.setIntercellSpacing(new Dimension(2, 2));

        // Center align all cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableStaffRevenue.getColumnCount(); i++) {
            tableStaffRevenue.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Header styling with custom renderer
        javax.swing.table.JTableHeader staffHeader = tableStaffRevenue.getTableHeader();
        staffHeader.setPreferredSize(new Dimension(0, 45));
        staffHeader.setReorderingAllowed(false);
        staffHeader.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Column widths
        tableStaffRevenue.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableStaffRevenue.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableStaffRevenue.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableStaffRevenue.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableStaffRevenue.getColumnModel().getColumn(4).setPreferredWidth(150);
        tableStaffRevenue.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tableStaffRevenue);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBestSellingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel lblTitle = new JLabel("Món bán chạy");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Best selling table
        String[] bestSellingColumns = {"Mã món", "Tên món", "Số lượng đã bán", "Doanh thu"};
        bestSellingModel = new DefaultTableModel(bestSellingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBestSelling = new JTable(bestSellingModel) {
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setBackground(new Color(41, 128, 185));
                        setForeground(Color.WHITE);
                        setFont(UIConstants.FONT_SUBHEADER);
                    }
                };
            }
        };
        tableBestSelling.setFont(UIConstants.FONT_NORMAL);
        tableBestSelling.setRowHeight(40);
        tableBestSelling.setBackground(Color.WHITE);
        tableBestSelling.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableBestSelling.setSelectionForeground(UIConstants.TEXT_WHITE);
        tableBestSelling.setShowGrid(true);
        tableBestSelling.setGridColor(new Color(230, 235, 240));
        tableBestSelling.setIntercellSpacing(new Dimension(2, 2));

        // Center align all cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableBestSelling.getColumnCount(); i++) {
            tableBestSelling.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Force header styling with custom renderer
        javax.swing.table.JTableHeader bestSellingHeader = tableBestSelling.getTableHeader();
        bestSellingHeader.setPreferredSize(new Dimension(0, 45));
        bestSellingHeader.setReorderingAllowed(false);
        bestSellingHeader.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Column widths
        tableBestSelling.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableBestSelling.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableBestSelling.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableBestSelling.getColumnModel().getColumn(3).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(tableBestSelling);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRevenueChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title with refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIConstants.BG_SECONDARY);

        JLabel lblTitle = new JLabel("Biểu đồ doanh thu 7 ngày gần nhất");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);

        JButton btnRefresh = UIConstants.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadStatistics());

        titlePanel.add(lblTitle, BorderLayout.WEST);
        titlePanel.add(btnRefresh, BorderLayout.EAST);

        panel.add(titlePanel, BorderLayout.NORTH);

        // Chart canvas
        JPanel chartCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRevenueChart(g);
            }
        };
        chartCanvas.setBackground(Color.WHITE);
        chartCanvas.setPreferredSize(new Dimension(0, 280));

        panel.add(chartCanvas, BorderLayout.CENTER);

        return panel;
    }

    private void drawRevenueChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = g2d.getClipBounds().width;
        int height = g2d.getClipBounds().height;

        // Get last 7 days revenue data
        java.util.Map<String, Double> revenueData = getLast7DaysRevenue();
        if (revenueData.isEmpty()) {
            g2d.setFont(UIConstants.FONT_NORMAL);
            g2d.setColor(Color.GRAY);
            String noData = "Không có dữ liệu";
            int textWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - textWidth) / 2, height / 2);
            return;
        }

        // Chart dimensions
        int leftMargin = 80;
        int rightMargin = 40;
        int topMargin = 40;
        int bottomMargin = 60;
        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        // Find max value for scaling
        double maxRevenue = revenueData.values().stream().max(Double::compare).orElse(1.0);
        double scale = maxRevenue > 0 ? chartHeight / maxRevenue : 1;

        // Draw grid lines and Y-axis labels
        g2d.setColor(new Color(230, 230, 230));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            int y = topMargin + (chartHeight * i / 5);
            g2d.drawLine(leftMargin, y, leftMargin + chartWidth, y);

            // Y-axis label
            double value = maxRevenue * (5 - i) / 5;
            String label = formatRevenue(value);
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.drawString(label, 10, y + 5);
            g2d.setColor(new Color(230, 230, 230));
        }

        // Draw bars
        java.util.List<String> dates = new java.util.ArrayList<>(revenueData.keySet());
        int barCount = dates.size();
        int barSpacing = 20;
        int barWidth = (chartWidth - (barCount + 1) * barSpacing) / barCount;

        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            double revenue = revenueData.get(date);

            int barHeight = (int) (revenue * scale);
            int x = leftMargin + barSpacing + i * (barWidth + barSpacing);
            int y = topMargin + chartHeight - barHeight;

            // Draw bar with gradient
            Color barColor = new Color(26, 188, 156);
            GradientPaint gradient = new GradientPaint(
                x, y, barColor.brighter(),
                x, y + barHeight, barColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Draw value on top of bar
            g2d.setColor(UIConstants.PRIMARY_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valueText = formatRevenue(revenue);
            int textWidth = g2d.getFontMetrics().stringWidth(valueText);
            g2d.drawString(valueText, x + (barWidth - textWidth) / 2, y - 5);

            // Draw date label
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String dateLabel = date.substring(5); // MM-DD
            int dateLabelWidth = g2d.getFontMetrics().stringWidth(dateLabel);
            g2d.drawString(dateLabel, x + (barWidth - dateLabelWidth) / 2, topMargin + chartHeight + 20);
        }
    }

    private java.util.Map<String, Double> getLast7DaysRevenue() {
        java.util.Map<String, Double> data = new java.util.LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            double revenue = donDatDAO.getTotalRevenueByDate(dateStr);
            data.put(dateStr, revenue);
        }

        return data;
    }

    private String formatRevenue(double revenue) {
        if (revenue >= 1_000_000) {
            return String.format("%.1fM", revenue / 1_000_000);
        } else if (revenue >= 1_000) {
            return String.format("%.1fK", revenue / 1_000);
        } else {
            return String.format("%.0f", revenue);
        }
    }

    private void loadStatistics() {
        String fromDate = txtFromDate.getText().trim();
        String toDate = txtToDate.getText().trim();

        // Load revenue and order count for date range
        double totalRevenue = 0;
        int totalOrders = 0;

        // Get all orders in date range
        orderModel.setRowCount(0);
        List<DonDatDTO> orders = donDatDAO.getDonDatByDateRange(fromDate, toDate);

        // Map to store staff revenue: staffId -> {orderCount, totalRevenue}
        Map<Integer, double[]> staffRevenueMap = new HashMap<>();

        for (DonDatDTO order : orders) {
            if ("completed".equals(order.getTinhTrang())) {
                totalRevenue += order.getTongTien();
                totalOrders++;

                // Track revenue by staff
                int staffId = order.getMaNV();
                if (!staffRevenueMap.containsKey(staffId)) {
                    staffRevenueMap.put(staffId, new double[]{0, 0}); // {orderCount, revenue}
                }
                double[] staffData = staffRevenueMap.get(staffId);
                staffData[0]++; // Increment order count
                staffData[1] += order.getTongTien(); // Add revenue
            }

            orderModel.addRow(new Object[]{
                order.getMaDonDat(),
                order.getMaBan(),
                order.getNgayDat(),
                String.format("%,.0f VNĐ", order.getTongTien()),
                order.getTinhTrang()
            });
        }

        // Calculate average order value
        double avgOrder = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Update stat cards
        lblRevenue.setText(String.format("%,.0f VNĐ", totalRevenue));
        lblOrderCount.setText(String.valueOf(totalOrders));
        lblAvgOrder.setText(String.format("%,.0f VNĐ", avgOrder));

        // Load staff revenue table with ranking
        staffRevenueModel.setRowCount(0);

        // Get current month in YYYY-MM format
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        List<StaffRankingDTO> rankings = staffRankingDAO.getStaffRankingByMonth(currentMonth);

        for (StaffRankingDTO ranking : rankings) {
            // Only show staff who have orders in the selected date range
            if (staffRevenueMap.containsKey(ranking.getMaNV())) {
                double[] data = staffRevenueMap.get(ranking.getMaNV());
                int orderCount = (int) data[0];
                double revenue = data[1];
                double avgPerOrder = orderCount > 0 ? revenue / orderCount : 0;

                staffRevenueModel.addRow(new Object[]{
                    ranking.getXepHang(),
                    ranking.getHoTenNV(),
                    orderCount,
                    String.format("%,.0f VNĐ", revenue),
                    String.format("%,.0f VNĐ", avgPerOrder),
                    ranking.getRankIcon() + " " + ranking.getRank()
                });
            }
        }

        // Load best selling dishes for date range
        bestSellingModel.setRowCount(0);
        List<ChiTietDonDatDTO> bestSelling = chiTietDonDatDAO.getBestSellingDishesByDateRange(fromDate, toDate, 10);
        for (ChiTietDonDatDTO item : bestSelling) {
            bestSellingModel.addRow(new Object[]{
                item.getMaMon(),
                item.getTenMon(),
                item.getSoLuong(),
                String.format("%,.0f VNĐ", item.getThanhTien())
            });
        }
    }
}
