package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoVisibility
import com.example.ui.components.VerifiedChannelBadge
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    videos: List<VideoEntity>,
    channels: List<ChannelEntity>,
    history: List<SearchHistoryEntity>,
    boostedUserIds: Map<String, Int> = emptyMap(),
    searchBoostEnabled: Boolean = true,
    workBoostPercent: Int = 30,
    onBack: () -> Unit,
    onRecordSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onVoiceUnavailable: () -> Unit,
    onReportVideo: (VideoEntity) -> Unit = {},
    onShareVideo: (VideoEntity) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var sortByPopular by remember { mutableStateOf(false) }
    var filterLang by remember { mutableStateOf("ALL") }
    val channelOwnerMap = remember(channels) { channels.associate { it.id to it.ownerUserId } }
    val verifiedChannelIds = remember(channels) { channels.filter { it.isVerified }.map { it.id }.toSet() }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                query = spoken
                onRecordSearch(spoken)
            }
        }
    }

    val matchingChannels = remember(channels, query) {
        if (query.isBlank()) emptyList()
        else channels.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.handle.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }

    val matchingVideos = remember(videos, query, sortByPopular, filterLang, boostedUserIds, searchBoostEnabled, workBoostPercent, channelOwnerMap) {
        videos.filter { v ->
            val qMatch = query.isBlank() ||
                v.title.contains(query, ignoreCase = true) ||
                v.channelName.contains(query, ignoreCase = true) ||
                v.hashtags.contains(query, ignoreCase = true) ||
                v.category.contains(query, ignoreCase = true)
            val lMatch = filterLang == "ALL" || v.languageCode == filterLang
            v.visibility == VideoVisibility.PUBLIC.name &&
                v.chhotuCheckStatus == "APPROVED" &&
                qMatch &&
                lMatch
        }.let { list ->
            if (sortByPopular) {
                list.sortedByDescending { v ->
                    val ownerId = channelOwnerMap[v.channelId]
                    val userBoost = if (searchBoostEnabled) (boostedUserIds[ownerId] ?: 0) else 0
                    val mult = 1.0 + ((userBoost + if (userBoost > 0 && searchBoostEnabled) workBoostPercent else 0) / 100.0)
                    v.viewCount * mult
                }
            } else {
                list.sortedByDescending { v ->
                    val ownerId = channelOwnerMap[v.channelId]
                    val userBoost = if (searchBoostEnabled) (boostedUserIds[ownerId] ?: 0) else 0
                    val boostBonusMs = if (userBoost > 0 && searchBoostEnabled) (userBoost + workBoostPercent) * 3_600_000L else 0L
                    v.createdAt + boostBonusMs
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search videos, Shorts, channels, #tags") },
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        }
                                        voiceLauncher.launch(intent)
                                    } catch (_: Exception) {
                                        onVoiceUnavailable()
                                    }
                                }) {
                                    Icon(Icons.Filled.Mic, contentDescription = "Voice search")
                                }
                                IconButton(onClick = { onRecordSearch(query) }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Run search", tint = SaffronFlame)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("search_input")
                    )
                },
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
            // Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = !sortByPopular,
                            onClick = { sortByPopular = false },
                            label = { Text("Latest") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = sortByPopular,
                            onClick = { sortByPopular = true },
                            label = { Text("Most Popular") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterLang == "ALL",
                            onClick = { filterLang = "ALL" },
                            label = { Text("All Languages") }
                        )
                    }
                    items(BharatTubeCatalog.initialLanguages.take(6)) { lang ->
                        FilterChip(
                            selected = filterLang == lang.code,
                            onClick = { filterLang = lang.code },
                            label = { Text(lang.englishName) }
                        )
                    }
                }
            }

            // Search History
            if (query.isBlank() && history.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Recent Searches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = onClearHistory) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }
                items(history, key = { it.query }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { query = item.query }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        Text(item.query, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Matching Channels
            if (matchingChannels.isNotEmpty()) {
                item { Text("Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(matchingChannels, key = { it.id }) { ch ->
                    Surface(
                        onClick = { onOpenChannel(ch.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${ch.name} (${ch.handle})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (ch.isVerified) {
                                    Spacer(Modifier.width(4.dp))
                                    VerifiedChannelBadge(isVerified = true, size = 14.dp)
                                }
                            }
                            Text("${BharatTubeCatalog.formatCompactCount(ch.subscriberCount)} subscribers • ${ch.category}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Video Results
            item {
                Text("Videos & Shorts (${matchingVideos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(matchingVideos, key = { it.id }) { video ->
                VideoFeedCard(
                    video = video,
                    onClick = {
                        if (query.isNotBlank()) onRecordSearch(query)
                        onOpenVideo(video)
                    },
                    onChannelClick = { onOpenChannel(video.channelId) },
                    onToggleWatchLater = { onToggleWatchLater(video) },
                    isChannelVerified = video.channelId in verifiedChannelIds,
                    onReportClick = { onReportVideo(video) },
                    onShareClick = { onShareVideo(video) }
                )
            }
        }
    }
}
