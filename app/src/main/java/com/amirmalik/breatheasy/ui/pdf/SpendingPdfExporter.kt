package com.amirmalik.breatheasy.ui.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.domain.Money
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.max

enum class SpendingReportPeriod(
    val monthCount: Int,
    val label: String,
    val fileLabel: String,
) {
    CURRENT_MONTH(1, "Selected month", "selected-month"),
    LAST_3_MONTHS(3, "Last 3 months", "last-3-months"),
    LAST_6_MONTHS(6, "Last 6 months", "last-6-months"),
    LAST_12_MONTHS(12, "Last 12 months", "last-12-months");

    fun monthsEndingAt(currentMonth: YearMonth): List<YearMonth> =
        (monthCount - 1 downTo 0).map { offset -> currentMonth.minusMonths(offset.toLong()) }
}

/**
 * Produces a print-ready report with a period overview followed by one monthly
 * summary per month. Long transaction lists continue on clearly labelled pages.
 *
 * All values are restricted to the selected currency. No currency conversion is
 * attempted, so figures from different currencies are never combined.
 */
object SpendingPdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val FIRST_PAGE_ROWS = 8
    private const val CONTINUATION_ROWS = 15
    private const val ROW_HEIGHT = 35f

    private val ink = Color.rgb(27, 44, 37)
    private val forest = Color.rgb(19, 43, 34)
    private val forestLight = Color.rgb(37, 68, 54)
    private val mint = Color.rgb(179, 224, 195)
    private val pageColor = Color.rgb(247, 249, 246)
    private val paper = Color.WHITE
    private val muted = Color.rgb(107, 121, 112)
    private val border = Color.rgb(222, 230, 223)
    private val track = Color.rgb(232, 238, 232)
    private val accent = Color.rgb(102, 159, 125)
    private val softAccent = Color.rgb(225, 239, 228)

    private data class MonthReport(
        val month: YearMonth,
        val purchases: List<Purchase>,
    ) {
        val totalMinor: Long
            get() = SpendingPdfExporter.sumAmounts(purchases)
    }

    private data class MonthPage(
        val report: MonthReport,
        val rows: List<Purchase>,
        val continuation: Boolean,
    )

    fun write(
        context: Context,
        destination: Uri,
        purchases: List<Purchase>,
        currency: CurrencyCode,
        period: SpendingReportPeriod,
        currentMonth: YearMonth = YearMonth.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        val reports = buildMonthReports(purchases, currency, period, currentMonth, zoneId)
        val detailPages = buildMonthPages(reports)
        val totalPages = 1 + detailPages.size
        val document = PdfDocument()

        try {
            val output = context.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("The selected location could not be opened for writing.")

            output.use { stream ->
                val generatedAtMillis = System.currentTimeMillis()
                val overviewPage = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create(),
                )
                try {
                    drawOverviewPage(
                        canvas = overviewPage.canvas,
                        reports = reports,
                        currency = currency,
                        period = period,
                        generatedAtMillis = generatedAtMillis,
                        zoneId = zoneId,
                        totalPages = totalPages,
                    )
                } finally {
                    document.finishPage(overviewPage)
                }

                detailPages.forEachIndexed { index, monthPage ->
                    val pageNumber = index + 2
                    val pdfPage = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
                    )
                    try {
                        drawMonthPage(
                            canvas = pdfPage.canvas,
                            monthPage = monthPage,
                            currency = currency,
                            pageNumber = pageNumber,
                            totalPages = totalPages,
                            generatedAtMillis = generatedAtMillis,
                            zoneId = zoneId,
                        )
                    } finally {
                        document.finishPage(pdfPage)
                    }
                }

                document.writeTo(stream)
                stream.flush()
            }
        } finally {
            document.close()
        }
    }

    private fun buildMonthReports(
        purchases: List<Purchase>,
        currency: CurrencyCode,
        period: SpendingReportPeriod,
        currentMonth: YearMonth,
        zoneId: ZoneId,
    ): List<MonthReport> =
        period.monthsEndingAt(currentMonth).map { month ->
            val matching = purchases.asSequence()
                .filter { it.currency.isoCode == currency.isoCode }
                .filter {
                    YearMonth.from(Instant.ofEpochMilli(it.purchasedAtMillis).atZone(zoneId)) == month
                }
                .sortedBy { it.purchasedAtMillis }
                .toList()
            MonthReport(month, matching)
        }

    private fun buildMonthPages(reports: List<MonthReport>): List<MonthPage> =
        reports.flatMap { report ->
            if (report.purchases.isEmpty()) {
                listOf(MonthPage(report, emptyList(), continuation = false))
            } else {
                buildList {
                    add(MonthPage(report, report.purchases.take(FIRST_PAGE_ROWS), continuation = false))
                    report.purchases.drop(FIRST_PAGE_ROWS)
                        .chunked(CONTINUATION_ROWS)
                        .forEach { rows ->
                            add(MonthPage(report, rows, continuation = true))
                        }
                }
            }
        }

    private fun drawOverviewPage(
        canvas: Canvas,
        reports: List<MonthReport>,
        currency: CurrencyCode,
        period: SpendingReportPeriod,
        generatedAtMillis: Long,
        zoneId: ZoneId,
        totalPages: Int,
    ) {
        canvas.drawColor(pageColor)
        val allPurchases = reports.flatMap { it.purchases }
        val total = sumAmounts(allPurchases)
        val purchaseCount = allPurchases.size
        val averagePerMonth = formatAverage(total, reports.size, currency)
        val firstMonth = reports.first().month
        val lastMonth = reports.last().month
        val dateRange = formatMonthStart(firstMonth) + " — " + formatMonthEnd(lastMonth)

        drawOverviewHero(canvas, period, dateRange, currency)
        drawMetricCard(
            canvas, RectF(38f, 213f, 207f, 296f),
            "TOTAL SPENT", Money.format(total, currency), currency.isoCode,
            accentCard = true,
        )
        drawMetricCard(
            canvas, RectF(213f, 213f, 382f, 296f),
            "PURCHASES", purchaseCount.toString(), "recorded in this period",
        )
        drawMetricCard(
            canvas, RectF(388f, 213f, 557f, 296f),
            "AVERAGE PER MONTH", averagePerMonth, currency.isoCode,
        )

        val trendLastRowBottom = 335f + 27f + reports.lastIndex * 22.5f + 8f
        val categoryTop = (trendLastRowBottom + 43f).coerceIn(438f, 646f)
        drawMonthlyTrend(canvas, reports, currency, top = 335f)
        drawPeriodCategoryBreakdown(canvas, allPurchases, currency, top = categoryTop)
        drawFooter(
            canvas = canvas,
            pageNumber = 1,
            totalPages = totalPages,
            generatedAtMillis = generatedAtMillis,
            currency = currency,
            zoneId = zoneId,
        )
    }

    private fun drawOverviewHero(
        canvas: Canvas,
        period: SpendingReportPeriod,
        dateRange: String,
        currency: CurrencyCode,
    ) {
        canvas.drawRoundRect(RectF(28f, 26f, 567f, 190f), 24f, 24f, fillPaint(forest))
        val ring = strokePaint(Color.argb(35, 179, 224, 195), 1.3f)
        canvas.drawCircle(526f, 64f, 76f, ring)
        canvas.drawCircle(526f, 64f, 54f, ring)

        drawText(canvas, "B R E A T H E  E A S Y", 52f, 58f, 10f, mint, bold = true)
        drawText(canvas, "PERSONAL FINANCE", 542f, 58f, 7.5f, Color.rgb(204, 219, 207), align = Paint.Align.RIGHT)
        drawText(canvas, "SPENDING REPORT", 52f, 94f, 23f, paper, bold = true)
        drawText(canvas, "PERIOD OVERVIEW  ·  ${period.label.uppercase(Locale.getDefault())}",
            52f, 118f, 8f, Color.rgb(203, 218, 207), bold = true)
        drawText(canvas, dateRange, 52f, 145f, 11f, paper, bold = true)
        canvas.drawLine(52f, 160f, 543f, 160f, strokePaint(Color.argb(54, 220, 239, 225), 1f))
        drawFittedText(
            canvas,
            "Includes ${currency.isoCode} purchases only · Other currencies are excluded (not converted)",
            52f, 179f, 490f, textPaint(7f, Color.rgb(203, 218, 207)),
        )
    }

    private fun drawMetricCard(
        canvas: Canvas,
        rect: RectF,
        label: String,
        value: String,
        supportingText: String,
        accentCard: Boolean = false,
    ) {
        val background = if (accentCard) softAccent else paper
        val valueColor = if (accentCard) forest else ink
        canvas.drawRoundRect(rect, 14f, 14f, fillPaint(background))
        canvas.drawRoundRect(rect, 14f, 14f, strokePaint(border, 0.8f))
        drawText(canvas, label, rect.left + 13f, rect.top + 22f, 7f, muted, bold = true)
        drawFittedText(
            canvas, value, rect.left + 13f, rect.top + 52f, rect.width() - 26f,
            textPaint(17f, valueColor, bold = true),
        )
        drawFittedText(
            canvas, supportingText, rect.left + 13f, rect.top + 70f, rect.width() - 26f,
            textPaint(7f, muted),
        )
    }

    private fun drawMonthlyTrend(
        canvas: Canvas,
        reports: List<MonthReport>,
        currency: CurrencyCode,
        top: Float,
    ) {
        drawText(canvas, "MONTH-BY-MONTH SPENDING", 38f, top, 9f, ink, bold = true)
        drawText(canvas, "Amounts in ${currency.isoCode}", 557f, top, 7.5f, muted, align = Paint.Align.RIGHT)

        val maxTotal = max(reports.maxOfOrNull { it.totalMinor }?.toFloat() ?: 0f, 1f)
        reports.forEachIndexed { index, report ->
            val y = top + 27f + index * 22.5f
            val monthLabel = report.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .uppercase(Locale.getDefault()).take(3) + " " + report.month.year.toString().takeLast(2)
            drawText(canvas, monthLabel, 39f, y + 8f, 7f, ink, bold = true)
            val barTrack = RectF(112f, y, 416f, y + 8f)
            canvas.drawRoundRect(barTrack, 5f, 5f, fillPaint(track))
            val barWidth = 304f * (report.totalMinor.toFloat() / maxTotal).coerceIn(0f, 1f)
            if (barWidth > 0f) {
                canvas.drawRoundRect(
                    RectF(112f, y, 112f + barWidth, y + 8f),
                    5f, 5f, fillPaint(accent),
                )
            }
            drawFittedText(
                canvas, Money.format(report.totalMinor, currency), 548f, y + 8f, 125f,
                textPaint(7.5f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
        if (reports.all { it.purchases.isEmpty() }) {
            drawText(canvas, "No purchases were recorded in this period and currency.", 112f, top + 49f, 8f, muted)
        }
    }

    private fun drawPeriodCategoryBreakdown(
        canvas: Canvas,
        purchases: List<Purchase>,
        currency: CurrencyCode,
        top: Float,
    ) {
        drawText(canvas, "SPENDING BY PURCHASE TYPE", 38f, top, 9f, ink, bold = true)
        val totals = categoryTotals(purchases)
        if (totals.isEmpty()) {
            drawText(canvas, "No category totals to display.", 38f, top + 28f, 8f, muted)
            return
        }

        val maxTotal = max(totals.maxOf { it.second }.toFloat(), 1f)
        totals.forEachIndexed { index, (type, amount) ->
            val y = top + 19f + index * 27f
            drawFittedText(canvas, type.label, 38f, y + 8f, 106f, textPaint(7.5f, ink, bold = true))
            canvas.drawRoundRect(RectF(151f, y, 424f, y + 7f), 4f, 4f, fillPaint(track))
            val width = 273f * (amount.toFloat() / maxTotal).coerceIn(0f, 1f)
            if (width > 0f) {
                canvas.drawRoundRect(RectF(151f, y, 151f + width, y + 7f), 4f, 4f, fillPaint(accent))
            }
            drawFittedText(
                canvas, Money.format(amount, currency), 557f, y + 8f, 118f,
                textPaint(7.5f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
    }

    private fun drawMonthPage(
        canvas: Canvas,
        monthPage: MonthPage,
        currency: CurrencyCode,
        pageNumber: Int,
        totalPages: Int,
        generatedAtMillis: Long,
        zoneId: ZoneId,
    ) {
        canvas.drawColor(pageColor)
        if (monthPage.continuation) {
            drawContinuationHeader(canvas, monthPage.report, currency)
            drawTransactionTable(canvas, monthPage.rows, currency, top = 183f, zoneId = zoneId)
        } else {
            drawMonthlyHero(canvas, monthPage.report, currency)
            drawMonthlyMetrics(canvas, monthPage.report, currency)
            drawMonthlyCategoryBreakdown(canvas, monthPage.report.purchases, currency, top = 310f)
            drawTransactionTable(canvas, monthPage.rows, currency, top = 461f, zoneId = zoneId)
            if (monthPage.report.purchases.isEmpty()) {
                drawEmptyMonth(canvas, currency)
            }
        }

        drawFooter(canvas, pageNumber, totalPages, generatedAtMillis, currency, zoneId)
    }

    private fun drawMonthlyHero(
        canvas: Canvas,
        report: MonthReport,
        currency: CurrencyCode,
    ) {
        canvas.drawRoundRect(RectF(28f, 26f, 567f, 178f), 22f, 22f, fillPaint(forest))
        drawText(canvas, "B R E A T H E  E A S Y  /  MONTHLY DETAIL", 52f, 58f, 9f, mint, bold = true)
        val monthName = report.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        drawFittedText(
            canvas, "$monthName ${report.month.year}", 52f, 98f, 345f,
            textPaint(24f, paper, bold = true),
        )
        drawText(
            canvas,
            "${report.purchases.size} ${if (report.purchases.size == 1) "purchase" else "purchases"} recorded",
            52f, 121f, 8.5f, Color.rgb(203, 218, 207),
        )
        val start = report.month.atDay(1).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        val end = report.month.atEndOfMonth().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        drawText(canvas, "$start  —  $end", 52f, 147f, 8f, Color.rgb(203, 218, 207))
        drawText(canvas, currency.isoCode, 542f, 147f, 8f, mint, bold = true, align = Paint.Align.RIGHT)
    }

    private fun drawMonthlyMetrics(
        canvas: Canvas,
        report: MonthReport,
        currency: CurrencyCode,
    ) {
        val avgPurchase = if (report.purchases.isEmpty()) "—"
            else formatAverage(report.totalMinor, report.purchases.size, currency)
        drawMetricCard(
            canvas, RectF(38f, 195f, 207f, 277f),
            "TOTAL SPENT", Money.format(report.totalMinor, currency), currency.isoCode,
            accentCard = true,
        )
        drawMetricCard(
            canvas, RectF(213f, 195f, 382f, 277f),
            "PURCHASES", report.purchases.size.toString(), "transactions",
        )
        drawMetricCard(
            canvas, RectF(388f, 195f, 557f, 277f),
            "AVERAGE PURCHASE", avgPurchase, currency.isoCode,
        )
    }

    private fun drawMonthlyCategoryBreakdown(
        canvas: Canvas,
        purchases: List<Purchase>,
        currency: CurrencyCode,
        top: Float,
    ) {
        drawText(canvas, "SPENDING BY PURCHASE TYPE", 38f, top, 9f, ink, bold = true)
        val totals = categoryTotals(purchases)
        if (totals.isEmpty()) {
            drawText(canvas, "No category spending recorded for this month.", 38f, top + 28f, 8f, muted)
            return
        }

        val maxTotal = max(totals.maxOf { it.second }.toFloat(), 1f)
        totals.forEachIndexed { index, (type, amount) ->
            val y = top + 19f + index * 29f
            drawFittedText(canvas, type.label, 38f, y + 9f, 108f, textPaint(8f, ink, bold = true))
            canvas.drawRoundRect(RectF(151f, y, 424f, y + 8f), 4f, 4f, fillPaint(track))
            val width = 273f * (amount.toFloat() / maxTotal).coerceIn(0f, 1f)
            if (width > 0f) {
                canvas.drawRoundRect(RectF(151f, y, 151f + width, y + 8f), 4f, 4f, fillPaint(accent))
            }
            drawFittedText(
                canvas, Money.format(amount, currency), 557f, y + 9f, 118f,
                textPaint(8f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        report: MonthReport,
        currency: CurrencyCode,
    ) {
        canvas.drawRoundRect(RectF(28f, 26f, 567f, 150f), 22f, 22f, fillPaint(forest))
        drawText(canvas, "B R E A T H E  E A S Y  /  SPENDING REPORT", 52f, 58f, 9f, mint, bold = true)
        val monthName = report.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        drawText(canvas, "$monthName ${report.month.year}", 52f, 98f, 22f, paper, bold = true)
        drawText(canvas, "PURCHASE DETAILS  ·  CONTINUED", 52f, 123f, 8f, Color.rgb(203, 218, 207), bold = true)
        drawText(
            canvas, Money.format(report.totalMinor, currency), 542f, 98f, 14f, mint,
            bold = true, align = Paint.Align.RIGHT,
        )
    }

    private fun drawEmptyMonth(canvas: Canvas, currency: CurrencyCode) {
        val empty = RectF(38f, 574f, 557f, 672f)
        canvas.drawRoundRect(empty, 16f, 16f, fillPaint(paper))
        canvas.drawRoundRect(empty, 16f, 16f, strokePaint(border, 1f))
        drawText(canvas, "No purchases recorded", 58f, 613f, 16f, ink, bold = true)
        drawText(canvas, "There are no transactions in ${currency.isoCode} for this month.", 58f, 636f, 8.5f, muted)
        drawText(canvas, "The monthly total is 0.", 58f, 654f, 8.5f, muted)
    }

    private fun drawTransactionTable(
        canvas: Canvas,
        rows: List<Purchase>,
        currency: CurrencyCode,
        top: Float,
        zoneId: ZoneId,
    ) {
        drawText(canvas, "PURCHASE LEDGER", 38f, top, 9f, ink, bold = true)
        drawText(canvas, "DATE", 42f, top + 25f, 7f, muted, bold = true)
        drawText(canvas, "ITEM & DETAILS", 123f, top + 25f, 7f, muted, bold = true)
        drawText(canvas, "QTY", 420f, top + 25f, 7f, muted, bold = true, align = Paint.Align.RIGHT)
        drawText(canvas, "AMOUNT · ${currency.isoCode}", 557f, top + 25f, 7f, muted, bold = true, align = Paint.Align.RIGHT)
        canvas.drawLine(38f, top + 34f, 557f, top + 34f, strokePaint(border, 1f))

        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()).withZone(zoneId)
        rows.forEachIndexed { index, purchase ->
            val rowTop = top + 39f + index * ROW_HEIGHT
            val rowRect = RectF(34f, rowTop, 561f, rowTop + ROW_HEIGHT - 2f)
            if (index % 2 == 0) canvas.drawRoundRect(rowRect, 7f, 7f, fillPaint(paper))

            drawText(
                canvas,
                dateFormatter.format(Instant.ofEpochMilli(purchase.purchasedAtMillis)),
                42f, rowTop + 20f, 8f, ink, bold = true,
            )
            drawFittedText(
                canvas,
                purchase.brand.ifBlank { purchase.type.label },
                123f, rowTop + 14f, 270f,
                textPaint(8.5f, ink, bold = true),
            )
            val secondary = buildString {
                append(purchase.type.label)
                append("  ·  ")
                append(if (purchase.planned) "Planned" else "Impulse")
                if (purchase.location.isNotBlank()) {
                    append("  ·  ")
                    append(purchase.location)
                }
                if (purchase.note.isNotBlank()) {
                    append("  ·  ")
                    append(purchase.note)
                }
            }
            drawFittedText(canvas, secondary, 123f, rowTop + 27f, 270f, textPaint(7f, muted))
            drawText(canvas, purchase.quantity.toString(), 420f, rowTop + 21f, 8.5f, ink, align = Paint.Align.RIGHT)
            drawFittedText(
                canvas,
                Money.format(purchase.amountMinor, currency),
                557f, rowTop + 21f, 112f,
                textPaint(8.5f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
        if (rows.isEmpty()) {
            drawText(canvas, "No transaction details for this month.", 42f, top + 62f, 9f, muted)
        }
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        totalPages: Int,
        generatedAtMillis: Long,
        currency: CurrencyCode,
        zoneId: ZoneId,
    ) {
        canvas.drawLine(38f, 806f, 557f, 806f, strokePaint(border, 1f))
        val generated = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
            .withZone(zoneId)
            .format(Instant.ofEpochMilli(generatedAtMillis))
        drawText(canvas, "BREATHE EASY  ·  PRIVATE SPENDING SUMMARY", 38f, 824f, 6.5f, muted, bold = true)
        drawText(canvas, "Generated $generated  ·  ${currency.isoCode}", 300f, 824f, 6.5f, muted, align = Paint.Align.CENTER)
        drawText(canvas, "$pageNumber / $totalPages", 557f, 824f, 7f, ink, bold = true, align = Paint.Align.RIGHT)
    }

    private fun categoryTotals(purchases: List<Purchase>): List<Pair<PurchaseType, Long>> =
        PurchaseType.entries.mapNotNull { type ->
            val total = sumAmounts(purchases.filter { it.type == type })
            if (total > 0L) type to total else null
        }

    private fun formatAverage(totalMinor: Long, divisor: Int, currency: CurrencyCode): String {
        if (divisor <= 0) return Money.format(0L, currency)
        val amount = BigDecimal.valueOf(totalMinor, currency.fractionDigits)
            .divide(BigDecimal.valueOf(divisor.toLong()), currency.fractionDigits, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            this.currency = Currency.getInstance(currency.isoCode)
            minimumFractionDigits = currency.fractionDigits
            maximumFractionDigits = currency.fractionDigits
        }
        return formatter.format(amount)
    }

    private fun formatMonthStart(month: YearMonth): String =
        month.atDay(1).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

    private fun formatMonthEnd(month: YearMonth): String =
        month.atEndOfMonth().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

    private fun sumAmounts(purchases: List<Purchase>): Long =
        purchases.fold(0L) { total, purchase -> Math.addExact(total, purchase.amountMinor) }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun strokePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
    }

    private fun textPaint(
        size: Float,
        color: Int,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT,
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        textAlign = align
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT,
        maxWidth: Float = Float.MAX_VALUE,
    ) {
        drawFittedText(canvas, text, x, y, maxWidth, textPaint(size, color, bold, align))
    }

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
    ) {
        var displayed = text
        if (maxWidth.isFinite()) {
            while (displayed.isNotEmpty() && paint.measureText(displayed) > maxWidth) {
                displayed = displayed.dropLast(1)
            }
            if (displayed != text && displayed.isNotEmpty()) {
                while (displayed.isNotEmpty() && paint.measureText(displayed + "…") > maxWidth) {
                    displayed = displayed.dropLast(1)
                }
                displayed += "…"
            }
        }
        canvas.drawText(displayed, x, y, paint)
    }
}
