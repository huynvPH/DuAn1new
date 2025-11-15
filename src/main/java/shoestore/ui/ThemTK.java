package shoestore.ui;

import shoestore.controller.TaiKhoanController;
import shoestore.entity.TaiKhoan;
import shoestore.until.MessageHelper;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Giao diện CRUD tài khoản riêng giúp tách biệt với màn quản lí nhân viên.
 */
public class ThemTK extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(ThemTK.class.getName());
    private final TaiKhoanController taiKhoanController = new TaiKhoanController(); // Giải thích: controller chịu trách nhiệm validate + gọi DAO.
    private DefaultTableModel tableModel;
    private List<TaiKhoan> displayedAccounts = new ArrayList<>();
    private Integer selectedAccountId;

    private JTextField txtIdTaiKhoan;
    private JTextField txtIdNhanVien;
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cboVaiTro;
    private JTextField txtTimKiem;
    private JTable tblTaiKhoan;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;
    private JButton btnTim;

    public ThemTK() {
        initComponents();
        initCustomComponents();
    }

    private void initCustomComponents() {
        setLocationRelativeTo(null); // Giải thích: hiển thị form ở giữa màn hình cho dễ thao tác.
        configureTable();
        loadAllAccounts();
        btnTim.addActionListener(evt -> handleSearch());
        txtTimKiem.addActionListener(evt -> handleSearch());
        btnThem.addActionListener(evt -> handleAddAccount());
        btnSua.addActionListener(evt -> handleUpdateAccount());
        btnXoa.addActionListener(evt -> handleDeleteAccount());
        btnLamMoi.addActionListener(evt -> clearForm());
    }

    private void configureTable() {
        tableModel = new DefaultTableModel(new Object[]{"Mã", "Mã nhân viên", "Tên đăng nhập", "Vai trò"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Giải thích: khóa chỉnh sửa trực tiếp để tránh làm sai lệch dữ liệu.
            }
        };
        tblTaiKhoan.setModel(tableModel);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                showSelectedAccount();
            }
        });
    }

    private void loadAllAccounts() {
        try {
            displayedAccounts = taiKhoanController.getAllAccounts();
            fillTable(displayedAccounts);
            selectedAccountId = null;
            tblTaiKhoan.clearSelection();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể tải danh sách tài khoản", ex);
            MessageHelper.showError(this, "Không thể kết nối CSDL GIAYTHETHAO để tải tài khoản");
        }
    }

    private void handleSearch() {
        String keyword = txtTimKiem.getText();
        try {
            displayedAccounts = taiKhoanController.searchAccounts(keyword);
            fillTable(displayedAccounts);
            selectedAccountId = null;
            tblTaiKhoan.clearSelection();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi tìm kiếm tài khoản", ex);
            MessageHelper.showError(this, "Không thể tìm kiếm tài khoản trong CSDL GIAYTHETHAO");
        }
    }

    private void fillTable(List<TaiKhoan> accounts) {
        tableModel.setRowCount(0);
        for (TaiKhoan taiKhoan : accounts) {
            tableModel.addRow(new Object[]{
                    taiKhoan.getIdTaiKhoan(),
                    taiKhoan.getIdNhanVien(),
                    taiKhoan.getTenDangNhap(),
                    taiKhoan.isVaiTro() ? "Quản lý" : "Nhân viên"
            });
        }
    }

    private void showSelectedAccount() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedAccounts.size()) {
            selectedAccountId = null;
            return;
        }
        TaiKhoan taiKhoan = displayedAccounts.get(selectedRow);
        selectedAccountId = taiKhoan.getIdTaiKhoan();
        txtIdTaiKhoan.setText(String.valueOf(taiKhoan.getIdTaiKhoan()));
        txtIdNhanVien.setText(String.valueOf(taiKhoan.getIdNhanVien()));
        txtTenDangNhap.setText(taiKhoan.getTenDangNhap());
        txtMatKhau.setText(taiKhoan.getMatKhau());
        cboVaiTro.setSelectedIndex(taiKhoan.isVaiTro() ? 1 : 0); // Giải thích: 1 = Quản lý, 0 = Nhân viên theo thứ tự combo.
    }

    private void handleAddAccount() {
        try {
            taiKhoanController.addAccount(
                    txtIdNhanVien.getText(),
                    txtTenDangNhap.getText(),
                    txtMatKhau.getPassword(),
                    isManagerSelected());
            MessageHelper.showInfo(this, "Thêm tài khoản thành công");
            loadAllAccounts();
            clearForm();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi thêm tài khoản", ex);
            MessageHelper.showError(this, "Không thể thêm tài khoản vào CSDL GIAYTHETHAO");
        }
    }

    private void handleUpdateAccount() {
        if (selectedAccountId == null) {
            MessageHelper.showError(this, "Vui lòng chọn tài khoản cần sửa");
            return;
        }
        try {
            taiKhoanController.updateAccount(
                    selectedAccountId,
                    txtIdNhanVien.getText(),
                    txtTenDangNhap.getText(),
                    txtMatKhau.getPassword(),
                    isManagerSelected());
            MessageHelper.showInfo(this, "Cập nhật tài khoản thành công");
            loadAllAccounts();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi cập nhật tài khoản", ex);
            MessageHelper.showError(this, "Không thể cập nhật tài khoản trong CSDL GIAYTHETHAO");
        }
    }

    private void handleDeleteAccount() {
        if (selectedAccountId == null) {
            MessageHelper.showError(this, "Vui lòng chọn tài khoản cần xóa");
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tài khoản này?", "Xác nhận", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            taiKhoanController.deleteAccount(selectedAccountId);
            MessageHelper.showInfo(this, "Đã xóa tài khoản");
            loadAllAccounts();
            clearForm();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi xóa tài khoản", ex);
            MessageHelper.showError(this, "Không thể xóa tài khoản khỏi CSDL GIAYTHETHAO");
        }
    }

    private boolean isManagerSelected() {
        return cboVaiTro.getSelectedIndex() == 1; // Giải thích: quy ước index 1 là Quản lý, index 0 là Nhân viên.
    }

    private void clearForm() {
        txtIdTaiKhoan.setText("");
        txtIdNhanVien.setText("");
        txtTenDangNhap.setText("");
        txtMatKhau.setText("");
        cboVaiTro.setSelectedIndex(0);
        selectedAccountId = null;
        tblTaiKhoan.clearSelection();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Quản lý tài khoản");

        JPanel contentPanel = new JPanel();
        JLabel lblTitle = new JLabel("Quản lý tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblId = new JLabel("Mã tài khoản:");
        txtIdTaiKhoan = new JTextField();
        txtIdTaiKhoan.setEditable(false); // Giải thích: Id tự tăng nên chỉ hiển thị.

        JLabel lblIdNhanVien = new JLabel("Mã nhân viên:");
        txtIdNhanVien = new JTextField();

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        txtTenDangNhap = new JTextField();

        JLabel lblPassword = new JLabel("Mật khẩu:");
        txtMatKhau = new JPasswordField();

        JLabel lblRole = new JLabel("Vai trò:");
        cboVaiTro = new JComboBox<>(new String[]{"Nhân viên", "Quản lý"});

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        txtTimKiem = new JTextField();
        btnTim = new JButton("Tìm");

        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm mới");

        tblTaiKhoan = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);

        javax.swing.GroupLayout contentLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentLayout);
        contentLayout.setHorizontalGroup(
                contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(contentLayout.createSequentialGroup()
                                .addGap(20)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane)
                                        .addGroup(contentLayout.createSequentialGroup()
                                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(lblId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblIdNhanVien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(contentLayout.createSequentialGroup()
                                                                .addComponent(txtTimKiem)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(btnTim))
                                                        .addComponent(txtIdTaiKhoan)
                                                        .addComponent(txtIdNhanVien)
                                                        .addComponent(txtTenDangNhap)
                                                        .addComponent(txtMatKhau)
                                                        .addComponent(cboVaiTro, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnSua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnXoa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnLamMoi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(lblTitle))
                                .addGap(20))
        );
        contentLayout.setVerticalGroup(
                contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(contentLayout.createSequentialGroup()
                                .addGap(20)
                                .addComponent(lblTitle)
                                .addGap(20)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblSearch)
                                        .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnTim))
                                .addGap(18)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblId)
                                        .addComponent(txtIdTaiKhoan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnThem))
                                .addGap(18)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblIdNhanVien)
                                        .addComponent(txtIdNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnSua))
                                .addGap(18)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblUsername)
                                        .addComponent(txtTenDangNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnXoa))
                                .addGap(18)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPassword)
                                        .addComponent(txtMatKhau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnLamMoi))
                                .addGap(18)
                                .addGroup(contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblRole)
                                        .addComponent(cboVaiTro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18)
                                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20))
        );

        getContentPane().add(contentPanel);
        pack();
    }
}
