package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chhotuai.CheckStatus
import com.example.chhotuai.ChhotuPendingActionType
import com.example.chhotuai.ChhotuPendingAdminAction
import com.example.chhotuai.SystemHealthSnapshot
import com.example.data.model.AuditLogEntity
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.ChhotuErrorReportEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.RoyalIndigo
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChhotuAdminCenterScreen(
    currentUser: UserProfileEntity?,
    settings: PlatformSettingsEntity,
    users: List<UserProfileEntity>,
    channels: List<ChannelEntity>,
    videos: List<VideoEntity>,
    payouts: List<PayoutEntity>,
    reports: List<ReportEntity>,
    auditLogs: List<AuditLogEntity>,
    chhotuErrorReports: List<ChhotuErrorReportEntity>,
    healthSnapshot: SystemHealthSnapshot?,
    isChecking: Boolean,
    onBack: () -> Unit,
    onRunSystemCheck: () -> Unit,
    onResolveErrorReport: (ChhotuErrorReportEntity) -> Unit,
    onExecuteConfirmedAction: (ChhotuPendingAdminAction) -> Unit
) {
    if (currentUser?.isAdmin != true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chhotu AI Control Center") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Admin Authorization Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Only authorized BharatTube Admins can access Chhotu AI System Health, Error Reports, Revenue Reports, and Security Controls.")
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingAction by remember { mutableStateOf<ChhotuPendingAdminAction?>(null) }

    val activeUsersCount = users.count { !it.isBanned }
    val totalViews = videos.sumOf { it.viewCount }
    val totalNetRevenue = videos.sumOf { it.netRevenueInr }
    val securityAlertsCount = auditLogs.count { it.action == "CHHOTU_SECURITY_BLOCK" } + reports.count { it.status == "OPEN" }
    val openErrorReportsCount = chhotuErrorReports.count { !it.isResolved }
    val admobCheck = healthSnapshot?.items?.firstOrNull { it.moduleName == "AdMob Integration" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chhotu AI Control Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRunSystemCheck, enabled = !isChecking) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Run 17-Point Check")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("AI Control", "17-Point Check", "Admin AI").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, maxLines = 1) })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    // Tab 0: AI Control Center
                    0 -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Filled.SmartToy, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Chhotu AI Status: ${if (settings.chhotuAiEnabled) "ONLINE" else "OFFLINE"}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (settings.chhotuAiEnabled) EmeraldVerified else LotusCoral
                                            )
                                            Text(
                                                "Requires Admin confirmation before toggling",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = settings.chhotuAiEnabled,
                                            onCheckedChange = { next ->
                                                pendingAction = ChhotuPendingAdminAction(
                                                    type = ChhotuPendingActionType.TOGGLE_CHHOTU_AI,
                                                    title = if (next) "Enable Chhotu AI?" else "Pause Chhotu AI?",
                                                    description = "Confirm changing BharatTube Official Chhotu AI availability to ${if (next) "ONLINE" else "OFFLINE"}. This action is recorded in Audit Logs.",
                                                    boolValue = next
                                                )
                                            },
                                            modifier = Modifier.testTag("chhotu_admin_enable_switch")
                                        )
                                    }

                                    val backend = healthSnapshot?.backendState
                                    val lastCheckStr = healthSnapshot?.checkedAt?.let {
                                        SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(it))
                                    } ?: "Running..."
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = (if (backend?.isOnline == true) EmeraldVerified else AmberGold).copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                "Backend Connection: ${backend?.statusLabel ?: "Checking..."}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (backend?.isOnline == true) EmeraldVerified else AmberGold
                                            )
                                            Text("Endpoint: ${backend?.endpointMasked ?: "Not Configured"}", style = MaterialTheme.typography.bodySmall)
                                            if (backend?.latencyMs != null || backend?.httpCode != null) {
                                                Text(
                                                    "HTTP: ${backend.httpCode ?: "-"} • Latency: ${backend.latencyMs ?: "-"} ms",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Text("Last System Check: $lastCheckStr", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    Button(
                                        onClick = onRunSystemCheck,
                                        enabled = !isChecking,
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        modifier = Modifier.fillMaxWidth().testTag("chhotu_run_check_button")
                                    ) {
                                        if (isChecking) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(if (isChecking) "Scanning 17 Modules..." else "Run Complete 17-Point System Check")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "Automatic Error & Connectivity Reports ($openErrorReportsCount Open)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (chhotuErrorReports.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldVerified.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "No error reports logged yet.",
                                        color = EmeraldVerified,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(chhotuErrorReports, key = { it.id }) { rep ->
                                val badgeColor = when {
                                    rep.isResolved -> EmeraldVerified
                                    rep.severity == CheckStatus.ERROR.name -> LotusCoral
                                    else -> AmberGold
                                }
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(rep.moduleName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Surface(color = badgeColor.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    text = if (rep.isResolved) "RESOLVED" else rep.severity,
                                                    color = badgeColor,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Text("Cause: ${rep.cause}", style = MaterialTheme.typography.bodySmall)
                                        Text("Suggested Fix: ${rep.solution}", style = MaterialTheme.typography.bodySmall, color = RoyalIndigo)
                                        if (!rep.isResolved) {
                                            OutlinedButton(
                                                onClick = { onResolveErrorReport(rep) },
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text("Mark Reviewed & Log")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("AI & Security Audit Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(auditLogs.take(20), key = { it.id }) { log ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(log.action, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SaffronFlame)
                                    Text(log.details, style = MaterialTheme.typography.bodySmall)
                                    Text("${log.previousValue} → ${log.newValue}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Tab 1: Complete App Checking (17 Modules)
                    1 -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatusCountBadge("Connected", healthSnapshot?.connectedCount ?: 0, EmeraldVerified)
                                    StatusCountBadge("Attention", healthSnapshot?.degradedCount ?: 0, AmberGold)
                                    StatusCountBadge("Not Conn.", healthSnapshot?.notConnectedCount ?: 0, RoyalIndigo)
                                    StatusCountBadge("Error", healthSnapshot?.errorCount ?: 0, LotusCoral)
                                }
                            }
                        }

                        val checkItems = healthSnapshot?.items.orEmpty()
                        items(checkItems, key = { it.moduleName }) { item ->
                            val (color, icon) = when (item.status) {
                                CheckStatus.CONNECTED -> EmeraldVerified to Icons.Filled.CheckCircle
                                CheckStatus.DEGRADED -> AmberGold to Icons.Filled.WarningAmber
                                CheckStatus.NOT_CONNECTED -> RoyalIndigo to Icons.Filled.CloudOff
                                CheckStatus.ERROR -> LotusCoral to Icons.Filled.ErrorOutline
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Icon(icon, contentDescription = item.status.label, tint = color, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("${item.moduleName} (${item.category})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                item.status.label,
                                                color = color,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text(item.detail, style = MaterialTheme.typography.bodySmall)
                                    if (item.cause.isNotBlank()) {
                                        Text("Cause: ${item.cause}", style = MaterialTheme.typography.bodySmall, color = color)
                                    }
                                    if (item.solution.isNotBlank()) {
                                        Text("Solution: ${item.solution}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Tab 2: Admin AI Dashboard
                    else -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Admin AI Platform Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("• Total Users: ${users.size} (Active: $activeUsersCount, Suspended: ${users.count { it.isBanned }})")
                                    Text("• Total Creators / Channels: ${channels.size}")
                                    Text("• Total Videos: ${videos.size}")
                                    Text("• Total Views: ${BharatTubeCatalog.formatCompactCount(totalViews)}")
                                    Text("• AdMob Status: ${admobCheck?.status?.label ?: "Checking"} — ${admobCheck?.detail ?: ""}")
                                    Text("• Revenue Reports: Net ₹${String.format("%.2f", totalNetRevenue)} | Payout Requests: ${payouts.size}")
                                    Text("• Server Status: ${healthSnapshot?.backendState?.statusLabel ?: "Not Connected"}")
                                    Text("• Open Error Reports: $openErrorReportsCount")
                                    Text("• Security Alerts: $securityAlertsCount", color = if (securityAlertsCount > 0) AmberGold else EmeraldVerified)
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Security, contentDescription = null, tint = EmeraldVerified)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Chhotu AI Security & Confirmation Guard", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        "• Never accesses or exposes Passwords, OTPs, or cross-user private records.\n" +
                                            "• Auto-ban and auto-payout modifications are blocked unless explicitly confirmed by an Admin.\n" +
                                            "• Every Admin action records an entry in Audit Logs.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Automatic Payouts Enabled", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("Requires Admin confirmation", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Switch(
                                            checked = settings.automaticPayoutEnabled,
                                            onCheckedChange = { next ->
                                                pendingAction = ChhotuPendingAdminAction(
                                                    type = ChhotuPendingActionType.TRIGGER_ADMIN_PAYOUT_REVIEW,
                                                    title = "Confirm Payout Switch Change",
                                                    description = "Set Automatic Payouts to ${if (next) "ENABLED" else "PAUSED"}? Existing creator balances remain preserved.",
                                                    boolValue = next
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("Admin-Confirmed Account Moderation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(users.filter { !it.isAdmin }, key = { it.uid }) { target ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${target.username} (${target.handle})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            if (target.isBanned) "SUSPENDED" else "Active • ${target.countryCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (target.isBanned) LotusCoral else EmeraldVerified
                                        )
                                    }
                                    OutlinedButton(onClick = {
                                        val nextBan = !target.isBanned
                                        pendingAction = ChhotuPendingAdminAction(
                                            type = ChhotuPendingActionType.BAN_OR_RESTORE_USER,
                                            title = if (nextBan) "Confirm Account Suspension" else "Confirm Account Restore",
                                            description = "${if (nextBan) "Suspend" else "Restore"} ${target.username} (${target.handle})? Chhotu AI never performs auto-bans without Admin confirmation.",
                                            targetId = target.uid,
                                            boolValue = nextBan
                                        )
                                    }) {
                                        Text(if (target.isBanned) "Restore" else "Ban")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(action.title) },
            text = { Text(action.description) },
            confirmButton = {
                Button(
                    onClick = {
                        onExecuteConfirmedAction(action)
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                ) {
                    Text("Confirm & Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusCountBadge(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
