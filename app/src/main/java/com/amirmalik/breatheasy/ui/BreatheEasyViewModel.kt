package com.amirmalik.breatheasy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.AppSnapshot
import com.amirmalik.breatheasy.data.BreatheEasyRepository
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.CravingOutcome
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BreatheEasyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BreatheEasyRepository(application)
    // Serialize reads and writes so overlapping user actions cannot publish stale
    // snapshots or race the transient busy/error UI state.
    private val operationMutex = Mutex()

    private val _snapshot = MutableStateFlow(AppSnapshot())
    val snapshot: StateFlow<AppSnapshot> = _snapshot.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            operationMutex.withLock {
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
    }

    fun savePreferences(preferences: AppPreferences) = perform("Settings saved") {
        repository.savePreferences(preferences)
    }

    fun recordPurchase(purchase: Purchase) = perform("Purchase added to your ledger") {
        repository.recordPurchase(purchase)
    }

    fun deletePurchase(id: String) = perform("Purchase removed. Monthly totals have been updated.") {
        repository.deletePurchase(id)
    }

    fun logSmoke(trigger: com.amirmalik.breatheasy.data.CravingTrigger, note: String = "") =
        perform("Cigarette logged. No judgement — just useful data.") {
            repository.logSmoke(SmokeEvent(trigger = trigger, note = note))
        }

    fun logCraving(event: CravingEvent) = perform(
        if (event.outcome == CravingOutcome.RESISTED) "Craving recorded as resisted" else "Craving and cigarette recorded",
    ) { repository.logCraving(event) }

    fun clearMessage() { _message.value = null }

    private fun perform(successMessage: String, operation: suspend () -> Unit) {
        viewModelScope.launch {
            operationMutex.withLock {
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
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}
