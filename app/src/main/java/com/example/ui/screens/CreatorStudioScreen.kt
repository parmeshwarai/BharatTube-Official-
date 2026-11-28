package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AnalyticsTimeframe
import com.example.data.model.BharatTubeAnalyticsEngine
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutMethod
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioScreen(
    user: UserProfileEntity?,
    myChannels: List<ChannelEntity>,
    allChannels: List<ChannelEntity>,
    allVideos: List<VideoEntity>,
    payouts: List<PayoutEntity>,
    settings: PlatformSettingsEntity,
    countryCode: String,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVideoAnalytics: (VideoEntity) -> Unit,
    onOpenChhotuCheck: (VideoEntity) -> Unit,
    onInitiatePayout: (ChannelEntity, Double, PayoutMethod) -> Unit,
    onSavePayoutMethod: (PayoutMethod, String, String) -> Unit
) {
    val activeChannel = myChannels.firstOrNull() ?: allChannels.firstOrNull()
    val channelVideos = remember(allVideos, activeChannel) {
        if (activeChannel == null) emptyList() else allVideos.filter { it.channelId == activeChannel.id }
    }
    val longVideos = channelVideos.filter { it.format != VideoFormat.SHORT.name }
    val shorts = channelVideos.filter { it.format == VideoFormat.SHORT.name }

    var timeframe by remember { mutableStateOf(AnalyticsTimeframe.LAST_7_DAYS) }
    val overview = remember(channelVideos, timeframe, settings, user?.boostPercent) {
        BharatTubeAnalyticsEngine.buildStudioOverview(
            videos = channelVideos,
            timeframe = timeframe,
            settings = settings,
            userBoostPct = user?.boostPercent ?: 0
        )
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
    val channelPayouts = remember(payouts, activeChannel) {
        if (activeChannel == null) emptyList() else payouts.filter { it.channelId == activeChannel.id }
    }
    val confirmedPaidInr = channelPayouts
        .filter { it.status != "FAILED" }
        .sumOf { it.amountInr }
    val availableBalanceInr = (creatorTotalInr - confirmedPaidInr).coerceAtLeast(0.0)

    var selectedMethod by remember(user?.preferredPayoutMethod) {
        mutableStateOf(
            runCatching { PayoutMethod.valueOf(user?.preferredPayoutMethod ?: "UPI") }
                .getOrDefault(PayoutMethod.UPI)
        )
    }
    var upiDraft by remember(user?.upiId) { mutableStateOf(user?.upiId ?: "") }
    var bankDraft by remember(user?.bankAccountMasked) { mutableStateOf(user?.bankAccountMasked ?: "") }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("BharatTube", color = SaffronFlame, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Creator Studio", color = Color(0xFFA0A5B8), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("studio_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = activeChannel?.let { "${it.name} (${it.handle})" } ?: "No Channel Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA0A5B8)
                    )
                }
            }

            // Timeframe Filter Chips: Last 7 Days | 28 Days | 90 Days | All Time
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

            // Studio Overview Chart + KPIs (Ref WA6101)
            item {
                AnalyticsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Overview • ${timeframe.label}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "• Views  ${BharatTubeCatalog.formatCompactCount(overview.totalViews)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SaffronFlame,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Earnings  ${BharatTubeCatalog.formatCurrency(overview.totalEarningsInr, countryCode)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldVerified,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnalyticsLineGraph(
                        values = overview.trendSeries,
                        lineColor = SaffronFlame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .testTag("studio_overview_graph")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        overview.dateLabels.forEach { lbl ->
                            Text(lbl, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B91A6))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StudioMetricCard(
                            label = "Views",
                            value = BharatTubeCatalog.formatCompactCount(overview.totalViews),
                            sub = "+${overview.viewsChangePct}%",
                            modifier = Modifier.weight(1f)
                        )
                        StudioMetricCard(
                            label = "Watch time",
                            value = "${BharatTubeCatalog.formatCompactCount(overview.totalWatchHours)}h",
                            sub = "+${overview.watchTimeChangePct}%",
                            modifier = Modifier.weight(1f)
                        )
                        StudioMetricCard(
                            label = "Earnings",
                            value = BharatTubeCatalog.formatCurrency(overview.totalEarningsInr, countryCode),
                            sub = "+${overview.earningsChangePct}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Video-wise List with Views, Watch Time, Per-Video Earnings, Chhotu AI Status, and Analytics Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Videos • ${channelVideos.size} results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${longVideos.size} Videos • ${shorts.size} Shorts",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA0A5B8)
                    )
                }
            }

            items(channelVideos, key = { it.id }) { video ->
                val snap = remember(video, timeframe, settings, user?.boostPercent) {
                    BharatTubeAnalyticsEngine.buildVideoAnalytics(
                        video = video,
                        commentCount = 0,
                        timeframe = timeframe,
                        settings = settings,
                        userBoostPct = user?.boostPercent ?: 0
                    )
                }
                val chhotuColor = when (video.chhotuCheckStatus) {
                    "APPROVED" -> EmeraldVerified
                    "REJECTED" -> LotusCoral
                    else -> AmberGold
                }
                val chhotuLabel = when (video.chhotuCheckStatus) {
                    "APPROVED" -> "Chhotu AI Status: Approved"
                    "REJECTED" -> "Chhotu AI Status: Rejected"
                    else -> "Chhotu AI Status: Checking..."
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_video_card_${video.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(108.dp)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                BharatTubeThumbnail(
                                    thumbnailKey = video.thumbnailKey,
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${BharatTubeCatalog.formatCompactCount(snap.views)} views • ${String.format("%.0f", snap.watchTimeHours)}h watch • ${BharatTubeCatalog.formatCurrency(snap.earningsInr, countryCode)} • ${BharatTubeCatalog.formatCompactCount(snap.likes)} likes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA0A5B8),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = chhotuColor.copy(alpha = 0.18f),
                                modifier = Modifier.clickable { onOpenChhotuCheck(video) }
                            ) {
                                Text(
                                    text = chhotuLabel,
                                    color = chhotuColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = { onOpenVideoAnalytics(video) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SaffronFlame.copy(alpha = 0.65f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("video_analytics_btn_${video.id}")
                            ) {
                                Icon(Icons.Filled.BarChart, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Analytics", color = SaffronFlame, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Creator Revenue & Balance Card (NO 50/50 split text shown here per Brand Rule)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                    border = BorderStroke(1.dp, Color(0xFF252938))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Creator Revenue & Balance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Column {
                            Text("Lifetime Estimated Earnings", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
                            Text(
                                BharatTubeCatalog.formatCurrency(creatorTotalInr, countryCode),
                                style = MaterialTheme.typography.titleLarge,
                                color = EmeraldVerified,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Available Balance: ${BharatTubeCatalog.formatCurrency(availableBalanceInr, countryCode)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = SaffronFlame,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Automatic ₹2,000 Payout Rule & Method Selection (UPI / Bank Account)
            item {
                val threshold = settings.indiaPayoutThresholdInr
                val progress = (availableBalanceInr / threshold).toFloat().coerceIn(0f, 1f)
                val kycOk = user?.kycVerified == true
                val payOk = user?.paymentAccountVerified == true
                val taxOk = user?.taxVerified == true

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
                                Text(
                                    "Automatic Payout (Min ₹${String.format("%.0f", threshold)})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (settings.automaticPayoutEnabled && settings.creatorMonetizationEnabled) {
                                        "Auto Payout ON • Triggers automatically at ₹${threshold.toInt()}"
                                    } else {
                                        "Payouts Paused by Platform Control"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (settings.automaticPayoutEnabled) EmeraldVerified else LotusCoral
                                )
                            }
                        }
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = EmeraldVerified)
                        Text(
                            "Balance: ₹${String.format("%.2f", availableBalanceInr)} / ₹${String.format("%.0f", threshold)} minimum payout",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        VerificationRow("KYC Identity Verification", kycOk)
                        VerificationRow("Payment Account (UPI / Bank) Verified", payOk)
                        VerificationRow("Tax Verification (PAN / W-8BEN)", taxOk)

                        Text("Payout Method", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PayoutMethod.UPI to "UPI", PayoutMethod.BANK_TRANSFER to "Bank Account").forEach { (method, label) ->
                                FilterChip(
                                    selected = selectedMethod == method,
                                    onClick = { selectedMethod = method },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("studio_payout_method_${method.name}")
                                )
                            }
                        }

                        if (selectedMethod == PayoutMethod.UPI) {
                            OutlinedTextField(
                                value = upiDraft,
                                onValueChange = { upiDraft = it },
                                label = { Text("UPI ID (e.g., name@upi)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = bankDraft,
                                onValueChange = { bankDraft = it },
                                label = { Text("Bank Account / IFSC Details") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedButton(
                            onClick = { onSavePayoutMethod(selectedMethod, upiDraft, bankDraft) },
                            modifier = Modifier.fillMaxWidth().testTag("studio_save_payout_method")
                        ) {
                            Text("Save Payout Method", color = Color.White)
                        }

                        if (activeChannel != null) {
                            Button(
                                onClick = { onInitiatePayout(activeChannel, availableBalanceInr, selectedMethod) },
                                enabled = availableBalanceInr >= threshold && settings.automaticPayoutEnabled && settings.creatorMonetizationEnabled,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                modifier = Modifier.fillMaxWidth().testTag("initiate_payout_button")
                            ) {
                                Text("Request Payout Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                IntegrationDisclosureBanner(
                    title = "Automatic ₹${settings.indiaPayoutThresholdInr.toInt()} Payout Rail",
                    message = "Once available balance crosses ₹${settings.indiaPayoutThresholdInr.toInt()}, BharatTube automatically queues or settles payouts to your saved UPI ID or Bank Account."
                )
            }

            // Payout History (Date, Amount, UPI/Bank, Paid / Pending)
            item {
                Text("Payout History (${channelPayouts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (channelPayouts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF151822),
                        border = BorderStroke(1.dp, Color(0xFF252938)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No payouts yet. Automatic payout triggers at ₹${settings.indiaPayoutThresholdInr.toInt()}.",
                            color = Color(0xFFA0A5B8),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(channelPayouts, key = { it.id }) { payout ->
                    val isPaid = payout.status == "CONFIRMED"
                    val formattedDate = remember(payout.createdAt) {
                        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(payout.createdAt))
                    }
                    val methodLabel = if (payout.method == PayoutMethod.BANK_TRANSFER.name) "Bank Account" else payout.method
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF151822),
                        border = BorderStroke(1.dp, Color(0xFF252938)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "₹${String.format("%,.2f", payout.amountInr)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "$methodLabel • ${payout.destinationMasked}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB5BACB)
                                )
                                Text(
                                    "$formattedDate • ${payout.transactionId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B91A6)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isPaid) EmeraldVerified.copy(alpha = 0.2f) else AmberGold.copy(alpha = 0.2f)
                            ) {
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
            }
        }
    }
}

@Composable
private fun StudioMetricCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141E)),
        border = BorderStroke(1.dp, Color(0xFF252938))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8), maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = EmeraldVerified, maxLines = 1)
        }
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
