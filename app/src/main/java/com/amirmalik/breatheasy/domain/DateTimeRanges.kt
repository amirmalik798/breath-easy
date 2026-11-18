package com.amirmalik.breatheasy.domain

import java.time.LocalDate
import java.time.ZoneId

/** Local-calendar windows that remain correct on daylight-saving transition days. */
object DateTimeRanges {
    fun dayRangeMillis(date: LocalDate, zone: ZoneId): LongRange {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endExclusive = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start until endExclusive
    }
}
