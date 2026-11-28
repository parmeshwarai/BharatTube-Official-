package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.CommentEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeVideoPlayer
import com.example.ui.components.VerifiedChannelBadge
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsScreen(
    shorts: List<VideoEntity>,
    initialIndex: Int,
    channels: List<ChannelEntity>,
    settings: PlatformSettingsEntity,
    observeComments: (String) -> Flow<List<CommentEntity>>,
    onRecordPlay: (VideoEntity) -> Unit,
    onToggleLike: (VideoEntity) -> Unit,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onAddComment: (String, String) -> Unit,
    onOpenChannel: (String) -> Unit,
    currentUser: UserProfileEntity? = null,
    onPinComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> },
    onHideComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> },
    onDeleteComment: (VideoEntity, CommentEntity) -> Unit = { _, _ -> },
    onSubmitReport: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    if (!settings.shortsEnabled) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Shorts are currently disabled by Super Admin.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    if (shorts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Shorts published yet. Upload a Short in the Upload tab!", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, shorts.lastIndex)) { shorts.size }
    var commentSheetVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var analyticsShort by remember { mutableStateOf<VideoEntity?>(null) }
    var reportShort by remember { mutableStateOf<VideoEntity?>(null) }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val short = shorts[page]
        val channel = channels.firstOrNull { it.id == short.channelId }
        val isCurrentPage = pagerState.currentPage == page

        LaunchedEffect(isCurrentPage, short.id) {
            if (isCurrentPage) onRecordPlay(short)
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (isCurrentPage) {
                BharatTubeVideoPlayer(
                    videoUri = short.videoUri,
                    initialPositionMs = 0L,
                    captionsText = short.captionsText,
                    qualityLabel = short.playbackQuality,
                    isShortVertical = true,
                    onSavePosition = {},
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Color(0xCC000000))
                        )
                    )
            )

            // Right Vertical Action Rail
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onToggleLike(short) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("short_like_${short.id}")
                    ) {
                        Icon(
                            imageVector = if (short.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like Short",
                            tint = if (short.isLiked) LotusCoral else Color.White
                        )
                    }
                    Text(BharatTubeCatalog.formatCompactCount(short.likeCount), color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { commentSheetVideo = short },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("short_comment_${short.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments", tint = Color.White)
                    }
                    Text("Comments", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "Watch '${short.title}' by ${short.channelName} on BharatTube!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Short"))
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Short", tint = Color.White)
                    }
                    Text("Share", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = { analyticsShort = short },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Filled.Analytics, contentDescription = "Shorts Analytics", tint = Color.White)
                }

                IconButton(
                    onClick = { reportShort = short },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("short_report_${short.id}")
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = "Report Short", tint = Color.White)
                }
            }

            // Bottom Creator Info & Caption Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, end = 84.dp, bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = short.channelHandle,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenChannel(short.channelId) }
                    )
                    if (channel?.isVerified == true) {
                        Spacer(Modifier.width(4.dp))
                        VerifiedChannelBadge(isVerified = true, size = 14.dp)
                    }
                    Spacer(Modifier.width(10.dp))
                    if (channel != null) {
                        Surface(
                            onClick = { onToggleSubscribe(channel) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (channel.isSubscribedByMe) Color.White.copy(alpha = 0.25f) else SaffronFlame
                        ) {
                            Text(
                                text = if (channel.isSubscribedByMe) "Following" else "Follow",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(short.title, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(short.hashtags, color = SaffronFlame, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(short.musicAttribution, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    // Comments Sheet with Chhotu AI Guard + Owner/Creator Pin/Hide/Delete
    commentSheetVideo?.let { targetVideo ->
        val comments by observeComments(targetVideo.id).collectAsStateWithLifecycle(initialValue = emptyList())
        val ch = channels.firstOrNull { it.id == targetVideo.channelId }
        val canModerate = currentUser != null && (currentUser.isAdmin || ch?.ownerUserId == currentUser.uid)
        val visibleComments = if (canModerate) comments else comments.filter { !it.isHidden }
        var draft by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { commentSheetVideo = null }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Comments (${visibleComments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Chhotu AI Guard Active", style = MaterialTheme.typography.labelSmall, color = SaffronFlame)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Add a respectful comment...") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (draft.isNotBlank()) {
                            onAddComment(targetVideo.id, draft)
                            draft = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post comment", tint = SaffronFlame)
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleComments, key = { it.id }) { comment ->
                        val canDelete = canModerate || (currentUser != null && comment.authorUserId == currentUser.uid)
                        var showMenu by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    if (comment.isPinned) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.Filled.PushPin, contentDescription = "Pinned", tint = SaffronFlame, modifier = Modifier.size(12.dp))
                                        Text("Pinned", color = SaffronFlame, style = MaterialTheme.typography.labelSmall)
                                    }
                                    if (comment.isHidden) {
                                        Spacer(Modifier.width(6.dp))
                                        Text("Hidden", color = LotusCoral, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (canModerate || canDelete) {
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "Moderate comment")
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        if (canModerate) {
                                            DropdownMenuItem(
                                                text = { Text(if (comment.isPinned) "Unpin" else "Pin") },
                                                onClick = {
                                                    showMenu = false
                                                    onPinComment(targetVideo, comment)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (comment.isHidden) "Unhide" else "Hide") },
                                                onClick = {
                                                    showMenu = false
                                                    onHideComment(targetVideo, comment)
                                                }
                                            )
                                        }
                                        if (canDelete) {
                                            DropdownMenuItem(
                                                text = { Text("Delete", color = LotusCoral) },
                                                onClick = {
                                                    showMenu = false
                                                    onDeleteComment(targetVideo, comment)
                                                }
                                            )
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

    // Shorts Analytics Sheet
    analyticsShort?.let { short ->
        ModalBottomSheet(onDismissRequest = { analyticsShort = null }) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Shorts Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(short.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Total Views: ${BharatTubeCatalog.formatCompactCount(short.viewCount)}")
                Text("Total Likes: ${BharatTubeCatalog.formatCompactCount(short.likeCount)}")
                Text("Licensed Audio: ${short.musicAttribution}")
                TextButton(onClick = { analyticsShort = null }, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }

    // Report Short Dialog
    reportShort?.let { target ->
        var selectedReason by remember { mutableStateOf("Harassment / Safety") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportShort = null },
            title = { Text("Report Short") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Copyright Infringement", "Misleading or Spam", "Harassment / Safety", "Age-Inappropriate Content").forEach { reason ->
                        Text(
                            text = if (selectedReason == reason) "● $reason" else "○ $reason",
                            color = if (selectedReason == reason) SaffronFlame else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        )
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Additional details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSubmitReport("SHORT", target.id, target.title, selectedReason, details)
                    reportShort = null
                }) { Text("Submit Report") }
            },
            dismissButton = {
                TextButton(onClick = { reportShort = null }) { Text("Cancel") }
            }
        )
    }
}
