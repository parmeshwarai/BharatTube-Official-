package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeVideoPlayer
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    observeVideo: (String) -> Flow<VideoEntity?>,
    observeComments: (String) -> Flow<List<CommentEntity>>,
    allVideos: List<VideoEntity>,
    channels: List<ChannelEntity>,
    playlists: List<PlaylistEntity>,
    settings: PlatformSettingsEntity,
    userLanguage: String,
    onBack: () -> Unit,
    onRecordPlay: (VideoEntity) -> Unit,
    onSavePosition: (String, Long) -> Unit,
    onSelectQuality: (String, String) -> Unit,
    onToggleLike: (VideoEntity) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onToggleBell: (ChannelEntity) -> Unit,
    onAddComment: (String, String) -> Unit,
    onLikeComment: (String) -> Unit,
    onAddToPlaylist: (String, String) -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onSubmitReport: (String, String, String, String, String) -> Unit,
    onEnterPip: () -> Unit
) {
    val video by observeVideo(videoId).collectAsStateWithLifecycle(initialValue = null)
    val comments by observeComments(videoId).collectAsStateWithLifecycle(initialValue = emptyList())
    val currentVideo = video

    if (currentVideo == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Loading Video...") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Video unavailable")
            }
        }
        return
    }

    val context = LocalContext.current
    val channel = channels.firstOrNull { it.id == currentVideo.channelId }
    val upNext = remember(allVideos, currentVideo.id) {
        allVideos.filter { it.id != currentVideo.id && !it.isLive }.take(6)
    }

    var isFullscreen by remember { mutableStateOf(false) }
    var autoPlayNext by remember { mutableStateOf(true) }
    var commentDraft by remember { mutableStateOf("") }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Copyright Infringement") }
    var reportDetails by remember { mutableStateOf("") }
    var translatedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(currentVideo.id) {
        onRecordPlay(currentVideo)
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = { Text(currentVideo.channelName, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("player_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullscreen) PaddingValues(0.dp) else padding)
        ) {
            BharatTubeVideoPlayer(
                videoUri = currentVideo.videoUri,
                initialPositionMs = currentVideo.lastPositionMs,
                captionsText = currentVideo.captionsText,
                qualityLabel = currentVideo.playbackQuality,
                autoPlayNext = autoPlayNext,
                onSavePosition = { pos -> onSavePosition(currentVideo.id, pos) },
                onVideoEnded = {
                    if (autoPlayNext) upNext.firstOrNull()?.let { onOpenVideo(it) }
                },
                onSelectQuality = { q -> onSelectQuality(currentVideo.id, q) },
                onEnterPip = onEnterPip,
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                modifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                }
            )

            if (!isFullscreen) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Pre-roll / Mid-roll Ad Gate Status
                    if (settings.advertisementsEnabled && currentVideo.adEligible && (settings.preRollAdsEnabled || settings.midRollAdsEnabled)) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Campaign, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (settings.metaAudienceNetworkEnabled) {
                                            "Ad Slot: Meta Audience Network (${if (settings.metaTestMode) "Test Mode" else "Live"}) • 50/50 Creator Split"
                                        } else {
                                            "Ad Slot Ready (Pre-Roll/Mid-Roll) — Connect Meta Audience Network Placement IDs in Admin."
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Title & Stats
                    item {
                        Column {
                            Text(currentVideo.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${BharatTubeCatalog.formatCompactCount(currentVideo.viewCount)} views • ${currentVideo.category} • ${currentVideo.languageCode.uppercase()} • ${currentVideo.hashtags}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentVideo.chaptersSummary.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        "Chapters: ${currentVideo.chaptersSummary}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action Pills
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Surface(
                                    onClick = { onToggleLike(currentVideo) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (currentVideo.isLiked) SaffronFlame.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.testTag("player_like_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (currentVideo.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = "Like",
                                            tint = if (currentVideo.isLiked) SaffronFlame else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(BharatTubeCatalog.formatCompactCount(currentVideo.likeCount), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            item {
                                Surface(
                                    onClick = { onToggleWatchLater(currentVideo) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (currentVideo.isWatchLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Watch Later",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (currentVideo.isWatchLater) "Saved" else "Watch Later", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            item {
                                Surface(
                                    onClick = { showPlaylistDialog = true },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Save to playlist", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Playlist", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            item {
                                Surface(
                                    onClick = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_TEXT, "Watch '${currentVideo.title}' on BharatTube!")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share Video"))
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Share", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                            item {
                                Surface(
                                    onClick = { showReportDialog = true },
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Flag, contentDescription = "Report", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Report", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }

                    // Channel Row
                    if (channel != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { onOpenChannel(channel.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(channel.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(channel.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${BharatTubeCatalog.formatCompactCount(channel.subscriberCount)} subscribers",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (channel.isSubscribedByMe) {
                                        IconButton(onClick = { onToggleBell(channel) }) {
                                            Icon(
                                                if (channel.notificationsEnabled) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                                                contentDescription = "Channel notifications",
                                                tint = SaffronFlame
                                            )
                                        }
                                        OutlinedButton(onClick = { onToggleSubscribe(channel) }) {
                                            Text("Subscribed")
                                        }
                                    } else {
                                        Button(
                                            onClick = { onToggleSubscribe(channel) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                                        ) {
                                            Text("Subscribe")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Description + Autoplay Next
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(currentVideo.description, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Auto-play next video", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                                Switch(checked = autoPlayNext, onCheckedChange = { autoPlayNext = it })
                            }
                        }
                    }

                    // Comments Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Comments (${comments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (settings.commentsEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = commentDraft,
                                        onValueChange = { commentDraft = it },
                                        placeholder = { Text("Write a respectful comment...") },
                                        modifier = Modifier.weight(1f).testTag("player_comment_input")
                                    )
                                    IconButton(
                                        onClick = {
                                            if (commentDraft.isNotBlank()) {
                                                onAddComment(currentVideo.id, commentDraft)
                                                commentDraft = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("player_comment_send")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post comment", tint = SaffronFlame)
                                    }
                                }
                            } else {
                                Text("Comments are turned off globally by Super Admin.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    items(comments, key = { it.id }) { comment ->
                        val isTranslated = comment.id in translatedIds
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(comment.originalLanguage.uppercase(), style = MaterialTheme.typography.labelSmall, color = SaffronFlame)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isTranslated && comment.originalLanguage != userLanguage) {
                                        "[${userLanguage.uppercase()} preview] ${comment.text}"
                                    } else {
                                        comment.text
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { onLikeComment(comment.id) }) {
                                        Text("Like (${comment.likeCount})")
                                    }
                                    if (comment.originalLanguage != userLanguage) {
                                        TextButton(onClick = {
                                            translatedIds = if (isTranslated) translatedIds - comment.id else translatedIds + comment.id
                                        }) {
                                            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isTranslated) "Show Original" else "Translate to ${userLanguage.uppercase()}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Up Next
                    item {
                        Text("Up Next", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(upNext, key = { it.id }) { next ->
                        VideoFeedCard(
                            video = next,
                            onClick = { onOpenVideo(next) },
                            onChannelClick = { onOpenChannel(next.channelId) },
                            onToggleWatchLater = { onToggleWatchLater(next) }
                        )
                    }
                }
            }
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Save to Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (playlists.isEmpty()) {
                        Text("Create a playlist in Library first.")
                    } else {
                        playlists.forEach { pl ->
                            OutlinedButton(
                                onClick = {
                                    onAddToPlaylist(pl.id, currentVideo.id)
                                    showPlaylistDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(pl.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Close") }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Video / Copyright") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Copyright Infringement", "Misleading or Spam", "Harassment / Safety", "Age-Inappropriate Content").forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (reportReason == reason) "● $reason" else "○ $reason",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (reportReason == reason) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Details or timestamp") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSubmitReport("VIDEO", currentVideo.id, currentVideo.title, reportReason, reportDetails)
                    showReportDialog = false
                    reportDetails = ""
                }) { Text("Submit Report") }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}
