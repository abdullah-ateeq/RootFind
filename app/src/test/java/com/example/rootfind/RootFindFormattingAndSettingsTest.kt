package com.example.rootfind

import com.example.rootfind.domain.math.DecimalRootCalculator
import com.example.rootfind.domain.math.RootCalculator
import com.example.rootfind.domain.math.RootFindFormatter
import com.example.rootfind.model.RootType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class RootFindFormattingAndSettingsTest {

    // 35. Rounding modes
    @Test
    fun testRoundingModesOnSqrt2() {
        val sqrt2 = DecimalRootCalculator.decimalNthRoot(BigDecimal("2"), 2)

        // 1.41421356...
        val halfUp = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.HALF_UP)
        assertEquals("1.4142", halfUp)

        val up = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.UP)
        assertEquals("1.4143", up)

        val down = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.DOWN)
        assertEquals("1.4142", down)

        val ceiling = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.CEILING)
        assertEquals("1.4143", ceiling)

        val floor = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.FLOOR)
        assertEquals("1.4142", floor)

        val halfEven = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.HALF_EVEN)
        assertEquals("1.4142", halfEven)

        val halfDown = RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.HALF_DOWN)
        assertEquals("1.4142", halfDown)
    }

    // 36. Decimal display precision
    @Test
    fun testDecimalDisplayPrecisions() {
        val sqrt2 = DecimalRootCalculator.decimalNthRoot(BigDecimal("2"), 2)

        assertEquals("1", RootFindFormatter.formatResult(sqrt2, false, 0, RoundingMode.HALF_UP))
        assertEquals("1.41", RootFindFormatter.formatResult(sqrt2, false, 2, RoundingMode.HALF_UP))
        assertEquals("1.4142", RootFindFormatter.formatResult(sqrt2, false, 4, RoundingMode.HALF_UP))
        assertEquals("1.414214", RootFindFormatter.formatResult(sqrt2, false, 6, RoundingMode.HALF_UP))
        assertEquals("1.41421356", RootFindFormatter.formatResult(sqrt2, false, 8, RoundingMode.HALF_UP))
        assertEquals("1.4142135624", RootFindFormatter.formatResult(sqrt2, false, 10, RoundingMode.HALF_UP))
    }

    @Test
    fun testTrailingZeroRemoval() {
        // Exact integer: 4 instead of 4.0000000000
        val res4 = RootFindFormatter.formatResult(BigDecimal("4.0000000000"), true, 10, RoundingMode.HALF_UP)
        assertEquals("4", res4)

        // Exact decimal: 1.5 instead of 1.5000000000
        val res15 = RootFindFormatter.formatResult(BigDecimal("1.5000000000"), true, 10, RoundingMode.HALF_UP)
        assertEquals("1.5", res15)
    }

    @Test
    fun testExactIntegerNeverRounded() {
        // Even if precision is 0 or 2, an exact root of 16 is always 4
        val exactRes = BigDecimal("4")
        val formatted = RootFindFormatter.formatResult(exactRes, true, 0, RoundingMode.HALF_UP)
        assertEquals("4", formatted)
    }

    // 39. Copy formatting
    @Test
    fun testCopyFormattingSquareExact() {
        val result = RootCalculator.calculateRoot(BigDecimal("16"), 2).getOrThrow()
        val text = RootFindFormatter.formatCopyText(
            rootType = RootType.SQUARE,
            index = 2,
            value = BigDecimal("16"),
            result = result,
            displayedResult = "4"
        )
        val expected = """
            RootFind

            Square Root

            √16 = 4

            Exact Result

            Verification:
            4² = 16
        """.trimIndent()
        assertEquals(expected, text)
    }

    @Test
    fun testCopyFormattingCustomExact() {
        val result = RootCalculator.calculateRoot(BigDecimal("81"), 4).getOrThrow()
        val text = RootFindFormatter.formatCopyText(
            rootType = RootType.CUSTOM,
            index = 4,
            value = BigDecimal("81"),
            result = result,
            displayedResult = "3"
        )
        val expected = """
            RootFind

            Custom Root

            ⁴√81 = 3

            Exact Result

            Verification:
            3⁴ = 81
        """.trimIndent()
        assertEquals(expected, text)
    }

    @Test
    fun testCopyFormattingApproximate() {
        val result = RootCalculator.calculateRoot(BigDecimal("2"), 2).getOrThrow()
        val text = RootFindFormatter.formatCopyText(
            rootType = RootType.SQUARE,
            index = 2,
            value = BigDecimal("2"),
            result = result,
            displayedResult = "1.4142135624"
        )
        assertTrue(text.contains("√2 ≈ 1.4142135624"))
        assertTrue(text.contains("Approximate Result"))
    }

    // 40. Share formatting
    @Test
    fun testShareFormattingMatchesCopy() {
        val result = RootCalculator.calculateRoot(BigDecimal("125"), 3).getOrThrow()
        val copyText = RootFindFormatter.formatCopyText(RootType.CUBE, 3, BigDecimal("125"), result, "5")
        val shareText = RootFindFormatter.formatShareText(RootType.CUBE, 3, BigDecimal("125"), result, "5")
        assertEquals(copyText, shareText)
    }
}
