package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.components.VerifiedChannelBadge
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun LiveFeedScreen(
    liveStreams: List<VideoEntity>,
    allVideos: List<VideoEntity>,
    channels: List<ChannelEntity>,
    settings: PlatformSettingsEntity,
    onOpenLive: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onReportVideo: (VideoEntity) -> Unit,
    onStartLive: () -> Unit
) {
    if (!settings.liveStreamingEnabled) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Live streaming is currently paused by Owner Control Center.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val verifiedChannelIds = remember(channels) {
        channels.filter { it.isVerified }.map { it.id }.toSet()
    }

    val activeLives = remember(liveStreams, selectedCategory) {
        liveStreams.filter {
            it.isLive && (selectedCategory == "All" || it.category == selectedCategory)
        }
    }
    val pastLives = remember(allVideos, selectedCategory) {
        allVideos.filter {
            !it.isLive && it.format == VideoFormat.LIVE.name &&
                it.chhotuCheckStatus == "APPROVED" &&
                (selectedCategory == "All" || it.category == selectedCategory)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1016)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF151822),
                border = BorderStroke(1.dp, LotusCoral.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LotusCoral.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Sensors, contentDescription = "Live Now", tint = LotusCoral)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BharatTube Live", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "${activeLives.size} live streams active right now",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA0A5B8)
                        )
                    }
                    Button(
                        onClick = onStartLive,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("live_tab_go_live_button")
                    ) {
                        Icon(Icons.Filled.LiveTv, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Go Live", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BharatTubeCatalog.categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronFlame,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        if (activeLives.isEmpty()) {
            item {
                IntegrationDisclosureBanner(
                    title = "No Live Streams in $selectedCategory",
                    message = "Tap 'Go Live' above or use the + Upload button to start broadcasting live to BharatTube viewers."
                )
            }
        } else {
            item {
                Text(
                    "Live Now (${activeLives.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            items(activeLives, key = { it.id }) { live ->
                val isVerified = live.channelId in verifiedChannelIds
                Surface(
                    onClick = { onOpenLive(live) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth().testTag("live_feed_card_${live.id}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            BharatTubeThumbnail(live.thumbnailKey, live.title, Modifier.fillMaxSize())
                            Surface(
                                color = LotusCoral,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "LIVE • ${live.liveViewerCount} watching",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = live.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenChannel(live.channelId) }
                                ) {
                                    Text(
                                        text = "${live.channelName} • ${live.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                    if (isVerified) {
                                        Spacer(Modifier.width(4.dp))
                                        VerifiedChannelBadge(isVerified = true, size = 14.dp)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { onReportVideo(live) },
                                modifier = Modifier.testTag("report_live_${live.id}")
                            ) {
                                Icon(Icons.Filled.Flag, contentDescription = "Report stream", tint = Color(0xFFA0A5B8))
                            }
                        }
                    }
                }
            }
        }

        if (pastLives.isNotEmpty()) {
            item {
                Text(
                    "Recent Live Replays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            items(pastLives, key = { it.id }) { replay ->
                VideoFeedCard(
                    video = replay,
                    onClick = { onOpenLive(replay) },
                    onChannelClick = { onOpenChannel(replay.channelId) },
                    onToggleWatchLater = { onToggleWatchLater(replay) },
                    isChannelVerified = replay.channelId in verifiedChannelIds,
                    onReportClick = { onReportVideo(replay) }
                )
            }
        }
    }
}
