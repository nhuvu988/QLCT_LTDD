package com.example.qlct.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Index
import androidx.room.ColumnInfo

// This account protects the device's existing expense database, not a cloud account.
@Entity(tableName = "local_account", indices = [Index(value = ["username"], unique = true)])
data class LocalAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val salt: String,
    val passwordHash: String,
    @ColumnInfo(defaultValue = "0") val demoSeeded: Boolean = false
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM local_account ORDER BY id LIMIT 1")
    suspend fun account(): LocalAccount?

    @Query("SELECT * FROM local_account WHERE username = :username")
    suspend fun find(username: String): LocalAccount?

    @Insert
    suspend fun create(account: LocalAccount): Long
}
