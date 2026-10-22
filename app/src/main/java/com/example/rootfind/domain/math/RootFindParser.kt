package com.example.rootfind.domain.math

import java.math.BigDecimal
import java.math.BigInteger

object RootFindParser {
    const val MAX_INPUT_LENGTH = 500

    /**
     * Parses a user input string into a BigDecimal.
     * Rejects scientific notation, internal spaces, multiple signs, and invalid formats.
     */
    fun parseDecimal(input: String): Result<BigDecimal> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a value."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 500 characters."))
        }
        // Reject internal whitespace
        if (trimmed.any { it.isWhitespace() }) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        // Reject scientific notation
        if (trimmed.contains("e", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
        // Strict pattern: optional sign followed by digits and optional single '.' with digits,
        // or optional sign followed by '.' and digits (e.g. .5, -.5).
        // Must have at least one digit.
        val validPattern = Regex("^[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)$")
        if (!validPattern.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }

        return try {
            val bd = BigDecimal(trimmed)
            Result.success(bd)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid decimal number."))
        }
    }

    /**
     * Parses a user input string into a BigInteger.
     */
    fun parseInteger(input: String): Result<BigInteger> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a value."))
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 500 characters."))
        }
        if (trimmed.any { it.isWhitespace() } || trimmed.contains(".")) {
            return Result.failure(IllegalArgumentException("Enter a valid whole number."))
        }
        if (trimmed.contains("e", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Enter a valid whole number."))
        }
        val validPattern = Regex("^[+-]?\\d+$")
        if (!validPattern.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Enter a valid whole number."))
        }

        return try {
            val bi = BigInteger(trimmed)
            Result.success(bi)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Enter a valid whole number."))
        }
    }
}
