package com.example.qlct.model.validation



object AmountValidator {
    const val MAX_AMOUNT = 999999999999L

    /**
     * Kiểm tra số nguyên VND trong khoảng 1 đến MAX_AMOUNT; trả null khi hợp lệ.
     */
    fun errorForValue(value: Long): String? = when {
        value <= 0 -> "Số tiền phải lớn hơn 0"
        value > MAX_AMOUNT -> "Số tiền tối đa là 999.999.999.999 đ"
        else -> null
    }

    /**
     * Kiểm tra chuỗi nhập: không rỗng, chuyển được sang Long và nằm trong giới hạn số tiền.
     */
    fun errorForInput(input: String): String? {
        val text = input.trim()
        if (text.isEmpty()) return "Vui lòng nhập số tiền"
        val value = text.toLongOrNull() ?: return "Nhập số tiền nguyên hợp lệ (VND)"
        return errorForValue(value)
    }
}
