package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.VideoEntity
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun BharatTubeLogoMark(modifier: Modifier = Modifier) {
    val primary = SaffronFlame
    val secondary = LotusCoral
    Canvas(modifier = modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.linearGradient(listOf(primary, secondary)),
            radius = w * 0.48f,
            center = Offset(w / 2f, h / 2f)
        )
        val triangle = Path().apply {
            moveTo(w * 0.39f, h * 0.29f)
            lineTo(w * 0.72f, h * 0.50f)
            lineTo(w * 0.39f, h * 0.71f)
            close()
        }
        drawPath(triangle, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BharatTubeTopBar(
    isDarkTheme: Boolean,
    unreadNotificationCount: Int,
    countryCode: String,
    languageCode: String,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onRegionClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BharatTubeLogoMark()
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "BharatTube",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            Surface(
                onClick = onRegionClick,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("region_chip_button")
            ) {
                Text(
                    text = "$countryCode • ${languageCode.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Toggle theme"
                )
            }
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge { Text(unreadNotificationCount.coerceAtMost(99).toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadNotificationCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun BharatTubeThumbnail(
    thumbnailKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (thumbnailKey) {
        "drawable:img_thumb_tech" -> R.drawable.img_thumb_tech
        "drawable:img_thumb_music" -> R.drawable.img_thumb_music
        "drawable:img_thumb_travel" -> R.drawable.img_thumb_travel
        "drawable:img_hero_creators" -> R.drawable.img_hero_creators
        else -> null
    }
    if (drawableRes != null) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (thumbnailKey.isNotBlank()) {
        AsyncImage(
            model = thumbnailKey,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(listOf(SaffronFlame.copy(alpha = 0.7f), LotusCoral.copy(alpha = 0.7f)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = contentDescription, tint = Color.White)
        }
    }
}

@Composable
fun VideoFeedCard(
    video: VideoEntity,
    onClick: () -> Unit,
    onChannelClick: () -> Unit,
    onToggleWatchLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                BharatTubeThumbnail(
                    thumbnailKey = video.thumbnailKey,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = if (video.isLive) LotusCoral else Color.Black.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (video.isLive) "LIVE • ${video.liveViewerCount}" else BharatTubeCatalog.formatDuration(video.durationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "${video.category} • ${video.languageCode.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (video.lastPositionMs > 2000L && video.durationSeconds > 0) {
                    val progress = (video.lastPositionMs / (video.durationSeconds * 1000f)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = SaffronFlame,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral)))
                        .clickable(onClick = onChannelClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.channelName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${video.channelName} • ${BharatTubeCatalog.formatCompactCount(video.viewCount)} views • ${video.playbackQuality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onToggleWatchLater,
                    modifier = Modifier.testTag("watch_later_${video.id}")
                ) {
                    Icon(
                        imageVector = if (video.isWatchLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save to Watch Later",
                        tint = if (video.isWatchLater) SaffronFlame else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IntegrationDisclosureBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Integration requirement notice",
                tint = SaffronFlame,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class BharatTab(val route: String, val label: String) {
    HOME("home", "Home"),
    EXPLORE("explore", "Explore"),
    UPLOAD("upload", "Upload"),
    SHORTS("shorts", "Shorts"),
    PROFILE("profile", "Profile")
}

@Composable
fun BharatTubeBottomBar(
    currentRoute: String?,
    onSelectTab: (BharatTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets.navigationBars
    ) {
        BharatTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            val icon = when (tab) {
                BharatTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                BharatTab.EXPLORE -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
                BharatTab.UPLOAD -> if (selected) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline
                BharatTab.SHORTS -> if (selected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
                BharatTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = { Icon(icon, contentDescription = tab.label) },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SaffronFlame,
                    selectedTextColor = SaffronFlame,
                    indicatorColor = SaffronFlame.copy(alpha = 0.16f)
                ),
                modifier = Modifier.testTag("bottom_tab_${tab.route}")
            )
        }
    }
}
