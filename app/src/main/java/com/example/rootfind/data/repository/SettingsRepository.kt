package com.example.rootfind.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rootfind.model.AppSettings
import com.example.rootfind.model.AppTheme
import com.example.rootfind.model.CalculationRoundingMode
import com.example.rootfind.model.RootType

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("rootfind_settings", Context.MODE_PRIVATE)
    )

    fun getSettings(): AppSettings {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try {
            AppTheme.valueOf(themeName)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true)
        val confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true)
        val decimalPlaces = prefs.getInt(KEY_DECIMAL_PLACES, 10)

        val roundingModeName =
            prefs.getString(KEY_ROUNDING_MODE, CalculationRoundingMode.HALF_UP.name)
                ?: CalculationRoundingMode.HALF_UP.name
        val roundingMode = try {
            CalculationRoundingMode.valueOf(roundingModeName)
        } catch (e: Exception) {
            CalculationRoundingMode.HALF_UP
        }

        return AppSettings(
            theme = theme,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animationsEnabled = animationsEnabled,
            confirmDelete = confirmDelete,
            decimalPlaces = decimalPlaces,
            roundingMode = roundingMode
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS_ENABLED, settings.animationsEnabled)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putInt(KEY_DECIMAL_PLACES, settings.decimalPlaces)
            .putString(KEY_ROUNDING_MODE, settings.roundingMode.name)
            .apply()
    }

    fun resetSettings(): AppSettings {
        val defaultSettings = AppSettings()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    fun getLastRootType(): RootType {
        val typeStr = prefs.getString(KEY_LAST_ROOT_TYPE, RootType.SQUARE.name)
        return try {
            RootType.valueOf(typeStr ?: RootType.SQUARE.name)
        } catch (e: Exception) {
            RootType.SQUARE
        }
    }

    fun saveLastRootType(type: RootType) {
        prefs.edit().putString(KEY_LAST_ROOT_TYPE, type.name).apply()
    }

    fun getLastValue(): String {
        return prefs.getString(KEY_LAST_VALUE, "") ?: ""
    }

    fun saveLastValue(value: String) {
        prefs.edit().putString(KEY_LAST_VALUE, value).apply()
    }

    fun getLastCustomRootIndex(): String {
        return prefs.getString(KEY_LAST_CUSTOM_ROOT_INDEX, "2") ?: "2"
    }

    fun saveLastCustomRootIndex(index: String) {
        prefs.edit().putString(KEY_LAST_CUSTOM_ROOT_INDEX, index).apply()
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_ROUNDING_MODE = "rounding_mode"
        private const val KEY_LAST_ROOT_TYPE = "last_root_type"
        private const val KEY_LAST_VALUE = "last_value"
        private const val KEY_LAST_CUSTOM_ROOT_INDEX = "last_custom_root_index"
    }
}
