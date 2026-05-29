package com.restaurant.view;

import com.restaurant.dao.LoaiMonDAO;
import com.restaurant.dao.MonDAO;
import com.restaurant.dao.ChiTietDonDatDAO;
import com.restaurant.dao.DonDatDAO;
import com.restaurant.model.LoaiMonDTO;
import com.restaurant.model.MonDTO;
import com.restaurant.model.ChiTietDonDatDTO;
import com.restaurant.model.DonDatDTO;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog chọn món cho đơn hàng
 * Giao diện: Danh sách loại món (trái) | Danh sách món (giữa) | Giỏ hàng (phải)
 */
public class OrderDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private LoaiMonDAO loaiMonDAO;
    private MonDAO monDAO;
    private ChiTietDonDatDAO chiTietDonDatDAO;

    private JList<LoaiMonDTO> categoryList;
    private JTable dishTable;
    private DefaultTableModel dishModel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotal;
    private JPanel cartPanel;
    private JLabel cartTitleLabel;

    private int maDonDat;
    private double totalAmount = 0;
    private List<CartItem> cartItems;

    /**
     * Constructor: Khởi tạo dialog chọn món cho đơn hàng
     */
    public OrderDialog(Frame parent, int maDonDat) {
        super(parent, "Chọn món - Đơn #" + maDonDat, true);              // Modal dialog
        this.maDonDat = maDonDat;
        this.cartItems = new ArrayList<>();

        loaiMonDAO = new LoaiMonDAO();
        monDAO = new MonDAO();
        chiTietDonDatDAO = new ChiTietDonDatDAO();

        initComponents();
        loadExistingOrder();                                             // Load món đã chọn trước đó (nếu có)
        setSize(1000, 600);
        setLocationRelativeTo(parent);                                   // Hiển thị giữa màn hình
        setVisible(true);
    }

    /**
     * Khởi tạo các components của dialog
     * Layout: Categories (WEST) | Dishes (CENTER) | Cart (EAST) | Buttons (SOUTH)
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));                             // BorderLayout với gap 10px
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        getContentPane().setBackground(UIConstants.BG_PRIMARY);

        JPanel leftPanel = createCategoryPanel();                        // Panel danh sách loại món
        leftPanel.setPreferredSize(new Dimension(200, 0));               // Width 200px, height tự động

        JPanel centerPanel = createDishPanel();                          // Panel danh sách món

        JPanel rightPanel = createCartPanel();                           // Panel giỏ hàng
        rightPanel.setPreferredSize(new Dimension(300, 0));              // Width 300px

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);                    // Panel buttons (Lưu, Hủy)

        // Select loại món đầu tiên để hiển thị danh sách món
        if (categoryList.getModel().getSize() > 0) {
            categoryList.setSelectedIndex(0);
        }
    }

    /**
     * Tạo panel danh sách loại món (bên trái)
     */
    private JPanel createCategoryPanel() {
        JPanel panel = UIConstants.createTitledPanel("Loại món");

        DefaultListModel<LoaiMonDTO> listModel = new DefaultListModel<>();
        List<LoaiMonDTO> categories = loaiMonDAO.getAllLoaiMon();       // Lấy tất cả loại món từ database
        for (LoaiMonDTO loai : categories) {
            listModel.addElement(loai);                                  // Thêm vào list model
        }

        categoryList = new JList<>(listModel);
        categoryList.setFont(UIConstants.FONT_NORMAL);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ chọn 1 loại món
        // Listener: Khi chọn loại món → load danh sách món của loại đó
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && dishModel != null) {         // Tránh trigger 2 lần
                loadDishes();                                            // Load món theo loại đã chọn
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryList);          // Wrap list trong scroll pane
        panel.add(scrollPane, BorderLayout.CENTER);                      // Thêm vào center của panel

        return panel;
    }

    /**
     * Tạo panel danh sách món (giữa)
     */
    private JPanel createDishPanel() {
        JPanel panel = UIConstants.createTitledPanel("Danh sách món");

        String[] columns = {"Mã", "Tên món", "Giá", "Trạng thái"};
        dishModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        dishTable = new JTable(dishModel) {
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
        dishTable.setFont(UIConstants.FONT_NORMAL);
        dishTable.setRowHeight(30);
        dishTable.setShowGrid(true);
        dishTable.setGridColor(Color.WHITE);
        dishTable.setIntercellSpacing(new Dimension(2, 2));

        javax.swing.table.JTableHeader dishHeader = dishTable.getTableHeader();
        dishHeader.setFont(UIConstants.FONT_SUBHEADER);
        dishHeader.setBackground(new Color(41, 128, 185));
        dishHeader.setForeground(Color.WHITE);
        dishHeader.setOpaque(true);
        dishHeader.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setFont(UIConstants.FONT_SUBHEADER);
                label.setBackground(new Color(41, 128, 185));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.WHITE),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Double click để thêm món vào giỏ
        dishTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {                          // Nếu double click
                    addDishToCart();                                     // Thêm món vào giỏ
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dishTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Panel chứa button
        btnPanel.setBackground(UIConstants.BG_SECONDARY);
        JButton btnAdd = UIConstants.createPrimaryButton("Thêm vào giỏ hàng");
        btnAdd.setPreferredSize(new Dimension(180, 38));
        btnAdd.addActionListener(e -> addDishToCart());                  // Click button → thêm món , hàm addDishToCart là hàm xử lý thêm món vào giỏ hàng, addActionListener sẽ gọi hàm này khi button được click
        btnPanel.add(btnAdd);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tạo panel giỏ hàng (bên phải)
     */
    private JPanel createCartPanel() {
        cartPanel = new JPanel(new BorderLayout());                      // BorderLayout: Title (NORTH) + Table (CENTER) + Buttons (SOUTH)
        cartPanel.setBackground(UIConstants.BG_SECONDARY);      //UIConstants là class chứa các hằng số về màu sắc, font chữ, kích thước... để dễ dàng quản lý và thay đổi giao diện
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2), //UIConstants.PRIMARY_COLOR là màu chính của giao diện, thường dùng cho các thành phần nổi bật như header, buttons chính... ,  2 là độ dày của border
            BorderFactory.createEmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, //PADDING_MEDIUM là khoảng cách giữa border và nội dung bên trong panel
                UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM)
        ));

        cartTitleLabel = new JLabel("Giỏ hàng (0 món)");
        cartTitleLabel.setFont(UIConstants.FONT_HEADER);
        cartTitleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        cartTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, UIConstants.PADDING_MEDIUM, 0));
        cartPanel.add(cartTitleLabel, BorderLayout.NORTH);

        String[] columns = {"Món", "SL", "Giá"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        cartTable = new JTable(cartModel) {
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setBackground(new Color(46, 204, 113));
                        setForeground(Color.WHITE);
                        setFont(UIConstants.FONT_SUBHEADER);
                    }
                };
            }
        };
        cartTable.setFont(UIConstants.FONT_NORMAL);
        cartTable.setRowHeight(25);
        cartTable.setShowGrid(true);
        cartTable.setGridColor(Color.WHITE);
        cartTable.setIntercellSpacing(new Dimension(2, 2));

        javax.swing.table.JTableHeader cartHeader = cartTable.getTableHeader();
        cartHeader.setFont(UIConstants.FONT_SUBHEADER);
        cartHeader.setBackground(new Color(46, 204, 113));
        cartHeader.setForeground(Color.WHITE);
        cartHeader.setOpaque(true);
        cartHeader.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setFont(UIConstants.FONT_SUBHEADER);
                label.setBackground(new Color(46, 204, 113));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.WHITE),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        cartPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 5));       // Grid 4 hàng 1 cột cho 4 buttons
        btnPanel.setBackground(UIConstants.BG_SECONDARY);

        JButton btnIncrease = UIConstants.createSuccessButton("+ Tăng");
        btnIncrease.addActionListener(e -> changeQuantity(1));          // Tăng số lượng món đã chọn , hàm changeQuantity là hàm xử lý tăng giảm số lượng món trong giỏ hàng

        JButton btnDecrease = UIConstants.createWarningButton("- Giảm");
        btnDecrease.addActionListener(e -> changeQuantity(-1));         // Giảm số lượng món đã chọn

        JButton btnNote = UIConstants.createSecondaryButton("Ghi chú");
        btnNote.addActionListener(e -> addNote());                      // Thêm ghi chú cho món

        JButton btnRemove = UIConstants.createDangerButton("Xóa");
        btnRemove.addActionListener(e -> removeFromCart());             // Xóa món khỏi giỏ

        btnPanel.add(btnIncrease);
        btnPanel.add(btnDecrease);
        btnPanel.add(btnNote);
        btnPanel.add(btnRemove);

        cartPanel.add(btnPanel, BorderLayout.SOUTH);

        return cartPanel;
    }

    /**
     * Tạo panel bottom chứa tổng tiền và buttons Lưu/Hủy
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));             // BorderLayout: Total (WEST) + Buttons (EAST)
        panel.setBackground(UIConstants.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        lblTotal = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(UIConstants.DANGER_COLOR);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // FlowLayout ngang cho 2 buttons
        btnPanel.setBackground(UIConstants.BG_PRIMARY);

        JButton btnSave = UIConstants.createSuccessButton("Lưu đơn");
        btnSave.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
        btnSave.addActionListener(e -> saveOrder());                    // Lưu đơn hàng vào database, hàm saveOrder là hàm xử lý lưu đơn hàng, addActionListener sẽ gọi hàm này khi button được click

        JButton btnCancel = UIConstants.createDangerButton("Hủy");
        btnCancel.setPreferredSize(UIConstants.LARGE_BUTTON_SIZE);
        btnCancel.addActionListener(e -> dispose());                    // Đóng dialog không lưu

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        panel.add(lblTotal, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Load danh sách món theo loại món đã chọn
     */
    private void loadDishes() {
        dishModel.setRowCount(0);                                        // Xóa tất cả rows cũ
        LoaiMonDTO selectedCategory = categoryList.getSelectedValue();   // Lấy loại món đang chọn

        if (selectedCategory != null) {
            List<MonDTO> dishes = monDAO.getMonByLoai(selectedCategory.getMaLoai()); // Lấy món theo loại
            for (MonDTO mon : dishes) {                                  // Duyệt qua từng món
                dishModel.addRow(new Object[]{                           // Thêm row vào table
                    mon.getMaMon(),
                    mon.getTenMon(),
                    String.format("%,.0f VNĐ", mon.getGiaTien()),       // Format giá: 50,000 VNĐ
                    mon.isAvailable() ? "Còn" : "Hết"                    // Hiển thị trạng thái
                });
            }
        }
    }

    /**
     * Load món đã chọn trước đó (nếu đơn hàng đã có món)
     */
    private void loadExistingOrder() {
        List<ChiTietDonDatDTO> existingItems = chiTietDonDatDAO.getChiTietByDonDat(maDonDat); // Lấy chi tiết đơn từ database
        for (ChiTietDonDatDTO item : existingItems) {                    // Duyệt qua từng món đã chọn
            CartItem cartItem = new CartItem();
            cartItem.maMon = item.getMaMon();
            cartItem.tenMon = item.getTenMon();
            cartItem.giaTien = item.getGiaTien();
            cartItem.soLuong = item.getSoLuong();
            cartItem.ghiChu = item.getGhiChu() != null ? item.getGhiChu() : ""; // Ghi chú có thể null, nên set mặc định là empty string
            cartItems.add(cartItem);
        }
        updateCartDisplay();
    }

    private void addDishToCart() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maMon = (int) dishModel.getValueAt(selectedRow, 0);
        String tinhTrang = (String) dishModel.getValueAt(selectedRow, 3);

        // Check if dish is out of stock
        if ("Hết".equals(tinhTrang)) {
            JOptionPane.showMessageDialog(this, "Món này đã hết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MonDTO mon = monDAO.getMonById(maMon);

        if (mon == null) return;

        // Check if already in cart
        CartItem existingItem = findCartItem(maMon);
        if (existingItem != null) {     // Nếu đã có trong giỏ, tăng số lượng
            existingItem.soLuong++;
        } else {
            CartItem newItem = new CartItem();  // Nếu chưa có, tạo mới và thêm vào giỏ
            newItem.maMon = mon.getMaMon();     // Set mã món
            newItem.tenMon = mon.getTenMon();   // Set tên món
            newItem.giaTien = mon.getGiaTien(); // Set giá tiền
            newItem.soLuong = 1;    // Mặc định thêm 1 món vào giỏ
            newItem.ghiChu = "";    // Ghi chú mặc định là rỗng
            cartItems.add(newItem); // Thêm món vào danh sách giỏ hàng
        }

        updateCartDisplay();
    }

    private void changeQuantity(int delta) {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món trong giỏ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem item = cartItems.get(selectedRow);
        item.soLuong += delta;

        if (item.soLuong <= 0) {
            cartItems.remove(selectedRow);
        }

        updateCartDisplay();
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa món này khỏi giỏ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.remove(selectedRow);
            updateCartDisplay();
        }
    }

    private void addNote() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần ghi chú!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem item = cartItems.get(selectedRow);
        String currentNote = item.ghiChu != null ? item.ghiChu : "";

        // Create custom dialog
        JDialog noteDialog = new JDialog(this, "Ghi chú món ăn", true);
        noteDialog.setLayout(new BorderLayout(15, 15));
        noteDialog.setSize(450, 250);
        noteDialog.setLocationRelativeTo(this);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIConstants.BG_SECONDARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel lblTitle = new JLabel("Nhập ghi chú cho món: " + item.tenMon);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_COLOR);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Text area
        JTextArea txtNote = new JTextArea(currentNote);
        txtNote.setFont(UIConstants.FONT_NORMAL);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane scrollPane = new JScrollPane(txtNote);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(UIConstants.BG_SECONDARY);

        JButton btnOK = UIConstants.createSuccessButton("Lưu");
        btnOK.setPreferredSize(new Dimension(100, 35));
        btnOK.addActionListener(e -> {
            item.ghiChu = txtNote.getText().trim();
            updateCartDisplay();
            noteDialog.dispose();
        });

        JButton btnCancel = UIConstants.createSecondaryButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> noteDialog.dispose());

        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        noteDialog.add(mainPanel);
        noteDialog.setVisible(true);
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        totalAmount = 0;

        for (CartItem item : cartItems) {
            double itemTotal = item.giaTien * item.soLuong;
            totalAmount += itemTotal;
            cartModel.addRow(new Object[]{
                item.tenMon,
                item.soLuong,
                String.format("%,.0f", itemTotal)
            });
        }

        lblTotal.setText("Tổng tiền: " + String.format("%,.0f VNĐ", totalAmount));

        // Update cart panel title with item count
        updateCartTitle();
    }

    private void updateCartTitle() {
        int itemCount = cartItems.size();
        cartTitleLabel.setText("Giỏ hàng (" + itemCount + " món)");
    }

    private void saveOrder() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Delete old items
        chiTietDonDatDAO.deleteAllByDonDat(maDonDat);

        // Insert new items
        for (CartItem item : cartItems) {
            ChiTietDonDatDTO chiTiet = new ChiTietDonDatDTO();
            chiTiet.setMaDonDat(maDonDat);
            chiTiet.setMaMon(item.maMon);
            chiTiet.setSoLuong(item.soLuong);
            chiTiet.setGhiChu(item.ghiChu);
            chiTietDonDatDAO.insertChiTietDonDat(chiTiet);
        }

        // Update order total
        DonDatDAO donDatDAO = new DonDatDAO();
        DonDatDTO donDat = donDatDAO.getDonDatById(maDonDat);
        if (donDat != null) {
            donDat.setTongTien(totalAmount);
            donDatDAO.updateDonDat(donDat);
        }

        JOptionPane.showMessageDialog(this, "Lưu đơn hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private CartItem findCartItem(int maMon) {
        for (CartItem item : cartItems) {
            if (item.maMon == maMon) {
                return item;
            }
        }
        return null;
    }

    // Inner class for cart items
    private static class CartItem {
        int maMon;
        String tenMon;
        double giaTien;
        int soLuong;
        String ghiChu;
    }
}
