package shoestore.entity;

/**
 * Thực thể ánh xạ bảng TaiKhoan dùng cho chức năng đăng nhập.
 * Sau đoạn khai báo thuộc tính là phần giải thích giúp sinh viên hiểu ý nghĩa.
 */
public class TaiKhoan {
    private int idTaiKhoan; // Mã chính của tài khoản trong bảng TaiKhoan.
    private int idNhanVien; // Lưu khóa ngoại IdNhanVien để những màn khác biết nhân viên nào đăng nhập.
    private String tenDangNhap; // Tên đăng nhập ứng với cột TenDangNhap.
    private String matKhau; // Mật khẩu đã lưu ở CSDL (chưa mã hóa để dễ học).
    private boolean vaiTro; // VaiTro kiểu BIT: true = quản lý, false = nhân viên theo script người dùng cung cấp.

    public int getIdTaiKhoan() {
        return idTaiKhoan; // Giải thích: getter trả về IdTaiKhoan giúp controller dùng sau khi đăng nhập.
    }

    public void setIdTaiKhoan(int idTaiKhoan) {
        this.idTaiKhoan = idTaiKhoan; // Giải thích: setter dùng khi ánh xạ dữ liệu từ JDBC ResultSet.
    }

    public int getIdNhanVien() {
        return idNhanVien; // Giải thích: getter này giúp truyền IdNhanVien sang những nghiệp vụ cần ghi nhận nhân viên.
    }

    public void setIdNhanVien(int idNhanVien) {
        this.idNhanVien = idNhanVien; // Giải thích: setter ánh xạ trực tiếp với cột IdNhanVien trong ResultSet.
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public boolean isVaiTro() {
        return vaiTro; 
    }

    public void setVaiTro(boolean vaiTro) {
        this.vaiTro = vaiTro; 
    }

    public boolean isManager() {
        return vaiTro; 
    }

    public boolean isStaff() {
        return !vaiTro; 
    }
}
