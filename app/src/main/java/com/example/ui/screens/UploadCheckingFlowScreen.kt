package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chhotuai.CheckItemStatus
import com.example.chhotuai.ChhotuCheckDetail
import com.example.chhotuai.ChhotuUploadModerator
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCheckingFlowScreen(
    videoId: String,
    observeVideo: (String) -> Flow<VideoEntity?>,
    strictModeEnabled: Boolean,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onFixAndReupload: (VideoEntity) -> Unit,
    onOpenYourVideos: () -> Unit
) {
    val video by observeVideo(videoId).collectAsStateWithLifecycle(initialValue = null)
    val checkStatus = video?.chhotuCheckStatus ?: "CHECKING"

    // Animate progressive scan index while status == "CHECKING"
    var scanProgressIndex by remember(videoId, checkStatus) { mutableIntStateOf(0) }
    LaunchedEffect(videoId, checkStatus) {
        if (checkStatus == "CHECKING") {
            for (i in 0..4) {
                scanProgressIndex = i
                delay(340L)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chhotu_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val checkItems: List<ChhotuCheckDetail> = remember(video?.chhotuCheckSummary, checkStatus, scanProgressIndex) {
        if (checkStatus == "CHECKING") {
            val labels = listOf(
                "COPYRIGHT" to "Copyright Check",
                "COMMUNITY" to "Community Guidelines Check",
                "SAFETY" to "Ganda / Galat Content Check",
                "QUALITY" to "Quality Check",
                "TITLE_THUMB" to "Title / Thumbnail Check"
            )
            labels.mapIndexed { idx, (key, title) ->
                val itemStatus = when {
                    idx < scanProgressIndex -> CheckItemStatus.PASSED
                    idx == scanProgressIndex -> CheckItemStatus.CHECKING
                    else -> CheckItemStatus.PENDING
                }
                val note = when (itemStatus) {
                    CheckItemStatus.PASSED -> "Verified clean"
                    CheckItemStatus.CHECKING -> "Chhotu AI scanning now..."
                    else -> "Waiting in queue..."
                }
                ChhotuCheckDetail(key = key, title = title, status = itemStatus, note = note)
            }
        } else {
            ChhotuUploadModerator.parseSummary(video?.chhotuCheckSummary.orEmpty())
        }
    }

    val isChecking = checkStatus == "CHECKING"
    val isApproved = checkStatus == "APPROVED"
    val isRejected = checkStatus == "REJECTED"

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BharatTube",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Chhotu AI Upload Check",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaffronFlame
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("upload_check_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Upload Checking Flow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "by Chhotu AI • ${if (video?.chhotuCheckMode == "STRICT" || strictModeEnabled) "Strict Mode" else "Normal Mode"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldVerified
                    )
                }
            }

            // 3-Step Flow Stepper (Ref WA5971)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepPill(
                            stepLabel = "Step 1",
                            title = "Upload Video",
                            sub = "Completed",
                            color = EmeraldVerified,
                            done = true,
                            active = false
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (isChecking) AmberGold else EmeraldVerified)
                        )
                        StepPill(
                            stepLabel = "Step 2",
                            title = "Chhotu AI",
                            sub = if (isChecking) "Checking..." else "Completed",
                            color = if (isChecking) AmberGold else EmeraldVerified,
                            done = !isChecking,
                            active = isChecking
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    when {
                                        isApproved -> EmeraldVerified
                                        isRejected -> LotusCoral
                                        else -> Color(0xFF2D3346)
                                    }
                                )
                        )
                        StepPill(
                            stepLabel = "Step 3",
                            title = if (isRejected) "Rejected" else "Approved",
                            sub = when {
                                isApproved -> "Live on Home"
                                isRejected -> "Fix Needed"
                                else -> "Waiting"
                            },
                            color = when {
                                isApproved -> EmeraldVerified
                                isRejected -> LotusCoral
                                else -> Color(0xFF6B7280)
                            },
                            done = isApproved,
                            active = isRejected
                        )
                    }
                }
            }

            // Hero Status Card (Chhotu AI Mascot + Approved / Checking / Rejected)
            item {
                val statusBorder = when {
                    isApproved -> EmeraldVerified.copy(alpha = 0.55f)
                    isRejected -> LotusCoral.copy(alpha = 0.65f)
                    else -> AmberGold.copy(alpha = 0.55f)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.2.dp, statusBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chhotu_check_status_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .scale(if (isChecking) pulseScale else 1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1F2638), Color(0xFF111521))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BharatTubeLogoMark(modifier = Modifier.size(30.dp))
                                Spacer(Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.SmartToy,
                                    contentDescription = "Chhotu AI",
                                    tint = when {
                                        isApproved -> EmeraldVerified
                                        isRejected -> LotusCoral
                                        else -> SaffronFlame
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when {
                                    isChecking -> CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = AmberGold
                                    )
                                    isApproved -> Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldVerified,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    else -> Icon(
                                        Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = LotusCoral,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        isChecking -> "Checking..."
                                        isApproved -> "Approved!"
                                        else -> "Rejected!"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isChecking -> AmberGold
                                        isApproved -> EmeraldVerified
                                        else -> LotusCoral
                                    }
                                )
                            }
                            Text(
                                text = when {
                                    isChecking -> "Video is in 'Checking...' queue and hidden from Home Page until Chhotu AI finishes all 5 checks."
                                    isApproved -> "Your video passed all Chhotu AI checks and is now published on the BharatTube Home Page!"
                                    else -> "Your video was NOT published to the Home Page. Review the issues below, fix them, and re-upload."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD5D9E6)
                            )
                            video?.let { v ->
                                Text(
                                    text = v.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SaffronFlame,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Rejection Reason Box if Rejected
            if (isRejected && !video?.chhotuRejectionReason.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LotusCoral.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, LotusCoral.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chhotu_rejection_reason_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = LotusCoral)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Rejection Reason & How to Fix",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LotusCoral
                                )
                            }
                            Text(
                                text = video?.chhotuRejectionReason.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 5 Chhotu AI Checks List
            item {
                Text(
                    text = "Chhotu AI Checks (5-Point Safety & Quality Review)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            itemsIndexed(checkItems, key = { _, item -> item.key }) { _, item ->
                val badgeBg = when (item.status) {
                    CheckItemStatus.PASSED -> EmeraldVerified.copy(alpha = 0.2f)
                    CheckItemStatus.FAILED -> LotusCoral.copy(alpha = 0.22f)
                    CheckItemStatus.CHECKING -> AmberGold.copy(alpha = 0.22f)
                    CheckItemStatus.PENDING -> Color(0xFF232736)
                }
                val badgeFg = when (item.status) {
                    CheckItemStatus.PASSED -> EmeraldVerified
                    CheckItemStatus.FAILED -> LotusCoral
                    CheckItemStatus.CHECKING -> AmberGold
                    CheckItemStatus.PENDING -> Color(0xFF9AA1B6)
                }
                val badgeLabel = when (item.status) {
                    CheckItemStatus.PASSED -> "Passed"
                    CheckItemStatus.FAILED -> "Failed"
                    CheckItemStatus.CHECKING -> "Checking"
                    CheckItemStatus.PENDING -> "Pending"
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (item.status) {
                            CheckItemStatus.PASSED -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldVerified)
                            CheckItemStatus.FAILED -> Icon(Icons.Filled.Cancel, contentDescription = null, tint = LotusCoral)
                            CheckItemStatus.CHECKING -> Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = AmberGold)
                            CheckItemStatus.PENDING -> Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = Color(0xFF6B7280))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = item.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0A5B8)
                            )
                        }
                        Surface(shape = RoundedCornerShape(50), color = badgeBg) {
                            Text(
                                text = badgeLabel,
                                color = badgeFg,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Status Strip (Checking | Approved | Rejected)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusIndicatorChip("Checking", isChecking, AmberGold)
                            StatusIndicatorChip("Approved", isApproved, EmeraldVerified)
                            StatusIndicatorChip("Rejected", isRejected, LotusCoral)
                        }
                        Text(
                            text = "Current Status: ${checkStatus.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFA0A5B8)
                        )
                    }
                }
            }

            // Primary Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isApproved) {
                        Button(
                            onClick = onGoHome,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldVerified),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("chhotu_check_go_home_button")
                        ) {
                            Text("Go to Home Page", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    } else if (isRejected && video != null) {
                        Button(
                            onClick = { onFixAndReupload(video!!) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("chhotu_check_fix_reupload_button")
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("Fix & Re-Upload Video", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF252938)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Chhotu AI Checking in Progress...", color = AmberGold, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenYourVideos,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("View in Your Videos", color = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StepPill(
    stepLabel: String,
    title: String,
    sub: String,
    color: Color,
    done: Boolean,
    active: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(82.dp)) {
        Text(stepLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6))
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done || active) color.copy(alpha = 0.22f) else Color(0xFF232736)),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            } else if (active) {
                Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            } else {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6), maxLines = 1)
    }
}

@Composable
private fun StatusIndicatorChip(label: String, active: Boolean, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) color.copy(alpha = 0.22f) else Color(0xFF1D2130),
        border = BorderStroke(1.dp, if (active) color else Color(0xFF2B3042))
    ) {
        Text(
            text = "● $label",
            color = if (active) color else Color(0xFF7D859C),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
