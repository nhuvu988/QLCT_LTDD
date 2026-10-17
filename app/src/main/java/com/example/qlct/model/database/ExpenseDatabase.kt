package com.example.qlct.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.qlct.model.dao.AccountDao
import com.example.qlct.model.dao.ExpenseDao
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.LocalAccount
import com.example.qlct.model.entity.MonthlyBudget
import com.example.qlct.model.entity.TransactionEntity

// Room quản lý 4 bảng ở schema version 6; exportSchema xuất JSON phục vụ kiểm tra migration.
@Database(entities = [Category::class, TransactionEntity::class, LocalAccount::class, MonthlyBudget::class], version = 6, exportSchema = true)
abstract class ExpenseDatabase : RoomDatabase() {
    /**
     * Cung cấp DAO thao tác danh mục, giao dịch và ngân sách do Room sinh mã triển khai.
     */
    abstract fun dao(): ExpenseDao
    /**
     * Cung cấp DAO đọc và ghi tài khoản cục bộ.
     */
    abstract fun accountDao(): AccountDao

    companion object {
        // Volatile bảo đảm các luồng khác nhìn thấy instance mới; synchronized bên dưới bảo vệ lúc khởi tạo.
        @Volatile private var instance: ExpenseDatabase? = null

        // Bản SQLiteOpenHelper cũ dùng qlct.db phiên bản 1. Dựng lại các bảng
        // theo quy định null và index của Room, đồng thời giữ lại các dòng dữ liệu.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            /**
             * Nâng v1 lên v2: dựng lại bảng và index theo schema Room, sao lưu rồi chép lại dữ liệu cũ.
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE categories_backup AS SELECT * FROM categories")
                db.execSQL("CREATE TABLE transactions_backup AS SELECT * FROM transactions")
                db.execSQL("DROP TABLE transactions")
                db.execSQL("DROP TABLE categories")
                db.execSQL("CREATE TABLE categories (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
                db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, category_id INTEGER NOT NULL, date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '', FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX transactions_date ON transactions(date)")
                db.execSQL("CREATE INDEX index_transactions_category_id ON transactions(category_id)")
                db.execSQL("INSERT INTO categories SELECT * FROM categories_backup")
                db.execSQL("INSERT INTO transactions SELECT * FROM transactions_backup")
                db.execSQL("DROP TABLE transactions_backup")
                db.execSQL("DROP TABLE categories_backup")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            /**
             * Nâng v2 lên v3: thêm bảng tài khoản cục bộ để lưu tên, salt và hash mật khẩu.
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS local_account (id INTEGER NOT NULL PRIMARY KEY, username TEXT NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            /**
             * Nâng v3 lên v4: cho phép nhiều tài khoản, thêm user_id và gán giao dịch cá nhân cũ cho tài khoản 1 nếu có.
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE local_account_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL)")
                db.execSQL("INSERT INTO local_account_new SELECT * FROM local_account")
                db.execSQL("DROP TABLE local_account")
                db.execSQL("ALTER TABLE local_account_new RENAME TO local_account")
                db.execSQL("CREATE UNIQUE INDEX index_local_account_username ON local_account(username)")
                db.execSQL("ALTER TABLE transactions ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX index_transactions_user_id ON transactions(user_id)")
                // Giữ dữ liệu cũ: tài khoản thiết bị trước đây giữ các dòng cá nhân;
                // các dòng được đánh dấu mẫu không được gán vào tài khoản để tránh cộng nhầm tiền.
                db.execSQL("UPDATE transactions SET user_id=1 WHERE EXISTS (SELECT 1 FROM local_account WHERE id=1) AND note NOT IN ('Dữ liệu mẫu: trợ cấp tháng','Dữ liệu mẫu: làm thêm','Dữ liệu mẫu: ăn trưa','Dữ liệu mẫu: đổ xăng','Dữ liệu mẫu: sách','Dữ liệu mẫu: đồ dùng','Dữ liệu mẫu: tiền phòng','Dữ liệu mẫu: ăn sáng')")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            /**
             * Nâng v4 lên v5: thêm cờ demoSeeded, khóa ngoại tài khoản và đổi chủ sở hữu không tồn tại thành null.
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_account ADD COLUMN demoSeeded INTEGER NOT NULL DEFAULT 0")
                // Dựng bảng giao dịch mới có khóa ngoại; khi sao chép, user_id không tồn tại sẽ được chuyển thành NULL.
                db.execSQL("CREATE TABLE transactions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, category_id INTEGER NOT NULL, date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '', user_id INTEGER, FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(user_id) REFERENCES local_account(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("INSERT INTO transactions_new SELECT id,type,amount,category_id,date,note,CASE WHEN user_id IN (SELECT id FROM local_account) THEN user_id ELSE NULL END FROM transactions")
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                db.execSQL("CREATE INDEX transactions_date ON transactions(date)")
                db.execSQL("CREATE INDEX index_transactions_category_id ON transactions(category_id)")
                db.execSQL("CREATE INDEX index_transactions_user_id ON transactions(user_id)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            /**
             * Nâng v5 lên v6: thêm bảng ngân sách với khóa ghép user_id/tháng, giữ nguyên các bảng dữ liệu cũ.
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS budgets (user_id INTEGER NOT NULL, month TEXT NOT NULL, amount INTEGER NOT NULL, PRIMARY KEY(user_id, month), FOREIGN KEY(user_id) REFERENCES local_account(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            }
        }

        /**
         * Lấy một kết nối Room dùng chung trong tiến trình ứng dụng.
         * synchronized ngăn tạo trùng khi nhiều luồng truy cập; applicationContext tránh giữ tham chiếu Activity.
         */
        fun getInstance(context: Context): ExpenseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, ExpenseDatabase::class.java, "qlct.db")
                // Đăng ký đầy đủ chuỗi nâng cấp để Room chuyển schema cũ lên v6 và giữ dữ liệu hiện có.
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build().also { instance = it }
        }
    }
}
