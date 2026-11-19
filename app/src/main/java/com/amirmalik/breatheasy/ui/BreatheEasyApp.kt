package com.amirmalik.breatheasy.ui
import com.amirmalik.breatheasy.domain.SafeArithmetic

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amirmalik.breatheasy.R
import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.AppSnapshot
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.CravingOutcome
import com.amirmalik.breatheasy.data.CravingTrigger
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.data.SmokeEvent
import com.amirmalik.breatheasy.domain.AnalyticsCalculator
import com.amirmalik.breatheasy.domain.Money
import com.amirmalik.breatheasy.domain.InventoryCalculator
import com.amirmalik.breatheasy.domain.DailyTargetProgress
import com.amirmalik.breatheasy.domain.DateTimeRanges
import com.amirmalik.breatheasy.domain.MonthNavigation
import com.amirmalik.breatheasy.ui.components.MonthlySpendingChart
import com.amirmalik.breatheasy.ui.components.MonthlyConsumptionChart
import com.amirmalik.breatheasy.ui.pdf.SpendingPdfExporter
import com.amirmalik.breatheasy.ui.pdf.SpendingReportPeriod
import com.amirmalik.breatheasy.ui.components.ProgressArc
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.UUID

private val ScreenHorizontalPadding = 20.dp
private val BreathingPhases = listOf("Breathe in", "Hold", "Breathe out", "Rest")

private enum class MainTab(val label: String) {
    TODAY("Today"), SPENDING("Spending"), CRAVINGS("Cravings"), GOALS("Goals"),
}

private data class LocalDayContext(
    val zone: ZoneId,
    val date: LocalDate,
)

private fun currentLocalDayContext(): LocalDayContext {
    val zone = ZoneId.systemDefault()
    return LocalDayContext(zone = zone, date = LocalDate.now(zone))
}

/**
 * Keeps date-sensitive screens aligned with the device's current local day.
 * Refreshes on foreground return and at local midnight, without polling.
 */
@Composable
private fun rememberLocalDayContext(): LocalDayContext {
    val lifecycleOwner = LocalLifecycleOwner.current
    val appContext = LocalContext.current.applicationContext
    var context by remember { mutableStateOf(currentLocalDayContext()) }

    DisposableEffect(appContext) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: android.content.Context?, intent: Intent?) {
                context = currentLocalDayContext()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { appContext.unregisterReceiver(receiver) }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                context = currentLocalDayContext()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(context.zone, context.date) {
        val nextDayStart = context.date.plusDays(1).atStartOfDay(context.zone)
            .toInstant().toEpochMilli()
        val millisUntilNextDay = (nextDayStart - System.currentTimeMillis()).coerceAtLeast(1L)
        delay(millisUntilNextDay)
        context = currentLocalDayContext()
    }

    return context
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreatheEasyApp(viewModel: BreatheEasyViewModel) {
    val snapshot by viewModel.snapshot.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val operationsBlocked = busy || snapshot.isLoading
    val loadError by viewModel.loadError.collectAsState()
    val operationRevision by viewModel.operationRevision.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TODAY) }
    var showPurchaseScreen by rememberSaveable { mutableStateOf(false) }
    var showSmokingHistory by rememberSaveable { mutableStateOf(false) }
    var showCravingDialog by rememberSaveable { mutableStateOf(false) }
    var cravingBreathingSeconds by rememberSaveable { mutableIntStateOf(0) }
    var purchaseToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val purchaseToDelete = snapshot.purchases.firstOrNull { it.id == purchaseToDeleteId }
    val savedPurchaseIds = remember(snapshot.purchases) { snapshot.purchases.mapTo(mutableSetOf()) { it.id } }
    val savedCravingIds = remember(snapshot.cravingEvents) { snapshot.cravingEvents.mapTo(mutableSetOf()) { it.id } }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteRevision by remember { mutableLongStateOf(-1L) }
    var purchaseDeleteError by remember { mutableStateOf<String?>(null) }
    var pdfWorkflowBusy by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveableStateHolder = rememberSaveableStateHolder()
    val trackedStock = remember(
        snapshot.preferences.startingStockCigarettes,
        snapshot.purchases,
        snapshot.smokeEvents,
    ) {
        InventoryCalculator.remainingCigarettes(
            startingStock = snapshot.preferences.startingStockCigarettes,
            purchases = snapshot.purchases,
            smokeEvents = snapshot.smokeEvents,
        )
    }

    // Messages are presentation-only. Form dismissal is driven by the saved
    // record appearing in the latest snapshot, and deletion completion is tracked
    // independently below so an old snackbar can never affect a newer operation.
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { resultMessage ->
            snackbarHostState.showSnackbar(resultMessage)
        }
    }

    LaunchedEffect(pendingDeleteId, pendingDeleteRevision, busy, operationRevision, snapshot.purchases, loadError) {
        val deletingId = pendingDeleteId
        val resolution = resolvePurchaseDeleteCompletion(
            pendingPurchaseId = deletingId,
            submittedRevision = pendingDeleteRevision,
            currentRevision = operationRevision,
            isBusy = busy,
            purchaseStillExists = deletingId != null && snapshot.purchases.any { it.id == deletingId },
            snapshotLoadFailed = loadError,
        )
        if (resolution.operationFinished) {
            pendingDeleteId = null
            pendingDeleteRevision = -1L
            if (resolution.closeConfirmation) {
                purchaseToDeleteId = null
                purchaseDeleteError = null
            } else {
                purchaseDeleteError = "Couldn't remove this purchase. It remains in your ledger; you can try again."
            }
        }
    }

    if (showSmokingHistory) {
        SmokingHistoryScreen(
            events = snapshot.smokeEvents,
            busy = operationsBlocked,
            operationRevision = operationRevision,
            snackbarHostState = snackbarHostState,
            onDelete = viewModel::deleteSmokeEvent,
            onBack = { showSmokingHistory = false },
        )
    } else if (showPurchaseScreen) {
        RecordPurchaseScreen(
            preferences = snapshot.preferences,
            busy = operationsBlocked,
            operationRevision = operationRevision,
            savedPurchaseIds = savedPurchaseIds,
            snackbarHostState = snackbarHostState,
            onDismiss = { showPurchaseScreen = false },
            onSave = viewModel::recordPurchase,
        )
    } else {
    // Keep the main tab composition's scroll and form-navigation state intact
    // while history or the purchase editor is shown above it.
    saveableStateHolder.SaveableStateProvider("main") {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier.size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.breathe_easy_mark),
                                contentDescription = stringResource(R.string.app_name),
                                modifier = Modifier.size(36.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                            Text(
                                stringResource(R.string.developer_credit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSmokingHistory = true },
                        enabled = !operationsBlocked && !pdfWorkflowBusy,
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = "Smoking history")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                tonalElevation = 2.dp,
            ) {
                MainTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        MainTab.TODAY -> Icons.Outlined.Home
                        MainTab.SPENDING -> Icons.Outlined.BarChart
                        MainTab.CRAVINGS -> Icons.Outlined.Air
                        MainTab.GOALS -> Icons.Outlined.Flag
                    }
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        enabled = !pdfWorkflowBusy,
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                        alwaysShowLabel = true,
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        ),
                    ),
                )
                .padding(scaffoldPadding),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "main-tab-content",
            ) { tab ->
                saveableStateHolder.SaveableStateProvider("tab-${tab.name}") {
                    when (tab) {
                    MainTab.TODAY -> TodayScreen(
                        snapshot = snapshot,
                        busy = operationsBlocked,
                        operationRevision = operationRevision,
                        trackedStock = trackedStock,
                        onLogSmoke = viewModel::logSmoke,
                        onOpenCravings = { selectedTab = MainTab.CRAVINGS },
                        onRecordPurchase = { if (!operationsBlocked) showPurchaseScreen = true },
                        onDeletePurchase = { purchaseToDeleteId = it.id },
                    )
                    MainTab.SPENDING -> SpendingScreen(
                        snapshot = snapshot,
                        busy = operationsBlocked,
                        onRecordPurchase = { if (!operationsBlocked && !pdfWorkflowBusy) showPurchaseScreen = true },
                        onDeletePurchase = { purchaseToDeleteId = it.id },
                        onPdfWorkflowBusyChanged = { pdfWorkflowBusy = it },
                    )
                    MainTab.CRAVINGS -> CravingsScreen(
                        snapshot = snapshot,
                        busy = operationsBlocked,
                        onRecordCraving = { seconds ->
                            if (!operationsBlocked) {
                                cravingBreathingSeconds = seconds
                                showCravingDialog = true
                            }
                        },
                    )
                    MainTab.GOALS -> GoalsScreen(
                        snapshot = snapshot,
                        onSave = viewModel::savePreferences,
                        busy = operationsBlocked,
                    )
                    }
                }
            }
            if (snapshot.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
    }

    }

    if (showCravingDialog) {
        RecordCravingDialog(
            busy = operationsBlocked,
            operationRevision = operationRevision,
            loadError = loadError,
            trackedStock = trackedStock,
            breathingSeconds = cravingBreathingSeconds,
            savedCravingIds = savedCravingIds,
            onDismiss = {
                showCravingDialog = false
                cravingBreathingSeconds = 0
            },
            onSave = viewModel::logCraving,
        )
    }
    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = {
                if (pendingDeleteId == null) {
                    purchaseToDeleteId = null
                    purchaseDeleteError = null
                }
            },
            title = { Text("Delete this purchase?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${purchase.brand.ifBlank { purchase.type.label }} · ${Money.format(purchase.amountMinor, purchase.currency)} will be removed from the ledger and monthly totals. Cigarette stock will be recalculated.")
                    purchaseDeleteError?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteId = purchase.id
                        pendingDeleteRevision = operationRevision
                        purchaseDeleteError = null
                        viewModel.deletePurchase(purchase.id)
                    },
                    enabled = pendingDeleteId == null && !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    if (pendingDeleteId == purchase.id) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (pendingDeleteId == purchase.id) "Deleting…" else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        purchaseToDeleteId = null
                        purchaseDeleteError = null
                    },
                    enabled = pendingDeleteId == null && !busy,
                ) { Text("Cancel") }
            },
        )
    }

    // Keep the blocking recovery prompt above any transient form or confirmation
    // dialog, including a craving form whose write succeeded before reload failed.
    if (loadError) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Can't load your records") },
            text = {
                Text("Your saved data has not been changed. Check your device storage and try loading it again.")
            },
            confirmButton = {
                Button(onClick = viewModel::refresh, enabled = !busy) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Retry")
                    }
                }
            },
        )
    }
}

@Composable
private fun TodayScreen(
    snapshot: AppSnapshot,
    busy: Boolean,
    operationRevision: Long,
    trackedStock: Long,
    onLogSmoke: (SmokeEvent, Boolean) -> Unit,
    onOpenCravings: () -> Unit,
    onRecordPurchase: () -> Unit,
    onDeletePurchase: (Purchase) -> Unit,
) {
    val prefs = snapshot.preferences
    val locale = LocalConfiguration.current.locales[0]
    var smokePending by rememberSaveable { mutableStateOf(false) }
    var smokeDraftId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var smokeDraftAtMillis by rememberSaveable { mutableLongStateOf(0L) }
    var smokeDraftPrepared by rememberSaveable { mutableStateOf(false) }

    fun submitSmoke(confirmUnstockedSmoking: Boolean) {
        // Capture the actual action time on the first attempt, then reuse the same
        // ID and timestamp if a failed write or reload needs an idempotent retry.
        if (!smokeDraftPrepared) {
            smokeDraftId = UUID.randomUUID().toString()
            smokeDraftAtMillis = System.currentTimeMillis()
            smokeDraftPrepared = true
        }
        smokePending = true
        onLogSmoke(
            SmokeEvent(
                id = smokeDraftId,
                smokedAtMillis = smokeDraftAtMillis,
                trigger = CravingTrigger.OTHER,
            ),
            confirmUnstockedSmoking,
        )
    }

    LaunchedEffect(busy, snapshot.smokeEvents, operationRevision) {
        if (!busy) {
            if (snapshot.smokeEvents.any { it.id == smokeDraftId }) {
                smokeDraftId = UUID.randomUUID().toString()
                smokeDraftAtMillis = 0L
                smokeDraftPrepared = false
            }
            smokePending = false
        }
    }
    var showStockConfirmation by rememberSaveable { mutableStateOf(false) }
    val loggingSmoke = busy && smokePending
    val localDay = rememberLocalDayContext()
    val todayZone = localDay.zone
    val todayDate = localDay.date
    val todayRange = remember(todayDate, todayZone) {
        DateTimeRanges.dayRangeMillis(todayDate, todayZone)
    }
    val smokedToday = remember(snapshot.smokeEvents, todayRange) {
        snapshot.cigarettesFor(todayRange.first, todayRange.last + 1)
    }
    val targetProgress = remember(smokedToday, prefs.dailyTarget) {
        DailyTargetProgress.calculate(smokedToday, prefs.dailyTarget)
    }
    val resisted = remember(snapshot.cravingEvents, todayRange) {
        snapshot.cravingEvents.count {
            it.outcome == CravingOutcome.RESISTED && it.occurredAtMillis in todayRange
        }
    }
    val month = YearMonth.from(todayDate)
    val spend = remember(snapshot.purchases, prefs.displayCurrency, month, todayZone) {
        AnalyticsCalculator.spending(
            snapshot.purchases,
            month,
            prefs.displayCurrency,
            monthCount = 6,
            zoneId = todayZone,
        )
    }
    val todayLabel = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale).format(todayDate)
    val recentPurchases = remember(snapshot.purchases) { snapshot.purchases.take(3) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, top = 16.dp, bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(todayLabel.uppercase(locale), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.1.sp)
                Text("Your day", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(18.dp)) {
                    val compact = maxWidth < 350.dp
                    if (compact) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                                ProgressArc(
                                    progress = targetProgress.progressFraction,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(smokedToday.toString(), style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("today", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                                }
                            }
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("Daily target", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Personal goal · ${prefs.dailyTarget} cigarettes per day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
                                Text(
                                    if (prefs.baselinePerDay > 0) "Previous baseline · ${prefs.baselinePerDay}/day"
                                    else "No baseline set",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                                )
                                DailyTargetStatus(progress = targetProgress)
                            }
                        }
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(116.dp), contentAlignment = Alignment.Center) {
                                ProgressArc(
                                    progress = targetProgress.progressFraction,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(smokedToday.toString(), style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("today", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Daily target", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Personal goal · ${prefs.dailyTarget} cigarettes per day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
                                Text(
                                    if (prefs.baselinePerDay > 0) "Previous baseline · ${prefs.baselinePerDay}/day"
                                    else "No baseline set",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                                )
                                DailyTargetStatus(progress = targetProgress)
                            }
                        }
                    }
                }
                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f))
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (trackedStock <= 0L) {
                                showStockConfirmation = true
                            } else {
                                submitSmoke(confirmUnstockedSmoking = false)
                            }
                        },
                        enabled = !busy && !snapshot.isLoading && !smokePending,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (loggingSmoke) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (loggingSmoke) "Logging…" else "Log cigarette")
                    }
                    FilledTonalButton(onClick = onOpenCravings, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Spa, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Take a pause")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    eyebrow = "Cravings resisted",
                    value = resisted.toString(),
                    caption = "today",
                    icon = Icons.Outlined.CheckCircle,
                    highlight = true,
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    eyebrow = "${prefs.displayCurrency.isoCode} purchases",
                    value = spend.purchaseCount.toString(),
                    caption = "this month",
                    icon = Icons.Outlined.Waves,
                )
            }
        }
        item {
            val stockIsEmpty = trackedStock == 0L
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stockIsEmpty) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.24f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
                border = BorderStroke(
                    1.dp,
                    if (stockIsEmpty) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    val stockContentColor = if (stockIsEmpty) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                            .background(
                                if (stockIsEmpty) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.13f)
                                else MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (stockIsEmpty) Icons.Outlined.Info else Icons.Outlined.Waves,
                            contentDescription = null,
                            tint = if (stockIsEmpty) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            if (stockIsEmpty) "No cigarettes tracked in stock" else "Estimated stock",
                            fontWeight = FontWeight.SemiBold,
                            color = stockContentColor,
                        )
                        if (stockIsEmpty) {
                            Text(
                                "Your tracked cigarette stock is 0. Logging a cigarette will ask you to confirm.",
                                style = MaterialTheme.typography.bodySmall,
                                color = stockContentColor.copy(alpha = 0.88f),
                            )
                        } else {
                            Text(
                                run {
                                    val fullPacks = trackedStock / 20
                                    val looseCigarettes = trackedStock % 20
                                    "$trackedStock cigarettes · $fullPacks ${if (fullPacks == 1L) "pack" else "packs"} + " +
                                        "$looseCigarettes ${if (looseCigarettes == 1L) "loose cigarette" else "loose cigarettes"}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "Estimate assumes 20 cigarettes per pack and 200 per carton; rolling tobacco is excluded.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (stockIsEmpty) stockContentColor.copy(alpha = 0.78f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            SectionHeader("Your spending")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("This month", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge)
                            Text(
                                Money.format(spend.totalMinor, prefs.displayCurrency),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        AssistChip(
                            onClick = onRecordPurchase,
                            enabled = !busy && !snapshot.isLoading,
                            label = { Text("Add purchase") },
                            leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                        )
                    }
                    if (spend.purchaseCount == 0) {
                        Text(
                            "No ${prefs.displayCurrency.isoCode} purchases this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            "${spend.purchaseCount} purchases · ${prefs.displayCurrency.isoCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                }
            }
        }
        item {
            SectionHeader("Recent purchases")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (recentPurchases.isEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Waves, contentDescription = null)
                        Text("No purchases yet", fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = onRecordPurchase, enabled = !busy && !snapshot.isLoading) {
                            Text("Record a purchase")
                        }
                    }
                } else {
                    Column {
                        recentPurchases.forEachIndexed { index, purchase ->
                            PurchaseRow(purchase, enabled = !busy, onDelete = { onDeletePurchase(purchase) })
                            if (index < recentPurchases.lastIndex) Divider(
                                Modifier.padding(start = 18.dp), color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        TextButton(
                            onClick = onRecordPurchase,
                            enabled = !busy && !snapshot.isLoading,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Record another purchase")
                        }
                    }
                }
            }
        }
    }

    if (showStockConfirmation) {
        StockConfirmationDialog(
            remainingCigarettes = trackedStock,
            onDismiss = { showStockConfirmation = false },
            onConfirm = {
                showStockConfirmation = false
                submitSmoke(confirmUnstockedSmoking = true)
            },
            enabled = !busy && !smokePending,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmokingHistoryScreen(
    events: List<SmokeEvent>,
    busy: Boolean,
    operationRevision: Long,
    snackbarHostState: SnackbarHostState,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    var eventToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val eventToDelete = events.firstOrNull { it.id == eventToDeleteId }

    // Let the repository finish and publish a fresh snapshot before re-enabling
    // deletion. A failed operation leaves the event available for a retry.
    LaunchedEffect(busy, events, operationRevision) {
        if (!busy && pendingDeleteId != null) {
            val completedId = pendingDeleteId
            pendingDeleteId = null
            if (events.none { it.id == completedId }) {
                eventToDeleteId = null
            }
        }
    }

    val localDay = rememberLocalDayContext()
    val zone = localDay.zone
    val today = localDay.date
    val locale = LocalConfiguration.current.locales[0]
    var selectedDateText by rememberSaveable { mutableStateOf(today.toString()) }
    var followsCurrentDay by rememberSaveable { mutableStateOf(true) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val selectedDate = remember(selectedDateText, today) {
        runCatching { LocalDate.parse(selectedDateText) }
            .getOrDefault(today)
            .coerceAtMost(today)
    }
    // The initial "Today" selection follows local midnight while the user has
    // not explicitly navigated to or selected a historical date.
    LaunchedEffect(today, followsCurrentDay) {
        if (followsCurrentDay && selectedDateText != today.toString()) {
            selectedDateText = today.toString()
        }
    }
    LaunchedEffect(selectedDateText, selectedDate) {
        if (selectedDateText != selectedDate.toString()) {
            selectedDateText = selectedDate.toString()
            followsCurrentDay = selectedDate == today
        }
    }
    val dayRange = remember(selectedDate, zone) { DateTimeRanges.dayRangeMillis(selectedDate, zone) }
    val dayEvents = remember(events, dayRange) {
        events.asSequence()
            .filter { it.smokedAtMillis >= dayRange.first && it.smokedAtMillis <= dayRange.last }
            .sortedByDescending { it.smokedAtMillis }
            .toList()
    }
    val count = remember(dayEvents) {
        dayEvents.fold(0L) { total, event -> SafeArithmetic.add(total, event.quantity.toLong()) }
    }
    val dateLabel = remember(selectedDate, today, locale) {
        when (selectedDate) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale).format(selectedDate)
        }
    }
    val timeFormatter = remember(zone, locale) {
        DateTimeFormatter.ofPattern("h:mm a", locale).withZone(zone)
    }
    // Recreate the picker state whenever the displayed date changes. Otherwise,
    // arrow navigation updates the screen but the calendar can reopen on an old date.
    val datePickerState = key(selectedDate, today, showDatePicker) {
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return !date.isAfter(today)
                }
                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            },
        )
    }

    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Smoking history", fontWeight = FontWeight.Bold)
                        Text("Your cigarette log", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, top = 12.dp, bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(dateLabel.uppercase(locale), style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.1.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(count.toString(), style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(if (count == 1L) "cigarette logged" else "cigarettes logged",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                                modifier = Modifier.padding(bottom = 7.dp))
                        }
                        Text("A clear, timestamped record of your day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = {
                                selectedDateText = selectedDate.minusDays(1).toString()
                                followsCurrentDay = false
                            },
                            modifier = Modifier.size(44.dp),
                        ) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous day") }
                        Surface(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.Button) { showDatePicker = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 13.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(9.dp))
                                Text(dateLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(
                            onClick = {
                                val nextDate = selectedDate.plusDays(1)
                                selectedDateText = nextDate.toString()
                                followsCurrentDay = nextDate == today
                            },
                            enabled = selectedDate.isBefore(today),
                            modifier = Modifier.size(44.dp),
                        ) { Icon(Icons.Outlined.ChevronRight, contentDescription = "Next day") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("TIMELINE", style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(dayEvents.size.toString() + if (dayEvents.size == 1) " entry" else " entries",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (dayEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(Modifier.size(64.dp).clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Waves, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(30.dp))
                            }
                            Text("No cigarettes logged", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text(
                                if (selectedDate == today) "Your day is clear so far. Any cigarettes you log will appear here."
                                else "There are no saved cigarette entries for this date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.width(76.dp), horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(timeFormatter.format(Instant.ofEpochMilli(event.smokedAtMillis)),
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.width(3.dp).height(66.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)))
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Waves, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        if (event.quantity == 1) "Cigarette logged"
                                        else event.quantity.toString() + " cigarettes logged",
                                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(event.trigger.label, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (event.quantity > 1) {
                                    Text("×" + event.quantity, style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                if (pendingDeleteId == event.id) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    IconButton(
                                        onClick = { eventToDeleteId = event.id },
                                        enabled = !busy && pendingDeleteId == null,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Delete cigarette log",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = {
                if (!busy && pendingDeleteId == null) eventToDeleteId = null
            },
            title = { Text("Delete this cigarette log?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${event.quantity} ${if (event.quantity == 1) "cigarette" else "cigarettes"} " +
                            "logged at ${timeFormatter.format(Instant.ofEpochMilli(event.smokedAtMillis))} will be removed.",
                    )
                    Text(
                        "Your daily count, monthly smoking trend, and tracked stock will be recalculated.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (event.note == "Logged from craving entry") {
                        Text(
                            "The linked smoked-craving entry will also be removed so your craving history stays consistent.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteId = event.id
                        eventToDeleteId = null
                        onDelete(event.id)
                    },
                    enabled = !busy && pendingDeleteId == null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete log") }
            },
            dismissButton = {
                TextButton(
                    onClick = { eventToDeleteId = null },
                    enabled = !busy && pendingDeleteId == null,
                ) { Text("Cancel") }
            },
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val chosenDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                        selectedDateText = chosenDate.toString()
                        followsCurrentDay = chosenDate == today
                    }
                    showDatePicker = false
                }) { Text("Show date") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SpendingScreen(
    snapshot: AppSnapshot,
    busy: Boolean,
    onRecordPurchase: () -> Unit,
    onDeletePurchase: (Purchase) -> Unit,
    onPdfWorkflowBusyChanged: (Boolean) -> Unit,
) {
    val prefs = snapshot.preferences
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val exportScope = rememberCoroutineScope()
    var reportPeriod by rememberSaveable { mutableStateOf(SpendingReportPeriod.CURRENT_MONTH) }
    // This work is in-memory and cannot survive process recreation.
    var isExportingPdf by remember { mutableStateOf(false) }
    // A system-picker launch is transient and must not survive process recreation.
    var isPickingPdfDestination by remember { mutableStateOf(false) }
    var pdfExportMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMonthAnchorText by rememberSaveable { mutableStateOf("") }
    val localDay = rememberLocalDayContext()
    val currentMonth = YearMonth.from(localDay.date)
    val selectedMonthAnchor = remember(selectedMonthAnchorText) {
        selectedMonthAnchorText.takeIf { it.isNotBlank() }
            ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
    }
    val selectedMonth = MonthNavigation.selectedMonth(selectedMonthAnchor, currentMonth)
    val monthOffset = MonthNavigation.offsetForMonth(selectedMonth, currentMonth)
        ?: MonthNavigation.MAX_MONTH_OFFSET
    val latestSnapshot = rememberUpdatedState(snapshot)
    val latestCurrency = rememberUpdatedState(prefs.displayCurrency)
    val latestPeriod = rememberUpdatedState(reportPeriod)
    val latestSelectedMonth = rememberUpdatedState(selectedMonth)
    val latestZone = rememberUpdatedState(localDay.zone)
    DisposableEffect(Unit) {
        onDispose {
            isExportingPdf = false
            isPickingPdfDestination = false
            onPdfWorkflowBusyChanged(false)
        }
    }
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destination ->
        isPickingPdfDestination = false
        if (destination != null) {
            isExportingPdf = true
            // Reassert the cross-tab guard if Android restored a pending
            // document-picker result after process recreation.
            onPdfWorkflowBusyChanged(true)
            pdfExportMessage = null
            // The SAF result may arrive after the screen has recomposed or the
            // selected reporting period has changed. Snapshot all inputs at the
            // moment the document is actually created.
            val purchasesForReport = latestSnapshot.value.purchases
            val currencyForReport = latestCurrency.value
            val periodForReport = latestPeriod.value
            val monthForReport = latestSelectedMonth.value
            val zoneForReport = latestZone.value
            exportScope.launch {
                val result = try {
                    withContext(Dispatchers.IO) {
                        SpendingPdfExporter.write(
                            context = context,
                            destination = destination,
                            purchases = purchasesForReport,
                            currency = currencyForReport,
                            period = periodForReport,
                            currentMonth = monthForReport,
                            zoneId = zoneForReport,
                        )
                    }
                    Result.success(Unit)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }
                isExportingPdf = false
                onPdfWorkflowBusyChanged(false)
                pdfExportMessage = result.fold(
                    onSuccess = { "PDF saved." },
                    onFailure = { "PDF export failed. Please try again." },
                )
            }
        } else {
            onPdfWorkflowBusyChanged(false)
        }
    }
    val summary = remember(snapshot.purchases, selectedMonth, prefs.displayCurrency, localDay.zone) {
        AnalyticsCalculator.spending(
            snapshot.purchases,
            selectedMonth,
            prefs.displayCurrency,
            monthCount = 12,
            zoneId = localDay.zone,
        )
    }
    val usagePoints = remember(snapshot.smokeEvents, selectedMonth, localDay.zone) {
        AnalyticsCalculator.monthlyConsumption(
            snapshot.smokeEvents,
            selectedMonth,
            monthCount = 12,
            zoneId = localDay.zone,
        )
    }
    val previous = summary.previousMonthTotalMinor
    val change = summary.changeFromPreviousMinor
    val monthPurchases = remember(snapshot.purchases, selectedMonth, localDay.zone) {
        snapshot.purchases.filter { purchase ->
            YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(localDay.zone)) == selectedMonth
        }
    }
    val selectedPurchases = remember(monthPurchases, prefs.displayCurrency) {
        monthPurchases.filter { it.currency.isoCode == prefs.displayCurrency.isoCode }
    }
    val hasOtherCurrencyPurchases = remember(monthPurchases, prefs.displayCurrency) {
        monthPurchases.any { it.currency.isoCode != prefs.displayCurrency.isoCode }
    }
    val breakdown = remember(selectedPurchases) {
        selectedPurchases.groupBy { it.type }.mapValues { (_, rows) ->
            rows.fold(0L) { total, purchase -> SafeArithmetic.add(total, purchase.amountMinor) }
        }
    }
    val maxBreakdown = breakdown.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val ledgerPurchases = monthPurchases

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = ScreenHorizontalPadding,
            top = 16.dp,
            end = ScreenHorizontalPadding,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Spending", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                FilledTonalButton(
                    onClick = onRecordPurchase,
                    enabled = !busy && !snapshot.isLoading && !isExportingPdf && !isPickingPdfDestination,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Add")
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${selectedMonth.month.getDisplayName(TextStyle.FULL, locale)} ${selectedMonth.year}",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("TOTAL SPENT · ${prefs.displayCurrency.isoCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                        }
                        Text(
                            Money.format(summary.totalMinor, prefs.displayCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { selectedMonthAnchorText = selectedMonth.minusMonths(1).toString() },
                            enabled = monthOffset < 60 && !isExportingPdf && !isPickingPdfDestination,
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        ) { Text("Older") }
                        OutlinedButton(
                            onClick = {
                                val nextMonth = selectedMonth.plusMonths(1)
                                selectedMonthAnchorText = if (nextMonth == currentMonth) "" else nextMonth.toString()
                            },
                            enabled = monthOffset > 0 && !isExportingPdf && !isPickingPdfDestination,
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        ) { Text("Newer") }
                    }
                    if (previous != null && monthOffset == 0) {
                        val direction = when {
                            change == null || change == 0L -> "No change from last month"
                            change < 0L -> "${Money.format(-change, prefs.displayCurrency)} less than last month"
                            else -> "${Money.format(change, prefs.displayCurrency)} more than last month"
                        }
                        Text(direction, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    MonthlySpendingChart(summary.monthlyPoints, prefs.displayCurrency)
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cigarettes logged", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (snapshot.smokeEvents.isEmpty()) {
                        Text(
                            "No cigarette logs yet. Your monthly trend will appear after you start recording.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        MonthlyConsumptionChart(usagePoints)
                    }
                }
            }
        }
        item {
            SectionHeader("Monthly history")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column {
                    summary.monthlyPoints.asReversed().forEachIndexed { index, point ->
                        val targetMonthOffset = MonthNavigation.offsetForMonth(point.month, currentMonth)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = targetMonthOffset != null, role = Role.Button) {
                                    targetMonthOffset?.let {
                                        selectedMonthAnchorText = if (point.month == currentMonth) "" else point.month.toString()
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    "${point.month.month.getDisplayName(TextStyle.FULL, locale)} ${point.month.year}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    when {
                                        targetMonthOffset == null -> "Outside the 5-year navigation range"
                                        point.purchaseCount == 0 -> "No ${prefs.displayCurrency.isoCode} purchases"
                                        else -> "${point.purchaseCount} purchases · ${prefs.displayCurrency.isoCode}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    Money.format(point.totalMinor, prefs.displayCurrency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        if (index < summary.monthlyPoints.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Spending breakdown")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (breakdown.isEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text("No ${prefs.displayCurrency.isoCode} spending to break down", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (hasOtherCurrencyPurchases) {
                                "Other-currency purchases remain in the ledger, but are excluded from this total. No conversion is applied."
                            } else {
                                "Record a ${prefs.displayCurrency.isoCode} purchase in this month to see how your spending is distributed."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PurchaseType.entries.forEachIndexed { index, type ->
                            val amount = breakdown[type] ?: 0L
                            if (amount > 0L) {
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(type.label, style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold)
                                        Text(Money.format(amount, prefs.displayCurrency),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Box(
                                        Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Box(
                                            Modifier.fillMaxWidth((amount.toDouble() / maxBreakdown.toDouble()).toFloat())
                                                .height(9.dp).clip(RoundedCornerShape(50))
                                                .background(if (index % 2 == 0) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.secondary),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeader("Purchase ledger") }
        if (ledgerPurchases.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        "No purchases this month.",
                        Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // Keep each history row lazy. A busy month should not create every
            // purchase card at once when the spending screen first opens.
            itemsIndexed(
                items = ledgerPurchases,
                key = { _, purchase -> purchase.id },
            ) { _, purchase ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    PurchaseRow(
                        purchase,
                        enabled = !busy && !isExportingPdf && !isPickingPdfDestination,
                        onDelete = { onDeletePurchase(purchase) },
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(25.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Spending report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("REPORT PERIOD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.7.sp)
                    SpendingReportPeriod.entries.chunked(2).forEach { periodRow ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            periodRow.forEach { period ->
                                FilterChip(
                                    selected = reportPeriod == period,
                                    enabled = !busy && !snapshot.isLoading && !isExportingPdf && !isPickingPdfDestination,
                                    onClick = { reportPeriod = period; pdfExportMessage = null },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(when (period) {
                                        SpendingReportPeriod.CURRENT_MONTH -> "Selected month"
                                        SpendingReportPeriod.LAST_3_MONTHS -> "3 months"
                                        SpendingReportPeriod.LAST_6_MONTHS -> "6 months"
                                        SpendingReportPeriod.LAST_12_MONTHS -> "12 months"
                                    }, maxLines = 1, overflow = TextOverflow.Ellipsis) })
                            }
                        }
                    }
                    Button(onClick = {
                        isPickingPdfDestination = true
                        onPdfWorkflowBusyChanged(true)
                        try {
                            createPdfLauncher.launch(
                                "Breathe-Easy-Spending-${reportPeriod.fileLabel}-${selectedMonth}.pdf",
                            )
                        } catch (_: Exception) {
                            isPickingPdfDestination = false
                            onPdfWorkflowBusyChanged(false)
                            pdfExportMessage = "Couldn't open a save location. Please try again."
                        }
                    }, enabled = !busy && !snapshot.isLoading && !isExportingPdf && !isPickingPdfDestination,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(17.dp)) {
                        if (isExportingPdf) CircularProgressIndicator(Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(9.dp))
                        Text(
                            when {
                                isExportingPdf -> "Creating PDF…"
                                isPickingPdfDestination -> "Choose a location…"
                                else -> "Create PDF"
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    pdfExportMessage?.let { messageText ->
                        Text(messageText, style = MaterialTheme.typography.bodySmall,
                            color = if (messageText.startsWith("PDF saved")) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun CravingsScreen(
    snapshot: AppSnapshot,
    busy: Boolean,
    onRecordCraving: (Int) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    // Persist elapsed session time, but never restore an actively running timer
    // after process/activity recreation. Time away from the screen must not count.
    val runningState = remember { mutableStateOf(false) }
    var running by runningState
    val secondsState = rememberSaveable { mutableIntStateOf(0) }
    var seconds by secondsState
    val lastTickState = remember { mutableLongStateOf(0L) }
    var lastTickElapsedMillis by lastTickState
    val lifecycleOwner = LocalLifecycleOwner.current

    fun pauseBreathingTimer() {
        if (runningState.value) {
            val now = android.os.SystemClock.elapsedRealtime()
            val elapsedSeconds = (now - lastTickState.longValue).coerceAtLeast(0L) / 1_000L
            val availableSeconds = (Int.MAX_VALUE - secondsState.intValue).toLong()
            val increment = elapsedSeconds.coerceAtMost(availableSeconds)
            secondsState.intValue += increment.toInt()
            runningState.value = false
        }
        lastTickState.longValue = 0L
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                pauseBreathingTimer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Derive elapsed time from Android's monotonic clock, not coroutine wake-up
    // counts. This avoids drift if the UI thread or device is briefly busy.
    LaunchedEffect(running) {
        if (running) {
            while (true) {
                delay(250)
                val now = android.os.SystemClock.elapsedRealtime()
                val elapsed = (now - lastTickElapsedMillis).coerceAtLeast(0L)
                if (elapsed >= 1_000L) {
                    val wholeSeconds = (elapsed / 1_000L)
                        .coerceAtMost((Int.MAX_VALUE - seconds).toLong())
                    if (wholeSeconds > 0L) {
                        seconds = (seconds.toLong() + wholeSeconds).toInt()
                        lastTickElapsedMillis += wholeSeconds * 1_000L
                    }
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { pauseBreathingTimer() }
    }
    val phases = BreathingPhases

    // Trigger counts depend only on saved craving history, not on the 1-second timer.
    val triggerCounts = remember(snapshot.cravingEvents) {
        snapshot.cravingEvents.groupingBy { it.trigger }.eachCount()
            .entries.sortedByDescending { it.value }.take(4)
    }
    val localDay = rememberLocalDayContext()
    val cravingDateFormatter = remember(localDay.zone, locale) {
        DateTimeFormatter.ofPattern("d MMM yyyy", locale)
            .withZone(localDay.zone)
    }
    val phase = phases[(seconds / 4) % phases.size]
    val phaseSeconds = 4 - (seconds % 4)
    val breathingScale by animateFloatAsState(
        targetValue = if (!running && seconds == 0) 0.92f
            else if (phase == "Breathe in" || phase == "Hold") 1f else 0.78f,
        animationSpec = tween(durationMillis = 3_800, easing = FastOutSlowInEasing),
        label = "breathing-orb-scale",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = ScreenHorizontalPadding,
            top = 16.dp,
            end = ScreenHorizontalPadding,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cravings", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Spa, contentDescription = null,
                        modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Box breathing", color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Box(
                        Modifier.size(176.dp).graphicsLayer {
                            scaleX = breathingScale
                            scaleY = breathingScale
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        ProgressArc(
                            progress = if (running || seconds > 0) ((seconds % 16) / 16f) else 0.08f,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 9.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (running || seconds > 0) "$phaseSeconds" else "4–4–4–4",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                            Text(
                                when {
                                    running -> phase
                                    seconds > 0 -> "Paused"
                                    else -> "Ready"
                                },
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        phases.forEach { phaseName ->
                            val active = (running || seconds > 0) && phase == phaseName
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                color = if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                            ) {
                                Text(
                                    when (phaseName) {
                                        "Breathe in" -> "In"
                                        "Breathe out" -> "Out"
                                        else -> phaseName
                                    },
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                    Text(
                        when {
                            running -> "Keep the rhythm gentle. Pause or reset whenever you need to."
                            seconds > 0 -> "Session paused. Resume when you're ready, or reset to start again."
                            else -> "Follow the rhythm gently. Pause or reset whenever you need to."
                        },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            if (running) {
                                pauseBreathingTimer()
                            } else {
                                lastTickElapsedMillis = android.os.SystemClock.elapsedRealtime()
                                running = true
                            }
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )) {
                            Text(
                                when {
                                    running -> "Pause"
                                    seconds > 0 -> "Resume"
                                    else -> "Start breathing"
                                },
                            )
                        }
                        OutlinedButton(onClick = {
                            running = false
                            lastTickElapsedMillis = 0L
                            seconds = 0
                        }) { Text("Reset") }
                    }
                    if (seconds > 0) Text("Session ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            Button(
                onClick = {
                    pauseBreathingTimer()
                    onRecordCraving(seconds)
                    // The captured value now belongs to the pending form. Reset the
                    // on-screen timer so it cannot be accidentally reused by the next
                    // craving entry after this form succeeds or is cancelled.
                    seconds = 0
                    lastTickElapsedMillis = 0L
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !busy && !snapshot.isLoading,
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log a craving", fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Common triggers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (triggerCounts.isEmpty()) {
                        Text("No trigger data yet.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val maxTriggerCount = triggerCounts.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
                        triggerCounts.forEachIndexed { index, (trigger, count) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(trigger.label, fontWeight = FontWeight.Medium)
                                    Text("$count", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium)
                                }
                                Box(
                                    Modifier.fillMaxWidth().height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    Box(
                                        Modifier.fillMaxWidth(count.toFloat() / maxTriggerCount)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (index == 0) MaterialTheme.colorScheme.secondary
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeader("Recent moments") }
        if (snapshot.cravingEvents.isEmpty()) {
            item {
                EmptyPanel("Nothing logged yet", "Log a craving to start spotting your patterns.")
            }
        } else {
            items(snapshot.cravingEvents.take(12), key = { it.id }) { event ->
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (event.outcome == CravingOutcome.RESISTED) Icons.Outlined.CheckCircle else Icons.Outlined.Air,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (event.outcome == CravingOutcome.RESISTED) "Craving resisted" else "Craving led to smoking",
                                fontWeight = FontWeight.SemiBold)
                            Text("${event.trigger.label} · intensity ${event.intensity}/5",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                            if (event.breathingSeconds > 0) {
                                Text(
                                    "Breathing session · ${event.breathingSeconds / 60}:${(event.breathingSeconds % 60).toString().padStart(2, '0')}",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        Text(cravingDateFormatter.format(Instant.ofEpochMilli(event.occurredAtMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsScreen(
    snapshot: AppSnapshot,
    onSave: (AppPreferences) -> Unit,
    busy: Boolean,
) {
    var target by rememberSaveable(snapshot.preferences.dailyTarget) {
        mutableStateOf(snapshot.preferences.dailyTarget.toString())
    }
    var baseline by rememberSaveable(snapshot.preferences.baselinePerDay) {
        mutableStateOf(snapshot.preferences.baselinePerDay.toString())
    }
    var startingStock by rememberSaveable(snapshot.preferences.startingStockCigarettes) {
        mutableStateOf(snapshot.preferences.startingStockCigarettes.toString())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = ScreenHorizontalPadding,
            top = 16.dp,
            end = ScreenHorizontalPadding,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Goals", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Currency", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${snapshot.preferences.displayCurrency.displayName} (${snapshot.preferences.displayCurrency.isoCode})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text("Set automatically on first launch",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Daily tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it.asciiDigitsOnly(maxLength = 3) },
                        enabled = !busy,
                        label = { Text("Daily tracking target (cigarettes)") },
                        isError = target.toIntOrNull() !in 1..200,
                        supportingText = {
                            when {
                                target.isBlank() -> Text("Required · enter 1–200.")
                                target.toIntOrNull() !in 1..200 -> Text("Enter a value from 1 to 200.")
                                else -> Text("Personal target, not a safe-use limit.")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = baseline,
                        onValueChange = { baseline = it.asciiDigitsOnly(maxLength = 3) },
                        enabled = !busy,
                        label = { Text("Previous daily baseline (cigarettes)") },
                        isError = baseline.toIntOrNull() !in 0..200,
                        supportingText = {
                            when {
                                baseline.isBlank() -> Text("Required · enter 0–200.")
                                baseline.toIntOrNull() !in 0..200 -> Text("Enter a value from 0 to 200.")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = startingStock,
                        onValueChange = { startingStock = it.asciiDigitsOnly(maxLength = 6) },
                        enabled = !busy,
                        label = { Text("Opening stock (before first purchase)") },
                        isError = startingStock.toIntOrNull() !in 0..100_000,
                        supportingText = {
                            when {
                                startingStock.isBlank() -> Text("Required · enter 0–100,000.")
                                startingStock.toIntOrNull() !in 0..100_000 -> Text("Enter a value from 0 to 100,000.")
                                else -> Text("Opening balance for your stock estimate. Changing it adjusts the total tracked stock.")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    onSave(
                        AppPreferences(
                            displayCurrency = snapshot.preferences.displayCurrency,
                            dailyTarget = target.toIntOrNull()?.coerceIn(1, 200) ?: 4,
                            baselinePerDay = baseline.toIntOrNull()?.coerceIn(0, 200) ?: 15,
                            startingStockCigarettes = startingStock.toIntOrNull()?.coerceIn(0, 100_000) ?: 0,
                        ),
                    )
                },
                enabled = !busy && target.toIntOrNull() in 1..200 && baseline.toIntOrNull() in 0..200
                    && startingStock.toIntOrNull() in 0..100_000,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (busy) "Saving…" else "Save goals") }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                Text("Your records stay on this device.", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPurchaseScreen(
    preferences: AppPreferences,
    busy: Boolean,
    operationRevision: Long,
    savedPurchaseIds: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Purchase) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var type by rememberSaveable { mutableStateOf(PurchaseType.PACK) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var amount by rememberSaveable { mutableStateOf("") }
    var purchaseDraftId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var purchaseDraftPrepared by rememberSaveable { mutableStateOf(false) }
    val currency = preferences.displayCurrency
    val locale = LocalConfiguration.current.locales[0]
    val localDay = rememberLocalDayContext()
    var brand by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var planned by rememberSaveable { mutableStateOf(true) }
    var purchasedAtMillis by rememberSaveable {
        mutableLongStateOf(
            localDay.date.atTime(12, 0)
                .atZone(localDay.zone).toInstant().toEpochMilli(),
        )
    }
    var hasCustomPurchaseDate by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(localDay.date, localDay.zone, hasCustomPurchaseDate, purchaseDraftPrepared) {
        // Once a draft is submitted its ID and timestamp are an idempotency pair.
        // Do not move the timestamp at midnight while a failed/ambiguous save may
        // still need to be retried with exactly the same payload.
        if (shouldRefreshDefaultPurchaseDate(hasCustomPurchaseDate, purchaseDraftPrepared)) {
            purchasedAtMillis = localDay.date.atTime(12, 0)
                .atZone(localDay.zone).toInstant().toEpochMilli()
        }
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    var submitPending by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(busy, operationRevision) {
        if (!busy) submitPending = false
    }
    val saving = busy || submitPending

    fun prepareNewPurchaseDraftForEdit() {
        // Keep the same ID for an exact retry. Once a failed attempt is edited,
        // the changed payload must receive a new ID or SQLite correctly rejects it
        // as a conflicting reuse of an idempotency key.
        if (purchaseDraftPrepared && !saving) {
            purchaseDraftId = UUID.randomUUID().toString()
            purchaseDraftPrepared = false
        }
    }

    LaunchedEffect(purchaseDraftId, savedPurchaseIds, busy) {
        if (!busy && purchaseDraftId in savedPurchaseIds) {
            // The process may have been recreated after the insert committed but
            // before the success result reached the UI.
            onDismiss()
        }
    }

    val amountMinor = Money.parseToMinor(amount, currency, locale)
    val quantityValue = quantity.toIntOrNull()
    val canSave = amountMinor != null && quantityValue in 1..10_000
    val quantityLabel = when (type) {
        PurchaseType.PACK -> "packs"
        PurchaseType.SINGLE -> "cigarettes"
        PurchaseType.CARTON -> "cartons"
        PurchaseType.ROLLING -> "units"
    }
    val dateLabel = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale)
        .withZone(localDay.zone)
        .format(Instant.ofEpochMilli(purchasedAtMillis))


    BackHandler(enabled = true) {
        if (!saving) onDismiss()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onDismiss, enabled = !saving) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "Add purchase",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.4).sp,
                        )
                        Text(
                            "Record a transaction in your ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Button(
                        onClick = {
                            purchaseDraftPrepared = true
                            submitPending = true
                            onSave(
                                Purchase(
                                    id = purchaseDraftId,
                                    purchasedAtMillis = purchasedAtMillis,
                                    type = type,
                                    quantity = quantityValue ?: 1,
                                    amountMinor = amountMinor ?: 0L,
                                    currency = currency,
                                    brand = brand,
                                    location = location,
                                    planned = planned,
                                    note = note,
                                ),
                            )
                        },
                        enabled = canSave && !saving,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(19.dp),
                    ) {
                        if (saving) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Saving purchase…", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Save purchase", modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                                Text(
                                    amountMinor?.let { Money.format(it, currency, includeCode = true) }.orEmpty(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            Modifier.fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Purchase details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "Enter the total amount paid for this transaction, then choose what and how much you bought.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WHAT DID YOU BUY?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                    PurchaseType.entries.chunked(2).forEach { options ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            options.forEach { option ->
                                val selected = type == option
                                Surface(modifier = Modifier.weight(1f).heightIn(min = 64.dp).clip(RoundedCornerShape(17.dp))
                                    .selectable(
                                        selected = selected,
                                        enabled = !saving,
                                        role = Role.RadioButton,
                                        onClick = {
                                            prepareNewPurchaseDraftForEdit()
                                            type = option
                                        },
                                    )
                                    .animateContentSize(),
                                    shape = RoundedCornerShape(17.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(if (selected) 1.5.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text(option.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        if (selected) Icon(Icons.Outlined.CheckCircle, contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            if (options.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL SPEND", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f), letterSpacing = 0.9.sp)
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)) {
                                Text(currency.isoCode, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        OutlinedTextField(
                            value = amount,
                            enabled = !saving,
                            onValueChange = {
                                prepareNewPurchaseDraftForEdit()
                                amount = it.asciiMoneyInput(
                                    decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator,
                                    maxLength = 18,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(Money.inputPlaceholder(currency, locale))
                            },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                            isError = amount.isNotBlank() && amountMinor == null,
                            supportingText = {
                                if (amount.isNotBlank() && amountMinor == null) Text("Enter a valid ${currency.isoCode} amount.",
                                    color = MaterialTheme.colorScheme.error)

                            })
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("PAYING IN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f), letterSpacing = 0.7.sp)
                                Text(currency.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (amountMinor != null) Text(Money.format(amountMinor, currency, includeCode = true),
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Quantity", fontWeight = FontWeight.SemiBold)
                        Text(
                            quantityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    prepareNewPurchaseDraftForEdit()
                                    val current = quantity.toIntOrNull() ?: 1
                                    quantity = (current - 1).coerceAtLeast(1).toString()
                                },
                                enabled = !saving && (quantity.toIntOrNull() ?: 1) > 1,
                            ) {
                                Icon(Icons.Outlined.Remove, contentDescription = "Decrease quantity")
                            }
                            Text(
                                quantity,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(44.dp),
                                maxLines = 1,
                            )
                            IconButton(
                                onClick = {
                                    prepareNewPurchaseDraftForEdit()
                                    val current = quantity.toIntOrNull() ?: 1
                                    quantity = (current + 1).coerceAtMost(10_000).toString()
                                },
                                enabled = !saving && (quantity.toIntOrNull() ?: 1) < 10_000,
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Increase quantity")
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(9.dp))
                    Text("Date  ·  $dateLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDetails = !showDetails },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    if (showDetails) "Hide details" else "Add details",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "Brand, shop, context and notes — all optional",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                if (showDetails) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = if (showDetails) "Hide optional details" else "Show optional details",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    AnimatedVisibility(visible = showDetails, enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = {
                                prepareNewPurchaseDraftForEdit()
                                brand = it.take(60)
                            },
                            label = { Text("Brand or product") },
                            enabled = !saving,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                prepareNewPurchaseDraftForEdit()
                                location = it.take(80)
                            },
                            label = { Text("Shop or location") },
                            placeholder = { Text("Optional") },
                            enabled = !saving,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Purchase context",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = planned,
                                enabled = !saving,
                                onClick = {
                                    prepareNewPurchaseDraftForEdit()
                                    planned = true
                                },
                                label = { Text("Planned") },
                            )
                            FilterChip(
                                selected = !planned,
                                enabled = !saving,
                                onClick = {
                                    prepareNewPurchaseDraftForEdit()
                                    planned = false
                                },
                                label = { Text("Impulse") },
                            )
                        }
                        OutlinedTextField(
                            value = note,
                            onValueChange = {
                                prepareNewPurchaseDraftForEdit()
                                note = it.take(500)
                            },
                            label = { Text("Note (optional)") },
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                        }
                    }
                }

                if (quantityValue == null || quantityValue !in 1..10_000) {
                    Text(
                        "Quantity must be between 1 and 10,000.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

        }
    }

    if (showDatePicker) {
        val today = localDay.date
        val initialPurchaseDateUtc = Instant.ofEpochMilli(purchasedAtMillis)
            .atZone(localDay.zone)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = key(localDay.zone, today) {
            rememberDatePickerState(
                initialSelectedDateMillis = initialPurchaseDateUtc,
                selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val chosenDate = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    return !chosenDate.isAfter(today)
                }

                    override fun isSelectableYear(year: Int): Boolean = year <= today.year
                },
            )
        }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val selectedDate = Instant.ofEpochMilli(utcMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        prepareNewPurchaseDraftForEdit()
                        purchasedAtMillis = selectedDate.atTime(12, 0)
                            .atZone(localDay.zone)
                            .toInstant()
                            .toEpochMilli()
                        hasCustomPurchaseDate = true
                    }
                    showDatePicker = false
                }) { Text("Apply date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@Composable
private fun RecordCravingDialog(
    busy: Boolean,
    operationRevision: Long,
    loadError: Boolean,
    trackedStock: Long,
    breathingSeconds: Int,
    savedCravingIds: Set<String>,
    onDismiss: () -> Unit,
    onSave: (CravingEvent, Boolean) -> Unit,
) {
    var outcome by rememberSaveable { mutableStateOf(CravingOutcome.RESISTED) }
    var trigger by rememberSaveable { mutableStateOf(CravingTrigger.STRESS) }
    var intensity by rememberSaveable { mutableIntStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    var cravingDraftId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var cravingOccurredAtMillis by rememberSaveable { mutableLongStateOf(0L) }
    var cravingDraftPrepared by rememberSaveable { mutableStateOf(false) }
    var submitPending by rememberSaveable { mutableStateOf(false) }
    var showStockConfirmation by rememberSaveable { mutableStateOf(false) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(busy, operationRevision, savedCravingIds, loadError) {
        if (!busy && submitPending) {
            submitPending = false
            saveError = if (cravingDraftId !in savedCravingIds && !loadError) {
                "Couldn't save this craving. Your details are still here; try again."
            } else {
                null
            }
        }
    }
    val saving = busy || submitPending

    fun prepareNewCravingDraftForEdit() {
        // Preserve the ID for an exact retry, but never reuse it for edited details.
        if (cravingDraftPrepared && !saving) {
            cravingDraftId = UUID.randomUUID().toString()
            cravingOccurredAtMillis = 0L
            cravingDraftPrepared = false
        }
        saveError = null
    }

    LaunchedEffect(cravingDraftId, savedCravingIds, busy) {
        if (!busy && cravingDraftId in savedCravingIds) {
            onDismiss()
        }
    }

    fun submitCraving(confirmUnstockedSmoking: Boolean) {
        if (!cravingDraftPrepared) {
            cravingDraftId = UUID.randomUUID().toString()
            cravingOccurredAtMillis = System.currentTimeMillis()
            cravingDraftPrepared = true
        }
        submitPending = true
        showStockConfirmation = false
        saveError = null
        onSave(
            CravingEvent(
                id = cravingDraftId,
                occurredAtMillis = cravingOccurredAtMillis,
                outcome = outcome,
                trigger = trigger,
                intensity = intensity,
                note = note,
                breathingSeconds = breathingSeconds.coerceAtLeast(0),
            ),
            confirmUnstockedSmoking,
        )
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Record a craving", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("What happened?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                saveError?.let { message ->
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = outcome == CravingOutcome.RESISTED,
                        enabled = !saving,
                        onClick = {
                            prepareNewCravingDraftForEdit()
                            outcome = CravingOutcome.RESISTED
                        }, label = { Text("I resisted") })
                    FilterChip(
                        selected = outcome == CravingOutcome.SMOKED,
                        enabled = !saving,
                        onClick = {
                            prepareNewCravingDraftForEdit()
                            outcome = CravingOutcome.SMOKED
                        }, label = { Text("I smoked") })
                }
                Text("What triggered it?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CravingTrigger.entries.forEach { item ->
                        FilterChip(
                            selected = trigger == item,
                            enabled = !saving,
                            onClick = {
                                prepareNewCravingDraftForEdit()
                                trigger = item
                            },
                            label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
                Text("Intensity: $intensity / 5", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(
                            selected = intensity == value,
                            enabled = !saving,
                            onClick = {
                                prepareNewCravingDraftForEdit()
                                intensity = value
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(value.toString()) },
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        prepareNewCravingDraftForEdit()
                        note = it.take(500)
                    },
                    enabled = !saving,
                    label = { Text("What was happening? (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                if (outcome == CravingOutcome.SMOKED) {
                    Text("This will also add one cigarette to today's smoking log.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (outcome == CravingOutcome.SMOKED && trackedStock <= 0L) {
                        showStockConfirmation = true
                    } else {
                        submitCraving(confirmUnstockedSmoking = false)
                    }
                },
                enabled = !saving,
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…")
                } else {
                    Text("Save moment")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )

    if (showStockConfirmation) {
        StockConfirmationDialog(
            remainingCigarettes = trackedStock,
            onDismiss = { showStockConfirmation = false },
            onConfirm = { submitCraving(confirmUnstockedSmoking = true) },
            enabled = !saving,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    eyebrow: String,
    value: String,
    caption: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlight: Boolean = false,
) {
    val container = if (highlight) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surface
    val content = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurface
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = if (highlight) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 1.dp else 0.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                    .background(content.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (highlight)
                    MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
            Text(eyebrow, style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.76f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold,
                    color = content, maxLines = 1)
                Text(caption, style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.72f), modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun PurchaseRow(purchase: Purchase, enabled: Boolean = true, onDelete: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                purchase.brand.ifBlank { purchase.type.label },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${purchase.quantity} × ${purchase.type.label.lowercase()} · ${DateTimeFormatter.ofPattern("d MMM yyyy", locale).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(purchase.purchasedAtMillis))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (purchase.location.isNotBlank()) Text(purchase.location, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Money.format(purchase.amountMinor, purchase.currency, includeCode = true),
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(if (purchase.planned) "Planned" else "Impulse", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete, enabled = enabled) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete purchase",
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
    }
}

private fun String.asciiDigitsOnly(maxLength: Int): String = buildString {
    for (character in this@asciiDigitsOnly) {
        val digit = Character.digit(character, 10)
        if (digit >= 0) append(('0'.code + digit).toChar())
        if (length >= maxLength) break
    }
}

private fun String.asciiMoneyInput(decimalSeparator: Char, maxLength: Int): String = buildString {
    for (character in this@asciiMoneyInput) {
        val digit = Character.digit(character, 10)
        when {
            digit >= 0 -> append(('0'.code + digit).toChar())
            character == decimalSeparator || character == '.' || character == ',' -> append(character)
        }
        if (length >= maxLength) break
    }
}

@Composable
private fun DailyTargetStatus(progress: DailyTargetProgress) {
    val isAboveGoal = progress.excessCigarettes > 0L
    val isGoalReached = progress.remainingCigarettes == 0L && !isAboveGoal

    val title = when {
        isAboveGoal -> {
            val count = progress.excessCigarettes
            "$count ${if (count == 1L) "cigarette" else "cigarettes"} above your daily goal"
        }
        isGoalReached -> "Daily goal reached"
        else -> {
            val count = progress.remainingCigarettes
            "$count ${if (count == 1L) "cigarette" else "cigarettes"} left to your daily goal"
        }
    }
    val detail = when {
        isAboveGoal -> "No judgement — this is useful information. Tomorrow is another day."
        isGoalReached -> "You've reached the target you set for today."
        else -> "You're within the personal target you set for today."
    }
    val containerColor = when {
        isAboveGoal -> MaterialTheme.colorScheme.tertiaryContainer
        isGoalReached -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isAboveGoal -> MaterialTheme.colorScheme.onTertiaryContainer
        isGoalReached -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusIcon = when {
        isAboveGoal -> Icons.Outlined.Info
        isGoalReached -> Icons.Outlined.CheckCircle
        else -> Icons.Outlined.Flag
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.82f),
                )
            }
        }
    }
}

@Composable
private fun StockConfirmationDialog(
    remainingCigarettes: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    enabled: Boolean,
) {
    // This dialog is only opened for depleted stock. Clamp defensively so its
    // message can never expose a negative inventory value.
    val stockCount = remainingCigarettes.coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        iconContentColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 8.dp,
        icon = {
            Box(
                modifier = Modifier.size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        title = {
            Text(
                "Stock is empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Tracked cigarettes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "$stockCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Text(
                    "Are you sure you smoked a cigarette?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "If you confirm, it will be added to your smoking totals only. Stock will stay at 0, and no purchase or spending will be recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Yes, I smoked")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = enabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text("No, cancel")
            }
        },
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Spa, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
