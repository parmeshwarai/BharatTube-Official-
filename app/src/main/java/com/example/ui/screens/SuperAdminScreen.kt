package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLogEntity
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutStatus
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.OwnerCrownControlIcon
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

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
    onWarnUser: (UserProfileEntity, String) -> Unit,
    onBlockUser: (UserProfileEntity, Boolean, String) -> Unit,
    onLimitUser: (UserProfileEntity, Boolean, String) -> Unit,
    onBoostUser: (UserProfileEntity, Int) -> Unit,
    onTriggerAutoPayoutSweep: () -> Unit,
    onUpdatePayoutStatus: (PayoutEntity, PayoutStatus) -> Unit,
    onResolveReport: (ReportEntity, String) -> Unit
) {
    if (currentUser?.isAdmin != true) {
        Scaffold(
            containerColor = Color(0xFF0E1016),
            topBar = {
                TopAppBar(
                    title = { Text("Owner Control Center", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = LotusCoral, modifier = Modifier.size(40.dp))
                Text("Owner Only Access", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Owner Control Center is hidden and locked for all normal user accounts.",
                    color = Color(0xFFA0A5B8)
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var userSearch by remember { mutableStateOf("") }

    val totalViews = remember(videos) { videos.sumOf { it.viewCount } }
    val totalNetRevenue = remember(videos) { videos.sumOf { it.netRevenueInr } }
    val usersById = remember(users) { users.associateBy { it.uid } }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Owner Control Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("owner_control_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenChhotuAdmin,
                        modifier = Modifier.testTag("superadmin_open_chhotu_center")
                    ) {
                        Icon(Icons.Filled.SmartToy, contentDescription = "Chhotu AI Admin", tint = SaffronFlame)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // OWNER ONLY • ADMIN ACCESS Banner (Ref WA9985 / WA0542)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF171A25),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OwnerCrownControlIcon(modifier = Modifier.size(20.dp), tint = AmberGold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "OWNER ONLY • ADMIN ACCESS",
                        color = AmberGold,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                }
            }

            // 5 Focused Tabs
            val tabTitles = listOf("Control", "Paisa", "Work & Boost", "Users", "Payouts")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131620),
                contentColor = Color.White,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SaffronFlame,
                            height = 2.5.dp
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) SaffronFlame else Color(0xFFA0A5B8),
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("owner_tab_${index}")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    // TAB 0: MASTER CONTROL CENTER (Ref WA9985 & WA9133)
                    0 -> {
                        item {
                            // Brand Control + Revenue Split Pill (strictly Owner Control Center ONLY)
                            OwnerControlCard(borderColor = SaffronFlame.copy(alpha = 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = SaffronFlame)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Brand Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                                    androidx.compose.material3.Switch(
                                        checked = settings.brandControlEnabled,
                                        onCheckedChange = {
                                            onUpdateSettings(settings.copy(brandControlEnabled = it), "BRAND_CONTROL", "Toggled Brand Control", "${settings.brandControlEnabled}", "$it")
                                        }
                                    )
                                }
                                Text("Revenue Split Control (Visible only in Owner Control Center)", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0E1016), RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SaffronFlame,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "BharatTube ${settings.platformSharePct}%",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF1D2130),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Creator ${settings.creatorSharePct}%",
                                            color = EmeraldVerified,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.PowerSettingsNew,
                                iconTint = if (settings.appEnabled) EmeraldVerified else LotusCoral,
                                title = "App On / Off",
                                subtitle = if (settings.appEnabled) "BharatTube app is LIVE for all users" else "BharatTube is OFF for normal users",
                                checked = settings.appEnabled,
                                testTag = "control_app_on_off",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(appEnabled = it), "APP_ON_OFF", "Master App Switch", "${settings.appEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.Build,
                                iconTint = AmberGold,
                                title = "Maintenance Mode",
                                subtitle = "Show maintenance screen to non-owner users",
                                checked = settings.maintenanceMode,
                                testTag = "control_maintenance_mode",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(maintenanceMode = it), "MAINTENANCE_MODE", "Maintenance Mode toggled", "${settings.maintenanceMode}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.Home,
                                iconTint = ElectricCobalt,
                                title = "Home Page Control",
                                subtitle = "Creator spotlight & Home feed personalization",
                                checked = settings.homePageControlEnabled,
                                testTag = "control_home_page",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(homePageControlEnabled = it), "HOME_CONTROL", "Home Page Control toggled", "${settings.homePageControlEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.CloudUpload,
                                iconTint = SaffronFlame,
                                title = "Upload Control (Videos)",
                                subtitle = "Moderate & allow creator video uploads",
                                checked = settings.videoUploadsEnabled,
                                testTag = "control_upload",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(videoUploadsEnabled = it), "UPLOAD_CONTROL", "Video Uploads toggled", "${settings.videoUploadsEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.ManageAccounts,
                                iconTint = ElectricCobalt,
                                title = "Profile Control",
                                subtitle = "Allow users to edit channel banner, logo & details",
                                checked = settings.profileControlEnabled,
                                testTag = "control_profile",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(profileControlEnabled = it), "PROFILE_CONTROL", "Profile editing toggled", "${settings.profileControlEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerControlCard(borderColor = SaffronFlame.copy(alpha = 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = SaffronFlame)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Chhotu AI Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            text = if (settings.chhotuAiEnabled) {
                                                "Pre-publish video check ON (${if (settings.chhotuAiStrictMode) "Strict Mode" else "Normal Mode"})"
                                            } else {
                                                "Chhotu AI Pre-Check is OFF"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA0A5B8)
                                        )
                                    }
                                    androidx.compose.material3.Switch(
                                        checked = settings.chhotuAiEnabled,
                                        onCheckedChange = {
                                            onUpdateSettings(settings.copy(chhotuAiEnabled = it), "CHHOTU_AI_CONTROL", "Chhotu AI toggled", "${settings.chhotuAiEnabled}", "$it")
                                        },
                                        modifier = Modifier.testTag("control_chhotu_ai")
                                    )
                                }
                                Text("Moderation Mode (Owner Control)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    val isNormal = !settings.chhotuAiStrictMode
                                    OutlinedButton(
                                        onClick = {
                                            onUpdateSettings(settings.copy(chhotuAiStrictMode = false), "CHHOTU_AI_MODE", "Set Chhotu AI to Normal Mode", "${settings.chhotuAiStrictMode}", "false")
                                        },
                                        border = BorderStroke(1.dp, if (isNormal) EmeraldVerified else Color(0xFF2D3244)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isNormal) EmeraldVerified.copy(alpha = 0.16f) else Color.Transparent
                                        ),
                                        modifier = Modifier.weight(1f).testTag("chhotu_mode_normal")
                                    ) {
                                        Text("Normal Mode", color = if (isNormal) EmeraldVerified else Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onUpdateSettings(settings.copy(chhotuAiStrictMode = true), "CHHOTU_AI_MODE", "Set Chhotu AI to Strict Mode", "${settings.chhotuAiStrictMode}", "true")
                                        },
                                        border = BorderStroke(1.dp, if (!isNormal) SaffronFlame else Color(0xFF2D3244)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (!isNormal) SaffronFlame.copy(alpha = 0.18f) else Color.Transparent
                                        ),
                                        modifier = Modifier.weight(1f).testTag("chhotu_mode_strict")
                                    ) {
                                        Text("Strict Mode", color = if (!isNormal) SaffronFlame else Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                val checkingCount = videos.count { it.chhotuCheckStatus == "CHECKING" }
                                val rejectedCount = videos.count { it.chhotuCheckStatus == "REJECTED" }
                                Text(
                                    text = "Upload Queue: $checkingCount Checking • $rejectedCount Rejected • ${videos.count { it.chhotuCheckStatus == "APPROVED" }} Approved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.AttachMoney,
                                iconTint = EmeraldVerified,
                                title = "Monetization Control",
                                subtitle = "Creator earnings & automatic payout system",
                                checked = settings.creatorMonetizationEnabled,
                                testTag = "control_monetization",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(creatorMonetizationEnabled = it), "MONETIZATION_CONTROL", "Creator monetization toggled", "${settings.creatorMonetizationEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.Campaign,
                                iconTint = AmberGold,
                                title = "Ads Control",
                                subtitle = "Global AdMob Banner, Interstitial & Video ads",
                                checked = settings.advertisementsEnabled,
                                testTag = "control_ads",
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(advertisementsEnabled = it), "ADS_CONTROL", "Global ads toggled", "${settings.advertisementsEnabled}", "$it")
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.Security,
                                iconTint = EmeraldVerified,
                                title = "Privacy Control",
                                subtitle = "Comments, likes, subscriptions & chat controls",
                                checked = settings.privacyControlEnabled,
                                testTag = "control_privacy",
                                onCheckedChange = {
                                    onUpdateSettings(
                                        settings.copy(
                                            privacyControlEnabled = it,
                                            commentsEnabled = it,
                                            chatEnabled = it
                                        ),
                                        "PRIVACY_CONTROL",
                                        "Privacy, comments & chat toggled",
                                        "${settings.privacyControlEnabled}",
                                        "$it"
                                    )
                                }
                            )
                        }
                        item {
                            OwnerToggleRow(
                                icon = Icons.Filled.LiveTv,
                                iconTint = LotusCoral,
                                title = "Live & Shorts Control",
                                subtitle = "Allow Live Streaming and Shorts feed",
                                checked = settings.liveStreamingEnabled && settings.shortsEnabled,
                                testTag = "control_live_shorts",
                                onCheckedChange = {
                                    onUpdateSettings(
                                        settings.copy(liveStreamingEnabled = it, shortsEnabled = it),
                                        "LIVE_SHORTS_CONTROL",
                                        "Live & Shorts toggled",
                                        "${settings.liveStreamingEnabled}",
                                        "$it"
                                    )
                                }
                            )
                        }
                        item {
                            Button(
                                onClick = onOpenChhotuAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Open Chhotu AI Diagnostics & 17-Point Check", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        item {
                            OwnerControlCard {
                                Text("Platform Overview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    "Users: ${users.size} • Channels: ${channels.size} • Videos: ${videos.size} • Views: ${BharatTubeCatalog.formatCompactCount(totalViews)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA0A5B8)
                                )
                                Text(
                                    "Ad Pool Net: ₹${String.format("%,.2f", totalNetRevenue)} • Payouts Logged: ${payouts.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldVerified
                                )
                            }
                        }
                    }

                    // TAB 1: PAISA / MONEY CONTROL (Ref WA0542 & WA9906)
                    1 -> {
                        item {
                            OwnerPaisaControlSection(
                                settings = settings,
                                onUpdateSettings = onUpdateSettings
                            )
                        }
                    }

                    // TAB 2: WORK & BOOST CONTROL (Req #4)
                    2 -> {
                        item {
                            OwnerWorkAndBoostSection(
                                settings = settings,
                                onUpdateSettings = onUpdateSettings
                            )
                        }
                    }

                    // TAB 3: USER CONTROL (Req #5)
                    3 -> {
                        item {
                            OutlinedTextField(
                                value = userSearch,
                                onValueChange = { userSearch = it },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SaffronFlame) },
                                placeholder = { Text("Search user by Name or @Handle...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("owner_user_search_input")
                            )
                        }
                        val filteredUsers = users.filter {
                            userSearch.isBlank() ||
                                it.username.contains(userSearch, ignoreCase = true) ||
                                it.handle.contains(userSearch, ignoreCase = true)
                        }
                        items(filteredUsers, key = { it.uid }) { target ->
                            OwnerUserControlCard(
                                target = target,
                                channels = channels,
                                videos = videos,
                                settings = settings,
                                onBanToggle = { ban ->
                                    onModerateUser(target, ban, if (ban) "Banned via Owner Control Center" else "Unbanned by Owner")
                                },
                                onWarn = { reason -> onWarnUser(target, reason) },
                                onBlockToggle = { block ->
                                    onBlockUser(target, block, if (block) "Comments & Chat blocked by Owner" else "Unblocked by Owner")
                                },
                                onLimitToggle = { limit ->
                                    onLimitUser(target, limit, if (limit) "Upload/Live limit restricted by Owner" else "Work limit restored by Owner")
                                },
                                onBoostChange = { pct -> onBoostUser(target, pct) }
                            )
                        }
                    }

                    // TAB 4: AUTOMATIC PAYOUT (₹2,000), PAYOUT HISTORY, REPORTS & AUDIT LOGS (Req #6)
                    else -> {
                        item {
                            OwnerControlCard(borderColor = EmeraldVerified.copy(alpha = 0.45f)) {
                                Text(
                                    "Automatic Payout Engine — Minimum ₹${settings.indiaPayoutThresholdInr.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Auto Payout: ${if (settings.automaticPayoutEnabled) "ON" else "OFF"} • Methods: UPI / Bank Account\nWhenever a creator balance reaches ₹${settings.indiaPayoutThresholdInr.toInt()}, payout triggers automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB5BACB)
                                )
                                Button(
                                    onClick = onTriggerAutoPayoutSweep,
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("owner_run_autopayout_button")
                                ) {
                                    Text("Run Auto-Payout Check Now (≥ ₹${settings.indiaPayoutThresholdInr.toInt()})", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        item {
                            Text(
                                "Payout History (${payouts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (payouts.isEmpty()) {
                            item {
                                OwnerControlCard {
                                    Text("No payouts recorded yet.", color = Color(0xFFA0A5B8), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            items(payouts, key = { it.id }) { payout ->
                                val owner = usersById[payout.userId]
                                OwnerPayoutHistoryCard(
                                    payout = payout,
                                    ownerName = owner?.let { "${it.username} (${it.handle})" } ?: payout.channelId,
                                    onTogglePaid = { nextStatus -> onUpdatePayoutStatus(payout, nextStatus) }
                                )
                            }
                        }
                        if (reports.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                Text("Safety & Copyright Reports (${reports.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            items(reports, key = { it.id }) { report ->
                                OwnerControlCard {
                                    Text("${report.reason} — ${report.targetTitle}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Status: ${report.status} • ${report.details}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
                                    if (report.status == "OPEN") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { onResolveReport(report, "ACTION_TAKEN") }) { Text("Enforce") }
                                            OutlinedButton(onClick = { onResolveReport(report, "DISMISSED") }) { Text("Dismiss", color = Color.White) }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Owner Audit Log (${auditLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        items(auditLogs.take(15), key = { it.id }) { log ->
                            OwnerControlCard {
                                Text(log.action, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SaffronFlame)
                                Text(log.details, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Text("${log.previousValue} → ${log.newValue}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6))
                            }
                        }
                    }
                }
            }
        }
    }
}
