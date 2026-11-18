package com.amirmalik.breatheasy.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Small, offline-first SQLite store. Financial values are persisted as integer minor units;
 * no binary floating-point values are used for money.
 */
internal class BreatheEasyDatabase(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext

    init {
        setWriteAheadLoggingEnabled(true)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE purchases (
                id TEXT PRIMARY KEY NOT NULL,
                purchased_at_ms INTEGER NOT NULL,
                product_type TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                amount_minor INTEGER NOT NULL CHECK(amount_minor > 0),
                currency_code TEXT NOT NULL CHECK(length(currency_code) = 3),
                brand TEXT NOT NULL DEFAULT '',
                location TEXT NOT NULL DEFAULT '',
                planned INTEGER NOT NULL DEFAULT 1 CHECK(planned IN (0,1)),
                note TEXT NOT NULL DEFAULT '',
                created_at_ms INTEGER NOT NULL
            )""".trimIndent(),
        )
        db.execSQL("CREATE INDEX index_purchases_date ON purchases(purchased_at_ms DESC)")
        db.execSQL("CREATE INDEX index_purchases_currency_date ON purchases(currency_code, purchased_at_ms DESC)")

        db.execSQL(
            """CREATE TABLE smoke_events (
                id TEXT PRIMARY KEY NOT NULL,
                smoked_at_ms INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                trigger_code TEXT NOT NULL,
                note TEXT NOT NULL DEFAULT ''
            )""".trimIndent(),
        )
        db.execSQL("CREATE INDEX index_smoke_events_date ON smoke_events(smoked_at_ms DESC)")

        db.execSQL(
            """CREATE TABLE craving_events (
                id TEXT PRIMARY KEY NOT NULL,
                occurred_at_ms INTEGER NOT NULL,
                outcome TEXT NOT NULL CHECK(outcome IN ('RESISTED','SMOKED')),
                trigger_code TEXT NOT NULL,
                intensity INTEGER NOT NULL CHECK(intensity BETWEEN 1 AND 5),
                note TEXT NOT NULL DEFAULT '',
                breathing_seconds INTEGER NOT NULL DEFAULT 0 CHECK(breathing_seconds >= 0)
            )""".trimIndent(),
        )
        db.execSQL("CREATE INDEX index_craving_events_date ON craving_events(occurred_at_ms DESC)")

        db.execSQL(
            """CREATE TABLE app_settings (
                setting_key TEXT PRIMARY KEY NOT NULL,
                setting_value TEXT NOT NULL
            )""".trimIndent(),
        )
        insertDefaultSetting(db, "display_currency", CurrencyCode.detect(appContext).isoCode)
        insertDefaultSetting(db, "daily_target", "4")
        insertDefaultSetting(db, "baseline_per_day", "15")
        insertDefaultSetting(db, "starting_stock_cigarettes", "0")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2 && newVersion >= 2) {
            migrateV1ToV2(db)
            return
        }
        throw IllegalStateException("No database migration path from $oldVersion to $newVersion")
    }

    /**
     * Expand the purchase currency constraint to all ISO-4217 currencies and retire
     * the old, hard-coded exchange-rate columns. Existing purchase rows and their
     * original amounts/currency codes are copied without conversion.
     */
    private fun migrateV1ToV2(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE purchases_v2 (
                id TEXT PRIMARY KEY NOT NULL,
                purchased_at_ms INTEGER NOT NULL,
                product_type TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                amount_minor INTEGER NOT NULL CHECK(amount_minor > 0),
                currency_code TEXT NOT NULL CHECK(length(currency_code) = 3),
                brand TEXT NOT NULL DEFAULT '',
                location TEXT NOT NULL DEFAULT '',
                planned INTEGER NOT NULL DEFAULT 1 CHECK(planned IN (0,1)),
                note TEXT NOT NULL DEFAULT '',
                created_at_ms INTEGER NOT NULL
            )""".trimIndent(),
        )
        db.execSQL(
            """INSERT INTO purchases_v2 (
                id, purchased_at_ms, product_type, quantity, amount_minor, currency_code,
                brand, location, planned, note, created_at_ms
            )
            SELECT id, purchased_at_ms, product_type, quantity, amount_minor, currency_code,
                brand, location, planned, note, created_at_ms
            FROM purchases""".trimIndent(),
        )
        db.execSQL("DROP TABLE purchases")
        db.execSQL("ALTER TABLE purchases_v2 RENAME TO purchases")
        db.execSQL("CREATE INDEX index_purchases_date ON purchases(purchased_at_ms DESC)")
        db.execSQL("CREATE INDEX index_purchases_currency_date ON purchases(currency_code, purchased_at_ms DESC)")
        db.execSQL("DELETE FROM app_settings WHERE setting_key IN ('pkr_per_usd', 'rwf_per_usd')")
        db.execSQL(
            "INSERT OR IGNORE INTO app_settings(setting_key, setting_value) VALUES('display_currency', ?)",
            arrayOf(CurrencyCode.detect(appContext).isoCode),
        )
    }

    /**
     * Read every collection and its preferences from one SQLite snapshot. Without
     * a transaction, a concurrent insert between SELECTs could make the UI combine
     * records from different points in time. This is a non-exclusive transaction so
     * WAL readers can continue while the consistent snapshot is assembled.
     */
    fun readSnapshot(): AppSnapshot {
        val db = readableDatabase
        db.beginTransactionNonExclusive()
        try {
            val snapshot = AppSnapshot(
                preferences = readPreferences(db),
                purchases = readPurchases(db),
                smokeEvents = readSmokeEvents(db),
                cravingEvents = readCravingEvents(db),
                isLoading = false,
            )
            db.setTransactionSuccessful()
            return snapshot
        } finally {
            db.endTransaction()
        }
    }

    fun savePreferences(preferences: AppPreferences) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            putSetting(db, "daily_target", preferences.dailyTarget.toString())
            putSetting(db, "baseline_per_day", preferences.baselinePerDay.toString())
            putSetting(db, "starting_stock_cigarettes", preferences.startingStockCigarettes.toString())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertPurchase(purchase: Purchase) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", purchase.id)
            put("purchased_at_ms", purchase.purchasedAtMillis)
            put("product_type", purchase.type.name)
            put("quantity", purchase.quantity)
            put("amount_minor", purchase.amountMinor)
            put("currency_code", purchase.currency.isoCode)
            put("brand", purchase.brand)
            put("location", purchase.location)
            put("planned", if (purchase.planned) 1 else 0)
            put("note", purchase.note)
            put("created_at_ms", System.currentTimeMillis())
        }
        val inserted = db.insertWithOnConflict(
            "purchases",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE,
        )
        if (inserted != -1L) return

        // A restored form may retry after its original insert committed but before
        // the success UI was delivered. The stable ID makes that retry idempotent.
        val samePurchase = db.rawQuery(
            """SELECT purchased_at_ms, product_type, quantity, amount_minor, currency_code,
                brand, location, planned, note FROM purchases WHERE id = ?""".trimIndent(),
            arrayOf(purchase.id),
        ).use { cursor ->
            cursor.moveToFirst() &&
                cursor.getLong(0) == purchase.purchasedAtMillis &&
                cursor.getString(1) == purchase.type.name &&
                cursor.getInt(2) == purchase.quantity &&
                cursor.getLong(3) == purchase.amountMinor &&
                cursor.getString(4) == purchase.currency.isoCode &&
                cursor.getString(5) == purchase.brand &&
                cursor.getString(6) == purchase.location &&
                cursor.getInt(7) == if (purchase.planned) 1 else 0 &&
                cursor.getString(8) == purchase.note
        }
        check(samePurchase) {
            "This purchase entry ID is already associated with different details. Start a new entry."
        }
    }

    fun deletePurchase(id: String): Boolean =
        writableDatabase.delete("purchases", "id = ?", arrayOf(id)) == 1

    fun insertSmokeEvent(event: SmokeEvent) {
        val values = ContentValues().apply {
            put("id", event.id)
            put("smoked_at_ms", event.smokedAtMillis)
            put("quantity", event.quantity)
            put("trigger_code", event.trigger.name)
            put("note", event.note)
        }
        writableDatabase.insertOrThrow("smoke_events", null, values)
    }

    fun insertCravingEvent(event: CravingEvent) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("id", event.id)
                put("occurred_at_ms", event.occurredAtMillis)
                put("outcome", event.outcome.name)
                put("trigger_code", event.trigger.name)
                put("intensity", event.intensity)
                put("note", event.note)
                put("breathing_seconds", event.breathingSeconds)
            }
            val inserted = db.insertWithOnConflict(
                "craving_events",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE,
            )

            if (inserted == -1L) {
                val sameCraving = db.rawQuery(
                    """SELECT occurred_at_ms, outcome, trigger_code, intensity, note, breathing_seconds
                        FROM craving_events WHERE id = ?""".trimIndent(),
                    arrayOf(event.id),
                ).use { cursor ->
                    cursor.moveToFirst() &&
                        cursor.getLong(0) == event.occurredAtMillis &&
                        cursor.getString(1) == event.outcome.name &&
                        cursor.getString(2) == event.trigger.name &&
                        cursor.getInt(3) == event.intensity &&
                        cursor.getString(4) == event.note &&
                        cursor.getInt(5) == event.breathingSeconds
                }
                check(sameCraving) {
                    "This craving entry ID is already associated with different details. Start a new entry."
                }
            }

            if (event.outcome == CravingOutcome.SMOKED) {
                val smokeId = "craving-${event.id}"
                val smokeValues = ContentValues().apply {
                    put("id", smokeId)
                    put("smoked_at_ms", event.occurredAtMillis)
                    put("quantity", 1)
                    put("trigger_code", event.trigger.name)
                    put("note", "Logged from craving entry")
                }
                if (inserted != -1L) {
                    db.insertOrThrow("smoke_events", null, smokeValues)
                } else {
                    val sameSmoke = db.rawQuery(
                        "SELECT smoked_at_ms, quantity, trigger_code, note FROM smoke_events WHERE id = ?",
                        arrayOf(smokeId),
                    ).use { cursor ->
                        cursor.moveToFirst() &&
                            cursor.getLong(0) == event.occurredAtMillis &&
                            cursor.getInt(1) == 1 &&
                            cursor.getString(2) == event.trigger.name &&
                            cursor.getString(3) == "Logged from craving entry"
                    }
                    if (!sameSmoke) {
                        val repaired = db.insertWithOnConflict(
                            "smoke_events",
                            null,
                            smokeValues,
                            SQLiteDatabase.CONFLICT_IGNORE,
                        )
                        if (repaired == -1L) {
                            val matches = db.rawQuery(
                                "SELECT smoked_at_ms, quantity, trigger_code, note FROM smoke_events WHERE id = ?",
                                arrayOf(smokeId),
                            ).use { cursor ->
                                cursor.moveToFirst() &&
                                    cursor.getLong(0) == event.occurredAtMillis &&
                                    cursor.getInt(1) == 1 &&
                                    cursor.getString(2) == event.trigger.name &&
                                    cursor.getString(3) == "Logged from craving entry"
                            }
                            check(matches) { "The linked smoking record conflicts with this craving entry." }
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun readPreferences(db: SQLiteDatabase): AppPreferences {
        val values = mutableMapOf<String, String>()
        db.rawQuery("SELECT setting_key, setting_value FROM app_settings", null).use { cursor ->
            while (cursor.moveToNext()) values[cursor.getString(0)] = cursor.getString(1)
        }
        val storedCurrencyCode = values["display_currency"]
        val displayCurrency = CurrencyCode.fromCodeOrNull(storedCurrencyCode)
            ?: CurrencyCode.detect(appContext).also { detected ->
                // Repair a missing or invalid setting once. Do not let a later locale
                // change reinterpret an already persisted currency choice on every read.
                putSetting(db, "display_currency", detected.isoCode)
            }

        return AppPreferences(
            displayCurrency = displayCurrency,
            dailyTarget = values["daily_target"]?.toIntOrNull()?.coerceIn(1, 200) ?: 4,
            baselinePerDay = values["baseline_per_day"]?.toIntOrNull()?.coerceIn(0, 200) ?: 15,
            startingStockCigarettes = values["starting_stock_cigarettes"]?.toIntOrNull()?.coerceIn(0, 100_000) ?: 0,
        )
    }

    private fun readPurchases(db: SQLiteDatabase): List<Purchase> = db.rawQuery(
        """SELECT id, purchased_at_ms, product_type, quantity, amount_minor, currency_code,
            brand, location, planned, note
            FROM purchases ORDER BY purchased_at_ms DESC""".trimIndent(),
        null,
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    Purchase(
                        id = cursor.getString(0),
                        purchasedAtMillis = cursor.getLong(1),
                        type = enumValueOrNull<PurchaseType>(cursor.getString(2))
                            ?: throw IllegalStateException("A purchase contains an unsupported product type."),
                        quantity = cursor.getInt(3),
                        amountMinor = cursor.getLong(4),
                        currency = CurrencyCode.fromCodeOrNull(cursor.getString(5))
                            ?: throw IllegalStateException("A purchase contains an unsupported currency code."),
                        brand = cursor.getString(6),
                        location = cursor.getString(7),
                        planned = cursor.getInt(8) == 1,
                        note = cursor.getString(9),
                    ),
                )
            }
        }
    }

    private fun readSmokeEvents(db: SQLiteDatabase): List<SmokeEvent> = db.rawQuery(
        "SELECT id, smoked_at_ms, quantity, trigger_code, note FROM smoke_events ORDER BY smoked_at_ms DESC",
        null,
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    SmokeEvent(
                        id = cursor.getString(0),
                        smokedAtMillis = cursor.getLong(1),
                        quantity = cursor.getInt(2),
                        trigger = enumValueOrDefault(cursor.getString(3), CravingTrigger.OTHER),
                        note = cursor.getString(4),
                    ),
                )
            }
        }
    }

    private fun readCravingEvents(db: SQLiteDatabase): List<CravingEvent> = db.rawQuery(
        """SELECT id, occurred_at_ms, outcome, trigger_code, intensity, note, breathing_seconds
            FROM craving_events ORDER BY occurred_at_ms DESC""".trimIndent(),
        null,
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    CravingEvent(
                        id = cursor.getString(0),
                        occurredAtMillis = cursor.getLong(1),
                        outcome = enumValueOrNull<CravingOutcome>(cursor.getString(2))
                            ?: throw IllegalStateException("A craving contains an unsupported outcome."),
                        trigger = enumValueOrDefault(cursor.getString(3), CravingTrigger.OTHER),
                        intensity = cursor.getInt(4).coerceIn(1, 5),
                        note = cursor.getString(5),
                        breathingSeconds = cursor.getInt(6).coerceAtLeast(0),
                    ),
                )
            }
        }
    }

    private fun putSetting(db: SQLiteDatabase, key: String, value: String) {
        db.insertWithOnConflict(
            "app_settings",
            null,
            ContentValues().apply {
                put("setting_key", key)
                put("setting_value", value)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    private fun insertDefaultSetting(db: SQLiteDatabase, key: String, value: String) {
        putSetting(db, key, value)
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { it.name == value }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, fallback: T): T =
        enumValueOrNull<T>(value) ?: fallback

    companion object {
        private const val DATABASE_NAME = "breathe_easy.db"
        private const val DATABASE_VERSION = 2
    }
}
