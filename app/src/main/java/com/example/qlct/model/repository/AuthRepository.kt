package com.example.qlct.model.repository

import com.example.qlct.model.dao.AccountDao
import com.example.qlct.model.database.DatabaseInitializer
import com.example.qlct.model.database.DemoDataSeeder
import com.example.qlct.model.entity.LocalAccount
import com.example.qlct.model.validation.AccountValidator
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Điều phối tài khoản cục bộ, băm mật khẩu và nạp dữ liệu mặc định/demo thông qua Room.
class AuthRepository(
    private val dao: AccountDao,
    private val demoSeeder: DemoDataSeeder? = null,
    private val initializer: DatabaseInitializer? = null
) {
    /**
     * Đảm bảo dữ liệu mặc định đã có rồi kiểm tra thiết bị có ít nhất một tài khoản hay chưa.
     */
    suspend fun hasAccount(): Boolean {
        initializer?.initialize()
        return dao.account() != null
    }

    /**
     * Kiểm tra thông tin, chuẩn hóa tên và tạo tài khoản với salt ngẫu nhiên cùng hash PBKDF2.
     * Trả về tài khoản có ID thật từ Room; sau đó nạp dữ liệu demo nếu tài khoản đủ điều kiện.
     */
    suspend fun register(username: String, password: String, confirmation: String): LocalAccount {
        initializer?.initialize()
        require(AccountValidator.error(username, password, confirmation) == null)
        val name = AccountValidator.username(username)
        require(dao.find(name) == null) { "Tên đăng nhập đã được sử dụng." }
        // Mỗi tài khoản tự đăng ký dùng 32 byte salt ngẫu nhiên, kể cả khi hai người chọn cùng mật khẩu.
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        val account = LocalAccount(username = name, salt = hex(salt), passwordHash = hex(hash))
        val created = account.copy(id = dao.create(account).toInt())
        demoSeeder?.seed(created)
        return created
    }

    /**
     * Tìm tài khoản, tính lại hash từ mật khẩu nhập và salt đã lưu rồi so sánh.
     * Trả null khi sai thông tin; chỉ nạp dữ liệu demo sau khi xác thực thành công.
     */
    suspend fun login(username: String, password: String): LocalAccount? {
        initializer?.initialize()
        val account = dao.find(AccountValidator.username(username)) ?: return null
        val hash = derive(password, unhex(account.salt))
        // So sánh hash bằng MessageDigest.isEqual thay cho so sánh chuỗi thông thường.
        val matches = MessageDigest.isEqual(hash, unhex(account.passwordHash))
        if (!matches) return null
        demoSeeder?.seed(account)
        return account
    }

    /**
     * Dẫn xuất hash 256 bit bằng PBKDF2 với 600.000 vòng lặp trên Dispatchers.Default.
     * Khối finally xóa bản sao mật khẩu trong PBEKeySpec sau khi tính xong.
     */
    private suspend fun derive(password: String, salt: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        val spec = PBEKeySpec(password.toCharArray(), salt, 600_000, 256)
        try {
            // Có từ API 24; chỉ lưu kết quả hash, không lưu mật khẩu hay dạng mã hóa có thể giải ngược.
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally { spec.clearPassword() }
    }

    /**
     * Đổi từng byte thành hai ký tự hệ 16 để lưu salt/hash dưới dạng chuỗi trong Room.
     */
    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
    /**
     * Đổi chuỗi hệ 16 đã lưu thành mảng byte để tính hoặc so sánh hash.
     */
    private fun unhex(value: String): ByteArray = value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
