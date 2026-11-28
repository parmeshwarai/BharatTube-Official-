package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.ProfileFormatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourVideosScreen(
    channel: ChannelEntity?,
    myChannels: List<ChannelEntity>,
    allVideos: List<VideoEntity>,
    onBack: () -> Unit,
    onUploadClick: () -> Unit,
    onOpenProfileMenu: () -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onOpenChhotuCheck: (VideoEntity) -> Unit = {},
    onOpenAnalytics: (VideoEntity) -> Unit = {},
    onUpdateVideo: (
        videoId: String,
        title: String,
        description: String,
        visibility: VideoVisibility,
        category: String
    ) -> Unit,
    onToggleHideVideo: (VideoEntity) -> Unit,
    onDeleteVideo: (VideoEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var deletingVideo by remember { mutableStateOf<VideoEntity?>(null) }

    val myChannelIds = remember(myChannels, channel) {
        (myChannels.map { it.id } + listOfNotNull(channel?.id)).toSet()
    }
    val ownedVideos = remember(allVideos, myChannelIds) {
        allVideos.filter { it.channelId in myChannelIds }
    }

    val filteredVideos = remember(ownedVideos, selectedTab) {
        when (selectedTab) {
            0 -> ownedVideos.filter {
                it.format == VideoFormat.LONG.name && it.visibility != VideoVisibility.DRAFT.name
            }
            1 -> ownedVideos.filter {
                it.format == VideoFormat.SHORT.name && it.visibility != VideoVisibility.DRAFT.name
            }
            2 -> ownedVideos.filter {
                (it.format == VideoFormat.LIVE.name || it.isLive) && it.visibility != VideoVisibility.DRAFT.name
            }
            else -> ownedVideos.filter {
                it.visibility == VideoVisibility.DRAFT.name
            }
        }
    }
    val totalViews = remember(filteredVideos) { filteredVideos.sumOf { it.viewCount } }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "BharatTube",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("your_videos_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = onUploadClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaffronFlame,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("your_videos_upload_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Upload", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    ChannelLogoAvatar(
                        avatarUri = channel?.avatarUri.orEmpty(),
                        contentDescription = "Profile menu",
                        size = 34.dp,
                        borderWidth = 1.5.dp,
                        borderColor = Color(0xFF3A3F52),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable(onClick = onOpenProfileMenu)
                            .testTag("your_videos_avatar")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabs = listOf("Videos", "Shorts", "Live", "Drafts")
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
                        modifier = Modifier.testTag("your_videos_tab_${title.lowercase()}")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Your Videos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredVideos.size} videos • ${BharatTubeCatalog.formatCompactCount(totalViews)} total views",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA0A5B8)
                        )
                    }
                }

                if (filteredVideos.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF161822),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No ${tabs[selectedTab].lowercase()} yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap + Upload to publish your next video or Short to BharatTube.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA0A5B8)
                                )
                                Button(
                                    onClick = onUploadClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                                ) {
                                    Text("Upload Now")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredVideos, key = { it.id }) { video ->
                        YourVideoManageCard(
                            video = video,
                            onClick = {
                                if (video.chhotuCheckStatus != "APPROVED") {
                                    onOpenChhotuCheck(video)
                                } else {
                                    onOpenVideo(video)
                                }
                            },
                            onOpenChhotuCheck = { onOpenChhotuCheck(video) },
                            onOpenAnalytics = { onOpenAnalytics(video) },
                            onEdit = { editingVideo = video },
                            onDelete = { deletingVideo = video },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, video.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Watch '${video.title}' on BharatTube: https://bharattube.com/watch/${video.id}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                            },
                            onToggleHide = { onToggleHideVideo(video) }
                        )
                    }
                }
            }
        }
    }

    editingVideo?.let { video ->
        EditVideoMetadataDialog(
            video = video,
            onDismiss = { editingVideo = null },
            onSave = { title, desc, vis, cat ->
                onUpdateVideo(video.id, title, desc, vis, cat)
                editingVideo = null
            }
        )
    }

    deletingVideo?.let { video ->
        AlertDialog(
            onDismissRequest = { deletingVideo = null },
            title = { Text("Delete Video?") },
            text = { Text("Permanently remove '${video.title}' from your channel? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVideo(video)
                        deletingVideo = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LotusCoral)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVideo = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun YourVideoManageCard(
    video: VideoEntity,
    onClick: () -> Unit,
    onOpenChhotuCheck: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onToggleHide: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isHidden = video.visibility == VideoVisibility.PRIVATE.name
    val dateLabel = remember(video.createdAt) { ProfileFormatters.formatRelativeDate(video.createdAt) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF171923),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("your_video_row_${video.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(134.dp)
                    .height(78.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0E1016))
            ) {
                BharatTubeThumbnail(
                    thumbnailKey = video.thumbnailKey,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = if (video.isLive) LotusCoral else Color.Black.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                ) {
                    Text(
                        text = if (video.isLive) "LIVE" else BharatTubeCatalog.formatDuration(video.durationSeconds),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${BharatTubeCatalog.formatCompactCount(video.viewCount)} views • $dateLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA0A5B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (video.visibility != VideoVisibility.PUBLIC.name) {
                        val statusColor = when (video.visibility) {
                            VideoVisibility.PRIVATE.name -> LotusCoral
                            VideoVisibility.UNLISTED.name -> SaffronFlame
                            else -> Color(0xFF8B91A6)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (isHidden) "Hidden (Private)" else video.visibility.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Public",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldVerified
                        )
                    }

                    val chhotuColor = when (video.chhotuCheckStatus) {
                        "APPROVED" -> EmeraldVerified
                        "REJECTED" -> LotusCoral
                        else -> AmberGold
                    }
                    val chhotuLabel = when (video.chhotuCheckStatus) {
                        "APPROVED" -> "Chhotu Approved"
                        "REJECTED" -> "Rejected • Fix"
                        else -> "Checking..."
                    }
                    Surface(
                        onClick = onOpenChhotuCheck,
                        shape = RoundedCornerShape(6.dp),
                        color = chhotuColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = chhotuLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = chhotuColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("your_video_menu_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Video options",
                        tint = Color(0xFFC5CAD8)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Analytics") },
                        leadingIcon = { Icon(Icons.Filled.BarChart, contentDescription = null, tint = SaffronFlame) },
                        onClick = {
                            menuExpanded = false
                            onOpenAnalytics()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Chhotu AI Check") },
                        leadingIcon = { Icon(Icons.Filled.SmartToy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpenChhotuCheck()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isHidden) "Unhide (Public)" else "Hide") },
                        leadingIcon = {
                            Icon(
                                if (isHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleHide()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = LotusCoral) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = LotusCoral) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditVideoMetadataDialog(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, visibility: VideoVisibility, category: String) -> Unit
) {
    var title by remember(video.id) { mutableStateOf(video.title) }
    var description by remember(video.id) { mutableStateOf(video.description) }
    var visibility by remember(video.id) {
        mutableStateOf(runCatching { VideoVisibility.valueOf(video.visibility) }.getOrDefault(VideoVisibility.PUBLIC))
    }
    var category by remember(video.id) { mutableStateOf(video.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Video Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Visibility", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(VideoVisibility.PUBLIC, VideoVisibility.UNLISTED, VideoVisibility.PRIVATE, VideoVisibility.DRAFT).forEach { vis ->
                        FilterChip(
                            selected = visibility == vis,
                            onClick = { visibility = vis },
                            label = { Text(vis.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BharatTubeCatalog.categories.drop(1)) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, visibility, category) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
