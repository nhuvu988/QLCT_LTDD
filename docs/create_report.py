from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from xml.sax.saxutils import escape

out = Path(__file__).parent
sections = [
('ĐỒ ÁN LẬP TRÌNH DI ĐỘNG — QLCT', [
'Đề tài: Ứng dụng quản lý chi tiêu cá nhân trên Android.',
'BẢN ĐỀ CƯƠNG 0.1 — ngày 05/10/2026. Đây là thiết kế đề xuất, chưa phải báo cáo nghiệm thu.',
'Sinh viên, mã số sinh viên, lớp, giảng viên hướng dẫn: bổ sung theo thông tin người học.']),
('1. Mục tiêu và hiện trạng', [
'Xây dựng ứng dụng hỗ trợ ghi nhận thu nhập, chi tiêu và theo dõi tình hình tài chính cá nhân.',
'Hiện trạng: đã khởi tạo dự án Android Kotlin tại D:\\QLCT, có màn hình mở đầu. Các nghiệp vụ dưới đây chưa được triển khai hoặc kiểm thử.',
'Cần xác nhận: thời hạn nộp, số thành viên, mẫu báo cáo, yêu cầu đăng nhập, API và lưu trữ online.']),
('2. Phạm vi chức năng đề xuất', [
'F01 — Quản lý giao dịch: thêm, xem, sửa, xóa khoản thu/chi; số tiền, ngày, danh mục và ghi chú.',
'F02 — Quản lý danh mục: tạo, sửa, xóa danh mục; ngăn xóa danh mục đang được sử dụng hoặc yêu cầu chuyển giao dịch sang danh mục khác.',
'F03 — Tra cứu: lọc giao dịch theo thời gian, loại thu/chi và danh mục.',
'F04 — Tổng quan: tổng thu, tổng chi và chênh lệch thu–chi trong khoảng thời gian được chọn.',
'F05 — Ngân sách: đặt hạn mức chi theo tháng; hiển thị mức sử dụng và cảnh báo khi vượt hạn mức.',
'F06 — Thống kê: tổng hợp chi theo danh mục và theo tháng.',
'Đề xuất bản đầu lưu dữ liệu cục bộ. Đăng nhập, đồng bộ online, xuất dữ liệu và nhắc lịch chưa nằm trong phạm vi đã chốt.']),
('3. Use case và đặc tả nghiệp vụ', [
'Actor dự kiến: Người dùng cá nhân. Không đưa quản trị viên vào sơ đồ nếu không có nghiệp vụ quản trị thực tế.',
'UC01 Thêm giao dịch; UC02 Xem/lọc giao dịch; UC03 Sửa giao dịch; UC04 Xóa giao dịch; UC05 Quản lý danh mục; UC06 Đặt ngân sách; UC07 Xem thống kê.',
'UC01 — Tiền điều kiện: ứng dụng mở được và có danh mục phù hợp. Người dùng chọn loại giao dịch, nhập số tiền, chọn danh mục/ngày, nhấn Lưu. Hệ thống kiểm tra dữ liệu, lưu giao dịch, cập nhật danh sách và tổng quan.',
'Ngoại lệ UC01: số tiền trống, không hợp lệ hoặc không lớn hơn 0 thì báo lỗi tại trường nhập; thiếu danh mục thì yêu cầu chọn; lưu thất bại thì giữ nội dung biểu mẫu và cho phép thử lại. Chỉ thông báo thành công sau khi lưu thành công.',
'Hậu điều kiện UC01: giao dịch tồn tại sau khi mở lại ứng dụng; thống kê phản ánh giao dịch vừa tạo.']),
('4. Luồng chức năng đề xuất', [
'Thêm giao dịch: Mở biểu mẫu → Chọn Thu/Chi → Nhập thông tin → Lưu → Kiểm tra hợp lệ → Lưu dữ liệu → Cập nhật giao diện. Nếu không hợp lệ: quay về biểu mẫu kèm thông báo lỗi.',
'Sửa giao dịch: Chọn giao dịch → Hiển thị dữ liệu cũ → Chỉnh sửa → Kiểm tra → Cập nhật → Tính lại tổng quan và ngân sách.',
'Xóa giao dịch: Chọn giao dịch → Yêu cầu xác nhận → Đồng ý: xóa và tính lại thống kê; Hủy: giữ nguyên.',
'Ngân sách: Chọn tháng → Nhập hạn mức dương → Lưu → Tổng hợp các khoản chi trong tháng → So sánh hạn mức → Hiển thị trạng thái.',
'Thống kê: Chọn khoảng thời gian → Đọc dữ liệu → Tổng hợp → Hiển thị kết quả; nếu không có dữ liệu thì hiển thị trạng thái trống.']),
('5. Mô hình lớp và dữ liệu dự kiến', [
'Transaction: id, type, amountVnd, date, categoryId, note. Số tiền VND dùng số nguyên Long để tránh sai số số thực.',
'Category: id, name, type. Quan hệ: một Category có nhiều Transaction; mỗi Transaction thuộc một Category.',
'MonthlyBudget: id, month, year, limitVnd. Mỗi tháng/năm chỉ có một ngân sách tổng trong phạm vi bản đầu.',
'TransactionRepository: thêm, sửa, xóa, truy vấn giao dịch và tổng hợp số liệu. Cách chia lớp giao diện/ViewModel và lưu trữ sẽ được chốt trước khi triển khai.',
'Class diagram sẽ thể hiện lớp, thuộc tính, phương thức và quan hệ theo mã thực tế. ERD sẽ thể hiện bảng, khóa chính, khóa ngoại và ràng buộc. Không coi mô hình dự kiến này là cấu trúc đã triển khai.']),
('6. Danh sách sơ đồ và cấu trúc báo cáo hoàn chỉnh', [
'Sơ đồ cần vẽ: use case tổng quát; activity/flow cho thêm, sửa, xóa giao dịch và ngân sách; sequence cho lưu giao dịch; class diagram; ERD; sơ đồ điều hướng màn hình.',
'Chương 1: Giới thiệu đề tài, mục tiêu, phạm vi. Chương 2: Cơ sở lý thuyết và công nghệ thực sự sử dụng. Chương 3: Phân tích yêu cầu, đặc tả use case và sơ đồ.',
'Chương 4: Thiết kế giao diện, dữ liệu, kiến trúc và triển khai. Chương 5: Kiểm thử, kết quả và ảnh chụp ứng dụng. Chương 6: Kết luận, hạn chế và hướng phát triển. Cuối báo cáo: tài liệu tham khảo và phụ lục.',
'Các hình sơ đồ sẽ được chèn vào Word kèm số hình, chú thích và mô tả; lưu thêm nguồn sơ đồ để chỉnh sửa. Hiện bản đề cương chưa chứa hình sơ đồ.']),
('7. Kế hoạch kiểm thử và tiến độ', [
'Các ca kiểm thử dự kiến: thêm khoản thu/chi hợp lệ; từ chối số tiền rỗng/âm/bằng 0; sửa/xóa cập nhật tổng; hủy xóa giữ dữ liệu; lọc đúng tháng; vượt ngân sách; mở lại ứng dụng còn dữ liệu; màn hình không có giao dịch.',
'Chưa có kết quả kiểm thử nghiệp vụ. Không ghi Passed khi chưa chạy kiểm thử và thu thập bằng chứng.',
'Trình tự: chốt yêu cầu → thiết kế flow và sơ đồ → thiết kế dữ liệu/giao diện → triển khai từng chức năng → kiểm thử → chụp màn hình và hoàn thiện Word.'])]

sections.insert(2, ('Thông tin đã xác nhận và phân công đề xuất', [
'Nhóm có 4 thành viên. Chưa có mẫu báo cáo bắt buộc; được phép trình bày tự do. Ứng dụng phải có cơ sở dữ liệu.',
'Đề xuất Room trên SQLite để lưu cục bộ: bảng giao dịch, danh mục và ngân sách. Chưa có yêu cầu bắt buộc database online hoặc máy chủ. Công nghệ lưu trữ này chưa được tích hợp vào mã.',
'Thành viên 1: dữ liệu, Room, repository và kiểm thử lưu trữ. Thành viên 2: giao diện và nghiệp vụ thu/chi, danh mục. Thành viên 3: tổng quan, bộ lọc, thống kê và ngân sách. Thành viên 4: tích hợp, kiểm thử hệ thống, tổng hợp Word và slide. Mỗi thành viên cung cấp nội dung báo cáo và bằng chứng cho phần mình phụ trách.',
'Phân công trên là đề xuất, chưa gắn tên người thực hiện. Thời hạn nộp và yêu cầu đăng nhập/API vẫn chưa xác nhận.']))

def p(text, heading=False):
    prop = '<w:pPr><w:pStyle w:val="Heading1"/></w:pPr>' if heading else ''
    return '<w:p>'+prop+'<w:r><w:t xml:space="preserve">'+escape(text)+'</w:t></w:r></w:p>'
body = ''.join(p(title, True)+''.join(p(line) for line in lines) for title,lines in sections)
document = '<?xml version="1.0" encoding="UTF-8"?><w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>'+body+'<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1701"/></w:sectPr></w:body></w:document>'
styles = '''<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="26"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="160" w:line="360" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults><w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:pPr><w:keepNext/><w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:b/><w:sz w:val="30"/></w:rPr></w:style></w:styles>'''
with ZipFile(out/'Bao_cao_QLCT_De_cuong.docx','w',ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml','<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/></Types>')
    z.writestr('_rels/.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>')
    z.writestr('word/document.xml',document)
    z.writestr('word/styles.xml',styles)
    z.writestr('word/_rels/document.xml.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>')
(out/'PROJECT_CONTEXT.txt').write_text('Dự án: QLCT. Thư mục chính: D:\\QLCT.\nNgười dùng học lập trình di động; giao tiếp bằng tiếng Việt.\nĐồ án phải có ứng dụng Android và báo cáo Word (.docx).\nBáo cáo cần flow chức năng, use case diagram, class diagram, ERD, kiểm thử và ảnh chụp thực tế.\nCập nhật báo cáo theo mã; phân biệt thiết kế dự kiến và tính năng đã triển khai. Không bịa kết quả kiểm thử.\nPhạm vi đề xuất: thu/chi, danh mục, tìm kiếm/lọc, ngân sách và thống kê.\nChưa chốt: mẫu trường, hạn nộp, thành viên, đăng nhập, API và online.\nBáo cáo hiện tại là đề cương, chưa có sơ đồ hình hoặc tính năng nghiệp vụ.\n',encoding='utf-8')
from xml.etree import ElementTree as ET
with ZipFile(out/'Bao_cao_QLCT_De_cuong.docx') as z:
    assert z.testzip() is None
    for name in z.namelist(): ET.fromstring(z.read(name))
with (out/'PROJECT_CONTEXT.txt').open('a',encoding='utf-8') as f:
    f.write('\nCập nhật đã xác nhận: nhóm 4 thành viên; chưa có mẫu báo cáo, được trình bày tự do; bắt buộc có database. Đề xuất Room/SQLite cục bộ. Hạn nộp và yêu cầu online/API/đăng nhập chưa xác nhận.\n')
print('DOCX created; ZIP and XML validation passed.')
