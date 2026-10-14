package com.example.qlct.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// This account protects the device's existing expense database, not a cloud account.
@Entity(tableName = "local_account", indices = [Index(value = ["username"], unique = true)])
data class LocalAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val salt: String,
    val passwordHash: String,
    @ColumnInfo(defaultValue = "0") val demoSeeded: Boolean = false
)
