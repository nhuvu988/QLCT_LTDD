package com.example.qlct.model.repository

import com.example.qlct.model.dao.AccountDao
import com.example.qlct.model.database.DatabaseInitializer
import com.example.qlct.model.database.DemoDataSeeder
import com.example.qlct.model.entity.User
import com.example.qlct.model.validation.AccountValidator
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val dao: AccountDao,
    private val demoSeeder: DemoDataSeeder? = null,
    private val initializer: DatabaseInitializer? = null
) {
    suspend fun hasAccount(): Boolean {
        initializer?.initialize()
        return dao.account() != null
    }

    suspend fun register(username: String, password: String, confirmation: String): User {
        initializer?.initialize()
        require(AccountValidator.error(username, password, confirmation) == null)
        val name = AccountValidator.username(username)
        require(dao.find(name) == null) { "Tên đăng nhập đã được sử dụng." }
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        val account = User(username = name, salt = hex(salt), passwordHash = hex(hash))
        val created = account.copy(id = dao.create(account).toInt())
        demoSeeder?.seed(created)
        return created
    }

    suspend fun login(username: String, password: String): User? {
        initializer?.initialize()
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
