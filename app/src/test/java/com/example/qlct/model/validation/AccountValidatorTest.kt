package com.example.qlct.model.validation

import org.junit.Assert.*
import org.junit.Test

class AccountValidatorTest {
    /**
     * Kiểm tra thông tin hợp lệ và chuẩn hóa tên đăng nhập bằng trim/chữ thường.
     */
    @Test fun validCredentialsAndNormalization() {
        assertNull(AccountValidator.error(" Vu_123 ", "password1", "password1"))
        assertEquals("vu_123", AccountValidator.username(" Vu_123 "))
        assertNull(AccountValidator.error("abc", "12345678"))
    }

    /**
     * Kiểm tra các lỗi tên đăng nhập, độ dài mật khẩu và mật khẩu xác nhận.
     */
    @Test fun rejectsInvalidNamesAndPasswords() {
        listOf("", "ab", "nguyễn", "a b", "a".repeat(33)).forEach {
            assertNotNull(AccountValidator.error(it, "password1"))
        }
        listOf("", "1234567", "        ", "x".repeat(129)).forEach {
            assertNotNull(AccountValidator.error("abc", it))
        }
        assertNotNull(AccountValidator.error("abc", "password1", "password2"))
    }
}
