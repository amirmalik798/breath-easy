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

    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            operationMutex.withLock {
                _busy.value = true
                try {
                    _snapshot.value = repository.loadSnapshot()
                    _loadError.value = false
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    // Do not leave the app in its initial loading state forever. Keep
                    // the last good snapshot (if any) and expose an explicit retry path.
                    _snapshot.value = _snapshot.value.copy(isLoading = false)
                    _loadError.value = true
                    _message.value = "Your local records could not be loaded."
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

    fun logSmoke(event: SmokeEvent, confirmUnstockedSmoking: Boolean = false) =
        perform(successMessage = null) {
            repository.logSmoke(event, confirmUnstockedSmoking)
        }

    fun logCraving(event: CravingEvent, confirmUnstockedSmoking: Boolean = false) = perform(
        if (event.outcome == CravingOutcome.RESISTED) "Craving recorded as resisted" else "Craving and cigarette recorded",
    ) { repository.logCraving(event, confirmUnstockedSmoking) }

    fun clearMessage() { _message.value = null }

    private fun perform(successMessage: String?, operation: suspend () -> Unit) {
        viewModelScope.launch {
            operationMutex.withLock {
                _busy.value = true
                try {
                    try {
                        operation()
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        _message.value = error.message?.takeIf { it.length <= 140 }
                            ?: "That action could not be saved. Please try again."
                        return@withLock
                    }

                    // The write is already committed at this point. A later read failure
                    // must not be reported as a failed write, or users may retry and
                    // create duplicate purchases or activity records.
                    _message.value = successMessage
                    try {
                        _snapshot.value = repository.loadSnapshot()
                        _loadError.value = false
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        _snapshot.value = _snapshot.value.copy(isLoading = false)
                        _loadError.value = true
                        _message.value = successMessage?.let {
                            "$it. Saved, but the latest data could not be reloaded."
                        } ?: "Saved, but the latest data could not be reloaded."
                    }
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
