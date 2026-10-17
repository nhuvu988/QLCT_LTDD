package com.example.qlct.model.validation

import org.junit.Assert.*
import org.junit.Test

class AmountValidatorTest {
    /**
     * Kiểm tra số âm và số 0 đều bị từ chối.
     */
    @Test fun negativeAndZeroAmountsAreRejected() {
        listOf("-1", "-50000", "0", "-0").forEach {
            assertEquals("Số tiền phải lớn hơn 0", AmountValidator.errorForInput(it))
        }
        assertNotNull(AmountValidator.errorForValue(Long.MIN_VALUE))
    }

    /**
     * Kiểm tra hai biên số tiền hợp lệ: 1 VND và MAX_AMOUNT.
     */
    @Test fun positiveBoundsAreAccepted() {
        listOf("1", "50000", " 50000 ", AmountValidator.MAX_AMOUNT.toString()).forEach {
            assertNull(AmountValidator.errorForInput(it))
        }
    }

    /**
     * Kiểm tra chuỗi rỗng, số thập phân và chuỗi sai định dạng bị từ chối.
     */
    @Test fun emptyDecimalsAndMalformedInputAreRejected() {
        listOf("", " ", "1.5", "1,5", "abc", "--1", "999999999999999999999999").forEach {
            assertNotNull("Must reject $it", AmountValidator.errorForInput(it))
        }
    }

    /**
     * Kiểm tra số tiền vượt MAX_AMOUNT không được chấp nhận.
     */
    @Test fun amountsAboveLimitAreRejected() {
        assertNotNull(AmountValidator.errorForInput("1000000000000"))
        assertNotNull(AmountValidator.errorForValue(Long.MAX_VALUE))
    }
}
