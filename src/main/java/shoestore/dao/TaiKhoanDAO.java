package shoestore.dao;

import shoestore.entity.TaiKhoan;

import java.sql.SQLException;

/**
 * Định nghĩa các thao tác dữ liệu phục vụ đăng nhập.
 */
public interface TaiKhoanDAO {
    TaiKhoan findByUsername(String username) throws SQLException; // Giải thích: dùng để lấy thông tin và mật khẩu lưu trong DB.
}
