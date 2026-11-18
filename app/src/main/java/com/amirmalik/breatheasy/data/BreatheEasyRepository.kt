package com.amirmalik.breatheasy.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The only data-access entry point used by the UI and ViewModel. */
class BreatheEasyRepository(context: Context) {
    private val database = BreatheEasyDatabase(context.applicationContext)

    suspend fun loadSnapshot(): AppSnapshot = withContext(Dispatchers.IO) {
        database.readSnapshot()
    }

    suspend fun savePreferences(preferences: AppPreferences) = withContext(Dispatchers.IO) {
        database.savePreferences(preferences)
    }

    suspend fun recordPurchase(purchase: Purchase) = withContext(Dispatchers.IO) {
        require(purchase.quantity in 1..10_000) { "Quantity must be between 1 and 10,000" }
        require(purchase.amountMinor > 0) { "Purchase amount must be greater than zero" }
        database.insertPurchase(purchase)
    }

    suspend fun deletePurchase(id: String) = withContext(Dispatchers.IO) {
        require(id.isNotBlank()) { "Purchase id is required" }
        check(database.deletePurchase(id)) { "This purchase was already removed." }
    }

    suspend fun logSmoke(event: SmokeEvent) = withContext(Dispatchers.IO) {
        require(event.quantity in 1..100) { "Smoke quantity must be between 1 and 100" }
        database.insertSmokeEvent(event)
    }

    suspend fun logCraving(event: CravingEvent) = withContext(Dispatchers.IO) {
        require(event.intensity in 1..5) { "Craving intensity must be between 1 and 5" }
        database.insertCravingEvent(event)
    }

    fun close() = database.close()
}
