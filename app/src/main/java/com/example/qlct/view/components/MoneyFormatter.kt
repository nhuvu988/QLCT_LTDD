package com.example.qlct.view.components

import java.text.NumberFormat
import java.util.Locale

internal fun formatMoney(amount: Long) = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " đ"
