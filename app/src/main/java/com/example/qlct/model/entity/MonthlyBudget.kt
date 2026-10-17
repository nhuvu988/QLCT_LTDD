package com.example.qlct.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

// Mỗi tài khoản có tối đa một hạn mức cho mỗi tháng nhờ khóa chính ghép user_id và month.
@Entity(
    tableName = "budgets",
    primaryKeys = ["user_id", "month"],
    foreignKeys = [ForeignKey(entity = LocalAccount::class,
        parentColumns = ["id"], childColumns = ["user_id"])]
)
data class MonthlyBudget(
    @ColumnInfo(name = "user_id") val userId: Int,
    // Tháng ở dạng yyyy-MM, số tiền hạn mức là số nguyên VND dương.
    val month: String,
    val amount: Long
)
