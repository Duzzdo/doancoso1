package com.restaurant.view;

import com.restaurant.dao.StaffRankingDAO;
import com.restaurant.model.StaffRankingDTO;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel bảng xếp hạng nhân viên theo doanh thu
 * Giao diện: Filter tháng + Table xếp hạng (Hạng, Tên, Số đơn, Doanh thu, TB/đơn, Xếp hạng)
 */
public class LeaderboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private StaffRankingDAO staffRankingDAO;
    private JTable tableLeaderboard;
    private DefaultTableModel leaderboardModel;
    private JComboBox<String> cboMonth;
    private JLabel lblCurrentMonth;

    /**
     * Constructor: Khởi tạo panel bảng xếp hạng
     */
    public LeaderboardPanel() {
        staffRankingDAO = new StaffRankingDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title khác nhau cho Admin/Manager và Staff
        String titleText = SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isManager() ?
            "BẢNG XẾP HẠNG NHÂN VIÊN" : "BẢNG XẾP HẠNG";
        JLabel title = new JLabel(titleText);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));     // BorderLayout: Filter (NORTH) + Table (CENTER)
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel filterPanel = createFilterPanel();                        // Panel chọn tháng
        contentPanel.add(filterPanel, BorderLayout.NORTH);

        JPanel leaderboardPanel = createLeaderboardPanel();              // Panel table xếp hạng
        contentPanel.add(leaderboardPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadLeaderboard();                                               // Load dữ liệu xếp hạng
    }

    /**
     * Tạo panel filter chọn tháng
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // FlowLayout ngang
        panel.setBackground(UIConstants.BG_PRIMARY);

        JLabel lblFilter = new JLabel("Chọn tháng:");
        lblFilter.setFont(UIConstants.FONT_NORMAL);
        panel.add(lblFilter);

        // Tạo danh sách 6 tháng gần nhất
        String[] months = new String[6];
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 6; i++) {                                    // Loop 6 tháng
            LocalDate date = now.minusMonths(i);                         // Lùi i tháng
            months[i] = date.toString().substring(0, 7);                 // Format: YYYY-MM
        }

        cboMonth = new JComboBox<>(months);
        cboMonth.setFont(UIConstants.FONT_NORMAL);
        cboMonth.setPreferredSize(new Dimension(120, 35));
        cboMonth.addActionListener(e -> loadLeaderboard());              // Khi chọn tháng → load lại
        panel.add(cboMonth);

        JButton btnRefresh = UIConstants.createPrimaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadLeaderboard());            // Refresh dữ liệu
        panel.add(btnRefresh);

        lblCurrentMonth = new JLabel();
        lblCurrentMonth.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCurrentMonth.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(lblCurrentMonth);

        return panel;
    }

    /**
     * Tạo panel table xếp hạng
     */
    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));            // BorderLayout: Title (NORTH) + Table (CENTER)
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel("BẢNG XẾP HẠNG NHÂN VIÊN");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Hạng", "Tên nhân viên", "Số đơn", "Doanh thu", "TB/đơn", "Xếp hạng"};
        leaderboardModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        tableLeaderboard = new JTable(leaderboardModel) {
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
        tableLeaderboard.setFont(UIConstants.FONT_NORMAL);
        tableLeaderboard.setRowHeight(50);
        tableLeaderboard.setBackground(Color.WHITE);
        tableLeaderboard.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        tableLeaderboard.setSelectionForeground(UIConstants.TEXT_WHITE);
        tableLeaderboard.setShowGrid(true);
        tableLeaderboard.setGridColor(new Color(230, 235, 240));
        tableLeaderboard.setIntercellSpacing(new Dimension(2, 2));

        // Center align all cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableLeaderboard.getColumnCount(); i++) {
            tableLeaderboard.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom renderer for top 3 rows
        tableLeaderboard.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row == 0) {
                        c.setBackground(new Color(255, 215, 0, 50)); // Gold
                        c.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else if (row == 1) {
                        c.setBackground(new Color(192, 192, 192, 50)); // Silver
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if (row == 2) {
                        c.setBackground(new Color(205, 127, 50, 50)); // Bronze
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setFont(UIConstants.FONT_NORMAL);
                    }
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        // Header styling
        javax.swing.table.JTableHeader header = tableLeaderboard.getTableHeader();
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

        // Column widths
        tableLeaderboard.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableLeaderboard.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableLeaderboard.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableLeaderboard.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableLeaderboard.getColumnModel().getColumn(4).setPreferredWidth(150);
        tableLeaderboard.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tableLeaderboard);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadLeaderboard() {
        String selectedMonth = (String) cboMonth.getSelectedItem();
        if (selectedMonth == null) {
            selectedMonth = LocalDate.now().toString().substring(0, 7);
        }

        // Update month label
        lblCurrentMonth.setText("Tháng " + selectedMonth.substring(5) + "/" + selectedMonth.substring(0, 4));

        // Load rankings - get ALL staff (limit = 0 means no limit)
        leaderboardModel.setRowCount(0);
        List<StaffRankingDTO> rankings = staffRankingDAO.getTopStaff(selectedMonth, 0);

        for (StaffRankingDTO ranking : rankings) {
            String position = "#" + ranking.getXepHang();

            // Add text for top 3
            if (ranking.getXepHang() == 1) {
                position = "#1 (Vàng)";
            } else if (ranking.getXepHang() == 2) {
                position = "#2 (Bạc)";
            } else if (ranking.getXepHang() == 3) {
                position = "#3 (Đồng)";
            }

            leaderboardModel.addRow(new Object[]{
                position,
                ranking.getHoTenNV(),
                ranking.getSoDon(),
                String.format("%,.0f VNĐ", ranking.getDoanhThu()),
                String.format("%,.0f VNĐ", ranking.getTrungBinhDon()),
                ranking.getRank()
            });
        }
    }
}
