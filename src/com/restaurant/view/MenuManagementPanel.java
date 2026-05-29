package com.restaurant.view;

import com.restaurant.dao.LoaiMonDAO;
import com.restaurant.dao.MonDAO;
import com.restaurant.model.LoaiMonDTO;
import com.restaurant.model.MonDTO;
import com.restaurant.utils.ValidationUtil;
import com.restaurant.utils.UIConstants;
import com.restaurant.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.io.File;

/**
 * Panel quản lý thực đơn - Hiển thị grid các loại món với card layout hiện đại
 *
 * Layout: Grid 2 cột, mỗi loại món là 1 card với:
 * - Hình ảnh landscape (600x280)
 * - Tên loại món + số lượng món
 * - Action buttons: Xem món, Sửa, Xóa
 *
 * Features:
 * - Upload/preview hình ảnh từ file
 * - CRUD loại món và món ăn
 * - Validation giá tiền, tên món
 */
public class MenuManagementPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MonDAO monDAO;
    private LoaiMonDAO loaiMonDAO;
    private JPanel cardsContainer;
    private JScrollPane scrollPane;

    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_ORANGE = new Color(243, 156, 18);
    private static final Color DANGER_RED = new Color(231, 76, 60);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);

    /**
     * Constructor: Khởi tạo panel quản lý thực đơn
     */
    public MenuManagementPanel() {
        monDAO = new MonDAO();                                           // Khởi tạo DAO để thao tác với bảng Mon
        loaiMonDAO = new LoaiMonDAO();                                   // Khởi tạo DAO để thao tác với bảng LoaiMon

        setLayout(new BorderLayout(0, 20));                              // Layout chính: Title (NORTH) + Cards (CENTER)
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));     // Padding 25px cho toàn panel

        add(createTitlePanel(), BorderLayout.NORTH);                     // Thêm title panel với button "Thêm loại món"

        cardsContainer = new JPanel();
        cardsContainer.setLayout(new GridLayout(0, 2, 30, 30));         // Grid 2 cột, gap 30px giữa các card
        cardsContainer.setBackground(BG_COLOR);
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(cardsContainer);                    // Wrap grid trong scroll pane
        scrollPane.setBorder(null);                                      // Xóa border mặc định của scroll pane
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);         // Tăng tốc độ scroll
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);

        add(scrollPane, BorderLayout.CENTER);                            // Thêm scroll pane vào center

        loadMenuCards();                                                 // Load danh sách loại món từ database
    }

    /**
     * Tạo title panel với label "Quản lý thực đơn" và button "Thêm loại món"
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());                   // Layout: Title (WEST) + Button (EAST)
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Quản lý thực đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);

        JButton btnAddMenu = new JButton("+ Thêm loại món");
        btnAddMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddMenu.setForeground(Color.WHITE);
        btnAddMenu.setBackground(SUCCESS_GREEN);
        btnAddMenu.setFocusPainted(false);                               // Tắt focus border khi click
        btnAddMenu.setBorderPainted(false);                              // Tắt border mặc định
        btnAddMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));           // Đổi cursor thành tay khi hover
        btnAddMenu.setPreferredSize(new Dimension(160, 42));
        btnAddMenu.addActionListener(e -> handleAddCategory());         // Mở dialog thêm loại món

        btnAddMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAddMenu.setBackground(new Color(39, 174, 96));       // Tối màu khi hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAddMenu.setBackground(SUCCESS_GREEN);                 // Trả về màu gốc khi rời chuột
            }
        });

        panel.add(title, BorderLayout.WEST);
        panel.add(btnAddMenu, BorderLayout.EAST);

        return panel;
    }

    /**
     * Load tất cả loại món từ database và hiển thị trong grid
     */
    private void loadMenuCards() {
        cardsContainer.removeAll();                                      // Xóa tất cả card cũ trước khi load mới
        List<LoaiMonDTO> categories = loaiMonDAO.getAllLoaiMon();       // Lấy danh sách loại món từ database

        for (LoaiMonDTO category : categories) {                         // Duyệt qua từng loại món
            cardsContainer.add(createMenuCard(category));                // Tạo card và thêm vào grid
        }

        cardsContainer.revalidate();                                     // Refresh layout để hiển thị các card mới
        cardsContainer.repaint();                                        // Vẽ lại panel
    }

    /**
     * Tạo card cho mỗi loại món
     * Layout: Image panel (top) + Info panel (bottom)
     */
    private JPanel createMenuCard(LoaiMonDTO category) {
        JPanel card = new JPanel(new BorderLayout(0, 0));               // Layout dọc: Image trên, Info dưới
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),            // Border ngoài màu xám
            BorderFactory.createEmptyBorder(0, 0, 0, 0)                 // Padding trong = 0
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(250, 250, 250));
        imagePanel.setPreferredSize(new Dimension(0, 280));             // Chiều cao cố định 280px, width tự động
        imagePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR)); // Border dưới

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);

        // Load hình ảnh từ BLOB hoặc dùng ảnh mặc định
        if (category.getHinhAnh() != null) {                             // Nếu có ảnh trong database
            try {
                BufferedImage img = ImageUtil.byteArrayToImage(category.getHinhAnh()); // Convert byte[] sang BufferedImage
                BufferedImage resized = ImageUtil.resizeImage(img, 600, 280);          // Resize về 600x280
                lblImage.setIcon(new ImageIcon(resized));                               // Set icon cho label
            } catch (Exception e) {                                      // Nếu lỗi khi load ảnh
                BufferedImage defaultImg = ImageUtil.getDefaultCategoryImage();         // Lấy ảnh mặc định
                BufferedImage resized = ImageUtil.resizeImage(defaultImg, 600, 280);
                lblImage.setIcon(new ImageIcon(resized));
            }
        } else {                                                         // Nếu không có ảnh trong database
            BufferedImage defaultImg = ImageUtil.getDefaultCategoryImage();
            BufferedImage resized = ImageUtil.resizeImage(defaultImg, 600, 280);
            lblImage.setIcon(new ImageIcon(resized));
        }

        imagePanel.add(lblImage, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Layout dọc: Name, Count, Buttons
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblName = new JLabel(category.getTenLoai());             // Tên loại món
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);               // Căn giữa

        List<MonDTO> dishes = monDAO.getMonByLoai(category.getMaLoai()); // Đếm số món trong loại này
        JLabel lblCount = new JLabel(dishes.size() + " món");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblCount.setForeground(TEXT_LIGHT);
        lblCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(lblName);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));        // Khoảng cách 8px
        infoPanel.add(lblCount);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 18)));       // Khoảng cách 18px

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Layout ngang cho 3 buttons
        btnPanel.setBackground(CARD_BG);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnView = createActionButton("Xem món", PRIMARY_COLOR);
        btnView.setPreferredSize(new Dimension(100, 38));
        btnView.addActionListener(e -> showDishesDialog(category));     // Mở dialog danh sách món

        JButton btnEdit = createActionButton("Sửa", WARNING_ORANGE);
        btnEdit.setPreferredSize(new Dimension(80, 38));
        btnEdit.addActionListener(e -> handleEditCategory(category));   // Mở dialog sửa loại món

        JButton btnDelete = createActionButton("Xóa", DANGER_RED);
        btnDelete.setPreferredSize(new Dimension(80, 38));
        btnDelete.addActionListener(e -> handleDeleteCategory(category)); // Xóa loại món

        btnPanel.add(btnView);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        infoPanel.add(btnPanel);

        card.add(imagePanel, BorderLayout.NORTH);                        // Image ở trên
        card.add(infoPanel, BorderLayout.CENTER);                        // Info ở dưới

        // Hover effect: Đổi border màu xanh khi hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),    // Border xanh dày 2px
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),     // Trả về border xám 1px
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
        });

        return card;
    }

    /**
     * Tạo action button với màu tùy chỉnh và hover effect
     */
    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(65, 32));

        Color hoverColor = bgColor.darker();                             // Tạo màu tối hơn cho hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);                           // Đổi màu khi hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);                              // Trả về màu gốc
            }
        });

        return btn;
    }

    /**
     * Hiển thị dialog danh sách món ăn của loại món
     * Layout: Title + Button "Thêm món" (top) + Grid 3 cột các món (center)
     */
    private void showDishesDialog(LoaiMonDTO category) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), // Lấy parent frame
            category.getTenLoai(), true);                                // Modal dialog với title = tên loại món
        dialog.setSize(1100, 700);
        dialog.setLocationRelativeTo(this);                              // Hiển thị giữa màn hình

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Danh sách món - " + category.getTenLoai());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JButton btnAddDish = new JButton("+ Thêm món");
        btnAddDish.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddDish.setForeground(Color.WHITE);
        btnAddDish.setBackground(SUCCESS_GREEN);
        btnAddDish.setFocusPainted(false);
        btnAddDish.setBorderPainted(false);
        btnAddDish.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddDish.setPreferredSize(new Dimension(140, 40));
        btnAddDish.addActionListener(e -> {
            handleAddDish(category);                                     // Mở dialog thêm món
            dialog.dispose();                                            // Đóng dialog hiện tại
            showDishesDialog(category);                                  // Mở lại dialog để refresh danh sách
        });

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnAddDish, BorderLayout.EAST);

        JPanel dishesGrid = new JPanel(new GridLayout(0, 3, 20, 20));  // Grid 3 cột, gap 20px
        dishesGrid.setBackground(BG_COLOR);

        List<MonDTO> dishes = monDAO.getMonByLoai(category.getMaLoai()); // Lấy danh sách món theo loại
        for (MonDTO dish : dishes) {                                     // Duyệt qua từng món
            dishesGrid.add(createDishCard(dish, category, dialog));     // Tạo card và thêm vào grid
        }

        JScrollPane scrollPane = new JScrollPane(dishesGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(mainPanel);
        dialog.setVisible(true);                                         // Hiển thị dialog
    }

    /**
     * Tạo card cho mỗi món ăn
     * Layout: Image panel (top) + Info panel (bottom) với tên, giá, trạng thái, action buttons
     */
    private JPanel createDishCard(MonDTO dish, LoaiMonDTO category, JDialog parentDialog) {
        JPanel card = new JPanel(new BorderLayout(0, 0));               // Layout dọc: Image trên, Info dưới
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(250, 250, 250));
        imagePanel.setPreferredSize(new Dimension(240, 180));           // Kích thước ảnh món: 240x180
        imagePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR)); // Border dưới

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);

        // Load hình ảnh từ BLOB hoặc dùng ảnh mặc định
        if (dish.getHinhAnh() != null) {                                 // Nếu có ảnh trong database
            try {
                BufferedImage img = ImageUtil.byteArrayToImage(dish.getHinhAnh()); // Convert byte[] sang BufferedImage
                BufferedImage resized = ImageUtil.resizeImage(img, 240, 180);      // Resize về 240x180
                lblImage.setIcon(new ImageIcon(resized));
            } catch (Exception e) {                                      // Nếu lỗi khi load ảnh
                BufferedImage defaultImg = ImageUtil.getDefaultFoodImage();        // Lấy ảnh món ăn mặc định
                BufferedImage resized = ImageUtil.resizeImage(defaultImg, 240, 180);
                lblImage.setIcon(new ImageIcon(resized));
            }
        } else {                                                         // Nếu không có ảnh trong database
            BufferedImage defaultImg = ImageUtil.getDefaultFoodImage();
            BufferedImage resized = ImageUtil.resizeImage(defaultImg, 240, 180);
            lblImage.setIcon(new ImageIcon(resized));
        }

        imagePanel.add(lblImage, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Layout dọc
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblName = new JLabel(dish.getTenMon());                  // Tên món
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel(String.format("%,.0f đ", dish.getGiaTien())); // Giá tiền format: 50,000 đ
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPrice.setForeground(PRIMARY_COLOR);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean isAvailable = "true".equals(dish.getTinhTrang());       // Kiểm tra trạng thái món (string "true"/"false")
        JLabel lblStatus = new JLabel(isAvailable ? "Còn món" : "Hết món");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(isAvailable ? SUCCESS_GREEN : DANGER_RED); // Xanh nếu còn, đỏ nếu hết
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(lblName);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));        // Khoảng cách 5px
        infoPanel.add(lblPrice);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblStatus);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)); // Layout ngang cho 2 buttons
        btnPanel.setBackground(CARD_BG);

        JButton btnEdit = createActionButton("Sửa", WARNING_ORANGE);
        btnEdit.addActionListener(e -> {
            handleEditDish(dish);                                        // Mở dialog sửa món
            parentDialog.dispose();                                      // Đóng dialog danh sách món
            showDishesDialog(category);                                  // Mở lại để refresh
        });

        JButton btnDelete = createActionButton("Xóa", DANGER_RED);
        btnDelete.addActionListener(e -> {
            handleDeleteDish(dish);                                      // Xóa món
            parentDialog.dispose();
            showDishesDialog(category);
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        infoPanel.add(btnPanel);

        card.add(imagePanel, BorderLayout.NORTH);                        // Image ở trên
        card.add(infoPanel, BorderLayout.CENTER);                        // Info ở dưới

        return card;
    }

    /**
     * Xử lý thêm loại món mới
     * Flow: Nhập tên → Chọn hình ảnh → Validate → Insert database → Refresh
     */
    private void handleAddCategory() {
        JPanel panel = new JPanel(new GridBagLayout());                  // GridBagLayout để sắp xếp các components theo grid
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();               // Constraints để định vị từng component
        gbc.fill = GridBagConstraints.HORIZONTAL;                        // Component sẽ fill theo chiều ngang
        gbc.insets = new Insets(10, 10, 10, 10);                        // Padding 10px cho mỗi component

        JLabel lblName = new JLabel("Tên menu:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;                                                   // Cột 0
        gbc.gridy = 0;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtName.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 1;
        panel.add(txtName, gbc);

        JLabel lblImage = new JLabel("Hình ảnh:");
        lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        panel.add(lblImage, gbc);

        JLabel lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultCategoryImage(), 140, 140));
        gbc.gridy = 3;
        panel.add(lblPreview, gbc);

        // Custom paint button với màu xanh gradient
        JButton btnChooseImage = new JButton("Chọn ảnh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 123, 255));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Chọn ảnh")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("Chọn ảnh", x, y);
                g2.dispose();
            }
        };
        btnChooseImage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChooseImage.setPreferredSize(new Dimension(130, 42));
        btnChooseImage.setFocusPainted(false);
        btnChooseImage.setContentAreaFilled(false);
        btnChooseImage.setBorderPainted(true);
        btnChooseImage.setBorder(BorderFactory.createLineBorder(new Color(0, 86, 179), 4));
        btnChooseImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final BufferedImage[] selectedImage = {null};                    // Array để lưu ảnh đã chọn (dùng array vì lambda cần final)
        btnChooseImage.addActionListener(e -> {
            BufferedImage img = ImageUtil.chooseImageFile(this);         // Mở file chooser
            if (img != null) {
                selectedImage[0] = img;
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140));
            }
        });
        gbc.gridy = 4;
        panel.add(btnChooseImage, gbc);

        int option = JOptionPane.showConfirmDialog(this, panel, "Thêm menu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String tenLoai = txtName.getText().trim();

            if (!ValidationUtil.isNotEmpty(tenLoai)) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên menu!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LoaiMonDTO loai = new LoaiMonDTO();
            loai.setTenLoai(tenLoai);

            // Nếu có chọn ảnh, resize về kích thước chuẩn và convert sang byte[]
            if (selectedImage[0] != null) {
                try {
                    BufferedImage resized = ImageUtil.resizeImage(selectedImage[0],
                            ImageUtil.CATEGORY_IMAGE_SIZE, ImageUtil.CATEGORY_IMAGE_SIZE);
                    loai.setHinhAnh(ImageUtil.imageToByteArray(resized));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (loaiMonDAO.insertLoaiMon(loai)) {
                JOptionPane.showMessageDialog(this, "Thêm menu thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý sửa loại món
     * Flow: Pre-fill dữ liệu cũ → Nhập tên mới → Chọn ảnh mới (optional) → Update database
     */
    private void handleEditCategory(LoaiMonDTO loai) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblName = new JLabel("Tên menu:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField(loai.getTenLoai());
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtName.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 1;
        panel.add(txtName, gbc);

        JLabel lblImage = new JLabel("Hình ảnh:");
        lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        panel.add(lblImage, gbc);

        JLabel lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);

        // Load ảnh hiện tại từ database
        if (loai.getHinhAnh() != null) {
            try {
                BufferedImage img = ImageUtil.byteArrayToImage(loai.getHinhAnh());
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140));
            } catch (Exception e) {
                lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultCategoryImage(), 140, 140));
            }
        } else {
            lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultCategoryImage(), 140, 140));
        }

        gbc.gridy = 3;
        panel.add(lblPreview, gbc);

        JButton btnChooseImage = new JButton("Chọn ảnh mới") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 123, 255));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Chọn ảnh mới")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("Chọn ảnh mới", x, y);
                g2.dispose();
            }
        };
        btnChooseImage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChooseImage.setPreferredSize(new Dimension(150, 42));
        btnChooseImage.setFocusPainted(false);
        btnChooseImage.setContentAreaFilled(false);
        btnChooseImage.setBorderPainted(true);
        btnChooseImage.setBorder(BorderFactory.createLineBorder(new Color(0, 86, 179), 4));
        btnChooseImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final BufferedImage[] selectedImage = {null};
        btnChooseImage.addActionListener(e -> {
            BufferedImage img = ImageUtil.chooseImageFile(this);
            if (img != null) {
                selectedImage[0] = img;
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140));
            }
        });
        gbc.gridy = 4;
        panel.add(btnChooseImage, gbc);

        int option = JOptionPane.showConfirmDialog(this, panel, "Sửa menu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String tenLoai = txtName.getText().trim();

            if (!ValidationUtil.isNotEmpty(tenLoai)) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên menu!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            loai.setTenLoai(tenLoai);

            // Chỉ update ảnh nếu user chọn ảnh mới
            if (selectedImage[0] != null) {
                try {
                    BufferedImage resized = ImageUtil.resizeImage(selectedImage[0],
                            ImageUtil.CATEGORY_IMAGE_SIZE, ImageUtil.CATEGORY_IMAGE_SIZE);
                    loai.setHinhAnh(ImageUtil.imageToByteArray(resized));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (loaiMonDAO.updateLoaiMon(loai)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý xóa loại món
     * Cảnh báo: Xóa loại món sẽ xóa tất cả món thuộc loại đó (cascade delete)
     */
    private void handleDeleteCategory(LoaiMonDTO loai) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa menu này?\nTất cả món thuộc menu này cũng sẽ bị xóa!",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {                         // Nếu user confirm xóa
            if (loaiMonDAO.deleteLoaiMon(loai.getMaLoai())) {           // Xóa loại món từ database (cascade delete các món)
                JOptionPane.showMessageDialog(this, "Xóa thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards();                                         // Refresh danh sách
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý thêm món mới vào loại món
     * Flow: Nhập tên, giá, trạng thái → Chọn ảnh → Validate → Insert database
     */
    private void handleAddDish(LoaiMonDTO category) {
        JPanel panel = new JPanel(new GridBagLayout());                  // GridBagLayout để sắp xếp form
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblName = new JLabel("Tên món:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtName.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 1;
        panel.add(txtName, gbc);

        JLabel lblPrice = new JLabel("Giá tiền:");
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        panel.add(lblPrice, gbc);

        JTextField txtPrice = new JTextField();
        txtPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrice.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 3;
        panel.add(txtPrice, gbc);

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 4;
        panel.add(lblStatus, gbc);

        JCheckBox chkAvailable = new JCheckBox("Còn món");
        chkAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkAvailable.setBackground(Color.WHITE);
        chkAvailable.setSelected(true);                                  // Mặc định: còn món
        gbc.gridy = 5;
        panel.add(chkAvailable, gbc);

        JLabel lblImage = new JLabel("Hình ảnh:");
        lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 6;
        panel.add(lblImage, gbc);

        JLabel lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultFoodImage(), 140, 140)); // Ảnh món ăn mặc định
        gbc.gridy = 7;
        panel.add(lblPreview, gbc);

        JButton btnChooseImage = new JButton("Chọn ảnh") {
            @Override
            protected void paintComponent(Graphics g) {                  // Custom paint button
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 123, 255));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Chọn ảnh")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("Chọn ảnh", x, y);
                g2.dispose();
            }
        };
        btnChooseImage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChooseImage.setPreferredSize(new Dimension(130, 42));
        btnChooseImage.setFocusPainted(false);
        btnChooseImage.setContentAreaFilled(false);
        btnChooseImage.setBorderPainted(true);
        btnChooseImage.setBorder(BorderFactory.createLineBorder(new Color(0, 86, 179), 4));
        btnChooseImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final BufferedImage[] selectedImage = {null};                    // Array để lưu ảnh
        btnChooseImage.addActionListener(e -> {
            BufferedImage img = ImageUtil.chooseImageFile(this);         // Mở file chooser
            if (img != null) {
                selectedImage[0] = img;
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140)); // Update preview
            }
        });
        gbc.gridy = 8;
        panel.add(btnChooseImage, gbc);

        int option = JOptionPane.showConfirmDialog(this, panel, "Thêm món",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {                           // Nếu user click OK
            String tenMon = txtName.getText().trim();
            String giaStr = txtPrice.getText().trim();

            if (!ValidationUtil.isNotEmpty(tenMon)) {                    // Validate: Tên không rỗng
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên món!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isPositiveNumber(giaStr)) {              // Validate: Giá là số dương
                JOptionPane.showMessageDialog(this, "Giá tiền phải là số dương!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MonDTO mon = new MonDTO();
            mon.setTenMon(tenMon);
            mon.setGiaTien(Double.parseDouble(giaStr));                  // Convert string → double
            mon.setTinhTrang(chkAvailable.isSelected() ? "true" : "false"); // Lưu trạng thái dạng string
            mon.setMaLoai(category.getMaLoai());                         // Set mã loại món

            if (selectedImage[0] != null) {                              // Nếu có chọn ảnh
                try {
                    BufferedImage resized = ImageUtil.resizeImage(selectedImage[0],
                            ImageUtil.FOOD_IMAGE_SIZE, ImageUtil.FOOD_IMAGE_SIZE); // Resize về kích thước chuẩn
                    mon.setHinhAnh(ImageUtil.imageToByteArray(resized)); // Convert → byte[]
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (monDAO.insertMon(mon)) {                                 // Insert vào database
                JOptionPane.showMessageDialog(this, "Thêm món thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards();                                         // Refresh để update số lượng món
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditDish(MonDTO mon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblName = new JLabel("Tên món:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblName, gbc);

        JTextField txtName = new JTextField(mon.getTenMon());
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtName.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 1;
        panel.add(txtName, gbc);

        JLabel lblPrice = new JLabel("Giá tiền:");
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        panel.add(lblPrice, gbc);

        JTextField txtPrice = new JTextField(String.format("%.0f", mon.getGiaTien()));
        txtPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrice.setPreferredSize(new Dimension(300, 35));
        gbc.gridy = 3;
        panel.add(txtPrice, gbc);

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 4;
        panel.add(lblStatus, gbc);

        // Get current status from tinhTrang field
        boolean isAvailable = "true".equals(mon.getTinhTrang());

        JCheckBox chkAvailable = new JCheckBox("Còn món");
        chkAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkAvailable.setBackground(Color.WHITE);
        chkAvailable.setSelected(isAvailable);
        gbc.gridy = 5;
        panel.add(chkAvailable, gbc);

        JLabel lblImage = new JLabel("Hình ảnh:");
        lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 6;
        panel.add(lblImage, gbc);

        JLabel lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);

        if (mon.getHinhAnh() != null) {
            try {
                BufferedImage img = ImageUtil.byteArrayToImage(mon.getHinhAnh());
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140));
            } catch (Exception e) {
                lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultFoodImage(), 140, 140));
            }
        } else {
            lblPreview.setIcon(ImageUtil.createImageIcon(ImageUtil.getDefaultFoodImage(), 140, 140));
        }

        gbc.gridy = 7;
        panel.add(lblPreview, gbc);

        JButton btnChooseImage = new JButton("Chọn ảnh mới") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 123, 255));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Chọn ảnh mới")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("Chọn ảnh mới", x, y);
                g2.dispose();
            }
        };
        btnChooseImage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnChooseImage.setPreferredSize(new Dimension(150, 42));
        btnChooseImage.setFocusPainted(false);
        btnChooseImage.setContentAreaFilled(false);
        btnChooseImage.setBorderPainted(true);
        btnChooseImage.setBorder(BorderFactory.createLineBorder(new Color(0, 86, 179), 4));
        btnChooseImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        final BufferedImage[] selectedImage = {null};
        btnChooseImage.addActionListener(e -> {
            BufferedImage img = ImageUtil.chooseImageFile(this);
            if (img != null) {
                selectedImage[0] = img;
                lblPreview.setIcon(ImageUtil.createImageIcon(img, 140, 140));
            }
        });
        gbc.gridy = 8;
        panel.add(btnChooseImage, gbc);

        int option = JOptionPane.showConfirmDialog(this, panel, "Sửa món",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String tenMon = txtName.getText().trim();
            String giaStr = txtPrice.getText().trim();

            if (!ValidationUtil.isNotEmpty(tenMon)) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên món!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtil.isPositiveNumber(giaStr)) {
                JOptionPane.showMessageDialog(this, "Giá tiền phải là số dương!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            mon.setTenMon(tenMon);
            mon.setGiaTien(Double.parseDouble(giaStr));

            // Save status as string in tinhTrang field
            mon.setTinhTrang(chkAvailable.isSelected() ? "true" : "false");

            if (selectedImage[0] != null) {
                try {
                    BufferedImage resized = ImageUtil.resizeImage(selectedImage[0],
                            ImageUtil.FOOD_IMAGE_SIZE, ImageUtil.FOOD_IMAGE_SIZE);
                    mon.setHinhAnh(ImageUtil.imageToByteArray(resized));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (monDAO.updateMon(mon)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards(); // Refresh to update dish count
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteDish(MonDTO mon) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa món này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (monDAO.deleteMon(mon.getMaMon())) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadMenuCards(); // Refresh to update dish count
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

