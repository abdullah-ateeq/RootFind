package com.example.rootfind.model

import java.math.BigDecimal

data class RootFindResult(
    val value: BigDecimal,
    val rootIndex: Int,
    val result: BigDecimal?,
    val isExact: Boolean,
    val isReal: Boolean,
    val isPerfectPower: Boolean,
    val calculationSteps: List<CalculationStep>,
    val verificationExpression: String?,
    val calculatedAt: Long = System.currentTimeMillis()
)
