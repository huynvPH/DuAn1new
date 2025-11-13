package shoestore.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp tiện ích cung cấp kết nối JDBC SQL Server cho toàn bộ ứng dụng.
 * Ghi chú cấu hình ngay trong mã giúp sinh viên dễ thay đổi theo môi trường.
 */
public final class DatabaseHelper {

    private static final String JDBC_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=GIAYTHETHAO;encrypt=false"; // URL mặc định cho SQL Server local.
    private static final String USER = "sa"; // Chỉnh lại user SQL Server thực tế của bạn.
    private static final String PASSWORD = "yourStrong(!)Password"; // Thay bằng mật khẩu thật khi triển khai.

    private DatabaseHelper() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // Giải thích: đảm bảo driver được nạp trước khi kết nối.
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Không tìm thấy SQLServerDriver trong classpath", ex); // Giải thích: ném SQLException để caller xử lý thống nhất.
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD); // Giải thích: trả về Connection đã mở cho DAO sử dụng.
    }
}
