HƯỚNG DẪN LAB – DỰ ÁN MẪU QUẢN LÝ CỬA HÀNG GIÀY THỂ THAO
(Áp dụng cho CSDL GIAYTHETHAO – công nghệ Java + SQL Server)

----------------------------------------------------------------------
1. TỔNG QUAN DỰ ÁN

1.1. Mô tả dự án

Dự án mẫu: ứng dụng desktop “Quản lý cửa hàng giày thể thao” dùng Java (Swing) kết nối SQL Server với CSDL GIAYTHETHAO.

Chức năng chính:
- Quản lý danh mục dùng chung:
  • Đợt giảm giá (DotGiamGia)
  • Size (Size)
  • Màu sắc (MauSac)
  • Thương hiệu (ThuongHieu)
  • Chất liệu (ChatLieu)
  • Đế giày (DeGiay)
- Quản lý đối tượng:
  • Khách hàng (KhachHang)
  • Nhân viên (NhanVien)
  • Tài khoản đăng nhập (TaiKhoan)
- Quản lý sản phẩm:
  • Sản phẩm giày bóng đá (GiayBongDa)
  • Biến thể giày (BienTheGiay) – gắn với đợt giảm giá, giá bán, số lượng tồn
- Nghiệp vụ bán hàng:
  • Hóa đơn (HoaDon)
  • Chi tiết hóa đơn (ChiTietHoaDon)
  • Thanh toán (ThanhToan)

1.2. Công nghệ sử dụng

- Ngôn ngữ: Java 8+.
- Giao diện: Java Swing.
- Quản lý thư viện: Maven.
- CSDL: SQL Server (script: GIAYTHETHAO.sql).
- Thư viện:
  • JDBC driver SQL Server.
  • Lombok (giảm code getter/setter).
- Mô hình:
  • UI (JFrame, JDialog)
  • Controller
  • DAO/DAO impl
  • Entity (Model)
  • Helper (XJdbc, XMessage, Auth, DateHelper, v.v.)

----------------------------------------------------------------------
2. CƠ SỞ DỮ LIỆU GIAYTHETHAO

2.1. Danh sách bảng chính

1) DotGiamGia
- IdDotGiamGia INT (PK, identity)
- TenDotGiamGia NVARCHAR(200)
- NgayBatDau DATETIME
- NgayKetThuc DATETIME
- PhanTramGiam INT
- TrangThai INT

2) Size
- IdSize INT (PK, identity)
- KichThuoc VARCHAR(20)

3) MauSac
- IdMauSac INT (PK, identity)
- TenMau NVARCHAR(50)
- MaMauHex VARCHAR(7)

4) KhachHang
- IdKhachHang INT (PK, identity)
- MaKhachHang VARCHAR(50) UNIQUE
- TenKhachHang NVARCHAR(100)
- SoDienThoai VARCHAR(15) UNIQUE
- Email VARCHAR(100)

5) ThuongHieu
- IdThuongHieu INT (PK, identity)
- TenThuongHieu NVARCHAR(100)
- QuocGia NVARCHAR(100)

6) ChatLieu
- IdChatLieu INT (PK, identity)
- TenChatLieu NVARCHAR(100)
- MoTa NVARCHAR(MAX)

7) DeGiay
- IdDeGiay INT (PK, identity)
- TenDeGiay NVARCHAR(100)
- MoTa NVARCHAR(MAX)

8) NhanVien
- IdNhanVien INT (PK, identity)
- MaNhanVien VARCHAR(50) UNIQUE
- TenNhanVien NVARCHAR(100)
- SoDienThoai VARCHAR(15) UNIQUE
- Email VARCHAR(100)
- DiaChi NVARCHAR(255)
- NgaySinh DATE
- GioiTinh BIT
- TrangThai INT

9) TaiKhoan
- IdTaiKhoan INT (PK, identity)
- TenDangNhap VARCHAR(50) UNIQUE
- MatKhau VARCHAR(100)
- IdNhanVien INT (FK → NhanVien)
- QuyenHan NVARCHAR(50)   -- (ví dụ: ADMIN, STAFF)
- TrangThai INT

10) GiayBongDa
- IdGiay INT (PK, identity)
- IdThuongHieu INT (FK → ThuongHieu)
- IdMauSac INT (FK → MauSac)
- IdSize INT (FK → Size)
- IdChatLieu INT (FK → ChatLieu)
- IdDeGiay INT (FK → DeGiay)
- MaGiay VARCHAR(50) UNIQUE
- TenGiay NVARCHAR(200)
- MoTa NVARCHAR(MAX)

11) BienTheGiay
- IdBienTheGiay INT (PK, identity)
- IdDotGiamGia INT NULL (FK → DotGiamGia)
- IdGiay INT (FK → GiayBongDa)
- GiaBan DECIMAL(18,2)
- SoLuong INT

12) HoaDon
- IdHoaDon INT (PK, identity)
- IdKhachHang INT (FK → KhachHang)
- IdTaiKhoan INT NULL (FK → TaiKhoan - nhân viên tạo)
- TienGiam DECIMAL(18,2) DEFAULT 0
- MaHoaDon VARCHAR(50) UNIQUE
- NgayLap DATETIME DEFAULT GETDATE()
- TongTien DECIMAL(18,2)
- TrangThai INT
- GhiChu NVARCHAR(500)

13) ThanhToan
- IdThanhToan INT (PK, identity)
- IdHoaDon INT (FK → HoaDon)
- TienThanhToan DECIMAL(18,2)
- NgayThanhToan DATETIME DEFAULT GETDATE()
- HinhThucThanhToan NVARCHAR(100)
- TrangThai INT
- GhiChu NVARCHAR(500)

14) ChiTietHoaDon
- IdCTHoaDon INT (PK, identity)
- IdHoaDon INT (FK → HoaDon)
- IdBienTheGiay INT (FK → BienTheGiay)
- SoLuong INT
- DonGia DECIMAL(18,2)
- ThanhTien DECIMAL(18,2)

2.2. Quan hệ chính

- 1 DotGiamGia – N BienTheGiay.
- 1 ThuongHieu / MauSac / Size / ChatLieu / DeGiay – N GiayBongDa.
- 1 GiayBongDa – N BienTheGiay.
- 1 KhachHang – N HoaDon.
- 1 TaiKhoan – N HoaDon (nhân viên lập hóa đơn).
- 1 HoaDon – N ChiTietHoaDon.
- 1 HoaDon – N ThanhToan.

----------------------------------------------------------------------
3. TỔ CHỨC DỰ ÁN JAVA

3.1. Tên project và packages

- Tên project Maven: GIAYTHETHETHAO (hoặc ShoeStoreApp).
- Gợi ý cấu trúc package:

  • shoestore.entity
    - DotGiamGia, Size, MauSac, KhachHang, ThuongHieu,
      ChatLieu, DeGiay, NhanVien, TaiKhoan,
      GiayBongDa, BienTheGiay, HoaDon, ThanhToan, ChiTietHoaDon

  • shoestore.dao
    - CrudDAO<T, K>
    - các interface: DotGiamGiaDAO, SizeDAO, MauSacDAO, ThuongHieuDAO, ...

  • shoestore.dao.impl
    - các lớp triển khai: DotGiamGiaDAOImpl, SizeDAOImpl, ...

  • shoestore.helper
    - XJdbc, XMessage, DateHelper, Auth, ShareHelper, ...

  • shoestore.ui
    - MainJFrame (cửa sổ chính)
    - các JDialog quản lý: SizeManagerJDialog, MauSacManagerJDialog, ThuongHieuManagerJDialog,
      ChatLieuManagerJDialog, DeGiayManagerJDialog, GiayManagerJDialog,
      BienTheGiayManagerJDialog, KhachHangManagerJDialog, NhanVienManagerJDialog,
      TaiKhoanManagerJDialog, DotGiamGiaManagerJDialog,
      SalesJDialog, HoaDonJDialog, ThanhToanJDialog, LoginJDialog, WelcomeJDialog

  • shoestore.controller
    - các interface & lớp controller cho từng màn hình.

3.2. Entity & Lombok

- Mỗi bảng trong CSDL tạo 1 lớp entity tương ứng.
- Sử dụng Lombok:
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder

  Ví dụ:

  public class GiayBongDa {
      private Integer idGiay;
      private Integer idThuongHieu;
      private Integer idMauSac;
      private Integer idSize;
      private Integer idChatLieu;
      private Integer idDeGiay;
      private String maGiay;
      private String tenGiay;
      private String moTa;
  }

3.3. Lớp DAO tổng quát

- Interface:

  public interface CrudDAO<T, K> {
      void insert(T entity);
      void update(T entity);
      void delete(K id);
      T findById(K id);
      List<T> findAll();
  }

- Các DAO cụ thể (ví dụ):

  public interface GiayBongDaDAO extends CrudDAO<GiayBongDa, Integer> {
      List<GiayBongDa> findByThuongHieu(int idThuongHieu);
  }

- Lớp triển khai dùng XJdbc để thực hiện truy vấn SQL.

----------------------------------------------------------------------
4. HƯỚNG DẪN THEO TỪNG LAB (PHIÊN BẢN “BÁN GIÀY”)

Lưu ý: Phần này được viết lại dựa trên file PolyCafe cũ nhưng đã chỉnh sửa về nghiệp vụ và CSDL cho đúng với GIAYTHETHAO.

--------------------
LAB 1 – KHỞI TẠO DỰ ÁN & GIAO DIỆN CƠ BẢN

Mục tiêu:
- Tạo project Maven Java.
- Kết nối SQL Server.
- Xây dựng khung giao diện chính cho ứng dụng quản lý giày thể thao.
- Tạo màn hình Welcome (splash) kiểm tra kết nối CSDL.

Yêu cầu:

1. Tạo project Maven GIAYTHETHETHAO:
   - Thêm dependency:
     • SQL Server JDBC
     • Lombok
   - Cấu hình encoding, build…

2. Giao diện chính – MainJFrame:
   - Khu vực hiển thị thông tin tài khoản đang đăng nhập (tên nhân viên, quyền).
   - Các nút chức năng chính:
     • Bán hàng
     • Hóa đơn
     • Khách hàng
     • Quản lý giày
     • Danh mục (Size, Màu sắc, Thương hiệu, Chất liệu, Đế giày)
     • Đợt giảm giá
     • Nhân viên – Tài khoản
     • Đăng xuất / Thoát
   - Đặt tên component rõ ràng: btnBanHang, btnHoaDon, btnKhachHang, btnGiay, btnThuongHieu, btnSize, btnMauSac, btnChatLieu, btnDeGiay, btnDotGiamGia, btnNhanVien, btnTaiKhoan, btnLogout…

3. WelcomeJDialog:
   - Có JProgressBar, chạy từ 0 → 100%.
   - Trong quá trình load: kiểm tra kết nối CSDL GIAYTHETHAO.
   - Nếu kết nối thành công → mở LoginJDialog.
   - Nếu thất bại → thông báo lỗi, cho phép cấu hình lại chuỗi kết nối (nếu muốn).

--------------------
LAB 2 – LẬP TRÌNH TRUY XUẤT DỮ LIỆU (DAO & ENTITY)

Mục tiêu:
- Tạo toàn bộ entity cho 14 bảng.
- Xây dựng các DAO & DAOImpl tương ứng.
- Viết lớp XJdbc hỗ trợ thao tác CSDL.

Yêu cầu:

1. Tạo entity:
   - Mỗi bảng 1 lớp trong package shoestore.entity.
   - Thuộc tính trùng tên cột (hoặc đặt tên Java-style nhưng mapping đúng).
   - Có đầy đủ getter/setter hoặc dùng Lombok.

2. Xây dựng XJdbc:
   - Kết nối theo chuỗi URL tới CSDL GIAYTHETHAO.
   - Các phương thức:
     • update(String sql, Object... args)
     • query(String sql, Object... args)
     • value(String sql, Object... args)

3. Xây dựng DAO:
   - Tạo CrudDAO<T,K>.
   - Tạo DAO & Impl cho các bảng quan trọng:
     • ThuongHieuDAO, MauSacDAO, SizeDAO, ChatLieuDAO, DeGiayDAO
     • GiayBongDaDAO, BienTheGiayDAO
     • KhachHangDAO, NhanVienDAO, TaiKhoanDAO
     • DotGiamGiaDAO, HoaDonDAO, ChiTietHoaDonDAO, ThanhToanDAO
   - Mỗi DAOImpl:
     • Ánh xạ ResultSet → Entity.
     • Thực hiện CRUD với bảng tương ứng.

--------------------
LAB 3 – QUẢN LÝ DANH MỤC DÙNG CHUNG

Mục tiêu:
- Xây dựng các màn hình quản lý:
  • Size
  • Màu sắc
  • Chất liệu
  • Đế giày
  • Thương hiệu

Yêu cầu:

1. SizeManagerJDialog:
   - Tab “Danh sách”: JTable liệt kê các size (IdSize, KichThuoc).
   - Tab “Biểu mẫu”: text nhập KichThuoc + nút Thêm/Sửa/Xóa/Mới.
   - Điều hướng bản ghi (First/Prev/Next/Last).
   - Kiểm tra dữ liệu:
     • Không để trống.
     • Không trùng kích thước nếu muốn.

2. MauSacManagerJDialog:
   - Quản lý TenMau, MaMauHex.
   - Cho phép nhập mã màu hex (#RRGGBB), có thể hiển thị preview màu.

3. ChatLieuManagerJDialog & DeGiayManagerJDialog:
   - Mỗi form gồm:
     • Danh sách
     • Biểu mẫu (tên, mô tả)
   - CRUD đầy đủ.

4. ThuongHieuManagerJDialog:
   - Quản lý TenThuongHieu, QuocGia.
   - Hỗ trợ tìm kiếm theo tên thương hiệu.

--------------------
LAB 4 – QUẢN LÝ SẢN PHẨM GIÀY VÀ BIẾN THỂ

Mục tiêu:
- Quản lý sản phẩm giày cơ bản (GiayBongDa).
- Quản lý biến thể giày (BienTheGiay) theo đợt giảm giá, giá bán, số lượng.

Yêu cầu:

1. GiayManagerJDialog:
   - Danh sách giày: MaGiay, TenGiay, Thương hiệu, Màu, Size, Chất liệu, Đế giày.
   - Biểu mẫu:
     • MaGiay, TenGiay, MoTa.
     • Combobox chọn: Thương hiệu, Màu, Size, Chất liệu, Đế giày.
   - Chức năng:
     • Thêm/Sửa/Xóa/Mới.
     • Tìm kiếm theo mã, tên, thương hiệu.

2. BienTheGiayManagerJDialog (hoặc tab con trong GiayManager):
   - Danh sách biến thể theo từng IdGiay.
   - Trường: IdBienTheGiay, DotGiamGia, GiaBan, SoLuong.
   - Cho phép chọn đợt giảm giá (có thể để NULL).
   - Kiểm tra:
     • GiaBan > 0.
     • SoLuong ≥ 0.

3. DotGiamGiaManagerJDialog:
   - Quản lý các chương trình giảm giá.
   - TenDotGiamGia, NgayBatDau, NgayKetThuc, PhanTramGiam, TrangThai.
   - Kiểm tra:
     • NgayBatDau ≤ NgayKetThuc.
     • PhanTramGiam trong khoảng hợp lệ (0–100).

--------------------
LAB 5 – QUẢN LÝ KHÁCH HÀNG, NHÂN VIÊN, TÀI KHOẢN

Mục tiêu:
- Quản lý đối tượng sử dụng hệ thống và khách hàng mua hàng.

Yêu cầu:

1. KhachHangManagerJDialog:
   - Danh sách khách hàng.
   - Biểu mẫu: MaKhachHang, TenKhachHang, SoDienThoai, Email.
   - Ràng buộc:
     • SĐT, Email không trùng (theo CSDL).
     • Kiểm tra format SĐT, Email.

2. NhanVienManagerJDialog:
   - Quản lý thông tin nhân viên.
   - Các trường: MaNhanVien, TenNhanVien, SĐT, Email, Địa chỉ, Ngày sinh, Giới tính, Trạng thái.
   - Có thể kèm ảnh nhân viên (tùy chọn).

3. TaiKhoanManagerJDialog:
   - Gán tài khoản cho nhân viên (IdNhanVien).
   - Trường: TenDangNhap, MatKhau, QuyenHan, TrangThai.
   - Kiểm tra trùng tên đăng nhập, độ dài mật khẩu.
   - Hỗ trợ đổi mật khẩu.

4. LoginJDialog:
   - Đăng nhập bằng TenDangNhap + MatKhau.
   - Kiểm tra với bảng TaiKhoan.
   - Chỉ cho phép đăng nhập nếu TrangThai tài khoản & nhân viên đang hoạt động.

--------------------
LAB 6 – CHỨC NĂNG BÁN HÀNG (PHẦN 1 – MÀN HÌNH SALES)

Mục tiêu:
- Xây dựng màn hình bán hàng hiển thị danh sách sản phẩm.
- Lựa chọn khách hàng để lập hóa đơn.

Yêu cầu:

1. SalesJDialog:
   - Khu vực chọn khách hàng:
     • Ô tìm kiếm theo SĐT/Mã/Tên.
     • Nút “Thêm khách hàng mới”.
   - Khu vực danh sách sản phẩm:
     • Bảng hoặc panel hiển thị các biến thể giày (BienTheGiay) kèm:
       - Tên giày, Thương hiệu, Màu, Size, Giá bán, Số lượng tồn, Đợt giảm giá (nếu có).
   - Khi chọn khách hàng + chọn sản phẩm → chuyển sang HoaDonJDialog (Lab 7) để nhập số lượng & hoàn tất hóa đơn.

2. DAO hỗ trợ:
   - Viết các hàm:
     • Tìm khách hàng theo SĐT/Mã.
     • Lấy danh sách biến thể giày còn hàng.
     • Tính giá sau giảm dựa trên DotGiamGia (nếu có).

--------------------
LAB 7 – CHỨC NĂNG BÁN HÀNG (PHẦN 2 – HÓA ĐƠN & THANH TOÁN)

Mục tiêu:
- Lập hóa đơn, thêm/xóa/sửa chi tiết hóa đơn.
- Tính tổng tiền, tiền giảm, tiền thanh toán.
- Ghi nhận thanh toán theo bảng ThanhToan.

Yêu cầu:

1. HoaDonJDialog:
   - Thông tin hóa đơn:
     • Mã hóa đơn (MaHoaDon – sinh tự động).
     • Khách hàng, Nhân viên (từ tài khoản đăng nhập).
     • Ngày lập (NgayLap – mặc định NOW).
     • TienGiam, TongTien, TrangThai, GhiChu.
   - Bảng chi tiết:
     • Mỗi dòng ứng với 1 IdBienTheGiay:
       - Tên giày, Giá bán, Số lượng, Thành tiền.
   - Các nút:
     • Thêm sản phẩm: mở danh sách BienTheGiay hoặc nhận từ SalesJDialog.
     • Sửa số lượng (double click dòng).
     • Xóa dòng.
     • Lưu hóa đơn (insert/update HoaDon + ChiTietHoaDon).
     • Hủy hóa đơn (cập nhật TrangThai hoặc xóa nếu chưa thanh toán).

2. Tính toán:
   - ThanhTien = DonGia * SoLuong.
   - TongTien = tổng ThanhTien các dòng – TienGiam.
   - Có thể cho phép nhập TienGiam hoặc tính tự động theo DotGiamGia.

3. ThanhToanJDialog:
   - Liên kết với HoaDon:
     • IdHoaDon
     • TienThanhToan
     • NgayThanhToan
     • HinhThucThanhToan (tiền mặt, chuyển khoản, thẻ…)
     • TrangThai, GhiChu
   - Cho phép:
     • Thanh toán đủ một lần.
     • (Tùy chọn) Thanh toán nhiều lần cho một hóa đơn.

4. Cập nhật số lượng tồn:
   - Khi lưu hóa đơn:
     • Giảm SoLuong trong bảng BienTheGiay tương ứng.
   - Khi hủy hóa đơn:
     • Hoàn lại số lượng nếu trước đó đã trừ.

--------------------
LAB 8 – KIỂM THỬ ỨNG DỤNG

Mục tiêu:
- Thiết kế test case cho các chức năng chính.
- Đảm bảo nghiệp vụ phù hợp với CSDL GIAYTHETHAO.

Yêu cầu:

1. Test đăng nhập:
   - Đúng tài khoản / sai mật khẩu.
   - Tài khoản bị khóa.
   - Nhân viên bị khóa.
   - Bỏ trống các trường.

2. Test quản lý danh mục:
   - Thêm size trùng, màu trùng (nếu cấm trùng).
   - Phạm vi PhanTramGiam không hợp lệ cho DotGiamGia.
   - Thời gian NgayBatDau > NgayKetThuc.

3. Test quản lý sản phẩm:
   - Thêm giày với mã trùng (MaGiay).
   - Biến thể với số lượng âm hoặc giá âm.
   - Không chọn đầy đủ thương hiệu/màu/size/chất liệu/đế giày.

4. Test quản lý khách hàng, nhân viên, tài khoản:
   - Trùng SĐT/Email.
   - Định dạng SĐT, Email sai.
   - Đổi mật khẩu với mật khẩu yếu/không khớp.

5. Test bán hàng & hóa đơn:
   - Bán vượt quá số lượng tồn.
   - Hóa đơn không có chi tiết.
   - Hóa đơn chưa thanh toán nhưng đã chuyển trạng thái hoàn tất.
   - Kiểm tra tính toán TongTien, TienGiam, ThanhTien.

----------------------------------------------------------------------
5. GỢI Ý TRIỂN KHAI TRÊN CODEDEX

- Chia nội dung thành các module tương ứng 8 Lab:
  • Lab 1–2: Khởi tạo dự án, Entity, DAO.
  • Lab 3–5: Các màn hình quản lý danh mục, khách hàng, nhân viên, tài khoản.
  • Lab 6–7: Quy trình bán hàng, hóa đơn, thanh toán.
  • Lab 8: Kiểm thử.

- Mỗi Lab có:
  • Mục tiêu.
  • Hướng dẫn chi tiết (theo nội dung trên).
  • Yêu cầu nộp bài: code, script CSDL, ảnh chụp màn hình.

- Sản phẩm cuối:
  • Ứng dụng Java Swing quản lý cửa hàng giày thể thao kết nối CSDL GIAYTHETHAO.
  • Source code đầy đủ (entity, DAO, controller, UI).
  • Script GIAYTHETHAO.sql.
  • Bộ test case.


