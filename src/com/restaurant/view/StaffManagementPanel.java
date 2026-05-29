package com.restaurant.view;

import com.restaurant.dao.NhanVienDAO;
import com.restaurant.dao.QuyenDAO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.model.QuyenDTO;
import com.restaurant.utils.ValidationUtil;
import com.restaurant.utils.SessionManager;
import com.restaurant.utils.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý nhân viên (chỉ Admin)
 * Giao diện: Toolbar tìm kiếm + Table danh sách nhân viên + Buttons thêm/sửa/xóa
 */
public class StaffManagementPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private NhanVienDAO nhanVienDAO;
    private QuyenDAO quyenDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    /**
     * Constructor: Khởi tạo panel quản lý nhân viên
     */
    public StaffManagementPanel() {
        // Kiểm tra quyền truy cập - Chỉ Admin mới được vào
        if (!SessionManager.getInstance().isAdmin()) {
            setLayout(new BorderLayout());                               // BorderLayout: Message (CENTER)
            setBackground(UIConstants.BG_PRIMARY);

            JLabel lblAccessDenied = new JLabel("⛔ TRUY CẬP BỊ TỪ CHỐI", SwingConstants.CENTER);
            lblAccessDenied.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblAccessDenied.setForeground(UIConstants.DANGER_COLOR);

            JLabel lblMessage = new JLabel("Chỉ Admin mới có quyền truy cập chức năng này!", SwingConstants.CENTER);
            lblMessage.setFont(UIConstants.FONT_NORMAL);
            lblMessage.setForeground(UIConstants.TEXT_SECONDARY);

            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS)); // BoxLayout dọc
            messagePanel.setBackground(UIConstants.BG_PRIMARY);
            lblAccessDenied.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(Box.createVerticalGlue());                  // Căn giữa theo chiều dọc
            messagePanel.add(lblAccessDenied);
            messagePanel.add(Box.createVerticalStrut(20));
            messagePanel.add(lblMessage);
            messagePanel.add(Box.createVerticalGlue());

            add(messagePanel, BorderLayout.CENTER);
            return;                                                      // Dừng lại, không khởi tạo các components khác
        }

        nhanVienDAO = new NhanVienDAO();
        quyenDAO = new QuyenDAO();

        setLayout(new BorderLayout(15, 15));                             // BorderLayout: Title (NORTH) + Content (CENTER)
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));      // BorderLayout: Toolbar (NORTH) + Table (CENTER)
        centerPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel toolbarPanel = new JPanel(new BorderLayout(10, 0));      // BorderLayout: Search (WEST) + Buttons (EAST)
        toolbarPanel.setBackground(UIConstants.BG_PRIMARY);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // FlowLayout ngang cho search
        searchPanel.setBackground(UIConstants.BG_PRIMARY);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_NORMAL);
        searchPanel.add(lblSearch);

        txtSearch = UIConstants.createStyledTextField();
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.addActionListener(e -> searchStaff());                // Enter để tìm kiếm
        searchPanel.add(txtSearch);

        JButton btnSearch = UIConstants.createPrimaryButton("Tìm");
        btnSearch.addActionListener(e -> searchStaff());                // Click button để tìm kiếm
        searchPanel.add(btnSearch);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // FlowLayout ngang cho buttons
        btnPanel.setBackground(UIConstants.BG_PRIMARY);

        JButton btnAdd = UIConstants.createSuccessButton("+ Thêm nhân viên");
        btnAdd.setPreferredSize(new Dimension(160, 38));
        btnAdd.addActionListener(e -> handleAddStaff());                // Mở dialog thêm nhân viên

        JButton btnEdit = UIConstants.createPrimaryButton("Sửa");
        btnEdit.addActionListener(e -> handleEditStaff());              // Mở dialog sửa nhân viên

        JButton btnDelete = UIConstants.createDangerButton("Xóa");
        btnDelete.addActionListener(e -> handleDeleteStaff());          // Xóa nhân viên

        JButton btnRefresh = UIConstants.createSecondaryButton("Làm mới");
        btnRefresh.addActionListener(e -> loadStaff());                 // Refresh danh sách

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        toolbarPanel.add(searchPanel, BorderLayout.WEST);               // Search bên trái
        toolbarPanel.add(btnPanel, BorderLayout.EAST);                  // Buttons bên phải

        centerPanel.add(toolbarPanel, BorderLayout.NORTH);              // Toolbar ở trên

        String[] columns = {"STT", "Họ tên", "Username", "Email", "SĐT", "Giới tính", "Quyền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                            // Table không cho edit
            }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_NORMAL);
        table.setRowHeight(35);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        table.setSelectionForeground(UIConstants.TEXT_WHITE);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 225, 230));
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setFont(UIConstants.FONT_SUBHEADER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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

        // Set độ rộng các cột
        table.getColumnModel().getColumn(0).setPreferredWidth(70);      // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(180);     // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(120);     // Username
        table.getColumnModel().getColumn(3).setPreferredWidth(200);     // Email
        table.getColumnModel().getColumn(4).setPreferredWidth(120);     // SĐT
        table.getColumnModel().getColumn(5).setPreferredWidth(100);     // Giới tính
        table.getColumnModel().getColumn(6).setPreferredWidth(100);     // Quyền

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2));
        centerPanel.add(scrollPane, BorderLayout.CENTER);               // Table ở giữa

        add(centerPanel, BorderLayout.CENTER);

        loadStaff();                                                     // Load danh sách nhân viên
    }

    /**
     * Load danh sách nhân viên từ database
     */
    private void loadStaff() {
        tableModel.setRowCount(0);                                       // Xóa tất cả rows cũ
        List<NhanVienDTO> staffList = nhanVienDAO.getAllNhanVien();     // Lấy tất cả nhân viên

        int stt = 1;                                                     // Biến đếm STT
        for (NhanVienDTO nv : staffList) {                               // Duyệt qua từng nhân viên
            // Bỏ qua tài khoản Admin (MAQUYEN = 1)
            if (nv.getMaQuyen() == 1) {
                continue;
            }

            QuyenDTO quyen = quyenDAO.getQuyenById(nv.getMaQuyen());    // Lấy thông tin quyền
            Object[] row = new Object[]{
                stt++,                                                   // STT tự động tăng
                nv.getHoTenNV(),
                nv.getTenDN(),
                nv.getEmail() != null ? nv.getEmail() : "",             // Nếu null thì hiển thị ""
                nv.getSdt() != null ? nv.getSdt() : "",
                nv.getGioiTinh() != null ? nv.getGioiTinh() : "",
                quyen != null ? quyen.getTenQuyen() : ""
            };
            tableModel.addRow(row);
            // Lưu MANV vào client property để dùng khi edit/delete (vì table hiển thị STT thay vì MANV)
            table.putClientProperty("MANV_" + (tableModel.getRowCount() - 1), nv.getMaNV());
        }
    }

    /**
     * Tìm kiếm nhân viên theo tên hoặc username
     */
    private void searchStaff() {
        String keyword = txtSearch.getText().trim().toLowerCase();      // Lấy keyword, chuyển thành lowercase
        if (keyword.isEmpty()) {                                         // Nếu rỗng → load tất cả
            loadStaff();
            return;
        }

        tableModel.setRowCount(0);                                       // Xóa tất cả rows cũ
        List<NhanVienDTO> staffList = nhanVienDAO.getAllNhanVien();

        int stt = 1;
        for (NhanVienDTO nv : staffList) {
            if (nv.getMaQuyen() == 1) {                                  // Bỏ qua Admin
                continue;
            }

            // Tìm kiếm theo họ tên hoặc username (không phân biệt hoa thường)
            if (nv.getHoTenNV().toLowerCase().contains(keyword) ||
                nv.getTenDN().toLowerCase().contains(keyword)) {
                QuyenDTO quyen = quyenDAO.getQuyenById(nv.getMaQuyen());
                Object[] row = new Object[]{
                    stt++,
                    nv.getHoTenNV(),
                    nv.getTenDN(),
                    nv.getEmail() != null ? nv.getEmail() : "",
                    nv.getSdt() != null ? nv.getSdt() : "",
                    nv.getGioiTinh() != null ? nv.getGioiTinh() : "",
                    quyen != null ? quyen.getTenQuyen() : ""
                };
                tableModel.addRow(row);
                table.putClientProperty("MANV_" + (tableModel.getRowCount() - 1), nv.getMaNV());
            }
        }
    }

    /**
     * Xử lý thêm nhân viên mới
     */
    private void handleAddStaff() {
        System.out.println("handleAddStaff() called");
        JPanel panel = new JPanel(new GridBagLayout());                 // GridBagLayout để sắp xếp form
        panel.setBackground(UIConstants.BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;                        // Component fill theo chiều ngang
        gbc.insets = new Insets(8, 8, 8, 8);

        // Name
        JLabel lblName = new JLabel("Họ tên:");
        lblName.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblName, gbc);

        JTextField txtName = UIConstants.createStyledTextField();
        txtName.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblUsername, gbc);

        JTextField txtUsername = UIConstants.createStyledTextField();
        txtUsername.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPassword, gbc);

        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setFont(UIConstants.FONT_NORMAL);
        txtPassword.setPreferredSize(new Dimension(300, 35));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblEmail, gbc);

        JTextField txtEmail = UIConstants.createStyledTextField();
        txtEmail.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        // Phone
        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblPhone, gbc);

        JTextField txtPhone = UIConstants.createStyledTextField();
        txtPhone.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(txtPhone, gbc);

        // Gender
        JLabel lblGender = new JLabel("Giới tính:");
        lblGender.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(lblGender, gbc);

        JComboBox<String> cboGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cboGender.setFont(UIConstants.FONT_NORMAL);
        cboGender.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(cboGender, gbc);

        // Birth Date
        JLabel lblBirthDate = new JLabel("Ngày sinh:");
        lblBirthDate.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(lblBirthDate, gbc);

        JTextField txtBirthDate = UIConstants.createStyledTextField();
        txtBirthDate.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(txtBirthDate, gbc);

        JLabel lblBirthDateNote = new JLabel("(Định dạng: YYYY-MM-DD, ví dụ: 1990-01-15)");
        lblBirthDateNote.setFont(UIConstants.FONT_SMALL);
        lblBirthDateNote.setForeground(UIConstants.TEXT_SECONDARY);
        gbc.gridx = 1;
        gbc.gridy = 7;
        panel.add(lblBirthDateNote, gbc);

        JLabel lblRole = new JLabel("Quyền:");
        lblRole.setFont(UIConstants.FONT_NORMAL);
        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(lblRole, gbc);

        JComboBox<QuyenDTO> cboRole = new JComboBox<>();
        cboRole.setFont(UIConstants.FONT_NORMAL);
        cboRole.setPreferredSize(new Dimension(300, 35));
        List<QuyenDTO> roles = quyenDAO.getAllQuyen();                   // Lấy danh sách quyền từ database
        for (QuyenDTO quyen : roles) {
            cboRole.addItem(quyen);                                      // Thêm vào combobox
        }
        gbc.gridx = 1;
        panel.add(cboRole, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);                // Wrap panel trong scroll pane
        scrollPane.setPreferredSize(new Dimension(500, 500));
        scrollPane.setBorder(null);

        System.out.println("About to show dialog");
        int option = JOptionPane.showConfirmDialog(                      // Hiển thị dialog
                SwingUtilities.getWindowAncestor(this),
                scrollPane,
                "Thêm nhân viên",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        System.out.println("Dialog closed with option: " + option);

        if (option == JOptionPane.OK_OPTION) {                           // Nếu user click OK
            String name = txtName.getText().trim();
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String birthDate = txtBirthDate.getText().trim();

            // Validation - Kiểm tra dữ liệu nhập vào
            if (!ValidationUtil.isNotEmpty(name)) {
                showError("Vui lòng nhập họ tên!");
                return;
            }

            if (!ValidationUtil.isValidUsername(username)) {             // Username: 3-20 ký tự, chỉ chữ và số
                showError("Username phải từ 3-20 ký tự, chỉ chứa chữ và số!");
                return;
            }

            if (nhanVienDAO.isUsernameExists(username)) {                // Kiểm tra username đã tồn tại chưa
                showError("Username đã tồn tại!");
                return;
            }

            if (!ValidationUtil.isValidPassword(password)) {             // Password: ít nhất 6 ký tự
                showError("Mật khẩu phải có ít nhất 6 ký tự và không chứa dấu tiếng Việt!");
                return;
            }

            if (ValidationUtil.isNotEmpty(email) && !ValidationUtil.isValidEmail(email)) {
                showError("Email không hợp lệ!");
                return;
            }

            if (ValidationUtil.isNotEmpty(phone) && !ValidationUtil.isValidPhone(phone)) { // SĐT: 10-11 chữ số
                showError("Số điện thoại phải có 10-11 chữ số!");
                return;
            }

            if (ValidationUtil.isNotEmpty(birthDate) && !ValidationUtil.isValidDate(birthDate)) { // Ngày sinh: YYYY-MM-DD
                showError("Ngày sinh không hợp lệ! Định dạng: YYYY-MM-DD");
                return;
            }

            // Tạo object NhanVienDTO và set các giá trị
            NhanVienDTO nv = new NhanVienDTO();
            nv.setHoTenNV(name);
            nv.setTenDN(username);
            nv.setMatKhau(password);
            nv.setEmail(email);
            nv.setSdt(phone);
            nv.setGioiTinh((String) cboGender.getSelectedItem());
            nv.setNgaySinh(birthDate);
            nv.setMaQuyen(((QuyenDTO) cboRole.getSelectedItem()).getMaQuyen());

            if (nhanVienDAO.insertNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadStaff();
            } else {
                showError("Thêm thất bại!");
            }
        }
    }

    private void handleEditStaff() {
        System.out.println("handleEditStaff() called");
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Vui lòng chọn nhân viên cần sửa!");
            return;
        }

        // Get MANV from client property
        Integer maNV = (Integer) table.getClientProperty("MANV_" + selectedRow);
        if (maNV == null) {
            showError("Không tìm thấy thông tin nhân viên!");
            return;
        }

        NhanVienDTO nv = nhanVienDAO.getNhanVienById(maNV);

        if (nv != null) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(UIConstants.BG_SECONDARY);
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 8, 8, 8);

            // Name
            JLabel lblName = new JLabel("Họ tên:");
            lblName.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(lblName, gbc);

            JTextField txtName = UIConstants.createStyledTextField();
            txtName.setText(nv.getHoTenNV());
            txtName.setPreferredSize(new Dimension(300, 35));
            gbc.gridx = 1;
            panel.add(txtName, gbc);

            // Username (read-only)
            JLabel lblUsername = new JLabel("Username:");
            lblUsername.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(lblUsername, gbc);

            JTextField txtUsername = UIConstants.createStyledTextField();
            txtUsername.setText(nv.getTenDN());
            txtUsername.setEditable(false);
            txtUsername.setBackground(UIConstants.BG_DARK);
            txtUsername.setPreferredSize(new Dimension(300, 35));
            gbc.gridx = 1;
            panel.add(txtUsername, gbc);

            // New Password (optional)
            JLabel lblPassword = new JLabel("Mật khẩu mới:");
            lblPassword.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(lblPassword, gbc);

            JPasswordField txtPassword = new JPasswordField();
            txtPassword.setFont(UIConstants.FONT_NORMAL);
            txtPassword.setPreferredSize(new Dimension(300, 35));
            txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            gbc.gridx = 1;
            panel.add(txtPassword, gbc);

            JLabel lblPasswordNote = new JLabel("(Để trống nếu không đổi)");
            lblPasswordNote.setFont(UIConstants.FONT_SMALL);
            lblPasswordNote.setForeground(UIConstants.TEXT_SECONDARY);
            gbc.gridx = 1;
            gbc.gridy = 3;
            panel.add(lblPasswordNote, gbc);

            // Email
            JLabel lblEmail = new JLabel("Email:");
            lblEmail.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(lblEmail, gbc);

            JTextField txtEmail = UIConstants.createStyledTextField();
            txtEmail.setText(nv.getEmail() != null ? nv.getEmail() : "");
            txtEmail.setPreferredSize(new Dimension(300, 35));
            gbc.gridx = 1;
            panel.add(txtEmail, gbc);

            // Phone
            JLabel lblPhone = new JLabel("Số điện thoại:");
            lblPhone.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 5;
            panel.add(lblPhone, gbc);

            JTextField txtPhone = UIConstants.createStyledTextField();
            txtPhone.setText(nv.getSdt() != null ? nv.getSdt() : "");
            txtPhone.setPreferredSize(new Dimension(300, 35));
            gbc.gridx = 1;
            panel.add(txtPhone, gbc);

            // Gender
            JLabel lblGender = new JLabel("Giới tính:");
            lblGender.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 6;
            panel.add(lblGender, gbc);

            JComboBox<String> cboGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
            cboGender.setFont(UIConstants.FONT_NORMAL);
            cboGender.setPreferredSize(new Dimension(300, 35));
            if (nv.getGioiTinh() != null) {
                cboGender.setSelectedItem(nv.getGioiTinh());
            }
            gbc.gridx = 1;
            panel.add(cboGender, gbc);

            // Birth Date
            JLabel lblBirthDate = new JLabel("Ngày sinh:");
            lblBirthDate.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 7;
            panel.add(lblBirthDate, gbc);

            JTextField txtBirthDate = UIConstants.createStyledTextField();
            txtBirthDate.setText(nv.getNgaySinh() != null ? nv.getNgaySinh() : "");
            txtBirthDate.setPreferredSize(new Dimension(300, 35));
            gbc.gridx = 1;
            panel.add(txtBirthDate, gbc);

            JLabel lblBirthDateNote = new JLabel("(Định dạng: YYYY-MM-DD, ví dụ: 1990-01-15)");
            lblBirthDateNote.setFont(UIConstants.FONT_SMALL);
            lblBirthDateNote.setForeground(UIConstants.TEXT_SECONDARY);
            gbc.gridx = 1;
            gbc.gridy = 8;
            panel.add(lblBirthDateNote, gbc);

            // Role
            JLabel lblRole = new JLabel("Quyền:");
            lblRole.setFont(UIConstants.FONT_NORMAL);
            gbc.gridx = 0;
            gbc.gridy = 9;
            panel.add(lblRole, gbc);

            JComboBox<QuyenDTO> cboRole = new JComboBox<>();
            cboRole.setFont(UIConstants.FONT_NORMAL);
            cboRole.setPreferredSize(new Dimension(300, 35));
            List<QuyenDTO> roles = quyenDAO.getAllQuyen();
            for (QuyenDTO quyen : roles) {
                cboRole.addItem(quyen);
                if (quyen.getMaQuyen() == nv.getMaQuyen()) {
                    cboRole.setSelectedItem(quyen);
                }
            }
            gbc.gridx = 1;
            panel.add(cboRole, gbc);

            // Wrap panel in JScrollPane to handle large forms
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setPreferredSize(new Dimension(500, 550));
            scrollPane.setBorder(null);

            System.out.println("About to show edit dialog");
            int option = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    scrollPane,
                    "Sửa thông tin nhân viên",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            System.out.println("Edit dialog closed with option: " + option);

            if (option == JOptionPane.OK_OPTION) {
                String name = txtName.getText().trim();
                String password = new String(txtPassword.getPassword());
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                String birthDate = txtBirthDate.getText().trim();

                // Validation
                if (!ValidationUtil.isNotEmpty(name)) {
                    showError("Vui lòng nhập họ tên!");
                    return;
                }

                if (ValidationUtil.isNotEmpty(password) && !ValidationUtil.isValidPassword(password)) {
                    showError("Mật khẩu phải có ít nhất 6 ký tự!");
                    return;
                }

                if (ValidationUtil.isNotEmpty(email) && !ValidationUtil.isValidEmail(email)) {
                    showError("Email không hợp lệ!");
                    return;
                }

                if (ValidationUtil.isNotEmpty(phone) && !ValidationUtil.isValidPhone(phone)) {
                    showError("Số điện thoại phải có 10-11 chữ số!");
                    return;
                }

                if (ValidationUtil.isNotEmpty(birthDate) && !ValidationUtil.isValidDate(birthDate)) {
                    showError("Ngày sinh không hợp lệ! Định dạng: YYYY-MM-DD");
                    return;
                }

                nv.setHoTenNV(name);
                if (ValidationUtil.isNotEmpty(password)) {
                    nv.setMatKhau(password);
                }
                nv.setEmail(email);
                nv.setSdt(phone);
                nv.setGioiTinh((String) cboGender.getSelectedItem());
                nv.setNgaySinh(birthDate);
                nv.setMaQuyen(((QuyenDTO) cboRole.getSelectedItem()).getMaQuyen());

                if (nhanVienDAO.updateNhanVien(nv)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadStaff();
                } else {
                    showError("Cập nhật thất bại!");
                }
            }
        }
    }

    private void handleDeleteStaff() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Vui lòng chọn nhân viên cần xóa!");
            return;
        }

        // Get MANV from client property
        Integer maNV = (Integer) table.getClientProperty("MANV_" + selectedRow);
        if (maNV == null) {
            showError("Không tìm thấy thông tin nhân viên!");
            return;
        }

        String tenNV = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên: " + tenNV + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.deleteNhanVien(maNV)) {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadStaff();
            } else {
                showError("Xóa thất bại! Nhân viên có thể đang có đơn hàng liên quan.");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
