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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    // One-shot feedback should not use StateFlow: rapid identical updates can be
    // conflated before Compose observes them. A buffered channel preserves each
    // user-facing message in order for snackbar display.
    private val operationMessages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = operationMessages.receiveAsFlow()

    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError.asStateFlow()

    // Monotonic completion signal lets forms clear pending state even when a
    // fast operation's busy=true/busy=false updates are coalesced by the UI.
    private val _operationRevision = MutableStateFlow(0L)
    val operationRevision: StateFlow<Long> = _operationRevision.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            beginOperation()
            try {
                operationMutex.withLock {
                    try {
                        _snapshot.value = repository.loadSnapshot()
                        _loadError.value = false
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        // Do not leave the app in its initial loading state forever. Keep
                        // the last good snapshot (if any) and expose an explicit retry path.
                        _snapshot.value = _snapshot.value.copy(isLoading = false)
                        _loadError.value = true
                        publishMessage("Your local records could not be loaded.")
                    }
                }
            } finally {
                endOperation()
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

    fun deleteSmokeEvent(id: String) = perform(
        "Cigarette log removed. Daily totals, smoking trends, and tracked stock have been updated.",
    ) {
        repository.deleteSmokeEvent(id)
    }

    fun logSmoke(event: SmokeEvent, confirmUnstockedSmoking: Boolean = false) =
        perform(successMessage = null) {
            repository.logSmoke(event, confirmUnstockedSmoking)
        }

    fun logCraving(event: CravingEvent, confirmUnstockedSmoking: Boolean = false) = perform(
        if (event.outcome == CravingOutcome.RESISTED) "Craving recorded as resisted" else "Craving entry saved",
    ) { repository.logCraving(event, confirmUnstockedSmoking) }

    private suspend fun publishMessage(message: String) {
        operationMessages.send(message)
    }

    private fun perform(successMessage: String?, operation: suspend () -> Unit) {
        viewModelScope.launch {
            beginOperation()
            try {
                operationMutex.withLock {
                    try {
                        operation()
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        publishMessage(
                            error.message?.takeIf { it.length <= 140 }
                                ?: "That action could not be saved. Please try again.",
                        )
                        return@withLock
                    }

                    // The write is already committed at this point. A later read failure
                    // must not be reported as a failed write, or users may retry and
                    // create duplicate purchases or activity records.
                    if (successMessage != null) publishMessage(successMessage)
                    try {
                        _snapshot.value = repository.loadSnapshot()
                        _loadError.value = false
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        _snapshot.value = _snapshot.value.copy(isLoading = false)
                        _loadError.value = true
                        publishMessage(
                            successMessage?.let {
                                "$it. Saved, but the latest data could not be reloaded."
                            } ?: "Saved, but the latest data could not be reloaded.",
                        )
                    }
                }
            } finally {
                endOperation()
            }
        }
    }

    // viewModelScope launches on Main, so this count represents both the current
    // database operation and queued operations waiting for the serialization mutex.
    // Keeping busy true until the queue drains prevents brief false-ready windows.
    private var activeOperationCount = 0

    private fun beginOperation() {
        activeOperationCount += 1
        _busy.value = true
    }

    private fun endOperation() {
        activeOperationCount = (activeOperationCount - 1).coerceAtLeast(0)
        _busy.value = activeOperationCount > 0
        _operationRevision.value += 1L
    }

    override fun onCleared() {
        // ViewModel cancellation can arrive while a blocking SQLite call is still
        // unwinding on Dispatchers.IO. Close the helper only after that operation
        // releases the same mutex, and never block the main thread during teardown.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            operationMutex.withLock { runCatching { repository.close() } }
        }
    }
}
