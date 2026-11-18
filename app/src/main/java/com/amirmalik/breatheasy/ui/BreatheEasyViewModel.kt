package com.amirmalik.breatheasy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.AppSnapshot
import com.amirmalik.breatheasy.data.BreatheEasyRepository
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class BreatheEasyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BreatheEasyRepository(application)

    private val _snapshot = MutableStateFlow(AppSnapshot())
    val snapshot: StateFlow<AppSnapshot> = _snapshot.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _busy.value = true
            try {
                _snapshot.value = repository.loadSnapshot()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _message.value = "Your local records could not be loaded. Please reopen the app."
            } finally {
                _busy.value = false
            }
        }
    }

    fun savePreferences(preferences: AppPreferences) = perform("Settings saved") {
        require(preferences.dailyTarget in 1..200) { "Daily target must be between 1 and 200." }
        require(preferences.baselinePerDay in 0..200) { "Baseline must be between 0 and 200." }
        require(preferences.startingStockCigarettes in 0..100_000) { "Starting stock must be between 0 and 100,000." }
        validateFxRate(preferences.pkrPerUsd, "PKR")
        validateFxRate(preferences.rwfPerUsd, "RWF")
        repository.savePreferences(preferences)
    }

    fun recordPurchase(purchase: Purchase) = perform("Purchase added to your ledger") {
        val current = _snapshot.value.preferences
        repository.recordPurchase(
            purchase.copy(
                fxSnapshot = current.fxSnapshot(),
                brand = purchase.brand.trim().take(60),
                location = purchase.location.trim().take(80),
                note = purchase.note.trim().take(500),
            ),
        )
    }

    fun deletePurchase(id: String) = perform("Purchase removed. Monthly totals have been updated.") {
        repository.deletePurchase(id)
    }

    fun logSmoke(trigger: com.amirmalik.breatheasy.data.CravingTrigger, note: String = "") =
        perform("Cigarette logged. No judgement — just useful data.") {
            repository.logSmoke(SmokeEvent(trigger = trigger, note = note.trim().take(500)))
        }

    fun logCraving(event: CravingEvent) = perform(
        if (event.outcome.name == "RESISTED") "Craving recorded as resisted" else "Craving and cigarette recorded",
    ) { repository.logCraving(event.copy(note = event.note.trim().take(500))) }

    fun clearMessage() { _message.value = null }

    private fun perform(successMessage: String, operation: suspend () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                operation()
                _snapshot.value = repository.loadSnapshot()
                _message.value = successMessage
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _message.value = error.message?.takeIf { it.length <= 140 }
                    ?: "That action could not be saved. Please try again."
            } finally {
                _busy.value = false
            }
        }
    }

    private fun validateFxRate(raw: String, currency: String) {
        if (raw.isBlank()) return
        val rate = raw.toBigDecimalOrNull()
        require(rate != null && rate.signum() > 0) { "Enter a valid positive $currency-per-USD rate." }
        require(rate.scale() <= 6) { "$currency-per-USD rate can have at most 6 decimal places." }
        require(rate.precision() <= 18) { "$currency-per-USD rate is too large." }
    }

    companion object {
        fun todayRangeMillis(): LongRange {
            val zone = ZoneId.systemDefault()
            val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return start until end
        }
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}
