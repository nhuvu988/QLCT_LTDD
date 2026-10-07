package com.example.qlct.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<Category>>

    @Query("SELECT t.id,t.type,t.amount,t.category_id,t.date,t.note,c.name AS categoryName FROM transactions t JOIN categories c ON c.id = t.category_id WHERE t.user_id IS :userId ORDER BY t.date DESC, t.id DESC")
    fun observeTransactions(userId: Int? = null): Flow<List<TransactionRow>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Query("UPDATE transactions SET type=:type, amount=:amount, category_id=:categoryId, date=:date, note=:note WHERE id=:id AND user_id IS :userId")
    suspend fun updateOwned(id: Long, userId: Int?, type: String, amount: Long, categoryId: Long, date: String, note: String): Int

    @Query("DELETE FROM transactions WHERE id=:id AND user_id IS :userId")
    suspend fun deleteOwned(id: Long, userId: Int?): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long): Int
}
