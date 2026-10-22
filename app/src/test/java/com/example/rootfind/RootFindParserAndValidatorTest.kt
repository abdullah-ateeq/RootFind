package com.example.rootfind

import com.example.rootfind.domain.math.RootFindParser
import com.example.rootfind.domain.math.RootFindValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class RootFindParserAndValidatorTest {

    // 1. Decimal parsing
    @Test
    fun testValidDecimalParsing() {
        val cases = listOf(
            "100" to BigDecimal("100"),
            "-100" to BigDecimal("-100"),
            "+100" to BigDecimal("100"),
            "1.5" to BigDecimal("1.5"),
            "-2.75" to BigDecimal("-2.75"),
            "0.125" to BigDecimal("0.125"),
            "0" to BigDecimal("0"),
            "16" to BigDecimal("16"),
            "125" to BigDecimal("125"),
            "81" to BigDecimal("81"),
            "2" to BigDecimal("2"),
            "2.5" to BigDecimal("2.5"),
            "-27" to BigDecimal("-27"),
            "1" to BigDecimal("1"),
            "100000000000000000000" to BigDecimal("100000000000000000000")
        )

        for ((input, expected) in cases) {
            val result = RootFindParser.parseDecimal(input)
            assertTrue("Expected success for input '$input'", result.isSuccess)
            assertEquals(0, expected.compareTo(result.getOrThrow()))
        }
    }

    // 2. Integer parsing
    @Test
    fun testValidIntegerParsing() {
        val result = RootFindParser.parseInteger("12345678901234567890")
        assertTrue(result.isSuccess)
        assertEquals(BigInteger("12345678901234567890"), result.getOrThrow())

        val negative = RootFindParser.parseInteger("-50")
        assertTrue(negative.isSuccess)
        assertEquals(BigInteger("-50"), negative.getOrThrow())
    }

    // 3. Empty input
    @Test
    fun testEmptyInput() {
        val emptyDecimal = RootFindParser.parseDecimal("")
        assertTrue(emptyDecimal.isFailure)
        assertEquals("Enter a value.", emptyDecimal.exceptionOrNull()?.message)

        val whitespaceOnly = RootFindParser.parseDecimal("   ")
        assertTrue(whitespaceOnly.isFailure)
        assertEquals("Enter a value.", whitespaceOnly.exceptionOrNull()?.message)

        val emptyInteger = RootFindParser.parseInteger("")
        assertTrue(emptyInteger.isFailure)
        assertEquals("Enter a value.", emptyInteger.exceptionOrNull()?.message)
    }

    // 4. Invalid input (e.g. letters)
    @Test
    fun testInvalidInput() {
        val letters = RootFindParser.parseDecimal("abc")
        assertTrue(letters.isFailure)
        assertEquals("Enter a valid decimal number.", letters.exceptionOrNull()?.message)

        val mixed = RootFindParser.parseDecimal("12a34")
        assertTrue(mixed.isFailure)
        assertEquals("Enter a valid decimal number.", mixed.exceptionOrNull()?.message)
    }

    // 5. Decimal format validation
    @Test
    fun testDecimalFormatValidation() {
        val invalidCases = listOf(
            "1.2.3",
            "--",
            "++",
            "+-",
            "-+",
            "1E5",
            "2E-5",
            "1e10",
            ".",
            "+.",
            "-.",
            "1 000",
            " 1 2 3 "
        )

        for (case in invalidCases) {
            val result = RootFindParser.parseDecimal(case)
            assertTrue("Expected failure for malformed decimal '$case'", result.isFailure)
            assertEquals("Enter a valid decimal number.", result.exceptionOrNull()?.message)
        }
    }

    @Test
    fun testSurroundingWhitespaceAllowed() {
        val padded = RootFindParser.parseDecimal("   25   ")
        assertTrue(padded.isSuccess)
        assertEquals(0, BigDecimal("25").compareTo(padded.getOrThrow()))
    }

    @Test
    fun testInputLengthLimit() {
        val long500 = "1".repeat(500)
        val result500 = RootFindParser.parseDecimal(long500)
        assertTrue(result500.isSuccess)

        val long501 = "1".repeat(501)
        val result501 = RootFindParser.parseDecimal(long501)
        assertTrue(result501.isFailure)
        assertEquals("Number is too long. Maximum 500 characters.", result501.exceptionOrNull()?.message)
    }

    // 6. Root index validation
    @Test
    fun testValidRootIndexValidation() {
        val validIndices = listOf("1", "2", "3", "4", "5", "10", "100", "1000", "+2")
        for (idx in validIndices) {
            val res = RootFindValidator.validateRootIndex(idx)
            assertTrue("Expected valid root index '$idx'", res.isSuccess)
        }
        assertEquals(1, RootFindValidator.validateRootIndex("1").getOrThrow())
        assertEquals(1000, RootFindValidator.validateRootIndex("1000").getOrThrow())
    }

    // 7. Root index zero
    @Test
    fun testRootIndexZero() {
        val result = RootFindValidator.validateRootIndex("0")
        assertTrue(result.isFailure)
        assertEquals("Root index must be a positive whole number.", result.exceptionOrNull()?.message)
    }

    // 8. Negative root index
    @Test
    fun testNegativeRootIndex() {
        val result = RootFindValidator.validateRootIndex("-3")
        assertTrue(result.isFailure)
        assertEquals("Root index must be a positive whole number.", result.exceptionOrNull()?.message)
    }

    // 9. Decimal root index
    @Test
    fun testDecimalRootIndex() {
        val result = RootFindValidator.validateRootIndex("2.5")
        assertTrue(result.isFailure)
        assertEquals("Root index must be a positive whole number.", result.exceptionOrNull()?.message)

        val resultAlphabet = RootFindValidator.validateRootIndex("abc")
        assertTrue(resultAlphabet.isFailure)
        assertEquals("Root index must be a positive whole number.", resultAlphabet.exceptionOrNull()?.message)
    }

    // 10. Root index limit
    @Test
    fun testRootIndexLimit() {
        val result1000 = RootFindValidator.validateRootIndex("1000")
        assertTrue(result1000.isSuccess)

        val result1001 = RootFindValidator.validateRootIndex("1001")
        assertTrue(result1001.isFailure)
        assertEquals("Root index is too large. Maximum is 1000.", result1001.exceptionOrNull()?.message)

        val hugeNumber = RootFindValidator.validateRootIndex("999999999999999999999999999")
        assertTrue(hugeNumber.isFailure)
        assertEquals("Root index is too large. Maximum is 1000.", hugeNumber.exceptionOrNull()?.message)
    }
}
