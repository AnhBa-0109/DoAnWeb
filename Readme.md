Be Fair! - Ứng dụng Quản lý & Chia đều Chi phí Chuyến đi

Khẩu hiệu: Tiền bạc sòng phẳng - Cuộc vui trọn vẹn.

Be Fair! là một ứng dụng web xây dựng trên nền tảng Spring Boot (Java), giúp các nhóm bạn, gia đình hoặc đồng nghiệp dễ dàng ghi chép, quản lý hóa đơn và tự động tính toán, giải quyết bài toán "ai nợ ai bao nhiêu tiền" sau mỗi chuyến du lịch, liên hoan hay hoạt động tập thể một cách minh bạch và nhanh chóng.

---

✨ Tính năng nổi bật của hệ thống

- Hệ thống Tài khoản & Bảo mật:
  + Đăng ký và đăng nhập bảo mật tích hợp hệ thống.
  + Mã hóa mật khẩu người dùng bằng giải thuật BCryptPasswordEncoder.
- Quản lý nhóm chi tiêu:
  - Tạo mới nhóm chi tiêu gắn liền với tài khoản sở hữu.
  - Cho phép xóa nhóm, tự động dọn sạch các hóa đơn và thành viên thuộc nhóm đó.
- Quản lý thành viên linh hoạt:
  - Thêm thành viên mới vào nhóm.
  - Hỗ trợ đổi tên hoặc xóa mềm thành viên khi có người rời nhóm mà không làm ảnh hưởng việc tính toán tiền nợ.
- Ghi chép hóa đơn:
  - Ghi nhận người chi trả hóa đơn và danh sách những người có liên quan đến hóa đơn đó.
  - Tích hợp tính năng tải ảnh hóa đơn trực quan.
  - Tự động chia đều hóa đơn cho toàn bộ thành viên đang hoạt động nếu người dùng không chọn cụ thể danh sách người chia.
- Giải thuật Quyết toán & Tính toán công nợ tự động:
  - Tự động phân tích toàn bộ lịch sử hóa đơn để tính toán số tiền cần trả của từng người.
  - Tự động ghép cặp người nợ và người chủ nợ để hoàn trả số tiền một cách gọn gàng nhất.

---

🛠️ Công nghệ tích hợp

Backend
- Java 17+
- Spring Boot Framework:
  - Spring Boot  Web
  - Spring Boot  Security
  - Spring Boot  Data JPA
- Database: MySQL

Frontend
- Thymeleaf HTML Engine
- Tailwind CSS Framework
- Google Material Symbols

---

📂 Kiến trúc và Cấu trúc thư mục Source Code

Dự án được cấu trúc chuẩn hóa theo mô hình MVC:

```text
src/main/java/khanh/ntu/BF/
├── configs/
│   ├── SecurityConfig.java          # Cấu hình phân quyền bảo mật và Form Login/Logout
│   └── WebConfig.java               # Cấu hình ResourceHandler để hiển thị ảnh hóa đơn tải lên
├── controllers/
│   ├── AuthController.java          # Điều hướng đăng ký, đăng nhập tài khoản
│   ├── ExpenseController.java       # Xử lý nghiệp vụ thêm/xóa hóa đơn
│   ├── TravelGroupController.java   # Xử lý luồng xem danh sách nhóm, chi tiết nhóm và quản lý thành viên
│   └── GroupApiController.java      # REST API cung cấp dữ liệu tính toán nợ và quyết toán
├── models/
│   ├── User.java, TravelGroup.java, Member.java, Expense.java  # Các thực thể dữ liệu 
│   └── MemberDebtDto.java, SettleUpDto.java                     # Các đối tượng chuyển đổi dữ liệu
├── Repository/
│   └── UserRepository.java, TravelGroupRepository.java...      # Tầng kết nối Database qua Spring Data JPA
└── services/
    ├── BeFairService.java           # Chứa toàn bộ core-logic cốt lõi, giải thuật tính toán số dư và nợ nần
    └── CustomUserDetailsService.java # Cung cấp dữ liệu người dùng cho hệ thống Spring Security
