package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.CommentEntity
import com.example.data.model.LabelShare
import com.example.data.model.VideoEntity
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ProfileFormatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun StudioKpiTile(
    icon: ImageVector,
    label: String,
    value: String,
    deltaText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, Color(0xFF252938)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8), maxLines = 1)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(deltaText, style = MaterialTheme.typography.labelSmall, color = EmeraldVerified, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun StudioVideoManageCard(
    video: VideoEntity,
    viewsText: String,
    earningsText: String,
    commentCount: Int,
    onEdit: () -> Unit,
    onAnalytics: () -> Unit,
    onComments: () -> Unit,
    onDelete: () -> Unit,
    onOpenChhotuCheck: () -> Unit
) {
    val statusLabel: String
    val statusColor: Color
    when {
        video.visibility == VideoVisibility.DRAFT.name -> {
            statusLabel = "Draft"
            statusColor = Color(0xFFA0A5B8)
        }
        video.chhotuCheckStatus == "REJECTED" -> {
            statusLabel = "Rejected"
            statusColor = LotusCoral
        }
        video.chhotuCheckStatus != "APPROVED" -> {
            statusLabel = "Checking by Chhotu AI"
            statusColor = AmberGold
        }
        video.visibility == VideoVisibility.SCHEDULED.name && video.scheduledAtEpochMs > System.currentTimeMillis() -> {
            val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(video.scheduledAtEpochMs))
            statusLabel = "Scheduled • $fmt"
            statusColor = AmberGold
        }
        video.visibility == VideoVisibility.PRIVATE.name -> {
            statusLabel = "Private"
            statusColor = Color(0xFFA0A5B8)
        }
        video.visibility == VideoVisibility.UNLISTED.name -> {
            statusLabel = "Unlisted"
            statusColor = AmberGold
        }
        else -> {
            statusLabel = "Published"
            statusColor = EmeraldVerified
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, Color(0xFF252938)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_video_card_${video.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(114.dp)
                        .height(66.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    BharatTubeThumbnail(
                        thumbnailKey = video.thumbnailKey,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                    ) {
                        Text(
                            text = BharatTubeCatalog.formatDuration(video.durationSeconds),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$viewsText • $earningsText • ${ProfileFormatters.formatRelativeDate(video.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA0A5B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.16f),
                        modifier = Modifier.clickable { onOpenChhotuCheck() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            if (video.chhotuCheckStatus != "APPROVED") {
                                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(statusLabel, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (video.chhotuCheckStatus == "REJECTED" && video.chhotuRejectionReason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LotusCoral.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, LotusCoral.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenChhotuCheck() }
                ) {
                    Text(
                        text = "Chhotu AI Reason: ${video.chhotuRejectionReason}",
                        color = LotusCoral,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StudioActionChipButton(
                    icon = Icons.Filled.Edit,
                    label = "Edit",
                    tint = Color.White,
                    tag = "studio_edit_btn_${video.id}",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                StudioActionChipButton(
                    icon = Icons.Filled.BarChart,
                    label = "Analytics",
                    tint = SaffronFlame,
                    tag = "video_analytics_btn_${video.id}",
                    onClick = onAnalytics,
                    modifier = Modifier.weight(1f)
                )
                StudioActionChipButton(
                    icon = Icons.Filled.ChatBubbleOutline,
                    label = "Comments ($commentCount)",
                    tint = ElectricOrWhite(commentCount > 0),
                    tag = "studio_comments_btn_${video.id}",
                    onClick = onComments,
                    modifier = Modifier.weight(1.25f)
                )
                StudioActionChipButton(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    tint = LotusCoral,
                    tag = "studio_delete_btn_${video.id}",
                    onClick = onDelete,
                    modifier = Modifier.weight(0.95f)
                )
            }
        }
    }
}

private fun ElectricOrWhite(highlighted: Boolean): Color =
    if (highlighted) EmeraldVerified else Color(0xFFB5BACB)

@Composable
private fun StudioActionChipButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1D2130),
        border = BorderStroke(1.dp, Color(0xFF2B3042)),
        modifier = modifier.height(34.dp).testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                color = tint,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun StudioCommentModerationCard(
    comment: CommentEntity,
    videoTitle: String,
    onReply: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, if (comment.isPinned) SaffronFlame.copy(alpha = 0.5f) else Color(0xFF252938)),
        modifier = Modifier.fillMaxWidth().testTag("studio_comment_card_${comment.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (comment.isPinned) {
                    Surface(shape = RoundedCornerShape(50), color = SaffronFlame.copy(alpha = 0.2f)) {
                        Text("Pinned", color = SaffronFlame, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                }
                if (comment.isHidden) {
                    Surface(shape = RoundedCornerShape(50), color = LotusCoral.copy(alpha = 0.2f)) {
                        Text("Hidden", color = LotusCoral, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                }
                Text(
                    text = ProfileFormatters.formatRelativeDate(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B91A6)
                )
            }
            Text(
                text = "On: $videoTitle",
                style = MaterialTheme.typography.labelSmall,
                color = SaffronFlame,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (comment.parentCommentId != null) {
                Text(
                    text = "Reply thread",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldVerified
                )
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (comment.isHidden) Color(0xFF8B91A6) else Color(0xFFE5E7EF)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onReply, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reply", color = SaffronFlame, style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onTogglePin, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = if (comment.isPinned) "Unpin" else "Pin",
                        tint = if (comment.isPinned) SaffronFlame else Color(0xFFA0A5B8),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onToggleHide, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (comment.isHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (comment.isHidden) "Unhide" else "Hide",
                        tint = if (comment.isHidden) AmberGold else Color(0xFFA0A5B8),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Comment",
                        tint = LotusCoral,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun StudioShareProgressRow(item: LabelShare, barColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.label, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (item.subtitle.isNotBlank()) "${item.percent}% (${item.subtitle})" else "${item.percent}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB5BACB),
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { (item.percent / 100f).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = Color(0xFF232736),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

@Composable
internal fun StudioEditVideoDialog(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        visibility: VideoVisibility,
        category: String,
        hashtags: String,
        thumbnailKey: String,
        scheduledAtEpochMs: Long
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember(video.id) { mutableStateOf(video.title) }
    var description by remember(video.id) { mutableStateOf(video.description) }
    var tags by remember(video.id) { mutableStateOf(video.hashtags) }
    var thumbnailKey by remember(video.id) { mutableStateOf(video.thumbnailKey) }
    var category by remember(video.id) { mutableStateOf(video.category) }
    val initialVis = remember(video.id) {
        runCatching { VideoVisibility.valueOf(video.visibility) }.getOrDefault(VideoVisibility.PUBLIC)
    }
    var isScheduled by remember(video.id) { mutableStateOf(initialVis == VideoVisibility.SCHEDULED) }
    var visibility by remember(video.id) {
        mutableStateOf(if (initialVis == VideoVisibility.SCHEDULED) VideoVisibility.PUBLIC else initialVis)
    }
    var scheduledTimeMs by remember(video.id) {
        mutableLongStateOf(
            if (video.scheduledAtEpochMs > System.currentTimeMillis()) video.scheduledAtEpochMs
            else System.currentTimeMillis() + 86_400_000L
        )
    }

    val thumbPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            thumbnailKey = uri.toString()
        }
    }

    val sampleThumbnails = listOf(
        "drawable:img_thumb_tech" to "Tech",
        "drawable:img_thumb_music" to "Music",
        "drawable:img_thumb_travel" to "Travel",
        "drawable:img_hero_creators" to "Studio"
    )
    val schedulePresets = listOf(
        "Tomorrow 6 PM" to (System.currentTimeMillis() + 86_400_000L),
        "In 2 Days" to (System.currentTimeMillis() + 86_400_000L * 2),
        "Next Week" to (System.currentTimeMillis() + 86_400_000L * 7)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151822),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Edit Video", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("studio_edit_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Thumbnail", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        BharatTubeThumbnail(
                            thumbnailKey = thumbnailKey,
                            contentDescription = "Thumbnail Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        border = BorderStroke(1.dp, SaffronFlame),
                        modifier = Modifier.testTag("studio_edit_change_thumbnail")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change", color = SaffronFlame)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sampleThumbnails) { (key, label) ->
                        FilterChip(
                            selected = thumbnailKey == key,
                            onClick = { thumbnailKey = key },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (e.g. #cooking #india)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BharatTubeCatalog.categories.filter { it != "All" }) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronFlame,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Text("Visibility", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        VideoVisibility.PUBLIC to "Public",
                        VideoVisibility.PRIVATE to "Private",
                        VideoVisibility.UNLISTED to "Unlisted"
                    ).forEach { (vis, label) ->
                        FilterChip(
                            selected = !isScheduled && visibility == vis,
                            onClick = {
                                isScheduled = false
                                visibility = vis
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronFlame,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Schedule Publish", style = MaterialTheme.typography.labelLarge)
                    }
                    Switch(checked = isScheduled, onCheckedChange = { isScheduled = it })
                }
                if (isScheduled) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(schedulePresets) { (label, epoch) ->
                            FilterChip(
                                selected = kotlin.math.abs(scheduledTimeMs - epoch) < 3_600_000L,
                                onClick = { scheduledTimeMs = epoch },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalVis = if (isScheduled) VideoVisibility.SCHEDULED else visibility
                    onSave(title, description, finalVis, category, tags, thumbnailKey, if (isScheduled) scheduledTimeMs else 0L)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                modifier = Modifier.testTag("studio_edit_save_button")
            ) { Text("Save", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFA0A5B8)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudioUploadBottomSheet(
    onDismiss: () -> Unit,
    onUploadVideo: () -> Unit,
    onCreateShort: () -> Unit,
    onGoLive: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1D1F27),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Upload from BharatTube Studio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Every upload is checked by Chhotu AI before appearing on BharatTube Home.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA0A5B8)
            )
            StudioUploadOptionRow(Icons.Filled.FileUpload, "Upload Video", "Long-form HD / 4K video", "studio_sheet_upload_video", onUploadVideo)
            StudioUploadOptionRow(Icons.Filled.AutoAwesome, "Create Short", "Vertical 60s Short", "studio_sheet_create_short", onCreateShort)
            StudioUploadOptionRow(Icons.Filled.Sensors, "Go Live", "Start live broadcast", "studio_sheet_go_live", onGoLive)
        }
    }
}

@Composable
private fun StudioUploadOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF252836))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(SaffronFlame.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = SaffronFlame, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
        }
    }
}
