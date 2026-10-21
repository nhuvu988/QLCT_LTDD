package com.example.qlct.model.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.example.qlct.model.entity.User

@Dao
interface AccountDao {
    @Query("SELECT * FROM users ORDER BY id LIMIT 1")
    suspend fun account(): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun find(username: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(account: User): Long

    @Query("UPDATE users SET demoSeeded = 1 WHERE id = :accountId")
    suspend fun markDemoSeeded(accountId: Int)

    @Insert
    suspend fun create(account: User): Long
}
