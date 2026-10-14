package com.example.qlct.model.validation

import java.util.Locale

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
