package shoestore.dao;

import shoestore.entity.NhanVien;

import java.sql.SQLException;
import java.util.List;

/**
 * Định nghĩa CRUD cơ bản cho bảng NhanVien.
 */
public interface NhanVienDAO {
    List<NhanVien> findAll() throws SQLException; // Giải thích: lấy toàn bộ nhân viên để hiển thị bảng.

    List<NhanVien> searchByKeyword(String keyword) throws SQLException; // Giải thích: tìm theo tên hoặc SĐT từ ô tìm kiếm.

    NhanVien findById(int id) throws SQLException; // Giải thích: hỗ trợ lấy lại thông tin khi cần đồng bộ giao diện.

    int insert(NhanVien nhanVien) throws SQLException; // Giải thích: trả về Id giúp đồng bộ tạo tài khoản ngay sau khi thêm.

    void update(NhanVien nhanVien) throws SQLException; // Giải thích: cập nhật nhân viên đã chọn.

    void delete(int id) throws SQLException; // Giải thích: xóa nhân viên khỏi hệ thống.
}
