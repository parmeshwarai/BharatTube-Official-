package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalyticsTimeframe
import com.example.data.model.BharatTubeAnalyticsEngine
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.CommentEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutMethod
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.components.VerifiedChannelBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class StudioNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard),
    VIDEOS("Videos", Icons.Filled.PlayCircle),
    ANALYTICS("Analytics", Icons.Filled.BarChart),
    EARN_COMMENTS("Earn", Icons.Filled.AccountBalanceWallet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioScreen(
    user: UserProfileEntity?,
    myChannels: List<ChannelEntity>,
    allChannels: List<ChannelEntity>,
    allVideos: List<VideoEntity>,
    allComments: List<CommentEntity> = emptyList(),
    payouts: List<PayoutEntity>,
    settings: PlatformSettingsEntity,
    countryCode: String,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVideoAnalytics: (VideoEntity) -> Unit,
    onOpenChhotuCheck: (VideoEntity) -> Unit,
    onInitiatePayout: (ChannelEntity, Double, PayoutMethod) -> Unit,
    onSavePayoutMethod: (PayoutMethod, String, String) -> Unit,
    onUploadVideo: () -> Unit = {},
    onCreateShort: () -> Unit = {},
    onGoLive: () -> Unit = {},
    onUpdateVideo: (String, String, String, VideoVisibility, String, String?, String?, Long?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteVideo: (VideoEntity) -> Unit = {},
    onAddCommentReply: (String, String, String?) -> Unit = { _, _, _ -> },
    onPinComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> },
    onHideComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> },
    onDeleteComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> }
) {
    val activeChannel = myChannels.firstOrNull() ?: allChannels.firstOrNull()
    val myChannelIds = remember(myChannels, activeChannel) {
        if (myChannels.isNotEmpty()) myChannels.map { it.id }.toSet()
        else listOfNotNull(activeChannel?.id).toSet()
    }
    val channelVideos = remember(allVideos, myChannelIds) {
        allVideos.filter { it.channelId in myChannelIds }.sortedByDescending { it.createdAt }
    }
    val videoMap = remember(channelVideos) { channelVideos.associateBy { it.id } }
    val channelComments = remember(allComments, videoMap) {
        allComments.filter { it.videoId in videoMap }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Dashboard, 1=Videos, 2=Analytics, 3=Earn/Comments
    var earnCommentsSubTab by remember { mutableIntStateOf(0) } // 0=Earn, 1=Comments
    var commentFilterVideoId by remember { mutableStateOf<String?>(null) }
    var videoListFilter by remember { mutableStateOf("ALL") }
    var videoSearchQuery by remember { mutableStateOf("") }
    var timeframe by remember { mutableStateOf(AnalyticsTimeframe.LAST_7_DAYS) }

    var showUploadSheet by remember { mutableStateOf(false) }
    var editingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var deletingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var replyingComment by remember { mutableStateOf<CommentEntity?>(null) }
    var replyText by remember { mutableStateOf("") }

    val overview = remember(channelVideos, timeframe, settings, user?.boostPercent) {
        BharatTubeAnalyticsEngine.buildStudioOverview(
            videos = channelVideos,
            timeframe = timeframe,
            settings = settings,
            userBoostPct = user?.boostPercent ?: 0
        )
    }
    val aggregateSnap = remember(channelVideos, timeframe, settings, user?.boostPercent) {
        val anchor = channelVideos.maxByOrNull { it.viewCount }
        anchor?.let {
            BharatTubeAnalyticsEngine.buildVideoAnalytics(
                video = it,
                commentCount = channelComments.size,
                timeframe = timeframe,
                settings = settings,
                userBoostPct = user?.boostPercent ?: 0
            )
        }
    }

    val totalViews = channelVideos.sumOf { it.viewCount }
    val totalNetInr = channelVideos.sumOf { it.netRevenueInr }
    val creatorTotalInr = BharatTubeCatalog.calculateCreatorEarningsInr(
        viewCount = totalViews,
        netRevenueInr = totalNetInr,
        creatorSharePct = settings.creatorSharePct,
        per1000ViewsPaymentInr = settings.per1000ViewsPaymentInr,
        bonusPercent = settings.bonusPercent,
        userBoostPct = user?.boostPercent ?: 0
    )
    val channelPayouts = remember(payouts, myChannelIds) {
        payouts.filter { it.channelId in myChannelIds }
    }
    val confirmedPaidInr = channelPayouts.filter { it.status != "FAILED" }.sumOf { it.amountInr }
    val availableBalanceInr = (creatorTotalInr - confirmedPaidInr).coerceAtLeast(0.0)

    var selectedMethod by remember(user?.preferredPayoutMethod) {
        mutableStateOf(runCatching { PayoutMethod.valueOf(user?.preferredPayoutMethod ?: "UPI") }.getOrDefault(PayoutMethod.UPI))
    }
    var upiDraft by remember(user?.upiId) { mutableStateOf(user?.upiId ?: "") }
    var bankDraft by remember(user?.bankAccountMasked) { mutableStateOf(user?.bankAccountMasked ?: "") }

    val approvedCount = channelVideos.count { it.chhotuCheckStatus == "APPROVED" }
    val checkingCount = channelVideos.count { it.chhotuCheckStatus != "APPROVED" && it.chhotuCheckStatus != "REJECTED" }
    val rejectedCount = channelVideos.count { it.chhotuCheckStatus == "REJECTED" }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "BharatTube Studio",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("studio_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { showUploadSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).padding(end = 4.dp).testTag("studio_upload_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Upload", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Upload", color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        },
        bottomBar = {
            Surface(color = Color(0xFF0E1018), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StudioNavTab.entries.forEachIndexed { idx, tab ->
                            val selected = selectedTab == idx
                            val tint = if (selected) SaffronFlame else Color(0xFFA0A5B8)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable { selectedTab = idx }
                                    .testTag("studio_tab_$idx"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (idx == 3 && earnCommentsSubTab == 1) "Comments" else tab.label,
                                    color = tint,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // DASHBOARD TAB
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                ChannelLogoAvatar(
                                    avatarUri = activeChannel?.avatarUri ?: user?.avatarUri.orEmpty(),
                                    contentDescription = activeChannel?.name ?: "Creator",
                                    size = 46.dp,
                                    borderWidth = 2.dp,
                                    borderColor = SaffronFlame
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Hello, ${user?.username ?: activeChannel?.name ?: "Creator"}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (activeChannel?.isVerified == true) {
                                            Spacer(Modifier.width(5.dp))
                                            VerifiedChannelBadge(isVerified = true, size = 16.dp)
                                        }
                                    }
                                    Text(
                                        text = "${activeChannel?.name ?: "No Channel"} • ${activeChannel?.handle ?: "@creator"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AnalyticsTimeframe.entries) { option ->
                                FilterChip(
                                    selected = timeframe == option,
                                    onClick = { timeframe = option },
                                    label = { Text(option.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronFlame,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1A1E2B),
                                        labelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("studio_filter_${option.name}")
                                )
                            }
                        }
                    }

                    // 2x2 Dashboard KPI Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                StudioKpiTile(
                                    icon = Icons.Filled.Visibility,
                                    label = "Total Views",
                                    value = BharatTubeCatalog.formatCompactCount(overview.totalViews),
                                    deltaText = "+${overview.viewsChangePct}% vs prev",
                                    accentColor = SaffronFlame,
                                    modifier = Modifier.weight(1f)
                                )
                                StudioKpiTile(
                                    icon = Icons.Filled.Group,
                                    label = "Total Subscribers",
                                    value = BharatTubeCatalog.formatCompactCount(activeChannel?.subscriberCount ?: 0L),
                                    deltaText = "+${channelVideos.sumOf { it.subscribersGained }} gained",
                                    accentColor = ElectricCobalt,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                StudioKpiTile(
                                    icon = Icons.Filled.Schedule,
                                    label = "Total Watch Time",
                                    value = "${BharatTubeCatalog.formatCompactCount(overview.totalWatchHours)} hrs",
                                    deltaText = "+${overview.watchTimeChangePct}% vs prev",
                                    accentColor = AmberGold,
                                    modifier = Modifier.weight(1f)
                                )
                                StudioKpiTile(
                                    icon = Icons.Filled.Payments,
                                    label = "Total Earnings",
                                    value = BharatTubeCatalog.formatCurrency(overview.totalEarningsInr, countryCode),
                                    deltaText = "+${overview.earningsChangePct}% vs prev",
                                    accentColor = EmeraldVerified,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Chhotu AI Status Card
                    item {
                        val aiMode = if (!settings.chhotuAiEnabled) "Paused by Owner" else if (settings.chhotuAiStrictMode) "Strict Guard" else "Active & Healthy"
                        val aiColor = if (!settings.chhotuAiEnabled) AmberGold else EmeraldVerified
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF151822),
                            border = BorderStroke(1.dp, aiColor.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("studio_chhotu_status_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(26.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Chhotu AI Studio Check", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Status: $aiMode • Comment Shield ON", style = MaterialTheme.typography.labelSmall, color = aiColor)
                                    }
                                    OutlinedButton(
                                        onClick = { showUploadSheet = true },
                                        border = BorderStroke(1.dp, SaffronFlame),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) { Text("Upload", color = SaffronFlame, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                                }
                                Text(
                                    text = "$approvedCount Published • $checkingCount Checking • $rejectedCount Rejected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB5BACB)
                                )
                            }
                        }
                    }

                    // Recent Videos
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Recent Videos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "See all (${channelVideos.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = SaffronFlame,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { selectedTab = 1 }
                            )
                        }
                    }
                    items(channelVideos.take(3), key = { "dash_${it.id}" }) { video ->
                        val snap = remember(video, timeframe, settings, user?.boostPercent) {
                            BharatTubeAnalyticsEngine.buildVideoAnalytics(video, 0, timeframe, settings, user?.boostPercent ?: 0)
                        }
                        val cmtCount = channelComments.count { it.videoId == video.id }
                        StudioVideoManageCard(
                            video = video,
                            viewsText = "${BharatTubeCatalog.formatCompactCount(snap.views)} views",
                            earningsText = BharatTubeCatalog.formatCurrency(snap.earningsInr, countryCode),
                            commentCount = cmtCount,
                            onEdit = { editingVideo = video },
                            onAnalytics = { onOpenVideoAnalytics(video) },
                            onComments = {
                                commentFilterVideoId = video.id
                                earnCommentsSubTab = 1
                                selectedTab = 3
                            },
                            onDelete = { deletingVideo = video },
                            onOpenChhotuCheck = { onOpenChhotuCheck(video) }
                        )
                    }

                    // Recent Comments
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Recent Comments (${channelComments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "Manage all",
                                style = MaterialTheme.typography.labelLarge,
                                color = SaffronFlame,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    commentFilterVideoId = null
                                    earnCommentsSubTab = 1
                                    selectedTab = 3
                                }
                            )
                        }
                    }
                    if (channelComments.isEmpty()) {
                        item {
                            Text("Comments from viewers on your videos will appear here.", color = Color(0xFFA0A5B8), style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        items(channelComments.take(3), key = { "dash_cmt_${it.id}" }) { cmt ->
                            val v = videoMap[cmt.videoId]
                            StudioCommentModerationCard(
                                comment = cmt,
                                videoTitle = v?.title ?: "Video",
                                onReply = {
                                    replyingComment = cmt
                                    replyText = ""
                                },
                                onTogglePin = { if (v != null) onPinComment(v, cmt) },
                                onToggleHide = { if (v != null) onHideComment(v, cmt) },
                                onDelete = { if (v != null) onDeleteComment(v, cmt) }
                            )
                        }
                    }
                }

                1 -> {
                    // VIDEOS TAB
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Studio Content", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Published, Checking by Chhotu AI, Drafts & Rejected", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
                            }
                            Button(
                                onClick = { showUploadSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Upload", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = videoSearchQuery,
                            onValueChange = { videoSearchQuery = it },
                            label = { Text("Search your videos, Shorts or tags") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    "ALL" to "All (${channelVideos.size})",
                                    "LONG" to "Videos",
                                    "SHORT" to "Shorts",
                                    "LIVE" to "Live",
                                    "CHECKING" to "Checking ($checkingCount)",
                                    "DRAFT" to "Drafts",
                                    "REJECTED" to "Rejected ($rejectedCount)"
                                )
                            ) { (key, lbl) ->
                                FilterChip(
                                    selected = videoListFilter == key,
                                    onClick = { videoListFilter = key },
                                    label = { Text(lbl) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronFlame,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1A1E2B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    val filteredVideos = channelVideos.filter { v ->
                        val matchesTab = when (videoListFilter) {
                            "LONG" -> v.format == VideoFormat.LONG.name
                            "SHORT" -> v.format == VideoFormat.SHORT.name
                            "LIVE" -> v.format == VideoFormat.LIVE.name || v.isLive
                            "CHECKING" -> v.chhotuCheckStatus != "APPROVED" && v.chhotuCheckStatus != "REJECTED"
                            "DRAFT" -> v.visibility == VideoVisibility.DRAFT.name
                            "REJECTED" -> v.chhotuCheckStatus == "REJECTED"
                            else -> true
                        }
                        val matchesQuery = videoSearchQuery.isBlank() ||
                            v.title.contains(videoSearchQuery, ignoreCase = true) ||
                            v.hashtags.contains(videoSearchQuery, ignoreCase = true)
                        matchesTab && matchesQuery
                    }
                    if (filteredVideos.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("No videos match this filter.", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Button(onClick = { showUploadSheet = true }, colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)) {
                                        Text("Upload Video / Short / Live", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredVideos, key = { "vid_${it.id}" }) { video ->
                            val snap = remember(video, timeframe, settings, user?.boostPercent) {
                                BharatTubeAnalyticsEngine.buildVideoAnalytics(video, 0, timeframe, settings, user?.boostPercent ?: 0)
                            }
                            StudioVideoManageCard(
                                video = video,
                                viewsText = "${BharatTubeCatalog.formatCompactCount(snap.views)} views",
                                earningsText = BharatTubeCatalog.formatCurrency(snap.earningsInr, countryCode),
                                commentCount = channelComments.count { it.videoId == video.id },
                                onEdit = { editingVideo = video },
                                onAnalytics = { onOpenVideoAnalytics(video) },
                                onComments = {
                                    commentFilterVideoId = video.id
                                    earnCommentsSubTab = 1
                                    selectedTab = 3
                                },
                                onDelete = { deletingVideo = video },
                                onOpenChhotuCheck = { onOpenChhotuCheck(video) }
                            )
                        }
                    }
                }

                2 -> {
                    // ANALYTICS TAB
                    item {
                        Text("Channel Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            items(AnalyticsTimeframe.entries) { option ->
                                FilterChip(
                                    selected = timeframe == option,
                                    onClick = { timeframe = option },
                                    label = { Text(option.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronFlame,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1A1E2B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    // Views & Earnings Graphs
                    item {
                        AnalyticsCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Views Graph • ${timeframe.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(BharatTubeCatalog.formatCompactCount(overview.totalViews), style = MaterialTheme.typography.titleSmall, color = SaffronFlame, fontWeight = FontWeight.Bold)
                            }
                            AnalyticsLineGraph(values = overview.trendSeries, lineColor = SaffronFlame, modifier = Modifier.fillMaxWidth().height(105.dp).testTag("studio_overview_graph"))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                overview.dateLabels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B91A6)) }
                            }
                        }
                    }
                    item {
                        val earningsTrend = remember(overview.trendSeries) { overview.trendSeries.map { (it * 0.92f).coerceIn(0.12f, 0.98f) } }
                        AnalyticsCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Earnings Graph • ${timeframe.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(BharatTubeCatalog.formatCurrency(overview.totalEarningsInr, countryCode), style = MaterialTheme.typography.titleSmall, color = EmeraldVerified, fontWeight = FontWeight.Bold)
                            }
                            AnalyticsLineGraph(values = earningsTrend, lineColor = EmeraldVerified, modifier = Modifier.fillMaxWidth().height(95.dp))
                        }
                    }
                    if (aggregateSnap != null) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Watch Time", style = MaterialTheme.typography.labelLarge, color = Color(0xFFA0A5B8))
                                    Text("${overview.totalWatchHours} hrs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                                    Text("+${overview.watchTimeChangePct}% vs previous", style = MaterialTheme.typography.labelSmall, color = EmeraldVerified)
                                }
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Subscriber Gained", style = MaterialTheme.typography.labelLarge, color = Color(0xFFA0A5B8))
                                    val totalGained = channelVideos.sumOf { it.subscribersGained }
                                    val totalLost = channelVideos.sumOf { it.subscribersLost }
                                    Text("+${(totalGained * timeframe.viewScale).toInt()} net", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = EmeraldVerified)
                                    Text("Lost ${(totalLost * timeframe.viewScale).toInt()}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8))
                                }
                            }
                        }
                        item {
                            AnalyticsCard {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Audience Retention Graph", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Avg ${String.format("%.1f", aggregateSnap.avgRetentionPct)}%", color = AmberGold, fontWeight = FontWeight.Bold)
                                }
                                AnalyticsLineGraph(values = aggregateSnap.retentionSeries, lineColor = AmberGold, modifier = Modifier.fillMaxWidth().height(95.dp))
                            }
                        }
                        item {
                            AnalyticsCard {
                                Text("Traffic Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                aggregateSnap.trafficSources.forEachIndexed { idx, src ->
                                    val barColor = listOf(SaffronFlame, ElectricCobalt, EmeraldVerified, AmberGold)[idx % 4]
                                    StudioShareProgressRow(src, barColor)
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Age", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    aggregateSnap.ageGroups.forEach { StudioShareProgressRow(it, SaffronFlame) }
                                }
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Gender", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    aggregateSnap.genderSplit.forEachIndexed { i, g ->
                                        StudioShareProgressRow(g, if (i == 0) SaffronFlame else if (i == 1) LotusCoral else ElectricCobalt)
                                    }
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Top Cities", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    aggregateSnap.topCities.forEach { StudioShareProgressRow(it, EmeraldVerified) }
                                }
                                AnalyticsCard(modifier = Modifier.weight(1f)) {
                                    Text("Top Countries", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    aggregateSnap.topCountries.forEach { StudioShareProgressRow(it, AmberGold) }
                                }
                            }
                        }
                    }
                    item {
                        Text("Tap a video for Video-by-Video Analytics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    items(channelVideos, key = { "ana_${it.id}" }) { video ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF151822),
                            border = BorderStroke(1.dp, Color(0xFF252938)),
                            modifier = Modifier.fillMaxWidth().clickable { onOpenVideoAnalytics(video) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(video.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${BharatTubeCatalog.formatCompactCount(video.viewCount)} views", color = Color(0xFFA0A5B8), style = MaterialTheme.typography.labelSmall)
                                }
                                Icon(Icons.Filled.BarChart, contentDescription = "Open Video Analytics", tint = SaffronFlame)
                            }
                        }
                    }
                }

                else -> {
                    // TAB 3: EARN / COMMENTS SWITCHER
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF151822))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0 to "Earn & Payouts", 1 to "Comments (${channelComments.size})").forEach { (idx, title) ->
                                val active = earnCommentsSubTab == idx
                                Surface(
                                    onClick = { earnCommentsSubTab = idx },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (active) SaffronFlame else Color.Transparent,
                                    modifier = Modifier.weight(1f).testTag("studio_subtab_$idx")
                                ) {
                                    Text(
                                        text = title,
                                        color = if (active) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (earnCommentsSubTab == 0) {
                        // EARN SUB-TAB (NO 50% brand split text shown here!)
                        item {
                            val monActive = (activeChannel?.monetizationEnabled != false) && settings.creatorMonetizationEnabled
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                                border = BorderStroke(1.dp, Color(0xFF252938))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Total Balance", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
                                    Text(
                                        BharatTubeCatalog.formatCurrency(availableBalanceInr, countryCode),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = EmeraldVerified,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Lifetime Earned: ${BharatTubeCatalog.formatCurrency(creatorTotalInr, countryCode)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB5BACB)
                                    )
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("Channel Monetization", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Controlled by Owner Control Center", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8))
                                        }
                                        Surface(shape = RoundedCornerShape(50), color = (if (monActive) EmeraldVerified else LotusCoral).copy(alpha = 0.2f)) {
                                            Text(
                                                text = if (monActive) "ON" else "OFF",
                                                color = if (monActive) EmeraldVerified else LotusCoral,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Per 1,000 Views Earning Rate: ₹${settings.per1000ViewsPaymentInr.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SaffronFlame,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        item {
                            val threshold = settings.indiaPayoutThresholdInr
                            val progress = (availableBalanceInr / threshold).toFloat().coerceIn(0f, 1f)
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                                border = BorderStroke(1.dp, Color(0xFF252938))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Auto Payout ₹${threshold.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = if (settings.automaticPayoutEnabled && settings.creatorMonetizationEnabled) "Auto Payout ON • Triggers automatically at ₹${threshold.toInt()}" else "Payouts Paused",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (settings.automaticPayoutEnabled) EmeraldVerified else LotusCoral
                                            )
                                        }
                                    }
                                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = EmeraldVerified)
                                    Text("Balance: ₹${String.format("%.2f", availableBalanceInr)} / ₹${threshold.toInt()} minimum", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    VerificationRow("KYC Identity Verification", user?.kycVerified == true)
                                    VerificationRow("Payment Account (UPI / Bank)", user?.paymentAccountVerified == true)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(PayoutMethod.UPI to "UPI", PayoutMethod.BANK_TRANSFER to "Bank Account").forEach { (m, l) ->
                                            FilterChip(selected = selectedMethod == m, onClick = { selectedMethod = m }, label = { Text(l) })
                                        }
                                    }
                                    if (selectedMethod == PayoutMethod.UPI) {
                                        OutlinedTextField(value = upiDraft, onValueChange = { upiDraft = it }, label = { Text("UPI ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    } else {
                                        OutlinedTextField(value = bankDraft, onValueChange = { bankDraft = it }, label = { Text("Bank Account / IFSC") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    }
                                    OutlinedButton(onClick = { onSavePayoutMethod(selectedMethod, upiDraft, bankDraft) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Save Payout Method", color = Color.White)
                                    }
                                    if (activeChannel != null) {
                                        Button(
                                            onClick = { onInitiatePayout(activeChannel, availableBalanceInr, selectedMethod) },
                                            enabled = availableBalanceInr >= threshold && settings.automaticPayoutEnabled && settings.creatorMonetizationEnabled,
                                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                            modifier = Modifier.fillMaxWidth().testTag("initiate_payout_button")
                                        ) { Text("Request Payout Now", color = Color.Black, fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }

                        item {
                            IntegrationDisclosureBanner(
                                title = "Automatic ₹${settings.indiaPayoutThresholdInr.toInt()} Payout Rail",
                                message = "Once available balance crosses ₹${settings.indiaPayoutThresholdInr.toInt()}, BharatTube automatically queues or settles payouts to your UPI or Bank Account."
                            )
                        }

                        item {
                            Text("Payout History (${channelPayouts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        items(channelPayouts, key = { it.id }) { payout ->
                            val isPaid = payout.status == "CONFIRMED"
                            val formattedDate = remember(payout.createdAt) {
                                SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(payout.createdAt))
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("₹${String.format("%,.2f", payout.amountInr)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${if (payout.method == PayoutMethod.BANK_TRANSFER.name) "Bank Account" else payout.method} • ${payout.destinationMasked}", color = Color(0xFFB5BACB), style = MaterialTheme.typography.bodySmall)
                                        Text("$formattedDate • ${payout.transactionId}", color = Color(0xFF8B91A6), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Surface(shape = RoundedCornerShape(50), color = (if (isPaid) EmeraldVerified else AmberGold).copy(alpha = 0.2f)) {
                                        Text(
                                            text = if (isPaid) "Paid" else "Pending",
                                            color = if (isPaid) EmeraldVerified else AmberGold,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // COMMENTS SUB-TAB
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldVerified.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Shield, contentDescription = null, tint = EmeraldVerified)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Chhotu AI Comment Guard automatically blocks abusive, hate, or scam comments. You can Reply, Pin, Hide, or Delete below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = commentFilterVideoId == null,
                                        onClick = { commentFilterVideoId = null },
                                        label = { Text("All Videos (${channelComments.size})") }
                                    )
                                }
                                items(channelVideos, key = { "cf_${it.id}" }) { v ->
                                    FilterChip(
                                        selected = commentFilterVideoId == v.id,
                                        onClick = { commentFilterVideoId = v.id },
                                        label = { Text(v.title.take(18)) }
                                    )
                                }
                            }
                        }
                        val filteredComments = if (commentFilterVideoId == null) {
                            channelComments
                        } else {
                            channelComments.filter { it.videoId == commentFilterVideoId }
                        }
                        if (filteredComments.isEmpty()) {
                            item {
                                Text("No comments found for this selection yet.", color = Color(0xFFA0A5B8), style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            items(filteredComments, key = { it.id }) { cmt ->
                                val v = videoMap[cmt.videoId]
                                StudioCommentModerationCard(
                                    comment = cmt,
                                    videoTitle = v?.title ?: "Video",
                                    onReply = {
                                        replyingComment = cmt
                                        replyText = ""
                                    },
                                    onTogglePin = { if (v != null) onPinComment(v, cmt) },
                                    onToggleHide = { if (v != null) onHideComment(v, cmt) },
                                    onDelete = { if (v != null) onDeleteComment(v, cmt) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadSheet) {
        StudioUploadBottomSheet(
            onDismiss = { showUploadSheet = false },
            onUploadVideo = {
                showUploadSheet = false
                onUploadVideo()
            },
            onCreateShort = {
                showUploadSheet = false
                onCreateShort()
            },
            onGoLive = {
                showUploadSheet = false
                onGoLive()
            }
        )
    }

    editingVideo?.let { target ->
        StudioEditVideoDialog(
            video = target,
            onDismiss = { editingVideo = null },
            onSave = { title, desc, vis, cat, tags, thumbKey, scheduledAt ->
                onUpdateVideo(target.id, title, desc, vis, cat, tags, thumbKey, scheduledAt)
                editingVideo = null
            }
        )
    }

    deletingVideo?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingVideo = null },
            title = { Text("Delete Video?") },
            text = { Text("Remove '${target.title}' permanently from BharatTube Studio?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVideo(target)
                        deletingVideo = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LotusCoral)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingVideo = null }) { Text("Cancel") }
            }
        )
    }

    replyingComment?.let { target ->
        AlertDialog(
            onDismissRequest = { replyingComment = null },
            title = { Text("Reply to ${target.authorName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(target.text, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Write Creator Reply") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("studio_reply_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCommentReply(target.videoId, "@${target.authorName} ${replyText.trim()}", target.id)
                        replyingComment = null
                        replyText = ""
                    },
                    enabled = replyText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                    modifier = Modifier.testTag("studio_reply_send_button")
                ) { Text("Send Reply", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { replyingComment = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun VerificationRow(label: String, verified: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (verified) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (verified) EmeraldVerified else AmberGold
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ${if (verified) "Verified" else "Pending"}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}
