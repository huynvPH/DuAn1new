package shoestore.dao.impl;

import shoestore.dao.TaiKhoanDAO;
import shoestore.entity.TaiKhoan;
import shoestore.until.XJdbc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt DAO thao tác bảng TaiKhoan + NhanVien để kiểm tra trạng thái tài khoản.
 */
public class TaiKhoanDAOImpl implements TaiKhoanDAO {

    private static final String BASE_SELECT = "SELECT IdTaiKhoan, IdNhanVien, TenDangNhap, MatKhau, VaiTro FROM TaiKhoan";
    private static final String SELECT_BY_USERNAME = BASE_SELECT + " WHERE TenDangNhap = ?"; // Giải thích: tái sử dụng SELECT chung để dễ bảo trì.
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY IdTaiKhoan DESC";
    private static final String SELECT_BY_ID = BASE_SELECT + " WHERE IdTaiKhoan = ?";
    private static final String SEARCH = BASE_SELECT + " WHERE TenDangNhap LIKE ? OR CONVERT(VARCHAR(10), IdNhanVien) LIKE ? ORDER BY TenDangNhap";
    private static final String INSERT = "INSERT INTO TaiKhoan (IdNhanVien, TenDangNhap, MatKhau, VaiTro) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE TaiKhoan SET IdNhanVien = ?, TenDangNhap = ?, MatKhau = ?, VaiTro = ? WHERE IdTaiKhoan = ?";
    private static final String DELETE = "DELETE FROM TaiKhoan WHERE IdTaiKhoan = ?";
    private static final String CHECK_USERNAME = "SELECT COUNT(1) FROM TaiKhoan WHERE TenDangNhap = ?";
    private static final String CHECK_USERNAME_EXCLUDE_ID = "SELECT COUNT(1) FROM TaiKhoan WHERE TenDangNhap = ? AND IdTaiKhoan <> ?";

    @Override
    public TaiKhoan findByUsername(String username) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {
            statement.setString(1, username); // Giải thích: dùng PreparedStatement để chống SQL Injection.
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // Giải thích: gom việc ánh xạ dữ liệu vào một hàm dùng chung cho các truy vấn.
                }
                return null; // Giải thích: username không tồn tại → trả null để controller hiển thị lỗi.
            }
        }
    }

    @Override
    public List<TaiKhoan> findAll() throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {
            return mapToList(rs); // Giải thích: đọc toàn bộ bảng và chuyển sang danh sách cho bảng Swing.
        }
    }

    @Override
    public List<TaiKhoan> search(String keyword) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH)) {
            String pattern = "%" + keyword + "%"; // Giải thích: LIKE %keyword% để tìm linh hoạt.
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                return mapToList(rs);
            }
        }
    }

    @Override
    public TaiKhoan findById(int id) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    @Override
    public void insert(TaiKhoan taiKhoan) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            bindParams(statement, taiKhoan); // Giải thích: gom logic set tham số để dùng chung cho insert/update.
            statement.executeUpdate();
        }
    }

    @Override
    public void update(TaiKhoan taiKhoan) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bindParams(statement, taiKhoan);
            statement.setInt(5, taiKhoan.getIdTaiKhoan()); // Giải thích: tham số cuối là Id để xác định bản ghi cần sửa.
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isUsernameDuplicated(String username, Integer excludeId) throws SQLException {
        if (excludeId == null) {
            try (Connection connection = XJdbc.openConnection();
                 PreparedStatement statement = connection.prepareStatement(CHECK_USERNAME)) {
                statement.setString(1, username);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0; // Giải thích: COUNT > 0 nghĩa là username đã tồn tại.
                }
            }
        } else {
            try (Connection connection = XJdbc.openConnection();
                 PreparedStatement statement = connection.prepareStatement(CHECK_USERNAME_EXCLUDE_ID)) {
                statement.setString(1, username);
                statement.setInt(2, excludeId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        }
    }

    private void bindParams(PreparedStatement statement, TaiKhoan taiKhoan) throws SQLException {
        statement.setInt(1, taiKhoan.getIdNhanVien());
        statement.setString(2, taiKhoan.getTenDangNhap());
        statement.setString(3, taiKhoan.getMatKhau());
        statement.setBoolean(4, taiKhoan.isVaiTro());
    }

    private List<TaiKhoan> mapToList(ResultSet rs) throws SQLException {
        List<TaiKhoan> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list; // Giải thích: trả về danh sách giúp JTable hiển thị "đủ" CRUD.
    }

    private TaiKhoan mapRow(ResultSet rs) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setIdTaiKhoan(rs.getInt("IdTaiKhoan"));
        taiKhoan.setIdNhanVien(rs.getInt("IdNhanVien"));
        taiKhoan.setTenDangNhap(rs.getString("TenDangNhap"));
        taiKhoan.setMatKhau(rs.getString("MatKhau"));
        taiKhoan.setVaiTro(rs.getBoolean("VaiTro"));
        return taiKhoan;
    }
}
