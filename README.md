# QLCT_LTDD — Quản lý chi tiêu cá nhân

Đồ án môn Lập trình di động, nhóm 4 thành viên.

## Phiên bản hiện tại

- Thêm, sửa, xóa giao dịch thu/chi.
- Đăng nhập/đăng ký cục bộ, nhiều tài khoản với giao dịch tách riêng theo `user_id`. Tài khoản mới có 0 giao dịch, tổng thu/chi bằng 0. Nút Đăng xuất cố định ở đầu màn hình và hoạt động trên cả ba tab. Tên đăng nhập 3–32 ký tự không dấu/số/_, mật khẩu 8–128 ký tự. Mật khẩu lưu dưới dạng PBKDF2 với salt ngẫu nhiên, không lưu dạng rõ. Phiên chỉ ở bộ nhớ; chưa có cloud hoặc khôi phục mật khẩu.
- Lọc theo loại giao dịch, chọn tháng hoặc toàn bộ thời gian; hiển thị tổng thu/chi và chênh lệch.
- Room lưu dữ liệu cục bộ; `seed.sql` chỉ tạo 7 danh mục. Theo yêu cầu demo, tài khoản `vu98` được nạp riêng 6 khoản chi (ăn trưa, xăng, sách, đồ dùng, phòng, ăn sáng), tổng 1.345.000 VND, một lần sau đăng ký/đăng nhập thành công. `DemoDataSeeder` lưu cờ `demoSeeded` trong Room nên mở lại/đăng nhập lại không nhân đôi và không tái tạo dòng đã xóa. Các tài khoản khác vẫn bắt đầu rỗng. Mật khẩu demo do người dùng chọn được nhập qua màn hình, không hardcode trong seed/app.
- Biểu đồ cột tổng chi của 6 tháng đến tháng đã chọn, kèm số tiền từng tháng.
- Giao diện tông tím, thẻ bo góc, biểu đồ vòng phân bổ khoản chi theo danh mục và thanh điều hướng Tổng quan / Giao dịch / Thống kê. Nút + mở biểu mẫu thêm giao dịch; chạm giao dịch để sửa và nút thùng rác để xóa có xác nhận.
- Báo cáo đồ án hiện tại: [Word](docs/Bao_cao_Do_an_QLCT_Compose_MVVM_Room.docx) và [PDF](docs/Bao_cao_Do_an_QLCT_Compose_MVVM_Room.pdf), mô tả bản Compose–MVVM–Room, 9 use case, 12 sơ đồ và 8 ảnh giao diện thật. Nguồn sơ đồ PNG/SVG nằm trong `docs/diagrams/current/`; sinh báo cáo bằng `docs/build_final_report.py` (cần python-docx, matplotlib và Pillow). Bổ sung tên trường, khoa, lớp, giảng viên, năm học và xác nhận phân công thực tế trước khi nộp. Báo cáo Basic và đề cương cũ là tài liệu lịch sử.

## Yêu cầu kỹ thuật đã triển khai

- Giao diện 100% Jetpack Compose, không dùng XML layout.
- Kiến trúc MVVM.
- Room lưu giao dịch.
- Biểu đồ chi tiêu theo tháng.

Giao diện, biểu mẫu và bộ chọn ngày đều dùng Compose Material 3. XML còn lại là manifest, theme và drawable, không có XML layout.

Luồng MVVM: `MainActivity` → `ExpenseScreen` → `ExpenseViewModel` → `ExpenseRepository` → `ExpenseDao` → Room. DAO trả Flow, ViewModel cung cấp StateFlow, giao diện quan sát theo lifecycle. Bộ lọc và tháng dùng SavedStateHandle; dữ liệu nhập dùng rememberSaveable.

Database `qlct.db` phiên bản 5 có migration 1→2→3→4→5. Quan hệ: `local_account.id` → `transactions.user_id` và `categories.id` → `transactions.category_id`; cả hai là khóa ngoại có index, NO ACTION. Danh mục dùng chung, không có FK trực tiếp từ account tới categories. Version 5 thêm cờ demo và chuyển dữ liệu cũ chưa gán tài khoản thành `user_id=NULL`; dữ liệu/ID và dữ liệu cá nhân của tài khoản cũ được giữ. Repository đọc/update/delete theo tài khoản. Room xuất schema vào `app/schemas/`. Báo cáo Word hiện tại là snapshot trước đăng nhập; phần seed tám giao dịch trong báo cáo là mô tả bản cũ.

Số tiền phải là số nguyên VND từ 1 đến 999.999.999.999; không chấp nhận số âm hoặc số 0. Loại Thu/Chi quyết định dấu khi hiển thị và tổng hợp, số tiền lưu luôn dương. Khi nhấn Lưu với dữ liệu sai, ô Số tiền hiện lỗi cụ thể và giữ biểu mẫu; quy tắc được kiểm tra lại trong repository cho cả thêm và sửa.

Biểu đồ chỉ cộng khoản chi, độc lập với bộ lọc Thu/Chi của danh sách. Tổng quan phụ thuộc khoảng thời gian đã chọn và cũng độc lập với bộ lọc loại giao dịch.

## Chạy dự án

Mở thư mục repo bằng Android Studio, chờ Gradle Sync, chọn cấu hình `app`, chọn máy ảo hoặc điện thoại Android API 24 trở lên và nhấn Run. Dự án dùng compileSdk/targetSdk 37; cấu hình JDK của Gradle nằm trong `gradle/gradle-daemon-jvm.properties`.

Máy ảo đã cấu hình trên máy phát triển: `QLCT_API_35`, Pixel 6, Android 15 / API 35, Google APIs x86_64, RAM 4 GB. Mở **Tools → Device Manager**, chọn máy ảo này và nhấn Play; sau đó chọn nó làm thiết bị chạy cấu hình `app`. System image và AVD nằm trong thư mục người dùng, không đưa vào Git.

Máy ảo đã bật nhận bàn phím PC (`hw.keyboard=yes`, `hw.keyboard.lid=no`) và hiển thị bàn phím trên màn hình. Chạm ô Số tiền để nhập; có thể gõ từ bàn phím PC hoặc bấm bàn phím số Android. Khi thay đổi cấu hình keyboard của AVD cần khởi động lại máy ảo.

Build APK trên Windows:

```powershell
.\gradlew.bat assembleDebug
```

Kiểm tra thống kê và lint:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

Kiểm tra migration trên SQLite máy tính sau khi build (đối chiếu schema Room do KSP xuất):

```powershell
python docs/tests/test_room_migration.py
```

Kiểm thử Android cho migration Room, CRUD, tài khoản và lưu dữ liệu sau mở lại (dùng máy ảo riêng cho kiểm thử; Gradle có thể gỡ ứng dụng sau khi chạy, làm mất dữ liệu trên máy ảo đó):

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Kiểm tra hợp đồng SQLite của phiên bản Basic cũ:

```powershell
python docs/tests/test_database.py
```

`local.properties`, thư mục build, cache IDE, khóa ký và database trên thiết bị không được đưa lên Git. Không cần cài SQLite riêng để chạy ứng dụng Android.
