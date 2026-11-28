package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.ads.AdMobBannerAd
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun HomeScreen(
    recommendedVideos: List<VideoEntity>,
    shorts: List<VideoEntity>,
    liveStreams: List<VideoEntity>,
    channels: List<ChannelEntity>,
    selectedCategory: String,
    settings: PlatformSettingsEntity,
    countryCode: String,
    languageCode: String,
    onSelectCategory: (String) -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenShort: (Int) -> Unit,
    onOpenLive: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onOpenCreateChannel: () -> Unit,
    onOpenSubscriptionsFeed: () -> Unit
) {
    val longAndLiveVideos = recommendedVideos.filter { it.format != VideoFormat.SHORT.name }
    val subscribedCount = channels.count { it.isSubscribedByMe }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Category Chips + Subscriptions Quick Pill
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = onOpenSubscriptionsFeed,
                        label = { Text("Subscriptions ($subscribedCount)") },
                        modifier = Modifier.testTag("home_subscriptions_chip")
                    )
                }
                items(BharatTubeCatalog.categories) { category ->
                    val selected = category == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronFlame,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
        }

        // Hero Spotlight Banner
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_creators),
                    contentDescription = "BharatTube Creators Spotlight",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xE60B0D16), Color(0x880B0D16), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(18.dp)
                        .fillMaxWidth(0.75f)
                ) {
                    Text(
                        text = "CREATOR SPOTLIGHT • $countryCode FEED",
                        style = MaterialTheme.typography.labelMedium,
                        color = SaffronFlame
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Create Your Channel & Reach Global Audiences",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onOpenCreateChannel,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("hero_create_channel_button")
                    ) {
                        Text("Create Your Channel", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Live Streams Strip
        if (settings.liveStreamingEnabled && liveStreams.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LiveTv, contentDescription = null, tint = LotusCoral, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Live Now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    liveStreams.forEach { live ->
                        Surface(
                            onClick = { onOpenLive(live) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_card_${live.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    BharatTubeThumbnail(live.thumbnailKey, live.title, Modifier.fillMaxSize())
                                    Surface(
                                        color = LotusCoral,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            "LIVE",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(live.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${live.channelName} • ${live.liveViewerCount} watching",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shorts Shelf
        if (settings.shortsEnabled && shorts.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "BharatTube Shorts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shorts.indices.toList()) { idx ->
                            val short = shorts[idx]
                            Box(
                                modifier = Modifier
                                    .width(142.dp)
                                    .height(236.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onOpenShort(idx) }
                                    .testTag("home_short_${short.id}")
                            ) {
                                BharatTubeThumbnail(short.thumbnailKey, short.title, Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color(0xCC0B0D16))
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = short.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${BharatTubeCatalog.formatCompactCount(short.viewCount)} views",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        // Personalized Recommended Videos
        item {
            Text(
                text = "Recommended for You",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (longAndLiveVideos.isEmpty()) {
            item {
                IntegrationDisclosureBanner(
                    title = "No matching videos in $selectedCategory",
                    message = "Switch categories or upload a new video from the Upload tab.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(longAndLiveVideos, key = { it.id }) { video ->
                VideoFeedCard(
                    video = video,
                    onClick = { if (video.isLive) onOpenLive(video) else onOpenVideo(video) },
                    onChannelClick = { onOpenChannel(video.channelId) },
                    onToggleWatchLater = { onToggleWatchLater(video) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        }
        if (settings.advertisementsEnabled && settings.bannerAdsEnabled) {
            AdMobBannerAd()
        }
    }
}
