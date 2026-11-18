package com.amirmalik.breatheasy.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amirmalik.breatheasy.data.AppPreferences
import com.amirmalik.breatheasy.data.AppSnapshot
import com.amirmalik.breatheasy.data.CravingEvent
import com.amirmalik.breatheasy.data.CravingOutcome
import com.amirmalik.breatheasy.data.CravingTrigger
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.domain.AnalyticsCalculator
import com.amirmalik.breatheasy.domain.Money
import com.amirmalik.breatheasy.domain.InventoryCalculator
import com.amirmalik.breatheasy.ui.components.MonthlySpendingChart
import com.amirmalik.breatheasy.ui.components.MonthlyConsumptionChart
import com.amirmalik.breatheasy.ui.components.ProgressArc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class MainTab(val label: String) {
    TODAY("Today"), SPENDING("Spending"), CRAVINGS("Cravings"), GOALS("Goals"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreatheEasyApp(viewModel: BreatheEasyViewModel) {
    val snapshot by viewModel.snapshot.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val message by viewModel.message.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TODAY) }
    var showPurchaseDialog by rememberSaveable { mutableStateOf(false) }
    var showCravingDialog by rememberSaveable { mutableStateOf(false) }
    var purchaseToDelete by remember { mutableStateOf<Purchase?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text("BreatheEasy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp)
                        Text("Small steps. More breathing room.", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { selectedTab = MainTab.GOALS }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
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
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        alwaysShowLabel = true,
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
                when (tab) {
                    MainTab.TODAY -> TodayScreen(
                        snapshot = snapshot,
                        onLogSmoke = { trigger, note -> viewModel.logSmoke(trigger, note) },
                        onOpenCravings = { selectedTab = MainTab.CRAVINGS; showCravingDialog = true },
                        onRecordPurchase = { showPurchaseDialog = true },
                        onDeletePurchase = { purchaseToDelete = it },
                    )
                    MainTab.SPENDING -> SpendingScreen(
                        snapshot = snapshot,
                        onRecordPurchase = { showPurchaseDialog = true },
                        onDeletePurchase = { purchaseToDelete = it },
                    )
                    MainTab.CRAVINGS -> CravingsScreen(
                        snapshot = snapshot,
                        onRecordCraving = { showCravingDialog = true },
                    )
                    MainTab.GOALS -> GoalsScreen(
                        snapshot = snapshot,
                        onSave = viewModel::savePreferences,
                        busy = busy,
                    )
                }
            }
            if (busy && snapshot.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }

    if (showPurchaseDialog) {
        RecordPurchaseDialog(
            preferences = snapshot.preferences,
            onDismiss = { showPurchaseDialog = false },
            onSave = { purchase ->
                viewModel.recordPurchase(purchase)
                showPurchaseDialog = false
            },
        )
    }
    if (showCravingDialog) {
        RecordCravingDialog(
            onDismiss = { showCravingDialog = false },
            onSave = { event ->
                viewModel.logCraving(event)
                showCravingDialog = false
            },
        )
    }
    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text("Delete this purchase?") },
            text = { Text("${purchase.brand.ifBlank { purchase.type.label }} · ${Money.format(purchase.amountMinor, purchase.currency)} will be removed from the ledger and monthly totals. Cigarette stock will be recalculated.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deletePurchase(purchase.id)
                    purchaseToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { purchaseToDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TodayScreen(
    snapshot: AppSnapshot,
    onLogSmoke: (CravingTrigger, String) -> Unit,
    onOpenCravings: () -> Unit,
    onRecordPurchase: () -> Unit,
    onDeletePurchase: (Purchase) -> Unit,
) {
    val prefs = snapshot.preferences
    val todayDate = java.time.LocalDate.now()
    val todayRange = remember(todayDate) { BreatheEasyViewModel.todayRangeMillis() }
    val smokedToday = snapshot.cigarettesFor(todayRange.first, todayRange.last + 1)
    val resisted = snapshot.cravingEvents.count {
        it.outcome == CravingOutcome.RESISTED && it.occurredAtMillis in todayRange
    }
    val month = YearMonth.now()
    val spend = remember(snapshot.purchases, prefs.displayCurrency, month) {
        AnalyticsCalculator.spending(snapshot.purchases, month, prefs.displayCurrency, 6)
    }
    val todayLabel = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(todayDate)
    val recentPurchases = snapshot.purchases.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp, end = 18.dp, top = 16.dp, bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(todayLabel.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.1.sp)
                Text("A fresh chance to choose", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Track without judgement. Your progress is yours.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(128.dp), contentAlignment = Alignment.Center) {
                        ProgressArc(progress = smokedToday.toFloat() / prefs.dailyTarget.coerceAtLeast(1),
                            modifier = Modifier.fillMaxSize())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(smokedToday.toString(), style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("today", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Your daily rhythm", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$smokedToday of ${prefs.dailyTarget} cigarettes in your personal target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
                        val remaining = (prefs.dailyTarget - smokedToday).coerceAtLeast(0)
                        Text(
                            if (remaining > 0) "$remaining below your target" else "Your target has been reached",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text("A personal tracking goal — not medical advice.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                    }
                }
                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f))
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onLogSmoke(CravingTrigger.OTHER, "") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log cigarette")
                    }
                    FilledTonalButton(onClick = onOpenCravings, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Spa, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Need a pause?")
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
                    eyebrow = "Purchases logged",
                    value = snapshot.purchasesFor(month)
                        .count { it.currency.isoCode == prefs.displayCurrency.isoCode }
                        .toString(),
                    caption = "this month",
                    icon = Icons.Outlined.Waves,
                )
            }
        }
        item {
            val stock = InventoryCalculator.remainingCigarettes(
                startingStock = prefs.startingStockCigarettes,
                purchases = snapshot.purchases,
                smokeEvents = snapshot.smokeEvents,
            )
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Waves, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Tracked cigarette stock", fontWeight = FontWeight.SemiBold)
                        if (stock >= 0L) {
                            Text("${stock} sticks · ${stock / 20} full-pack equivalent(s) and ${stock % 20} loose",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("${-stock} more smoked than purchases logged. Add your opening stock in Goals to reconcile the counter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Your spending", "Actual purchase costs")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Spent this month", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge)
                            Text(
                                Money.format(spend.totalMinor, prefs.displayCurrency),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        AssistChip(onClick = onRecordPurchase, label = { Text("Add purchase") },
                            leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) })
                    }
                    if (spend.purchaseCount == 0) {
                        Text(
                            "No purchases recorded in ${prefs.displayCurrency.isoCode} this month. Older entries in another currency remain in your ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            "${spend.purchaseCount} purchase(s) recorded in ${prefs.displayCurrency.isoCode}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("Monthly totals are based on purchase dates, not estimated cigarette consumption.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            SectionHeader("Recent purchases", "Your personal spending ledger")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (recentPurchases.isEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Waves, contentDescription = null)
                        Text("No purchases yet", fontWeight = FontWeight.SemiBold)
                        Text("Your records will appear here when you add a purchase.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onRecordPurchase) { Text("Record first purchase") }
                    }
                } else {
                    Column {
                        recentPurchases.forEachIndexed { index, purchase ->
                            PurchaseRow(purchase, onDelete = { onDeletePurchase(purchase) })
                            if (index < recentPurchases.lastIndex) Divider(
                                Modifier.padding(start = 18.dp), color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        TextButton(onClick = onRecordPurchase, modifier = Modifier.align(Alignment.End)) {
                            Text("Record another purchase")
                        }
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Spa, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Give the urge a little space", color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold)
                        Text("Try a short, guided breathing pause before deciding what comes next.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = onOpenCravings) {
                        Icon(Icons.Outlined.Air, contentDescription = "Open breathing exercise",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendingScreen(
    snapshot: AppSnapshot,
    onRecordPurchase: () -> Unit,
    onDeletePurchase: (Purchase) -> Unit,
) {
    val prefs = snapshot.preferences
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val selectedMonth = YearMonth.now().minusMonths(monthOffset.toLong())
    val summary = remember(snapshot.purchases, selectedMonth, prefs.displayCurrency) {
        AnalyticsCalculator.spending(snapshot.purchases, selectedMonth, prefs.displayCurrency, 12)
    }
    val usagePoints = remember(snapshot.smokeEvents, selectedMonth) {
        AnalyticsCalculator.monthlyConsumption(snapshot.smokeEvents, selectedMonth, 12)
    }
    val previous = summary.previousMonthTotalMinor
    val change = summary.changeFromPreviousMinor

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp, 16.dp, 18.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Spending insights", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Your smoking costs, clearly and month by month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onRecordPurchase) {
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
                            Text("${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Actual purchase spending · ${prefs.displayCurrency.isoCode}",
                                style = MaterialTheme.typography.labelSmall,
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
                            onClick = { monthOffset = (monthOffset + 1).coerceAtMost(60) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        ) {
                            Text("Previous month")
                        }
                        OutlinedButton(onClick = { monthOffset = (monthOffset - 1).coerceAtLeast(0) },
                            enabled = monthOffset > 0,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        ) {
                            Text("Next month")
                        }
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
                    Text(
                        "Charts include only purchases recorded in ${prefs.displayCurrency.isoCode}. Entries in other currencies remain unchanged in your ledger and are not mixed into these totals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
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
                    Text("A separate view of consumption activity — not purchase spending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    MonthlyConsumptionChart(usagePoints)
                }
            }
        }
        item {
            SectionHeader("Monthly spending history", "Exact totals for every month · tap to inspect")
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column {
                    summary.monthlyPoints.asReversed().forEachIndexed { index, point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    monthOffset = java.time.temporal.ChronoUnit.MONTHS
                                        .between(point.month, YearMonth.now()).toInt().coerceAtLeast(0)
                                }
                                .padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    "${point.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${point.month.year}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    if (point.purchaseCount == 0) "No purchases recorded"
                                    else "${point.purchaseCount} purchase(s) in ${prefs.displayCurrency.isoCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    Money.format(point.totalMinor, prefs.displayCurrency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text("${point.purchaseCount} purchases included",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            SectionHeader("Spending breakdown", "By purchase type")
            val selectedPurchases = snapshot.purchases.filter { purchase ->
                YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(ZoneId.systemDefault())) == selectedMonth
                    && purchase.currency.isoCode == prefs.displayCurrency.isoCode
            }
            val breakdown = selectedPurchases.groupBy { it.type }.mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                if (breakdown.isEmpty()) {
                    Text("No purchases in ${prefs.displayCurrency.isoCode} for this month.", Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PurchaseType.entries.forEach { type ->
                            val amount = breakdown[type] ?: 0L
                            if (amount > 0L) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(type.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(Money.format(amount, prefs.displayCurrency), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Purchase ledger", "Original purchase amounts are preserved for this month")
            val ledgerPurchases = snapshot.purchases.filter { purchase ->
                YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(ZoneId.systemDefault())) == selectedMonth
            }
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (ledgerPurchases.isEmpty()) {
                    Text("No purchases recorded for this month.", Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column {
                        ledgerPurchases.forEachIndexed { index, purchase ->
                            PurchaseRow(purchase, onDelete = { onDeletePurchase(purchase) })
                            if (index < ledgerPurchases.lastIndex) Divider(
                                Modifier.padding(start = 18.dp), color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CravingsScreen(
    snapshot: AppSnapshot,
    onRecordCraving: () -> Unit,
) {
    var running by rememberSaveable { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        while (running) {
            delay(1_000)
            seconds++
        }
    }
    val phases = listOf("Breathe in", "Hold", "Breathe out", "Rest")
    val phase = phases[(seconds / 4) % phases.size]
    val phaseSeconds = 4 - (seconds % 4)
    val breathingScale by animateFloatAsState(
        targetValue = if (!running) 0.92f
            else if (phase == "Breathe in" || phase == "Hold") 1f else 0.78f,
        animationSpec = tween(durationMillis = 3_800, easing = FastOutSlowInEasing),
        label = "breathing-orb-scale",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp, 16.dp, 18.dp, 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Craving space", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Notice the urge. Slow down. Choose your next step.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Mindful pause", color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Box(
                        Modifier.size(176.dp).graphicsLayer {
                            scaleX = breathingScale
                            scaleY = breathingScale
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        ProgressArc(
                            progress = if (running) ((seconds % 16) / 16f) else 0.08f,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 9.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (running) "$phaseSeconds" else "4–4–4–4",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                            Text(if (running) phase else "box breathing",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Text(
                        if (running) "Follow the pace gently. There is no need to force or hold your breath."
                        else "A gentle 4-second rhythm for each phase. Breathe comfortably and stop if you feel unwell.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { running = !running }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )) { Text(if (running) "Pause" else "Start breathing") }
                        OutlinedButton(onClick = { running = false; seconds = 0 }) { Text("Reset") }
                    }
                    if (running) Text("Session ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Log what happened", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Record a craving you rode out or one that led to smoking. Both are useful information.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onRecordCraving, modifier = Modifier.fillMaxWidth()) {
                        Text("Record a craving")
                    }
                }
            }
        }
        item {
            val triggerCounts = snapshot.cravingEvents.groupingBy { it.trigger }.eachCount()
                .entries.sortedByDescending { it.value }.take(4)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Patterns you have logged", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (triggerCounts.isEmpty()) {
                        Text("As you record cravings, common triggers will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val maxTriggerCount = triggerCounts.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
                        triggerCounts.forEachIndexed { index, (trigger, count) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(trigger.label, fontWeight = FontWeight.Medium)
                                    Text("$count logged", color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        item { SectionHeader("Recent moments", "Your last recorded cravings") }
        if (snapshot.cravingEvents.isEmpty()) {
            item {
                EmptyPanel("Nothing logged yet", "Your craving history will appear here — privately on this device.")
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
                        }
                        Text(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()).withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(event.occurredAtMillis)),
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
    var target by remember(snapshot.preferences) { mutableStateOf(snapshot.preferences.dailyTarget.toString()) }
    var baseline by remember(snapshot.preferences) { mutableStateOf(snapshot.preferences.baselinePerDay.toString()) }
    var startingStock by remember(snapshot.preferences) { mutableStateOf(snapshot.preferences.startingStockCigarettes.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp, 16.dp, 18.dp, 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Goals & settings", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Shape the app around your own tracking preferences.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Currency for this device", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${snapshot.preferences.displayCurrency.displayName} (${snapshot.preferences.displayCurrency.isoCode})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        "Selected automatically from your mobile network country or device region on first launch. It stays fixed for this installation, including when you travel. Spending charts include only purchases recorded in this currency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Personal tracking goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it.filter(Char::isDigit).take(3) },
                        label = { Text("Daily tracking target (cigarettes)") },
                        supportingText = { Text("A personal tracking setting, not a recommended safe amount.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = baseline,
                        onValueChange = { baseline = it.filter(Char::isDigit).take(3) },
                        label = { Text("Previous daily baseline (cigarettes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = startingStock,
                        onValueChange = { startingStock = it.filter(Char::isDigit).take(6) },
                        label = { Text("Starting stock on hand (cigarettes)") },
                        supportingText = { Text("Used to keep the stock counter accurate when you start using the app.") },
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
            ) { Text(if (busy) "Saving…" else "Save preferences") }
        }
        item {
            EmptyPanel(
                title = "Your records stay on this device",
                body = "Purchase, cigarette and craving entries are stored locally in the app database. No account or cloud sync is required in this build.",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPurchaseDialog(
    preferences: AppPreferences,
    onDismiss: () -> Unit,
    onSave: (Purchase) -> Unit,
) {
    var type by remember { mutableStateOf(PurchaseType.PACK) }
    var quantity by remember { mutableStateOf("1") }
    var amount by remember { mutableStateOf("") }
    val currency = preferences.displayCurrency
    var brand by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var planned by remember { mutableStateOf(true) }
    var purchasedAtMillis by remember {
        mutableLongStateOf(
            LocalDate.now().atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) }

    val amountMinor = Money.parseToMinor(amount, currency)
    val quantityValue = quantity.toIntOrNull()
    val canSave = amountMinor != null && quantityValue in 1..10_000
    val quantityLabel = when (type) {
        PurchaseType.PACK -> "packs"
        PurchaseType.SINGLE -> "cigarettes"
        PurchaseType.CARTON -> "cartons"
        PurchaseType.ROLLING -> "units"
    }
    val dateLabel = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(purchasedAtMillis))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                Modifier.fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(25.dp),
                        )
                    }
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            "Add a purchase",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "A quick note for your spending history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        "WHAT DID YOU BUY?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.7.sp,
                    )
                    PurchaseType.entries.chunked(2).forEach { options ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            options.forEach { option ->
                                FilterChip(
                                    selected = type == option,
                                    onClick = { type = option },
                                    modifier = Modifier.weight(1f),
                                    label = {
                                        Text(
                                            option.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                )
                            }
                            if (options.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "TOTAL PAID",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                            letterSpacing = 0.7.sp,
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it.filter { c ->
                                    c.isDigit() || c == '.' || c == ','
                                }.take(18)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(if (currency.fractionDigits == 0) "0" else "0.00")
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = amount.isNotBlank() && amountMinor == null,
                            supportingText = {
                                if (amount.isNotBlank() && amountMinor == null) {
                                    Text(
                                        "Enter a valid ${currency.isoCode} amount.",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "CURRENCY",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                            )
                            Text(
                                "${currency.displayName} (${currency.isoCode})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
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
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    val current = quantity.toIntOrNull() ?: 1
                                    quantity = (current - 1).coerceAtLeast(1).toString()
                                },
                                enabled = (quantity.toIntOrNull() ?: 1) > 1,
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
                                    val current = quantity.toIntOrNull() ?: 1
                                    quantity = (current + 1).coerceAtMost(10_000).toString()
                                },
                                enabled = (quantity.toIntOrNull() ?: 1) < 10_000,
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Increase quantity")
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(9.dp))
                    Text("Purchase date  ·  $dateLabel")
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showDetails = !showDetails },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 0.dp,
                            vertical = 2.dp,
                        ),
                    ) {
                        Text(
                            if (showDetails) "Hide optional details" else "Add details (optional)",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (showDetails) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it.take(60) },
                            label = { Text("Brand or product") },
                            placeholder = { Text("e.g. your usual brand") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it.take(80) },
                            label = { Text("Shop or location") },
                            placeholder = { Text("Where did you buy it?") },
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
                                onClick = { planned = true },
                                label = { Text("Planned") },
                            )
                            FilterChip(
                                selected = !planned,
                                onClick = { planned = false },
                                label = { Text("Impulse") },
                            )
                        }
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it.take(500) },
                            label = { Text("Note") },
                            placeholder = { Text("Anything you want to remember?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                    }
                }

                if (quantityValue == null || quantityValue !in 1..10_000) {
                    Text(
                        "Quantity must be between 1 and 10,000.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text(
                    "Your total is saved as the full amount paid for this purchase.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Button(
                    onClick = {
                        onSave(
                            Purchase(
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
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Save purchase", fontWeight = FontWeight.Bold)
                        Text(
                            amountMinor?.let { Money.format(it, currency, includeCode = true) }.orEmpty(),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    "Only the total price is needed. Everything else is optional.",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchasedAtMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val selectedDate = Instant.ofEpochMilli(utcMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        purchasedAtMillis = selectedDate.atTime(12, 0)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
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
    onDismiss: () -> Unit,
    onSave: (CravingEvent) -> Unit,
) {
    var outcome by remember { mutableStateOf(CravingOutcome.RESISTED) }
    var trigger by remember { mutableStateOf(CravingTrigger.STRESS) }
    var intensity by remember { mutableIntStateOf(3) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record a craving", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 540.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("What happened?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = outcome == CravingOutcome.RESISTED,
                        onClick = { outcome = CravingOutcome.RESISTED }, label = { Text("I resisted") })
                    FilterChip(selected = outcome == CravingOutcome.SMOKED,
                        onClick = { outcome = CravingOutcome.SMOKED }, label = { Text("I smoked") })
                }
                Text("What triggered it?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CravingTrigger.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { item ->
                                FilterChip(selected = trigger == item, onClick = { trigger = item },
                                    label = { Text(item.label, maxLines = 1) })
                            }
                        }
                    }
                }
                Text("Intensity: $intensity / 5", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(selected = intensity == value, onClick = { intensity = value },
                            label = { Text(value.toString()) })
                    }
                }
                OutlinedTextField(value = note, onValueChange = { note = it.take(500) },
                    label = { Text("What was happening? (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (outcome == CravingOutcome.SMOKED) {
                    Text("This will also add one cigarette to today's smoking log.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(CravingEvent(outcome = outcome, trigger = trigger, intensity = intensity, note = note))
            }) { Text("Save moment") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = if (highlight) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null,
                tint = if (highlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp))
            Text(eyebrow, style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.72f))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = content)
                Text(caption, style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.72f),
                    modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun PurchaseRow(purchase: Purchase, onDelete: () -> Unit) {
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
                "${purchase.quantity} × ${purchase.type.label.lowercase()} · ${DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(purchase.purchasedAtMillis))}",
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
        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete purchase",
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    OutlinedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
