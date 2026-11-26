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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.SupportTicketEntity
import com.example.data.model.VideoEntity
import com.example.data.model.WatchHistoryEntity
import com.example.ui.components.VideoFeedCard
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
    onToggleWatchLater: (VideoEntity) -> Unit
) {
    var tab by remember { mutableIntStateOf(initialTab) }
    var newPlaylistName by remember { mutableStateOf("") }

    val videoMap = remember(videos) { videos.associateBy { it.id } }
    val subscribedChannelIds = remember(channels) { channels.filter { it.isSubscribedByMe }.map { it.id }.toSet() }
    val subscriptionVideos = videos.filter { it.channelId in subscribedChannelIds }
    val historyVideos = watchHistory.mapNotNull { videoMap[it.videoId] }
    val watchLaterVideos = videos.filter { it.isWatchLater }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library & Subscriptions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Subscriptions", "History", "Watch Later", "Playlists").forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title, maxLines = 1) })
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
                                VideoFeedCard(v, { onOpenVideo(v) }, { onOpenChannel(v.channelId) }, { onToggleWatchLater(v) })
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
                        items(historyVideos, key = { it.id }) { v ->
                            VideoFeedCard(v, { onOpenVideo(v) }, { onOpenChannel(v.channelId) }, { onToggleWatchLater(v) })
                        }
                    }
                    2 -> {
                        if (watchLaterVideos.isEmpty()) {
                            item { Text("Tap the bookmark icon on any video to save it to Watch Later.") }
                        } else {
                            items(watchLaterVideos, key = { it.id }) { v ->
                                VideoFeedCard(v, { onOpenVideo(v) }, { onOpenChannel(v.channelId) }, { onToggleWatchLater(v) })
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
                            val count = playlistRefs.count { it.playlistId == pl.id }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pl.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("$count videos", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete playlist")
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
    onBack: () -> Unit,
    onSubmitTicket: (String, String, String) -> Unit
) {
    var category by remember { mutableStateOf("Payment & Payouts") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val faqs = listOf(
        "How does BharatTube 50/50 revenue sharing work?" to "Eligible net advertising revenue after network fees and applicable taxes is split 50% to the Creator and 50% to BharatTube by default.",
        "When are India payouts initiated?" to "Once a creator in India reaches ₹1,000 in eligible available balance with KYC, UPI/Bank, and tax verification completed.",
        "How are copyright claims handled?" to "Submit a complaint or appeal here or via the video Report menu. Similarity alone is never treated as proof of infringement without review."
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
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
    }
}
