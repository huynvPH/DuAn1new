
package shoestore.ui;

import shoestore.controller.NhanVienController;
import shoestore.entity.TaiKhoan;
import shoestore.until.AuthHelper;
import shoestore.until.MessageHelper;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class NhanVien extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NhanVien.class.getName());
    private final NhanVienController nhanVienController = new NhanVienController(); // Giải thích: controller gói toàn bộ nghiệp vụ/DAO.
    private DefaultTableModel tableModel;
    private List<shoestore.entity.NhanVien> displayedEmployees = new ArrayList<>();
    private Integer selectedNhanVienId; // Giải thích: lưu Id nhân viên đang chọn để thao tác sửa/xóa.

    public NhanVien() {
        initComponents();
        initCustomComponents();
    }

    private void initCustomComponents() {
        setLocationRelativeTo(null); // Giải thích: hiển thị form tại giữa màn hình cho dễ thao tác.
        configureTable();
        configurePersonalTab();
        loadAllEmployees();
        loadLoggedInEmployeeInfo();
        btTimKiem.addActionListener(evt -> handleSearchEmployee());
        txtTimKiem.addActionListener(evt -> handleSearchEmployee());
        jButton2.addActionListener(evt -> handleUpdateEmployee());
        jButton3.addActionListener(evt -> handleDeleteEmployee());
    }

    private void configurePersonalTab() {
        jLabel10.setText("Mã nhân viên:"); // Giải thích: tái sử dụng ô tìm kiếm cũ thành ô hiển thị mã nhân viên.
        jLabel11.setText("Thông tin nhân viên đang đăng nhập");
        btSua.setText("Làm mới");
        btSua.addActionListener(evt -> loadLoggedInEmployeeInfo()); // Giải thích: cho phép cập nhật lại thông tin khi đổi tài khoản.
        txtTimKiem1.setEditable(false);
        txtTen1.setEditable(false);
        txtTuoi1.setEditable(false);
        txtSDT1.setEditable(false);
        txtEmail1.setEditable(false);
        rdoNam1.setEnabled(false);
        rdoNu1.setEnabled(false);
    }

    private void configureTable() {
        tableModel = new DefaultTableModel(new Object[]{"Mã", "Họ tên", "Tuổi", "Giới tính", "Số điện thoại", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Giải thích: khóa không cho chỉnh sửa trực tiếp trên bảng để tránh sai sót.
            }
        };
        jTable1.setModel(tableModel);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                showSelectedEmployee();
            }
        });
    }

    private void loadAllEmployees() {
        try {
            displayedEmployees = nhanVienController.getAllEmployees();
            fillTable(displayedEmployees);
            selectedNhanVienId = null;
            jTable1.clearSelection();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể tải danh sách nhân viên", ex);
            MessageHelper.showError(this, "Không thể kết nối CSDL GIAYTHETHAO để tải nhân viên");
        }
    }

    private void fillTable(List<shoestore.entity.NhanVien> employees) {
        tableModel.setRowCount(0);
        for (shoestore.entity.NhanVien nv : employees) {
            tableModel.addRow(new Object[]{
                    nv.getIdNhanVien(),
                    nv.getHoTen(),
                    nv.getTuoi() == null ? "" : nv.getTuoi(),
                    nv.isGioiTinh() ? "Nam" : "Nữ",
                    nv.getSoDienThoai(),
                    nv.getEmail() == null ? "" : nv.getEmail()
            });
        }
    }

    private void showSelectedEmployee() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedEmployees.size()) {
            selectedNhanVienId = null;
            return;
        }
        shoestore.entity.NhanVien nhanVien = displayedEmployees.get(selectedRow);
        selectedNhanVienId = nhanVien.getIdNhanVien();
        txtTen.setText(nhanVien.getHoTen());
        txtTuoi.setText(nhanVien.getTuoi() == null ? "" : String.valueOf(nhanVien.getTuoi()));
        txtSDT.setText(nhanVien.getSoDienThoai());
        txtEmail.setText(nhanVien.getEmail() == null ? "" : nhanVien.getEmail());
        if (nhanVien.isGioiTinh()) {
            rdoNam.setSelected(true);
        } else {
            rdoNu.setSelected(true);
        }
    }

    private void clearForm() {
        txtTen.setText("");
        txtTuoi.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        buttonGroup1.clearSelection();
        txtTimKiem.setText("");
        selectedNhanVienId = null;
        jTable1.clearSelection();
    }

    private void clearPersonalInfoFields() {
        txtTimKiem1.setText("");
        txtTen1.setText("");
        txtTuoi1.setText("");
        txtSDT1.setText("");
        txtEmail1.setText("");
        rdoNam1.setSelected(false);
        rdoNu1.setSelected(false);
    }

    private void loadLoggedInEmployeeInfo() {
        if (!AuthHelper.isLoggedIn()) {
            jLabel11.setText("Nhân viên: Chưa đăng nhập"); // Giải thích: báo cho sinh viên biết cần đăng nhập trước.
            clearPersonalInfoFields();
            return;
        }
        TaiKhoan currentUser = AuthHelper.getCurrentUser();
        try {
            shoestore.entity.NhanVien nhanVien = nhanVienController.getEmployeeById(currentUser.getIdNhanVien());
            if (nhanVien == null) {
                MessageHelper.showError(this, "Không tìm thấy thông tin nhân viên cho tài khoản hiện tại");
                clearPersonalInfoFields();
                return;
            }
            fillLoggedInEmployee(nhanVien);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể tải thông tin nhân viên đang đăng nhập", ex);
            MessageHelper.showError(this, "Không thể kết nối CSDL GIAYTHETHAO để lấy thông tin nhân viên đang đăng nhập");
            clearPersonalInfoFields();
        }
    }

    private void fillLoggedInEmployee(shoestore.entity.NhanVien nhanVien) {
        txtTimKiem1.setText(String.valueOf(nhanVien.getIdNhanVien()));
        txtTen1.setText(nhanVien.getHoTen());
        txtTuoi1.setText(nhanVien.getTuoi() == null ? "" : String.valueOf(nhanVien.getTuoi()));
        txtSDT1.setText(nhanVien.getSoDienThoai());
        txtEmail1.setText(nhanVien.getEmail() == null ? "" : nhanVien.getEmail());
        if (nhanVien.isGioiTinh()) {
            rdoNam1.setSelected(true);
            rdoNu1.setSelected(false);
        } else {
            rdoNam1.setSelected(false);
            rdoNu1.setSelected(true);
        }
        jLabel11.setText("Nhân viên: " + nhanVien.getHoTen()); // Giải thích: headline thể hiện rõ ai đang đăng nhập.
    }

    public void showPersonalTab() {
        jTabbedPane1.setSelectedIndex(0); // Giải thích: đảm bảo vừa đăng nhập sẽ mở đúng tab thông tin cá nhân.
        loadLoggedInEmployeeInfo();
    }

    private Boolean getSelectedGender() {
        if (rdoNam.isSelected()) {
            return Boolean.TRUE;
        }
        if (rdoNu.isSelected()) {
            return Boolean.FALSE;
        }
        return null; // Giải thích: trả về null khi chưa chọn, controller sẽ báo lỗi giúp sinh viên biết cần tick ô.
    }

    private void handleAddEmployee() {
        try {
            nhanVienController.addEmployee(
                    txtTen.getText(),
                    txtTuoi.getText(),
                    getSelectedGender(),
                    txtSDT.getText(),
                    txtEmail.getText());
            MessageHelper.showInfo(this, "Thêm nhân viên thành công");
            loadAllEmployees();
            clearForm();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi thêm nhân viên", ex);
            MessageHelper.showError(this, "Không thể thêm nhân viên vào CSDL GIAYTHETHAO");
        }
    }

    private void handleUpdateEmployee() {
        if (selectedNhanVienId == null) {
            MessageHelper.showError(this, "Vui lòng chọn nhân viên cần sửa");
            return;
        }
        try {
            nhanVienController.updateEmployee(
                    selectedNhanVienId,
                    txtTen.getText(),
                    txtTuoi.getText(),
                    getSelectedGender(),
                    txtSDT.getText(),
                    txtEmail.getText());
            MessageHelper.showInfo(this, "Cập nhật nhân viên thành công");
            loadAllEmployees();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi cập nhật nhân viên", ex);
            MessageHelper.showError(this, "Không thể cập nhật nhân viên trong CSDL GIAYTHETHAO");
        }
    }

    private void handleDeleteEmployee() {
        if (selectedNhanVienId == null) {
            MessageHelper.showError(this, "Vui lòng chọn nhân viên cần xóa");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            nhanVienController.deleteEmployee(selectedNhanVienId);
            MessageHelper.showInfo(this, "Đã xóa nhân viên");
            loadAllEmployees();
            clearForm();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi xóa nhân viên", ex);
            MessageHelper.showError(this, "Không thể xóa nhân viên khỏi CSDL GIAYTHETHAO");
        }
    }

    private void handleSearchEmployee() {
        try {
            displayedEmployees = nhanVienController.searchEmployees(txtTimKiem.getText());
            fillTable(displayedEmployees);
            selectedNhanVienId = null;
            jTable1.clearSelection();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi tìm kiếm nhân viên", ex);
            MessageHelper.showError(this, "Không thể tìm kiếm trên CSDL GIAYTHETHAO");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        rdoNam1 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        rdoNu1 = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        txtTen1 = new javax.swing.JTextField();
        btSua = new javax.swing.JButton();
        txtTuoi1 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtSDT1 = new javax.swing.JTextField();
        txtTimKiem1 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtEmail1 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        rdoNam = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        rdoNu = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        txtTen = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        txtTuoi = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        btTimKiem = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtSDT = new javax.swing.JTextField();
        txtTimKiem = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txtEmail = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        rdoNam1.setText("Nam");

        jLabel8.setText("Số điện thoại:");

        rdoNu1.setText("Nữ");

        jLabel9.setText("Email:");

        btSua.setText("Sửa");

        jLabel10.setText("Tìm kiếm: ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel11.setText("Nhân viên");

        jLabel12.setText("Tên: ");

        jLabel13.setText("Email:");

        jLabel14.setText("Tuổi:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(294, 294, 294)
                .addComponent(jLabel11)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btSua, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTimKiem1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTen1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTuoi1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(18, 18, 18)
                                .addComponent(txtSDT1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(rdoNam1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(27, 27, 27)
                                        .addComponent(rdoNu1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(38, 38, 38))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtTimKiem1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSDT1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtTen1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtTuoi1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(rdoNam1)
                    .addComponent(rdoNu1))
                .addGap(36, 36, 36)
                .addComponent(btSua)
                .addGap(254, 254, 254))
        );

        jTabbedPane1.addTab("Nhân viên", jPanel1);

        rdoNam.setText("Nam");

        jLabel3.setText("Số điện thoại:");

        rdoNu.setText("Nữ");

        jLabel4.setText("Email:");

        jButton1.setText("Thêm");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Sửa");

        jButton3.setText("Xóa");

        btTimKiem.setText("Tìm kiếm");

        jLabel6.setText("Tìm kiếm: ");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText("Nhân viên");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setText("Tên: ");

        jLabel5.setText("Email:");

        jLabel2.setText("Tuổi:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtTuoi, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(73, 73, 73)
                        .addComponent(txtTen, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoNam, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(59, 59, 59)
                                .addComponent(rdoNu, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(66, 66, 66)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btTimKiem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(131, 131, 131))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 666, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(281, 281, 281)
                        .addComponent(jLabel7)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btTimKiem))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTuoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(rdoNam)
                    .addComponent(rdoNu))
                .addGap(40, 40, 40)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        jTabbedPane1.addTab("Quản lí", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        handleAddEmployee(); // Giải thích: gom toàn bộ nghiệp vụ thêm vào phương thức riêng để dễ bảo trì.
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new NhanVien().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btSua;
    private javax.swing.JButton btTimKiem;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JRadioButton rdoNam;
    private javax.swing.JRadioButton rdoNam1;
    private javax.swing.JRadioButton rdoNu;
    private javax.swing.JRadioButton rdoNu1;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEmail1;
    private javax.swing.JTextField txtSDT;
    private javax.swing.JTextField txtSDT1;
    private javax.swing.JTextField txtTen;
    private javax.swing.JTextField txtTen1;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTimKiem1;
    private javax.swing.JTextField txtTuoi;
    private javax.swing.JTextField txtTuoi1;
    // End of variables declaration//GEN-END:variables
}
