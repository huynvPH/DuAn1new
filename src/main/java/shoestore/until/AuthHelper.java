package shoestore.until;

import shoestore.entity.TaiKhoan;

/**
 * Lưu trạng thái người dùng đã đăng nhập và cung cấp hàm kiểm tra phân quyền.
 */
public final class AuthHelper {

    private static TaiKhoan currentUser; // Giải thích: lưu tài khoản đang đăng nhập để các màn hình khác truy cập.

    private AuthHelper() {
    }

    public static void login(TaiKhoan taiKhoan) {
        currentUser = taiKhoan; // Giải thích: gọi sau khi xác thực thành công.
    }

    public static void logout() {
        currentUser = null; // Giải thích: clear thông tin khi người dùng đăng xuất.
    }

    public static TaiKhoan getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null; // Giải thích: tiện để UI kiểm tra đã đăng nhập chưa.
    }

    public static boolean isManager() {
        return isLoggedIn() && currentUser.isManager(); // Giải thích: chỉ true khi đã đăng nhập và có quyền quản lí.
    }
}
