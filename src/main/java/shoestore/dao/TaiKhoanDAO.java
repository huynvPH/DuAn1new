package shoestore.dao;

import shoestore.entity.TaiKhoan;

import java.sql.SQLException;
import java.util.List;

/**
 * Định nghĩa các thao tác dữ liệu phục vụ đăng nhập và quản lý tài khoản.
 */
public interface TaiKhoanDAO {
    TaiKhoan findByUsername(String username) throws SQLException; // Giải thích: dùng để lấy thông tin và mật khẩu lưu trong DB.

    List<TaiKhoan> findAll() throws SQLException; // Giải thích: hỗ trợ màn CRUD liệt kê toàn bộ tài khoản.

    List<TaiKhoan> search(String keyword) throws SQLException; // Giải thích: tìm nhanh theo tên đăng nhập/mã nhân viên.

    TaiKhoan findById(int id) throws SQLException; // Giải thích: lấy chi tiết tài khoản đang chọn trong bảng.

    TaiKhoan findByEmployeeId(int idNhanVien) throws SQLException; // Giải thích: phục vụ việc bấm chọn nhân viên thì tự động đổ tài khoản.

    TaiKhoan findByEmployeeEmail(String email) throws SQLException; // Giải thích: phục vụ đăng nhập Gmail dựa trên email nhân viên.

    void insert(TaiKhoan taiKhoan) throws SQLException; // Giải thích: thêm mới tài khoản dựa trên dữ liệu form ThemTK.

    void update(TaiKhoan taiKhoan) throws SQLException; // Giải thích: cập nhật tài khoản khi người dùng bấm Sửa.

    void delete(int id) throws SQLException; // Giải thích: xóa bản ghi tương ứng IdTaiKhoan được chọn.

    boolean isUsernameDuplicated(String username, Integer excludeId) throws SQLException; // Giải thích: kiểm tra trùng username khi thêm/sửa.
}
