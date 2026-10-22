package com.example.rootfind

import com.example.rootfind.domain.math.DecimalRootCalculator
import com.example.rootfind.domain.math.IntegerRootCalculator
import com.example.rootfind.domain.math.RootCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class RootFindMathEngineTest {

    // 11. Square root
    @Test
    fun testSquareRootExact() {
        val outcome = RootCalculator.calculateRoot(BigDecimal("16"), 2)
        assertTrue(outcome.isSuccess)
        val result = outcome.getOrThrow()
        assertTrue(result.isExact)
        assertTrue(result.isReal)
        assertEquals(0, BigDecimal("4").compareTo(result.result!!))
        assertEquals("4² = 16 ✓", result.verificationExpression)
    }

    // 12. Cube root
    @Test
    fun testCubeRootExact() {
        val outcome = RootCalculator.calculateRoot(BigDecimal("125"), 3)
        assertTrue(outcome.isSuccess)
        val result = outcome.getOrThrow()
        assertTrue(result.isExact)
        assertTrue(result.isReal)
        assertEquals(0, BigDecimal("5").compareTo(result.result!!))
        assertEquals("5³ = 125 ✓", result.verificationExpression)
    }

    // 13. Custom nth root
    @Test
    fun testCustomNthRoot() {
        val outcome = RootCalculator.calculateRoot(BigDecimal("81"), 4)
        assertTrue(outcome.isSuccess)
        val result = outcome.getOrThrow()
        assertTrue(result.isExact)
        assertEquals(0, BigDecimal("3").compareTo(result.result!!))
        assertEquals("3⁴ = 81 ✓", result.verificationExpression)

        // Fifth root of 32 = 2
        val fifth = RootCalculator.calculateRoot(BigDecimal("32"), 5).getOrThrow()
        assertTrue(fifth.isExact)
        assertEquals(0, BigDecimal("2").compareTo(fifth.result!!))
    }

    // 14. Root index one
    @Test
    fun testRootIndexOne() {
        val outcome = RootCalculator.calculateRoot(BigDecimal("25"), 1)
        assertTrue(outcome.isSuccess)
        val result = outcome.getOrThrow()
        assertTrue(result.isExact)
        assertEquals(0, BigDecimal("25").compareTo(result.result!!))
        assertEquals("25¹ = 25 ✓", result.verificationExpression)
    }

    // 15. Zero
    @Test
    fun testZeroRoots() {
        val indices = listOf(1, 2, 3, 4, 10, 100)
        for (idx in indices) {
            val outcome = RootCalculator.calculateRoot(BigDecimal.ZERO, idx)
            assertTrue(outcome.isSuccess)
            val result = outcome.getOrThrow()
            assertTrue(result.isExact)
            assertEquals(0, BigDecimal.ZERO.compareTo(result.result!!))
        }
    }

    // 16. One
    @Test
    fun testOneRoots() {
        val indices = listOf(1, 2, 3, 4, 10, 100)
        for (idx in indices) {
            val outcome = RootCalculator.calculateRoot(BigDecimal.ONE, idx)
            assertTrue(outcome.isSuccess)
            val result = outcome.getOrThrow()
            assertTrue(result.isExact)
            assertEquals(0, BigDecimal.ONE.compareTo(result.result!!))
        }
    }

    // 17. Positive integer roots
    @Test
    fun testPositiveIntegerRoots() {
        val cases = listOf(
            Triple(BigDecimal("25"), 2, BigDecimal("5")),
            Triple(BigDecimal("144"), 2, BigDecimal("12")),
            Triple(BigDecimal("27"), 3, BigDecimal("3")),
            Triple(BigDecimal("64"), 6, BigDecimal("2"))
        )
        for ((value, index, expected) in cases) {
            val res = RootCalculator.calculateRoot(value, index).getOrThrow()
            assertTrue(res.isExact)
            assertEquals(0, expected.compareTo(res.result!!))
        }
    }

    // 18. Decimal roots
    @Test
    fun testDecimalRoots() {
        // sqrt(2.25) = 1.5
        val res225 = RootCalculator.calculateRoot(BigDecimal("2.25"), 2).getOrThrow()
        assertTrue(res225.isExact)
        assertEquals(0, BigDecimal("1.5").compareTo(res225.result!!))

        // cbrt(0.125) = 0.5
        val res0125 = RootCalculator.calculateRoot(BigDecimal("0.125"), 3).getOrThrow()
        assertTrue(res0125.isExact)
        assertEquals(0, BigDecimal("0.5").compareTo(res0125.result!!))

        // 4th root of 0.0625 = 0.5
        val res00625 = RootCalculator.calculateRoot(BigDecimal("0.0625"), 4).getOrThrow()
        assertTrue(res00625.isExact)
        assertEquals(0, BigDecimal("0.5").compareTo(res00625.result!!))
    }

    // 19. Exact roots detection
    @Test
    fun testExactRootsDetection() {
        val exactSquares = listOf("1", "4", "9", "16", "25", "36", "49", "64", "81", "100", "121", "144")
        for (sq in exactSquares) {
            val res = RootCalculator.calculateRoot(BigDecimal(sq), 2).getOrThrow()
            assertTrue("Expected exact result for $sq", res.isExact)
            assertTrue(res.isPerfectPower)
        }
    }

    // 20. Non-exact roots
    @Test
    fun testNonExactRoots() {
        // sqrt(2)
        val resSqrt2 = RootCalculator.calculateRoot(BigDecimal("2"), 2).getOrThrow()
        assertFalse(resSqrt2.isExact)
        assertFalse(resSqrt2.isPerfectPower)
        assertTrue(resSqrt2.isReal)
        assertNotNull(resSqrt2.result)
        // Check first 10 digits: 1.4142135623...
        val str10 = resSqrt2.result!!.setScale(10, RoundingMode.HALF_UP).toPlainString()
        assertEquals("1.4142135624", str10)

        // cbrt(10)
        val resCbrt10 = RootCalculator.calculateRoot(BigDecimal("10"), 3).getOrThrow()
        assertFalse(resCbrt10.isExact)
        assertTrue(resCbrt10.isReal)
    }

    // 21. Negative odd roots
    @Test
    fun testNegativeOddRoots() {
        // cbrt(-27) = -3
        val resNeg27 = RootCalculator.calculateRoot(BigDecimal("-27"), 3).getOrThrow()
        assertTrue(resNeg27.isReal)
        assertTrue(resNeg27.isExact)
        assertEquals(0, BigDecimal("-3").compareTo(resNeg27.result!!))
        assertEquals("(-3)³ = -27 ✓", resNeg27.verificationExpression)

        // 5th root of -32 = -2
        val resNeg32 = RootCalculator.calculateRoot(BigDecimal("-32"), 5).getOrThrow()
        assertTrue(resNeg32.isReal)
        assertTrue(resNeg32.isExact)
        assertEquals(0, BigDecimal("-2").compareTo(resNeg32.result!!))
        assertEquals("(-2)⁵ = -32 ✓", resNeg32.verificationExpression)
    }

    // 22. Negative even roots
    @Test
    fun testNegativeEvenRoots() {
        // sqrt(-16) -> No real result
        val resNeg16 = RootCalculator.calculateRoot(BigDecimal("-16"), 2).getOrThrow()
        assertFalse(resNeg16.isReal)
        assertFalse(resNeg16.isExact)
        assertNull(resNeg16.result)
        assertNull(resNeg16.verificationExpression)
        assertTrue(resNeg16.calculationSteps.any { it.result.contains("not a real number") })

        // 4th root of -81 -> No real result
        val resNeg81 = RootCalculator.calculateRoot(BigDecimal("-81"), 4).getOrThrow()
        assertFalse(resNeg81.isReal)
        assertNull(resNeg81.result)
    }

    // 23, 24, 25. Perfect powers: squares, cubes, nth powers
    @Test
    fun testPerfectPowers() {
        // Perfect 4th power: 625 -> 5 (5^4 = 625)
        val res625 = RootCalculator.calculateRoot(BigDecimal("625"), 4).getOrThrow()
        assertTrue(res625.isExact)
        assertEquals(0, BigDecimal("5").compareTo(res625.result!!))

        // Perfect cube: 343 -> 7
        val res343 = RootCalculator.calculateRoot(BigDecimal("343"), 3).getOrThrow()
        assertTrue(res343.isExact)
        assertEquals(0, BigDecimal("7").compareTo(res343.result!!))
    }

    // 26. Integer nth-root calculation (IntegerRootCalculator)
    @Test
    fun testIntegerRootCalculatorFloor() {
        assertEquals(BigInteger("4"), IntegerRootCalculator.integerNthRoot(BigInteger("16"), 2))
        assertEquals(BigInteger("4"), IntegerRootCalculator.integerNthRoot(BigInteger("17"), 2))
        assertEquals(BigInteger("4"), IntegerRootCalculator.integerNthRoot(BigInteger("24"), 2))
        assertEquals(BigInteger("5"), IntegerRootCalculator.integerNthRoot(BigInteger("25"), 2))

        assertEquals(BigInteger("3"), IntegerRootCalculator.integerNthRoot(BigInteger("27"), 3))
        assertEquals(BigInteger("3"), IntegerRootCalculator.integerNthRoot(BigInteger("28"), 3))
        assertEquals(BigInteger("3"), IntegerRootCalculator.integerNthRoot(BigInteger("63"), 3))
        assertEquals(BigInteger("4"), IntegerRootCalculator.integerNthRoot(BigInteger("64"), 3))

        assertEquals(BigInteger("3"), IntegerRootCalculator.integerNthRoot(BigInteger("81"), 4))
    }

    // 27 & 28. Decimal nth-root calculation & Newton-Raphson convergence
    @Test
    fun testDecimalNthRootConvergence() {
        val mathContext = MathContext(80, RoundingMode.HALF_UP)
        val root2 = DecimalRootCalculator.decimalNthRoot(BigDecimal("2"), 2, mathContext)
        // Verify root2^2 is extremely close to 2 (within 80 digits)
        val squared = root2.multiply(root2, mathContext)
        val diff = squared.subtract(BigDecimal("2")).abs()
        assertTrue(diff < BigDecimal("1E-70"))

        val rootCbrt7 = DecimalRootCalculator.decimalNthRoot(BigDecimal("7"), 3, mathContext)
        val cubed = rootCbrt7.multiply(rootCbrt7, mathContext).multiply(rootCbrt7, mathContext)
        val diffCubed = cubed.subtract(BigDecimal("7")).abs()
        assertTrue(diffCubed < BigDecimal("1E-70"))
    }

    // 29 & 30. Exact and approximate result verification
    @Test
    fun testVerificationExpressions() {
        val exact = RootCalculator.calculateRoot(BigDecimal("81"), 4).getOrThrow()
        assertEquals("3⁴ = 81 ✓", exact.verificationExpression)

        val approx = RootCalculator.calculateRoot(BigDecimal("2"), 2).getOrThrow()
        assertNotNull(approx.verificationExpression)
        assertTrue(approx.verificationExpression!!.contains("≈ 2"))
    }

    // 31. BigInteger precision
    @Test
    fun testBigIntegerPrecisionLarge() {
        // Larger than Long.MAX_VALUE (9223372036854775807)
        // 10^20 = 100000000000000000000
        val bigVal = BigInteger("100000000000000000000")
        val root = IntegerRootCalculator.integerNthRoot(bigVal, 2)
        assertEquals(BigInteger("10000000000"), root)
        assertEquals(bigVal, root.pow(2))
    }

    // 32. BigDecimal precision
    @Test
    fun testBigDecimalPrecision() {
        val mc = MathContext(80, RoundingMode.HALF_UP)
        val root3 = DecimalRootCalculator.decimalNthRoot(BigDecimal("3"), 2, mc)
        // 80 digits of precision must be present
        assertTrue(root3.toPlainString().length >= 75)
    }

    // 33. Large perfect powers
    @Test
    fun testLargePerfectPowers() {
        // (10^15)^3 = 10^45
        val base = BigInteger("1000000000000000")
        val cube = base.pow(3)
        val res = RootCalculator.calculateRoot(BigDecimal(cube), 3).getOrThrow()
        assertTrue(res.isExact)
        assertEquals(0, BigDecimal(base).compareTo(res.result!!))
    }

    // 34. Large non-perfect powers
    @Test
    fun testLargeNonPerfectPowers() {
        val base = BigInteger("1000000000000000")
        val nonPerfect = base.pow(3).add(BigInteger.ONE)
        val res = RootCalculator.calculateRoot(BigDecimal(nonPerfect), 3).getOrThrow()
        assertFalse(res.isExact)
        assertTrue(res.isReal)
        assertNotNull(res.result)
    }
}
