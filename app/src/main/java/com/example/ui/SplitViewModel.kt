package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.audio.SoundEffects
import com.example.data.PreferencesManager
import com.example.data.SplitDatabase
import com.example.data.SplitRepository
import com.example.engine.CurrencyConfig
import com.example.engine.RoundingOption
import com.example.engine.SplitEngine
import com.example.engine.SplitMode
import com.example.engine.SplitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object SetupParticipants : Screen
    data object SetupBill : Screen
    data object SetupRandomness : Screen
    data object FateAnimation : Screen
    data class IndividualReveal(val currentIndex: Int) : Screen
    data object FinalResults : Screen
    data object History : Screen
    data object Settings : Screen
    data class HistoryDetail(val result: SplitResult) : Screen
}

data class SplitUiState(
    val currentScreen: Screen = Screen.Home,
    val peopleCount: Int = 4,
    val participantNames: List<String> = listOf("", "", "", ""),
    val billAmountText: String = "",
    val selectedCurrency: CurrencyConfig = CurrencyConfig.INR,
    val selectedMode: SplitMode = SplitMode.CHAOS,
    val selectedRounding: RoundingOption = RoundingOption.CLEAN_NOTE,
    val activeResult: SplitResult? = null,
    val historySplits: List<SplitResult> = emptyList(),
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isReducedMotion: Boolean = false,
    val validationError: String? = null
)

class SplitViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val database = SplitDatabase.getInstance(application)
    private val repository = SplitRepository(database.splitDao())

    private val _uiState = MutableStateFlow(
        SplitUiState(
            selectedCurrency = CurrencyConfig.fromCode(prefs.defaultCurrency),
            isSoundEnabled = prefs.soundEnabled,
            isHapticsEnabled = prefs.hapticsEnabled,
            isReducedMotion = prefs.reducedMotion
        )
    )
    val uiState: StateFlow<SplitUiState> = _uiState.asStateFlow()

    init {
        SoundEffects.isSoundEnabled = prefs.soundEnabled

        viewModelScope.launch {
            repository.allSplits.map { list ->
                list.map { SplitRepository.toResult(it) }
            }.collect { splits ->
                _uiState.update { it.copy(historySplits = splits) }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, validationError = null) }
    }

    fun setPeopleCount(count: Int) {
        val clamped = count.coerceIn(2, 20)
        _uiState.update { state ->
            val currentNames = state.participantNames
            val newNames = if (clamped > currentNames.size) {
                currentNames + List(clamped - currentNames.size) { "" }
            } else {
                currentNames.take(clamped)
            }
            state.copy(peopleCount = clamped, participantNames = newNames, validationError = null)
        }
    }

    fun updateName(index: Int, name: String) {
        _uiState.update { state ->
            val names = state.participantNames.toMutableList()
            if (index in names.indices) {
                names[index] = name
            }
            state.copy(participantNames = names, validationError = null)
        }
    }

    fun validateAndProceedFromParticipants(): Boolean {
        val state = _uiState.value
        val trimmed = state.participantNames.mapIndexed { idx, raw ->
            val t = raw.trim()
            if (t.isEmpty()) "Person ${idx + 1}" else t
        }

        // Check for duplicates
        val duplicateCheck = trimmed.groupingBy { it.lowercase() }.eachCount()
        val duplicates = duplicateCheck.filter { it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            _uiState.update {
                it.copy(validationError = "Please ensure all participant names are distinct (${duplicates.first().replaceFirstChar { c -> c.uppercase() }} is duplicated)")
            }
            return false
        }

        _uiState.update {
            it.copy(
                participantNames = trimmed,
                validationError = null,
                currentScreen = Screen.SetupBill
            )
        }
        return true
    }

    fun setBillAmountText(text: String) {
        // Only allow valid numbers and one decimal dot
        val filtered = text.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _uiState.update { it.copy(billAmountText = filtered, validationError = null) }
        }
    }

    fun setCurrency(currency: CurrencyConfig) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun validateAndProceedFromBill(): Boolean {
        val state = _uiState.value
        val amount = state.billAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(validationError = "Please enter a valid bill amount greater than 0") }
            return false
        }
        if (amount > 10_000_000.0) {
            _uiState.update { it.copy(validationError = "Bill amount exceeds maximum supported limit") }
            return false
        }

        _uiState.update {
            it.copy(validationError = null, currentScreen = Screen.SetupRandomness)
        }
        return true
    }

    fun setMode(mode: SplitMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setRounding(rounding: RoundingOption) {
        _uiState.update { it.copy(selectedRounding = rounding) }
    }

    fun startFateCalculation() {
        val state = _uiState.value
        val billAmount = state.billAmountText.toDoubleOrNull() ?: return
        val names = state.participantNames.mapIndexed { idx, n ->
            n.trim().ifEmpty { "Person ${idx + 1}" }
        }

        val result = SplitEngine.calculateSplit(
            names = names,
            totalBill = billAmount,
            currency = state.selectedCurrency,
            mode = state.selectedMode,
            roundingOption = state.selectedRounding
        )

        _uiState.update {
            it.copy(
                activeResult = result,
                currentScreen = if (state.isReducedMotion) Screen.IndividualReveal(0) else Screen.FateAnimation
            )
        }

        // Ensure interstitial ad is preloading while Fate animation runs
        AdMobManager.preloadInterstitial(getApplication())

        // Save to Room database in background
        viewModelScope.launch {
            repository.insert(result)
        }
    }

    fun onAnimationFinished() {
        _uiState.update { it.copy(currentScreen = Screen.IndividualReveal(0)) }
    }

    fun nextReveal() {
        val current = _uiState.value.currentScreen
        if (current is Screen.IndividualReveal) {
            val total = _uiState.value.activeResult?.participants?.size ?: 0
            val nextIdx = current.currentIndex + 1
            if (nextIdx < total) {
                _uiState.update { it.copy(currentScreen = Screen.IndividualReveal(nextIdx)) }
            } else {
                _uiState.update { it.copy(currentScreen = Screen.FinalResults) }
            }
        }
    }

    fun skipToFinalResults() {
        _uiState.update { it.copy(currentScreen = Screen.FinalResults) }
    }

    fun startNewSplit() {
        _uiState.update {
            it.copy(
                currentScreen = Screen.Home,
                billAmountText = "",
                activeResult = null,
                validationError = null
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun toggleSound() {
        val newVal = !_uiState.value.isSoundEnabled
        prefs.soundEnabled = newVal
        SoundEffects.isSoundEnabled = newVal
        _uiState.update { it.copy(isSoundEnabled = newVal) }
    }

    fun toggleHaptics() {
        val newVal = !_uiState.value.isHapticsEnabled
        prefs.hapticsEnabled = newVal
        _uiState.update { it.copy(isHapticsEnabled = newVal) }
    }

    fun toggleReducedMotion() {
        val newVal = !_uiState.value.isReducedMotion
        prefs.reducedMotion = newVal
        _uiState.update { it.copy(isReducedMotion = newVal) }
    }

    fun setDefaultCurrency(code: String) {
        prefs.defaultCurrency = code
        val config = CurrencyConfig.fromCode(code)
        _uiState.update { it.copy(selectedCurrency = config) }
    }
}
