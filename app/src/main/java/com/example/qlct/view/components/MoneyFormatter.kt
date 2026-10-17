package com.example.qlct.view.components

import java.text.NumberFormat
import java.util.Locale

/**
 * Định dạng số nguyên theo quy ước Việt Nam (dấu chấm phân nhóm nghìn) và thêm đơn vị đ.
 */
internal fun formatMoney(amount: Long) = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " đ"
