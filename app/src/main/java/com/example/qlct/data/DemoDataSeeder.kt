package com.example.qlct.data

import androidx.room.withTransaction
import java.time.LocalDate

/** Explicit demo data for vu98 only. The completion flag survives logout/restarts. */
class DemoDataSeeder(private val database: ExpenseDatabase) {
    suspend fun seed(account: LocalAccount) {
        if (account.username != "vu98") return
        database.withTransaction {
            val current = database.accountDao().find("vu98") ?: return@withTransaction
            if (current.demoSeeded) return@withTransaction
            val today = LocalDate.now()
            val rows = listOf(
                Triple(45000L, 1L, "Demo vu98: ăn trưa"),
                Triple(70000L, 2L, "Demo vu98: đổ xăng"),
                Triple(150000L, 3L, "Demo vu98: mua sách"),
                Triple(250000L, 4L, "Demo vu98: mua đồ dùng"),
                Triple(800000L, 5L, "Demo vu98: tiền phòng"),
                Triple(30000L, 1L, "Demo vu98: ăn sáng")
            )
            val repository = ExpenseRepository(database.dao(), current.id)
            rows.forEachIndexed { index, (amount, category, note) ->
                repository.save(TransactionEntity(type = "EXPENSE", amount = amount,
                    categoryId = category, date = today.minusDays(index.toLong()).toString(), note = note))
            }
            database.openHelper.writableDatabase.execSQL("UPDATE local_account SET demoSeeded=1 WHERE id=?", arrayOf(current.id))
        }
    }
}
