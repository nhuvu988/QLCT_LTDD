from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from xml.sax.saxutils import escape
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, Ellipse

out = Path(__file__).parent
def canvas():
    fig, ax = plt.subplots(figsize=(10,7))
    ax.set_xlim(0,10); ax.set_ylim(0,7); ax.axis('off')
    return fig,ax
def box(ax,x,y,w,h,text,ellipse=False):
    ax.add_patch(Ellipse((x+w/2,y+h/2),w,h,fill=False) if ellipse else Rectangle((x,y),w,h,fill=False))
    ax.text(x+w/2,y+h/2,text,ha='center',va='center',fontsize=10)
def arrow(ax,a,b): ax.annotate('',xy=b,xytext=a,arrowprops={'arrowstyle':'->'})
fig,ax=canvas()
box(ax,3,0.4,6,6.1,'')
ax.text(6,6.2,'QLCT — chức năng bản Basic',ha='center',fontsize=13)
ax.plot([1,1],[2.8,3.8],color='black'); ax.add_patch(Ellipse((1,4.1),.4,.4,fill=False))
ax.plot([.5,1.5],[3.5,3.5],color='black'); ax.plot([.5,1,1.5],[2.2,2.8,2.2],color='black')
ax.text(1,1.8,'Người dùng',ha='center')
for y,title in [(5.2,'Thêm giao dịch'),(4.2,'Sửa giao dịch'),(3.2,'Xóa giao dịch'),(2.2,'Lọc loại / tháng'),(1.2,'Xem tổng thu–chi')]:
    box(ax,4.4,y,3.6,.7,title,True); ax.plot([1.6,4.4],[3.5,y+.35],color='gray')
fig.savefig(out/'use_case.png',dpi=170,bbox_inches='tight'); plt.close(fig)
fig,ax=canvas()
for x,y,w,h,title in [(0.2,4.4,4.3,2,'MainActivity\nHiển thị, lọc, biểu mẫu\nrefresh(), edit(), confirmDelete()'),
 (5,4.4,4.7,2,'ExpenseDatabase : SQLiteOpenHelper\nonCreate(), categories(), transactions()\nsave(), delete()'),
 (.2,.6,4.3,2.4,'Transaction\nid: Long; type: String; amount: Long\ncategoryId: Long; date: String\nnote: String; categoryName: String'),
 (5.5,.6,3.8,2.4,'Category\nid: Long\nname: String')]: box(ax,x,y,w,h,title)
arrow(ax,(4.5,5.4),(5,5.4)); arrow(ax,(6.5,4.4),(3,3)); arrow(ax,(7.5,4.4),(7.5,3))
ax.plot([4.5,5.5],[1.7,1.7],color='black'); ax.text(4.6,1.9,'0..*'); ax.text(5.2,1.9,'1')
fig.savefig(out/'class_diagram.png',dpi=170,bbox_inches='tight'); plt.close(fig)
fig,ax=canvas()
for y,title in [(5.7,'Mở biểu mẫu / nhập dữ liệu'),(4.4,'Kiểm tra số tiền và danh mục'),(3.1,'Lưu SQLite trên luồng nền'),(1.8,'Thành công: đóng biểu mẫu'),(.5,'Đọc lại dữ liệu / cập nhật tổng')]:
    box(ax,1,y,5,0.8,title)
for y in [5.7,4.4,3.1,1.8]: arrow(ax,(3.5,y),(3.5,y-.5))
box(ax,7,4.4,2.7,.8,'Không hợp lệ:\nbáo lỗi nhập liệu'); arrow(ax,(6,4.8),(7,4.8))
box(ax,7,3.1,2.7,.8,'Lưu thất bại:\ngiữ biểu mẫu'); arrow(ax,(6,3.5),(7,3.5))
fig.savefig(out/'add_flow.png',dpi=170,bbox_inches='tight'); plt.close(fig)

def p(s,heading=False):
    return '<w:p>'+('<w:pPr><w:pStyle w:val="Heading1"/></w:pPr>' if heading else '')+'<w:r><w:t>'+escape(s)+'</w:t></w:r></w:p>'
body=p('QLCT — Báo cáo triển khai Basic 0.2',True)+p('Nhóm 4 thành viên. Ngày 05/10/2026. Báo cáo tiến độ, chưa phải bản nghiệm thu cuối.')
sections=[('1. Phạm vi đã viết mã',[
'Thêm, sửa, xóa có xác nhận; xem giao dịch theo ngày; lọc thu/chi và tháng hiện tại; tổng thu, tổng chi, chênh lệch theo thời gian.',
'Chọn một trong 7 danh mục có sẵn, ngày bằng DatePicker và ghi chú. Tổng quan theo khoảng thời gian, không thay đổi theo bộ lọc Thu/Chi.',
'Ngân sách, biểu đồ, quản lý danh mục tùy chỉnh, đăng nhập và đồng bộ chưa triển khai.']),
('2. Cơ sở dữ liệu thực tế',[
'Dùng SQLiteOpenHelper của Android, không dùng Room trong phiên bản này. Không cần cài SQLite server hoặc tải database riêng.',
'File qlct.db trong vùng dữ liệu riêng của ứng dụng. SQLiteOpenHelper.onCreate đọc assets/schema.sql chỉ khi tạo database lần đầu.',
'categories(id INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL). transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, amount INTEGER, category_id INTEGER, date TEXT, note TEXT).',
'Một danh mục có nhiều giao dịch. category_id là khóa ngoại đến categories.id. amount từ 1 đến 999999999999 VND; type chỉ INCOME hoặc EXPENSE. Có chỉ mục ngày.',
'SQL dùng tham số khi cập nhật/xóa, insert dùng ContentValues. Hoạt động database chạy trên ExecutorService một luồng.',
'Dữ liệu mẫu: 7 danh mục, 8 giao dịch gắn nhãn Dữ liệu mẫu. Thu 4.200.000 VND, chi 1.345.000 VND, chênh lệch 2.855.000 VND. Ngày mẫu thuộc tháng khởi tạo theo giờ máy.',
'Không nạp lại mẫu khi khởi động hoặc sau khi xóa hết giao dịch. Xóa dữ liệu ứng dụng/gỡ cài đặt sẽ mất database; cài mới sẽ tạo lại mẫu.']),
('3. Đặc tả use case',[
'Actor: Người dùng. UC01 Thêm giao dịch: mở biểu mẫu, chọn Thu/Chi, số tiền, danh mục, ngày, ghi chú, Lưu. Không hợp lệ thì giữ biểu mẫu và báo lỗi; lưu thành công mới đóng và cập nhật số liệu.',
'UC02 Sửa: chạm giao dịch, chọn Sửa, chỉnh dữ liệu, kiểm tra và lưu. UC03 Xóa: chạm giao dịch, chọn Xóa và xác nhận; hủy không thay đổi dữ liệu.',
'UC04 Lọc: chọn loại giao dịch và bật/tắt tháng hiện tại. UC05 Tổng quan: tổng hợp tất cả giao dịch trong thời gian đã chọn.']),
('4. Kiểm thử và giới hạn',[
'Đã chạy kiểm thử SQLite trên máy tính với chính schema.sql: 8 bản ghi mẫu, tổng tiền, số tiền 0/âm/vượt giới hạn, loại không hợp lệ, khóa ngoại sai, thêm/sửa/xóa, truy vấn ngày, dữ liệu sau mở lại và trạng thái rỗng — đạt.',
'Đây là kiểm thử hợp đồng SQL trên SQLite máy tính, không thay thế kiểm thử Android hoặc kiểm thử giao diện.',
'Chưa có thiết bị Android kết nối tại thời điểm kiểm tra. Chưa xác nhận trực quan dialog, xoay màn hình, bàn phím hoặc hoạt động trên thiết bị thật.',
'Biểu mẫu đang nhập chưa được phục hồi khi Activity bị tái tạo. Danh sách bản đầu nạp toàn bộ giao dịch, phù hợp dữ liệu nhỏ; cần phân trang nếu mở rộng.']),
('5. Nguồn tham khảo',[
'Android Developers — Save data using SQLite: https://developer.android.com/training/data-storage/sqlite',
'Android Developers — SQLiteOpenHelper: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper'])]
for title,lines in sections:
    body+=p(title,True)+''.join(p(s) for s in lines)
with ZipFile('D:/QLCT/docs/Bao_cao_QLCT_De_cuong.docx') as source:
    styles=source.read('word/styles.xml')
rels=['<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>']
images=['use_case.png','class_diagram.png','add_flow.png']
for i,(name,caption) in enumerate(zip(images,['Use case của bản Basic','Class diagram theo mã hiện tại','Luồng thêm giao dịch']),2):
    body+=p(caption,True)
    body+=f'''<w:p><w:r><w:drawing><wp:inline xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"><wp:extent cx="5486400" cy="3840480"/><wp:docPr id="{i}" name="{name}"/><a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:nvPicPr><pic:cNvPr id="{i}" name="{name}"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="rId{i}"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="5486400" cy="3840480"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>'''
    rels.append(f'<Relationship Id="rId{i}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/{name}"/>')
with ZipFile(out/'Bao_cao_QLCT_Basic.docx','w',ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml','<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Default Extension="png" ContentType="image/png"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/></Types>')
    z.writestr('_rels/.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>')
    z.writestr('word/document.xml','<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><w:body>'+body+'<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134"/></w:sectPr></w:body></w:document>')
    z.writestr('word/styles.xml',styles)
    z.writestr('word/_rels/document.xml.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'+''.join(rels)+'</Relationships>')
    for name in images: z.write(out/name,'word/media/'+name)
from xml.etree import ElementTree as ET
with ZipFile(out/'Bao_cao_QLCT_Basic.docx') as z:
    assert z.testzip() is None
    for name in z.namelist():
        if not name.endswith('.png'): ET.fromstring(z.read(name))
print('Basic report and 3 diagrams generated; XML/ZIP validated.')
