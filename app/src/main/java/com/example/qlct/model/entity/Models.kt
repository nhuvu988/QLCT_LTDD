package com.example.qlct.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Bảng danh mục dùng chung; tên duy nhất tránh hai danh mục cùng tên.
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class Category(@PrimaryKey val id: Long, val name: String)

// Bản ghi giao dịch lưu trong Room; khóa ngoại buộc danh mục/tài khoản tham chiếu phải tồn tại.
// Các index hỗ trợ truy vấn theo ngày, danh mục và tài khoản.
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["category_id"]),
        ForeignKey(entity = LocalAccount::class, parentColumns = ["id"], childColumns = ["user_id"])
    ],
    indices = [Index(value = ["date"], name = "transactions_date"), Index(value = ["category_id"]), Index(value = ["user_id"])]
)
data class TransactionEntity(
    // INCOME là thu, EXPENSE là chi; amount luôn là số nguyên VND dương.
    // Dấu cộng/trừ chỉ được quyết định khi hiển thị hoặc tính chênh lệch.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    // Ngày lưu dạng ISO yyyy-MM-dd để sắp xếp chuỗi và lọc theo tiền tố yyyy-MM được chính xác.
    val date: String,
    @ColumnInfo(defaultValue = "''") val note: String = "",
    // null dành cho dữ liệu cũ chưa có chủ; Repository gán userId hiện tại khi lưu.
    @ColumnInfo(name = "user_id") val userId: Int? = null
)

// Kết quả SELECT JOIN của DAO: bổ sung categoryName để UI không phải tự tra tên danh mục.
// Đây là dữ liệu đọc cho giao diện, không phải một bảng Room riêng.
data class TransactionRow(
    val id: Long,
    val type: String,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val date: String,
    val note: String,
    val categoryName: String
)
