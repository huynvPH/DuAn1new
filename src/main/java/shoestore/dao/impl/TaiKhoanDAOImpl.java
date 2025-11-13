package shoestore.dao.impl;

import shoestore.dao.TaiKhoanDAO;
import shoestore.entity.TaiKhoan;
import shoestore.until.XJdbc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cài đặt DAO thao tác bảng TaiKhoan + NhanVien để kiểm tra trạng thái tài khoản.
 */
public class TaiKhoanDAOImpl implements TaiKhoanDAO {

    private static final String SELECT_BY_USERNAME =
            "SELECT IdTaiKhoan, IdNhanVien, TenDangNhap, MatKhau, VaiTro "
                    + "FROM TaiKhoan WHERE TenDangNhap = ?"; // Giải thích: script mới chỉ còn các cột này nên truy vấn đơn giản hơn.

    @Override
    public TaiKhoan findByUsername(String username) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {
            statement.setString(1, username); // Giải thích: dùng PreparedStatement để chống SQL Injection.
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan taiKhoan = new TaiKhoan();
                    taiKhoan.setIdTaiKhoan(rs.getInt("IdTaiKhoan"));
                    taiKhoan.setIdNhanVien(rs.getInt("IdNhanVien")); // Giải thích: giúp controller biết nhân viên nào gắn với tài khoản.
                    taiKhoan.setTenDangNhap(rs.getString("TenDangNhap"));
                    taiKhoan.setMatKhau(rs.getString("MatKhau"));
                    taiKhoan.setVaiTro(rs.getBoolean("VaiTro")); // Giải thích: getBoolean ánh xạ trực tiếp bit 0/1.
                    return taiKhoan; // Giải thích: trả về entity đã map để controller so sánh mật khẩu.
                }
                return null; // Giải thích: username không tồn tại → trả null để controller hiển thị lỗi.
            }
        }
    }
}
