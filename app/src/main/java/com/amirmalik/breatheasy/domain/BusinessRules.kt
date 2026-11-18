package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent
import com.amirmalik.breatheasy.data.CurrencyCode
import java.time.Instant
import java.time.ZoneId

/**
 * Shared application invariants. These rules live below the UI so actions remain
 * valid when new screens, background tasks, or other repository callers are added.
 */
object BusinessRules {
    const val MAX_PURCHASE_QUANTITY = 10_000
    const val MAX_SMOKE_EVENT_QUANTITY = 100
    const val MAX_NOTE_LENGTH = 500
    const val MAX_BRAND_LENGTH = 60
    const val MAX_LOCATION_LENGTH = 80

    fun validatePreferences(preferences: AppPreferences) {
        require(preferences.dailyTarget in 1..200) {
            "Daily target must be between 1 and 200."
        }
        require(preferences.baselinePerDay in 0..200) {
            "Baseline must be between 0 and 200."
        }
        require(preferences.startingStockCigarettes in 0..100_000) {
            "Starting stock must be between 0 and 100,000."
        }
        validateCurrency(preferences.displayCurrency)
    }

    fun validatePurchase(
        purchase: Purchase,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        require(purchase.id.isNotBlank()) { "Purchase id is required." }
        require(purchase.quantity in 1..MAX_PURCHASE_QUANTITY) {
            "Quantity must be between 1 and $MAX_PURCHASE_QUANTITY."
        }
        require(purchase.amountMinor > 0L) { "Purchase amount must be greater than zero." }
        validateCurrency(purchase.currency)

        val purchaseDate = Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(zoneId).toLocalDate()
        val today = now.atZone(zoneId).toLocalDate()
        require(!purchaseDate.isAfter(today)) { "Purchase date cannot be in the future." }

        // Evaluate the derived count while the quantity is known to be within the
        // supported business range. This also guards future changes to pack sizes.
        purchase.totalCigarettes
    }

    fun validateSmokeEvent(event: SmokeEvent, now: Instant = Instant.now()) {
        require(event.id.isNotBlank()) { "Smoking log id is required." }
        require(event.smokedAtMillis <= now.toEpochMilli()) {
            "Smoking log time cannot be in the future."
        }
        require(event.quantity in 1..MAX_SMOKE_EVENT_QUANTITY) {
            "Smoke quantity must be between 1 and $MAX_SMOKE_EVENT_QUANTITY."
        }
        require(event.note.length <= MAX_NOTE_LENGTH) {
            "Smoking notes cannot exceed $MAX_NOTE_LENGTH characters."
        }
    }

    fun validateCravingEvent(event: CravingEvent, now: Instant = Instant.now()) {
        require(event.id.isNotBlank()) { "Craving log id is required." }
        require(event.occurredAtMillis <= now.toEpochMilli()) {
            "Craving log time cannot be in the future."
        }
        require(event.intensity in 1..5) { "Craving intensity must be between 1 and 5." }
        require(event.breathingSeconds >= 0) { "Breathing duration cannot be negative." }
        require(event.note.length <= MAX_NOTE_LENGTH) {
            "Craving notes cannot exceed $MAX_NOTE_LENGTH characters."
        }
    }

    private fun validateCurrency(currency: CurrencyCode) {
        val canonical = CurrencyCode.fromCodeOrNull(currency.isoCode)
            ?: throw IllegalArgumentException("Unsupported ISO currency code: ${currency.isoCode}.")
        require(currency.isoCode == canonical.isoCode) {
            "Currency code must use its uppercase ISO-4217 form."
        }
        require(currency.fractionDigits == canonical.fractionDigits) {
            "Currency precision does not match ISO-4217 metadata for ${currency.isoCode}."
        }
    }
}
