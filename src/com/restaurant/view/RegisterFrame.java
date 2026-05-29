package com.restaurant.view;

import com.restaurant.dao.NhanVienDAO;
import com.restaurant.model.NhanVienDTO;
import com.restaurant.utils.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Màn hình đăng ký tài khoản - Wizard 2 bước: thông tin cơ bản → giới tính + ngày sinh
 */
public class RegisterFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Step 1 fields
    private JTextField txtFullName;                                      // Field nhập họ tên
    private JTextField txtUsername;                                      // Field nhập username
    private JTextField txtEmail;                                         // Field nhập email
    private JTextField txtPhone;                                         // Field nhập số điện thoại
    private JPasswordField txtPassword;                                  // Field nhập password

    // Step 2 fields
    private ButtonGroup genderGroup;                                     // Group cho radio buttons giới tính
    private ButtonGroup roleGroup;                                       // Group cho radio buttons quyền
    private JRadioButton rbMale;                                         // Radio button "Nam"
    private JRadioButton rbFemale;                                       // Radio button "Nữ"
    private JRadioButton rbOther;                                        // Radio button "Khác"
    private JRadioButton rbAdmin;                                        // Radio button "Admin"
    private JRadioButton rbManager;                                      // Radio button "Manager"
    private JRadioButton rbStaff;                                        // Radio button "Staff"
    private JList<String> monthList;                                     // Scroll picker tháng (Jan-Dec)
    private JList<String> dayList;                                       // Scroll picker ngày (1-31)
    private JList<String> yearList;                                      // Scroll picker năm (1950-hiện tại)

    private NhanVienDAO nhanVienDAO;                                     // DAO xử lý đăng ký
    private CardLayout cardLayout;                                       // Layout chuyển đổi giữa Step 1 và Step 2
    private JPanel contentPanel;                                         // Panel chứa Step 1 và Step 2

    // Color scheme - Modern cyan theme
    private static final Color CYAN_BG = new Color(160, 231, 229);      // Background cyan nhạt
    private static final Color DARK_BLUE = new Color(0, 0, 139);        // Text/border xanh đậm
    private static final Color FIELD_BG = new Color(200, 245, 244);     // Field background cyan rất nhạt
    private static final Color NAVY_BLUE = new Color(0, 0, 128);        // Button xanh navy

    public RegisterFrame() {
        nhanVienDAO = new NhanVienDAO();                                 // Khởi tạo DAO

        setTitle("Đăng ký - Nhà hàng Hương Vị Việt");                   // Tiêu đề cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                  // Đóng app khi tắt
        setSize(450, 700);                                               // Kích thước cửa sổ (cao hơn để chứa form)
        setLocationRelativeTo(null);                                     // Hiển thị giữa màn hình
        setResizable(false);                                             // Không cho resize

        cardLayout = new CardLayout();                                   // Khởi tạo CardLayout
        contentPanel = new JPanel(cardLayout);                           // Panel chứa các step
        contentPanel.setBackground(CYAN_BG);                             // Màu nền cyan

        contentPanel.add(createStep1Panel(), "step1");                   // Thêm Step 1 panel
        contentPanel.add(createStep2Panel(), "step2");                   // Thêm Step 2 panel

        add(contentPanel);                                               // Thêm content panel vào frame
        setVisible(true);                                                // Hiển thị frame
    }

    private JPanel createStep1Panel() {
        // Main panel với BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));         // Panel chính với spacing 20px
        mainPanel.setBackground(CYAN_BG);                                // Màu nền cyan
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 40, 30, 40)); // Padding

        // ==================== TOP PANEL ====================
        // Back button + Title + Step indicator
        JPanel topPanel = new JPanel(new BorderLayout());                // Panel trên với BorderLayout
        topPanel.setBackground(CYAN_BG);                                 // Màu nền cyan

        // Back button "←" quay lại WelcomeFrame
        JButton btnBack = new JButton("←");                              // Nút back với icon mũi tên
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 24));           // Font size 24
        btnBack.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm
        btnBack.setBackground(CYAN_BG);                                  // Màu nền cyan
        btnBack.setBorderPainted(false);                                 // Không vẽ border
        btnBack.setFocusPainted(false);                                  // Không vẽ focus
        btnBack.setContentAreaFilled(false);                             // Không fill background
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));              // Con trỏ tay khi hover
        btnBack.addActionListener(e -> {                                 // Xử lý click
            dispose();                                                   // Đóng RegisterFrame
            new WelcomeFrame();                                          // Mở WelcomeFrame
        });

        // Title "TẠO TÀI KHOẢN"
        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER); // Label tiêu đề căn giữa
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));          // Font size 28
        lblTitle.setForeground(DARK_BLUE);                               // Màu chữ xanh đậm

        // Step indicator "1/2"
        JLabel lblStep = new JLabel("1/2");                              // Label hiển thị bước 1/2
        lblStep.setFont(new Font("Segoe UI", Font.PLAIN, 14));          // Font size 14
        lblStep.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm

        topPanel.add(btnBack, BorderLayout.WEST);                        // Thêm nút back bên trái
        topPanel.add(lblTitle, BorderLayout.CENTER);                     // Thêm title ở giữa
        topPanel.add(lblStep, BorderLayout.EAST);                        // Thêm step indicator bên phải

        // ==================== FORM PANEL ====================
        // Chứa tất cả input fields
        JPanel formPanel = new JPanel();                                 // Panel chứa form
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Layout dọc
        formPanel.setBackground(CYAN_BG);                                // Màu nền cyan

        // ==================== ROLE SELECTION ====================
        // Radio buttons: Admin / Manager / Staff
        JLabel lblRole = new JLabel("Chọn Quyền");                       // Label chọn quyền
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 16));           // Font size 16
        lblRole.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);               // Căn giữa

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10)); // Panel chứa radio buttons
        rolePanel.setBackground(CYAN_BG);                                // Màu nền cyan
        rolePanel.setMaximumSize(new Dimension(370, 50));                // Kích thước tối đa

        roleGroup = new ButtonGroup();                                   // Group cho radio buttons
        rbAdmin = createRadioButton("Admin");                            // Radio button Admin
        rbManager = createRadioButton("Manager");                        // Radio button Manager
        rbStaff = createRadioButton("Staff");                            // Radio button Staff

        roleGroup.add(rbAdmin);                                          // Thêm Admin vào group
        roleGroup.add(rbManager);                                        // Thêm Manager vào group
        roleGroup.add(rbStaff);                                          // Thêm Staff vào group
        rbAdmin.setSelected(true);                                       // Default: Admin

        rolePanel.add(rbAdmin);                                          // Thêm radio Admin
        rolePanel.add(rbManager);                                        // Thêm radio Manager
        rolePanel.add(rbStaff);                                          // Thêm radio Staff

        // ==================== FULL NAME FIELD ====================
        // Icon ✏️ + Text field
        JPanel fullNamePanel = createFieldPanelWithIcon("✏️", "Họ và tên"); // Panel họ tên với icon
        txtFullName = new JTextField();                                  // Field nhập họ tên
        txtFullName.setFont(new Font("Segoe UI", Font.PLAIN, 14));      // Font size 14
        txtFullName.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Padding
        txtFullName.setBackground(FIELD_BG);                             // Màu nền field
        addPlaceholder(txtFullName, "Nhập họ và tên đầy đủ");           // Thêm placeholder
        fullNamePanel.add(txtFullName, BorderLayout.CENTER);             // Thêm field vào panel

        // ==================== USERNAME FIELD ====================
        // Icon 👤 + Text field
        JPanel usernamePanel = createFieldPanelWithIcon("👤", "Tên tài khoản"); // Panel username với icon
        txtUsername = new JTextField();                                  // Field nhập username
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));      // Font size 14
        txtUsername.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Padding
        txtUsername.setBackground(FIELD_BG);                             // Màu nền field
        addPlaceholder(txtUsername, "Nhập tên tài khoản");              // Thêm placeholder
        usernamePanel.add(txtUsername, BorderLayout.CENTER);             // Thêm field vào panel

        // ==================== EMAIL FIELD ====================
        // Icon 📧 + Text field
        JPanel emailPanel = createFieldPanelWithIcon("📧", "Email");     // Panel email với icon
        txtEmail = new JTextField();                                     // Field nhập email
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));         // Font size 14
        txtEmail.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Padding
        txtEmail.setBackground(FIELD_BG);                                // Màu nền field
        addPlaceholder(txtEmail, "Nhập địa chỉ email");                 // Thêm placeholder
        emailPanel.add(txtEmail, BorderLayout.CENTER);                   // Thêm field vào panel

        // ==================== PHONE FIELD ====================
        // Icon 📞 + Text field
        JPanel phonePanel = createFieldPanelWithIcon("📞", "Số điện thoại"); // Panel phone với icon
        txtPhone = new JTextField();                                     // Field nhập số điện thoại
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));         // Font size 14
        txtPhone.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Padding
        txtPhone.setBackground(FIELD_BG);                                // Màu nền field
        addPlaceholder(txtPhone, "Nhập số điện thoại");                 // Thêm placeholder
        phonePanel.add(txtPhone, BorderLayout.CENTER);                   // Thêm field vào panel

        // ==================== PASSWORD FIELD WITH EYE ICON ====================
        // Icon 🔒 + Password field + Eye icon toggle
        JPanel passwordPanel = createFieldPanelWithIcon("🔒", "Mật khẩu"); // Panel password với icon
        JPanel passwordInputPanel = new JPanel(new BorderLayout());      // Panel chứa field + eye button
        passwordInputPanel.setBackground(FIELD_BG);                      // Màu nền field

        txtPassword = new JPasswordField();                              // Field nhập password
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));      // Font size 14
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); // Padding
        txtPassword.setBackground(FIELD_BG);                             // Màu nền field
        addPasswordPlaceholder(txtPassword, "Nhập mật khẩu");           // Thêm placeholder

        // Load eye icons từ resources/images/
        ImageIcon eyeSlashIcon = new ImageIcon("resources/images/eye-slash.png"); // Icon ẩn password
        ImageIcon eyeIcon = new ImageIcon("resources/images/eye.png");  // Icon hiện password

        // Scale icons về 32x32
        Image eyeSlashImg = eyeSlashIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH); // Resize eye-slash
        Image eyeImg = eyeIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH); // Resize eye
        eyeSlashIcon = new ImageIcon(eyeSlashImg);                       // Tạo lại icon đã resize
        eyeIcon = new ImageIcon(eyeImg);                                 // Tạo lại icon đã resize

        // Button toggle hiển thị/ẩn password
        JButton btnShowPassword = new JButton(eyeSlashIcon);             // Nút với icon eye-slash
        btnShowPassword.setPreferredSize(new Dimension(40, 40));         // Kích thước 40x40
        btnShowPassword.setBackground(FIELD_BG);                         // Màu nền field
        btnShowPassword.setBorderPainted(false);                         // Không vẽ border
        btnShowPassword.setFocusPainted(false);                          // Không vẽ focus
        btnShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));      // Con trỏ tay khi hover
        btnShowPassword.setContentAreaFilled(false);                     // Không fill background

        // Final variables cho lambda
        ImageIcon finalEyeIcon = eyeIcon;                                // Final variable cho eye icon
        ImageIcon finalEyeSlashIcon = eyeSlashIcon;                      // Final variable cho eye-slash icon
        btnShowPassword.addActionListener(e -> {                         // Xử lý click toggle
            char currentEcho = txtPassword.getEchoChar();                // Lấy echo char hiện tại

            if (currentEcho == 0 || currentEcho == '\0') {               // Nếu đang hiển thị password
                // Đang hiển thị → Ẩn password
                txtPassword.setEchoChar('•');                            // Ẩn bằng dấu chấm
                btnShowPassword.setIcon(finalEyeSlashIcon);              // Đổi icon sang eye-slash
            } else {                                                     // Nếu đang ẩn password
                // Đang ẩn → Hiển thị password
                txtPassword.setEchoChar((char) 0);                       // Hiển thị rõ password
                btnShowPassword.setIcon(finalEyeIcon);                   // Đổi icon sang eye
            }
        });

        passwordInputPanel.add(txtPassword, BorderLayout.CENTER);        // Thêm field vào giữa
        passwordInputPanel.add(btnShowPassword, BorderLayout.EAST);      // Thêm button bên phải
        passwordPanel.add(passwordInputPanel, BorderLayout.CENTER);      // Thêm input panel vào password panel

        // ==================== ADD ALL COMPONENTS ====================
        formPanel.add(lblRole);                                          // Thêm label chọn quyền
        formPanel.add(Box.createVerticalStrut(5));                       // Khoảng cách 5px
        formPanel.add(rolePanel);                                        // Thêm panel radio buttons quyền
        formPanel.add(Box.createVerticalStrut(12));                      // Khoảng cách 12px
        formPanel.add(fullNamePanel);                                    // Thêm panel họ tên
        formPanel.add(Box.createVerticalStrut(12));                      // Khoảng cách 12px
        formPanel.add(usernamePanel);                                    // Thêm panel username
        formPanel.add(Box.createVerticalStrut(12));                      // Khoảng cách 12px
        formPanel.add(emailPanel);                                       // Thêm panel email
        formPanel.add(Box.createVerticalStrut(12));                      // Khoảng cách 12px
        formPanel.add(phonePanel);                                       // Thêm panel phone
        formPanel.add(Box.createVerticalStrut(12));                      // Khoảng cách 12px
        formPanel.add(passwordPanel);                                    // Thêm panel password
        formPanel.add(Box.createVerticalStrut(20));                      // Khoảng cách 20px

        // ==================== NEXT BUTTON ====================
        JButton btnNext = new JButton("TIẾP THEO");                      // Nút tiếp theo
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 16));           // Font size 16
        btnNext.setForeground(Color.WHITE);                              // Màu chữ trắng
        btnNext.setBackground(DARK_BLUE);                                // Màu nền xanh đậm
        btnNext.setFocusPainted(false);                                  // Không vẽ focus
        btnNext.setBorderPainted(false);                                 // Không vẽ border
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));              // Con trỏ tay khi hover
        btnNext.setMaximumSize(new Dimension(370, 50));                 // Kích thước tối đa
        btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);               // Căn giữa
        btnNext.addActionListener(e -> validateStep1());                 // Validate và chuyển sang Step 2

        formPanel.add(btnNext);                                          // Thêm nút tiếp theo
        formPanel.add(Box.createVerticalStrut(20));                      // Khoảng cách 20px

        // ==================== LOGIN LINK ====================
        // "Đã có tài khoản? ĐĂNG NHẬP"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Panel chứa link đăng nhập
        bottomPanel.setBackground(CYAN_BG);                              // Màu nền cyan
        bottomPanel.setMaximumSize(new Dimension(370, 30));              // Kích thước tối đa

        JLabel lblHaveAccount = new JLabel("Đã có tài khoản?");         // Label text thường
        lblHaveAccount.setFont(new Font("Segoe UI", Font.PLAIN, 14));   // Font size 14
        lblHaveAccount.setForeground(DARK_BLUE);                         // Màu chữ xanh đậm

        JLabel lblLogin = new JLabel("ĐĂNG NHẬP");                       // Label link đăng nhập
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));          // Font size 14 bold
        lblLogin.setForeground(DARK_BLUE);                               // Màu chữ xanh đậm
        lblLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));             // Con trỏ tay khi hover
        lblLogin.addMouseListener(new java.awt.event.MouseAdapter() {   // Xử lý mouse events
            public void mouseClicked(java.awt.event.MouseEvent evt) {   // Khi click
                // Chuyển sang LoginFrame
                dispose();                                               // Đóng RegisterFrame
                new LoginFrame();                                        // Mở LoginFrame
            }
        });

        bottomPanel.add(lblHaveAccount);                                 // Thêm label text thường
        bottomPanel.add(lblLogin);                                       // Thêm label link
        formPanel.add(bottomPanel);                                      // Thêm bottom panel

        mainPanel.add(topPanel, BorderLayout.NORTH);                     // Thêm top panel ở trên
        mainPanel.add(formPanel, BorderLayout.CENTER);                   // Thêm form panel ở giữa

        return mainPanel;                                                // Trả về panel hoàn chỉnh
    }

    /**
     * Tạo Step 2 panel - Giới tính và Ngày sinh
     *
     * Components:
     * - Back button "←" → Quay lại Step 1
     * - Title "TẠO TÀI KHOẢN" + Step indicator "2/2"
     * - Radio buttons: Giới tính (Nam / Nữ / Khác)
     * - Scroll pickers: Ngày sinh (Month / Day / Year)
     * - Button "HOÀN THÀNH" → Tạo tài khoản và chuyển sang LoginFrame
     *
     * Scroll Pickers:
     * - Custom JList với style đặc biệt
     * - Selected item: Bold font + underline border
     * - Non-selected items: Gray color
     * - Default date: 01/01/2007
     *
     * @return JPanel chứa Step 2 UI
     */
    private JPanel createStep2Panel() {
        // Main panel với BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 30));         // Panel chính với spacing 30px
        mainPanel.setBackground(CYAN_BG);                                // Màu nền cyan
        mainPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40)); // Padding

        // ==================== TOP PANEL ====================
        // Back button + Title + Step indicator
        JPanel topPanel = new JPanel(new BorderLayout());                // Panel trên với BorderLayout
        topPanel.setBackground(CYAN_BG);                                 // Màu nền cyan

        // Back button "←" quay lại Step 1
        JButton btnBack = new JButton("←");                              // Nút back với icon mũi tên
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 24));           // Font size 24
        btnBack.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm
        btnBack.setBackground(CYAN_BG);                                  // Màu nền cyan
        btnBack.setBorderPainted(false);                                 // Không vẽ border
        btnBack.setFocusPainted(false);                                  // Không vẽ focus
        btnBack.setContentAreaFilled(false);                             // Không fill background
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));              // Con trỏ tay khi hover
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, "step1")); // Quay lại Step 1

        // Title "TẠO TÀI KHOẢN"
        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER); // Label tiêu đề căn giữa
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));          // Font size 28
        lblTitle.setForeground(DARK_BLUE);                               // Màu chữ xanh đậm

        // Step indicator "2/2"
        JLabel lblStep = new JLabel("2/2");                              // Label hiển thị bước 2/2
        lblStep.setFont(new Font("Segoe UI", Font.PLAIN, 14));          // Font size 14
        lblStep.setForeground(DARK_BLUE);                                // Màu chữ xanh đậm

        topPanel.add(btnBack, BorderLayout.WEST);                        // Thêm nút back bên trái
        topPanel.add(lblTitle, BorderLayout.CENTER);                     // Thêm title ở giữa
        topPanel.add(lblStep, BorderLayout.EAST);                        // Thêm step indicator bên phải

        // ==================== FORM PANEL ====================
        JPanel formPanel = new JPanel();                                 // Panel chứa form
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Layout dọc
        formPanel.setBackground(CYAN_BG);                                // Màu nền cyan

        // ==================== GENDER SELECTION ====================
        // Radio buttons: Nam / Nữ / Khác
        JLabel lblGender = new JLabel("Chọn Giới tính");                 // Label chọn giới tính
        lblGender.setFont(new Font("Segoe UI", Font.BOLD, 18));         // Font size 18
        lblGender.setForeground(DARK_BLUE);                              // Màu chữ xanh đậm
        lblGender.setAlignmentX(Component.CENTER_ALIGNMENT);             // Căn giữa

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15)); // Panel chứa radio buttons
        genderPanel.setBackground(CYAN_BG);                              // Màu nền cyan
        genderPanel.setMaximumSize(new Dimension(370, 60));              // Kích thước tối đa

        genderGroup = new ButtonGroup();                                 // Group cho radio buttons
        rbMale = createRadioButton("Nam");                               // Radio button Nam
        rbFemale = createRadioButton("Nữ");                              // Radio button Nữ
        rbOther = createRadioButton("Khác");                             // Radio button Khác

        genderGroup.add(rbMale);                                         // Thêm Nam vào group
        genderGroup.add(rbFemale);                                       // Thêm Nữ vào group
        genderGroup.add(rbOther);                                        // Thêm Khác vào group
        rbMale.setSelected(true);                                        // Default: Nam

        genderPanel.add(rbMale);                                         // Thêm radio Nam
        genderPanel.add(rbFemale);                                       // Thêm radio Nữ
        genderPanel.add(rbOther);                                        // Thêm radio Khác

        formPanel.add(lblGender);                                        // Thêm label giới tính
        formPanel.add(Box.createVerticalStrut(15));                      // Khoảng cách 15px
        formPanel.add(genderPanel);                                      // Thêm panel radio buttons
        formPanel.add(Box.createVerticalStrut(40));                      // Khoảng cách 40px

        // ==================== BIRTHDAY SELECTION ====================
        // 3 scroll pickers: Month / Day / Year
        JLabel lblBirthday = new JLabel("Chọn Ngày sinh");               // Label chọn ngày sinh
        lblBirthday.setFont(new Font("Segoe UI", Font.BOLD, 18));       // Font size 18
        lblBirthday.setForeground(DARK_BLUE);                            // Màu chữ xanh đậm
        lblBirthday.setAlignmentX(Component.CENTER_ALIGNMENT);           // Căn giữa

        JPanel birthdayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)); // Panel chứa 3 pickers
        birthdayPanel.setBackground(CYAN_BG);                            // Màu nền cyan
        birthdayPanel.setMaximumSize(new Dimension(370, 160));           // Kích thước tối đa

        // Tạo scroll pickers với default date: 01/01/2007
        // Month picker: Jan-Dec
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",    // Array tên tháng
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        monthList = createScrollPicker(months, "Jan", 100);              // Tạo picker tháng, default Jan

        // Day picker: 1-31
        String[] days = new String[31];                                  // Array ngày 1-31
        for (int i = 0; i < 31; i++) {                                   // Duyệt từ 0-30
            days[i] = String.valueOf(i + 1);                             // Gán giá trị 1-31
        }
        dayList = createScrollPicker(days, "1", 80);                     // Tạo picker ngày, default 1

        // Year picker: 1950 - năm hiện tại
        int currentYear = LocalDate.now().getYear();                     // Lấy năm hiện tại
        String[] years = new String[currentYear - 1950 + 1];             // Array năm từ 1950 đến hiện tại
        for (int i = 0; i < years.length; i++) {                         // Duyệt qua từng năm
            years[i] = String.valueOf(1950 + i);                         // Gán giá trị năm
        }
        yearList = createScrollPicker(years, "2007", 100);               // Tạo picker năm, default 2007

        birthdayPanel.add(createPickerWrapper(monthList));               // Thêm month picker
        birthdayPanel.add(createPickerWrapper(dayList));                 // Thêm day picker
        birthdayPanel.add(createPickerWrapper(yearList));                // Thêm year picker

        formPanel.add(lblBirthday);                                      // Thêm label ngày sinh
        formPanel.add(Box.createVerticalStrut(15));                      // Khoảng cách 15px
        formPanel.add(birthdayPanel);                                    // Thêm panel pickers
        formPanel.add(Box.createVerticalStrut(50));                      // Khoảng cách 50px

        // ==================== COMPLETE BUTTON ====================
        JButton btnComplete = new JButton("HOÀN THÀNH");                 // Nút hoàn thành
        btnComplete.setFont(new Font("Segoe UI", Font.BOLD, 16));       // Font size 16
        btnComplete.setForeground(Color.WHITE);                          // Màu chữ trắng
        btnComplete.setBackground(NAVY_BLUE);                            // Màu nền navy blue
        btnComplete.setFocusPainted(false);                              // Không vẽ focus
        btnComplete.setBorderPainted(false);                             // Không vẽ border
        btnComplete.setCursor(new Cursor(Cursor.HAND_CURSOR));          // Con trỏ tay khi hover
        btnComplete.setMaximumSize(new Dimension(370, 50));             // Kích thước tối đa
        btnComplete.setAlignmentX(Component.CENTER_ALIGNMENT);           // Căn giữa
        btnComplete.addActionListener(e -> handleRegister());            // Xử lý đăng ký

        formPanel.add(btnComplete);                                      // Thêm nút hoàn thành

        mainPanel.add(topPanel, BorderLayout.NORTH);                     // Thêm top panel ở trên
        mainPanel.add(formPanel, BorderLayout.CENTER);                   // Thêm form panel ở giữa

        return mainPanel;                                                // Trả về panel hoàn chỉnh
    }

    /**
     * Tạo field panel với icon
     *
     * Tạo panel chứa icon + text field với style nhất quán:
     * - Border: DARK_BLUE 2px
     * - Background: FIELD_BG
     * - Icon container: Fixed width 50px
     * - Icon: Emoji (✏️, 👤, 📧, 📞, 🔒)
     *
     * @param icon Icon emoji (VD: "👤")
     * @param placeholder Placeholder text (không sử dụng, chỉ để tham khảo)
     * @return JPanel chứa icon container (text field sẽ được add sau)
     */
    private JPanel createFieldPanelWithIcon(String icon, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));               // Panel với BorderLayout
        panel.setBackground(FIELD_BG);                                   // Màu nền field
        panel.setBorder(BorderFactory.createCompoundBorder(              // Border kép
            BorderFactory.createLineBorder(DARK_BLUE, 2),               // Border xanh đậm 2px
            BorderFactory.createEmptyBorder(0, 10, 0, 10)               // Padding trong
        ));
        panel.setMaximumSize(new Dimension(370, 45));                    // Kích thước tối đa

        // Icon container: Fixed width 50px để tất cả icons align đều
        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Container cho icon
        iconContainer.setBackground(FIELD_BG);                           // Màu nền field
        iconContainer.setPreferredSize(new Dimension(50, 45));           // Kích thước ưu tiên
        iconContainer.setMinimumSize(new Dimension(50, 45));             // Kích thước tối thiểu
        iconContainer.setMaximumSize(new Dimension(50, 45));             // Kích thước tối đa

        JLabel lblIcon = new JLabel(icon);                               // Label hiển thị icon emoji
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));    // Font emoji size 18
        lblIcon.setBorder(BorderFactory.createEmptyBorder(14, 5, 0, 0)); // Padding để căn giữa
        iconContainer.add(lblIcon);                                      // Thêm icon vào container

        panel.add(iconContainer, BorderLayout.WEST);                     // Thêm icon container bên trái

        return panel;                                                    // Trả về panel
    }

    /**
     * Tạo radio button với style nhất quán
     *
     * @param text Text hiển thị trên radio button
     * @return JRadioButton đã được style
     */
    private JRadioButton createRadioButton(String text) {
        JRadioButton rb = new JRadioButton(text);                        // Tạo radio button
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 15));               // Font size 15
        rb.setBackground(CYAN_BG);                                       // Màu nền cyan
        rb.setForeground(DARK_BLUE);                                     // Màu chữ xanh đậm
        rb.setFocusPainted(false);                                       // Không vẽ focus
        return rb;                                                       // Trả về radio button
    }

    /**
     * Tạo scroll picker (custom JList) cho date selection
     *
     * Features:
     * - Hiển thị 3 items cùng lúc (visibleRowCount = 3)
     * - Selected item: Bold font 20pt + underline border
     * - Non-selected items: Gray color, font 16pt
     * - Background: CYAN_BG
     * - Selection background: CYAN_BG (không đổi màu)
     *
     * @param items Array các items (VD: ["Jan", "Feb", ...])
     * @param defaultValue Item được chọn mặc định
     * @param width Chiều rộng của picker
     * @return JList đã được style
     */
    private JList<String> createScrollPicker(String[] items, String defaultValue, int width) {
        JList<String> list = new JList<>(items);                         // Tạo JList với array items
        list.setFont(new Font("Segoe UI", Font.PLAIN, 16));             // Font size 16
        list.setVisibleRowCount(3);                                      // Hiển thị 3 items
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);      // Chỉ chọn 1 item
        list.setSelectedValue(defaultValue, true);                       // Set default value
        list.setBackground(CYAN_BG);                                     // Màu nền cyan
        list.setSelectionBackground(CYAN_BG);                            // Không đổi màu khi select
        list.setSelectionForeground(DARK_BLUE);                          // Màu chữ khi select

        // Custom renderer: Style selected item khác với non-selected
        list.setCellRenderer(new DefaultListCellRenderer() {             // Custom renderer
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent( // Lấy label mặc định
                    list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(CENTER);                    // Căn giữa
                label.setBackground(CYAN_BG);                            // Màu nền cyan
                label.setOpaque(true);                                   // Vẽ background

                if (isSelected) {                                        // Nếu item được chọn
                    // Selected item: Bold + underline
                    label.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Font bold size 20
                    label.setForeground(DARK_BLUE);                      // Màu chữ xanh đậm
                    // Add underline border
                    label.setBorder(BorderFactory.createCompoundBorder(  // Border kép
                        BorderFactory.createEmptyBorder(8, 5, 5, 5),    // Padding ngoài
                        BorderFactory.createMatteBorder(0, 0, 2, 0, DARK_BLUE) // Underline
                    ));
                } else {                                                 // Nếu item không được chọn
                    // Non-selected item: Gray color
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Font plain size 16
                    label.setForeground(new Color(180, 180, 180));      // Màu xám
                    label.setBorder(BorderFactory.createEmptyBorder(8, 5, 7, 5)); // Padding
                }
                return label;                                            // Trả về label đã style
            }
        });

        return list;                                                     // Trả về JList
    }

    /**
     * Tạo scroll pane wrapper cho scroll picker
     *
     * Features:
     * - Minimal scrollbar (8px width)
     * - No arrow buttons
     * - Thumb color: Gray
     * - Track color: CYAN_BG
     * - Auto scroll để selected item ở top position
     *
     * @param list JList cần wrap
     * @return JScrollPane chứa list
     */
    private JScrollPane createPickerWrapper(JList<String> list) {
        JScrollPane scrollPane = new JScrollPane(list);                  // Tạo scroll pane cho list
        scrollPane.setPreferredSize(new Dimension(                       // Set kích thước ưu tiên
            list == monthList ? 100 : (list == dayList ? 80 : 100), 120)); // Month/Year: 100px, Day: 80px
        scrollPane.setBorder(null);                                      // Không có border
        scrollPane.setBackground(CYAN_BG);                               // Màu nền cyan
        scrollPane.getViewport().setBackground(CYAN_BG);                 // Màu nền viewport
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Không có scrollbar ngang
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Scrollbar dọc khi cần

        // Style the scrollbar to be minimal
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); // Chiều rộng 8px
        scrollPane.getVerticalScrollBar().setBackground(CYAN_BG);        // Màu nền scrollbar
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() { // Custom UI
            @Override
            protected void configureScrollBarColors() {                  // Cấu hình màu sắc
                this.thumbColor = new Color(150, 150, 150);              // Màu thumb xám
                this.trackColor = CYAN_BG;                               // Màu track cyan
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {    // Tạo nút giảm (ẩn)
                JButton button = new JButton();                          // Tạo button rỗng
                button.setPreferredSize(new Dimension(0, 0));            // Kích thước 0 (ẩn)
                return button;                                           // Trả về button
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {    // Tạo nút tăng (ẩn)
                JButton button = new JButton();                          // Tạo button rỗng
                button.setPreferredSize(new Dimension(0, 0));            // Kích thước 0 (ẩn)
                return button;                                           // Trả về button
            }
        });

        // Scroll to show selected item at TOP position
        SwingUtilities.invokeLater(() -> {                               // Chạy sau khi UI render xong
            int selectedIndex = list.getSelectedIndex();                 // Lấy index item được chọn
            if (selectedIndex >= 0) {                                    // Nếu có item được chọn
                Rectangle cellBounds = list.getCellBounds(selectedIndex, selectedIndex); // Lấy bounds của cell
                if (cellBounds != null) {                                // Nếu bounds tồn tại
                    // Scroll so selected item is at the top
                    scrollPane.getViewport().setViewPosition(new Point(0, cellBounds.y)); // Scroll đến vị trí item
                }
            }
        });

        return scrollPane;                                               // Trả về scroll pane
    }

    // Thêm placeholder text cho JTextField
    private void addPlaceholder(JTextField field, String placeholder) {
        field.setForeground(new Color(128, 128, 128));                   // Màu xám cho placeholder
        field.setText(placeholder);                                      // Set text gợi ý

        field.addFocusListener(new java.awt.event.FocusAdapter() {      // Lắng nghe focus events
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {    // Khi focus vào field
                if (field.getText().equals(placeholder)) {               // Nếu đang hiển thị placeholder
                    field.setText("");                                   // Xóa placeholder
                    field.setForeground(DARK_BLUE);                      // Đổi màu text thành xanh đậm
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {      // Khi focus ra khỏi field
                if (field.getText().isEmpty()) {                         // Nếu user không nhập gì
                    field.setForeground(new Color(128, 128, 128));       // Màu xám
                    field.setText(placeholder);                          // Hiển thị lại placeholder
                }
            }
        });
    }

    // Thêm placeholder text cho JPasswordField
    private void addPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setForeground(new Color(128, 128, 128));                   // Màu xám
        field.setText(placeholder);                                      // Set text gợi ý
        field.setEchoChar((char) 0);                                     // Hiển thị rõ placeholder (không ẩn)

        field.addFocusListener(new java.awt.event.FocusAdapter() {      // Lắng nghe focus events
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {    // Khi focus vào field
                String text = new String(field.getPassword());           // Lấy text hiện tại
                if (text.equals(placeholder)) {                          // Nếu đang hiển thị placeholder
                    field.setText("");                                   // Xóa placeholder
                    field.setForeground(DARK_BLUE);                      // Đổi màu text
                    field.setEchoChar('•');                              // Ẩn password bằng dấu chấm
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {      // Khi focus ra khỏi field
                if (field.getPassword().length == 0) {                   // Nếu user không nhập gì
                    field.setForeground(new Color(128, 128, 128));       // Màu xám
                    field.setText(placeholder);                          // Hiển thị lại placeholder
                    field.setEchoChar((char) 0);                         // Hiển thị rõ placeholder
                }
            }
        });
    }

    // Validate dữ liệu Step 1 trước khi chuyển sang Step 2
    private void validateStep1() {
        // Lấy dữ liệu từ các fields
        String fullName = txtFullName.getText();                         // Lấy họ tên
        String username = txtUsername.getText();                         // Lấy username
        String password = new String(txtPassword.getPassword());         // Lấy password
        String email = txtEmail.getText();                               // Lấy email
        String phone = txtPhone.getText();                               // Lấy số điện thoại

        // Loại bỏ placeholder text nếu user chưa nhập gì
        if (fullName.equals("Nhập họ và tên đầy đủ")) fullName = "";    // Xóa placeholder họ tên
        if (username.equals("Nhập tên tài khoản")) username = "";       // Xóa placeholder username
        if (password.equals("Nhập mật khẩu")) password = "";            // Xóa placeholder password
        if (email.equals("Nhập địa chỉ email")) email = "";             // Xóa placeholder email
        if (phone.equals("Nhập số điện thoại")) phone = "";             // Xóa placeholder phone

        // Sanitize input: Trim whitespace
        fullName = ValidationUtil.sanitize(fullName);                    // Trim khoảng trắng họ tên
        username = ValidationUtil.sanitize(username);                    // Trim khoảng trắng username
        email = ValidationUtil.sanitize(email);                          // Trim khoảng trắng email
        phone = ValidationUtil.sanitize(phone);                          // Trim khoảng trắng phone

        // Validation: Họ tên không rỗng
        if (!ValidationUtil.isNotEmpty(fullName)) {                      // Nếu họ tên rỗng
            showError("Vui lòng nhập họ tên!");                          // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Validation: Username 3-20 ký tự, chỉ chữ và số
        if (!ValidationUtil.isValidUsername(username)) {                 // Nếu username không hợp lệ
            showError("Tên đăng nhập phải từ 3-20 ký tự, chỉ chứa chữ và số!"); // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Validation: Username không trùng trong database
        if (nhanVienDAO.isUsernameExists(username)) {                    // Nếu username đã tồn tại
            showError("Tên đăng nhập đã tồn tại!");                      // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Validation: Password ít nhất 6 ký tự, không dấu tiếng Việt
        if (!ValidationUtil.isValidPassword(password)) {                 // Nếu password không hợp lệ
            showError("Mật khẩu phải có ít nhất 6 ký tự và không chứa dấu tiếng Việt!"); // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Validation: Email format hợp lệ (nếu không rỗng)
        if (ValidationUtil.isNotEmpty(email) && !ValidationUtil.isValidEmail(email)) { // Nếu email không hợp lệ
            showError("Email không hợp lệ!");                            // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Validation: Phone 10-11 chữ số (nếu không rỗng)
        if (ValidationUtil.isNotEmpty(phone) && !ValidationUtil.isValidPhone(phone)) { // Nếu phone không hợp lệ
            showError("Số điện thoại phải có 10-11 chữ số!");            // Hiển thị lỗi
            return;                                                      // Dừng xử lý
        }

        // Tất cả validation OK → Chuyển sang Step 2
        cardLayout.show(contentPanel, "step2");                          // Hiển thị Step 2
    }

    // Xử lý đăng ký tài khoản mới (khi click "HOÀN THÀNH" ở Step 2)
    private void handleRegister() {
        try {
            // Lấy dữ liệu Step 1
            String fullName = txtFullName.getText();                     // Lấy họ tên
            String username = txtUsername.getText();                     // Lấy username
            String password = new String(txtPassword.getPassword());     // Lấy password
            String email = txtEmail.getText();                           // Lấy email
            String phone = txtPhone.getText();                           // Lấy số điện thoại

            // Loại bỏ placeholder text
            if (fullName.equals("Nhập họ và tên đầy đủ")) fullName = ""; // Xóa placeholder họ tên
            if (username.equals("Nhập tên tài khoản")) username = "";   // Xóa placeholder username
            if (password.equals("Nhập mật khẩu")) password = "";        // Xóa placeholder password
            if (email.equals("Nhập địa chỉ email")) email = "";         // Xóa placeholder email
            if (phone.equals("Nhập số điện thoại")) phone = "";         // Xóa placeholder phone

            // Sanitize input
            fullName = ValidationUtil.sanitize(fullName);                // Trim khoảng trắng họ tên
            username = ValidationUtil.sanitize(username);                // Trim khoảng trắng username
            email = ValidationUtil.sanitize(email);                      // Trim khoảng trắng email
            phone = ValidationUtil.sanitize(phone);                      // Trim khoảng trắng phone

            // Lấy dữ liệu Step 2: Giới tính
            String gender = rbMale.isSelected() ? "Nam" : rbFemale.isSelected() ? "Nữ" : "Khác"; // Xác định giới tính

            // Lấy quyền: Admin=1, Manager=2, Staff=3
            int roleId = rbAdmin.isSelected() ? 1 : rbManager.isSelected() ? 2 : 3; // Xác định quyền

            // Lấy ngày sinh từ scroll pickers
            String monthStr = monthList.getSelectedValue();              // Lấy tháng (VD: "Jan")
            int month = getMonthNumber(monthStr);                        // Convert "Jan" → 1
            int day = Integer.parseInt(dayList.getSelectedValue());      // Lấy ngày (VD: "15")
            int year = Integer.parseInt(yearList.getSelectedValue());    // Lấy năm (VD: "2007")
            String birthday = String.format("%04d-%02d-%02d", year, month, day); // Format: yyyy-MM-dd

            // Tạo NhanVienDTO object
            NhanVienDTO nv = new NhanVienDTO();                          // Khởi tạo DTO
            nv.setHoTenNV(fullName);                                     // Set họ tên
            nv.setTenDN(username);                                       // Set username
            nv.setMatKhau(password);                                     // Set password (sẽ được hash BCrypt trong DAO)
            nv.setEmail(email);                                          // Set email
            nv.setSdt(phone);                                            // Set số điện thoại
            nv.setGioiTinh(gender);                                      // Set giới tính
            nv.setNgaySinh(birthday);                                    // Set ngày sinh
            nv.setMaQuyen(roleId);                                       // Set quyền

            // Insert vào database
            if (nhanVienDAO.insertNhanVien(nv)) {                        // Nếu insert thành công
                // Thành công: Hiển thị thông báo và chuyển sang LoginFrame
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!\nVui lòng đăng nhập.", // Thông báo thành công
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();                                               // Đóng RegisterFrame
                new LoginFrame();                                        // Mở LoginFrame
            } else {                                                     // Nếu insert thất bại
                // Thất bại: Hiển thị thông báo lỗi
                showError("Đăng ký thất bại! Vui lòng thử lại.");        // Thông báo lỗi
            }
        } catch (Exception e) {                                          // Catch tất cả exceptions
            // Catch tất cả exceptions
            e.printStackTrace();                                         // In stack trace
            showError("Lỗi: " + e.getMessage());                         // Hiển thị thông báo lỗi
        }
    }

    // Convert tên tháng (Jan-Dec) sang số tháng (1-12)
    private int getMonthNumber(String monthStr) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",    // Array tên tháng
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Tìm index của tháng trong array
        for (int i = 0; i < months.length; i++) {                        // Duyệt qua từng tháng
            if (months[i].equals(monthStr)) {                            // Nếu tìm thấy tháng
                return i + 1;                                            // Index 0 → Tháng 1, Index 11 → Tháng 12
            }
        }

        return 1;                                                        // Fallback: Return 1 nếu không tìm thấy
    }

    // Hiển thị thông báo lỗi
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE); // Dialog lỗi
    }
}
