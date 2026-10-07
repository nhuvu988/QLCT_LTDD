"""Editable, code-native diagrams for the current Compose/MVVM/Room report."""
from pathlib import Path
import sys, os
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / '.gradle/report-libs'))
os.environ.setdefault('MPLCONFIGDIR', str(ROOT / '.gradle/matplotlib-report'))
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, Polygon, Rectangle

OUT = ROOT / 'docs/diagrams/current'
OUT.mkdir(parents=True, exist_ok=True)
plt.rcParams.update({'font.family': 'DejaVu Sans', 'font.size': 10})
INK = '#332b53'; PURPLE = '#7560b5'; LIGHT = '#f3f0fa'; GREEN = '#e5f2ed'

def canvas(title, width=12, height=9):
    fig, ax = plt.subplots(figsize=(width, height))
    ax.set_xlim(0, 12); ax.set_ylim(0, 10); ax.axis('off')
    ax.text(6, 9.7, title, ha='center', va='center', fontsize=15, weight='bold', color=INK)
    return fig, ax

def node(ax, x, y, text, w=3.4, h=.72, kind='box', fill=LIGHT, size=10):
    if kind == 'decision':
        patch = Polygon([(x,y+h/2),(x+w/2,y+h),(x+w,y+h/2),(x+w/2,y)], fc=fill, ec=PURPLE, lw=1.4)
    elif kind == 'ellipse': patch = Ellipse((x+w/2,y+h/2),w,h,fc=fill,ec=PURPLE,lw=1.4)
    else: patch = FancyBboxPatch((x,y),w,h,boxstyle='round,pad=0.025,rounding_size=0.08',fc=fill,ec=PURPLE,lw=1.4)
    ax.add_patch(patch)
    ax.text(x+w/2,y+h/2,text,ha='center',va='center',fontsize=size,color=INK)
    return (x,y,w,h)

def arrow(ax, a, b, label='', dash=False, route=None):
    if route:
        points=[a,*route,b]
        for p,q in zip(points[:-2],points[1:-1]): ax.plot([p[0],q[0]],[p[1],q[1]],color=PURPLE,lw=1.2,ls='--' if dash else '-')
        a=points[-2]
    ax.annotate('',xy=b,xytext=a,arrowprops={'arrowstyle':'->','color':PURPLE,'lw':1.25,'linestyle':'--' if dash else '-'})
    if label: ax.text((a[0]+b[0])/2,(a[1]+b[1])/2+.1,label,fontsize=9,ha='center',color=INK,bbox={'fc':'white','ec':'none','pad':1})

def save(fig, name):
    fig.savefig(OUT / (name+'.png'),dpi=210,bbox_inches='tight',facecolor='white')
    fig.savefig(OUT / (name+'.svg'),bbox_inches='tight',facecolor='white')
    plt.close(fig)

def usecase():
    fig,ax=canvas('SƠ ĐỒ USE CASE — QLCT',12,9)
    ax.add_patch(Rectangle((3.0,.4),8.6,8.6,fill=False,ec=INK,lw=1.5))
    ax.text(7.3,8.75,'Ứng dụng quản lý chi tiêu cá nhân',ha='center',weight='bold',color=INK)
    ax.add_patch(Ellipse((1.25,5.3),.44,.44,fill=False,ec=INK))
    ax.plot([1.25,1.25],[4.3,5.08],color=INK); ax.plot([.65,1.85],[4.85,4.85],color=INK)
    ax.plot([.75,1.25,1.75],[3.7,4.3,3.7],color=INK)
    ax.text(1.25,3.35,'Người dùng',ha='center',weight='bold',color=INK)
    labels=['UC01 Xem tổng quan','UC02 Thêm giao dịch','UC03 Xem danh sách','UC04 Sửa giao dịch','UC05 Xóa giao dịch','UC06 Chọn thời gian','UC07 Lọc Thu / Chi','UC08 Phân bổ danh mục','UC09 Chi tiêu theo tháng']
    for i,label in enumerate(labels):
        y=7.7-i*.8
        node(ax,3.65,y,label,w=3.8,h=.57,kind='ellipse',size=10)
        ax.plot([1.85,3.65],[4.65,y+.285],color='#bbb6c8',lw=.8)
    node(ax,8.35,6.8,'Kiểm tra dữ liệu',w=2.7,h=.65,kind='ellipse',size=9)
    arrow(ax,(7.45,7.185),(8.35,7.125),'«include»',True)
    arrow(ax,(7.45,5.585),(9.0,6.8),'«include»',True)
    node(ax,8.35,4.7,'Xác nhận xóa',w=2.7,h=.65,kind='ellipse',size=9)
    arrow(ax,(7.45,4.785),(8.35,5.025),'«include»',True)
    ax.text(9.7,2.25,'Một actor duy nhất.\nKhông có đăng nhập,\nquản trị viên hoặc\nngân sách trong bản này.',ha='center',color=INK,fontsize=10)
    save(fig,'01_use_case')

def flow(name,title,actions,decisions=None):
    fig,ax=canvas(title,11,11)
    ys=[8.45-i*1.02 for i in range(len(actions))]
    for i,(text,kind) in enumerate(actions):
        node(ax,2.7,ys[i],text,w=5.3,h=.72 if kind!='decision' else .85,kind=kind,size=10)
        if i: arrow(ax,(5.35,ys[i-1]),(5.35,ys[i]+(.85 if kind=='decision' else .72)))
    for index,text,target in decisions or []:
        y=ys[index]
        node(ax,8.7,y,text,w=2.8,h=.85,fill='#fff0e8',size=9)
        arrow(ax,(8,y+.42),(8.7,y+.42),'Không')
        if target is not None:
            arrow(ax,(11.5,y+.42),(8,ys[target]+.35),'',route=[(11.8,y+.42),(11.8,ys[target]+.35)])
        if index+1<len(actions): ax.text(5.6,y-.18,'Có',fontsize=9,color=INK)
    save(fig,name)

def architecture():
    fig,ax=canvas('KIẾN TRÚC MVVM VÀ DÒNG DỮ LIỆU',12,8)
    boxes=[(1,7.3,'VIEW\nMainActivity • ExpenseScreen\nDashboardComponents • TransactionEditor'),(1,5.35,'VIEWMODEL\nExpenseViewModel • ExpenseUiState\nSavedStateHandle • StateFlow'),(1,3.4,'MODEL / DATA\nExpenseRepository • AmountValidator\nExpenseDao • Category • TransactionEntity'),(1,1.45,'LƯU TRỮ\nExpenseDatabase : RoomDatabase\nSQLite cục bộ: qlct.db (version 2)')]
    for x,y,t in boxes: node(ax,x,y,t,w=7,h=1.15,size=11)
    for i in range(3):
        arrow(ax,(3.1,boxes[i][1]),(3.1,boxes[i+1][1]+1.15),'Sự kiện / lệnh')
        arrow(ax,(6.15,boxes[i+1][1]+1.15),(6.15,boxes[i][1]),'Trạng thái / Flow')
    node(ax,8.6,5.35,'summarize()\nTổng thu / chi\nDanh sách đã lọc\nBiểu đồ 6 tháng\nPhân bổ danh mục',w=2.8,h=2.2,size=10)
    arrow(ax,(8,5.9),(8.6,6.1))
    ax.text(6,.65,'Room → Flow → combine → StateFlow → collectAsStateWithLifecycle → Compose',ha='center',fontsize=10,color=INK)
    save(fig,'07_mvvm_architecture')

def classes():
    fig,ax=canvas('SƠ ĐỒ LỚP — CÁC LỚP CHÍNH THEO MÃ NGUỒN',14,10)
    def cls(x,y,w,h,title,body):
        node(ax,x,y,'',w,h)
        ax.plot([x,x+w],[y+h-.42,y+h-.42],color=PURPLE,lw=1)
        ax.text(x+w/2,y+h-.21,title,ha='center',weight='bold',fontsize=10,color=INK)
        ax.text(x+.12,y+h-.56,body,va='top',fontsize=8.5,color=INK,linespacing=1.5)
    cls(.2,6.8,3.5,2.35,'MainActivity : ComponentActivity','+ onCreate(Bundle)\n+ setContent { ExpenseScreen(...) }\n+ khởi tạo repository / VM factory')
    cls(4.2,6.1,4.2,3.05,'ExpenseViewModel : ViewModel','- repository: ExpenseRepository\n- savedState: SavedStateHandle\n+ state: StateFlow<ExpenseUiState>\n+ busy, actionError, completedActions\n+ save(t), delete(t)\n+ changeMonth(delta), setMonthOnly(v)\n+ setType(v), retryLoad(), clearError()')
    cls(8.9,6.65,2.9,2.5,'ExpenseUiState','loading, categories\nmonth, monthOnly, type\nsummary, loadError')
    cls(.2,3.75,3.5,2.25,'ExpenseRepository','- dao: ExpenseDao\n+ categories, transactions: Flow\n+ save(TransactionEntity)\n+ delete(id: Long)')
    cls(4.2,3.0,4.2,2.4,'«interface» ExpenseDao','+ observeCategories(): Flow<List<...>>\n+ observeTransactions(): Flow<List<...>>\n+ insert(t): Long [suspend]\n+ update(t): Int [suspend]\n+ delete(id): Int [suspend]')
    cls(8.9,3.15,2.9,2.2,'ExpenseDatabase','RoomDatabase; version=2\n+ dao(): ExpenseDao\n+ getInstance(context)\n+ MIGRATION_1_2')
    cls(.2,.45,3.5,2.15,'AmountValidator','MAX_AMOUNT: Long\n+ errorForValue(Long): String?\n+ errorForInput(String): String?')
    cls(4.2,.45,4.2,1.8,'TransactionEntity','id, type, amount, categoryId\ndate, note\n«Room Entity» transactions')
    cls(8.9,.45,2.9,1.8,'Category','id: Long\nname: String\n«Room Entity» categories')
    arrow(ax,(3.7,8),(4.2,8),'tạo / cung cấp')
    arrow(ax,(4.2,6.7),(3.7,5.1),'gọi',route=[(3.95,6.7),(3.95,5.1)])
    arrow(ax,(8.4,7.9),(8.9,7.9),'cung cấp')
    arrow(ax,(3.7,4.85),(4.2,4.85),'gọi DAO')
    arrow(ax,(1.95,3.75),(1.95,2.6),'validate')
    arrow(ax,(8.9,4.25),(8.4,4.25),'dao()')
    arrow(ax,(6.3,3),(6.3,2.25),'CRUD')
    ax.plot([8.4,8.9],[1.3,1.3],color=PURPLE); ax.text(8.42,1.48,'0..*',fontsize=8); ax.text(8.76,1.48,'1',fontsize=8)
    save(fig,'08_class_diagram')

def erd():
    fig,ax=canvas('ERD — CƠ SỞ DỮ LIỆU ROOM VERSION 2',12,7)
    node(ax,.5,3.7,'',w=4.1,h=4.4)
    node(ax,7.1,1.6,'',w=4.4,h=6.5)
    ax.text(2.55,7.7,'categories',ha='center',weight='bold',fontsize=15,color=INK)
    ax.text(.8,7.05,'PK  id: INTEGER NOT NULL\n     name: TEXT NOT NULL\n\nUNIQUE INDEX\nindex_categories_name(name)',va='top',fontsize=11,color=INK,linespacing=1.8)
    ax.text(9.3,7.7,'transactions',ha='center',weight='bold',fontsize=15,color=INK)
    ax.text(7.4,7.05,'PK  id: INTEGER, AUTOINCREMENT\n     type: TEXT NOT NULL\n     amount: INTEGER NOT NULL\nFK  category_id: INTEGER NOT NULL\n     date: TEXT NOT NULL\n     note: TEXT NOT NULL DEFAULT \'\'\n\nINDEX transactions_date(date)\nINDEX index_transactions_category_id\n      (category_id)',va='top',fontsize=10,color=INK,linespacing=1.65)
    ax.plot([4.6,7.1],[5.2,5.2],color=PURPLE,lw=1.5)
    ax.plot([4.8,4.8],[5.0,5.4],color=PURPLE,lw=1.5)
    ax.plot([6.8,7.1],[5.2,5.5],color=PURPLE); ax.plot([6.8,7.1],[5.2,4.9],color=PURPLE)
    ax.text(4.85,5.48,'1',color=INK); ax.text(6.2,5.48,'0..*',color=INK)
    ax.text(5.85,4.3,'category_id → id\nON DELETE NO ACTION\nON UPDATE NO ACTION',ha='center',fontsize=9,color=INK)
    ax.text(2.65,2.15,'Một giao dịch thuộc đúng\nmột danh mục. Một danh mục\ncó thể chưa có giao dịch.',ha='center',fontsize=11,color=INK)
    ax.text(6,.65,'amount > 0 và type ∈ {INCOME, EXPENSE}: kiểm tra ở ứng dụng; không có SQL CHECK trong schema v2.',ha='center',fontsize=9,color=INK)
    save(fig,'09_database_erd')

def sequence():
    fig,ax=canvas('SEQUENCE — THÊM / SỬA GIAO DỊCH THÀNH CÔNG',14,10)
    xs=[.8,3,5.3,7.6,10.2]
    names=['Người dùng','Compose View','ViewModel','Repository','Room / DAO']
    for x,n in zip(xs,names):
        node(ax,x-.65,8.7,n,w=1.8,h=.5,size=9)
        ax.plot([x+.25,x+.25],[.7,8.7],color='#b5aec7',ls='--',lw=1)
    steps=[(0,1,'1. Nhập dữ liệu, nhấn Lưu'),(1,1,'2. Validate biểu mẫu'),(1,2,'3. save(TransactionEntity)'),(2,2,'4. busy = true; viewModelScope.launch'),(2,3,'5. repository.save(t)'),(3,3,'6. Validate và trim ghi chú'),(3,4,'7. insert(t) hoặc update(t)'),(4,3,'8. Kết quả ghi database'),(3,2,'9. Hoàn tất / không có exception'),(2,1,'10. completedActions++; busy=false'),(4,2,'11. Flow phát danh sách mới'),(2,1,'12. state: tổng + danh sách + biểu đồ')]
    for i,(a,b,label) in enumerate(steps):
        y=8.05-i*.56
        if a==b:
            x=xs[a]+.25
            arrow(ax,(x,y),(x,y-.18),'',route=[(x+.9,y),(x+.9,y-.18)])
            ax.text(x+.13,y+.11,label,fontsize=8,color=INK)
        else:
            arrow(ax,(xs[a]+.25,y),(xs[b]+.25,y),dash=i in (7,8,9,10,11))
            ax.text((xs[a]+xs[b])/2+.25,y+.13,label,ha='center',fontsize=8,color=INK)
    ax.text(6,.65,'Luồng phát Flow và cập nhật Compose diễn ra bất đồng bộ; sơ đồ diễn tả quan hệ nhân quả, không cam kết thứ tự thời gian tuyệt đối.',ha='center',fontsize=8,color=INK)
    save(fig,'10_sequence_save')

def navigation():
    fig,ax=canvas('ĐIỀU HƯỚNG MÀN HÌNH VÀ HỘP THOẠI',12,7)
    node(ax,4.1,8,'Mở ứng dụng / MainActivity',w=3.8,h=.75)
    node(ax,4.1,6.45,'Thanh điều hướng dưới',w=3.8,h=.75)
    arrow(ax,(6,8),(6,7.2))
    for x,title,detail in [(0.35,'Tổng quan','Tổng thu / chi\nBiểu đồ vòng + cột\n4 giao dịch gần đây'),(4.25,'Giao dịch','Danh sách đầy đủ\nBộ lọc Thu / Chi\nChạm dòng để sửa'),(8.15,'Thống kê','Biểu đồ vòng + cột\nSố tiền chi tiết\nTheo thời gian đã chọn')]:
        node(ax,x,3.75,title+'\n\n'+detail,w=3.5,h=1.8,size=10)
        arrow(ax,(6,6.45),(x+1.75,5.55))
    node(ax,.55,1.5,'Nút +: thêm giao dịch\nHộp thoại Compose',w=3.2,h=1.1,size=10)
    node(ax,4.4,1.5,'Chạm giao dịch: sửa\nCùng biểu mẫu thêm',w=3.2,h=1.1,size=10)
    node(ax,8.25,1.5,'Nút thùng rác\nHộp thoại xác nhận xóa',w=3.2,h=1.1,size=10)
    arrow(ax,(6,3.75),(6,2.6))
    ax.text(6,.6,'Điều hướng bằng trạng thái tab (0/1/2), không sử dụng NavController hoặc nhiều Activity.',ha='center',fontsize=10,color=INK)
    save(fig,'11_navigation')

def generate():
    usecase()
    flow('02_flow_add','FLOW — THÊM GIAO DỊCH',[
        ('Bắt đầu: nhấn nút +','ellipse'),('Chọn Thu/Chi, nhập tiền, danh mục, ngày, ghi chú','box'),('Nhấn Lưu; dữ liệu hợp lệ?','decision'),('busy=true; gọi ViewModel → Repository','box'),('DAO.insert(); ghi thành công?','decision'),('completedActions++; đóng biểu mẫu','box'),('Flow mới → cập nhật danh sách và thống kê','box'),('Kết thúc','ellipse')],[(2,'Hiện lỗi tại ô nhập;\ngiữ nội dung',1),(4,'actionError; busy=false;\ngiữ biểu mẫu để thử lại',1)])
    flow('03_flow_edit','FLOW — SỬA GIAO DỊCH',[
        ('Chạm một dòng giao dịch','ellipse'),('Nạp dữ liệu cũ vào biểu mẫu','box'),('Sửa thông tin; nhấn Lưu','box'),('Dữ liệu hợp lệ?','decision'),('DAO.update(id); đúng 1 dòng?','decision'),('Đóng biểu mẫu; Flow tính lại thống kê','box'),('Kết thúc','ellipse')],[(3,'Báo lỗi nhập liệu;\ngiữ dữ liệu chỉnh sửa',2),(4,'Báo lỗi; không giả định\ncập nhật thành công',2)])
    flow('04_flow_delete','FLOW — XÓA GIAO DỊCH',[
        ('Nhấn biểu tượng thùng rác','ellipse'),('Hiển thị danh mục và số tiền cần xóa','box'),('Người dùng xác nhận Xóa?','decision'),('busy=true; Repository.delete(id)','box'),('DAO.delete(): đúng 1 dòng?','decision'),('Flow mới; cập nhật tổng và danh sách','box'),('Kết thúc','ellipse')],[(2,'Hủy / đóng dialog:\nkhông thay đổi dữ liệu',None),(4,'Báo lỗi; mở lại nút\nđể người dùng thử lại',None)])
    flow('05_flow_validation','FLOW — KIỂM TRA SỐ TIỀN',[
        ('Nhận chuỗi input; trim()','ellipse'),('Chuỗi không rỗng?','decision'),('toLongOrNull() thành công?','decision'),('Giá trị lớn hơn 0?','decision'),('Giá trị ≤ 999.999.999.999?','decision'),('Hợp lệ: trả null (không có lỗi)','box'),('Gửi số tiền dương tới Repository','ellipse')],[(1,'Vui lòng nhập số tiền',None),(2,'Nhập số tiền nguyên\nhợp lệ (VND)',None),(3,'Số tiền phải lớn hơn 0',None),(4,'Số tiền vượt mức tối đa',None)])
    flow('06_flow_statistics','FLOW — LỌC VÀ TÍNH THỐNG KÊ',[
        ('Room phát danh sách giao dịch','ellipse'),('Đọc month, monthOnly, type từ SavedStateHandle','box'),('Tạo period theo tháng hoặc toàn bộ','box'),('Tính income / expense; groupBy categoryId','box'),('Tạo visible = period lọc theo loại','box'),('Tạo chart 6 tháng từ toàn bộ rows, chỉ EXPENSE','box'),('ExpenseUiState → StateFlow → Compose','box'),('Hiển thị trạng thái có dữ liệu hoặc rỗng','ellipse')])
    architecture(); classes(); erd(); sequence(); navigation()
    flow('12_flow_migration','FLOW — KHỞI TẠO VÀ MIGRATION DATABASE',[
        ('Room mở qlct.db','ellipse'),('Database đã tồn tại?','decision'),('Database cũ version=1?','decision'),('Sao chép 2 bảng vào bảng backup','box'),('Tạo bảng Room v2; chép lại dữ liệu; xóa backup','box'),('Room xác nhận schema v2','box'),('DAO sẵn sàng / Flow phát dữ liệu','ellipse')],[(1,'Tạo schema v2;\nonCreate nạp seed.sql',5),(2,'Nếu version=2:\nmở bình thường',5)])
    print('Generated 12 diagrams as PNG and editable SVG; sources: docs/report_diagrams.py')

if __name__ == '__main__': generate()
