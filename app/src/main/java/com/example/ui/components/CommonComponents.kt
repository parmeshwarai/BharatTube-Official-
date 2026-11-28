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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun BharatTubeLogoMark(modifier: Modifier = Modifier) {
    val petalBrush = Brush.verticalGradient(listOf(Color(0xFFFF8F3F), SaffronFlame, LotusCoral))
    Canvas(modifier = modifier.size(32.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val baseY = h * 0.84f

        // Center tall lotus petal
        val centerPetal = Path().apply {
            moveTo(cx, baseY)
            cubicTo(w * 0.30f, h * 0.56f, w * 0.34f, h * 0.24f, cx, h * 0.12f)
            cubicTo(w * 0.66f, h * 0.24f, w * 0.70f, h * 0.56f, cx, baseY)
            close()
        }
        // Inner left petal
        val leftInner = Path().apply {
            moveTo(cx, baseY)
            cubicTo(w * 0.22f, h * 0.68f, w * 0.14f, h * 0.38f, w * 0.24f, h * 0.22f)
            cubicTo(w * 0.38f, h * 0.30f, w * 0.44f, h * 0.54f, cx, baseY)
            close()
        }
        // Inner right petal
        val rightInner = Path().apply {
            moveTo(cx, baseY)
            cubicTo(w * 0.78f, h * 0.68f, w * 0.86f, h * 0.38f, w * 0.76f, h * 0.22f)
            cubicTo(w * 0.62f, h * 0.30f, w * 0.56f, h * 0.54f, cx, baseY)
            close()
        }
        // Outer left petal
        val leftOuter = Path().apply {
            moveTo(cx, baseY)
            cubicTo(w * 0.14f, h * 0.82f, w * 0.05f, h * 0.58f, w * 0.08f, h * 0.40f)
            cubicTo(w * 0.22f, h * 0.48f, w * 0.32f, h * 0.66f, cx, baseY)
            close()
        }
        // Outer right petal
        val rightOuter = Path().apply {
            moveTo(cx, baseY)
            cubicTo(w * 0.86f, h * 0.82f, w * 0.95f, h * 0.58f, w * 0.92f, h * 0.40f)
            cubicTo(w * 0.78f, h * 0.48f, w * 0.68f, h * 0.66f, cx, baseY)
            close()
        }
        // Lotus curved calyx base
        val lotusBase = Path().apply {
            moveTo(w * 0.18f, h * 0.86f)
            quadraticTo(cx, h * 0.95f, w * 0.82f, h * 0.86f)
            quadraticTo(cx, h * 0.80f, w * 0.18f, h * 0.86f)
            close()
        }

        drawPath(leftOuter, brush = petalBrush, alpha = 0.88f)
        drawPath(rightOuter, brush = petalBrush, alpha = 0.88f)
        drawPath(leftInner, brush = petalBrush, alpha = 0.95f)
        drawPath(rightInner, brush = petalBrush, alpha = 0.95f)
        drawPath(centerPetal, brush = petalBrush)
        drawPath(lotusBase, color = SaffronFlame)
    }
}

@Composable
fun VerifiedChannelBadge(
    isVerified: Boolean,
    size: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    if (!isVerified) return
    Icon(
        imageVector = Icons.Filled.Verified,
        contentDescription = "Verified Channel",
        tint = Color(0xFF2F80ED),
        modifier = modifier.size(size)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BharatTubeTopBar(
    unreadNotificationCount: Int,
    onChhotuAiClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "BharatTube",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(Modifier.width(10.dp))
                Surface(
                    onClick = onSearchClick,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("search_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Search...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        actions = {
            // Small circular Chhotu AI button next to Bell and Search Bar
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .padding(start = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SaffronFlame.copy(alpha = 0.22f), LotusCoral.copy(alpha = 0.22f))
                            )
                        )
                        .border(1.2.dp, SaffronFlame.copy(alpha = 0.75f), CircleShape)
                        .clickable(onClick = onChhotuAiClick)
                        .testTag("header_chhotu_ai_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "Open Chhotu AI Assistant",
                        tint = SaffronFlame,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
    isChannelVerified: Boolean = false,
    onReportClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = video.channelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        VerifiedChannelBadge(isVerified = isChannelVerified, size = 14.dp)
                        Text(
                            text = "• ${BharatTubeCatalog.formatCompactCount(video.viewCount)} views",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("video_menu_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Video options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (video.isWatchLater) "Remove Watch Later" else "Save to Watch Later") },
                            leadingIcon = {
                                Icon(
                                    if (video.isWatchLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleWatchLater()
                            }
                        )
                        if (onShareClick != null) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onShareClick()
                                }
                            )
                        }
                        if (onReportClick != null) {
                            DropdownMenuItem(
                                text = { Text("Report Video") },
                                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = LotusCoral) },
                                onClick = {
                                    menuExpanded = false
                                    onReportClick()
                                }
                            )
                        }
                    }
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
    SHORTS("shorts", "Shorts"),
    CREATE("create", "Upload"),
    LIVE("live_tab", "Live"),
    PROFILE("profile", "Profile")
}

@Composable
fun BharatTubeBottomBar(
    currentRoute: String?,
    onSelectTab: (BharatTab) -> Unit,
    onCreateClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0E1018),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BharatTab.entries.forEach { tab ->
                    if (tab == BharatTab.CREATE) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable(onClick = onCreateClick)
                                .testTag("bottom_tab_create_plus"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Create or Upload",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    } else {
                        val selected = currentRoute == tab.route
                        val icon = when (tab) {
                            BharatTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                            BharatTab.SHORTS -> if (selected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
                            BharatTab.LIVE -> Icons.Filled.Sensors
                            BharatTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                            BharatTab.CREATE -> Icons.Filled.Add
                        }
                        val tint = if (selected) SaffronFlame else Color(0xFFA0A5B8)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable { onSelectTab(tab) }
                                .testTag("bottom_tab_${tab.route}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                color = tint,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOptionsBottomSheet(
    onDismiss: () -> Unit,
    onUploadVideo: () -> Unit,
    onCreateShort: () -> Unit,
    onGoLive: () -> Unit,
    onCreatePost: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1D1F27),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Create",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
            CreateSheetOptionRow(
                icon = Icons.Filled.FileUpload,
                label = "Upload Video",
                tag = "create_option_upload_video",
                onClick = onUploadVideo
            )
            CreateSheetOptionRow(
                icon = Icons.Filled.AutoAwesome,
                label = "Create Short",
                tag = "create_option_create_short",
                onClick = onCreateShort
            )
            CreateSheetOptionRow(
                icon = Icons.Filled.Sensors,
                label = "Go Live",
                tag = "create_option_go_live",
                onClick = onGoLive
            )
            CreateSheetOptionRow(
                icon = Icons.Filled.EditNote,
                label = "Create Post",
                tag = "create_option_create_post",
                onClick = onCreatePost
            )
        }
    }
}

@Composable
private fun CreateSheetOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF2B2E3B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
