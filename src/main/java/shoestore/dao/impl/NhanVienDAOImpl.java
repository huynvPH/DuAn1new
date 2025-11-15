package shoestore.dao.impl;

import shoestore.dao.NhanVienDAO;
import shoestore.entity.NhanVien;
import shoestore.until.XJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt các thao tác JDBC tương ứng bảng NhanVien.
 */
public class NhanVienDAOImpl implements NhanVienDAO {

    private static final String BASE_SELECT = "SELECT IdNhanVien, HoTen, Tuoi, GioiTinh, SoDienThoai, Email FROM NhanVien";
    private static final String SELECT_ALL = BASE_SELECT + " ORDER BY IdNhanVien DESC";
    private static final String SELECT_BY_ID = BASE_SELECT + " WHERE IdNhanVien = ?";
    private static final String SEARCH = BASE_SELECT + " WHERE HoTen LIKE ? OR SoDienThoai LIKE ? ORDER BY HoTen";
    private static final String INSERT = "INSERT INTO NhanVien (HoTen, Tuoi, GioiTinh, SoDienThoai, Email) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE NhanVien SET HoTen = ?, Tuoi = ?, GioiTinh = ?, SoDienThoai = ?, Email = ? WHERE IdNhanVien = ?";
    private static final String DELETE = "DELETE FROM NhanVien WHERE IdNhanVien = ?";

    @Override
    public List<NhanVien> findAll() throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapToList(resultSet); // Giải thích: dùng chung hàm chuyển ResultSet sang List để tránh lặp mã.
        }
    }

    @Override
    public List<NhanVien> searchByKeyword(String keyword) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH)) {
            String pattern = "%" + keyword + "%"; // Giải thích: LIKE %keyword% để tìm cả giữa chuỗi.
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                return mapToList(rs);
            }
        }
    }

    @Override
    public NhanVien findById(int id) throws SQLException {
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
    public int insert(NhanVien nhanVien) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bindCommonParams(statement, nhanVien); // Giải thích: gom logic set tham số chung (trừ Id) giúp code ngắn.
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không thể thêm nhân viên, không có bản ghi nào được chèn");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Giải thích: trả về IdNhanVien mới để controller tạo tài khoản ngay sau đó.
                }
                throw new SQLException("Không đọc được mã nhân viên vừa thêm");
            }
        }
    }

    @Override
    public void update(NhanVien nhanVien) throws SQLException {
        try (Connection connection = XJdbc.openConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bindCommonParams(statement, nhanVien);
            statement.setInt(6, nhanVien.getIdNhanVien()); // Giải thích: tham số cuối là Id để xác định bản ghi cần sửa.
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

    private void bindCommonParams(PreparedStatement statement, NhanVien nhanVien) throws SQLException {
        statement.setString(1, nhanVien.getHoTen());
        if (nhanVien.getTuoi() != null) {
            statement.setInt(2, nhanVien.getTuoi());
        } else {
            statement.setNull(2, Types.INTEGER);
        }
        statement.setBoolean(3, nhanVien.isGioiTinh());
        statement.setString(4, nhanVien.getSoDienThoai());
        statement.setString(5, nhanVien.getEmail());
    }

    private List<NhanVien> mapToList(ResultSet rs) throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setIdNhanVien(rs.getInt("IdNhanVien"));
        nhanVien.setHoTen(rs.getNString("HoTen"));
        Object tuoiObj = rs.getObject("Tuoi");
        nhanVien.setTuoi(tuoiObj == null ? null : ((Number) tuoiObj).intValue());
        nhanVien.setGioiTinh(rs.getBoolean("GioiTinh"));
        nhanVien.setSoDienThoai(rs.getString("SoDienThoai"));
        nhanVien.setEmail(rs.getString("Email"));
        return nhanVien;
    }
}
