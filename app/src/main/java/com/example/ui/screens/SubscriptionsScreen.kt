package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun SubscriptionsScreen(
    channels: List<ChannelEntity>,
    videos: List<VideoEntity>,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit
) {
    val subscribedChannels = remember(channels) { channels.filter { it.isSubscribedByMe } }
    val discoverChannels = remember(channels) { channels.filter { !it.isSubscribedByMe } }
    var selectedChannelId by remember { mutableStateOf<String?>(null) }

    val feedVideos = remember(videos, subscribedChannels, selectedChannelId) {
        val subIds = subscribedChannels.map { it.id }.toSet()
        videos
            .filter { it.visibility == VideoVisibility.PUBLIC.name }
            .filter {
                if (selectedChannelId != null) {
                    it.channelId == selectedChannelId
                } else {
                    it.channelId in subIds
                }
            }
            .sortedByDescending { it.createdAt }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Subscribed Channels Strip
        if (subscribedChannels.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedChannelId = null }
                                .testTag("sub_filter_all")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedChannelId == null) SaffronFlame else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All",
                                    color = if (selectedChannelId == null) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    items(subscribedChannels, key = { it.id }) { ch ->
                        val active = selectedChannelId == ch.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(68.dp)
                                .clickable {
                                    selectedChannelId = if (active) null else ch.id
                                }
                                .testTag("sub_channel_${ch.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (active) 2.5.dp else 1.dp,
                                        color = if (active) SaffronFlame else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (ch.avatarUri.isNotBlank()) {
                                    BharatTubeThumbnail(
                                        thumbnailKey = ch.avatarUri,
                                        contentDescription = ch.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = ch.name.take(1).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = ch.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (active) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (feedVideos.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Subscriptions,
                            contentDescription = null,
                            tint = SaffronFlame,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No videos from subscriptions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Subscribe to creators below to see their latest uploads and Shorts here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(feedVideos, key = { it.id }) { video ->
                VideoFeedCard(
                    video = video,
                    onClick = { onOpenVideo(video) },
                    onChannelClick = { onOpenChannel(video.channelId) },
                    onToggleWatchLater = { onToggleWatchLater(video) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Discover Creators to Subscribe
        if (discoverChannels.isNotEmpty()) {
            item {
                Text(
                    text = "Discover Channels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(discoverChannels, key = { "disc_${it.id}" }) { ch ->
                Surface(
                    onClick = { onOpenChannel(ch.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ch.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ch.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${ch.handle} • ${BharatTubeCatalog.formatCompactCount(ch.subscriberCount)} subscribers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onToggleSubscribe(ch) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                        ) {
                            Text("Subscribe")
                        }
                    }
                }
            }
        }
    }
}
