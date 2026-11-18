package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun resolvesCurrenciesFromCountryAndIsoCodeWithoutAnAppSpecificAllowlist() {
        assertEquals("RWF", CurrencyCode.fromCountry("RW")?.isoCode)
        assertEquals("USD", CurrencyCode.fromCountry("US")?.isoCode)
        assertEquals("JPY", CurrencyCode.fromCountry("JP")?.isoCode)
        assertEquals("BHD", CurrencyCode.fromCode("BHD").isoCode)
        assertEquals("EUR", CurrencyCode.fromCode("EUR").isoCode)
    }

    @Test
    fun formatsUsingPlatformCurrencyMetadata() {
        val rwf = CurrencyCode.fromCode("RWF")
        val jpy = CurrencyCode.fromCode("JPY")
        assertEquals(0, rwf.fractionDigits)
        assertEquals(0, jpy.fractionDigits)
        val formatted = Money.format(200L, rwf)
        assertTrue(formatted.contains("200"))
        assertTrue(formatted.contains(rwf.symbol))
        assertTrue(Money.format(200L, rwf, includeCode = true).endsWith("(RWF)"))
    }

    @Test
    fun parsesAmountsUsingTheSelectedCurrenciesMinorUnitPrecision() {
        val usd = CurrencyCode.fromCode("USD")
        val bhd = CurrencyCode.fromCode("BHD")
        val rwf = CurrencyCode.fromCode("RWF")
        assertEquals(1_405L, Money.parseToMinor("14.05", usd))
        assertEquals(12_345L, Money.parseToMinor("12.345", bhd))
        assertEquals(25_000L, Money.parseToMinor("25000", rwf))
        assertNull(Money.parseToMinor("25.50", rwf))
    }

    @Test
    fun parsesTheDeviceLocaleDecimalSeparatorWithoutGuessingGrouping() {
        val originalLocale = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.US)
            assertNull(Money.parseToMinor("1,000.00", CurrencyCode.fromCode("USD")))
            assertEquals(100_000L, Money.parseToMinor("1000.00", CurrencyCode.fromCode("USD")))
            java.util.Locale.setDefault(java.util.Locale.GERMANY)
            assertEquals(1_250L, Money.parseToMinor("12,50", CurrencyCode.fromCode("USD")))
            assertNull(Money.parseToMinor("1.000,00", CurrencyCode.fromCode("USD")))
        } finally {
            java.util.Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun rejectsZeroNegativeAndMalformedAmounts() {
        val usd = CurrencyCode.fromCode("USD")
        assertNull(Money.parseToMinor("0", usd))
        assertNull(Money.parseToMinor("-2.00", usd))
        assertNull(Money.parseToMinor("12..3", usd))
    }
}
