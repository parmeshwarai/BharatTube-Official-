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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.data.model.AuditLogEntity
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    currentUser: UserProfileEntity?,
    settings: PlatformSettingsEntity,
    users: List<UserProfileEntity>,
    channels: List<ChannelEntity>,
    videos: List<VideoEntity>,
    payouts: List<PayoutEntity>,
    reports: List<ReportEntity>,
    auditLogs: List<AuditLogEntity>,
    onBack: () -> Unit,
    onOpenChhotuAdmin: () -> Unit,
    onUpdateSettings: (PlatformSettingsEntity, String, String, String, String) -> Unit,
    onModerateUser: (UserProfileEntity, Boolean, String) -> Unit,
    onResolveReport: (ReportEntity, String) -> Unit
) {
    if (currentUser?.isAdmin != true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Super Admin") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Access Restricted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Only authorized Super Admins can access platform controls and financial audit logs.")
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var userSearch by remember { mutableStateOf("") }
    var draftCreatorPct by remember(settings.creatorSharePct) { mutableIntStateOf(settings.creatorSharePct) }
    var draftThreshold by remember(settings.indiaPayoutThresholdInr) { mutableStateOf(settings.indiaPayoutThresholdInr.toInt().toString()) }

    val totalViews = videos.sumOf { it.viewCount }
    val totalNetRevenue = videos.sumOf { it.netRevenueInr }
    val totalCreatorShare = totalNetRevenue * (settings.creatorSharePct / 100.0)
    val totalPlatformShare = totalNetRevenue * (settings.platformSharePct / 100.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Control Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Analytics", "Controls", "Money", "Users & Reports").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, maxLines = 1) })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Platform Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Total Users: ${users.size} • Suspended: ${users.count { it.isBanned }}")
                                    Text("Total Creators / Channels: ${channels.size}")
                                    Text("Long Videos: ${videos.count { it.format == VideoFormat.LONG.name }} • Shorts: ${videos.count { it.format == VideoFormat.SHORT.name }}")
                                    Text("Total Views: ${BharatTubeCatalog.formatCompactCount(totalViews)}")
                                    Text("Net Ad Revenue: ₹${String.format("%.2f", totalNetRevenue)}")
                                    Text("Creator Share (${settings.creatorSharePct}%): ₹${String.format("%.2f", totalCreatorShare)}", color = EmeraldVerified)
                                    Text("BharatTube Share (${settings.platformSharePct}%): ₹${String.format("%.2f", totalPlatformShare)}", color = SaffronFlame)
                                    Text("Payout Requests: ${payouts.size} • Open Reports: ${reports.count { it.status == "OPEN" }}")
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = onOpenChhotuAdmin,
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        modifier = Modifier.fillMaxWidth().testTag("superadmin_open_chhotu_center")
                                    ) {
                                        Text("Open Chhotu AI Control Center & 17-Point Check")
                                    }
                                }
                            }
                        }
                        item {
                            Text("Immutable Admin Audit Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(auditLogs, key = { it.id }) { log ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(log.action, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SaffronFlame)
                                    Text(log.details, style = MaterialTheme.typography.bodySmall)
                                    Text("${log.previousValue} → ${log.newValue}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text("Global ON/OFF Controls (Preserves all user balances & data)", style = MaterialTheme.typography.bodySmall)
                        }
                        item {
                            AdminSwitchRow("Video Uploads", settings.videoUploadsEnabled) {
                                onUpdateSettings(settings.copy(videoUploadsEnabled = it), "TOGGLE_UPLOADS", "Video uploads toggled", "${settings.videoUploadsEnabled}", "$it")
                            }
                            AdminSwitchRow("Shorts & Reels", settings.shortsEnabled) {
                                onUpdateSettings(settings.copy(shortsEnabled = it), "TOGGLE_SHORTS", "Shorts toggled", "${settings.shortsEnabled}", "$it")
                            }
                            AdminSwitchRow("Live Streaming", settings.liveStreamingEnabled) {
                                onUpdateSettings(settings.copy(liveStreamingEnabled = it), "TOGGLE_LIVE", "Live streaming toggled", "${settings.liveStreamingEnabled}", "$it")
                            }
                            AdminSwitchRow("Comments", settings.commentsEnabled) {
                                onUpdateSettings(settings.copy(commentsEnabled = it), "TOGGLE_COMMENTS", "Comments toggled", "${settings.commentsEnabled}", "$it")
                            }
                            AdminSwitchRow("Likes & Subscriptions", settings.likesEnabled && settings.subscriptionsEnabled) {
                                onUpdateSettings(
                                    settings.copy(likesEnabled = it, subscriptionsEnabled = it),
                                    "TOGGLE_ENGAGEMENT",
                                    "Likes & Subscriptions toggled",
                                    "${settings.likesEnabled}",
                                    "$it"
                                )
                            }
                            AdminSwitchRow("Direct & Group Chat", settings.chatEnabled) {
                                onUpdateSettings(settings.copy(chatEnabled = it), "TOGGLE_CHAT", "Chat toggled", "${settings.chatEnabled}", "$it")
                            }
                            AdminSwitchRow("Advertisements Global Switch", settings.advertisementsEnabled) {
                                onUpdateSettings(settings.copy(advertisementsEnabled = it), "TOGGLE_ADS", "Global ads toggled", "${settings.advertisementsEnabled}", "$it")
                            }
                            AdminSwitchRow("Pre-Roll Ads", settings.preRollAdsEnabled) {
                                onUpdateSettings(settings.copy(preRollAdsEnabled = it), "TOGGLE_PREROLL", "Pre-roll ads toggled", "${settings.preRollAdsEnabled}", "$it")
                            }
                            AdminSwitchRow("Mid-Roll Ads", settings.midRollAdsEnabled) {
                                onUpdateSettings(settings.copy(midRollAdsEnabled = it), "TOGGLE_MIDROLL", "Mid-roll ads toggled", "${settings.midRollAdsEnabled}", "$it")
                            }
                            AdminSwitchRow("Meta Audience Network Adapter", settings.metaAudienceNetworkEnabled) {
                                onUpdateSettings(settings.copy(metaAudienceNetworkEnabled = it), "TOGGLE_META_SDK", "Meta Audience Network adapter toggled", "${settings.metaAudienceNetworkEnabled}", "$it")
                            }
                            AdminSwitchRow("Meta Test Mode", settings.metaTestMode) {
                                onUpdateSettings(settings.copy(metaTestMode = it), "TOGGLE_META_TEST", "Meta test mode toggled", "${settings.metaTestMode}", "$it")
                            }
                        }
                    }

                    2 -> {
                        item {
                            val platformPct = 100 - draftCreatorPct
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Revenue Share Rule (Must Total 100%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Creator: $draftCreatorPct%  |  BharatTube: $platformPct%",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SaffronFlame
                                    )
                                    Slider(
                                        value = draftCreatorPct.toFloat(),
                                        onValueChange = { draftCreatorPct = it.roundToInt() },
                                        valueRange = 10f..90f
                                    )
                                    OutlinedTextField(
                                        value = draftThreshold,
                                        onValueChange = { draftThreshold = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("India Minimum Payout (INR ₹)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            val newThresh = draftThreshold.toDoubleOrNull()?.coerceAtLeast(100.0) ?: 1000.0
                                            onUpdateSettings(
                                                settings.copy(
                                                    creatorSharePct = draftCreatorPct,
                                                    platformSharePct = platformPct,
                                                    indiaPayoutThresholdInr = newThresh
                                                ),
                                                "UPDATE_MONETIZATION_SPLIT",
                                                "Updated global revenue share & India payout threshold",
                                                "${settings.creatorSharePct}:${settings.platformSharePct} | ₹${settings.indiaPayoutThresholdInr.toInt()}",
                                                "$draftCreatorPct:$platformPct | ₹${newThresh.toInt()}"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        modifier = Modifier.fillMaxWidth().testTag("save_revenue_split_button")
                                    ) {
                                        Text("Save & Log Audit Entry")
                                    }
                                    AdminSwitchRow("Creator Monetization Enabled", settings.creatorMonetizationEnabled) {
                                        onUpdateSettings(settings.copy(creatorMonetizationEnabled = it), "TOGGLE_MONETIZATION", "Creator monetization toggled", "${settings.creatorMonetizationEnabled}", "$it")
                                    }
                                    AdminSwitchRow("Automatic Payout Enabled", settings.automaticPayoutEnabled) {
                                        onUpdateSettings(settings.copy(automaticPayoutEnabled = it), "TOGGLE_AUTOPAYOUT", "Auto payout toggled", "${settings.automaticPayoutEnabled}", "$it")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        item {
                            OutlinedTextField(
                                value = userSearch,
                                onValueChange = { userSearch = it },
                                label = { Text("Search users by name or handle") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        items(
                            users.filter {
                                userSearch.isBlank() ||
                                    it.username.contains(userSearch, ignoreCase = true) ||
                                    it.handle.contains(userSearch, ignoreCase = true)
                            },
                            key = { it.uid }
                        ) { target ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${target.username} (${target.handle})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            if (target.isBanned) "SUSPENDED: ${target.banReason}" else "Active • ${target.countryCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (target.isBanned) LotusCoral else EmeraldVerified
                                        )
                                    }
                                    if (!target.isAdmin) {
                                        OutlinedButton(onClick = {
                                            onModerateUser(target, !target.isBanned, if (target.isBanned) "Restored by Admin" else "Policy review suspension")
                                        }) {
                                            Text(if (target.isBanned) "Restore" else "Ban")
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("Copyright & Safety Reports (${reports.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(reports, key = { it.id }) { report ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${report.reason} — ${report.targetTitle}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Status: ${report.status} • ${report.details}", style = MaterialTheme.typography.bodySmall)
                                    if (report.status == "OPEN") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { onResolveReport(report, "ACTION_TAKEN") }) { Text("Enforce") }
                                            OutlinedButton(onClick = { onResolveReport(report, "DISMISSED") }) { Text("Dismiss") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
