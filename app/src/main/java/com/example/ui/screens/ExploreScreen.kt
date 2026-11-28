package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoVisibility
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.VerifiedChannelBadge
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun ExploreScreen(
    videos: List<VideoEntity>,
    channels: List<ChannelEntity>,
    currentCountry: String,
    currentLanguage: String,
    boostedUserIds: Map<String, Int> = emptyMap(),
    trendingBoostEnabled: Boolean = true,
    workBoostPercent: Int = 30,
    onSelectCountry: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChannel: (String) -> Unit,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onReportVideo: (VideoEntity) -> Unit = {},
    onShareVideo: (VideoEntity) -> Unit = {}
) {
    var filterCountry by remember(currentCountry) { mutableStateOf("ALL") }
    var filterLang by remember(currentLanguage) { mutableStateOf("ALL") }
    val channelOwnerMap = remember(channels) { channels.associate { it.id to it.ownerUserId } }
    val verifiedChannelIds = remember(channels) { channels.filter { it.isVerified }.map { it.id }.toSet() }

    val filteredTrending = remember(videos, filterCountry, filterLang, boostedUserIds, trendingBoostEnabled, workBoostPercent, channelOwnerMap) {
        videos.filter { video ->
            video.visibility == VideoVisibility.PUBLIC.name &&
                video.chhotuCheckStatus == "APPROVED" &&
                (filterCountry == "ALL" || video.countryCode == filterCountry) &&
                (filterLang == "ALL" || video.languageCode == filterLang)
        }.sortedByDescending { video ->
            val ownerId = channelOwnerMap[video.channelId]
            val userBoost = if (trendingBoostEnabled) (boostedUserIds[ownerId] ?: 0) else 0
            val boostMultiplier = 1.0 + ((userBoost + if (userBoost > 0 && trendingBoostEnabled) workBoostPercent else 0) / 100.0)
            video.viewCount * boostMultiplier
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = SaffronFlame)
                    Spacer(Modifier.width(8.dp))
                    Text("Trending on BharatTube", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Discover top trending videos, live streams & regional creators across 20+ languages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Country Selector Row
        item {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaffronFlame)
                    Spacer(Modifier.width(6.dp))
                    Text("Country Feed", style = MaterialTheme.typography.labelLarge)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterCountry == "ALL",
                            onClick = { filterCountry = "ALL" },
                            label = { Text("Global") }
                        )
                    }
                    items(BharatTubeCatalog.countries) { country ->
                        FilterChip(
                            selected = filterCountry == country.code,
                            onClick = {
                                filterCountry = country.code
                                onSelectCountry(country.code)
                            },
                            label = { Text("${country.name} (${country.currencySymbol.trim()})") },
                            modifier = Modifier.testTag("explore_country_${country.code}")
                        )
                    }
                }
            }
        }

        // Language Selector Row
        item {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = LotusCoral)
                    Spacer(Modifier.width(6.dp))
                    Text("Language Feed (20 Initial • 100+ Architecture)", style = MaterialTheme.typography.labelLarge)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterLang == "ALL",
                            onClick = { filterLang = "ALL" },
                            label = { Text("All Languages") }
                        )
                    }
                    items(BharatTubeCatalog.initialLanguages) { lang ->
                        FilterChip(
                            selected = filterLang == lang.code,
                            onClick = {
                                filterLang = lang.code
                                onSelectLanguage(lang.code)
                            },
                            label = { Text("${lang.nativeName} (${lang.englishName})") },
                            modifier = Modifier.testTag("explore_lang_${lang.code}")
                        )
                    }
                }
            }
        }

        // Popular Creators
        item {
            Column {
                Text(
                    "Popular Creators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(channels, key = { it.id }) { channel ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .width(200.dp)
                                .clickable { onOpenChannel(channel.id) }
                                .testTag("explore_channel_${channel.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(channel.name.take(1), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    if (channel.isVerified) {
                                        Spacer(Modifier.width(4.dp))
                                        VerifiedChannelBadge(isVerified = true, size = 14.dp)
                                    }
                                }
                                Text(
                                    "${BharatTubeCatalog.formatCompactCount(channel.subscriberCount)} subscribers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                if (channel.isSubscribedByMe) {
                                    OutlinedButton(
                                        onClick = { onToggleSubscribe(channel) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Subscribed") }
                                } else {
                                    Button(
                                        onClick = { onToggleSubscribe(channel) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Subscribe") }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trending Results
        item {
            Text(
                "Trending Videos (${filteredTrending.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        itemsIndexed(filteredTrending, key = { _, v -> v.id }) { idx, video ->
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (idx < 5) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SaffronFlame.copy(alpha = 0.16f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "#${idx + 1} Trending",
                            color = SaffronFlame,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                VideoFeedCard(
                    video = video,
                    onClick = { onOpenVideo(video) },
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
