# Báo cáo QLCT — cập nhật 21/10/2026

Sử dụng **Bao_cao_Do_an_QLCT.docx** để chỉnh sửa và nộp; bản PDF cùng tên để xem nhanh. Đây là báo cáo đồ án tập trung mục tiêu, chức năng, thiết kế tổng thể, Use Case, MVVM, ERD, luồng xử lý, giao diện, kiểm thử và phân công nhóm. Không trình bày lịch sử sửa code hoặc nâng cấp dữ liệu. Các bản có tên `Cap_nhat_v7` và `Compose_MVVM_Room` là bản trước khi chỉnh nội dung theo yêu cầu này.

Bản hiện tại đưa vào báo cáo 22 ảnh thật và 10 sơ đồ, có phân công đề xuất cho 4 thành viên, kết quả kiểm thử, kịch bản demo và câu hỏi bảo vệ. Thư mục `screenshots/current/` còn giữ thêm ảnh kiểm tra sáng/tối. Các sơ đồ `21_current_*` đến `30_current_*` trong `diagrams/current/` có cả PNG và SVG.

Thông tin tên trường/khoa, lớp, giảng viên và năm học chưa được xác nhận nên để trống có nhãn. Họ tên, mã sinh viên và phân công cũng cần nhóm kiểm tra trước khi nộp. Có thể tạo `report_metadata.json` với các khóa `school`, `faculty`, `class`, `lecturer`, `academic_year` rồi sinh lại báo cáo.

## Sinh lại

Yêu cầu Python với `python-docx`, `Pillow`, `matplotlib`; máy hiện tại có các thư viện ở `.gradle/report-libs`.

```powershell
python docs/build_final_report.py
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/export_current_report.ps1
```

Lệnh thứ hai yêu cầu Microsoft Word, cập nhật mục lục và xuất PDF. Nó không thay đổi chính sách PowerShell của hệ thống. Khi sửa thủ công bằng Word, cập nhật các trường bằng **Ctrl+A → F9** rồi xuất PDF mới.

`report_current_content.py` là nội dung đang dùng; `report_current_diagrams.py` sinh sơ đồ; `capture_report_screens.py` chụp trên package demo riêng. Các ảnh là ảnh thật, không phải bản thiết kế giả lập. Không chạy script chụp ảnh với package ứng dụng chính.

## Bằng chứng

Ngày 21/10/2026: build thành công, 16 unit test và 10 test Android đạt, lint không có lỗi và có 12 cảnh báo, 6 script SQLite/Python đạt. Bản mới bao gồm UserTableMigrationTest và test_user_migration.py. Cài nâng cấp thực tế từ v6→v7 trên máy ảo đã đối chiếu fingerprint toàn bộ tài khoản, danh mục, giao dịch và ngân sách: dữ liệu giữ nguyên, FK hợp lệ. `report_validation_current.json` lưu số liệu. `collect_report_validation.py` đọc kết quả Gradle sau khi đã chạy test; không tự chạy test hay các script host.

Lần chạy Android ban đầu trước nâng cấp v7 có một test tham chiếu SQL lỗi do dấu chấm phẩy trong chú thích; test đã được sửa và chạy lại đạt 9/9. Sau khi thêm kiểm thử nâng cấp bảng users, bộ hiện tại đạt 10/10.

Các file `Bao_cao_QLCT_Basic.docx`, `Bao_cao_QLCT_De_cuong.docx`, `Phu_luc_Ngan_sach_Tim_kiem.docx`, các script `report_sections_*.py`, `create_report.py`, `update_report.py`, `budget_report.py` và ảnh/sơ đồ cũ là tài liệu lịch sử. Chúng không được gọi để sinh báo cáo chính hiện tại. Không dùng nội dung cũ để suy ra số bảng, phiên bản database, đường dẫn mã hoặc chức năng hiện nay.
