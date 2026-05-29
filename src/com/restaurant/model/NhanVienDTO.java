package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng NHANVIEN trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin nhân viên (Admin, Manager, Staff)
 * - Transfer data giữa các layer: DAO ↔ Business Logic ↔ UI
 * - Validation dữ liệu trước khi lưu vào database
 *
 * Business Rules:
 * - MANV: Auto-increment, primary key
 * - TENDN: Unique, dùng để đăng nhập (username)
 * - MATKHAU: Mã hóa bằng BCrypt trước khi lưu (PasswordUtil.hashPassword)
 * - EMAIL: Unique, dùng để reset password
 * - SDT: Format 10 số (0xxxxxxxxx)
 * - GIOITINH: "Nam" hoặc "Nữ"
 * - NGAYSINH: Format yyyy-MM-dd
 * - MAQUYEN: Foreign key → QUYEN (1=Admin, 2=Manager, 3=Staff)
 *
 * Sử dụng trong:
 * - LoginFrame: Xác thực đăng nhập
 * - RegisterFrame: Đăng ký tài khoản mới
 * - StaffManagementPanel: Quản lý nhân viên (CRUD)
 * - SessionManager: Lưu thông tin user đang đăng nhập
 */
public class NhanVienDTO {
    private int maNV;           // Mã nhân viên (Primary Key, Auto-increment)
    private String hoTenNV;     // Họ tên đầy đủ (VD: "Nguyễn Văn A")
    private String tenDN;       // Tên đăng nhập (Username, Unique)
    private String matKhau;     // Mật khẩu đã mã hóa BCrypt
    private String email;       // Email (Unique, dùng để reset password)
    private String sdt;         // Số điện thoại (10 số)
    private String gioiTinh;    // Giới tính ("Nam" hoặc "Nữ")
    private String ngaySinh;    // Ngày sinh (yyyy-MM-dd)
    private int maQuyen;        // Mã quyền (FK → QUYEN: 1=Admin, 2=Manager, 3=Staff)

    // Constructors
    public NhanVienDTO() {
    }

    public NhanVienDTO(int maNV, String hoTenNV, String tenDN, String matKhau,
                       String email, String sdt, String gioiTinh, String ngaySinh, int maQuyen) {
        this.maNV = maNV;
        this.hoTenNV = hoTenNV;
        this.tenDN = tenDN;
        this.matKhau = matKhau;
        this.email = email;
        this.sdt = sdt;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.maQuyen = maQuyen;
    }

    // Getters and Setters
    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String hoTenNV) {
        this.hoTenNV = hoTenNV;
    }

    public String getTenDN() {
        return tenDN;
    }

    public void setTenDN(String tenDN) {
        this.tenDN = tenDN;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public int getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(int maQuyen) {
        this.maQuyen = maQuyen;
    }
}
