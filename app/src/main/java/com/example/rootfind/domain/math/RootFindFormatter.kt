package com.example.rootfind.domain.math

import com.example.rootfind.model.CalculationRoundingMode
import com.example.rootfind.model.RootFindResult
import com.example.rootfind.model.RootType
import java.math.BigDecimal
import java.math.RoundingMode

object RootFindFormatter {

    fun toSuperscript(number: Int): String {
        val digits = number.toString()
        val sb = java.lang.StringBuilder()
        for (ch in digits) {
            val sup = when (ch) {
                '0' -> '⁰'
                '1' -> '¹'
                '2' -> '²'
                '3' -> '³'
                '4' -> '⁴'
                '5' -> '⁵'
                '6' -> '⁶'
                '7' -> '⁷'
                '8' -> '⁸'
                '9' -> '⁹'
                '-' -> '⁻'
                else -> ch
            }
            sb.append(sup)
        }
        return sb.toString()
    }

    fun getRootSymbol(index: Int): String {
        return when (index) {
            2 -> "√"
            3 -> "∛"
            else -> "${toSuperscript(index)}√"
        }
    }

    /**
     * Formats the displayed result string based on exactness, display precision, and rounding mode.
     * Never rounds exact integer results.
     */
    fun formatResult(
        result: BigDecimal,
        isExact: Boolean,
        decimalPlaces: Int,
        roundingMode: RoundingMode
    ): String {
        if (isExact) {
            // Strip trailing zeros for exact numbers (e.g., 4 instead of 4.00, 1.5 instead of 1.50)
            return if (result.compareTo(BigDecimal.ZERO) == 0) {
                "0"
            } else {
                result.stripTrailingZeros().toPlainString()
            }
        }

        // Approximate result: round to user-configured decimal places
        val rounded = result.setScale(decimalPlaces, roundingMode)
        return if (rounded.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            rounded.stripTrailingZeros().toPlainString()
        }
    }

    fun formatValue(value: BigDecimal): String {
        return if (value.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            value.stripTrailingZeros().toPlainString()
        }
    }

    /**
     * Formats the verification string.
     */
    fun formatVerification(
        result: BigDecimal,
        index: Int,
        value: BigDecimal,
        isExact: Boolean,
        displayedResult: String
    ): String {
        val valStr = formatValue(value)
        val sup = toSuperscript(index)
        return if (isExact) {
            val resStr = formatValue(result)
            if (result < BigDecimal.ZERO) {
                "($resStr)$sup = $valStr ✓"
            } else {
                "$resStr$sup = $valStr ✓"
            }
        } else {
            "($displayedResult)$sup ≈ $valStr"
        }
    }

    /**
     * Generates plain text representation for copying to clipboard.
     */
    fun formatCopyText(
        rootType: RootType,
        index: Int,
        value: BigDecimal,
        result: RootFindResult,
        displayedResult: String
    ): String {
        val typeTitle = when (rootType) {
            RootType.SQUARE -> "Square Root"
            RootType.CUBE -> "Cube Root"
            RootType.CUSTOM -> "Custom Root"
        }

        val valStr = formatValue(value)
        val symbol = getRootSymbol(index)

        if (!result.isReal || result.result == null) {
            return buildString {
                appendLine("RootFind")
                appendLine()
                appendLine(typeTitle)
                appendLine()
                val valDisplay = if (value < BigDecimal.ZERO) "($valStr)" else valStr
                appendLine("$symbol$valDisplay: No real result")
                appendLine()
                appendLine("An even root of a negative number is not a real number.")
            }.trimEnd()
        }

        val sign = if (result.isExact) "=" else "≈"
        val statusText = if (result.isExact) "Exact Result" else "Approximate Result"
        val valDisplay = if (value < BigDecimal.ZERO) "($valStr)" else valStr

        return buildString {
            appendLine("RootFind")
            appendLine()
            appendLine(typeTitle)
            appendLine()
            appendLine("$symbol$valDisplay $sign $displayedResult")
            appendLine()
            appendLine(statusText)
            if (result.verificationExpression != null) {
                appendLine()
                appendLine("Verification:")
                // Remove trailing checkmark for cleaner verification text if present
                appendLine(result.verificationExpression.removeSuffix(" ✓"))
            }
        }.trimEnd()
    }

    /**
     * Generates plain text representation for sharing.
     */
    fun formatShareText(
        rootType: RootType,
        index: Int,
        value: BigDecimal,
        result: RootFindResult,
        displayedResult: String
    ): String {
        return formatCopyText(rootType, index, value, result, displayedResult)
    }

    /**
     * Generates accessible content descriptions for talkback/screen readers.
     */
    fun getAccessibilityDescription(
        rootType: RootType,
        index: Int,
        value: BigDecimal,
        result: RootFindResult?,
        displayedResult: String
    ): String {
        val rootName = when (index) {
            2 -> "Square root"
            3 -> "Cube root"
            4 -> "Fourth root"
            5 -> "Fifth root"
            else -> "${toSuperscript(index)} root"
        }
        val valStr = formatValue(value)

        if (result == null) {
            return "$rootName of $valStr"
        }
        if (!result.isReal) {
            return "$rootName of $valStr has no real result. An even root of a negative number is not a real number."
        }
        val exactness = if (result.isExact) "Exact root result" else "Approximate root result"
        return "$rootName of $valStr is $exactness: $displayedResult"
    }
}
