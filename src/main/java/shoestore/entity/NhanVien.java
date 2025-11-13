package shoestore.entity;

/**
 * Thực thể ánh xạ bảng NhanVien theo đúng cấu trúc CSDL GIAYTHETHAO.
 */
public class NhanVien {
    private int idNhanVien; // Giải thích: khóa chính IdNhanVien để biết nhân viên nào đang thao tác.
    private String hoTen; // Giải thích: cột HoTen lưu tên nhân viên hiển thị ngoài giao diện.
    private Integer tuoi; // Giải thích: dùng wrapper Integer để tiện xử lý giá trị null trong DB.
    private boolean gioiTinh; // Giải thích: BIT trong SQL Server tương ứng boolean (true = Nam, false = Nữ).
    private String soDienThoai; // Giải thích: cột SoDienThoai phục vụ tìm kiếm & đăng nhập.
    private String email; // Giải thích: dùng để liên lạc, có thể null.

    public int getIdNhanVien() {
        return idNhanVien;
    }

    public void setIdNhanVien(int idNhanVien) {
        this.idNhanVien = idNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public Integer getTuoi() {
        return tuoi;
    }

    public void setTuoi(Integer tuoi) {
        this.tuoi = tuoi;
    }

    public boolean isGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
