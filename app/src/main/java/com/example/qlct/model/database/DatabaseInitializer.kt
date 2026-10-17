package com.example.qlct.model.database

import androidx.room.withTransaction
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.LocalAccount

/** Khởi tạo qua Room DAO trước xác thực; có thể chạy lại trên database đã có dữ liệu. */
class DatabaseInitializer(private val database: ExpenseDatabase) {
    /**
     * Nạp 7 danh mục và tài khoản vu98 trong cùng một transaction Room.
     * Có thể gọi lại trước mỗi lần xác thực vì các lệnh chèn đều bỏ qua dữ liệu đã có.
     */
    suspend fun initialize() {
        // Transaction bảo đảm danh mục và tài khoản mặc định cùng hoàn tất hoặc cùng bị hoàn tác.
        database.withTransaction {
            database.dao().insertCategoriesIfMissing(
                listOf(
                    Category(1, "Ăn uống"),
                    Category(2, "Di chuyển"),
                    Category(3, "Học tập"),
                    Category(4, "Mua sắm"),
                    Category(5, "Sinh hoạt"),
                    Category(6, "Lương / Trợ cấp"),
                    Category(7, "Khác")
                )
            )
            // Giữ thông tin demo hiện có; IGNORE bảo toàn ID, mật khẩu và cờ của tài khoản đã tồn tại.
            database.accountDao().insertIfMissing(
                LocalAccount(
                    username = "vu98",
                    salt = "b2ea81dcc8c674ea3914db94b644bf0fae3a677bf0387dbd8bc06a3deea60b92",
                    passwordHash = "a014aeb700922c52bd3cc1cd5ecde76baf01f54ea9454097acb3e45c9fecf021"
                )
            )
        }
    }
}
