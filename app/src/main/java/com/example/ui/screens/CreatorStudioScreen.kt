package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutMethod
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
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
    onInitiatePayout: (ChannelEntity, Double, PayoutMethod) -> Unit,
    onSavePayoutMethod: (PayoutMethod, String, String) -> Unit
) {
    val activeChannel = myChannels.firstOrNull() ?: allChannels.firstOrNull()
    val channelVideos = remember(allVideos, activeChannel) {
        if (activeChannel == null) emptyList() else allVideos.filter { it.channelId == activeChannel.id }
    }
    val longVideos = channelVideos.filter { it.format != VideoFormat.SHORT.name }
    val shorts = channelVideos.filter { it.format == VideoFormat.SHORT.name }

    val totalViews = channelVideos.sumOf { it.viewCount }
    val totalWatchHours = (totalViews * 4.2 / 60.0).toLong()
    val longNetInr = longVideos.sumOf { it.netRevenueInr }
    val shortsNetInr = shorts.sumOf { it.netRevenueInr }
    val totalNetInr = longNetInr + shortsNetInr
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
                title = { Text("Creator Studio", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                Text(
                    text = activeChannel?.let { "${it.name} (${it.handle})" } ?: "No Channel Active",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Aggregate creator analytics only — individual viewer histories remain private.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA0A5B8)
                )
            }

            // KPI Summary Grid
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudioMetricCard(
                        label = "Total Views",
                        value = BharatTubeCatalog.formatCompactCount(totalViews),
                        sub = "${longVideos.size} Videos • ${shorts.size} Shorts",
                        modifier = Modifier.weight(1f)
                    )
                    StudioMetricCard(
                        label = "Subscribers",
                        value = BharatTubeCatalog.formatCompactCount(activeChannel?.subscriberCount ?: 0L),
                        sub = "Watch Time: ${BharatTubeCatalog.formatCompactCount(totalWatchHours)}h",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Creator Earnings Card (NO 50/50 split text shown here per Brand Rule)
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
                            Text("Estimated Creator Earnings", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
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

            // Aggregate Audience Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
                    border = BorderStroke(1.dp, Color(0xFF252938))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Aggregate Audience Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Top Countries: India (68%), UAE (11%), USA (9%), UK (5%), Others (7%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD0D4E2))
                        Text("Top Languages: Hindi (46%), English (32%), Tamil (9%), Marathi (7%), Others (6%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD0D4E2))
                        Text("Devices: Mobile Android/iOS (81%), Desktop Web (14%), Smart TV (5%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD0D4E2))
                        Text("Avg Watch Duration: 6m 18s • Audience Retention: 58%", style = MaterialTheme.typography.labelMedium, color = SaffronFlame)
                    }
                }
            }

            // Video-wise Revenue Breakdown
            item {
                Text("Video & Shorts Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(channelVideos, key = { it.id }) { video ->
                val videoEarnedInr = BharatTubeCatalog.calculateCreatorEarningsInr(
                    viewCount = video.viewCount,
                    netRevenueInr = video.netRevenueInr,
                    creatorSharePct = settings.creatorSharePct,
                    per1000ViewsPaymentInr = settings.per1000ViewsPaymentInr,
                    bonusPercent = settings.bonusPercent,
                    userBoostPct = user?.boostPercent ?: 0
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${video.format} • ${video.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            "${BharatTubeCatalog.formatCompactCount(video.viewCount)} views • Earned: ₹${String.format("%.2f", videoEarnedInr)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldVerified
                        )
                    }
                }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151822)),
        border = BorderStroke(1.dp, Color(0xFF252938))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = SaffronFlame)
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
