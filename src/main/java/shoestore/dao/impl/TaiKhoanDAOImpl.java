package shoestore.dao.impl;

import shoestore.dao.TaiKhoanDAO;
import shoestore.entity.TaiKhoan;
import shoestore.helper.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cài đặt DAO thao tác bảng TaiKhoan + NhanVien để kiểm tra trạng thái tài khoản.
 */
public class TaiKhoanDAOImpl implements TaiKhoanDAO {

    private static final String SELECT_BY_USERNAME =
            "SELECT tk.IdTaiKhoan, tk.TenDangNhap, tk.MatKhau, ISNULL(tk.QuyenHan, 1) AS QuyenHan, "
                    + "tk.TrangThai AS TrangThaiTaiKhoan, ISNULL(nv.TrangThai, 1) AS TrangThaiNhanVien "
                    + "FROM TaiKhoan tk JOIN NhanVien nv ON tk.IdNhanVien = nv.IdNhanVien "
                    + "WHERE tk.TenDangNhap = ?"; // Giải thích: câu SQL lấy đủ thông tin cần thiết trong một lần truy vấn.

    @Override
    public TaiKhoan findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {
            statement.setString(1, username); // Giải thích: dùng PreparedStatement để chống SQL Injection.
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan taiKhoan = new TaiKhoan();
                    taiKhoan.setIdTaiKhoan(rs.getInt("IdTaiKhoan"));
                    taiKhoan.setTenDangNhap(rs.getString("TenDangNhap"));
                    taiKhoan.setMatKhau(rs.getString("MatKhau"));
                    taiKhoan.setQuyenHan(parseRole(rs.getString("QuyenHan")));
                    taiKhoan.setTrangThaiTaiKhoan(rs.getInt("TrangThaiTaiKhoan"));
                    taiKhoan.setTrangThaiNhanVien(rs.getInt("TrangThaiNhanVien"));
                    return taiKhoan; // Giải thích: trả về entity đã map để controller so sánh mật khẩu.
                }
                return null; // Giải thích: username không tồn tại → trả null để controller hiển thị lỗi.
            }
        }
    }

    private int parseRole(String dbValue) {
        try {
            return Integer.parseInt(dbValue); // Giải thích: chuyển chuỗi (0/1) sang số nguyên để tiện kiểm tra quyền.
        } catch (NumberFormatException ex) {
            return 1; // Giải thích: nếu dữ liệu không hợp lệ thì mặc định là nhân viên nhằm đảm bảo an toàn.
        }
    }
}
