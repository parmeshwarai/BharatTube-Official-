package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.UserPreferences
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.ProfileFormatters
import com.example.ui.components.SwitchOrCreateAccountDialog
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserProfileEntity?,
    allUsers: List<UserProfileEntity>,
    myChannels: List<ChannelEntity>,
    allVideos: List<VideoEntity>,
    playlists: List<PlaylistEntity>,
    playlistRefs: List<PlaylistVideoCrossRef>,
    prefs: UserPreferences,
    onSelectChannel: (String) -> Unit,
    onCreateChannel: () -> Unit,
    onEditChannel: () -> Unit,
    onManageVideos: () -> Unit,
    onOpenAbout: (String) -> Unit,
    onOpenProfileMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenChhotuAi: () -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenPlaylistsLibrary: () -> Unit,
    onOpenUpload: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onCreateLocalAccount: (String, String, String, String, String, Boolean) -> Unit,
    onSignInBack: (String?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSwitchDialog by remember { mutableStateOf(false) }

    val activeChannel = remember(myChannels, prefs.activeChannelId) {
        myChannels.firstOrNull { it.id == prefs.activeChannelId } ?: myChannels.firstOrNull()
    }

    val channelVideos = remember(allVideos, activeChannel) {
        if (activeChannel == null) emptyList()
        else allVideos.filter { it.channelId == activeChannel.id && it.visibility != VideoVisibility.DRAFT.name }
    }

    val tabItems = remember(channelVideos, selectedTab) {
        when (selectedTab) {
            0 -> channelVideos.filter { it.format == VideoFormat.LONG.name }
            1 -> channelVideos.filter { it.format == VideoFormat.SHORT.name }
            2 -> channelVideos.filter { it.format == VideoFormat.LIVE.name || it.isLive }
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1016))
    ) {
        // Dedicated Profile Header with BharatTube Orange Lotus, Chhotu AI, Search, Settings, and Profile Menu icon
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "BharatTube",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenChhotuAi,
                    modifier = Modifier.testTag("profile_chhotu_button")
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = "Chhotu AI", tint = SaffronFlame)
                }
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("profile_search_button")
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("profile_settings_icon_button")
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
                IconButton(
                    onClick = onOpenProfileMenu,
                    modifier = Modifier.testTag("profile_menu_icon_button")
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile Menu", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
        )

        if (prefs.isSignedOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF171923),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BharatTubeLogoMark(modifier = Modifier.size(52.dp))
                        Text(
                            text = "You're Signed Out",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Sign back in or switch to another BharatTube profile to manage your channel and videos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA0A5B8)
                        )
                        Button(
                            onClick = { onSignInBack(user?.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                            modifier = Modifier.fillMaxWidth().testTag("sign_back_in_button")
                        ) {
                            Text("Continue as ${user?.username ?: "Creator"}")
                        }
                        OutlinedButton(
                            onClick = { showSwitchDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Switch Account", color = Color.White)
                        }
                    }
                }
            }
        } else if (activeChannel == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF171923),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ChannelLogoAvatar(
                                avatarUri = user?.avatarUri.orEmpty(),
                                contentDescription = user?.username ?: "Profile",
                                size = 84.dp,
                                borderWidth = 2.dp,
                                borderColor = SaffronFlame
                            )
                            Text(
                                text = user?.username ?: "BharatTube Creator",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = user?.handle ?: "@creator",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA0A5B8)
                            )
                            Button(
                                onClick = onCreateChannel,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                modifier = Modifier.fillMaxWidth().testTag("profile_create_channel_button")
                            ) {
                                Icon(Icons.Filled.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Create Your Channel")
                            }
                            OutlinedButton(
                                onClick = onOpenProfileMenu,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Profile Menu", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Multi-channel selector if user owns multiple channels
                if (myChannels.size > 1) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myChannels, key = { it.id }) { ch ->
                                FilterChip(
                                    selected = ch.id == activeChannel.id,
                                    onClick = { onSelectChannel(ch.id) },
                                    label = { Text(ch.name) }
                                )
                            }
                        }
                    }
                }

                // 1. Wide Channel Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable(onClick = onEditChannel)
                            .testTag("profile_banner_image")
                    ) {
                        BharatTubeThumbnail(
                            thumbnailKey = activeChannel.bannerUri.ifBlank { "drawable:img_hero_creators" },
                            contentDescription = "${activeChannel.name} Banner",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x880E1016))
                                    )
                                )
                        )
                    }
                }

                // 2. Channel Logo + Name + @Handle + Subscribers + Videos + About Link
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelLogoAvatar(
                                avatarUri = activeChannel.avatarUri,
                                contentDescription = activeChannel.name,
                                size = 80.dp,
                                borderWidth = 2.5.dp,
                                borderColor = Color(0xFF282B36),
                                modifier = Modifier
                                    .clickable(onClick = onEditChannel)
                                    .testTag("profile_channel_logo")
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeChannel.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = activeChannel.handle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFA0A5B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${BharatTubeCatalog.formatCompactCount(activeChannel.subscriberCount)} subscribers • ${channelVideos.size} videos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB5BACB)
                                )
                            }
                            IconButton(
                                onClick = { onOpenAbout(activeChannel.id) },
                                modifier = Modifier.testTag("profile_about_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "About Channel",
                                    tint = Color(0xFFA0A5B8)
                                )
                            }
                        }

                        // Clickable channel bio preview leading to About section
                        if (activeChannel.description.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenAbout(activeChannel.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeChannel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA0A5B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "About Channel",
                                    tint = Color(0xFFA0A5B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 2 Action Buttons: Edit Channel & Manage Videos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = onEditChannel,
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFF232632),
                                border = BorderStroke(1.dp, Color(0xFF333849)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("profile_edit_channel_button")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Channel",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                onClick = onManageVideos,
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFF232632),
                                border = BorderStroke(1.dp, Color(0xFF333849)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("profile_manage_videos_button")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Manage Videos",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Tabs: Videos, Shorts, Live, Playlists
                item {
                    val tabs = listOf("Videos", "Shorts", "Live", "Playlists")
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF14161F),
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = SaffronFlame,
                                    height = 2.5.dp
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == index) Color.White else Color(0xFF8B91A6)
                                    )
                                },
                                modifier = Modifier.testTag("profile_tab_${title.lowercase()}")
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // 4. Tab Content
                if (selectedTab == 3) {
                    // Playlists Tab
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Playlists (${playlists.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Manage all",
                                color = SaffronFlame,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable(onClick = onOpenPlaylistsLibrary)
                                    .padding(6.dp)
                            )
                        }
                    }
                    if (playlists.isEmpty()) {
                        item {
                            EmptyChannelTabCard(
                                title = "No playlists created yet",
                                subtitle = "Create playlists to organize your tutorials, vlogs, and series.",
                                buttonLabel = "Create Playlist",
                                onAction = onOpenPlaylistsLibrary
                            )
                        }
                    } else {
                        items(playlists, key = { it.id }) { pl ->
                            val count = playlistRefs.count { it.playlistId == pl.id }
                            Surface(
                                onClick = onOpenPlaylistsLibrary,
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF171923),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SaffronFlame.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = SaffronFlame)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pl.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            text = "$count videos • ${if (pl.isPrivate) "Private" else "Public"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA0A5B8)
                                        )
                                    }
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF8B91A6))
                                }
                            }
                        }
                    }
                } else {
                    if (tabItems.isEmpty()) {
                        val tabLabel = listOf("videos", "Shorts", "Live streams")[selectedTab]
                        item {
                            EmptyChannelTabCard(
                                title = "No $tabLabel uploaded yet",
                                subtitle = "Upload your next creation and it will appear right here on your channel.",
                                buttonLabel = "Upload",
                                onAction = onOpenUpload
                            )
                        }
                    } else {
                        // 2-Column Video Thumbnail Grid matching WA2379 / WA3866
                        val rows = tabItems.chunked(2)
                        items(rows) { rowVideos ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowVideos.forEach { video ->
                                    ProfileVideoGridCard(
                                        video = video,
                                        onClick = { onOpenVideo(video) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowVideos.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSwitchDialog) {
        SwitchOrCreateAccountDialog(
            currentUser = user,
            allUsers = allUsers,
            defaultCountry = prefs.preferredCountry,
            defaultLanguage = prefs.preferredLanguage,
            onDismiss = { showSwitchDialog = false },
            onSwitchUser = onSwitchUser,
            onCreateLocalAccount = onCreateLocalAccount
        )
    }
}

@Composable
private fun ProfileVideoGridCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relativeDate = remember(video.createdAt) { ProfileFormatters.formatRelativeDate(video.createdAt) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("profile_video_tile_${video.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF171923))
        ) {
            BharatTubeThumbnail(
                thumbnailKey = video.thumbnailKey,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color = if (video.isLive) LotusCoral else Color.Black.copy(alpha = 0.80f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = if (video.isLive) "LIVE" else BharatTubeCatalog.formatDuration(video.durationSeconds),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            if (video.visibility != VideoVisibility.PUBLIC.name) {
                Surface(
                    color = Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = video.visibility.lowercase().replaceFirstChar { it.uppercase() },
                        color = SaffronFlame,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (video.lastPositionMs > 2000L && video.durationSeconds > 0) {
                val progress = (video.lastPositionMs / (video.durationSeconds * 1000f)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.BottomCenter),
                    color = SaffronFlame,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${BharatTubeCatalog.formatCompactCount(video.viewCount)} views • $relativeDate",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA0A5B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyChannelTabCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171923),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(buttonLabel)
            }
        }
    }
}
