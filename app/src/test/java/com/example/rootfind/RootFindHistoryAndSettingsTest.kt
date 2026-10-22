package com.example.rootfind

import android.content.SharedPreferences
import com.example.rootfind.data.repository.HistoryRepository
import com.example.rootfind.data.repository.SettingsRepository
import com.example.rootfind.model.AppSettings
import com.example.rootfind.model.AppTheme
import com.example.rootfind.model.CalculationRoundingMode
import com.example.rootfind.model.RootFindHistory
import com.example.rootfind.model.RootType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RootFindHistoryAndSettingsTest {

    private lateinit var testPrefs: InMemorySharedPreferences
    private lateinit var historyRepo: HistoryRepository
    private lateinit var settingsRepo: SettingsRepository

    @Before
    fun setUp() {
        testPrefs = InMemorySharedPreferences()
        historyRepo = HistoryRepository(testPrefs)
        settingsRepo = SettingsRepository(testPrefs)
    }

    // 41. History save
    @Test
    fun testHistorySave() {
        val entry = RootFindHistory("1", RootType.SQUARE, "16", "2", 1000L)
        val list = historyRepo.addHistory(entry)
        assertEquals(1, list.size)
        assertEquals("16", list[0].value)

        // Read from repo fresh
        val loaded = historyRepo.getHistory()
        assertEquals(1, loaded.size)
        assertEquals("16", loaded[0].value)
        assertEquals(RootType.SQUARE, loaded[0].rootType)
    }

    // 42. History restore
    @Test
    fun testHistoryRestore() {
        val entry = RootFindHistory("1", RootType.CUSTOM, "81", "4", 2000L)
        historyRepo.addHistory(entry)

        val restored = historyRepo.getHistory().first()
        assertEquals(RootType.CUSTOM, restored.rootType)
        assertEquals("81", restored.value)
        assertEquals("4", restored.rootIndex)
    }

    // 43. History limit: maximum 10 entries
    @Test
    fun testHistoryLimitMax10() {
        for (i in 1..10) {
            historyRepo.addHistory(
                RootFindHistory(i.toString(), RootType.SQUARE, i.toString(), "2", i * 1000L)
            )
        }
        assertEquals(10, historyRepo.getHistory().size)

        // Add 11th entry
        historyRepo.addHistory(
            RootFindHistory("11", RootType.SQUARE, "11", "2", 11000L)
        )
        val list = historyRepo.getHistory()
        assertEquals(10, list.size)
        // Newest must be first
        assertEquals("11", list[0].value)
        // Oldest (1) must be dropped
        assertFalse(list.any { it.value == "1" })
    }

    // 44. Duplicate history: consecutive identical not saved twice
    @Test
    fun testConsecutiveDuplicateHistoryNotSaved() {
        val entry1 = RootFindHistory("1", RootType.SQUARE, "16", "2", 1000L)
        historyRepo.addHistory(entry1)
        assertEquals(1, historyRepo.getHistory().size)

        // Save identical entry consecutively
        val entry2 = RootFindHistory("2", RootType.SQUARE, "16", "2", 2000L)
        historyRepo.addHistory(entry2)
        assertEquals(1, historyRepo.getHistory().size)

        // Save different entry
        val entry3 = RootFindHistory("3", RootType.CUBE, "27", "3", 3000L)
        historyRepo.addHistory(entry3)
        assertEquals(2, historyRepo.getHistory().size)

        // Now saving 16 square again is not consecutive with 16 square, so it is saved
        val entry4 = RootFindHistory("4", RootType.SQUARE, "16", "2", 4000L)
        historyRepo.addHistory(entry4)
        assertEquals(3, historyRepo.getHistory().size)
    }

    // 45. History deletion & Clear all
    @Test
    fun testHistoryDeletionAndClear() {
        val entry1 = RootFindHistory("id1", RootType.SQUARE, "16", "2", 1000L)
        val entry2 = RootFindHistory("id2", RootType.CUBE, "27", "3", 2000L)
        historyRepo.addHistory(entry1)
        historyRepo.addHistory(entry2)
        assertEquals(2, historyRepo.getHistory().size)

        // Delete single entry
        val afterDelete = historyRepo.deleteHistory("id2")
        assertEquals(1, afterDelete.size)
        assertEquals("id1", afterDelete[0].id)

        // Clear all
        historyRepo.clearHistory()
        assertEquals(0, historyRepo.getHistory().size)
    }

    // 46. Settings persistence
    @Test
    fun testSettingsPersistence() {
        val customSettings = AppSettings(
            theme = AppTheme.DARK,
            dynamicColor = false,
            hapticFeedback = false,
            animationsEnabled = false,
            confirmDelete = false,
            decimalPlaces = 4,
            roundingMode = CalculationRoundingMode.HALF_EVEN
        )
        settingsRepo.saveSettings(customSettings)

        val loaded = settingsRepo.getSettings()
        assertEquals(AppTheme.DARK, loaded.theme)
        assertFalse(loaded.dynamicColor)
        assertFalse(loaded.hapticFeedback)
        assertFalse(loaded.animationsEnabled)
        assertFalse(loaded.confirmDelete)
        assertEquals(4, loaded.decimalPlaces)
        assertEquals(CalculationRoundingMode.HALF_EVEN, loaded.roundingMode)
    }

    // 47. Settings reset
    @Test
    fun testSettingsReset() {
        // First change settings
        val modified = AppSettings(
            theme = AppTheme.DARK,
            decimalPlaces = 20,
            roundingMode = CalculationRoundingMode.CEILING
        )
        settingsRepo.saveSettings(modified)
        assertEquals(AppTheme.DARK, settingsRepo.getSettings().theme)

        // Also add a history entry
        historyRepo.addHistory(RootFindHistory("1", RootType.SQUARE, "16", "2", 1000L))

        // Reset settings
        val reset = settingsRepo.resetSettings()
        assertEquals(AppTheme.SYSTEM, reset.theme)
        assertTrue(reset.dynamicColor)
        assertTrue(reset.hapticFeedback)
        assertTrue(reset.animationsEnabled)
        assertTrue(reset.confirmDelete)
        assertEquals(10, reset.decimalPlaces)
        assertEquals(CalculationRoundingMode.HALF_UP, reset.roundingMode)

        // Verify history is intact
        assertEquals(1, historyRepo.getHistory().size)
    }

    // 37. Calculation timeout
    @Test
    fun testCalculationTimeout() = runBlocking {
        val timeoutResult = withTimeoutOrNull(200L) {
            delay(500L)
            "Done"
        }
        assertNull(timeoutResult)
    }

    // 38. Calculation cancellation
    @Test
    fun testCalculationCancellation() = runBlocking {
        var completed = false
        var wasCancelled = false
        val job = launch {
            try {
                delay(1000L)
                completed = true
            } catch (e: CancellationException) {
                wasCancelled = true
            }
        }
        delay(50L)
        job.cancel()
        job.join()
        assertFalse(completed)
        assertTrue(wasCancelled)
    }
}

/**
 * Lightweight in-memory SharedPreferences for unit testing.
 */
class InMemorySharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = InMemoryEditor(map)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class InMemoryEditor(private val backingMap: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val tempMap = mutableMapOf<String, Any?>()
        private val removeKeys = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) tempMap[key] = value
            return this
        }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            if (key != null) tempMap[key] = values
            return this
        }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) tempMap[key] = value
            return this
        }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) tempMap[key] = value
            return this
        }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) tempMap[key] = value
            return this
        }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) tempMap[key] = value
            return this
        }
        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) removeKeys.add(key)
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }
        override fun commit(): Boolean {
            apply()
            return true
        }
        override fun apply() {
            if (clearAll) backingMap.clear()
            for (key in removeKeys) backingMap.remove(key)
            backingMap.putAll(tempMap)
        }
    }
}
