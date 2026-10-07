package com.example.qlct.data

object AmountValidator {
    const val MAX_AMOUNT = 999999999999L

    fun errorForValue(value: Long): String? = when {
        value <= 0 -> "Số tiền phải lớn hơn 0"
        value > MAX_AMOUNT -> "Số tiền tối đa là 999.999.999.999 đ"
        else -> null
    }

    fun errorForInput(input: String): String? {
        val text = input.trim()
        if (text.isEmpty()) return "Vui lòng nhập số tiền"
        val value = text.toLongOrNull() ?: return "Nhập số tiền nguyên hợp lệ (VND)"
        return errorForValue(value)
    }
}
