package com.amirmalik.breatheasy.data

import android.content.Context
import com.amirmalik.breatheasy.domain.BusinessRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The only data-access entry point used by the UI and ViewModel. */
class BreatheEasyRepository(context: Context) {
    private val database = BreatheEasyDatabase(context.applicationContext)

    suspend fun loadSnapshot(): AppSnapshot = withContext(Dispatchers.IO) {
        database.readSnapshot()
    }

    suspend fun savePreferences(preferences: AppPreferences) = withContext(Dispatchers.IO) {
        BusinessRules.validatePreferences(preferences)
        database.savePreferences(preferences)
    }

    suspend fun recordPurchase(purchase: Purchase) = withContext(Dispatchers.IO) {
        BusinessRules.validatePurchase(purchase)
        database.insertPurchase(
            purchase.copy(
                brand = purchase.brand.trim().take(BusinessRules.MAX_BRAND_LENGTH),
                location = purchase.location.trim().take(BusinessRules.MAX_LOCATION_LENGTH),
                note = purchase.note.trim().take(BusinessRules.MAX_NOTE_LENGTH),
            ),
        )
    }

    suspend fun deletePurchase(id: String) = withContext(Dispatchers.IO) {
        require(id.isNotBlank()) { "Purchase id is required" }
        check(database.deletePurchase(id)) { "This purchase was already removed." }
    }

    suspend fun logSmoke(event: SmokeEvent, confirmUnstockedSmoking: Boolean = false) = withContext(Dispatchers.IO) {
        val normalized = event.copy(note = event.note.trim().take(BusinessRules.MAX_NOTE_LENGTH))
        BusinessRules.validateSmokeEvent(normalized)
        database.insertSmokeEvent(normalized, confirmUnstockedSmoking)
    }

    suspend fun logCraving(event: CravingEvent, confirmUnstockedSmoking: Boolean = false) = withContext(Dispatchers.IO) {
        val normalized = event.copy(note = event.note.trim().take(BusinessRules.MAX_NOTE_LENGTH))
        BusinessRules.validateCravingEvent(normalized)
        database.insertCravingEvent(normalized, confirmUnstockedSmoking)
    }

    fun close() = database.close()
}
