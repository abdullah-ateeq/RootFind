package com.example.rootfind.domain.math

import java.math.BigInteger

object IntegerRootCalculator {

    /**
     * Calculates floor(value^(1/index)) using exact BigInteger arithmetic.
     * Never converts to Double or Float.
     */
    fun integerNthRoot(value: BigInteger, index: Int): BigInteger {
        require(value >= BigInteger.ZERO) { "Value must be non-negative" }
        require(index >= 1) { "Root index must be at least 1" }

        if (value == BigInteger.ZERO) return BigInteger.ZERO
        if (value == BigInteger.ONE) return BigInteger.ONE
        if (index == 1) return value

        val bitLength = value.bitLength()
        val rootBitLen = (bitLength + index - 1) / index
        var x = BigInteger.ONE.shiftLeft(rootBitLen)

        val bigIndex = BigInteger.valueOf(index.toLong())
        val bigIndexMinus1 = BigInteger.valueOf((index - 1).toLong())

        // Newton-Raphson on BigInteger
        while (true) {
            val xPowMinus1 = x.pow(index - 1)
            val divResult = value.divide(xPowMinus1)
            val xNext = (bigIndexMinus1.multiply(x).add(divResult)).divide(bigIndex)
            if (xNext >= x) {
                break
            }
            x = xNext
        }

        // Final boundary correction ensuring floor(value^(1/index))
        while (x.pow(index) > value) {
            x = x.subtract(BigInteger.ONE)
        }
        while ((x.add(BigInteger.ONE)).pow(index) <= value) {
            x = x.add(BigInteger.ONE)
        }

        return x
    }

    /**
     * Determines whether [value] is a perfect [index]-th power,
     * and returns the exact integer root if it is.
     */
    fun exactIntegerNthRootOrNull(value: BigInteger, index: Int): BigInteger? {
        if (value < BigInteger.ZERO) return null
        val root = integerNthRoot(value, index)
        return if (root.pow(index) == value) root else null
    }
}
