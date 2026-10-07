package com.example.qlct.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class Category(@PrimaryKey val id: Long, val name: String)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["category_id"]),
        ForeignKey(entity = LocalAccount::class, parentColumns = ["id"], childColumns = ["user_id"])
    ],
    indices = [Index(value = ["date"], name = "transactions_date"), Index(value = ["category_id"]), Index(value = ["user_id"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val date: String,
    @ColumnInfo(defaultValue = "''") val note: String = "",
    @ColumnInfo(name = "user_id") val userId: Int? = null
)

data class TransactionRow(
    val id: Long,
    val type: String,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val date: String,
    val note: String,
    val categoryName: String
)
