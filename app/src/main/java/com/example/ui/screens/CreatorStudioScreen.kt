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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.SaffronFlame

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
    onInitiatePayout: (ChannelEntity, Double, PayoutMethod) -> Unit
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
    val creatorTotalInr = totalNetInr * (settings.creatorSharePct / 100.0)
    val platformTotalInr = totalNetInr * (settings.platformSharePct / 100.0)
    val confirmedPaidInr = payouts
        .filter { it.channelId == activeChannel?.id && it.status != "FAILED" }
        .sumOf { it.amountInr }
    val availableBalanceInr = (creatorTotalInr - confirmedPaidInr).coerceAtLeast(0.0)

    var selectedMethod by remember { mutableStateOf(PayoutMethod.UPI) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creator Studio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Aggregate creator analytics only — individual viewer histories remain private.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Transparent 50/50 Revenue Sharing Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Transparent Revenue Sharing (${settings.creatorSharePct}% Creator / ${settings.platformSharePct}% BharatTube)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Eligible Net Revenue: ${BharatTubeCatalog.formatCurrency(totalNetInr, countryCode)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Your Creator Share (${settings.creatorSharePct}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    BharatTubeCatalog.formatCurrency(creatorTotalInr, countryCode),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EmeraldVerified,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("BharatTube Share (${settings.platformSharePct}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    BharatTubeCatalog.formatCurrency(platformTotalInr, countryCode),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Text(
                            "Long Videos Net: ${BharatTubeCatalog.formatCurrency(longNetInr, countryCode)} • Shorts Net: ${BharatTubeCatalog.formatCurrency(shortsNetInr, countryCode)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Available Balance: ${BharatTubeCatalog.formatCurrency(availableBalanceInr, countryCode)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = SaffronFlame,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // India ₹1,000 Automatic Payout System
            item {
                val threshold = settings.indiaPayoutThresholdInr
                val progress = (availableBalanceInr / threshold).toFloat().coerceIn(0f, 1f)
                val kycOk = user?.kycVerified == true
                val payOk = user?.paymentAccountVerified == true
                val taxOk = user?.taxVerified == true

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = SaffronFlame)
                            Spacer(Modifier.width(8.dp))
                            Text("India Payout Rule (Min ₹${String.format("%.0f", threshold)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = EmeraldVerified)
                        Text(
                            "Balance: ₹${String.format("%.2f", availableBalanceInr)} / ₹${String.format("%.0f", threshold)} threshold",
                            style = MaterialTheme.typography.labelMedium
                        )
                        VerificationRow("KYC Identity Verification", kycOk)
                        VerificationRow("Payment Account (UPI / Bank) Verified", payOk)
                        VerificationRow("Tax Verification (PAN / W-8BEN)", taxOk)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PayoutMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = selectedMethod == method,
                                    onClick = { selectedMethod = method },
                                    label = { Text(method.name.replace("_", " ")) }
                                )
                            }
                        }

                        if (activeChannel != null) {
                            Button(
                                onClick = { onInitiatePayout(activeChannel, availableBalanceInr, selectedMethod) },
                                enabled = availableBalanceInr >= threshold && kycOk && payOk && taxOk,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                modifier = Modifier.fillMaxWidth().testTag("initiate_payout_button")
                            ) {
                                Text("Initiate Verified Payout")
                            }
                        }
                    }
                }
            }

            item {
                IntegrationDisclosureBanner(
                    title = "Payment Gateway & Ad Network Verification",
                    message = "Payouts initiated here enter 'INITIATED' state and await Razorpay/Stripe/UPI banking rail confirmation. Never marks payment completed before provider webhook confirmation."
                )
            }

            // Aggregate Audience Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Aggregate Audience Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Top Countries: India (68%), UAE (11%), USA (9%), UK (5%), Others (7%)", style = MaterialTheme.typography.bodySmall)
                        Text("Top Languages: Hindi (46%), English (32%), Tamil (9%), Marathi (7%), Others (6%)", style = MaterialTheme.typography.bodySmall)
                        Text("Devices: Mobile Android/iOS (81%), Desktop Web (14%), Smart TV (5%)", style = MaterialTheme.typography.bodySmall)
                        Text("Avg Watch Duration: 6m 18s • Audience Retention: 58%", style = MaterialTheme.typography.labelMedium, color = SaffronFlame)
                    }
                }
            }

            // Video-wise Revenue Breakdown
            item {
                Text("Video & Shorts Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(channelVideos, key = { it.id }) { video ->
                val split = BharatTubeCatalog.calculateRevenueSplit(
                    grossRevenueInr = video.netRevenueInr / 0.8,
                    creatorSharePct = settings.creatorSharePct,
                    platformSharePct = settings.platformSharePct
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${video.format} • ${video.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${BharatTubeCatalog.formatCompactCount(video.viewCount)} views • Creator Share: ₹${String.format("%.2f", split.creatorEarningsInr)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldVerified
                        )
                    }
                }
            }

            // Payout History
            if (payouts.isNotEmpty()) {
                item {
                    Text("Payout History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(payouts, key = { it.id }) { payout ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "₹${String.format("%.2f", payout.amountInr)} • ${payout.method} (${payout.destinationMasked})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "TXN: ${payout.transactionId} • Status: ${payout.status}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberGold
                            )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
            style = MaterialTheme.typography.bodySmall
        )
    }
}
