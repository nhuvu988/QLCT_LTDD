package com.example.qlct.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Tài khoản cục bộ dùng với dữ liệu chi tiêu trên thiết bị; không có đồng bộ cloud.
@Entity(tableName = "local_account", indices = [Index(value = ["username"], unique = true)])
data class LocalAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    // Salt ngẫu nhiên và hash đều lưu dạng hệ 16; không lưu mật khẩu dạng rõ.
    val salt: String,
    val passwordHash: String,
    // Cờ lưu bền để vu98 chỉ được nạp giao dịch demo một lần, kể cả sau đăng xuất hoặc khởi động lại.
    @ColumnInfo(defaultValue = "0") val demoSeeded: Boolean = false
)
