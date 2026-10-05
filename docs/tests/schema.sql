CREATE TABLE categories (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE);
CREATE TABLE transactions (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 type TEXT NOT NULL CHECK(type IN ('INCOME','EXPENSE')),
 amount INTEGER NOT NULL CHECK(amount > 0 AND amount <= 999999999999),
 category_id INTEGER NOT NULL REFERENCES categories(id),
 date TEXT NOT NULL,
 note TEXT NOT NULL DEFAULT ''
);
CREATE INDEX transactions_date ON transactions(date);
INSERT INTO categories VALUES (1,'Ăn uống'),(2,'Di chuyển'),(3,'Học tập'),(4,'Mua sắm'),(5,'Sinh hoạt'),(6,'Lương / Trợ cấp'),(7,'Khác');
INSERT INTO transactions(type,amount,category_id,date,note) VALUES
 ('INCOME',3000000,6,date('now','localtime','start of month'),'Dữ liệu mẫu: trợ cấp tháng'),
 ('INCOME',1200000,6,date('now','localtime'),'Dữ liệu mẫu: làm thêm'),
 ('EXPENSE',45000,1,date('now','localtime'),'Dữ liệu mẫu: ăn trưa'),
 ('EXPENSE',70000,2,date('now','localtime'),'Dữ liệu mẫu: đổ xăng'),
 ('EXPENSE',150000,3,date('now','localtime'),'Dữ liệu mẫu: sách'),
 ('EXPENSE',250000,4,date('now','localtime'),'Dữ liệu mẫu: đồ dùng'),
 ('EXPENSE',800000,5,date('now','localtime','start of month'),'Dữ liệu mẫu: tiền phòng'),
 ('EXPENSE',30000,1,date('now','localtime'),'Dữ liệu mẫu: ăn sáng');
