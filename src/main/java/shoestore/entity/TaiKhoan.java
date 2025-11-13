package shoestore.entity;

/**
 * Thực thể ánh xạ bảng TaiKhoan dùng cho chức năng đăng nhập.
 * Sau đoạn khai báo thuộc tính là phần giải thích giúp sinh viên hiểu ý nghĩa.
 */
public class TaiKhoan {
    private int idTaiKhoan; // Mã chính của tài khoản trong bảng TaiKhoan.
    private String tenDangNhap; // Tên đăng nhập ứng với cột TenDangNhap.
    private String matKhau; // Mật khẩu đã lưu ở CSDL (chưa mã hóa để dễ học).
    private int vaiTro; // 0 = quản lí, 1 = nhân viên; đặt tên khớp cột VaiTro trong bảng TaiKhoan.
    private int trangThaiTaiKhoan; // Dùng để khóa/mở tài khoản (TrangThai bảng TaiKhoan).
    private int trangThaiNhanVien; // Lưu trạng thái nhân viên liên kết nhằm chặn nhân viên bị khóa.

    public int getIdTaiKhoan() {
        return idTaiKhoan; // Giải thích: getter trả về IdTaiKhoan giúp controller dùng sau khi đăng nhập.
    }

    public void setIdTaiKhoan(int idTaiKhoan) {
        this.idTaiKhoan = idTaiKhoan; // Giải thích: setter dùng khi ánh xạ dữ liệu từ JDBC ResultSet.
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

    public int getVaiTro() {
        return vaiTro; // Giải thích: vai trò (VaiTro) xác định phân quyền theo bảng TaiKhoan.
    }

    public void setVaiTro(int vaiTro) {
        this.vaiTro = vaiTro; // Giải thích: setter dùng khi ánh xạ ResultSet trả về cột VaiTro.
    }

    public int getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(int trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public int getTrangThaiNhanVien() {
        return trangThaiNhanVien;
    }

    public void setTrangThaiNhanVien(int trangThaiNhanVien) {
        this.trangThaiNhanVien = trangThaiNhanVien;
    }

    public boolean isManager() {
        return vaiTro == 0; // Giải thích: điều kiện VaiTro == 0 biểu thị tài khoản quản lí.
    }

    public boolean isStaff() {
        return vaiTro == 1; // Giải thích: VaiTro == 1 tương ứng nhân viên bán hàng.
    }
}
