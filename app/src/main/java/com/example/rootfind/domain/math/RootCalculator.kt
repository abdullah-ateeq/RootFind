package com.example.rootfind.domain.math

import com.example.rootfind.model.CalculationStep
import com.example.rootfind.model.RootFindResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object RootCalculator {

    fun calculateRoot(
        value: BigDecimal,
        rootIndex: Int,
        mathContext: MathContext = DecimalRootCalculator.DEFAULT_MATH_CONTEXT
    ): Result<RootFindResult> {
        if (rootIndex < 1) {
            return Result.failure(IllegalArgumentException("Root index must be a positive whole number."))
        }
        if (rootIndex > RootFindValidator.MAX_ROOT_INDEX) {
            return Result.failure(IllegalArgumentException("Root index is too large. Maximum is ${RootFindValidator.MAX_ROOT_INDEX}."))
        }

        val valStr = RootFindFormatter.formatValue(value)
        val sup = RootFindFormatter.toSuperscript(rootIndex)
        val symbol = RootFindFormatter.getRootSymbol(rootIndex)

        // Case 1: Negative value with even root index -> No real result
        if (value < BigDecimal.ZERO && rootIndex % 2 == 0) {
            val steps = listOf(
                CalculationStep(
                    label = "Target equation",
                    expression = "Find x where:",
                    result = "x$sup = $valStr"
                ),
                CalculationStep(
                    label = "Real power rule",
                    expression = "Even power of real numbers:",
                    result = "x$sup ≥ 0 for all real x"
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "An even root of a negative number is not a real number."
                )
            )
            return Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = rootIndex,
                    result = null,
                    isExact = false,
                    isReal = false,
                    isPerfectPower = false,
                    calculationSteps = steps,
                    verificationExpression = null
                )
            )
        }

        // Case 2: Root index 1 -> value itself
        if (rootIndex == 1) {
            val steps = listOf(
                CalculationStep(
                    label = "First root rule",
                    expression = "Identity property:",
                    result = "The first root of a value is the value itself."
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "¹√$valStr = $valStr"
                )
            )
            val verification = "$valStr¹ = $valStr ✓"
            return Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = 1,
                    result = value,
                    isExact = true,
                    isReal = true,
                    isPerfectPower = true,
                    calculationSteps = steps,
                    verificationExpression = verification
                )
            )
        }

        // Case 3: Zero
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            val steps = listOf(
                CalculationStep(
                    label = "Target equation",
                    expression = "Find x where:",
                    result = "x$sup = 0"
                ),
                CalculationStep(
                    label = "Zero property",
                    expression = "Power of zero:",
                    result = "0$sup = 0"
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "$symbol 0 = 0"
                )
            )
            val verification = "0$sup = 0 ✓"
            return Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = rootIndex,
                    result = BigDecimal.ZERO,
                    isExact = true,
                    isReal = true,
                    isPerfectPower = true,
                    calculationSteps = steps,
                    verificationExpression = verification
                )
            )
        }

        // Case 4: One
        if (value.compareTo(BigDecimal.ONE) == 0) {
            val steps = listOf(
                CalculationStep(
                    label = "Target equation",
                    expression = "Find x where:",
                    result = "x$sup = 1"
                ),
                CalculationStep(
                    label = "Unity property",
                    expression = "Power of unity:",
                    result = "1$sup = 1"
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "$symbol 1 = 1"
                )
            )
            val verification = "1$sup = 1 ✓"
            return Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = rootIndex,
                    result = BigDecimal.ONE,
                    isExact = true,
                    isReal = true,
                    isPerfectPower = true,
                    calculationSteps = steps,
                    verificationExpression = verification
                )
            )
        }

        // Case 5: Negative value with odd root index
        if (value < BigDecimal.ZERO && rootIndex % 2 != 0) {
            val absValue = value.abs()
            val exactAbs = DecimalRootCalculator.exactDecimalRootOrNull(absValue, rootIndex)
            return if (exactAbs != null) {
                val exactResult = exactAbs.negate()
                val resStr = RootFindFormatter.formatValue(exactResult)
                val steps = listOf(
                    CalculationStep(
                        label = "Target equation",
                        expression = "Find x where:",
                        result = "x$sup = $valStr"
                    ),
                    CalculationStep(
                        label = "Power evaluation",
                        expression = "Direct computation:",
                        result = "($resStr)$sup = $valStr"
                    ),
                    CalculationStep(
                        label = "Conclusion",
                        expression = "Therefore:",
                        result = "$symbol($valStr) = $resStr"
                    )
                )
                val verification = "($resStr)$sup = $valStr ✓"
                Result.success(
                    RootFindResult(
                        value = value,
                        rootIndex = rootIndex,
                        result = exactResult,
                        isExact = true,
                        isReal = true,
                        isPerfectPower = true,
                        calculationSteps = steps,
                        verificationExpression = verification
                    )
                )
            } else {
                return try {
                    val approxAbs = DecimalRootCalculator.decimalNthRoot(absValue, rootIndex, mathContext)
                    val approxResult = approxAbs.negate()
                    val displayed = RootFindFormatter.formatResult(approxResult, false, 10, RoundingMode.HALF_UP)
                    val steps = listOf(
                        CalculationStep(
                            label = "Target equation",
                            expression = "Find x where:",
                            result = "x$sup = $valStr"
                        ),
                        CalculationStep(
                            label = "Odd root property",
                            expression = "Negative rule:",
                            result = "$symbol($valStr) = -($symbol${RootFindFormatter.formatValue(absValue)})"
                        ),
                        CalculationStep(
                            label = "Conclusion",
                            expression = "Therefore:",
                            result = "$symbol($valStr) ≈ $displayed"
                        )
                    )
                    val verification = "($displayed)$sup ≈ $valStr"
                    Result.success(
                        RootFindResult(
                            value = value,
                            rootIndex = rootIndex,
                            result = approxResult,
                            isExact = false,
                            isReal = true,
                            isPerfectPower = false,
                            calculationSteps = steps,
                            verificationExpression = verification
                        )
                    )
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        // Case 6: Positive value
        val exactRoot = DecimalRootCalculator.exactDecimalRootOrNull(value, rootIndex)
        if (exactRoot != null) {
            val resStr = RootFindFormatter.formatValue(exactRoot)
            val steps = listOf(
                CalculationStep(
                    label = "Target equation",
                    expression = "Find x where:",
                    result = "x$sup = $valStr"
                ),
                CalculationStep(
                    label = "Power evaluation",
                    expression = "Direct computation:",
                    result = "$resStr$sup = $valStr"
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "$symbol$valStr = $resStr"
                )
            )
            val verification = "$resStr$sup = $valStr ✓"
            return Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = rootIndex,
                    result = exactRoot,
                    isExact = true,
                    isReal = true,
                    isPerfectPower = true,
                    calculationSteps = steps,
                    verificationExpression = verification
                )
            )
        }

        // Approximate root
        return try {
            val approxRoot = DecimalRootCalculator.decimalNthRoot(value, rootIndex, mathContext)
            val displayed = RootFindFormatter.formatResult(approxRoot, false, 10, RoundingMode.HALF_UP)
            val steps = listOf(
                CalculationStep(
                    label = "Target equation",
                    expression = "Find x where:",
                    result = "x$sup = $valStr"
                ),
                CalculationStep(
                    label = "Power evaluation",
                    expression = "Exact test:",
                    result = "No integer x produces exactly $valStr."
                ),
                CalculationStep(
                    label = "Conclusion",
                    expression = "Therefore:",
                    result = "$symbol$valStr ≈ $displayed"
                )
            )
            val verification = "($displayed)$sup ≈ $valStr"
            Result.success(
                RootFindResult(
                    value = value,
                    rootIndex = rootIndex,
                    result = approxRoot,
                    isExact = false,
                    isReal = true,
                    isPerfectPower = false,
                    calculationSteps = steps,
                    verificationExpression = verification
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
