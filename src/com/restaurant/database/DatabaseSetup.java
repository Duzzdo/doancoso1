package com.restaurant.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class khởi tạo database schema và seed dữ liệu ban đầu
 *
 * Chức năng:
 * - Tạo tất cả bảng trong database (7 bảng)
 * - Seed dữ liệu mặc định (roles, tables)
 * - Kiểm tra dữ liệu đã tồn tại trước khi insert (tránh duplicate)
 *
 * Database Schema:
 * 1. QUYEN - Bảng quyền (Admin, Manager, Staff)
 * 2. NHANVIEN - Bảng nhân viên (có FK đến QUYEN)
 * 3. BAN - Bảng bàn ăn (trạng thái trống/đang dùng)
 * 4. LOAIMON - Bảng loại món (có BLOB hình ảnh)
 * 5. MON - Bảng món ăn (có FK đến LOAIMON, có BLOB hình ảnh)
 * 6. DONDAT - Bảng đơn đặt (có FK đến BAN, NHANVIEN)
 * 7. CHITIETDONDAT - Bảng chi tiết đơn (composite PK, FK đến DONDAT, MON)
 *
 * Quy trình khởi tạo:
 * 1. Gọi initialize() khi ứng dụng khởi động (Main.java)
 * 2. createTables() tạo tất cả bảng (IF NOT EXISTS)
 * 3. seedData() insert dữ liệu mặc định nếu chưa có
 *
 * Tại sao dùng IF NOT EXISTS?
 * - Tránh lỗi khi chạy lại ứng dụng (bảng đã tồn tại)
 * - Cho phép upgrade schema trong tương lai
 *
 * Sử dụng trong:
 * - Main.java: Gọi DatabaseSetup.initialize() trước khi show UI
 */
public class DatabaseSetup {

    /**
     * Tạo tất cả bảng trong database
     *
     * Quy trình:
     * 1. Lấy connection từ DatabaseConnection singleton
     * 2. Tạo Statement object
     * 3. Execute CREATE TABLE IF NOT EXISTS cho từng bảng
     * 4. Đảm bảo foreign keys được định nghĩa đúng
     *
     * Lưu ý:
     * - IF NOT EXISTS: Không lỗi nếu bảng đã tồn tại
     * - AUTOINCREMENT: Primary key tự động tăng
     * - FOREIGN KEY: Đảm bảo referential integrity
     * - DEFAULT values: Giá trị mặc định cho một số cột
     *
     * Thứ tự tạo bảng quan trọng:
     * 1. QUYEN (không có FK)
     * 2. NHANVIEN (FK → QUYEN)
     * 3. BAN (không có FK)
     * 4. LOAIMON (không có FK)
     * 5. MON (FK → LOAIMON)
     * 6. DONDAT (FK → BAN, NHANVIEN)
     * 7. CHITIETDONDAT (FK → DONDAT, MON)
     */
    public static void createTables() {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {

            // Bảng QUYEN - Quyền truy cập (Admin, Manager, Staff)
            // MAQUYEN: Primary key tự động tăng
            // TENQUYEN: Tên quyền (UNIQUE để tránh trùng)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS QUYEN (" +
                "MAQUYEN INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TENQUYEN TEXT NOT NULL UNIQUE" +
                ")"
            );

            // Bảng NHANVIEN - Thông tin nhân viên
            // MANV: Primary key tự động tăng
            // TENDN: Username (UNIQUE để tránh trùng)
            // MATKHAU: Password đã hash BCrypt
            // MAQUYEN: Foreign key đến QUYEN (1=Admin, 2=Manager, 3=Staff)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS NHANVIEN (" +
                "MANV INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "HOTENNV TEXT NOT NULL, " +
                "TENDN TEXT UNIQUE NOT NULL, " +
                "MATKHAU TEXT NOT NULL, " +
                "EMAIL TEXT, " +
                "SDT TEXT, " +
                "GIOITINH TEXT, " +
                "NGAYSINH TEXT, " +
                "MAQUYEN INTEGER, " +
                "FOREIGN KEY (MAQUYEN) REFERENCES QUYEN(MAQUYEN)" +
                ")"
            );

            // Bảng BAN - Bàn ăn trong nhà hàng
            // MABAN: Primary key tự động tăng
            // TENBAN: Tên bàn (VD: "Bàn 1", "Bàn 2")
            // TINHTRANG: 'false' = trống, 'true' = đang dùng (default: 'false')
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS BAN (" +
                "MABAN INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TENBAN TEXT NOT NULL, " +
                "TINHTRANG TEXT DEFAULT 'false'" +
                ")"
            );

            // Bảng LOAIMON - Loại món ăn (Khai vị, Món chính, Tráng miệng, Đồ uống)
            // MALOAI: Primary key tự động tăng
            // TENLOAI: Tên loại món
            // HINHANH: Hình ảnh dạng BLOB (byte array)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS LOAIMON (" +
                "MALOAI INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TENLOAI TEXT NOT NULL, " +
                "HINHANH BLOB" +
                ")"
            );

            // Bảng MON - Món ăn
            // MAMON: Primary key tự động tăng
            // TENMON: Tên món
            // GIATIEN: Giá tiền (REAL = double)
            // TINHTRANG: 'true' = còn, 'false' = hết (default: 'true')
            // HINHANH: Hình ảnh dạng BLOB (byte array)
            // MALOAI: Foreign key đến LOAIMON
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS MON (" +
                "MAMON INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TENMON TEXT NOT NULL, " +
                "GIATIEN REAL NOT NULL, " +
                "TINHTRANG TEXT DEFAULT 'true', " +
                "HINHANH BLOB, " +
                "MALOAI INTEGER, " +
                "FOREIGN KEY (MALOAI) REFERENCES LOAIMON(MALOAI)" +
                ")"
            );

            // Bảng DONDAT - Đơn đặt món
            // MADONDAT: Primary key tự động tăng
            // MABAN: Foreign key đến BAN (bàn nào đặt)
            // MANV: Foreign key đến NHANVIEN (nhân viên nào phục vụ)
            // NGAYDAT: Ngày giờ đặt (TEXT format: yyyy-MM-dd HH:mm:ss)
            // TONGTIEN: Tổng tiền đơn hàng (REAL = double)
            // TINHTRANG: 'pending', 'completed', 'cancelled' (default: 'pending')
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS DONDAT (" +
                "MADONDAT INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MABAN INTEGER, " +
                "MANV INTEGER, " +
                "NGAYDAT TEXT, " +
                "TONGTIEN REAL, " +
                "TINHTRANG TEXT DEFAULT 'pending', " +
                "FOREIGN KEY (MABAN) REFERENCES BAN(MABAN), " +
                "FOREIGN KEY (MANV) REFERENCES NHANVIEN(MANV)" +
                ")"
            );

            // Bảng CHITIETDONDAT - Chi tiết đơn đặt (món nào, số lượng bao nhiêu)
            // Composite Primary Key: (MADONDAT, MAMON)
            // MADONDAT: Foreign key đến DONDAT
            // MAMON: Foreign key đến MON
            // SOLUONG: Số lượng món (INTEGER)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS CHITIETDONDAT (" +
                "MADONDAT INTEGER, " +
                "MAMON INTEGER, " +
                "SOLUONG INTEGER NOT NULL, " +
                "PRIMARY KEY (MADONDAT, MAMON), " +
                "FOREIGN KEY (MADONDAT) REFERENCES DONDAT(MADONDAT), " +
                "FOREIGN KEY (MAMON) REFERENCES MON(MAMON)" +
                ")"
            );

            System.out.println("All tables created successfully.");

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seed dữ liệu ban đầu vào database
     *
     * Dữ liệu seed:
     * 1. QUYEN: 3 roles (Admin, Manager, Staff)
     * 2. BAN: 10 bàn ăn (Bàn 1 - Bàn 10, tất cả trống)
     *
     * Tại sao không seed admin account?
     * - Bảo mật: Không có tài khoản mặc định với password cố định
     * - User phải tự đăng ký tài khoản đầu tiên
     * - Tài khoản đầu tiên có thể được set quyền Admin thủ công trong database
     *
     * Kiểm tra trước khi insert:
     * - SELECT COUNT(*) để kiểm tra bảng có dữ liệu chưa
     * - Chỉ insert nếu COUNT = 0 (tránh duplicate)
     */
    public static void seedData() {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {

            // Seed QUYEN - 3 roles cố định
            // MAQUYEN = 1: Admin (full access)
            // MAQUYEN = 2: Manager (quản lý menu, bàn, đơn hàng)
            // MAQUYEN = 3: Staff (chỉ xem và tạo đơn hàng)
            var rsQuyen = stmt.executeQuery("SELECT COUNT(*) FROM QUYEN");
            if (rsQuyen.next() && rsQuyen.getInt(1) == 0) {
                stmt.execute("INSERT INTO QUYEN (TENQUYEN) VALUES ('Admin')");
                stmt.execute("INSERT INTO QUYEN (TENQUYEN) VALUES ('Manager')");
                stmt.execute("INSERT INTO QUYEN (TENQUYEN) VALUES ('Staff')");
                System.out.println("- 3 roles created (Admin, Manager, Staff)");
            } else {
                System.out.println("- Roles already exist");
            }

            // Seed BAN - 10 bàn ăn mặc định
            // Tất cả bàn đều trống (TINHTRANG = 'false')
            // Tên bàn: "Bàn 1", "Bàn 2", ..., "Bàn 10"
            var rsBan = stmt.executeQuery("SELECT COUNT(*) FROM BAN");
            if (rsBan.next() && rsBan.getInt(1) == 0) {
                for (int i = 1; i <= 10; i++) {
                    stmt.execute("INSERT INTO BAN (TENBAN, TINHTRANG) VALUES ('Bàn " + i + "', 'false')");
                }
                System.out.println("- 10 tables created (Bàn 1 - Bàn 10)");
            } else {
                System.out.println("- Tables already exist");
            }

            System.out.println("Database seeding complete.");
            System.out.println("NOTE: No default admin account. Please register through the app.");

        } catch (SQLException e) {
            System.err.println("Error seeding data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo database (tạo bảng + seed dữ liệu)
     *
     * Quy trình:
     * 1. Gọi createTables() - Tạo tất cả bảng
     * 2. Gọi seedData() - Insert dữ liệu mặc định
     *
     * Sử dụng khi:
     * - Ứng dụng khởi động lần đầu (Main.java)
     * - Cần reset database về trạng thái ban đầu
     *
     * Lưu ý:
     * - Method này idempotent (gọi nhiều lần không gây lỗi)
     * - IF NOT EXISTS đảm bảo không tạo lại bảng đã tồn tại
     * - COUNT check đảm bảo không insert duplicate data
     */
    public static void initialize() {
        System.out.println("Initializing database...");
        createTables();
        seedData();
        System.out.println("Database initialization complete.");
    }
}

