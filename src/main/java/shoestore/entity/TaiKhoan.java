package shoestore.entity;

/**
 * Thực thể ánh xạ bảng TaiKhoan dùng cho chức năng đăng nhập.
 * Sau đoạn khai báo thuộc tính là phần giải thích giúp sinh viên hiểu ý nghĩa.
 */
public class TaiKhoan {
    private int idTaiKhoan; // Mã chính của tài khoản trong bảng TaiKhoan.
    private String tenDangNhap; // Tên đăng nhập ứng với cột TenDangNhap.
    private String matKhau; // Mật khẩu đã lưu ở CSDL (chưa mã hóa để dễ học).
    private int quyenHan; // 0 = quản lí, 1 = nhân viên; mapping đúng yêu cầu đề bài.
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

    public int getQuyenHan() {
        return quyenHan;
    }

    public void setQuyenHan(int quyenHan) {
        this.quyenHan = quyenHan;
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
        return quyenHan == 0; // Giải thích: điều kiện quyenHan == 0 biểu thị tài khoản quản lí.
    }

    public boolean isStaff() {
        return quyenHan == 1; // Giải thích: quyenHan == 1 tương ứng nhân viên bán hàng.
    }
}
