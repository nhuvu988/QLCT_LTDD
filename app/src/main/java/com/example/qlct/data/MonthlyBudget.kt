package com.example.qlct.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "budgets",
    primaryKeys = ["user_id", "month"],
    foreignKeys = [ForeignKey(entity = LocalAccount::class,
        parentColumns = ["id"], childColumns = ["user_id"])]
)
data class MonthlyBudget(
    @ColumnInfo(name = "user_id") val userId: Int,
    val month: String,
    val amount: Long
)
