package shoestore.ui;

import shoestore.controller.LoginController;
import shoestore.entity.TaiKhoan;
import shoestore.helper.MessageHelper;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

/**
 * JFrame hiển thị giao diện đăng nhập đơn giản bằng Swing như yêu cầu.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(20); // Giải thích: ô nhập tên đăng nhập.
    private final JPasswordField txtPassword = new JPasswordField(20); // Giải thích: ô nhập mật khẩu che ký tự.
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final LoginController loginController = new LoginController();

    public LoginFrame() {
        initUI();
        initEvents();
    }

    private void initUI() {
        setTitle("Shoes Store - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        var formPanel = new javax.swing.JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.add(new JLabel("Tên đăng nhập:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Mật khẩu:"));
        formPanel.add(txtPassword);

        var container = new javax.swing.JPanel(new BorderLayout(0, 15));
        container.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        container.add(formPanel, BorderLayout.CENTER);
        container.add(btnLogin, BorderLayout.SOUTH);

        setContentPane(container);
        pack();
        // Giải thích: phương thức initUI tạo layout rõ ràng, dễ đọc cho sinh viên.
    }

    private void initEvents() {
        btnLogin.addActionListener(evt -> handleLogin());
        getRootPane().setDefaultButton(btnLogin);
        // Giải thích: tách sự kiện ra method riêng giúp dễ bảo trì và unit test controller.
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();
        try {
            TaiKhoan taiKhoan = loginController.login(username, password);
            if (taiKhoan.isManager()) {
                MessageHelper.showInfo(this, "Đăng nhập thành công - Xin chào Quản lí " + taiKhoan.getTenDangNhap());
            } else {
                MessageHelper.showInfo(this, "Đăng nhập thành công - Xin chào Nhân viên " + taiKhoan.getTenDangNhap());
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            MessageHelper.showError(this, "Lỗi kết nối CSDL: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        // Giải thích: main dùng để chạy nhanh module đăng nhập độc lập với các màn hình khác.
    }
}
