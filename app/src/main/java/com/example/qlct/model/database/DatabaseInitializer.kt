package com.example.qlct.model.database

import androidx.room.withTransaction
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.User

/** Seed through Room DAOs before authentication; safe to repeat on existing databases. */
class DatabaseInitializer(private val database: ExpenseDatabase) {
    suspend fun initialize() {
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
            // Same demo credentials as before. IGNORE preserves an existing ID/password/flag.
            database.accountDao().insertIfMissing(
                User(
                    username = "vu98",
                    salt = "b2ea81dcc8c674ea3914db94b644bf0fae3a677bf0387dbd8bc06a3deea60b92",
                    passwordHash = "a014aeb700922c52bd3cc1cd5ecde76baf01f54ea9454097acb3e45c9fecf021"
                )
            )
        }
    }
}
