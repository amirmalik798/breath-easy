package com.amirmalik.breatheasy.domain

/**
 * Overflow-safe arithmetic for cumulative values derived from a long-lived local
 * history. Saturation prevents valid large datasets from wrapping or crashing
 * a screen with ArithmeticException.
 */
object SafeArithmetic {
    fun add(left: Long, right: Long): Long = when {
        right > 0L && left > Long.MAX_VALUE - right -> Long.MAX_VALUE
        right < 0L && left < Long.MIN_VALUE - right -> Long.MIN_VALUE
        else -> left + right
    }

    fun subtract(left: Long, right: Long): Long = when {
        right < 0L && left > Long.MAX_VALUE + right -> Long.MAX_VALUE
        right > 0L && left < Long.MIN_VALUE + right -> Long.MIN_VALUE
        else -> left - right
    }
}
