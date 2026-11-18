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
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

enum class SpendingReportPeriod(
    val monthCount: Int,
    val label: String,
    val fileLabel: String,
) {
    CURRENT_MONTH(1, "This month", "this-month"),
    LAST_3_MONTHS(3, "Last 3", "last-3-months"),
    LAST_6_MONTHS(6, "Last 6", "last-6-months"),
    LAST_12_MONTHS(12, "Last 12", "last-12-months");

    fun monthsEndingAt(currentMonth: YearMonth): List<YearMonth> =
        (monthCount - 1 downTo 0).map { offset -> currentMonth.minusMonths(offset.toLong()) }
}

/**
 * Builds a print-ready spending report. Each month starts on a fresh page; a month
 * with many transactions continues on extra pages before the next month begins.
 */
object SpendingPdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val FIRST_PAGE_ROWS = 8
    private const val CONTINUATION_ROWS = 15
    private const val ROW_HEIGHT = 36f

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

    private data class MonthPage(
        val month: YearMonth,
        val monthPurchases: List<Purchase>,
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
        val monthPages = buildMonthPages(purchases, currency, period, currentMonth, zoneId)
        // Own the PDF document before opening the destination. Both resources are
        // closed even when the provider rejects the URI or writing fails midway.
        val document = PdfDocument()
        try {
            val output = context.contentResolver.openOutputStream(destination, "w")
                ?: throw IOException("The selected location could not be opened for writing.")
            output.use { stream ->
                val generatedAtMillis = System.currentTimeMillis()
                monthPages.forEachIndexed { index, monthPage ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                    val pdfPage = document.startPage(pageInfo)
                    try {
                        drawPage(
                            canvas = pdfPage.canvas,
                            monthPage = monthPage,
                            currency = currency,
                            pageNumber = index + 1,
                            totalPages = monthPages.size,
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

    private fun buildMonthPages(
        purchases: List<Purchase>,
        currency: CurrencyCode,
        period: SpendingReportPeriod,
        currentMonth: YearMonth,
        zoneId: ZoneId,
    ): List<MonthPage> {
        return period.monthsEndingAt(currentMonth).flatMap { month ->
            val matching = purchases.asSequence()
                .filter { it.currency.isoCode == currency.isoCode }
                .filter {
                    YearMonth.from(Instant.ofEpochMilli(it.purchasedAtMillis).atZone(zoneId)) == month
                }
                .sortedBy { it.purchasedAtMillis }
                .toList()

            if (matching.isEmpty()) {
                listOf(MonthPage(month, matching, emptyList(), continuation = false))
            } else {
                buildList {
                    add(MonthPage(month, matching, matching.take(FIRST_PAGE_ROWS), continuation = false))
                    matching.drop(FIRST_PAGE_ROWS)
                        .chunked(CONTINUATION_ROWS)
                        .forEach { rows ->
                            add(MonthPage(month, matching, rows, continuation = true))
                        }
                }
            }
        }
    }

    private fun drawPage(
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
            drawContinuationHeader(canvas, monthPage.month, monthPage.monthPurchases, currency)
            drawTransactionTable(canvas, monthPage.rows, currency, top = 183f, zoneId = zoneId)
        } else {
            drawHeroHeader(canvas, monthPage.month, monthPage.monthPurchases, currency)
            drawCategoryBreakdown(canvas, monthPage.monthPurchases, currency)
            drawTransactionTable(canvas, monthPage.rows, currency, top = 454f, zoneId = zoneId)
            if (monthPage.monthPurchases.isEmpty()) {
                drawEmptyMonth(canvas)
            }
        }
        drawFooter(canvas, pageNumber, totalPages, generatedAtMillis, currency, zoneId)
    }

    private fun drawHeroHeader(
        canvas: Canvas,
        month: YearMonth,
        purchases: List<Purchase>,
        currency: CurrencyCode,
    ) {
        val header = RectF(28f, 26f, 567f, 244f)
        canvas.drawRoundRect(header, 24f, 24f, fillPaint(forest))

        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 179, 224, 195)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }
        canvas.drawCircle(520f, 73f, 73f, ring)
        canvas.drawCircle(520f, 73f, 52f, ring)

        drawText(canvas, "B R E A T H E  E A S Y", 54f, 62f, 10f, mint, bold = true)
        drawText(canvas, "SPENDING REPORT", 541f, 62f, 8f, Color.rgb(204, 219, 207), align = Paint.Align.RIGHT)
        drawText(canvas, "MONTHLY SUMMARY", 54f, 96f, 8f, Color.rgb(204, 219, 207), bold = true)
        val monthName = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        drawText(canvas, monthName + " " + month.year, 54f, 130f, 27f, paper, bold = true)

        val start = month.atDay(1).format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
        val end = month.atEndOfMonth().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
        drawText(canvas, start + "  —  " + end, 54f, 151f, 9f, Color.rgb(203, 218, 207))

        canvas.drawLine(54f, 166f, 541f, 166f, strokePaint(Color.argb(54, 220, 239, 225), 1f))
        drawText(canvas, "TOTAL SPENT", 54f, 187f, 8f, Color.rgb(203, 218, 207), bold = true)
        drawFittedText(
            canvas,
            Money.format(sumAmounts(purchases), currency),
            54f,
            221f,
            350f,
            textPaint(28f, mint, bold = true),
        )

        val countCard = RectF(424f, 182f, 541f, 226f)
        canvas.drawRoundRect(countCard, 12f, 12f, fillPaint(forestLight))
        drawText(canvas, "PURCHASES", 482.5f, 199f, 7f, Color.rgb(203, 218, 207), bold = true, align = Paint.Align.CENTER)
        drawText(canvas, purchases.size.toString(), 482.5f, 219f, 15f, paper, bold = true, align = Paint.Align.CENTER)
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        month: YearMonth,
        purchases: List<Purchase>,
        currency: CurrencyCode,
    ) {
        canvas.drawRoundRect(RectF(28f, 26f, 567f, 150f), 22f, 22f, fillPaint(forest))
        drawText(canvas, "B R E A T H E  E A S Y  /  SPENDING REPORT", 52f, 58f, 9f, mint, bold = true)
        val monthName = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        drawText(canvas, monthName + " " + month.year, 52f, 98f, 23f, paper, bold = true)
        drawText(canvas, "PURCHASE DETAILS  ·  CONTINUED", 52f, 123f, 8f, Color.rgb(203, 218, 207), bold = true)
        drawText(
            canvas,
            Money.format(sumAmounts(purchases), currency),
            542f,
            98f,
            14f,
            mint,
            bold = true,
            align = Paint.Align.RIGHT,
        )
    }

    private fun drawCategoryBreakdown(
        canvas: Canvas,
        purchases: List<Purchase>,
        currency: CurrencyCode,
    ) {
        drawText(canvas, "SPENDING BY PURCHASE TYPE", 38f, 278f, 9f, ink, bold = true)
        drawText(canvas, "Only " + currency.isoCode + " purchases", 557f, 278f, 8f, muted, align = Paint.Align.RIGHT)

        val totals = PurchaseType.entries.mapNotNull { type ->
            val total = sumAmounts(purchases.filter { it.type == type })
            if (total > 0L) type to total else null
        }
        if (totals.isEmpty()) {
            drawText(canvas, "No purchases recorded in this currency for this month.", 38f, 315f, 9f, muted)
            return
        }

        val maxTotal = max(totals.maxOf { it.second }.toFloat(), 1f)
        totals.forEachIndexed { index, pair ->
            val type = pair.first
            val total = pair.second
            val y = 306f + index * 31f
            drawFittedText(canvas, type.label, 38f, y + 11f, 112f, textPaint(8.5f, ink, bold = true))
            val bar = RectF(158f, y + 3f, 438f, y + 12f)
            canvas.drawRoundRect(bar, 5f, 5f, fillPaint(track))
            val width = 280f * (total.toFloat() / maxTotal).coerceIn(0f, 1f)
            if (width > 0f) {
                canvas.drawRoundRect(RectF(158f, y + 3f, 158f + width, y + 12f), 5f, 5f, fillPaint(accent))
            }
            drawFittedText(
                canvas,
                Money.format(total, currency),
                557f,
                y + 11f,
                110f,
                textPaint(8.5f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
    }

    private fun drawEmptyMonth(canvas: Canvas) {
        val empty = RectF(38f, 508f, 557f, 606f)
        canvas.drawRoundRect(empty, 16f, 16f, fillPaint(paper))
        canvas.drawRoundRect(empty, 16f, 16f, strokePaint(border, 1f))
        drawText(canvas, "A quiet month", 58f, 548f, 16f, ink, bold = true)
        drawText(canvas, "There are no purchases in the selected currency to report.", 58f, 571f, 9f, muted)
        drawText(canvas, "Your monthly spending total is 0.", 58f, 589f, 9f, muted)
    }

    private fun drawTransactionTable(
        canvas: Canvas,
        rows: List<Purchase>,
        currency: CurrencyCode,
        top: Float,
        zoneId: ZoneId,
    ) {
        drawText(canvas, "PURCHASE DETAILS", 38f, top, 9f, ink, bold = true)
        drawText(canvas, "DATE", 42f, top + 25f, 7f, muted, bold = true)
        drawText(canvas, "ITEM", 123f, top + 25f, 7f, muted, bold = true)
        drawText(canvas, "QTY", 420f, top + 25f, 7f, muted, bold = true, align = Paint.Align.RIGHT)
        drawText(canvas, "AMOUNT · " + currency.isoCode, 557f, top + 25f, 7f, muted, bold = true, align = Paint.Align.RIGHT)
        canvas.drawLine(38f, top + 34f, 557f, top + 34f, strokePaint(border, 1f))

        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
            .withZone(zoneId)
        rows.forEachIndexed { index, purchase ->
            val rowTop = top + 39f + index * ROW_HEIGHT
            val rowRect = RectF(34f, rowTop, 561f, rowTop + ROW_HEIGHT - 2f)
            if (index % 2 == 0) canvas.drawRoundRect(rowRect, 7f, 7f, fillPaint(paper))

            drawText(canvas, dateFormatter.format(Instant.ofEpochMilli(purchase.purchasedAtMillis)), 42f, rowTop + 15f, 8f, ink, bold = true)
            drawFittedText(
                canvas,
                purchase.brand.ifBlank { purchase.type.label },
                123f,
                rowTop + 14f,
                270f,
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
            }
            drawFittedText(canvas, secondary, 123f, rowTop + 27f, 270f, textPaint(7f, muted))
            drawText(canvas, purchase.quantity.toString(), 420f, rowTop + 21f, 8.5f, ink, align = Paint.Align.RIGHT)
            drawFittedText(
                canvas,
                Money.format(purchase.amountMinor, currency),
                557f,
                rowTop + 21f,
                112f,
                textPaint(8.5f, ink, bold = true, align = Paint.Align.RIGHT),
            )
        }
        if (rows.isEmpty()) {
            drawText(canvas, "No purchase details for this month.", 42f, top + 62f, 9f, muted)
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
        drawText(canvas, "BREATHE EASY  ·  PRIVATE SPENDING SUMMARY", 38f, 824f, 7f, muted, bold = true)
        drawText(canvas, "Generated " + generated + "  ·  " + currency.isoCode, 300f, 824f, 7f, muted, align = Paint.Align.CENTER)
        drawText(canvas, "$pageNumber / $totalPages", 557f, 824f, 7f, ink, bold = true, align = Paint.Align.RIGHT)
    }

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
