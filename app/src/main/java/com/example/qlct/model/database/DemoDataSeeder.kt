package com.example.qlct.model.database

import androidx.room.withTransaction
import com.example.qlct.model.entity.LocalAccount
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.repository.ExpenseRepository
import java.time.LocalDate

/** Dữ liệu demo chỉ dành cho vu98; cờ hoàn tất được giữ qua đăng xuất/khởi động lại. */
class DemoDataSeeder(private val database: ExpenseDatabase) {
    /**
     * Nạp 6 khoản chi mẫu chỉ cho vu98 nếu cờ demoSeeded chưa bật.
     * Thêm dữ liệu và bật cờ trong cùng transaction: lỗi giữa chừng sẽ hoàn tác toàn bộ.
     */
    suspend fun seed(account: LocalAccount) {
        if (account.username != "vu98") return
        database.withTransaction {
            val current = database.accountDao().find("vu98") ?: return@withTransaction
            if (current.demoSeeded) return@withTransaction
            // Ngày mẫu được tính tại lần nạp đầu tiên, mỗi khoản lùi thêm một ngày.
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
            // Đặt cờ sau khi thêm đủ các dòng; xóa giao dịch demo về sau sẽ không khiến hệ thống tự nạp lại.
            database.accountDao().markDemoSeeded(current.id)
        }
    }
}
