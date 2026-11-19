package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SafeArithmeticTest {
    @Test
    fun addPreservesOrdinaryValues() {
        assertEquals(42L, SafeArithmetic.add(19L, 23L))
        assertEquals(-4L, SafeArithmetic.add(-9L, 5L))
    }

    @Test
    fun addSaturatesAtBothBounds() {
        assertEquals(Long.MAX_VALUE, SafeArithmetic.add(Long.MAX_VALUE, 1L))
        assertEquals(Long.MIN_VALUE, SafeArithmetic.add(Long.MIN_VALUE, -1L))
    }

    @Test
    fun subtractPreservesOrdinaryValues() {
        assertEquals(7L, SafeArithmetic.subtract(12L, 5L))
        assertEquals(-14L, SafeArithmetic.subtract(-9L, 5L))
    }

    @Test
    fun subtractSaturatesAtBothBounds() {
        assertEquals(Long.MAX_VALUE, SafeArithmetic.subtract(Long.MAX_VALUE, -1L))
        assertEquals(Long.MIN_VALUE, SafeArithmetic.subtract(Long.MIN_VALUE, 1L))
    }

    @Test
    fun handlesExtremeOppositeSignedOperands() {
        assertEquals(-1L, SafeArithmetic.add(Long.MIN_VALUE, Long.MAX_VALUE))
        assertEquals(-1L, SafeArithmetic.subtract(Long.MIN_VALUE, Long.MIN_VALUE + 1L))
    }
}
