package com.example.qlct.model.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.qlct.model.entity.Category
import com.example.qlct.model.entity.MonthlyBudget
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.entity.TransactionRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    /**
     * Thêm danh mục mặc định; bỏ qua các dòng trùng khóa để có thể khởi tạo nhiều lần.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoriesIfMissing(categories: List<Category>)

    /**
     * Theo dõi ngân sách của một tài khoản; Flow phát lại danh sách khi bảng ngân sách thay đổi.
     */
    @Query("SELECT * FROM budgets WHERE user_id = :userId ORDER BY month DESC")
    fun observeBudgets(userId: Int): Flow<List<MonthlyBudget>>

    /**
     * Thêm hoặc cập nhật hạn mức theo khóa ghép tài khoản và tháng (Upsert).
     */
    @Upsert
    suspend fun saveBudget(budget: MonthlyBudget)

    /**
     * Xóa hạn mức của đúng tài khoản và tháng; không xóa các giao dịch.
     */
    @Query("DELETE FROM budgets WHERE user_id = :userId AND month = :month")
    suspend fun deleteBudget(userId: Int, month: String)

    /**
     * Theo dõi danh mục dùng chung, sắp theo ID để thứ tự chọn trên giao diện ổn định.
     */
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<Category>>

    /**
     * Ghép giao dịch với danh mục để lấy tên, chỉ đọc dữ liệu thuộc userId và ưu tiên ngày mới nhất.
     * SQL dùng IS để xử lý cả userId = null của dữ liệu cũ chưa gán tài khoản.
     */
    @Query("SELECT t.id,t.type,t.amount,t.category_id,t.date,t.note,c.name AS categoryName FROM transactions t JOIN categories c ON c.id = t.category_id WHERE t.user_id IS :userId ORDER BY t.date DESC, t.id DESC")
    fun observeTransactions(userId: Int? = null): Flow<List<TransactionRow>>

    /**
     * Thêm giao dịch và trả về ID tự sinh; việc kiểm tra dữ liệu được thực hiện tại Repository.
     */
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    /**
     * Sửa giao dịch khi cả ID và chủ sở hữu khớp; trả số dòng bị sửa để Repository phát hiện thất bại.
     */
    @Query("UPDATE transactions SET type=:type, amount=:amount, category_id=:categoryId, date=:date, note=:note WHERE id=:id AND user_id IS :userId")
    suspend fun updateOwned(id: Long, userId: Int?, type: String, amount: Long, categoryId: Long, date: String, note: String): Int

    /**
     * Xóa giao dịch theo ID và chủ sở hữu; trả số dòng bị xóa để tránh thao tác nhầm tài khoản.
     */
    @Query("DELETE FROM transactions WHERE id=:id AND user_id IS :userId")
    suspend fun deleteOwned(id: Long, userId: Int?): Int

}
