package com.example.qlct.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AccountValidator {
    fun username(value: String): String = value.trim().lowercase(Locale.ROOT)
    fun error(username: String, password: String, confirmation: String? = null): String? = when {
        !Regex("[a-zA-Z0-9_]{3,32}").matches(username.trim()) ->
            "Tên đăng nhập từ 3–32 ký tự, chỉ gồm chữ không dấu, số và dấu _."
        password.length !in 8..128 -> "Mật khẩu cần từ 8–128 ký tự."
        password.isBlank() -> "Mật khẩu không được chỉ chứa khoảng trắng."
        confirmation != null && password != confirmation -> "Mật khẩu xác nhận không khớp."
        else -> null
    }
}

class AuthRepository(private val dao: AccountDao, private val demoSeeder: DemoDataSeeder? = null) {
    suspend fun hasAccount(): Boolean = dao.account() != null

    suspend fun register(username: String, password: String, confirmation: String): LocalAccount {
        require(AccountValidator.error(username, password, confirmation) == null)
        val name = AccountValidator.username(username)
        require(dao.find(name) == null) { "Tên đăng nhập đã được sử dụng." }
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        val account = LocalAccount(username = name, salt = hex(salt), passwordHash = hex(hash))
        val created = account.copy(id = dao.create(account).toInt())
        demoSeeder?.seed(created)
        return created
    }

    suspend fun login(username: String, password: String): LocalAccount? {
        val account = dao.find(AccountValidator.username(username)) ?: return null
        val hash = derive(password, unhex(account.salt))
        val matches = MessageDigest.isEqual(hash, unhex(account.passwordHash))
        if (!matches) return null
        demoSeeder?.seed(account)
        return account
    }

    private suspend fun derive(password: String, salt: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        val spec = PBEKeySpec(password.toCharArray(), salt, 600_000, 256)
        try {
            // Available from API 24; store neither the password nor a reversible encoding.
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally { spec.clearPassword() }
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun unhex(value: String): ByteArray = value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
