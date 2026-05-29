package com.restaurant.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class quản lý kết nối database SQLite
 *
 * Design Pattern: Singleton Pattern
 * - Đảm bảo chỉ có 1 instance duy nhất trong toàn bộ ứng dụng
 * - Thread-safe với synchronized method
 * - Lazy initialization (tạo instance khi cần)
 *
 * Chức năng:
 * - Tạo và quản lý connection đến SQLite database
 * - Tự động tạo thư mục database nếu chưa tồn tại
 * - Enable foreign key constraints
 * - Kiểm tra và tái tạo connection nếu bị đóng
 *
 * Database location: resources/database/restaurant.db
 *
 * @author Restaurant Management Team
 * @version 1.0.0
 */
public class DatabaseConnection {
    // Instance duy nhất của class (Singleton pattern)
    private static DatabaseConnection instance;

    // Connection object đến SQLite database
    private Connection connection;

    // Đường dẫn tuyệt đối đến file database
    private static final String DB_PATH = getAbsoluteDatabasePath();

    // JDBC URL cho SQLite
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    /**
     * Lấy đường dẫn tuyệt đối đến file database
     *
     * Cấu trúc thư mục:
     * - user.dir (thư mục chạy ứng dụng)
     *   └── resources/
     *       └── database/
     *           └── restaurant.db
     *
     * @return Đường dẫn tuyệt đối đến restaurant.db
     */
    private static String getAbsoluteDatabasePath() {
        // Lấy thư mục hiện tại nơi ứng dụng đang chạy
        String userDir = System.getProperty("user.dir");

        // Tạo đường dẫn: user.dir/resources/database/restaurant.db
        String dbPath = userDir + File.separator + "resources" + File.separator + "database" + File.separator + "restaurant.db";

        // Tạo File object để kiểm tra và tạo thư mục
        File dbFile = new File(dbPath);
        File dbDir = dbFile.getParentFile(); // Lấy thư mục cha (resources/database)

        // Tạo thư mục nếu chưa tồn tại (mkdirs tạo cả thư mục cha)
        if (!dbDir.exists()) {
            dbDir.mkdirs();
            System.out.println("Created database directory: " + dbDir.getAbsolutePath());
        }

        System.out.println("Database path: " + dbFile.getAbsolutePath());
        return dbFile.getAbsolutePath();
    }

    /**
     * Private constructor để ngăn việc tạo instance từ bên ngoài
     * (Singleton pattern requirement)
     *
     * Các bước khởi tạo:
     * 1. Load SQLite JDBC driver
     * 2. Tạo connection đến database
     * 3. Enable foreign key constraints (mặc định SQLite tắt)
     */
    private DatabaseConnection() {
        try {
            // Bước 1: Load SQLite JDBC driver vào memory
            // Driver này cần thiết để DriverManager có thể tạo connection
            Class.forName("org.sqlite.JDBC");

            // Bước 2: Tạo connection đến database
            // Nếu file restaurant.db chưa tồn tại, SQLite sẽ tự động tạo file mới
            connection = DriverManager.getConnection(DB_URL);

            // Bước 3: Enable foreign key constraints
            // SQLite mặc định TẮT foreign keys, phải bật thủ công
            // Điều này đảm bảo tính toàn vẹn dữ liệu (referential integrity)
            connection.createStatement().execute("PRAGMA foreign_keys = ON");

            System.out.println("Database connection established successfully.");

        } catch (ClassNotFoundException e) {
            // Lỗi: Không tìm thấy SQLite JDBC driver trong classpath
            // Kiểm tra: lib/sqlite-jdbc-3.44.1.0.jar có được thêm vào classpath không?
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();

        } catch (SQLException e) {
            // Lỗi: Không thể kết nối đến database
            // Nguyên nhân có thể: file bị lock, quyền truy cập, đường dẫn sai
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất của DatabaseConnection (Singleton pattern)
     *
     * Thread-safe: Sử dụng synchronized để đảm bảo chỉ 1 thread
     * có thể tạo instance tại một thời điểm
     *
     * Lazy initialization: Instance chỉ được tạo khi method này được gọi lần đầu
     *
     * @return Instance duy nhất của DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        // Kiểm tra nếu instance chưa tồn tại HOẶC connection không còn valid
        if (instance == null || !isConnectionValid()) {
            // Tạo instance mới
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Lấy Connection object để thực hiện các thao tác database
     *
     * Auto-reconnect: Nếu connection bị đóng, tự động tạo connection mới
     *
     * @return Connection object đến SQLite database
     */
    public Connection getConnection() {
        try {
            // Kiểm tra nếu connection null hoặc đã bị đóng
            if (connection == null || connection.isClosed()) {
                // Tạo connection mới
                connection = DriverManager.getConnection(DB_URL);

                // Enable foreign keys lại (mỗi connection mới cần enable lại)
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Kiểm tra connection có còn valid không
     *
     * Valid nghĩa là:
     * - Instance tồn tại
     * - Connection không null
     * - Connection chưa bị đóng
     * - Connection còn hoạt động (timeout 2 giây)
     *
     * @return true nếu connection còn valid, false nếu không
     */
    private static boolean isConnectionValid() {
        try {
            return instance != null &&
                   instance.connection != null &&
                   !instance.connection.isClosed() &&
                   instance.connection.isValid(2); // Timeout 2 giây
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đóng connection đến database
     *
     * Nên gọi method này khi:
     * - Ứng dụng đang shutdown
     * - Cần giải phóng tài nguyên
     *
     * Note: Sau khi đóng, gọi getInstance() sẽ tạo connection mới
     */
    public void closeConnection() {
        try {
            // Kiểm tra connection tồn tại và chưa bị đóng
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
