package com.example.rootfind.domain.math

object RootFindValidator {
    const val MAX_ROOT_INDEX = 1000

    /**
     * Validates that the input represents a positive whole number between 1 and 1000.
     */
    fun validateRootIndex(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Root index must be a positive whole number."))
        }
        // Only accept positive digits (optional leading +)
        val positiveIntegerRegex = Regex("^\\+?\\d+$")
        if (!positiveIntegerRegex.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Root index must be a positive whole number."))
        }

        val parsed = try {
            trimmed.removePrefix("+").toInt()
        } catch (e: NumberFormatException) {
            return Result.failure(IllegalArgumentException("Root index is too large. Maximum is $MAX_ROOT_INDEX."))
        }

        if (parsed <= 0) {
            return Result.failure(IllegalArgumentException("Root index must be a positive whole number."))
        }
        if (parsed > MAX_ROOT_INDEX) {
            return Result.failure(IllegalArgumentException("Root index is too large. Maximum is $MAX_ROOT_INDEX."))
        }

        return Result.success(parsed)
    }
}
