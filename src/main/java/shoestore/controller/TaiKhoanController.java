package shoestore.controller;

import shoestore.dao.TaiKhoanDAO;
import shoestore.dao.impl.TaiKhoanDAOImpl;
import shoestore.entity.TaiKhoan;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controller phụ trách validate dữ liệu và gọi DAO thao tác bảng TaiKhoan.
 */
public class TaiKhoanController {

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAOImpl(); // Giải thích: khai báo qua interface để dễ thay đổi/mock DAO.
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,}$"); // Giải thích: username chỉ gồm chữ, số, _ và tối thiểu 4 ký tự.

    public List<TaiKhoan> getAllAccounts() throws SQLException {
        return taiKhoanDAO.findAll(); // Giải thích: load toàn bộ danh sách để đổ vào bảng.
    }

    public List<TaiKhoan> searchAccounts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAccounts(); // Giải thích: bỏ trống ô tìm kiếm thì hiển thị lại toàn bộ dữ liệu.
        }
        return taiKhoanDAO.search(keyword.trim());
    }

    public void addAccount(String idNhanVienText, String username, char[] password, boolean isManager) throws SQLException {
        TaiKhoan taiKhoan = buildEntity(null, idNhanVienText, username, password, isManager);
        ensureUniqueUsername(taiKhoan.getTenDangNhap(), null);
        taiKhoanDAO.insert(taiKhoan); // Giải thích: sau khi validate xong thì giao DAO lưu xuống SQL Server.
    }

    public void updateAccount(int id, String idNhanVienText, String username, char[] password, boolean isManager) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản cần sửa");
        }
        TaiKhoan taiKhoan = buildEntity(id, idNhanVienText, username, password, isManager);
        ensureUniqueUsername(taiKhoan.getTenDangNhap(), id);
        taiKhoanDAO.update(taiKhoan); // Giải thích: truyền entity đã hợp lệ cho DAO cập nhật dữ liệu.
    }

    public void deleteAccount(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản cần xóa");
        }
        taiKhoanDAO.delete(id); // Giải thích: giao DAO xóa bản ghi dựa trên IdTaiKhoan.
    }

    private TaiKhoan buildEntity(Integer id, String idNhanVienText, String username, char[] password, boolean isManager) {
        if (idNhanVienText == null || idNhanVienText.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã nhân viên");
        }
        int idNhanVien;
        try {
            idNhanVien = Integer.parseInt(idNhanVienText.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã nhân viên phải là số nguyên");
        }
        if (idNhanVien <= 0) {
            throw new IllegalArgumentException("Mã nhân viên phải lớn hơn 0");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập");
        }
        String normalizedUsername = username.trim();
        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ được chứa chữ, số, dấu gạch dưới và tối thiểu 4 ký tự");
        }

        if (password == null || password.length < 4) {
            throw new IllegalArgumentException("Mật khẩu phải từ 4 ký tự trở lên");
        }
        String normalizedPassword = new String(password);
        Arrays.fill(password, '\\0'); // Giải thích: xóa mật khẩu khỏi mảng char để hạn chế lộ thông tin trong bộ nhớ.

        TaiKhoan taiKhoan = new TaiKhoan();
        if (id != null) {
            taiKhoan.setIdTaiKhoan(id);
        }
        taiKhoan.setIdNhanVien(idNhanVien);
        taiKhoan.setTenDangNhap(normalizedUsername);
        taiKhoan.setMatKhau(normalizedPassword);
        taiKhoan.setVaiTro(isManager);
        return taiKhoan;
    }

    private void ensureUniqueUsername(String username, Integer excludeId) throws SQLException {
        if (taiKhoanDAO.isUsernameDuplicated(username, excludeId)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
    }
}
