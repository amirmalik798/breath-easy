# Breathe Easy

Breathe Easy is an offline-first native Android companion for mindful smoking reduction, craving awareness, and transparent spending insights. The Kotlin + Jetpack Compose UI uses a warm orange-and-charcoal visual system with clear light and dark themes.

## Included in this initial implementation

- Native Android app with Today, Spending, Cravings, and Goals screens.
- Daily cigarette logging, craving outcome/trigger/intensity journaling, and a paced-breathing session.
- Purchase ledger for packs, loose cigarettes, cartons, and rolling tobacco with quantity, amount, purchase date, currency, brand, location, notes, and planned/impulse classification.
- Monthly cash-spend reporting based on the purchase date, with month selection, a 12-month spending chart, and a separate monthly cigarette-consumption trend.
- Automatic first-run currency selection using the mobile network country when available, with device-region fallback. Currency precision, symbols, and names are sourced from the platform ISO-4217 currency database.
- The detected currency is stored once for the installation and remains fixed while travelling. Spending totals and charts include only transactions recorded in that currency; older transactions keep their original currency and amount.
- No exchange-rate entry or cross-currency conversion is used, so values in different currencies are never added together.
- Stock reconciliation using opening stock + recorded pack/carton/single purchases - cigarettes logged as smoked. Rolling tobacco is not assigned an invented cigarette yield.
- Local SQLite persistence, write-ahead logging, indexed dates, and a transaction that keeps a smoked craving and its smoking event in sync.

## Currency and spending rules

The amount entered for a purchase is the total amount paid for that transaction. The monthly cash-spend report groups it by purchase date; the consumption graph groups smoking events by the time they were logged. These are intentionally separate measures.

BreatheEasy selects a currency automatically on first launch using the currently registered mobile network country when available, then the device's configured region as a fallback. No location permission or network exchange-rate lookup is required. The choice is stored once for the installation and is not changed when the user travels. Spending graphs, monthly totals, and purchase-count spending statistics include only records in that selected currency. Older purchases in other currencies remain visible in the ledger with their original amounts; they are not converted or included in selected-currency totals.

## Build

Requirements: Android Studio with the Android 17 / API 37 preview SDK platform and Build Tools 37.0.0, JDK 17, and internet access for the first Gradle distribution and dependency downloads.

```bash
sh gradlew :app:testDebugUnitTest
sh gradlew :app:assembleDebug
```

On Windows, use `gradlew.bat` with the same tasks. The bootstrap scripts download the pinned Gradle 9.6.0 distribution. Android Gradle Plugin 9.5.0 is used with AGP's built-in Kotlin support and the Kotlin Compose compiler plugin 2.4.10.

## Privacy and local data

Records are stored in the app-private SQLite database on the device. This initial build includes no account, analytics SDK, advertising SDK, or network permission. Android auto-backup is disabled. Clearing app data or uninstalling removes local records, so this version should not be treated as a backup system.

## Validation status

Money formatting and parsing use each selected currency's ISO minor-unit precision. Monthly spending analytics filter by the single installation currency and never combine unlike currencies.

## Entry validation and consistency

New records are validated at the repository boundary, not only in the visible forms. Purchases require a positive total amount, a supported ISO-4217 currency with matching minor-unit precision, a quantity from 1 to 10,000, and a purchase date no later than the current local calendar date. Smoking entries accept 1 to 100 cigarettes per event; craving intensity is limited to 1–5.

A craving recorded as **Smoked** creates one matching smoking event in the same SQLite transaction. Either both records are saved or neither is saved. A **Resisted** craving does not alter cigarette consumption.

Stock is an estimate: opening stock + recorded pack/carton/loose-cigarette purchases − cigarettes explicitly logged as smoked. Full packs count as 20 cigarettes and cartons as 200. Rolling tobacco is excluded because its yield is user-dependent. Negative stock is retained as a reconciliation signal rather than preventing a smoking entry.

The previous daily baseline is shown as a personal reference alongside the daily tracking target. Neither value is presented as a medically recommended safe limit.
