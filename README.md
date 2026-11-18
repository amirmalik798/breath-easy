# BreatheEasy

BreatheEasy is an offline-first native Android companion for mindful smoking reduction, craving awareness, and transparent spending insights. The Kotlin + Jetpack Compose UI follows the calm forest-and-sage direction of the supplied Google Stitch concept.

## Included in this initial implementation

- Native Android app with Today, Spending, Cravings, and Goals screens.
- Daily cigarette logging, craving outcome/trigger/intensity journaling, and a paced-breathing session.
- Purchase ledger for packs, loose cigarettes, cartons, and rolling tobacco with quantity, amount, purchase date, currency, brand, location, notes, and planned/impulse classification.
- Monthly cash-spend reporting based on the purchase date, with month selection, a 12-month spending chart, and a separate monthly cigarette-consumption trend.
- PKR, RWF, and USD as first-class currencies. Original transaction currencies are always retained; RWF uses whole currency units, while PKR and USD support two decimal places.
- Optional user-entered USD reference rates for PKR and RWF. Each transaction keeps the rates that were in effect when it was recorded; changing settings does not rewrite historical calculations.
- Clear disclosure when conversion rates are missing. The report still shows totals in each original currency and does not invent a rate or silently treat an unconverted purchase as zero.
- Stock reconciliation using opening stock + recorded pack/carton/single purchases - cigarettes logged as smoked. Rolling tobacco is not assigned an invented cigarette yield.
- Local SQLite persistence, write-ahead logging, indexed dates, and a transaction that keeps a smoked craving and its smoking event in sync.

## Currency and spending rules

The amount entered for a purchase is the total amount paid for that transaction. The monthly cash-spend report groups it by purchase date; the consumption graph groups smoking events by the time they were logged. These are intentionally separate measures.

Choose a preferred reporting currency in **Goals & settings**. To combine different transaction currencies in one chart, enter how many PKR and RWF equal one USD. Rates are entered and controlled by the user. The rate snapshot is stored with each purchase, ensuring that later rate edits do not rewrite past reports. The original purchase currency and amount remain available in every month breakdown.

## Build

Requirements: Android Studio with the Android 17 / API 37 SDK platform, JDK 17, and internet access for the first Gradle distribution and dependency downloads.

```bash
sh gradlew :app:testDebugUnitTest
sh gradlew :app:assembleDebug
```

On Windows, use `gradlew.bat` with the same tasks. The bootstrap scripts download the pinned Gradle 9.6.0 distribution. Android Gradle Plugin 9.5.0 and Kotlin 2.4.21 are pinned in the build configuration.

## Privacy and local data

Records are stored in the app-private SQLite database on the device. This initial build includes no account, analytics SDK, advertising SDK, or network permission. Android auto-backup is disabled. Clearing app data or uninstalling removes local records, so this version should not be treated as a backup system.

## Validation status

The pure Kotlin money, currency conversion, monthly analytics, and inventory logic was compiled and exercised with local smoke checks. Full Android unit tests and APK assembly must be confirmed by Android Studio or the GitHub Actions workflow; they have not been claimed as passing in this environment.