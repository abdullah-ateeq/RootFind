package com.example.rootfind.model

import java.math.RoundingMode

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class CalculationRoundingMode(val roundingMode: RoundingMode, val displayName: String) {
    HALF_UP(RoundingMode.HALF_UP, "Half Up"),
    HALF_DOWN(RoundingMode.HALF_DOWN, "Half Down"),
    HALF_EVEN(RoundingMode.HALF_EVEN, "Half Even"),
    UP(RoundingMode.UP, "Up"),
    DOWN(RoundingMode.DOWN, "Down"),
    CEILING(RoundingMode.CEILING, "Ceiling"),
    FLOOR(RoundingMode.FLOOR, "Floor");

    companion object {
        fun fromDisplayName(name: String): CalculationRoundingMode =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: HALF_UP
    }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val confirmDelete: Boolean = true,
    val decimalPlaces: Int = 10,
    val roundingMode: CalculationRoundingMode = CalculationRoundingMode.HALF_UP
) {
    companion object {
        val ALLOWED_DECIMAL_PLACES = listOf(0, 2, 4, 6, 8, 10, 12, 16, 20, 30, 50)
    }
}
