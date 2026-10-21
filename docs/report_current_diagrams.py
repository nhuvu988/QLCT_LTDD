"""Diagrams reflecting the four-table schema and current five-button UI."""
from report_diagrams import canvas, node, arrow, save as save_diagram, OUT, plt

def save(fig, name):
    save_diagram(fig, name)
    path = OUT / (name + '.svg')
    path.write_text('\n'.join(line.rstrip() for line in path.read_text(encoding='utf-8').splitlines()) + '\n', encoding='utf-8')

def chain(name, title, labels):
    fig,ax=canvas(title,12,8)
    step=8/len(labels)
    for i,text in enumerate(labels):
        y=8.3-i*step
        node(ax,2.0,y,text,w=8,h=min(.95,step*.72),size=11)
        if i: arrow(ax,(6,y+step),(6,y+min(.95,step*.72)))
    save(fig,name)

def generate():
    chain('21_current_architecture','KIẾN TRÚC MVVM VÀ LUỒNG DỮ LIỆU',[
        'VIEW • Compose Material 3\nAuthScreen / ExpenseScreen / các biểu mẫu',
        'VIEWMODEL • AuthViewModel / ExpenseViewModel\nNhận sự kiện • kiểm soát busy/error • phát UiState',
        'REPOSITORY • AuthRepository / ExpenseRepository\nKiểm tra nghiệp vụ • gắn userId • phối hợp lưu trữ',
        'DAO • AccountDao / ExpenseDao\n@Query • @Insert • @Upsert • Flow',
        'ROOM / SQLITE • qlct.db\nusers • categories • transactions • budgets'])
    chain('22_current_auth','LUỒNG ĐĂNG NHẬP VÀ DỮ LIỆU DEMO',[
        'Khởi tạo danh mục và tài khoản demo bằng DAO (IGNORE)',
        'Nhận tên đăng nhập và mật khẩu\nChuẩn hóa tên • kiểm tra độ dài / ký tự',
        'Tìm tài khoản → PBKDF2 với salt đã lưu\nSo sánh hash; sai → báo lỗi và giữ trang đăng nhập',
        'Đúng → chỉ seed giao dịch cho vu98 nếu demoSeeded = false',
        'Tạo ExpenseRepository theo userId\nMở giao diện và quan sát dữ liệu của tài khoản'])
    chain('23_current_save','LUỒNG THÊM / SỬA GIAO DỊCH',[
        'Người dùng mở nút + hoặc chọn một dòng để sửa',
        'TransactionEditor: loại, tiền, danh mục, ngày, ghi chú\nKhông hợp lệ → hiển thị lỗi, chưa đóng biểu mẫu',
        'ExpenseViewModel.save → mutate\nbusy = true • chống gửi lặp',
        'ExpenseRepository.save kiểm tra lại và gắn userId\nid = 0 → insert; có id → updateOwned',
        'Room ghi SQLite; Flow phát danh sách mới\ncompletedActions tăng → đóng hộp thoại; lỗi → giữ dữ liệu'])
    chain('24_current_search','LUỒNG TRA CỨU VÀ TỔNG HỢP',[
        'ExpenseDao: giao dịch riêng theo userId\nJOIN categories; ORDER BY date DESC, id DESC',
        'summarize: chọn tháng / toàn bộ → tính tổng thu, tổng chi',
        'Lọc ALL / INCOME / EXPENSE → summary.visible',
        'searchTransactions: bỏ dấu, chuyển chữ thường\nMọi từ khóa phải có trong ghi chú hoặc tên danh mục',
        'Hiển thị searchResults ở Giao dịch\nTìm kiếm không thay đổi tổng quan và biểu đồ'])
    chain('25_current_budget','NGÂN SÁCH THÁNG VÀ TRẠNG THÁI CẢNH BÁO',[
        'Chọn tháng → lấy budgets theo (user_id, month)',
        'spent = tổng EXPENSE của riêng tháng đang chọn\nKhông phụ thuộc từ khóa / loại / chế độ toàn bộ',
        'Chưa có hạn mức → NONE; chi < 80% → NORMAL',
        '80% ≤ chi < hạn mức → WARNING\nChi = hạn mức → REACHED; chi > hạn mức → EXCEEDED',
        'Lưu: @Upsert; xóa: chỉ bỏ hạn mức\nGiao dịch và lịch sử chi tiêu vẫn giữ nguyên'])
    chain('26_current_system','MÔ HÌNH TỔNG THỂ HỆ THỐNG QLCT',[
        'NGƯỜI DÙNG\nNhập yêu cầu • xem thông tin • lựa chọn sáng/tối',
        'ỨNG DỤNG ANDROID\nĐăng ký/đăng nhập • Tổng quan • Giao dịch • Thống kê • Ngân sách',
        'XỬ LÝ NGHIỆP VỤ\nKiểm tra đầu vào • quản lý thu/chi • tìm kiếm • tổng hợp • cảnh báo',
        'TRUY CẬP DỮ LIỆU\nRepository • DAO • Room • Flow',
        'CƠ SỞ DỮ LIỆU SQLITE\nusers • categories • transactions • budgets'])
    fig,ax=canvas('SƠ ĐỒ QUAN HỆ CƠ SỞ DỮ LIỆU QLCT',13,10)
    node(ax,.3,5.8,'users\nPK id: Int (tự tăng)\nusername: TEXT, UNIQUE\nsalt: TEXT\npasswordHash: TEXT\ndemoSeeded: BOOLEAN',w=4.3,h=2.6,size=12)
    node(ax,7.0,5.8,'categories\nPK id: Long\nname: TEXT, UNIQUE\n7 danh mục dùng chung',w=4.5,h=2.6,size=12)
    node(ax,.3,1.0,'budgets\nPK/FK user_id: Int\nPK month: TEXT (yyyy-MM)\namount: Long\nPK ghép: (user_id, month)',w=4.3,h=2.7,size=12)
    node(ax,7.0,.7,'transactions\nPK id: Long (tự tăng)\ntype: TEXT; amount: Long\nFK category_id: Long\ndate: TEXT (yyyy-MM-dd)\nnote: TEXT; FK user_id: Int?',w=4.5,h=3.2,size=12)
    arrow(ax,(2.4,5.8),(2.4,3.7),'1 → N')
    arrow(ax,(9.3,5.8),(9.3,3.9),'1 → N')
    arrow(ax,(4.6,6.5),(7.0,3.0),'1 → N (user_id có thể NULL)',route=[(5.8,6.5),(5.8,3.0)])
    ax.text(6,.1,'FK: NO ACTION • Một tài khoản có nhiều giao dịch và nhiều ngân sách tháng',ha='center',fontsize=10)
    save(fig,'27_current_erd')
    fig,ax=canvas('ĐIỀU HƯỚNG • BỐN TRANG VÀ NÚT THÊM Ở GIỮA',13,8)
    node(ax,3.8,8,'Đăng nhập / Đăng ký',w=4.4,h=.8)
    node(ax,3.8,6.4,'ExpenseScreen • sau xác thực',w=4.4,h=.8)
    arrow(ax,(6,8),(6,7.2))
    labels=['Tổng quan\ntab = 0','Giao dịch\ntab = 1','+\nMở biểu mẫu','Thống kê\ntab = 2','Ngân sách\ntab = 3']
    for i,label in enumerate(labels):
        x=.15+i*2.4
        node(ax,x,3.7,label,w=2.1,h=1.2,size=11)
        arrow(ax,(6,6.4),(x+1.05,4.9))
    node(ax,3.6,1.0,'TransactionEditor\nThêm / sửa, không phải tab thứ năm',w=4.8,h=1.1)
    arrow(ax,(6,3.7),(6,2.1))
    save(fig,'28_current_navigation')
    fig,ax=canvas('CÁC NHÓM CHỨC NĂNG CỦA NGƯỜI DÙNG',13,8)
    node(ax,4.2,7.8,'Người dùng trên thiết bị',w=3.6,h=.9,kind='ellipse')
    labels=[('Tài khoản','Đăng ký • Đăng nhập • Đăng xuất'),('Giao dịch','Thêm • Sửa • Xóa • Danh mục • Ngày'),('Tra cứu','Tìm ghi chú / danh mục • Lọc tháng / loại'),('Thống kê','Tổng thu/chi • Biểu đồ vòng và cột'),('Ngân sách','Hạn mức tháng • Cảnh báo • Xóa hạn mức')]
    for i,(title,desc) in enumerate(labels):
        y=6.2-i*1.2
        node(ax,2.1,y,title+'\n'+desc,w=7.8,h=.85,size=11)
        if i==0: arrow(ax,(6,7.8),(6,7.05))
    save(fig,'29_current_features')
    fig,ax=canvas('TỪ DỮ LIỆU THỐNG KÊ ĐẾN BIỂU ĐỒ CANVAS',13,8)
    node(ax,3.4,7.9,'ExpenseSummary • dữ liệu thu/chi',w=5.2,h=.9)
    node(ax,.3,4.4,'CategoryChart\nGom EXPENSE theo categoryId\nTỷ lệ = khoản chi / tổng chi\nGóc = tỷ lệ × 360°\ndrawArc + Stroke',w=5.3,h=2.0,size=12)
    node(ax,6.4,4.4,'SpendingChart\n6 tháng kết thúc ở tháng chọn\nCột cao = chi / max × chiều cao\nKhông có chi → không vẽ cột\ndrawRoundRect + drawLine',w=5.3,h=2.0,size=12)
    arrow(ax,(5,7.9),(3,6.4)); arrow(ax,(7,7.9),(9,6.4))
    node(ax,2.0,1.5,'Canvas là thành phần của Jetpack Compose\nKhông sử dụng thư viện biểu đồ bên ngoài\nLong lưu tiền; Double/Float dùng để tính tỷ lệ vẽ',w=8,h=1.3,size=12)
    arrow(ax,(3,4.4),(4.5,2.8)); arrow(ax,(9,4.4),(7.5,2.8))
    save(fig,'30_current_charts')
    usecase()

def usecase():
    from matplotlib.patches import Rectangle, Circle
    fig,ax=canvas('SƠ ĐỒ USE CASE TỔNG QUÁT',13,10)
    ax.add_patch(Rectangle((3.0,.4),8.7,8.6,fill=False,ec='#7560b5',lw=1.5))
    ax.text(7.35,8.65,'HỆ THỐNG QUẢN LÝ CHI TIÊU CÁ NHÂN',ha='center',fontsize=11,weight='bold')
    ax.add_patch(Circle((1.3,5.8),.25,fill=False,ec='#332b53',lw=1.5))
    ax.plot([1.3,1.3],[4.6,5.55],color='#332b53')
    ax.plot([.7,1.9],[5.2,5.2],color='#332b53')
    ax.plot([.8,1.3,1.8],[3.9,4.6,3.9],color='#332b53')
    ax.text(1.3,3.5,'Người dùng',ha='center',weight='bold')
    labels=['Đăng ký / đăng nhập','Xem tổng quan tài chính','Thêm / sửa / xóa giao dịch',
        'Tìm kiếm và lọc giao dịch','Xem thống kê và biểu đồ','Quản lý ngân sách tháng',
        'Đổi giao diện sáng / tối','Đăng xuất']
    for i,label in enumerate(labels):
        y=7.55-i*.9
        node(ax,4.0,y,label,w=6.7,h=.65,kind='ellipse',size=11)
        ax.plot([1.9,4.0],[5.0,y+.325],color='#b5a9c8',lw=.8)
    save(fig,'31_current_usecase')

if __name__ == '__main__': generate()
