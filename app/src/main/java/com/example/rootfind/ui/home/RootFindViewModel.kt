package com.example.rootfind.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rootfind.data.repository.HistoryRepository
import com.example.rootfind.data.repository.SettingsRepository
import com.example.rootfind.domain.math.DecimalRootCalculator
import com.example.rootfind.domain.math.RootCalculator
import com.example.rootfind.domain.math.RootFindFormatter
import com.example.rootfind.domain.math.RootFindParser
import com.example.rootfind.domain.math.RootFindValidator
import com.example.rootfind.model.AppSettings
import com.example.rootfind.model.RootFindHistory
import com.example.rootfind.model.RootFindResult
import com.example.rootfind.model.RootType
import com.example.rootfind.util.HapticFeedbackUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.math.BigDecimal
import java.util.UUID

data class RootFindUiState(
    val rootType: RootType = RootType.SQUARE,
    val valueInput: String = "",
    val rootIndexInput: String = "2",
    val result: RootFindResult? = null,
    val displayedResult: String = "",
    val history: List<RootFindHistory> = emptyList(),
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    val isCalculationExpanded: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val showClearHistoryDialog: Boolean = false,
    val showResetSettingsDialog: Boolean = false,
    val itemToDelete: RootFindHistory? = null,
    val userNotice: String? = null
)

class RootFindViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val historyRepo = HistoryRepository(application)

    private val _uiState = MutableStateFlow(RootFindUiState())
    val uiState: StateFlow<RootFindUiState> = _uiState.asStateFlow()

    private var calculationJob: Job? = null

    init {
        val loadedSettings = settingsRepo.getSettings()
        val loadedHistory = historyRepo.getHistory()
        val lastType = settingsRepo.getLastRootType()
        val lastVal = settingsRepo.getLastValue()
        val lastCustomIndex = settingsRepo.getLastCustomRootIndex()

        val initialIndex = when (lastType) {
            RootType.SQUARE -> "2"
            RootType.CUBE -> "3"
            RootType.CUSTOM -> if (lastCustomIndex.isNotBlank()) lastCustomIndex else "2"
        }

        _uiState.update {
            it.copy(
                settings = loadedSettings,
                history = loadedHistory,
                rootType = lastType,
                valueInput = lastVal,
                rootIndexInput = initialIndex
            )
        }
    }

    fun setRootType(type: RootType) {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        settingsRepo.saveLastRootType(type)

        val newIndex = when (type) {
            RootType.SQUARE -> "2"
            RootType.CUBE -> "3"
            RootType.CUSTOM -> {
                val savedCustom = settingsRepo.getLastCustomRootIndex()
                if (savedCustom.isNotBlank()) savedCustom else "2"
            }
        }

        _uiState.update {
            it.copy(
                rootType = type,
                rootIndexInput = newIndex,
                errorMessage = null
            )
        }
    }

    fun onValueChange(value: String) {
        _uiState.update {
            it.copy(
                valueInput = value,
                errorMessage = null
            )
        }
        settingsRepo.saveLastValue(value)
    }

    fun onRootIndexChange(index: String) {
        _uiState.update {
            it.copy(
                rootIndexInput = index,
                errorMessage = null
            )
        }
        if (_uiState.value.rootType == RootType.CUSTOM) {
            settingsRepo.saveLastCustomRootIndex(index)
        }
    }

    fun setQuickRoot(index: Int) {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        val strIndex = index.toString()
        _uiState.update {
            it.copy(
                rootIndexInput = strIndex,
                errorMessage = null
            )
        }
        settingsRepo.saveLastCustomRootIndex(strIndex)
    }

    fun calculate() {
        val currentState = _uiState.value
        val parsedValueResult = RootFindParser.parseDecimal(currentState.valueInput)
        if (parsedValueResult.isFailure) {
            val error = parsedValueResult.exceptionOrNull()?.message ?: "Enter a valid decimal number."
            HapticFeedbackUtil.performErrorHaptic(getApplication(), currentState.settings.hapticFeedback)
            _uiState.update { it.copy(errorMessage = error, result = null, displayedResult = "") }
            return
        }

        val targetIndex = when (currentState.rootType) {
            RootType.SQUARE -> 2
            RootType.CUBE -> 3
            RootType.CUSTOM -> {
                val indexValidation = RootFindValidator.validateRootIndex(currentState.rootIndexInput)
                if (indexValidation.isFailure) {
                    val error = indexValidation.exceptionOrNull()?.message ?: "Root index must be a positive whole number."
                    HapticFeedbackUtil.performErrorHaptic(getApplication(), currentState.settings.hapticFeedback)
                    _uiState.update { it.copy(errorMessage = error, result = null, displayedResult = "") }
                    return
                }
                indexValidation.getOrThrow()
            }
        }

        val parsedValue = parsedValueResult.getOrThrow()
        HapticFeedbackUtil.performLightHaptic(getApplication(), currentState.settings.hapticFeedback)

        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculating = true,
                    errorMessage = null
                )
            }

            try {
                val resultOutcome = withTimeoutOrNull(5000L) {
                    withContext(Dispatchers.Default) {
                        RootCalculator.calculateRoot(
                            value = parsedValue,
                            rootIndex = targetIndex,
                            mathContext = DecimalRootCalculator.DEFAULT_MATH_CONTEXT
                        )
                    }
                }

                if (resultOutcome == null) {
                    // Timed out
                    _uiState.update {
                        it.copy(
                            isCalculating = false,
                            errorMessage = "Calculation took too long. Try a smaller value or lower precision.",
                            result = null,
                            displayedResult = ""
                        )
                    }
                    return@launch
                }

                if (resultOutcome.isSuccess) {
                    val rootResult = resultOutcome.getOrThrow()
                    val displayed = if (rootResult.isReal && rootResult.result != null) {
                        RootFindFormatter.formatResult(
                            result = rootResult.result,
                            isExact = rootResult.isExact,
                            decimalPlaces = currentState.settings.decimalPlaces,
                            roundingMode = currentState.settings.roundingMode.roundingMode
                        )
                    } else {
                        "No real result"
                    }

                    _uiState.update {
                        it.copy(
                            isCalculating = false,
                            result = rootResult,
                            displayedResult = displayed,
                            errorMessage = null
                        )
                    }
                } else {
                    val err = resultOutcome.exceptionOrNull()?.message
                        ?: "Unable to calculate this root at the selected precision."
                    _uiState.update {
                        it.copy(
                            isCalculating = false,
                            errorMessage = err,
                            result = null,
                            displayedResult = ""
                        )
                    }
                }
            } catch (c: CancellationException) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = "Calculation cancelled.",
                        result = null,
                        displayedResult = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = e.message ?: "An error occurred during calculation.",
                        result = null,
                        displayedResult = ""
                    )
                }
            }
        }
    }

    fun cancelCalculation() {
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                isCalculating = false,
                errorMessage = "Calculation cancelled."
            )
        }
    }

    fun toggleCalculationExpanded() {
        _uiState.update { it.copy(isCalculationExpanded = !it.isCalculationExpanded) }
    }

    fun saveToHistory() {
        val currentState = _uiState.value
        val result = currentState.result ?: return
        HapticFeedbackUtil.performLightHaptic(getApplication(), currentState.settings.hapticFeedback)

        val entry = RootFindHistory(
            id = UUID.randomUUID().toString(),
            rootType = currentState.rootType,
            value = currentState.valueInput.trim(),
            rootIndex = when (currentState.rootType) {
                RootType.SQUARE -> "2"
                RootType.CUBE -> "3"
                RootType.CUSTOM -> currentState.rootIndexInput.trim()
            },
            savedAt = System.currentTimeMillis()
        )

        val updated = historyRepo.addHistory(entry)
        _uiState.update {
            it.copy(
                history = updated,
                userNotice = "Calculation saved to history."
            )
        }
    }

    fun restoreHistory(item: RootFindHistory) {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                rootType = item.rootType,
                valueInput = item.value,
                rootIndexInput = item.rootIndex,
                errorMessage = null
            )
        }
        settingsRepo.saveLastRootType(item.rootType)
        settingsRepo.saveLastValue(item.value)
        if (item.rootType == RootType.CUSTOM) {
            settingsRepo.saveLastCustomRootIndex(item.rootIndex)
        }
        calculate()
    }

    fun requestDeleteHistoryItem(item: RootFindHistory) {
        val confirm = _uiState.value.settings.confirmDelete
        if (confirm) {
            _uiState.update { it.copy(itemToDelete = item) }
        } else {
            confirmDeleteHistoryItem(item)
        }
    }

    fun confirmDeleteHistoryItem(item: RootFindHistory? = null) {
        val target = item ?: _uiState.value.itemToDelete ?: return
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        val updated = historyRepo.deleteHistory(target.id)
        _uiState.update {
            it.copy(
                history = updated,
                itemToDelete = null,
                userNotice = "Calculation deleted."
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(itemToDelete = null) }
    }

    fun showClearHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showClearHistoryDialog = show) }
    }

    fun confirmClearHistory() {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        historyRepo.clearHistory()
        _uiState.update {
            it.copy(
                history = emptyList(),
                showClearHistoryDialog = false,
                userNotice = "History cleared."
            )
        }
    }

    fun showResetSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showResetSettingsDialog = show) }
    }

    fun confirmResetSettings() {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        val defaultSettings = settingsRepo.resetSettings()
        _uiState.update {
            val recomputedDisplayed = if (it.result?.result != null && it.result.isReal) {
                RootFindFormatter.formatResult(
                    result = it.result.result,
                    isExact = it.result.isExact,
                    decimalPlaces = defaultSettings.decimalPlaces,
                    roundingMode = defaultSettings.roundingMode.roundingMode
                )
            } else {
                it.displayedResult
            }
            it.copy(
                settings = defaultSettings,
                displayedResult = recomputedDisplayed,
                showResetSettingsDialog = false,
                userNotice = "Settings reset to defaults."
            )
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepo.saveSettings(newSettings)
        _uiState.update {
            val recomputedDisplayed = if (it.result?.result != null && it.result.isReal) {
                RootFindFormatter.formatResult(
                    result = it.result.result,
                    isExact = it.result.isExact,
                    decimalPlaces = newSettings.decimalPlaces,
                    roundingMode = newSettings.roundingMode.roundingMode
                )
            } else {
                it.displayedResult
            }
            it.copy(
                settings = newSettings,
                displayedResult = recomputedDisplayed
            )
        }
    }

    fun clearInputs() {
        HapticFeedbackUtil.performLightHaptic(getApplication(), _uiState.value.settings.hapticFeedback)
        val defaultIndex = when (_uiState.value.rootType) {
            RootType.SQUARE -> "2"
            RootType.CUBE -> "3"
            RootType.CUSTOM -> "2"
        }
        _uiState.update {
            it.copy(
                valueInput = "",
                rootIndexInput = defaultIndex,
                result = null,
                displayedResult = "",
                errorMessage = null,
                isCalculationExpanded = false
            )
        }
        settingsRepo.saveLastValue("")
        if (_uiState.value.rootType == RootType.CUSTOM) {
            settingsRepo.saveLastCustomRootIndex(defaultIndex)
        }
    }

    fun getCopyText(): String {
        val state = _uiState.value
        val result = state.result ?: return ""
        val index = when (state.rootType) {
            RootType.SQUARE -> 2
            RootType.CUBE -> 3
            RootType.CUSTOM -> state.rootIndexInput.toIntOrNull() ?: 2
        }
        val valueBd = result.value
        return RootFindFormatter.formatCopyText(
            rootType = state.rootType,
            index = index,
            value = valueBd,
            result = result,
            displayedResult = state.displayedResult
        )
    }

    fun getShareText(): String {
        return getCopyText()
    }

    fun dismissNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }
}
