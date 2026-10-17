package com.example.qlct.model.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.example.qlct.model.entity.LocalAccount

@Dao
interface AccountDao {
    /**
     * Lấy tài khoản có ID nhỏ nhất, hoặc null nếu chưa có; dùng để chọn màn hình đăng nhập/đăng ký.
     */
    @Query("SELECT * FROM local_account ORDER BY id LIMIT 1")
    suspend fun account(): LocalAccount?

    /**
     * Tìm tài khoản theo tên đăng nhập đã chuẩn hóa; trả về null nếu tên chưa tồn tại.
     */
    @Query("SELECT * FROM local_account WHERE username = :username")
    suspend fun find(username: String): LocalAccount?

    /**
     * Thêm tài khoản mẫu nếu chưa tồn tại; IGNORE giữ nguyên bản ghi cũ và trả -1 khi xung đột.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(account: LocalAccount): Long

    /**
     * Đánh dấu tài khoản đã nhận giao dịch mẫu để lần đăng nhập sau không nạp trùng.
     */
    @Query("UPDATE local_account SET demoSeeded = 1 WHERE id = :accountId")
    suspend fun markDemoSeeded(accountId: Int)

    /**
     * Thêm tài khoản mới và trả về ID tự sinh; Room báo lỗi nếu tên đăng nhập bị trùng.
     */
    @Insert
    suspend fun create(account: LocalAccount): Long
}
