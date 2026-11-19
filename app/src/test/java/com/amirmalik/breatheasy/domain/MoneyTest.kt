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
    fun usesASafeFallbackWhenTheDeviceRegionHasNoIsoCurrency() {
        assertEquals("USD", CurrencyCode.fromLocale(java.util.Locale("", "AQ")).isoCode)
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
    fun formatsCurrencyAndAmountWithAnOrdinarySpace() {
        val originalLocale = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.US)
            val rwf = CurrencyCode.fromCode("RWF")
            val formattedZero = Money.format(0L, rwf)
            val formattedAmount = Money.format(2_400L, rwf)

            assertTrue(formattedZero.contains("RWF 0"))
            assertTrue(formattedAmount.contains("RWF 2,400"))
            assertTrue(!formattedZero.contains('\u00A0'))
            assertTrue(!formattedZero.contains('\u202F'))
            assertTrue(Money.formatMajor(java.math.BigDecimal.ZERO, rwf).contains("RWF 0"))
        } finally {
            java.util.Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun amountPlaceholderReflectsCurrencyPrecisionAndLocaleSeparator() {
        val rwf = CurrencyCode.fromCode("RWF")
        val usd = CurrencyCode.fromCode("USD")
        val bhd = CurrencyCode.fromCode("BHD")

        assertEquals("0", Money.inputPlaceholder(rwf, java.util.Locale.US))
        assertEquals("0.00", Money.inputPlaceholder(usd, java.util.Locale.US))
        assertEquals("0.000", Money.inputPlaceholder(bhd, java.util.Locale.US))
        assertEquals("0,000", Money.inputPlaceholder(bhd, java.util.Locale.GERMANY))
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
            // A grouping dot must not be reinterpreted as a decimal fraction.
            assertNull(Money.parseToMinor("1.000", CurrencyCode.fromCode("USD")))
            assertNull(Money.parseToMinor("12.50", CurrencyCode.fromCode("USD")))
            java.util.Locale.setDefault(java.util.Locale.US)
            assertNull(Money.parseToMinor("1,000", CurrencyCode.fromCode("USD")))
        } finally {
            java.util.Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun parsesUsingTheComposeLocaleWhenItDiffersFromTheProcessDefault() {
        val originalLocale = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.US)
            val usd = CurrencyCode.fromCode("USD")

            // The purchase field uses the locale from Compose's Configuration.
            // That locale may differ from the process default (for example, with
            // app-specific language settings), so parsing must use the same locale
            // that supplied the visible decimal separator.
            assertNull(Money.parseToMinor("12,50", usd))
            assertEquals(1_250L, Money.parseToMinor("12,50", usd, java.util.Locale.GERMANY))
            assertNull(Money.parseToMinor("12.50", usd, java.util.Locale.GERMANY))
            assertEquals(1_250L, Money.parseToMinor("12.50", usd, java.util.Locale.US))
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
