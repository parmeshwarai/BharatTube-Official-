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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.AppShareBottomSheet
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.SwitchOrCreateAccountDialog
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

private data class SettingsCategoryItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenSection: (String) -> Unit,
    onOpenSupport: () -> Unit,
    onSignOut: () -> Unit
) {
    var showAppShareSheet by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            SettingsCategoryItem("general", "General", Icons.Filled.Settings),
            SettingsCategoryItem("account", "Account", Icons.Filled.AccountCircle),
            SettingsCategoryItem("notifications", "Notifications", Icons.Filled.Notifications),
            SettingsCategoryItem("playback", "Playback", Icons.Filled.PlayCircleOutline),
            SettingsCategoryItem("downloads", "Downloads", Icons.Filled.Download),
            SettingsCategoryItem("privacy", "Privacy", Icons.Filled.Security),
            SettingsCategoryItem("history_privacy", "History and Privacy", Icons.Filled.History),
            SettingsCategoryItem("connected_apps", "Connected Apps", Icons.Filled.Link)
        )
    }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                SettingsCategoryRow(
                    icon = cat.icon,
                    title = cat.title,
                    tag = "settings_item_${cat.id}",
                    onClick = { onOpenSection(cat.id) }
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.06f),
                    thickness = 0.8.dp
                )
            }

            item {
                SettingsCategoryRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help and Feedback",
                    tag = "settings_item_help_feedback",
                    onClick = onOpenSupport
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.06f),
                    thickness = 0.8.dp
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { confirmSignOut = true }
                        .padding(vertical = 15.dp)
                        .testTag("settings_item_sign_out"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotusCoral.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = LotusCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = LotusCoral,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF8B91A6),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Very bottom of Settings: App Share
            item {
                Spacer(Modifier.height(14.dp))
                Surface(
                    onClick = { showAppShareSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF171923),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_app_share")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SaffronFlame.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "App Share", tint = SaffronFlame)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Share",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "App Share Karo - Dosto ko BharatTube bhejo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0A5B8)
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF8B91A6))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign Out of BharatTube?") },
            text = { Text("You can sign back in or switch accounts anytime from Profile.") },
            confirmButton = {
                Button(
                    onClick = {
                        confirmSignOut = false
                        onSignOut()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LotusCoral)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") }
            }
        )
    }

    if (showAppShareSheet) {
        AppShareBottomSheet(onDismiss = { showAppShareSheet = false })
    }
}

@Composable
private fun SettingsCategoryRow(
    icon: ImageVector,
    title: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222530)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFD5D9E6),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF8B91A6),
            modifier = Modifier.size(22.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDetailScreen(
    sectionId: String,
    user: UserProfileEntity?,
    activeChannel: ChannelEntity?,
    allUsers: List<UserProfileEntity>,
    allVideos: List<VideoEntity>,
    prefs: UserPreferences,
    onBack: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectCountry: (String) -> Unit,
    onToggleRestrictedMode: () -> Unit,
    onUpdateProfile: (UserProfileEntity) -> Unit,
    onSwitchUser: (String) -> Unit,
    onCreateLocalAccount: (String, String, String, String, String, Boolean) -> Unit,
    onOpenEditChannel: () -> Unit,
    onOpenCreatorStudio: () -> Unit,
    onOpenNotificationsCenter: () -> Unit,
    onOpenHistoryLibrary: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenChhotuAi: () -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onSetPushOptIn: (Boolean) -> Unit,
    onSetNotifyUploads: (Boolean) -> Unit,
    onSetNotifyComments: (Boolean) -> Unit,
    onSetNotifyLive: (Boolean) -> Unit,
    onSetPlaybackAutoPlay: (Boolean) -> Unit,
    onSetPlaybackQuality: (String) -> Unit,
    onSetPlaybackCaptions: (Boolean) -> Unit,
    onSetPlaybackDataSaver: (Boolean) -> Unit,
    onSetDownloadsWifiOnly: (Boolean) -> Unit,
    onSetDownloadsQuality: (String) -> Unit,
    onToggleDownloadVideo: (VideoEntity) -> Unit,
    onClearDownloads: () -> Unit,
    onSetPrivacySubsPrivate: (Boolean) -> Unit,
    onSetPrivacyPlaylistsPrivate: (Boolean) -> Unit,
    onToggleHistoryPaused: () -> Unit,
    onClearWatchHistory: () -> Unit,
    onClearSearchHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    val screenTitle = when (sectionId) {
        "general" -> "General"
        "account" -> "Account"
        "notifications" -> "Notifications"
        "playback" -> "Playback"
        "downloads" -> "Downloads"
        "privacy" -> "Privacy"
        "history_privacy" -> "History and Privacy"
        else -> "Connected Apps"
    }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showSwitchDialog by remember { mutableStateOf(false) }
    var confirmClearHistory by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("setting_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (sectionId) {
                "general" -> {
                    item {
                        SettingsSwitchCard(
                            title = "Dark Theme",
                            subtitle = "Use BharatTube dark appearance across screens",
                            checked = prefs.isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() }
                        )
                    }
                    item {
                        val currentLang = BharatTubeCatalog.initialLanguages.firstOrNull { it.code == prefs.preferredLanguage }
                        SettingsActionCard(
                            title = "App Language",
                            subtitle = "${currentLang?.nativeName ?: "English"} (${prefs.preferredLanguage.uppercase()})",
                            onClick = { showLanguageDialog = true }
                        )
                    }
                    item {
                        val currentCountry = BharatTubeCatalog.countries.firstOrNull { it.code == prefs.preferredCountry }
                        SettingsActionCard(
                            title = "Location & Feed Region",
                            subtitle = "${currentCountry?.name ?: "India"} (${currentCountry?.currencyCode ?: "INR"})",
                            onClick = { showCountryDialog = true }
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Restricted Mode",
                            subtitle = "Helps hide potentially mature videos from feeds",
                            checked = prefs.restrictedMode,
                            onCheckedChange = { onToggleRestrictedMode() }
                        )
                    }
                }

                "account" -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF171923),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelLogoAvatar(
                                    avatarUri = activeChannel?.avatarUri?.ifBlank { user?.avatarUri.orEmpty() } ?: "",
                                    contentDescription = user?.username ?: "Account",
                                    size = 56.dp,
                                    borderWidth = 2.dp,
                                    borderColor = SaffronFlame
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (prefs.isSignedOut) "Signed Out" else (user?.username ?: "Creator"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${user?.handle ?: "@bharattube"} • ${user?.email?.ifBlank { "Local Profile" } ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        SettingsActionCard(
                            title = "Edit Channel Profile",
                            subtitle = "Update channel banner, logo, name, @handle & links",
                            onClick = onOpenEditChannel
                        )
                    }
                    item {
                        SettingsActionCard(
                            title = "Switch or Create Account",
                            subtitle = "Manage local BharatTube profiles on this device",
                            onClick = { showSwitchDialog = true }
                        )
                    }
                    item {
                        SettingsActionCard(
                            title = "Creator Studio & Verification",
                            subtitle = "Channel analytics, KYC status & payout setup",
                            onClick = onOpenCreatorStudio
                        )
                    }
                    if (user != null) {
                        item {
                            SettingsSwitchCard(
                                title = "Two-Factor Authentication (2FA)",
                                subtitle = "Adds extra protection to account & payout controls",
                                checked = user.twoFactorEnabled,
                                onCheckedChange = { onUpdateProfile(user.copy(twoFactorEnabled = it)) }
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                onSignOut()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LotusCoral),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "notifications" -> {
                    item {
                        SettingsSwitchCard(
                            title = "Push Notifications",
                            subtitle = "Receive alerts from BharatTube channels and updates",
                            checked = prefs.pushNotificationsOptIn,
                            onCheckedChange = onSetPushOptIn
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "New Video Uploads",
                            subtitle = "Notify when subscribed creators post new videos",
                            checked = prefs.notifyUploads,
                            onCheckedChange = onSetNotifyUploads
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Comments & Replies",
                            subtitle = "Notify when someone replies to your comment or video",
                            checked = prefs.notifyComments,
                            onCheckedChange = onSetNotifyComments
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Live Streams",
                            subtitle = "Notify when channels you follow go Live",
                            checked = prefs.notifyLive,
                            onCheckedChange = onSetNotifyLive
                        )
                    }
                    item {
                        SettingsActionCard(
                            title = "View All Notifications",
                            subtitle = "Open your BharatTube notifications inbox",
                            onClick = onOpenNotificationsCenter
                        )
                    }
                }

                "playback" -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF171923),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Default Video Quality", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Auto", "1080p", "720p", "480p", "360p").forEach { quality ->
                                        FilterChip(
                                            selected = prefs.playbackDefaultQuality == quality,
                                            onClick = { onSetPlaybackQuality(quality) },
                                            label = { Text(quality) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Auto-Play Next Video",
                            subtitle = "Automatically play the next recommended video",
                            checked = prefs.playbackAutoPlayNext,
                            onCheckedChange = onSetPlaybackAutoPlay
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Always Show Captions",
                            subtitle = "Display video subtitles when available",
                            checked = prefs.playbackCaptionsEnabled,
                            onCheckedChange = onSetPlaybackCaptions
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Data Saver Mode",
                            subtitle = "Reduces streaming bitrate on mobile networks",
                            checked = prefs.playbackDataSaver,
                            onCheckedChange = onSetPlaybackDataSaver
                        )
                    }
                }

                "downloads" -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF171923),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Download Quality", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("1080p", "720p", "360p").forEach { q ->
                                        FilterChip(
                                            selected = prefs.downloadsQuality == q,
                                            onClick = { onSetDownloadsQuality(q) },
                                            label = { Text(q) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Download Over Wi-Fi Only",
                            subtitle = "Pause offline saves when on mobile data",
                            checked = prefs.downloadsWifiOnly,
                            onCheckedChange = onSetDownloadsWifiOnly
                        )
                    }
                    val downloadedIds = prefs.downloadedVideoIdsCsv.split(",").filter { it.isNotBlank() }.toSet()
                    val downloadedVideos = allVideos.filter { it.id in downloadedIds }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saved Downloads (${downloadedVideos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (downloadedVideos.isNotEmpty()) {
                                TextButton(onClick = onClearDownloads) {
                                    Text("Clear all", color = LotusCoral)
                                }
                            }
                        }
                    }
                    if (downloadedVideos.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF171923),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("No downloaded videos yet", style = MaterialTheme.typography.titleSmall, color = Color.White)
                                    Text(
                                        "Quick-save recommended videos below for offline library access.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                }
                            }
                        }
                        items(allVideos.take(3), key = { "suggest_${it.id}" }) { vid ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF171923),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    BharatTubeThumbnail(
                                        thumbnailKey = vid.thumbnailKey,
                                        contentDescription = vid.title,
                                        modifier = Modifier.size(96.dp, 56.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vid.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(vid.channelName, color = Color(0xFFA0A5B8), style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(onClick = { onToggleDownloadVideo(vid) }) {
                                        Icon(Icons.Filled.Download, contentDescription = "Save to Downloads", tint = SaffronFlame)
                                    }
                                }
                            }
                        }
                    } else {
                        items(downloadedVideos, key = { it.id }) { vid ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF171923),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenVideo(vid) }
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    BharatTubeThumbnail(
                                        thumbnailKey = vid.thumbnailKey,
                                        contentDescription = vid.title,
                                        modifier = Modifier.size(96.dp, 56.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vid.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${vid.channelName} • ${prefs.downloadsQuality}", color = EmeraldVerified, style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(onClick = { onToggleDownloadVideo(vid) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove download", tint = LotusCoral)
                                    }
                                }
                            }
                        }
                    }
                }

                "privacy" -> {
                    item {
                        SettingsSwitchCard(
                            title = "Keep All My Subscriptions Private",
                            subtitle = "Other viewers won't see channels you follow",
                            checked = prefs.privacySubscriptionsPrivate,
                            onCheckedChange = onSetPrivacySubsPrivate
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Keep All Saved Playlists Private",
                            subtitle = "Only you can view your saved playlists",
                            checked = prefs.privacyPlaylistsPrivate,
                            onCheckedChange = onSetPrivacyPlaylistsPrivate
                        )
                    }
                    item {
                        SettingsSwitchCard(
                            title = "Restricted Mode",
                            subtitle = "Hide age-sensitive or flagged titles on this device",
                            checked = prefs.restrictedMode,
                            onCheckedChange = { onToggleRestrictedMode() }
                        )
                    }
                    item {
                        SettingsActionCard(
                            title = "Safety Reports & Support",
                            subtitle = "Manage submitted reports, appeals, and privacy help",
                            onClick = onOpenSupport
                        )
                    }
                }

                "history_privacy" -> {
                    item {
                        SettingsSwitchCard(
                            title = "Pause Watch History",
                            subtitle = "Videos you watch won't be recorded in History",
                            checked = prefs.historyPaused,
                            onCheckedChange = { onToggleHistoryPaused() }
                        )
                    }
                    item {
                        SettingsActionCard(
                            title = "Manage Watch History",
                            subtitle = "View or remove previously watched videos",
                            onClick = onOpenHistoryLibrary
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { confirmClearHistory = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = LotusCoral)
                            Spacer(Modifier.width(8.dp))
                            Text("Clear Watch & Search History", color = LotusCoral)
                        }
                    }
                }

                else -> { // connected_apps
                    item {
                        ConnectedAppStatusCard(
                            title = "Chhotu AI Official Assistant",
                            subtitle = "Connected in-app — Hindi & English creator support active",
                            status = "Connected",
                            statusColor = EmeraldVerified,
                            icon = Icons.Filled.SmartToy,
                            onClick = onOpenChhotuAi
                        )
                    }
                    item {
                        ConnectedAppStatusCard(
                            title = "BharatTube Creator Studio",
                            subtitle = "Local Room + DataStore channel sync active",
                            status = "Connected",
                            statusColor = EmeraldVerified,
                            icon = Icons.Filled.AccountCircle,
                            onClick = onOpenCreatorStudio
                        )
                    }
                    item {
                        ConnectedAppStatusCard(
                            title = "Cloud Account Sync",
                            subtitle = "Configure Google OAuth / Firebase in Cloud Console for cross-device sync",
                            status = "Local Mode",
                            statusColor = SaffronFlame,
                            icon = Icons.Filled.Link,
                            onClick = onOpenSupport
                        )
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(BharatTubeCatalog.initialLanguages) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLanguage(lang.code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (prefs.preferredLanguage == lang.code) "✓ " else ""}${lang.nativeName} — ${lang.englishName}",
                                color = if (prefs.preferredLanguage == lang.code) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text("Close") } }
        )
    }

    if (showCountryDialog) {
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            title = { Text("Select Country & Region") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(BharatTubeCatalog.countries) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCountry(c.code)
                                    showCountryDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (prefs.preferredCountry == c.code) "✓ " else ""}${c.name} (${c.currencyCode})",
                                color = if (prefs.preferredCountry == c.code) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCountryDialog = false }) { Text("Close") } }
        )
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

    if (confirmClearHistory) {
        AlertDialog(
            onDismissRequest = { confirmClearHistory = false },
            title = { Text("Clear All History?") },
            text = { Text("This clears your watch history and search history on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearWatchHistory()
                        onClearSearchHistory()
                        confirmClearHistory = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LotusCoral)
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearHistory = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171923),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
            }
            Spacer(Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SaffronFlame)
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171923),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF8B91A6))
        }
    }
}

@Composable
private fun ConnectedAppStatusCard(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171923),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = statusColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFA0A5B8))
            }
            Surface(shape = RoundedCornerShape(10.dp), color = statusColor.copy(alpha = 0.18f)) {
                Text(
                    text = status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
