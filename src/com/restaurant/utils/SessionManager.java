package com.restaurant.utils;

import com.restaurant.model.NhanVienDTO;

/**
 * Singleton class quản lý phiên đăng nhập của người dùng
 *
 * Design Pattern: Singleton Pattern
 * - Đảm bảo chỉ có 1 session duy nhất trong toàn bộ ứng dụng
 * - Thread-safe với synchronized method
 *
 * Chức năng:
 * - Lưu thông tin người dùng đang đăng nhập
 * - Quản lý thời gian hoạt động (auto logout sau 30 phút không hoạt động)
 * - Kiểm tra quyền truy cập (Admin/Manager/Staff)
 * - Cập nhật thời gian hoạt động khi người dùng tương tác
 *
 * Security:
 * - Session timeout: 30 phút
 * - Auto logout khi hết thời gian
 * - Kiểm tra session trước mỗi thao tác
 *
 * @author Restaurant Management Team
 * @version 1.0.0
 */
public class SessionManager {
    // Instance duy nhất của class (Singleton pattern)
    private static SessionManager instance;

    // Thông tin người dùng đang đăng nhập
    private NhanVienDTO currentUser;

    // Thời gian hoạt động cuối cùng (milliseconds)
    private long lastActivityTime;

    // Thời gian timeout: 30 phút = 30 * 60 * 1000 milliseconds
    private static final long TIMEOUT_MS = 30 * 60 * 1000;

    /**
     * Private constructor để ngăn việc tạo instance từ bên ngoài
     * (Singleton pattern requirement)
     */
    private SessionManager() {
        // Constructor rỗng - không cần khởi tạo gì
    }

    /**
     * Lấy instance duy nhất của SessionManager (Singleton pattern)
     *
     * Thread-safe: Sử dụng synchronized để đảm bảo chỉ 1 thread
     * có thể tạo instance tại một thời điểm
     *
     * @return Instance duy nhất của SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Đăng nhập người dùng và bắt đầu session
     *
     * Các bước:
     * 1. Lưu thông tin người dùng vào currentUser
     * 2. Ghi nhận thời gian đăng nhập (lastActivityTime)
     *
     * @param user Thông tin người dùng đăng nhập (từ NhanVienDAO)
     */
    public void login(NhanVienDTO user) {
        this.currentUser = user;
        // Lưu thời gian hiện tại (milliseconds từ epoch)
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Đăng xuất người dùng và xóa session
     *
     * Các bước:
     * 1. Xóa thông tin người dùng (set null)
     * 2. Reset thời gian hoạt động về 0
     *
     * Gọi method này khi:
     * - Người dùng click nút Logout
     * - Session timeout (30 phút không hoạt động)
     */
    public void logout() {
        this.currentUser = null;
        this.lastActivityTime = 0;
    }

    /**
     * Kiểm tra người dùng có đang đăng nhập không
     *
     * Điều kiện để session valid:
     * 1. currentUser không null (đã đăng nhập)
     * 2. Chưa quá 30 phút kể từ lần hoạt động cuối
     *
     * @return true nếu đang đăng nhập và session còn hiệu lực, false nếu không
     */
    public boolean isLoggedIn() {
        // Kiểm tra 1: Người dùng đã đăng nhập chưa?
        if (currentUser == null) {
            return false;
        }

        // Kiểm tra 2: Session có bị timeout không?
        // Tính thời gian đã trôi qua = thời gian hiện tại - thời gian hoạt động cuối
        long elapsedTime = System.currentTimeMillis() - lastActivityTime;

        if (elapsedTime > TIMEOUT_MS) {
            // Quá 30 phút không hoạt động → Auto logout
            logout();
            return false;
        }

        // Session còn hiệu lực
        return true;
    }

    /**
     * Cập nhật thời gian hoạt động cuối cùng
     *
     * Gọi method này mỗi khi người dùng:
     * - Click button
     * - Mở dialog
     * - Thực hiện thao tác bất kỳ
     *
     * Mục đích: Reset bộ đếm timeout về 0
     */
    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Lấy thông tin người dùng đang đăng nhập
     *
     * Auto update activity: Mỗi lần gọi method này sẽ tự động
     * cập nhật thời gian hoạt động (reset timeout)
     *
     * @return NhanVienDTO nếu đang đăng nhập, null nếu không
     */
    public NhanVienDTO getCurrentUser() {
        // Kiểm tra session còn hiệu lực không
        if (isLoggedIn()) {
            // Cập nhật thời gian hoạt động (reset timeout)
            updateActivity();
            return currentUser;
        }
        // Session hết hạn hoặc chưa đăng nhập
        return null;
    }

    /**
     * Kiểm tra người dùng hiện tại có phải Admin không
     *
     * Admin có quyền:
     * - Quản lý nhân viên (CRUD, phân quyền)
     * - Xem thống kê toàn bộ hệ thống
     * - Giám sát bàn ăn real-time
     * - Truy cập tất cả chức năng
     *
     * @return true nếu là Admin (maQuyen = 1), false nếu không
     */
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getMaQuyen() == 1;
    }

    /**
     * Kiểm tra người dùng hiện tại có phải Manager không
     *
     * Manager có quyền:
     * - Quản lý thực đơn (CRUD món ăn, loại món)
     * - Xem thống kê doanh thu
     * - Xem lịch sử đơn hàng
     * - Không được quản lý nhân viên
     *
     * @return true nếu là Manager (maQuyen = 2), false nếu không
     */
    public boolean isManager() {
        return isLoggedIn() && currentUser.getMaQuyen() == 2;
    }

    /**
     * Kiểm tra người dùng hiện tại có phải Staff không
     *
     * Staff có quyền:
     * - Quản lý bàn ăn (đặt món, thanh toán)
     * - Xem doanh thu cá nhân
     * - Xem xếp hạng nhân viên
     * - Không được quản lý thực đơn, nhân viên
     *
     * @return true nếu là Staff (maQuyen = 3), false nếu không
     */
    public boolean isStaff() {
        return isLoggedIn() && currentUser.getMaQuyen() == 3;
    }
}
