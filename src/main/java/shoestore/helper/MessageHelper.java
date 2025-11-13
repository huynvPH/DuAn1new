package shoestore.helper;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Tiện ích hiển thị thông báo Swing, giúp UI không phải lặp lại nhiều đoạn JOptionPane.
 */
public final class MessageHelper {

    private MessageHelper() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        // Giải thích: phương thức gom logic hiển thị thông báo dạng thông tin.
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
        // Giải thích: dùng chung khi controller muốn báo lỗi đầu vào hoặc DB.
    }
}
