package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.CravingOutcome
import com.amirmalik.breatheasy.data.CravingTrigger
import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.data.SmokeEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class BusinessRulesTest {
    private val utc = ZoneOffset.UTC
    private val now = Instant.parse("2026-11-18T08:00:00Z")

    private fun purchase(
        date: String = "2026-11-18T12:00:00Z",
        quantity: Int = 1,
        amountMinor: Long = 100L,
        currency: CurrencyCode = CurrencyCode.fromCode("USD"),
    ) = Purchase(
        purchasedAtMillis = Instant.parse(date).toEpochMilli(),
        type = PurchaseType.PACK,
        quantity = quantity,
        amountMinor = amountMinor,
        currency = currency,
    )

    private fun assertRejected(block: () -> Unit) {
        var rejected = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue("Expected the business rule to reject this value", rejected)
    }

    @Test
    fun purchaseOnCurrentLocalDateIsAllowedEvenWhenStoredAtMidday() {
        // The picker stores dates at local midday. Comparing raw timestamps would
        // incorrectly reject today's purchase when the user records it in the morning.
        BusinessRules.validatePurchase(purchase(), now = now, zoneId = utc)
    }

    @Test
    fun futurePurchaseDateIsRejected() {
        assertRejected {
            BusinessRules.validatePurchase(
                purchase(date = "2026-11-19T00:00:00Z"),
                now = now,
                zoneId = utc,
            )
        }
    }

    @Test
    fun currentDateIsEvaluatedInTheSelectedTimeZone() {
        // 00:30 UTC is still the previous calendar day in Los Angeles.
        BusinessRules.validatePurchase(
            purchase(date = "2026-11-18T07:30:00Z"),
            now = Instant.parse("2026-11-18T07:00:00Z"),
            zoneId = java.time.ZoneId.of("America/Los_Angeles"),
        )
    }

    @Test
    fun purchaseQuantityAndAmountMustBeValid() {
        assertRejected { BusinessRules.validatePurchase(purchase(quantity = 0), now, utc) }
        assertRejected { BusinessRules.validatePurchase(purchase(quantity = 10_001), now, utc) }
        assertRejected { BusinessRules.validatePurchase(purchase(amountMinor = 0), now, utc) }
    }

    @Test
    fun purchaseCurrencyMustBeARealIsoCurrencyWithCorrectPrecision() {
        assertRejected {
            BusinessRules.validatePurchase(
                purchase(currency = CurrencyCode("USD", "US Dollar", "$", 0)),
                now,
                utc,
            )
        }
        assertRejected {
            BusinessRules.validatePurchase(
                purchase(currency = CurrencyCode("ZZZ", "Unknown", "ZZZ", 2)),
                now,
                utc,
            )
        }
    }

    @Test
    fun preferencesRespectUserDefinedBoundsWithoutChangingCurrency() {
        val currency = CurrencyCode.fromCode("RWF")
        BusinessRules.validatePreferences(AppPreferences(currency, dailyTarget = 4, baselinePerDay = 15, startingStockCigarettes = 20))
        assertRejected { BusinessRules.validatePreferences(AppPreferences(currency, dailyTarget = 0)) }
        assertRejected { BusinessRules.validatePreferences(AppPreferences(currency, baselinePerDay = 201)) }
        assertRejected { BusinessRules.validatePreferences(AppPreferences(currency, startingStockCigarettes = 100_001)) }
    }

    @Test
    fun smokeQuantityMustStayWithinSupportedBounds() {
        BusinessRules.validateSmokeEvent(SmokeEvent(quantity = 100))
        assertRejected { BusinessRules.validateSmokeEvent(SmokeEvent(quantity = 0)) }
        assertRejected { BusinessRules.validateSmokeEvent(SmokeEvent(quantity = 101)) }
    }

    @Test
    fun cravingIntensityAndBreathingDurationMustBeValid() {
        BusinessRules.validateCravingEvent(
            CravingEvent(outcome = CravingOutcome.RESISTED, trigger = CravingTrigger.STRESS, intensity = 5),
        )
        assertRejected {
            BusinessRules.validateCravingEvent(
                CravingEvent(outcome = CravingOutcome.RESISTED, trigger = CravingTrigger.STRESS, intensity = 0),
            )
        }
        assertRejected {
            BusinessRules.validateCravingEvent(
                CravingEvent(
                    outcome = CravingOutcome.RESISTED,
                    trigger = CravingTrigger.STRESS,
                    intensity = 3,
                    breathingSeconds = -1,
                ),
            )
        }
    }

    @Test
    fun currencyParserDoesNotTreatUnknownCodesAsTheCurrentDeviceCurrency() {
        assertEquals(null, CurrencyCode.fromCodeOrNull("ZZZ"))
        assertEquals("USD", CurrencyCode.fromCodeOrNull("usd")?.isoCode)
    }
}
