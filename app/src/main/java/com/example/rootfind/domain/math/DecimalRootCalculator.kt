package com.example.rootfind.domain.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

object DecimalRootCalculator {

    val DEFAULT_MATH_CONTEXT = MathContext(80, RoundingMode.HALF_UP)
    private const val MAX_ITERATIONS = 200

    /**
     * Attempts to find an exact decimal root without approximation.
     * Works for both integers and rational decimals like 2.25 -> 1.5, 0.125 -> 0.5, 0.0625 -> 0.5.
     */
    fun exactDecimalRootOrNull(value: BigDecimal, index: Int): BigDecimal? {
        if (value < BigDecimal.ZERO) return null
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        if (value.compareTo(BigDecimal.ONE) == 0) return BigDecimal.ONE
        if (index == 1) return value

        val stripped = value.stripTrailingZeros()
        val scale = stripped.scale()

        if (scale <= 0) {
            val bi = try {
                stripped.toBigIntegerExact()
            } catch (e: Exception) {
                stripped.toBigInteger()
            }
            val intRoot = IntegerRootCalculator.exactIntegerNthRootOrNull(bi, index)
            if (intRoot != null) {
                return BigDecimal(intRoot)
            }
        } else if (scale % index == 0) {
            val unscaled = stripped.unscaledValue()
            val rootUnscaled = IntegerRootCalculator.exactIntegerNthRootOrNull(unscaled, index)
            if (rootUnscaled != null) {
                val candidate = BigDecimal(rootUnscaled).movePointLeft(scale / index)
                if (candidate.pow(index).compareTo(stripped) == 0) {
                    return candidate
                }
            }
        }

        return null
    }

    /**
     * Calculates the nth root of [value] using Newton-Raphson iteration.
     * Strictly avoids Double, Float, Math.pow, and Math.sqrt.
     *
     * Iteration:
     * x_next = ((n - 1) * x + value / x^(n - 1)) / n
     */
    fun decimalNthRoot(
        value: BigDecimal,
        index: Int,
        mathContext: MathContext = DEFAULT_MATH_CONTEXT
    ): BigDecimal {
        require(value >= BigDecimal.ZERO) { "Value must be non-negative for real decimal root" }
        require(index >= 1) { "Root index must be at least 1" }

        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        if (value.compareTo(BigDecimal.ONE) == 0) return BigDecimal.ONE
        if (index == 1) return value

        // Check if there is an exact root first
        val exact = exactDecimalRootOrNull(value, index)
        if (exact != null) {
            return exact
        }

        // Generate an initial estimate purely using BigInteger & BigDecimal
        var x = deriveInitialEstimate(value, index, mathContext)

        val n = BigDecimal.valueOf(index.toLong())
        val nMinus1 = BigDecimal.valueOf((index - 1).toLong())
        val tolerance = BigDecimal.ONE.movePointLeft(mathContext.precision - 5)

        var iterations = 0
        while (iterations < MAX_ITERATIONS) {
            // x^(n - 1)
            val xPowNMinus1 = x.pow(index - 1, mathContext)
            if (xPowNMinus1.compareTo(BigDecimal.ZERO) == 0) {
                throw ArithmeticException("Unable to calculate this root at the selected precision.")
            }

            // A / x^(n - 1)
            val aDivXPow = value.divide(xPowNMinus1, mathContext)

            // ((n - 1) * x + A / x^(n - 1)) / n
            val numerator = nMinus1.multiply(x, mathContext).add(aDivXPow, mathContext)
            val xNext = numerator.divide(n, mathContext)

            val diff = xNext.subtract(x).abs()
            if (diff < tolerance) {
                return xNext
            }

            x = xNext
            iterations++
        }

        throw ArithmeticException("Unable to calculate this root at the selected precision.")
    }

    /**
     * Derives a high-quality initial estimate for Newton-Raphson without floating point.
     */
    private fun deriveInitialEstimate(
        value: BigDecimal,
        index: Int,
        mathContext: MathContext
    ): BigDecimal {
        return if (value >= BigDecimal.ONE) {
            val bi = value.toBigInteger()
            val intRoot = IntegerRootCalculator.integerNthRoot(bi, index).max(BigInteger.ONE)
            BigDecimal(intRoot)
        } else {
            // Scale up value until it is >= 1
            var scaled = value
            var k = 0
            val stepScale = BigDecimal.TEN.pow(index)
            while (scaled < BigDecimal.ONE && k < 50) {
                scaled = scaled.multiply(stepScale)
                k++
            }
            val intRoot = IntegerRootCalculator.integerNthRoot(scaled.toBigInteger(), index).max(BigInteger.ONE)
            BigDecimal(intRoot).divide(BigDecimal.TEN.pow(k), mathContext)
        }
    }
}
