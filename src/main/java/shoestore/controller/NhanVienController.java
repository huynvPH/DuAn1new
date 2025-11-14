package shoestore.controller;

import shoestore.dao.NhanVienDAO;
import shoestore.dao.impl.NhanVienDAOImpl;
import shoestore.entity.NhanVien;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controller gom toàn bộ nghiệp vụ validate + gọi DAO cho màn quản lý nhân viên.
 */
public class NhanVienController {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAOImpl(); // Giải thích: khai báo qua interface giúp dễ mở rộng/test.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$"); // Giải thích: áp dụng SĐT Việt 10 số.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    public List<NhanVien> getAllEmployees() throws SQLException {
        return nhanVienDAO.findAll();
    }

    public List<NhanVien> searchEmployees(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees(); // Giải thích: bỏ trống nghĩa là trả về toàn bộ danh sách.
        }
        return nhanVienDAO.searchByKeyword(keyword.trim());
    }

    public NhanVien getEmployeeById(int id) throws SQLException {
        return nhanVienDAO.findById(id); // Giải thích: hỗ trợ lấy thông tin nhân viên dựa trên Id từ tài khoản đăng nhập.
    }

    public void addEmployee(String hoTen, String tuoiText, Boolean gioiTinh, String soDienThoai, String email) throws SQLException {
        NhanVien nhanVien = buildEntity(null, hoTen, tuoiText, gioiTinh, soDienThoai, email);
        nhanVienDAO.insert(nhanVien);
    }

    public void updateEmployee(int id, String hoTen, String tuoiText, Boolean gioiTinh, String soDienThoai, String email) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên cần sửa");
        }
        NhanVien nhanVien = buildEntity(id, hoTen, tuoiText, gioiTinh, soDienThoai, email);
        nhanVienDAO.update(nhanVien);
    }

    public void deleteEmployee(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên cần xóa");
        }
        nhanVienDAO.delete(id);
    }

    private NhanVien buildEntity(Integer id, String hoTen, String tuoiText, Boolean gioiTinh, String soDienThoai, String email) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên nhân viên");
        }
        Integer tuoi = null;
        if (tuoiText != null && !tuoiText.trim().isEmpty()) {
            try {
                tuoi = Integer.parseInt(tuoiText.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Tuổi phải là số nguyên");
            }
            if (tuoi < 18 || tuoi > 65) {
                throw new IllegalArgumentException("Tuổi phải nằm trong khoảng 18 - 65");
            }
        }
        if (gioiTinh == null) {
            throw new IllegalArgumentException("Vui lòng chọn giới tính");
        }
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại");
        }
        if (!PHONE_PATTERN.matcher(soDienThoai.trim()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 số và bắt đầu bằng 0");
        }
        String normalizedEmail = (email == null || email.trim().isEmpty()) ? null : email.trim();
        if (normalizedEmail != null && !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng");
        }
        NhanVien nhanVien = new NhanVien();
        if (id != null) {
            nhanVien.setIdNhanVien(id);
        }
        nhanVien.setHoTen(hoTen.trim());
        nhanVien.setTuoi(tuoi);
        nhanVien.setGioiTinh(gioiTinh);
        nhanVien.setSoDienThoai(soDienThoai.trim());
        nhanVien.setEmail(normalizedEmail);
        return nhanVien;
    }
}
