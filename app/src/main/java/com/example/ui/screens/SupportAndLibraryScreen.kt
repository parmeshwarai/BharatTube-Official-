package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.SupportTicketEntity
import com.example.data.model.VideoEntity
import com.example.data.model.WatchHistoryEntity
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    videos: List<VideoEntity>,
    channels: List<ChannelEntity>,
    watchHistory: List<WatchHistoryEntity>,
    playlists: List<PlaylistEntity>,
    playlistRefs: List<PlaylistVideoCrossRef>,
    historyPaused: Boolean,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onToggleHistoryPaused: () -> Unit,
    onClearHistory: () -> Unit,
    onCreatePlaylist: (String, String, Boolean) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onRemoveFromPlaylist: (String, String) -> Unit = { _, _ -> },
    onReportVideo: (VideoEntity) -> Unit = {},
    onShareVideo: (VideoEntity) -> Unit = {}
) {
    var tab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 4)) }
    var newPlaylistName by remember { mutableStateOf("") }
    var expandedPlaylistId by remember { mutableStateOf<String?>(null) }

    val videoMap = remember(videos) { videos.associateBy { it.id } }
    val verifiedChannelIds = remember(channels) { channels.filter { it.isVerified }.map { it.id }.toSet() }
    val subscribedChannelIds = remember(channels) { channels.filter { it.isSubscribedByMe }.map { it.id }.toSet() }
    val subscriptionVideos = videos.filter { it.channelId in subscribedChannelIds }
    val historyVideos = watchHistory.mapNotNull { videoMap[it.videoId] }
    val watchLaterVideos = videos.filter { it.isWatchLater }
    val likedVideos = videos.filter { it.isLiked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library & Saved") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
                listOf("Subscriptions", "History", "Watch Later", "Liked Videos", "Playlists").forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        text = { Text(title, maxLines = 1) },
                        modifier = Modifier.testTag("library_tab_$i")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> {
                        if (subscriptionVideos.isEmpty()) {
                            item { Text("Subscribe to channels to see their latest videos and Shorts here.") }
                        } else {
                            items(subscriptionVideos, key = { it.id }) { v ->
                                VideoFeedCard(
                                    video = v,
                                    onClick = { onOpenVideo(v) },
                                    onChannelClick = { onOpenChannel(v.channelId) },
                                    onToggleWatchLater = { onToggleWatchLater(v) },
                                    isChannelVerified = v.channelId in verifiedChannelIds,
                                    onReportClick = { onReportVideo(v) },
                                    onShareClick = { onShareVideo(v) }
                                )
                            }
                        }
                    }
                    1 -> {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Pause Watch History", modifier = Modifier.weight(1f))
                                Switch(checked = historyPaused, onCheckedChange = { onToggleHistoryPaused() })
                            }
                            OutlinedButton(onClick = onClearHistory, modifier = Modifier.fillMaxWidth()) {
                                Text("Clear All Watch History")
                            }
                        }
                        if (historyVideos.isEmpty()) {
                            item { Text("Videos you watch on BharatTube appear here.") }
                        } else {
                            items(historyVideos, key = { it.id }) { v ->
                                VideoFeedCard(
                                    video = v,
                                    onClick = { onOpenVideo(v) },
                                    onChannelClick = { onOpenChannel(v.channelId) },
                                    onToggleWatchLater = { onToggleWatchLater(v) },
                                    isChannelVerified = v.channelId in verifiedChannelIds,
                                    onReportClick = { onReportVideo(v) },
                                    onShareClick = { onShareVideo(v) }
                                )
                            }
                        }
                    }
                    2 -> {
                        if (watchLaterVideos.isEmpty()) {
                            item { Text("Save any video to Watch Later from the player or feed menu.") }
                        } else {
                            items(watchLaterVideos, key = { it.id }) { v ->
                                VideoFeedCard(
                                    video = v,
                                    onClick = { onOpenVideo(v) },
                                    onChannelClick = { onOpenChannel(v.channelId) },
                                    onToggleWatchLater = { onToggleWatchLater(v) },
                                    isChannelVerified = v.channelId in verifiedChannelIds,
                                    onReportClick = { onReportVideo(v) },
                                    onShareClick = { onShareVideo(v) }
                                )
                            }
                        }
                    }
                    3 -> {
                        if (likedVideos.isEmpty()) {
                            item { Text("Videos and Shorts you Like will appear here.") }
                        } else {
                            items(likedVideos, key = { it.id }) { v ->
                                VideoFeedCard(
                                    video = v,
                                    onClick = { onOpenVideo(v) },
                                    onChannelClick = { onOpenChannel(v.channelId) },
                                    onToggleWatchLater = { onToggleWatchLater(v) },
                                    isChannelVerified = v.channelId in verifiedChannelIds,
                                    onReportClick = { onReportVideo(v) },
                                    onShareClick = { onShareVideo(v) }
                                )
                            }
                        }
                    }
                    else -> {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newPlaylistName,
                                    onValueChange = { newPlaylistName = it },
                                    label = { Text("New Playlist Name") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        onCreatePlaylist(newPlaylistName, "", false)
                                        newPlaylistName = ""
                                    },
                                    enabled = newPlaylistName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) { Text("Create") }
                            }
                        }
                        items(playlists, key = { it.id }) { pl ->
                            val plVideos = remember(playlistRefs, videoMap, pl.id) {
                                playlistRefs.filter { it.playlistId == pl.id }.mapNotNull { videoMap[it.videoId] }
                            }
                            val isExpanded = expandedPlaylistId == pl.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedPlaylistId = if (isExpanded) null else pl.id
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pl.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text("${plVideos.size} videos", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Icon(
                                            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = "Expand playlist"
                                        )
                                        IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete playlist", tint = LotusCoral)
                                        }
                                    }
                                    if (isExpanded) {
                                        if (plVideos.isEmpty()) {
                                            Text(
                                                "No videos in this playlist yet. Open any video and tap Playlist to add.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            plVideos.forEach { pv ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { onOpenVideo(pv) }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(pv.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                        Text(pv.channelName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    IconButton(onClick = { onRemoveFromPlaylist(pl.id, pv.id) }) {
                                                        Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove from playlist", tint = LotusCoral, modifier = Modifier.size(18.dp))
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    tickets: List<SupportTicketEntity>,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onSubmitTicket: (String, String, String) -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    var category by remember { mutableStateOf("Payment & Payouts") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val faqs = listOf(
        "How does BharatTube creator monetization work?" to "Eligible channels approved in Owner Control Center earn revenue tracked directly in Creator Studio, with automatic payouts unlocked once verification and regional thresholds are met.",
        "When are India payouts initiated?" to "Once an eligible creator in India reaches ₹2,000 in available balance with KYC, UPI/Bank, and tax verification completed, payouts trigger automatically.",
        "How does Chhotu AI protect comments and uploads?" to "Chhotu AI checks uploaded videos before publishing and automatically blocks abusive, hate-speech, or scam comments on every video and Short.",
        "How do I report a video or copyright issue?" to "Tap the Report flag icon on any Video, Short, or Live stream—or use the 3-dot menu on feed cards—to submit a safety or copyright review."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                listOf("Help Center", "Contact Us", "Terms", "Privacy Policy").forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label, maxLines = 1) },
                        modifier = Modifier.testTag("support_tab_$idx")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(faqs) { (q, a) ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { selectedTab = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Need more help? Contact Support") }
                        }
                    }

                    1 -> {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Email, contentDescription = null, tint = SaffronFlame)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("BharatTube Creator & Viewer Desk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Email: support@bharattube.in • Response within 24 hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Submit Support Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Payment & Payouts", "Account Recovery", "Copyright Appeal").forEach { cat ->
                                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                                        }
                                    }
                                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Describe your issue") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            onSubmitTicket(category, subject, message)
                                            subject = ""
                                            message = ""
                                        },
                                        enabled = subject.isNotBlank() && message.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Submit Ticket") }
                                }
                            }
                        }
                        if (tickets.isNotEmpty()) {
                            item { Text("My Support Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            items(tickets, key = { it.id }) { t ->
                                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("${t.category}: ${t.subject}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Status: ${t.status} • ${t.message}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            PolicyCard(
                                icon = Icons.Filled.Gavel,
                                title = "BharatTube Terms of Service",
                                paragraphs = listOf(
                                    "1. Original & Lawful Content: Creators may upload original videos, Shorts, and live streams that comply with Indian law and respect third-party copyrights.",
                                    "2. Community & Comment Safety: Hate speech, harassment, spam links, and abusive comments are automatically filtered by Chhotu AI and creator moderation controls.",
                                    "3. Creator Monetization & Verification: Channel monetization eligibility, Blue Tick verification badges, and payout schedules are governed by platform compliance checks and Owner Control Center policy.",
                                    "4. Reporting & Appeals: Any viewer may report suspicious, misleading, or infringing videos. Creators may submit an appeal via Contact Us for manual review."
                                )
                            )
                        }
                    }

                    else -> {
                        item {
                            PolicyCard(
                                icon = Icons.Filled.PrivacyTip,
                                title = "BharatTube Privacy Policy",
                                paragraphs = listOf(
                                    "1. Account Credentials: Passwords are stored securely with salted cryptographic hashing. Mobile OTP codes are generated only for session verification and password recovery.",
                                    "2. Watch & Search Controls: Users can pause or clear Watch History and Search History anytime from Settings > History and Privacy, or make Subscriptions and Playlists private.",
                                    "3. Camera, Mic & Gallery Access: Permissions and Android Photo Picker media access are used strictly when recording videos or choosing thumbnails, banners, and channel logos.",
                                    "4. Data Requests: For account deletion or privacy inquiries, submit a ticket under Contact Us or reach out to support@bharattube.in."
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    paragraphs: List<String>
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = SaffronFlame)
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            paragraphs.forEach { p ->
                Text(p, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
