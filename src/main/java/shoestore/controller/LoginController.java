package shoestore.controller;

import shoestore.dao.TaiKhoanDAO;
import shoestore.dao.impl.TaiKhoanDAOImpl;
import shoestore.entity.TaiKhoan;
import shoestore.until.AuthHelper;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Controller chịu trách nhiệm xác thực, kiểm tra trạng thái tài khoản và phân quyền.
 */
public class LoginController {

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAOImpl(); // Giải thích: khai báo interface giúp dễ thay thế/mock.

    public TaiKhoan login(String username, char[] password) throws SQLException {
        validateInput(username, password); // Giải thích: đảm bảo dữ liệu đầu vào không bỏ trống.
        TaiKhoan taiKhoan = taiKhoanDAO.findByUsername(username);
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Tên đăng nhập không tồn tại");
        }
        if (!taiKhoan.getMatKhau().equals(new String(password))) {
            throw new IllegalArgumentException("Mật khẩu không chính xác");
        }
        Arrays.fill(password, '\0'); // Giải thích: xóa mật khẩu khỏi bộ nhớ để tránh lộ thông tin.
        AuthHelper.login(taiKhoan); // Giải thích: lưu trạng thái đăng nhập toàn cục cho các màn hình khác.
        return taiKhoan;
    }

    private void validateInput(String username, char[] password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu");
        }
    }
}
