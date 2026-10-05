# QLCT_LTDD — Quản lý chi tiêu cá nhân

Đồ án môn Lập trình di động, nhóm 4 thành viên.

## Phiên bản hiện tại

- Thêm, sửa, xóa giao dịch thu/chi.
- Lọc theo loại giao dịch và tháng hiện tại; hiển thị tổng thu/chi.
- SQLiteOpenHelper lưu dữ liệu cục bộ; có 7 danh mục và 8 giao dịch mẫu khi tạo database lần đầu.
- Báo cáo Word và sơ đồ nằm trong `docs/`; báo cáo Basic phản ánh phiên bản cơ bản, đề cương cũ chứa các chức năng dự kiến.

## Yêu cầu chính thức cần hoàn thiện

- Giao diện 100% Jetpack Compose, không dùng XML layout.
- Kiến trúc MVVM.
- Room lưu giao dịch.
- Biểu đồ chi tiêu theo tháng.

Mã hiện tại vẫn dùng Android Views và SQLiteOpenHelper, chưa hoàn thành bốn yêu cầu kỹ thuật trên. Các yêu cầu này là hướng phát triển tiếp theo, không phải tính năng đã triển khai.

## Chạy dự án

Mở thư mục repo bằng Android Studio, chờ Gradle Sync, chọn cấu hình `app`, chọn máy ảo hoặc điện thoại Android API 24 trở lên và nhấn Run. Dự án dùng compileSdk/targetSdk 37; cấu hình JDK của Gradle nằm trong `gradle/gradle-daemon-jvm.properties`.

Build APK trên Windows:

```powershell
.\gradlew.bat assembleDebug
```

Kiểm tra logic SQLite trên máy tính:

```powershell
python docs/tests/test_database.py
```

`local.properties`, thư mục build, cache IDE, khóa ký và database trên thiết bị không được đưa lên Git. Không cần cài SQLite riêng để chạy ứng dụng Android.
