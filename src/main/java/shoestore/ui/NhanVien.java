
package shoestore.ui;

import shoestore.controller.NhanVienController;
import shoestore.controller.TaiKhoanController;
import shoestore.entity.TaiKhoan;
import shoestore.until.AuthHelper;
import shoestore.until.MessageHelper;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class NhanVien extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NhanVien.class.getName());
    private final NhanVienController nhanVienController = new NhanVienController(); // Giải thích: controller gói toàn bộ nghiệp vụ/DAO.
    private final TaiKhoanController taiKhoanController = new TaiKhoanController(); // Giải thích: controller chuyên xử lý validate + cập nhật tài khoản.
    private DefaultTableModel tableModel;
    private List<shoestore.entity.NhanVien> displayedEmployees = new ArrayList<>();
    private Integer selectedNhanVienId; // Giải thích: lưu Id nhân viên đang chọn để thao tác sửa/xóa.
    private Integer selectedTaiKhoanIdQL; // Giải thích: nhớ Id tài khoản của nhân viên được chọn để sửa/xóa cùng lúc.

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
        btSua.addActionListener(evt -> handleUpdateLoggedInEmployee()); // Giải thích: bấm nút sẽ cập nhật thông tin cá nhân lên CSDL.
        txtTimKiem1.setEditable(false);
        setPersonalFieldsEditable(false); // Giải thích: mặc định khóa ô nhập cho đến khi đăng nhập thành công.
        btSua.setEnabled(false); // Giải thích: tránh thao tác nhầm khi chưa có tài khoản đăng nhập.
        BtSuaTK.addActionListener(evt -> handleUpdateAccount()); // Giải thích: gom sự kiện nút sửa tài khoản để dễ tái sử dụng logic xử lý.
        setAccountFieldsEditable(false); // Giải thích: khóa các trường tài khoản cho đến khi xác định người đăng nhập.
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
            clearManagementAccountFields(); // Giải thích: khi làm mới bảng thì cũng xóa thông tin tài khoản đang hiển thị để tránh nhầm.
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
            clearManagementAccountFields(); // Giải thích: bỏ chọn bảng thì xóa tài khoản để tránh sửa sai đối tượng.
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
        loadAccountForSelectedEmployee(nhanVien.getIdNhanVien()); // Giải thích: đồng thời tải tài khoản để các nút thêm/sửa/xóa dùng ngay.
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
        clearManagementAccountFields(); // Giải thích: xóa ô tài khoản quản lí khi reset form thêm/sửa.
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

    private void clearAccountFields() {
        txtTaiKhoan.setText("");
        txtMatKhau.setText("");
    }

    private void clearManagementAccountFields() {
        txtTaiKhoanQL.setText("");
        txtMatKhauQL.setText("");
        selectedTaiKhoanIdQL = null; // Giải thích: reset Id tài khoản để tránh cập nhật nhầm người khác.
    }

    private void setPersonalFieldsEditable(boolean editable) {
        txtTen1.setEditable(editable); // Giải thích: bật/tắt cho phép nhập lại họ tên.
        txtTuoi1.setEditable(editable); // Giải thích: chỉ khi đăng nhập mới cho sửa tuổi.
        txtSDT1.setEditable(editable); // Giải thích: khóa/mở ô số điện thoại tương ứng.
        txtEmail1.setEditable(editable); // Giải thích: bảo vệ email khi chưa đăng nhập.
        rdoNam1.setEnabled(editable); // Giải thích: khóa radio Nam khi không cần tương tác.
        rdoNu1.setEnabled(editable); // Giải thích: tương tự với radio Nữ.
    }

    private void setAccountFieldsEditable(boolean editable) {
        txtTaiKhoan.setEditable(editable); // Giải thích: khóa/mở input tên đăng nhập tương ứng trạng thái đăng nhập.
        txtMatKhau.setEditable(editable); // Giải thích: chỉ cho phép sửa mật khẩu khi đã xác thực.
        BtSuaTK.setEnabled(editable); // Giải thích: disable nút tránh click khi chưa đăng nhập.
    }

    private void loadLoggedInEmployeeInfo() {
        if (!AuthHelper.isLoggedIn()) {
            jLabel11.setText("Nhân viên: Chưa đăng nhập"); // Giải thích: báo cho sinh viên biết cần đăng nhập trước.
            clearPersonalInfoFields();
            setPersonalFieldsEditable(false); // Giải thích: không cho nhập liệu khi chưa xác định nhân viên.
            btSua.setEnabled(false); // Giải thích: tránh bấm nhầm nút Sửa khi chưa đăng nhập.
            clearAccountFields();
            setAccountFieldsEditable(false); // Giải thích: khóa khu vực chỉnh sửa tài khoản khi chưa đăng nhập.
            updateManagementTabAccess(); // Giải thích: nhân viên chưa đăng nhập cũng không được truy cập tab quản lí.
            return;
        }
        TaiKhoan currentUser = AuthHelper.getCurrentUser();
        fillLoggedInAccount(currentUser); // Giải thích: đồng bộ lại username/mật khẩu lên giao diện.
        setAccountFieldsEditable(true); // Giải thích: cho phép sửa tài khoản khi đã xác định người đăng nhập.
        try {
            shoestore.entity.NhanVien nhanVien = nhanVienController.getEmployeeById(currentUser.getIdNhanVien());
            if (nhanVien == null) {
                MessageHelper.showError(this, "Không tìm thấy thông tin nhân viên cho tài khoản hiện tại");
                clearPersonalInfoFields();
                setPersonalFieldsEditable(false); // Giải thích: giữ an toàn dữ liệu khi không ánh xạ được nhân viên.
                btSua.setEnabled(false); // Giải thích: khóa nút khi không có dữ liệu để sửa.
                setAccountFieldsEditable(false); // Giải thích: tránh sửa tài khoản khi dữ liệu nhân viên bị lỗi đồng bộ.
                updateManagementTabAccess();
                return;
            }
            fillLoggedInEmployee(nhanVien);
            setPersonalFieldsEditable(true); // Giải thích: sau khi load thành công thì cho phép chỉnh sửa các ô dữ liệu.
            btSua.setEnabled(true); // Giải thích: bật nút Sửa để nhân viên lưu thay đổi.
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể tải thông tin nhân viên đang đăng nhập", ex);
            MessageHelper.showError(this, "Không thể kết nối CSDL GIAYTHETHAO để lấy thông tin nhân viên đang đăng nhập");
            clearPersonalInfoFields();
            setPersonalFieldsEditable(false);
            btSua.setEnabled(false);
            clearAccountFields();
            setAccountFieldsEditable(false);
            updateManagementTabAccess();
            return;
        }
        updateManagementTabAccess();
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

    private void fillLoggedInAccount(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            clearAccountFields();
            return; // Giải thích: không có dữ liệu thì xóa trắng để tránh nhầm lẫn.
        }
        txtTaiKhoan.setText(taiKhoan.getTenDangNhap());
        txtMatKhau.setText(taiKhoan.getMatKhau());
    }

    public void showPersonalTab() {
        jTabbedPane1.setSelectedIndex(0); // Giải thích: đảm bảo vừa đăng nhập sẽ mở đúng tab thông tin cá nhân.
        loadLoggedInEmployeeInfo();
    }

    public void showManagementTab() {
        if (AuthHelper.isManager()) {
            jTabbedPane1.setSelectedIndex(1); // Giải thích: chuyển sang tab quản lí khi tài khoản có quyền.
            loadLoggedInEmployeeInfo(); // Giải thích: refresh lại thông tin cá nhân/phân quyền ngay khi hiển thị tab.
        } else {
            showPersonalTab(); // Giải thích: nếu không đủ quyền thì quay về tab cá nhân để tránh lỗi giao diện.
        }
    }

    private void updateManagementTabAccess() {
        boolean canManage = AuthHelper.isManager(); // Giải thích: chỉ tài khoản có quyền quản lý mới được phép truy cập tab 2.
        jTabbedPane1.setEnabledAt(1, canManage); // Giải thích: disable toàn bộ tab "Quản lí" đối với nhân viên thường.
        if (!canManage) {
            jTabbedPane1.setSelectedIndex(0); // Giải thích: tự động đưa người dùng về tab cá nhân khi không đủ quyền.
        }
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

    private Boolean getPersonalGenderSelection() {
        if (rdoNam1.isSelected()) {
            return Boolean.TRUE; // Giải thích: true ứng với giới tính Nam khi nhân viên tự cập nhật.
        }
        if (rdoNu1.isSelected()) {
            return Boolean.FALSE; // Giải thích: false ứng với giới tính Nữ.
        }
        return null; // Giải thích: null để controller hiển thị thông báo chọn giới tính khi cần.
    }

    private void handleAddEmployee() {
        Integer newEmployeeId = null;
        try {
            newEmployeeId = nhanVienController.addEmployee(
                    txtTen.getText(),
                    txtTuoi.getText(),
                    getSelectedGender(),
                    txtSDT.getText(),
                    txtEmail.getText());
            boolean accountSaved = saveManagementAccount(newEmployeeId, null); // Giải thích: nếu nhập TK/MK thì tạo luôn tài khoản.
            if (accountSaved) {
                MessageHelper.showInfo(this, "Thêm nhân viên và tài khoản thành công");
            } else {
                MessageHelper.showInfo(this, "Thêm nhân viên thành công (chưa tạo tài khoản)");
            }
            loadAllEmployees();
            clearForm();
        } catch (IllegalArgumentException ex) {
            if (newEmployeeId != null) {
                rollbackEmployeeInsert(newEmployeeId);
            }
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            if (newEmployeeId != null) {
                rollbackEmployeeInsert(newEmployeeId);
                logger.log(Level.SEVERE, "Không thể tạo tài khoản cho nhân viên vừa thêm", ex);
                MessageHelper.showError(this, "Không thể tạo tài khoản cho nhân viên vừa thêm");
            } else {
                logger.log(Level.SEVERE, "Lỗi thêm nhân viên", ex);
                MessageHelper.showError(this, "Không thể thêm nhân viên vào CSDL GIAYTHETHAO");
            }
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
            boolean accountSaved = saveManagementAccount(selectedNhanVienId, selectedTaiKhoanIdQL); // Giải thích: cập nhật song song tài khoản nếu có dữ liệu nhập.
            if (accountSaved) {
                MessageHelper.showInfo(this, "Đã cập nhật nhân viên và tài khoản");
            } else {
                MessageHelper.showInfo(this, "Cập nhật nhân viên thành công");
            }
            loadAllEmployees();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi cập nhật nhân viên", ex);
            MessageHelper.showError(this, "Không thể cập nhật nhân viên trong CSDL GIAYTHETHAO");
        }
    }

    private void handleUpdateLoggedInEmployee() {
        if (!AuthHelper.isLoggedIn()) {
            MessageHelper.showError(this, "Vui lòng đăng nhập trước khi sửa thông tin cá nhân");
            return;
        }
        TaiKhoan currentUser = AuthHelper.getCurrentUser();
        try {
            nhanVienController.updateEmployee(
                    currentUser.getIdNhanVien(),
                    txtTen1.getText(),
                    txtTuoi1.getText(),
                    getPersonalGenderSelection(),
                    txtSDT1.getText(),
                    txtEmail1.getText());
            MessageHelper.showInfo(this, "Đã cập nhật thông tin cá nhân");
            loadLoggedInEmployeeInfo();
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi cập nhật thông tin nhân viên đang đăng nhập", ex);
            MessageHelper.showError(this, "Không thể cập nhật thông tin cá nhân trong CSDL GIAYTHETHAO");
        }
    }

    private void handleUpdateAccount() {
        if (!AuthHelper.isLoggedIn()) {
            MessageHelper.showError(this, "Vui lòng đăng nhập trước khi sửa tài khoản");
            return; // Giải thích: bảo vệ trường hợp người dùng mở form nhưng chưa đăng nhập.
        }
        TaiKhoan currentUser = AuthHelper.getCurrentUser();
        char[] passwordChars = txtMatKhau.getText().toCharArray(); // Giải thích: chuyển String sang mảng char cho controller xử lý.
        try {
            taiKhoanController.updateAccount(
                    currentUser.getIdTaiKhoan(),
                    txtTimKiem1.getText(),
                    txtTaiKhoan.getText(),
                    passwordChars,
                    currentUser.isVaiTro());
            currentUser.setTenDangNhap(txtTaiKhoan.getText().trim()); // Giải thích: cập nhật lại cache đăng nhập để đồng bộ giao diện.
            currentUser.setMatKhau(txtMatKhau.getText());
            MessageHelper.showInfo(this, "Đã cập nhật tài khoản đăng nhập");
        } catch (IllegalArgumentException ex) {
            MessageHelper.showError(this, ex.getMessage());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi cập nhật tài khoản", ex);
            MessageHelper.showError(this, "Không thể cập nhật tài khoản trên CSDL GIAYTHETHAO");
        } finally {
            Arrays.fill(passwordChars, '\0'); // Giải thích: xóa mật khẩu khỏi bộ nhớ sau khi xử lý.
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
            boolean hadAccount = selectedTaiKhoanIdQL != null; // Giải thích: ghi nhận trạng thái để thông báo sau khi xóa.
            deleteAccountForSelectedEmployee();
            nhanVienController.deleteEmployee(selectedNhanVienId);
            if (hadAccount) {
                MessageHelper.showInfo(this, "Đã xóa nhân viên và tài khoản đi kèm");
            } else {
                MessageHelper.showInfo(this, "Đã xóa nhân viên");
            }
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
            clearManagementAccountFields(); // Giải thích: kết quả tìm kiếm mới cũng cần xóa tài khoản cũ để tránh thao tác nhầm.
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Lỗi tìm kiếm nhân viên", ex);
            MessageHelper.showError(this, "Không thể tìm kiếm trên CSDL GIAYTHETHAO");
        }
    }

    private void loadAccountForSelectedEmployee(int idNhanVien) {
        try {
            TaiKhoan taiKhoan = taiKhoanController.getAccountByEmployeeId(idNhanVien);
            fillManagementAccountFields(taiKhoan);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể tải tài khoản của nhân viên", ex);
            MessageHelper.showError(this, "Không thể tải tài khoản trên CSDL GIAYTHETHAO cho nhân viên đang chọn");
            clearManagementAccountFields();
        }
    }

    private void fillManagementAccountFields(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            clearManagementAccountFields();
            return; // Giải thích: nhân viên chưa được cấp tài khoản thì để trống để tránh hiểu nhầm.
        }
        selectedTaiKhoanIdQL = taiKhoan.getIdTaiKhoan(); // Giải thích: ghi nhận Id để 3 nút Thêm/Sửa/Xóa thao tác đồng bộ.
        txtTaiKhoanQL.setText(taiKhoan.getTenDangNhap());
        txtMatKhauQL.setText(taiKhoan.getMatKhau());
    }

    private boolean hasManagementAccountInput() {
        boolean hasUsername = txtTaiKhoanQL.getText() != null && !txtTaiKhoanQL.getText().trim().isEmpty();
        boolean hasPassword = txtMatKhauQL.getText() != null && !txtMatKhauQL.getText().trim().isEmpty();
        return hasUsername || hasPassword; // Giải thích: dùng để biết người dùng có nhập tài khoản/mật khẩu hay không.
    }

    private boolean saveManagementAccount(int employeeId, Integer accountId) throws SQLException {
        if (!hasManagementAccountInput()) {
            return false; // Giải thích: người dùng không nhập gì → bỏ qua để chỉ lưu nhân viên.
        }
        String username = txtTaiKhoanQL.getText();
        String passwordText = txtMatKhauQL.getText();
        if (username == null || username.trim().isEmpty() || passwordText == null || passwordText.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
        }
        char[] passwordChars = passwordText.toCharArray();
        try {
            if (accountId == null) {
                taiKhoanController.addAccount(String.valueOf(employeeId), username, passwordChars, false); // Giải thích: mặc định tài khoản mới thuộc vai trò nhân viên.
            } else {
                taiKhoanController.updateAccount(accountId, String.valueOf(employeeId), username, passwordChars, false);
            }
            if (selectedNhanVienId != null && selectedNhanVienId.equals(employeeId)) {
                loadAccountForSelectedEmployee(employeeId); // Giải thích: refresh để hiển thị đúng Id tài khoản sau khi lưu.
            }
            return true;
        } finally {
            Arrays.fill(passwordChars, '\0'); // Giải thích: xóa mật khẩu khỏi bộ nhớ tạm thời.
        }
    }

    private void deleteAccountForSelectedEmployee() throws SQLException {
        if (selectedTaiKhoanIdQL != null) {
            taiKhoanController.deleteAccount(selectedTaiKhoanIdQL); // Giải thích: xóa tài khoản trước khi xóa nhân viên để tránh lỗi khóa ngoại.
            selectedTaiKhoanIdQL = null;
        }
    }

    private void rollbackEmployeeInsert(int employeeId) {
        try {
            nhanVienController.deleteEmployee(employeeId); // Giải thích: đảm bảo khi tạo tài khoản thất bại thì nhân viên vừa thêm cũng bị hủy để dữ liệu đồng nhất.
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Không thể rollback nhân viên sau khi tạo tài khoản thất bại", ex);
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
        jLabel15 = new javax.swing.JLabel();
        txtTaiKhoan = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtMatKhau = new javax.swing.JTextField();
        BtSuaTK = new javax.swing.JButton();
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
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtTaiKhoanQL = new javax.swing.JTextField();
        txtMatKhauQL = new javax.swing.JTextField();

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

        jLabel13.setText("Giới tính:");

        jLabel14.setText("Tuổi:");

        jLabel15.setText("Tài khoản:");

        jLabel16.setText("Mật Khẩu:");

        BtSuaTK.setText("Sửa TK");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(BtSuaTK)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel11)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel10)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(29, 29, 29)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtTimKiem1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                        .addComponent(txtTen1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                        .addComponent(txtTuoi1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                        .addComponent(txtTaiKhoan))))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btSua, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(rdoNam1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(27, 27, 27)
                                            .addComponent(rdoNu1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtEmail1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                        .addComponent(txtMatKhau))))
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(59, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(32, 32, 32)
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
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtTaiKhoan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(txtMatKhau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(BtSuaTK)
                .addGap(132, 132, 132))
        );

        jTabbedPane1.addTab("Nhân viên", jPanel1);

        buttonGroup1.add(rdoNam);
        rdoNam.setText("Nam");

        jLabel3.setText("Số điện thoại:");

        buttonGroup1.add(rdoNu);
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
        jLabel7.setText("Quản lí nhân viên");

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

        jLabel5.setText("giới tính:");

        jLabel2.setText("Tuổi:");

        jLabel17.setText("Tài khoản:");

        jLabel18.setText("Mật khẩu:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 666, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtTen, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtTuoi, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(41, 41, 41)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel18)
                                            .addComponent(jLabel17)))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(rdoNam, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(48, 48, 48)
                                                .addComponent(rdoNu, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(27, 27, 27)
                                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btTimKiem)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtTaiKhoanQL, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(txtMatKhauQL, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(246, 246, 246)
                        .addComponent(jLabel7)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel7)
                .addGap(51, 51, 51)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtTimKiem)
                    .addComponent(btTimKiem))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(txtTaiKhoanQL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTuoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel18)
                    .addComponent(txtMatKhauQL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rdoNu)
                        .addComponent(jButton3)
                        .addComponent(jButton2)
                        .addComponent(jButton1))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(rdoNam)))
                .addGap(39, 39, 39)
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
    private javax.swing.JButton BtSuaTK;
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
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
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
    private javax.swing.JTextField txtMatKhau;
    private javax.swing.JTextField txtMatKhauQL;
    private javax.swing.JTextField txtSDT;
    private javax.swing.JTextField txtSDT1;
    private javax.swing.JTextField txtTaiKhoan;
    private javax.swing.JTextField txtTaiKhoanQL;
    private javax.swing.JTextField txtTen;
    private javax.swing.JTextField txtTen1;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtTimKiem1;
    private javax.swing.JTextField txtTuoi;
    private javax.swing.JTextField txtTuoi1;
    // End of variables declaration//GEN-END:variables
}
