package com.example.qlct.data

import org.junit.Assert.*
import org.junit.Test

class AmountValidatorTest {
    @Test fun negativeAndZeroAmountsAreRejected() {
        listOf("-1", "-50000", "0", "-0").forEach {
            assertEquals("Số tiền phải lớn hơn 0", AmountValidator.errorForInput(it))
        }
        assertNotNull(AmountValidator.errorForValue(Long.MIN_VALUE))
    }

    @Test fun positiveBoundsAreAccepted() {
        listOf("1", "50000", " 50000 ", AmountValidator.MAX_AMOUNT.toString()).forEach {
            assertNull(AmountValidator.errorForInput(it))
        }
    }

    @Test fun emptyDecimalsAndMalformedInputAreRejected() {
        listOf("", " ", "1.5", "1,5", "abc", "--1", "999999999999999999999999").forEach {
            assertNotNull("Must reject $it", AmountValidator.errorForInput(it))
        }
    }

    @Test fun amountsAboveLimitAreRejected() {
        assertNotNull(AmountValidator.errorForInput("1000000000000"))
        assertNotNull(AmountValidator.errorForValue(Long.MAX_VALUE))
    }
}
