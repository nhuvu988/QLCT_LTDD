package com.example.qlct.model.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.example.qlct.model.entity.LocalAccount

@Dao
interface AccountDao {
    @Query("SELECT * FROM local_account ORDER BY id LIMIT 1")
    suspend fun account(): LocalAccount?

    @Query("SELECT * FROM local_account WHERE username = :username")
    suspend fun find(username: String): LocalAccount?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(account: LocalAccount): Long

    @Query("UPDATE local_account SET demoSeeded = 1 WHERE id = :accountId")
    suspend fun markDemoSeeded(accountId: Int)

    @Insert
    suspend fun create(account: LocalAccount): Long
}
