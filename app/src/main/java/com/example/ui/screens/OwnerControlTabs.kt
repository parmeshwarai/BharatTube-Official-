package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutStatus
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun OwnerControlCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF252938),
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
internal fun OwnerToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, Color(0xFF232736)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
                }
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SaffronFlame,
                    uncheckedThumbColor = Color(0xFFB0B5C6),
                    uncheckedTrackColor = Color(0xFF2A2E3D)
                )
            )
        }
    }
}

@Composable
internal fun OwnerPaisaControlSection(
    settings: PlatformSettingsEntity,
    onUpdateSettings: (PlatformSettingsEntity, String, String, String, String) -> Unit
) {
    var draftCreatorPct by remember(settings.creatorSharePct) { mutableIntStateOf(settings.creatorSharePct) }
    var draftCpm by remember(settings.per1000ViewsPaymentInr) { mutableStateOf(settings.per1000ViewsPaymentInr.toInt().toString()) }
    var draftBonusPct by remember(settings.bonusPercent) { mutableIntStateOf(settings.bonusPercent) }
    var draftThreshold by remember(settings.indiaPayoutThresholdInr) { mutableStateOf(settings.indiaPayoutThresholdInr.toInt().toString()) }
    var draftAutoPayout by remember(settings.automaticPayoutEnabled) { mutableStateOf(settings.automaticPayoutEnabled) }
    var draftPayoutsOn by remember(settings.creatorMonetizationEnabled) { mutableStateOf(settings.creatorMonetizationEnabled) }

    val platformPct = 100 - draftCreatorPct
    val cpmNumeric = draftCpm.toIntOrNull()?.coerceAtLeast(0) ?: 120

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Money Control • Manage payments, earnings & bonuses",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA0A5B8)
        )

        // Creator Earning % (50% BharatTube / 50% Creator — Owner Only)
        OwnerControlCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Creator Earning %", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaffronFlame.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, SaffronFlame.copy(alpha = 0.55f))
                ) {
                    Text(
                        "ADJUSTABLE",
                        color = SaffronFlame,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text("Split revenue between BharatTube and Creator", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${platformPct}% BharatTube", color = SaffronFlame, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text("${draftCreatorPct}% Creator", color = EmeraldVerified, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Slider(
                value = draftCreatorPct.toFloat(),
                onValueChange = { draftCreatorPct = it.roundToInt() },
                valueRange = 10f..90f,
                colors = SliderDefaults.colors(thumbColor = SaffronFlame, activeTrackColor = SaffronFlame),
                modifier = Modifier.testTag("paisa_creator_pct_slider")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleStepperButton(icon = Icons.Filled.Remove, contentDesc = "Decrease Creator %") {
                    draftCreatorPct = (draftCreatorPct - 5).coerceIn(10, 90)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF232634)) {
                    Text(
                        text = "$platformPct / $draftCreatorPct",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                CircleStepperButton(icon = Icons.Filled.Add, contentDesc = "Increase Creator %") {
                    draftCreatorPct = (draftCreatorPct + 5).coerceIn(10, 90)
                }
            }
        }

        // Per 1000 Views Payment (CPM)
        OwnerControlCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Per 1000 Views Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Set payout amount per 1000 views (CPM)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
                }
                Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = SaffronFlame)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircleStepperButton(icon = Icons.Filled.Remove, contentDesc = "Decrease CPM") {
                    draftCpm = (cpmNumeric - 10).coerceAtLeast(10).toString()
                }
                OutlinedTextField(
                    value = draftCpm,
                    onValueChange = { draftCpm = it.filter { ch -> ch.isDigit() }.take(5) },
                    prefix = { Text("₹", color = SaffronFlame, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("paisa_cpm_input")
                )
                CircleStepperButton(icon = Icons.Filled.Add, contentDesc = "Increase CPM") {
                    draftCpm = (cpmNumeric + 10).coerceAtMost(5000).toString()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(60, 120, 200, 350).forEach { preset ->
                    FilterChip(
                        selected = cpmNumeric == preset,
                        onClick = { draftCpm = preset.toString() },
                        label = { Text("₹$preset") }
                    )
                }
            }
            Text(
                "Current: ₹$cpmNumeric per 1,000 views",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFA0A5B8)
            )
        }

        // Bonus Control
        OwnerControlCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bonus Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Additional bonus percentage for creators", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
                }
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AmberGold)
            }
            Text("${draftBonusPct}% BONUS", color = SaffronFlame, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Slider(
                value = draftBonusPct.toFloat(),
                onValueChange = { draftBonusPct = it.roundToInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = SaffronFlame, activeTrackColor = SaffronFlame)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleStepperButton(icon = Icons.Filled.Remove, contentDesc = "Decrease Bonus") {
                    draftBonusPct = (draftBonusPct - 5).coerceIn(0, 100)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF232634)) {
                    Text("${draftBonusPct}%", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                }
                CircleStepperButton(icon = Icons.Filled.Add, contentDesc = "Increase Bonus") {
                    draftBonusPct = (draftBonusPct + 5).coerceIn(0, 100)
                }
            }
        }

        // Payout ON/OFF & ₹2,000 Minimum Auto Payout
        OwnerControlCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Payout & Auto-Payout (₹2,000)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Enable or disable creator payouts globally", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
                }
                Icon(Icons.Filled.Wallet, contentDescription = null, tint = SaffronFlame)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payouts Status", color = Color.White, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = draftPayoutsOn,
                        onCheckedChange = { draftPayoutsOn = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SaffronFlame),
                        modifier = Modifier.testTag("paisa_payouts_switch")
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (draftPayoutsOn) "ON" else "OFF", color = if (draftPayoutsOn) EmeraldVerified else LotusCoral, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Automatic Payout (≥ Min)", color = Color.White, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = draftAutoPayout,
                    onCheckedChange = { draftAutoPayout = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SaffronFlame)
                )
            }
            OutlinedTextField(
                value = draftThreshold,
                onValueChange = { draftThreshold = it.filter { ch -> ch.isDigit() }.take(6) },
                label = { Text("Minimum Auto Payout (INR ₹)") },
                prefix = { Text("₹", color = SaffronFlame) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("paisa_min_threshold_input")
            )
        }

        // Reset to Defaults & Save Changes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    draftCreatorPct = 50
                    draftCpm = "120"
                    draftBonusPct = 15
                    draftThreshold = "2000"
                    draftAutoPayout = true
                    draftPayoutsOn = true
                    onUpdateSettings(
                        settings.copy(
                            creatorSharePct = 50,
                            platformSharePct = 50,
                            per1000ViewsPaymentInr = 120.0,
                            bonusPercent = 15,
                            indiaPayoutThresholdInr = 2000.0,
                            automaticPayoutEnabled = true,
                            creatorMonetizationEnabled = true
                        ),
                        "RESET_MONEY_DEFAULTS",
                        "Reset Money Control to 50/50 split, ₹120 CPM, 15% Bonus, ₹2,000 Auto Payout",
                        "${settings.creatorSharePct}:${settings.platformSharePct}",
                        "50:50 | ₹120 | 15% | ₹2000"
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Reset Defaults", color = Color.White, maxLines = 1)
            }
            Button(
                onClick = {
                    val newThresh = draftThreshold.toDoubleOrNull()?.coerceAtLeast(100.0) ?: 2000.0
                    val newCpm = cpmNumeric.toDouble().coerceAtLeast(10.0)
                    onUpdateSettings(
                        settings.copy(
                            creatorSharePct = draftCreatorPct,
                            platformSharePct = platformPct,
                            per1000ViewsPaymentInr = newCpm,
                            bonusPercent = draftBonusPct,
                            indiaPayoutThresholdInr = newThresh,
                            automaticPayoutEnabled = draftAutoPayout,
                            creatorMonetizationEnabled = draftPayoutsOn
                        ),
                        "SAVE_PAISA_CONTROL",
                        "Updated Money Control: Split $platformPct:$draftCreatorPct, CPM ₹${newCpm.toInt()}, Bonus $draftBonusPct%, Min ₹${newThresh.toInt()}",
                        "${settings.creatorSharePct}:${settings.platformSharePct} | ₹${settings.per1000ViewsPaymentInr.toInt()}",
                        "$draftCreatorPct:$platformPct | ₹${newCpm.toInt()} | ₹${newThresh.toInt()}"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("paisa_save_changes_button")
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun OwnerWorkAndBoostSection(
    settings: PlatformSettingsEntity,
    onUpdateSettings: (PlatformSettingsEntity, String, String, String, String) -> Unit
) {
    var draftVideoLimit by remember(settings.dailyVideoUploadLimit) { mutableIntStateOf(settings.dailyVideoUploadLimit) }
    var draftShortsLimit by remember(settings.dailyShortsUploadLimit) { mutableIntStateOf(settings.dailyShortsUploadLimit) }
    var draftLiveLimit by remember(settings.dailyLiveLimit) { mutableIntStateOf(settings.dailyLiveLimit) }
    var draftWorkBoostPct by remember(settings.workBoostPercent) { mutableIntStateOf(settings.workBoostPercent) }
    var draftHomeBoost by remember(settings.homePageBoostEnabled) { mutableStateOf(settings.homePageBoostEnabled) }
    var draftTrendingBoost by remember(settings.trendingBoostEnabled) { mutableStateOf(settings.trendingBoostEnabled) }
    var draftSearchBoost by remember(settings.searchBoostEnabled) { mutableStateOf(settings.searchBoostEnabled) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OwnerControlCard {
            Text("Kaam Jyada Karne Ka Control • Daily Limits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Increase or decrease daily creator upload & live stream limits", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))

            LimitSliderRow(
                label = "Video Upload Limit (per day)",
                value = draftVideoLimit,
                range = 0f..50f,
                onValueChange = { draftVideoLimit = it }
            )
            LimitSliderRow(
                label = "Shorts Limit (per day)",
                value = draftShortsLimit,
                range = 0f..100f,
                onValueChange = { draftShortsLimit = it }
            )
            LimitSliderRow(
                label = "Live Stream Limit (per day)",
                value = draftLiveLimit,
                range = 0f..25f,
                onValueChange = { draftLiveLimit = it }
            )
        }

        OwnerControlCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Work Boost & Discovery Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Boost visibility multiplier for selected creators", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
                }
                Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = SaffronFlame)
            }
            Text("${draftWorkBoostPct}% BOOST", color = SaffronFlame, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Slider(
                value = draftWorkBoostPct.toFloat(),
                onValueChange = { draftWorkBoostPct = it.roundToInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = SaffronFlame, activeTrackColor = SaffronFlame)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleStepperButton(icon = Icons.Filled.Remove, contentDesc = "Decrease Work Boost") {
                    draftWorkBoostPct = (draftWorkBoostPct - 10).coerceIn(0, 100)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF232634)) {
                    Text("${draftWorkBoostPct}%", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                }
                CircleStepperButton(icon = Icons.Filled.Add, contentDesc = "Increase Work Boost") {
                    draftWorkBoostPct = (draftWorkBoostPct + 10).coerceIn(0, 100)
                }
            }
        }

        OwnerToggleRow(
            icon = Icons.Filled.RocketLaunch,
            iconTint = SaffronFlame,
            title = "Home Page Boost",
            subtitle = "Prioritize boosted creators in Home recommendations",
            checked = draftHomeBoost,
            testTag = "boost_home_switch",
            onCheckedChange = { draftHomeBoost = it }
        )
        OwnerToggleRow(
            icon = Icons.Filled.RocketLaunch,
            iconTint = EmeraldVerified,
            title = "Trending Boost",
            subtitle = "Boost promoted creators in Global & Regional Explore",
            checked = draftTrendingBoost,
            testTag = "boost_trending_switch",
            onCheckedChange = { draftTrendingBoost = it }
        )
        OwnerToggleRow(
            icon = Icons.Filled.RocketLaunch,
            iconTint = ElectricCobalt,
            title = "Search Boost",
            subtitle = "Elevate boosted creator videos in Search results",
            checked = draftSearchBoost,
            testTag = "boost_search_switch",
            onCheckedChange = { draftSearchBoost = it }
        )

        Button(
            onClick = {
                onUpdateSettings(
                    settings.copy(
                        dailyVideoUploadLimit = draftVideoLimit,
                        dailyShortsUploadLimit = draftShortsLimit,
                        dailyLiveLimit = draftLiveLimit,
                        workBoostPercent = draftWorkBoostPct,
                        homePageBoostEnabled = draftHomeBoost,
                        trendingBoostEnabled = draftTrendingBoost,
                        searchBoostEnabled = draftSearchBoost
                    ),
                    "SAVE_WORK_BOOST",
                    "Limits: V=$draftVideoLimit S=$draftShortsLimit L=$draftLiveLimit | Boost=$draftWorkBoostPct%",
                    "V=${settings.dailyVideoUploadLimit},S=${settings.dailyShortsUploadLimit}",
                    "V=$draftVideoLimit,S=$draftShortsLimit,L=$draftLiveLimit,Boost=$draftWorkBoostPct%"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_work_boost_button")
        ) {
            Text("Save Work Limits & Boost", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LimitSliderRow(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text("$value / day", style = MaterialTheme.typography.labelLarge, color = SaffronFlame, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = SaffronFlame, activeTrackColor = SaffronFlame)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OwnerUserControlCard(
    target: UserProfileEntity,
    channels: List<ChannelEntity>,
    videos: List<VideoEntity>,
    settings: PlatformSettingsEntity,
    onBanToggle: (Boolean) -> Unit,
    onWarn: (String) -> Unit,
    onBlockToggle: (Boolean) -> Unit,
    onLimitToggle: (Boolean) -> Unit,
    onBoostChange: (Int) -> Unit
) {
    val ownedChannelIds = remember(channels, target.uid) {
        channels.filter { it.ownerUserId == target.uid }.map { it.id }.toSet()
    }
    val userVideos = remember(videos, ownedChannelIds) {
        videos.filter { it.channelId in ownedChannelIds }
    }
    val totalViews = remember(userVideos) { userVideos.sumOf { it.viewCount } }
    val totalNetRev = remember(userVideos) { userVideos.sumOf { it.netRevenueInr } }
    val estimatedEarnings = BharatTubeCatalog.calculateCreatorEarningsInr(
        viewCount = totalViews,
        netRevenueInr = totalNetRev,
        creatorSharePct = settings.creatorSharePct,
        per1000ViewsPaymentInr = settings.per1000ViewsPaymentInr,
        bonusPercent = settings.bonusPercent,
        userBoostPct = target.boostPercent
    )

    var showWarnDialog by remember { mutableStateOf(false) }
    var showBoostDialog by remember { mutableStateOf(false) }

    OwnerControlCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChannelLogoAvatar(
                avatarUri = target.avatarUri,
                contentDescription = target.username,
                size = 46.dp,
                borderWidth = 1.5.dp,
                borderColor = if (target.isBanned) LotusCoral else SaffronFlame
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.username + if (target.isAdmin) " (Owner)" else "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${target.handle} • ${BharatTubeCatalog.formatCompactCount(totalViews)} views • ₹${String.format("%,.0f", estimatedEarnings)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldVerified,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active status badges
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (target.isBanned) StatusBadge("BANNED", LotusCoral)
            if (target.commentRestricted) StatusBadge("BLOCKED", LotusCoral)
            if (target.workLimited) StatusBadge("LIMITED", AmberGold)
            if (target.warningCount > 0) StatusBadge("WARNED (${target.warningCount})", AmberGold)
            if (target.boostPercent > 0) StatusBadge("BOOST +${target.boostPercent}%", EmeraldVerified)
            if (!target.isBanned && !target.commentRestricted && !target.workLimited && target.boostPercent == 0) {
                StatusBadge("ACTIVE", Color(0xFF8B91A6))
            }
        }

        // Control Actions: Ban/Unban, Boost, Warn, Block, Limit
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserActionChip(
                label = if (target.boostPercent > 0) "Boost (${target.boostPercent}%)" else "Boost",
                tint = EmeraldVerified,
                onClick = { showBoostDialog = true }
            )
            if (!target.isAdmin) {
                UserActionChip(
                    label = if (target.isBanned) "Unban" else "Ban",
                    tint = if (target.isBanned) EmeraldVerified else LotusCoral,
                    onClick = { onBanToggle(!target.isBanned) }
                )
                UserActionChip(
                    label = "Warn",
                    tint = AmberGold,
                    onClick = { showWarnDialog = true }
                )
                UserActionChip(
                    label = if (target.commentRestricted) "Unblock" else "Block",
                    tint = if (target.commentRestricted) EmeraldVerified else LotusCoral,
                    onClick = { onBlockToggle(!target.commentRestricted) }
                )
                UserActionChip(
                    label = if (target.workLimited) "Unlimit" else "Limit",
                    tint = if (target.workLimited) EmeraldVerified else AmberGold,
                    onClick = { onLimitToggle(!target.workLimited) }
                )
            }
        }
    }

    if (showWarnDialog) {
        var reason by remember { mutableStateOf("Please follow BharatTube creator & upload policy.") }
        AlertDialog(
            onDismissRequest = { showWarnDialog = false },
            title = { Text("Warn ${target.handle}") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Warning Reason") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onWarn(reason)
                        showWarnDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) { Text("Send Warning", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showWarnDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBoostDialog) {
        var boostVal by remember(target.boostPercent) { mutableIntStateOf(if (target.boostPercent > 0) target.boostPercent else 40) }
        AlertDialog(
            onDismissRequest = { showBoostDialog = false },
            title = { Text("Boost ${target.handle}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Visibility & Earnings Multiplier: +${boostVal}%", color = EmeraldVerified, fontWeight = FontWeight.Bold)
                    Slider(
                        value = boostVal.toFloat(),
                        onValueChange = { boostVal = it.roundToInt() },
                        valueRange = 0f..100f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBoostChange(boostVal)
                        showBoostDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                ) { Text("Apply Boost") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onBoostChange(0)
                    showBoostDialog = false
                }) { Text("Clear (0%)") }
            }
        )
    }
}

@Composable
internal fun OwnerPayoutHistoryCard(
    payout: PayoutEntity,
    ownerName: String,
    onTogglePaid: (PayoutStatus) -> Unit
) {
    val isPaid = payout.status == PayoutStatus.CONFIRMED.name
    val dateStr = remember(payout.createdAt) {
        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(payout.createdAt))
    }
    val methodLabel = when (payout.method) {
        "UPI" -> "UPI"
        "BANK_TRANSFER" -> "Bank Account"
        else -> payout.method
    }

    OwnerControlCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ownerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9AA1B6))
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isPaid) EmeraldVerified.copy(alpha = 0.16f) else AmberGold.copy(alpha = 0.16f)
            ) {
                Text(
                    text = if (isPaid) "Paid" else "Pending",
                    color = if (isPaid) EmeraldVerified else AmberGold,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "₹${String.format("%,.2f", payout.amountInr)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaffronFlame
                )
                Text(
                    text = "$methodLabel • ${payout.destinationMasked}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD0D5E6)
                )
            }
            OutlinedButton(
                onClick = {
                    onTogglePaid(if (isPaid) PayoutStatus.INITIATED else PayoutStatus.CONFIRMED)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isPaid) {
                    Text("Mark Pending", color = AmberGold, style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mark Paid", color = EmeraldVerified, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CircleStepperButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF232634),
        border = BorderStroke(1.dp, Color(0xFF34394E)),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDesc, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun StatusBadge(label: String, tint: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = tint.copy(alpha = 0.16f)) {
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun UserActionChip(
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
    ) {
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
