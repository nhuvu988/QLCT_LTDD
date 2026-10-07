"""Architecture, database, algorithms, flows, and UI implementation."""
import json
from pathlib import Path

def write_design(r):
    root=Path(__file__).resolve().parents[1]
    r.chapter('CHƯƠNG 4. THIẾT KẾ KIẾN TRÚC VÀ CƠ SỞ DỮ LIỆU')
    r.h('4.1. Kiến trúc tổng thể')
    r.p('Ứng dụng triển khai theo kiến trúc MVVM ở quy mô một module app. MainActivity là điểm vào, kế thừa ComponentActivity và gọi setContent. Activity tạo ExpenseRepository từ DAO của singleton ExpenseDatabase, rồi cung cấp factory để Compose lấy ExpenseViewModel. Các thành phần này tạo một luồng phụ thuộc từ giao diện xuống dữ liệu, trong khi dữ liệu quan sát đi theo hướng ngược lại.')
    r.image('docs/diagrams/current/07_mvvm_architecture.png','Kiến trúc MVVM và hai chiều sự kiện – trạng thái')
    r.p('Đường ghi dữ liệu: người dùng → TransactionEditor → ExpenseViewModel → ExpenseRepository → ExpenseDao → SQLite. Đường cập nhật màn hình: Room nhận thay đổi → Flow danh mục/giao dịch → combine với bộ lọc → summarize → StateFlow<ExpenseUiState> → collectAsStateWithLifecycle → Compose. Việc tách đường lệnh và trạng thái giúp tránh cập nhật số tổng trực tiếp ở từng nút.')
    r.h('4.2. Trách nhiệm của từng thành phần')
    r.table('Trách nhiệm lớp và thành phần giao diện',['Tên','Trách nhiệm','Không chịu trách nhiệm'],[
        ('MainActivity','Khởi tạo dependency, setContent, theme, edge-to-edge','Không validate tiền; không viết truy vấn SQL.'),
        ('ExpenseScreen','Tab, danh sách, loading/error, editor/delete dialog','Không trực tiếp insert/update/delete database.'),
        ('DashboardComponents','Header, thẻ, donut, cột, dòng giao dịch, icon, navigation','Không quản lý phiên bản database.'),
        ('TransactionEditor','State nhập, chọn loại/ngày/danh mục, validate ở UI','Không tự sinh ID cho bản ghi mới.'),
        ('ExpenseViewModel','StateFlow, bộ lọc, busy, gọi repository, lỗi, hoàn tất','Không vẽ biểu đồ bằng Canvas.'),
        ('ExpenseRepository','Validate và chuẩn hóa; quyết định insert/update; kiểm tra số dòng','Không điều khiển đóng dialog.'),
        ('AmountValidator','Quy tắc tiền đầu vào và giá trị Long','Không gọi Room hoặc biết tab nào đang mở.'),
        ('ExpenseDao','Quan sát danh sách/danh mục; insert/update/delete','Không quyết định màu, hình biểu đồ hoặc nhãn tiếng Việt.'),
        ('ExpenseDatabase','Singleton, schema, callback seed, migration','Không giữ số tiền đang nhập trên biểu mẫu.')],[3.4,7.0,5.6])
    r.image('docs/diagrams/current/08_class_diagram.png','Sơ đồ lớp chính và quan hệ phụ thuộc trong mã hiện tại')
    r.p('TransactionEntity là đối tượng ánh xạ bảng; TransactionRow là kết quả truy vấn JOIN có thêm categoryName để giao diện không phải tự ghép tên. CategoryExpense và MonthlyExpense là mô hình dữ liệu cho thống kê, không phải các bảng lưu trữ. ExpenseSummary chứa kết quả tổng hợp; ExpenseUiState gói dữ liệu cùng trạng thái loading và lỗi tải.')
    r.h('4.3. Thiết kế trạng thái giao diện')
    r.table('Các trường ExpenseUiState',['Trường','Kiểu / mặc định','Ý nghĩa'],[
        ('loading','Boolean / true','Đang đợi dữ liệu ban đầu.'),('categories','List<Category> / emptyList','Danh mục để hiển thị và chọn khi thêm/sửa.'),('month','YearMonth / now()','Mốc tháng của phạm vi và cuối biểu đồ cột.'),('monthOnly','Boolean / true','true: chỉ period tháng; false: toàn bộ giao dịch.'),('type','String / ALL','ALL, INCOME hoặc EXPENSE cho danh sách visible.'),('summary','ExpenseSummary / 0 và các list rỗng','income, expense, visible, chart và categoryExpenses.'),('loadError','String? / null','Thông báo lỗi đọc dữ liệu khi Flow phát lỗi.')],[3.0,5.0,8.0])
    r.table('Các trạng thái thao tác và vị trí lưu',['Trạng thái','Nơi lưu','Hành vi'],[
        ('month, monthOnly, type','SavedStateHandle trong ViewModel','Cập nhật bằng changeMonth / setMonthOnly / setType.'),('busy','MutableStateFlow trong ViewModel','Chặn thao tác ghi tiếp theo; disable nút khi đang ghi.'),('actionError','MutableStateFlow<String?>','Hiện thông báo lỗi ghi; giữ dữ liệu nhập.'),('completedActions','MutableStateFlow<Long>','Tăng sau save/delete thành công; UI đóng dialog.'),('tab, editorOpen, editingId, deletingId','rememberSaveable trong ExpenseScreen','Giữ lựa chọn trang và hộp thoại qua tái tạo state được hỗ trợ.'),('type, amount, categoryId, date, note, amountChecked','rememberSaveable trong TransactionEditor','Giữ nội dung nhập và trạng thái validate.')],[5.2,5.2,5.6])
    r.p('Trạng thái lỗi tải và lỗi ghi được tách biệt. Khi đọc lỗi, state xuất ra loadError và có nút Thử lại; retryLoad tăng một giá trị MutableStateFlow để flatMapLatest đăng ký lại các luồng. Khi ghi lỗi, ViewModel giữ actionError và trả busy=false trong finally, cho phép người dùng thử lại. CancellationException được ném lại thay vì xử lý như lỗi nghiệp vụ thông thường.')
    r.p('Các flow busy, actionError và completedActions hiện được công khai dưới dạng MutableStateFlow; có thể cải tiến bằng cách chỉ công khai StateFlow và giữ nguồn mutable private. Đây là cải tiến về đóng gói, không phải chức năng đã có thêm trong phiên bản báo cáo.')
    r.h('4.4. Mô hình quan hệ dữ liệu')
    r.p('Database có đúng hai bảng nghiệp vụ: categories và transactions. Một danh mục có từ 0 đến nhiều giao dịch; mỗi giao dịch bắt buộc tham chiếu một danh mục tồn tại. ID của danh mục được seed cố định, còn ID giao dịch được SQLite sinh tự động khi thêm mới.')
    r.image('docs/diagrams/current/09_database_erd.png','ERD theo schema Room version 2')
    r.h('4.4.1. Từ điển bảng categories',3)
    r.table('Từ điển dữ liệu categories',['Cột','Kotlin / SQLite','Ràng buộc','Ý nghĩa / ví dụ'],[
        ('id','Long / INTEGER','PRIMARY KEY, NOT NULL; không autoGenerate','Mã danh mục; ví dụ 1 = Ăn uống.'),('name','String / TEXT','NOT NULL; UNIQUE qua index_categories_name','Tên hiển thị; ví dụ Di chuyển.')],[2.0,3.8,5.3,4.9])
    r.p('Bảng categories không có cột type. Vì vậy trong ứng dụng hiện tại danh mục không bị khóa riêng cho Thu hoặc Chi; người dùng có thể chọn bất kỳ danh mục nào trong bảy danh mục đã nạp. Không mô tả tính năng tự lọc danh mục theo loại vì mã chưa triển khai hành vi đó.')
    r.h('4.4.2. Từ điển bảng transactions',3)
    r.table('Từ điển dữ liệu transactions',['Cột','Kotlin / SQLite','Ràng buộc','Ý nghĩa / ví dụ'],[
        ('id','Long / INTEGER','PK, AUTOINCREMENT, NOT NULL','0 ở entity trước insert; database sinh ID dương.'),('type','String / TEXT','NOT NULL; kiểm tra giá trị tại repository','INCOME hoặc EXPENSE.'),('amount','Long / INTEGER','NOT NULL; kiểm tra 1..999999999999 trong ứng dụng','45000 VND; không lưu -45000.'),('category_id','Long / INTEGER','NOT NULL; FK → categories.id','1 liên kết danh mục Ăn uống.'),('date','String / TEXT','NOT NULL; LocalDate.parse tại repository','yyyy-MM-dd, ví dụ 2026-10-06.'),('note','String / TEXT','NOT NULL DEFAULT \'\'; trim khi lưu','“Tiền ăn trưa” hoặc chuỗi rỗng.')],[2.2,3.3,5.6,4.9])
    r.h('4.4.3. Khóa và chỉ mục',3)
    r.table('Các chỉ mục và hành động khóa ngoại',['Tên / quan hệ','Định nghĩa','Mục đích / hệ quả'],[
        ('index_categories_name','UNIQUE(name)','Tên danh mục không trùng ở mức database.'),('transactions_date','INDEX(date)','Hỗ trợ truy vấn/sắp xếp ngày; bộ lọc tháng hiện tại vẫn tính trong bộ nhớ.'),('index_transactions_category_id','INDEX(category_id)','Hỗ trợ JOIN và kiểm tra tham chiếu danh mục.'),('FK transactions.category_id','REFERENCES categories(id)','Bản ghi phải tham chiếu danh mục tồn tại.'),('ON DELETE / UPDATE','NO ACTION / NO ACTION','Không cascade giao dịch khi xóa/đổi ID danh mục; hiện UI không cung cấp CRUD danh mục.')],[4.4,4.7,6.9])
    r.h('4.5. Phân biệt ràng buộc database và validate ứng dụng')
    r.p('Schema Room v2 có PK, FK, UNIQUE, NOT NULL và DEFAULT của note. Nó không chứa CHECK(amount > 0) hoặc CHECK(type IN (...)) như bảng SQLiteOpenHelper v1. Vì vậy phải mô tả đúng: số tiền dương và loại hợp lệ được bảo vệ bằng AmountValidator/ExpenseRepository; việc gọi thẳng DAO bỏ qua repository có thể vượt các quy tắc nghiệp vụ này.')
    r.p('Trong luồng UI bình thường, bước kiểm tra diễn ra hai lần với mục đích khác nhau. UI kiểm tra chuỗi để báo lỗi dễ hiểu ngay tại trường nhập; repository kiểm tra giá trị đã parse để mọi thao tác lưu của ứng dụng áp dụng cùng quy tắc. Khóa ngoại là lớp bảo vệ bổ sung ở database nếu categoryId không tồn tại. Ngày là TEXT nên SQLite không tự xác nhận yyyy-MM-dd; kiểm tra ngày cũng ở repository.')
    r.p('Để mở rộng khả năng chống dữ liệu không hợp lệ qua các đường ghi mới, nhóm có thể cân nhắc thiết kế trigger hoặc ràng buộc ở database kèm migration. Báo cáo không coi phương án mở rộng này đã được triển khai. Các API ghi hiện tại phải đi qua repository thay vì để UI truy cập DAO trực tiếp.')
    r.h('4.6. DDL và truy vấn theo schema xuất từ KSP')
    schema=json.loads((root/'app/schemas/com.example.qlct.data.ExpenseDatabase/2.json').read_text(encoding='utf-8'))['database']
    for entity in schema['entities']:
        r.h(f'4.6.{1 if entity["tableName"]=="categories" else 2}. Tạo bảng {entity["tableName"]}',3)
        sql=entity['createSql'].replace('${TABLE_NAME}',entity['tableName'])
        sql=sql.replace(' (',' (\n  ',1).replace(', `',',\n  `').replace(', FOREIGN',',\n  FOREIGN')
        r.code(sql+';')
        for idx in entity.get('indices',[]): r.code(idx['createSql'].replace('${TABLE_NAME}',entity['tableName'])+';')
    r.h('4.6.3. Truy vấn đọc danh sách',3)
    r.code('SELECT t.*, c.name AS categoryName\nFROM transactions t\nJOIN categories c ON c.id = t.category_id\nORDER BY t.date DESC, t.id DESC;')
    r.p('DAO ánh xạ kết quả sang TransactionRow. JOIN bảo đảm giao diện nhận được categoryName cùng transaction. Quy tắc thứ tự ưu tiên ngày, sau đó ID, nghĩa là giao dịch vừa thêm cùng ngày thường đứng trước bản ghi cùng ngày có ID nhỏ hơn. Đây không phải thứ tự ghi chú hay tên danh mục.')
    r.h('4.6.4. Ghi dữ liệu và truy vấn tham số',3)
    r.code('@Insert suspend fun insert(transaction: TransactionEntity): Long\n@Update suspend fun update(transaction: TransactionEntity): Int\n@Query("DELETE FROM transactions WHERE id = :id")\nsuspend fun delete(id: Long): Int')
    r.p('Room sinh truy vấn insert/update từ annotation; phương thức delete dùng tham số :id thay vì nối chuỗi SQL từ người dùng. Ghi chú có dấu nháy không trở thành câu lệnh SQL vì là dữ liệu được bind. Repository yêu cầu update/delete tác động đúng một dòng để tránh báo thành công khi ID không còn tồn tại.')
    r.h('4.7. Dữ liệu khởi tạo và ví dụ tính toán')
    r.table('Bảy danh mục có sẵn',['ID','Tên danh mục'],[(str(i),name) for i,name in enumerate(['Ăn uống','Di chuyển','Học tập','Mua sắm','Sinh hoạt','Lương / Trợ cấp','Khác'],1)],[2,14])
    r.table('Tám giao dịch mẫu khi tạo database lần đầu',['Loại','Số tiền (VND)','Danh mục','Ghi chú'],[
        ('INCOME','3.000.000','Lương / Trợ cấp','Dữ liệu mẫu: trợ cấp tháng'),('INCOME','1.200.000','Lương / Trợ cấp','Dữ liệu mẫu: làm thêm'),('EXPENSE','45.000','Ăn uống','Dữ liệu mẫu: ăn trưa'),('EXPENSE','70.000','Di chuyển','Dữ liệu mẫu: đổ xăng'),('EXPENSE','150.000','Học tập','Dữ liệu mẫu: sách'),('EXPENSE','250.000','Mua sắm','Dữ liệu mẫu: đồ dùng'),('EXPENSE','800.000','Sinh hoạt','Dữ liệu mẫu: tiền phòng'),('EXPENSE','30.000','Ăn uống','Dữ liệu mẫu: ăn sáng')],[2.1,3.1,4.0,6.8])
    r.p('Các ngày mẫu được gắn theo ngày và đầu tháng hiện tại của thiết bị tại lần tạo database. Vì thế giá trị tổng mẫu thuộc tháng khởi tạo, không phải tháng cố định trong mã. Tổng thu mẫu = 4.200.000 VND; tổng chi mẫu = 1.345.000 VND; chênh lệch = 2.855.000 VND. Sau khi người dùng thêm/sửa/xóa, tổng thực tế có thể khác các giá trị mẫu này.')
    r.p('onCreate đọc assets/seed.sql, tách câu lệnh theo dấu chấm phẩy và chạy các INSERT cố định của ứng dụng. Đây không phải cơ chế chạy file SQL do người dùng cung cấp. Callback chỉ chạy khi tạo database mới; mở lại hoặc xóa hết giao dịch không tự nạp lại tám bản ghi mẫu.')

    r.h('4.7.1. Diễn giải INSERT danh mục trong seed.sql',3)
    r.p('INSERT INTO categories VALUES tạo bảy danh mục ID 1–7: Ăn uống, Di chuyển, Học tập, Mua sắm, Sinh hoạt, Lương / Trợ cấp và Khác. Danh mục tạo trước giao dịch vì category_id có FK tới categories.id. Mã 6 dùng cho hai khoản thu mẫu; mã 1–5 dùng cho chi.')
    r.p('Giao diện cho chọn tên nhưng database lưu ID; DAO JOIN lấy categoryName khi đọc. Không chép tên vào mỗi giao dịch. Phiên bản hiện tại chưa có CRUD danh mục trên giao diện.')
    r.h('4.7.2. Diễn giải INSERT giao dịch và tổng mẫu',3)
    r.p('INSERT INTO transactions(type,amount,category_id,date,note) cung cấp năm cột; bỏ id để SQLite tự sinh. type quy định thu/chi; amount là VND nguyên dương; category_id nối danh mục; date ghi ngày; note diễn giải khoản tiền.')
    r.p('Trợ cấp là INCOME 3000000, danh mục 6, ngày đầu tháng; làm thêm là INCOME 1200000, danh mục 6, ngày hiện tại. Tổng thu 4.200.000. Sáu EXPENSE mô phỏng ăn trưa, xăng, sách, đồ dùng, phòng và ăn sáng, tổng chi 1.345.000. Chênh lệch là 2.855.000 VND.')
    r.p('Ăn trưa và ăn sáng cùng ID danh mục 1 nên nhóm Ăn uống là 75.000. Di chuyển 70.000; Học tập 150.000; Mua sắm 250.000; Sinh hoạt 800.000. Tổng nhóm bằng tổng chi; hai khoản thu không vào donut. Ghi chú “Dữ liệu mẫu:” nhận diện dữ liệu giả lập, không phải khảo sát hay tài chính thật của thành viên.')
    r.h('4.7.3. Ngày khởi tạo và quan hệ với chart tháng',3)
    r.p("date('now','localtime') lấy ngày theo giờ địa phương khi tạo database; thêm 'start of month' lấy ngày đầu tháng. seed.sql không cố định 10/2026: tạo database tháng khác sẽ sinh tám dòng trong tháng mới.")
    r.p('Database mới có tám dòng thuộc tháng khởi tạo. Tổng thu/chi/chênh lệch là 4.200.000/1.345.000/2.855.000. Chart có cột tháng đó 1.345.000 và các tháng trước 0 nếu chưa có dữ liệu. Sau thao tác của người dùng, giao diện theo dữ liệu thực, không bắt buộc bằng số mẫu.')
    r.p('Room callback onCreate nạp seed một lần khi tạo database. Mở lại, chuyển tab, xóa hết giao dịch hay migration không nạp lại mẫu, tránh trùng và không ghi đè lịch sử. Giao dịch về sau đi qua View → ViewModel → Repository → DAO → Room.')
    r.h('4.8. Thiết kế migration version 1 sang version 2')
    r.image('docs/diagrams/current/12_flow_migration.png','Luồng mở database mới, đã có version 2 hoặc migration version 1')
    r.steps(['Room kiểm tra schema/database khi mở qlct.db. Với bản cũ user_version=1, chạy MIGRATION_1_2.', 'CREATE TABLE ..._backup AS SELECT * sao chép categories và transactions.', 'DROP transactions trước categories để giữ trật tự xử lý quan hệ tham chiếu.', 'Tạo lại categories và transactions theo nullability, default, FK và chỉ mục của Room v2.', 'INSERT INTO categories SELECT ... rồi INSERT INTO transactions SELECT ... để phục hồi các ID và dữ liệu.', 'Xóa hai bảng backup. Room kiểm tra schema mới và quản lý identity hash của version 2.', 'Mở DAO và phát Flow; không chạy seed.sql cho database đang nâng cấp.'])
    r.p('Bảng backup ở đây là các bảng trung gian trong quá trình migration, không phải file sao lưu bên ngoài cho người dùng. Room quản lý migration trong cơ chế nâng cấp database của nó. Khi không tồn tại đường migration cần thiết, ứng dụng báo lỗi mở dữ liệu thay vì tự xóa database. Hiện chỉ có đường 1→2; mọi nâng version trong tương lai phải bổ sung migration và kiểm thử tương ứng.')
    r.p('Kiểm thử hiện có xác nhận dữ liệu và ID đang tồn tại được giữ nguyên, ghi chú có nội dung đặc biệt không mất, database rỗng không bị seed lại, schema sau migration đúng cột/chỉ mục/FK/default. Báo cáo không khẳng định bảo toàn mọi thông tin phụ trợ của SQLite hoặc đã thử mọi tình huống bị ngắt điện giữa migration.')
    r.h('4.9. Bảo vệ dữ liệu và các giới hạn lưu trữ')
    r.p('File database nằm trong sandbox ứng dụng; QLCT không có chức năng gửi dữ liệu ra máy chủ. Manifest hiện dùng allowBackup=false, nhưng lint cảnh báo về dataExtractionRules trên Android mới; do đó không diễn đạt rằng mọi cơ chế chuyển dữ liệu giữa thiết bị đều đã được cấu hình hoàn chỉnh. Dữ liệu chưa được mã hóa bằng SQLCipher và không có cơ chế xuất file sao lưu trong giao diện.')
    r.p('Cài APK cập nhật với cùng applicationId và chữ ký có thể giữ dữ liệu ứng dụng. Gỡ cài đặt hoặc dùng Clear storage sẽ xóa dữ liệu riêng; khi cài/chạy lại, database mới được tạo và seed được nạp. Các file database thiết bị, khóa ký, local.properties và cache build không được đưa vào Git.')

    r.chapter('CHƯƠNG 5. TRIỂN KHAI CHỨC NĂNG VÀ GIAO DIỆN')
    r.h('5.1. Thiết kế điều hướng và ngôn ngữ giao diện')
    r.p('Thiết kế giao diện dựa trên phong cách người dùng cung cấp: header tím gradient, các thẻ sáng bo góc, biểu đồ vòng nhiều màu, biểu đồ cột và thanh điều hướng dưới. Giao diện dùng tên chức năng tiếng Việt thay vì chỉ icon để người dùng dễ nhận biết. Icon được vẽ bằng Canvas nên không cần tải ảnh từ mạng.')
    r.image('docs/diagrams/current/11_navigation.png','Điều hướng ba tab và các hộp thoại của QLCT')
    r.table('Thành phần giao diện và tương tác',['Thành phần','Dữ liệu hiển thị','Thao tác'],[
        ('DashboardHeader','Tháng/năm, chênh lệch, tổng thu, tổng chi','Nút trước/sau; chuyển Theo tháng/Toàn bộ.'),('CategoryChart','Vòng phân bổ, tên danh mục, %; số tiền chi tiết ở Thống kê','Xem dữ liệu theo period; chưa có drill-down khi chạm cung.'),('SpendingChart','Cột 6 tháng, nhãn tháng/năm; số VND chi tiết ở Thống kê','Đổi mốc bằng nút tháng; chưa có tooltip tương tác.'),('TransactionTile','Icon, tên, ngày, note rút gọn, tiền, thùng rác','Chạm dòng để sửa; thùng rác để yêu cầu xóa.'),('BottomNavigation','Tổng quan, Giao dịch, +, Thống kê','Chọn tab hoặc mở thêm giao dịch.'),('TransactionEditor','Thu/Chi, tiền, danh mục, ngày, note','Nhập/sửa và Lưu/Hủy; lỗi inline tại tiền.')],[4.0,6.8,5.2])
    r.h('5.2. Triển khai thêm giao dịch')
    r.p('Khi nút + được nhấn, editingId được đặt null và editorOpen=true. Điều kiện cho phép mở là dữ liệu đã tải, không loadError, danh mục không rỗng và không busy. TransactionEditor khởi tạo default từ existing hoặc giá trị mới. Trong trường hợp thêm, ID truyền vào TransactionEntity là 0 để Room sinh ID thay vì dùng ID do UI tự chọn.')
    r.image('docs/diagrams/current/02_flow_add.png','Flow thêm giao dịch với nhánh dữ liệu sai và ghi thất bại')
    r.p('Nhấn Lưu đặt amountChecked=true. Nếu AmountValidator.errorForInput trả lỗi, hệ thống đánh dấu isError ở OutlinedTextField và hiện supportingText cụ thể. Biểu mẫu không đóng và không gọi model.save(). Khi tiền hợp lệ nhưng danh mục không còn trong categories, hiện thông báo chọn danh mục. Chỉ khi các kiểm tra UI đạt mới gửi TransactionEntity tới ViewModel.')
    r.p('mutate() kiểm tra busy để chặn gọi lặp, sau đó chạy action trong viewModelScope.launch. Repository thực hiện bước kiểm tra nghiệp vụ thứ hai. Nếu insert thành công, completedActions tăng; finally đưa busy về false. LaunchedEffect ở ExpenseScreen đóng editor; Flow mới tính lại các vùng hiển thị. Nếu thao tác phát exception, actionError hiện thông báo tổng quát và dữ liệu nhập được giữ để thử lại.')
    r.h('5.3. Triển khai sửa giao dịch')
    r.image('docs/diagrams/current/03_flow_edit.png','Flow sửa giao dịch và kiểm tra bản ghi còn tồn tại')
    r.p('Chạm dòng lưu editingId rồi tìm giao dịch trong state.summary.visible. Cùng TransactionEditor được sử dụng cho thêm và sửa để tránh hai bộ validate khác nhau. Khi existing không null, biểu mẫu nạp ID và các trường cũ. Lưu tạo entity với đúng ID cũ; repository gọi update thay vì insert và kiểm tra số dòng trả về bằng 1.')
    r.p('Người dùng có thể chuyển một khoản chi thành thu hoặc đổi ngày sang tháng khác. Tổng chi và tổng thu sẽ thay đổi tương ứng; dòng giao dịch có thể không còn xuất hiện do bộ lọc đang chọn. Hành vi này được xác định bằng dữ liệu sau update, không phải lỗi mất dữ liệu. Khi người dùng hủy, những thay đổi chưa lưu chỉ tồn tại ở state nhập và không update Room.')
    r.h('5.4. Triển khai xóa có xác nhận')
    r.image('docs/diagrams/current/04_flow_delete.png','Flow xóa với nhánh hủy và lỗi ghi')
    r.p('Nút thùng rác tách biệt thao tác xóa với chạm sửa. Dialog hiển thị danh mục và tiền để người dùng nhận diện bản ghi. Hủy chỉ đóng dialog. Xóa mới gọi ViewModel.delete(transaction), lấy transaction.id và thực thi DAO query có tham số. Repository dùng check để bảo đảm một bản ghi được tác động.')
    r.p('Khi xóa thành công, completedActions khiến dialog đóng. Flow loại giao dịch khỏi kết quả đọc và summarize tính lại tổng, donut và cột. Không giảm tổng tiền bằng cách trừ trực tiếp trong onClick, nên các trường hợp khác thời gian hoặc đổi lọc vẫn theo cùng thuật toán. Hiện chưa có Undo hoặc thùng rác khôi phục; dialog đã thông báo tính không thể hoàn tác.')
    r.h('5.5. Validate số tiền và bảo vệ đầu vào')
    r.image('docs/diagrams/current/05_flow_validation.png','Flow validate số tiền từ chuỗi nhập tới Long hợp lệ')
    r.table('Thông báo validate theo dữ liệu đầu vào',['Đầu vào','Kết quả','Thông báo / hành vi'],[
        ('Rỗng hoặc chỉ khoảng trắng','Không hợp lệ','Vui lòng nhập số tiền.'),('abc; 1.5; 1,5; chuỗi vượt Long','Không hợp lệ','Nhập số tiền nguyên hợp lệ (VND).'),('-1; -50000; 0; -0','Không hợp lệ','Số tiền phải lớn hơn 0.'),('1000000000000','Không hợp lệ','Số tiền tối đa là 999.999.999.999 đ.'),('1; 50000; 999999999999','Hợp lệ','Không có lỗi; có thể lưu nếu các trường khác hợp lệ.'),('Khoảng trắng quanh 50000','Hợp lệ sau trim','Chuỗi được trim trước parse.')],[5.0,3.0,8.0])
    r.code('fun errorForValue(value: Long): String? = when {\n    value <= 0 -> "Số tiền phải lớn hơn 0"\n    value > MAX_AMOUNT -> "Số tiền tối đa là 999.999.999.999 đ"\n    else -> null\n}')
    r.p('Không đổi tiền âm thành trị tuyệt đối, vì cách đó có thể che giấu việc nhập sai. Người dùng nhập giá trị dương rồi chọn Thu hoặc Chi. Ví dụ chi 50.000 VND phải nhập 50000 và chọn Chi tiền. Giao diện hiển thị “−50.000 đ”, nhưng database giữ amount=50000 và type=EXPENSE. Quy tắc này giúp công thức sumOf đơn giản và tránh trường hợp một số âm bị trừ hai lần.')
    r.p('Bàn phím số được yêu cầu bằng KeyboardOptions(keyboardType=Number), nhưng keyboardType không thay thế validate: bàn phím vật lý, paste hoặc một IME khác có thể cung cấp ký tự không mong muốn. Vì vậy vẫn parse và kiểm tra trước khi lưu. Khi người dùng đã nhấn Lưu và tiếp tục sửa amount, lỗi được tính lại theo chuỗi hiện tại, không bắt buộc đóng/mở dialog.')
    r.h('5.6. Tổng hợp, lọc và các công thức thống kê')
    r.image('docs/diagrams/current/06_flow_statistics.png','Flow tính period, visible, donut và chart')
    r.h('5.6.1. Xác định period và danh sách visible',3)
    r.code('period = rows.filter { !monthOnly || it.date.startsWith(month + "-") }\nvisible = period.filter { type == "ALL" || it.type == type }')
    r.p('Tiền được tổng hợp từ period trước bước type filter. Nhờ đó khi chọn Thu, danh sách chỉ hiện khoản thu nhưng thẻ Tổng chi vẫn phản ánh chi thực của kỳ. Trên tab Tổng quan, giao dịch gần đây dùng visible.take(4), vì vậy cũng tôn trọng loại lọc đang giữ từ tab Giao dịch. Nếu muốn xem toàn bộ dòng, chọn Tất cả.')
    r.h('5.6.2. Tổng thu, tổng chi và chênh lệch',3)
    r.code('income  = Σ amount của t ∈ period có t.type = INCOME\nexpense = Σ amount của t ∈ period có t.type = EXPENSE\ndifference = income − expense')
    r.p('Với dữ liệu mẫu: income=3.000.000+1.200.000=4.200.000; expense=45.000+70.000+150.000+250.000+800.000+30.000=1.345.000; difference=2.855.000. Nếu tổng chi lớn hơn tổng thu, chênh lệch âm được hiển thị hợp lệ. Ứng dụng chưa biết số dư tiền mặt ban đầu hoặc tài sản khác, nên chênh lệch này không được gọi là số dư toàn bộ tài sản.')
    r.h('5.6.3. Biểu đồ vòng',3)
    r.code('categoryAmount[c] = Σ amount của EXPENSE trong period có categoryId = c\nratio[c] = categoryAmount[c] / expense\nsweepAngle[c] = ratio[c] × 360°\npercentLabel[c] = floor(ratio[c] × 100)')
    r.p('Các nhóm được sắp giảm dần theo amount. Vòng vẽ đủ tỷ lệ từ số tiền thực; nhãn phần trăm dùng số nguyên làm tròn xuống. Với dữ liệu mẫu, phần nhãn có thể là 59%, 18%, 11%, 5%, 5% nên tổng nhãn là 98%; đây là hệ quả cách hiển thị chứ không mất tiền. Tab Thống kê có số tiền chính xác để đối chiếu. Một cải tiến có thể là hiển thị một chữ số thập phân hoặc phân bổ phần dư khi làm tròn.')
    r.h('5.6.4. Biểu đồ cột sáu tháng',3)
    r.code('months = [M−5, M−4, M−3, M−2, M−1, M]\nmonthlyExpense[m] = Σ amount của rows có type=EXPENSE và tháng(date)=m\nbarHeight[m] = monthlyExpense[m] / max(1, maxMonthlyExpense) × chartHeight')
    r.p('Biểu đồ cột lấy toàn bộ rows làm nguồn, không lấy visible hoặc chỉ period. Nếu M là 10/2026, sáu tháng là 5/2026 đến 10/2026. Đổi sang Toàn bộ chỉ ảnh hưởng period của tổng quan/donut/danh sách; chart vẫn là sáu tháng kết thúc tại M. Cách tách này phải được giải thích khi bảo vệ để tránh nhận định sai rằng bộ lọc đang gây lỗi biểu đồ.')
    r.p('Các phép chia tỷ lệ dùng Double/Float ở bước vẽ để chuẩn hóa độ cao/góc; tổng tiền vẫn giữ Long. Khi tất cả số tiền bằng 0, max được giữ ít nhất 1 để tránh phép chia 0. Với số lượng giao dịch cực lớn, phép sumOf Long vẫn có nguy cơ overflow; ứng dụng hiện chưa có kiểm tra tràn tổng và chưa có benchmark dữ liệu lớn.')
    r.h('5.7. Sequence ghi dữ liệu và cập nhật UI')
    r.image('docs/diagrams/current/10_sequence_save.png','Sequence save từ View tới Room và chiều cập nhật Flow')
    r.p('Sequence mô tả sự phối hợp giữa lớp, không phải giao tiếp mạng. Khi action hoàn tất và khi Flow phát giá trị mới là các sự kiện bất đồng bộ; không nên khẳng định cả hai xảy ra theo thứ tự thời gian tuyệt đối như một lời gọi đồng bộ duy nhất. Chỉ đóng dialog sau khi repository hoàn tất; dữ liệu hiển thị mới đến từ Room.')
    r.h('5.8. Xử lý trạng thái rỗng, lỗi và thao tác đồng thời')
    r.p('Loading ban đầu: hiển thị spinner. LoadError: thông báo không tải được dữ liệu và nút Thử lại. Danh sách không có giao dịch: hiển thị thẻ gợi ý nút +. Donut không có khoản chi: vòng nền và lời nhắc. Biểu đồ cột không có khoản chi: thông báo giai đoạn rỗng. Các trạng thái này có ý nghĩa khác nhau; lỗi database không được mô tả là người dùng chưa tạo giao dịch.')
    r.p('Trong khi ghi, busy ngăn nhiều yêu cầu save/delete chạy cùng lúc; các nút lưu/hủy hoặc hành động liên quan bị disable theo trạng thái. Khi thao tác thất bại, nội dung nhập vẫn ở rememberSaveable và busy được giải phóng trong finally. Thông báo lỗi ghi hiện mang tính tổng quát; bản hiện tại chưa có mã lỗi riêng cho disk full, database locked hoặc ID mất.')
    r.h('5.9. Ảnh chụp giao diện thực tế')
    r.p('Các ảnh dưới đây được chụp từ máy ảo QLCT_API_35 đang chạy APK của project, không phải mockup. Giá trị giao dịch trong ảnh phản ánh dữ liệu của máy ảo tại thời điểm chụp. Ảnh validation là trạng thái đã kiểm tra nhập -50000; không có giao dịch âm được lưu.')
    pairs=[
        ('5.9.1. Tổng quan và lịch sử','overview_current.png','Màn hình Tổng quan với tổng thu/chi và hai biểu đồ','transactions.png','Màn hình Giao dịch, thứ tự ngày/ID và các thao tác'),
        ('5.9.2. Thống kê và biểu mẫu thêm','statistics.png','Thống kê với số tiền theo danh mục','add_transaction.png','Biểu mẫu thêm giao dịch bằng Compose'),
        ('5.9.3. Chọn danh mục và ngày','category_picker.png','DropdownMenu chọn một trong 7 danh mục','date_picker.png','DatePickerDialog chọn ngày giao dịch'),
        ('5.9.4. Nhập liệu và lỗi số tiền','keyboard_input.png','Bàn phím số và ô nhập giao dịch trên máy ảo','amount_validation.png','Lỗi inline khi nhập số tiền âm, biểu mẫu được giữ')]
    for title,left,lc,right,rc in pairs:
        r.doc.add_page_break(); r.h(title)
        r.p('Ảnh chụp trực tiếp trên máy ảo Pixel 6 / Android 15 (API 35).')
        r.pair_images('docs/screenshots/'+left,lc,'docs/screenshots/'+right,rc)
