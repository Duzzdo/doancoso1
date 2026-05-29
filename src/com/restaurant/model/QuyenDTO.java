package com.restaurant.model;

/**
 * DTO (Data Transfer Object) class đại diện cho bảng QUYEN trong database
 *
 * Chức năng:
 * - Lưu trữ thông tin quyền hạn trong hệ thống
 * - Phân quyền truy cập các chức năng theo vai trò
 *
 * Business Rules:
 * - Quyền cố định, không cho phép thêm/sửa/xóa
 * - MAQUYEN = 1: Admin (toàn quyền, quản lý nhân viên, xem tất cả thống kê)
 * - MAQUYEN = 2: Manager (quản lý thực đơn, xem thống kê, không quản lý nhân viên)
 * - MAQUYEN = 3: Staff (quản lý bàn, đặt món, thanh toán, xem doanh thu cá nhân)
 *
 * Phân quyền chức năng:
 * - Admin: Tất cả panels (Home, Table, Menu, Payment, Staff, Statistics)
 * - Manager: Home, Table, Menu, Payment, Statistics (không có Staff Management)
 * - Staff: Home, Table, Payment, Staff Revenue (chỉ xem doanh thu của mình)
 *
 * Sử dụng trong:
 * - MainFrame: Hiển thị/ẩn menu theo quyền
 * - StaffManagementPanel: JComboBox chọn quyền khi thêm/sửa nhân viên
 * - SessionManager: Kiểm tra quyền của user đang đăng nhập
 */
public class QuyenDTO {
    private int maQuyen;        // Mã quyền (1=Admin, 2=Manager, 3=Staff)
    private String tenQuyen;    // Tên quyền hiển thị (VD: "Admin", "Manager", "Staff")

    // Constructors
    public QuyenDTO() {
    }

    public QuyenDTO(int maQuyen, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen;
    }

    // Getters and Setters
    public int getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(int maQuyen) {
        this.maQuyen = maQuyen;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }

    @Override
    public String toString() {
        return tenQuyen;
    }
}
