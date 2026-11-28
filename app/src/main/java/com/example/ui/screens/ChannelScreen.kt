package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.CropTarget
import com.example.ui.components.ImageCropDialog
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelId: String,
    observeChannel: (String) -> Flow<ChannelEntity?>,
    allVideos: List<VideoEntity>,
    communityPosts: List<CommunityPostEntity>,
    currentUserId: String,
    onBack: () -> Unit,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onToggleBell: (ChannelEntity) -> Unit,
    onOpenVideo: (VideoEntity) -> Unit,
    onToggleWatchLater: (VideoEntity) -> Unit,
    onCreatePost: (ChannelEntity, String) -> Unit,
    onUpdateChannel: (ChannelEntity) -> Unit = {}
) {
    val channel by observeChannel(channelId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableIntStateOf(0) }
    var postDraft by remember { mutableStateOf("") }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCropTarget by remember { mutableStateOf(CropTarget.LOGO_CIRCLE) }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pendingCropTarget = CropTarget.LOGO_CIRCLE
            pendingCropUri = uri
        }
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pendingCropTarget = CropTarget.BANNER_WIDE
            pendingCropUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(channel?.name ?: "Channel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val ch = channel ?: return@Scaffold Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Channel not found")
        }
        val isOwner = ch.ownerUserId == currentUserId
        val channelVideos = allVideos.filter {
            it.channelId == ch.id &&
                it.format != VideoFormat.SHORT.name &&
                (isOwner || it.chhotuCheckStatus == "APPROVED")
        }
        val channelShorts = allVideos.filter {
            it.channelId == ch.id &&
                it.format == VideoFormat.SHORT.name &&
                (isOwner || it.chhotuCheckStatus == "APPROVED")
        }
        val channelPosts = communityPosts.filter { it.channelId == ch.id }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(136.dp)
                                .then(
                                    if (isOwner) {
                                        Modifier.clickable {
                                            bannerPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
                            BharatTubeThumbnail(ch.bannerUri, ch.name, Modifier.fillMaxSize())
                            if (isOwner) {
                                Surface(
                                    onClick = {
                                        bannerPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.Black.copy(alpha = 0.62f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .testTag("edit_channel_banner_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, contentDescription = "Edit Banner", tint = Color.White, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text("Banner", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(38.dp))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                            .size(76.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral)))
                                .then(
                                    if (isOwner) {
                                        Modifier.clickable {
                                            logoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ch.avatarUri.isNotBlank()) {
                                BharatTubeThumbnail(ch.avatarUri, ch.name, Modifier.fillMaxSize())
                            } else {
                                Text(
                                    ch.name.take(1).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (isOwner) {
                            Surface(
                                onClick = {
                                    logoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = CircleShape,
                                color = SaffronFlame,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                    .testTag("edit_channel_logo_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = "Edit Logo", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(ch.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${ch.handle} • ${BharatTubeCatalog.formatCompactCount(ch.subscriberCount)} subscribers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (ch.isSubscribedByMe) {
                            OutlinedButton(onClick = { onToggleSubscribe(ch) }, modifier = Modifier.weight(1f)) {
                                Text("Subscribed")
                            }
                            IconButton(onClick = { onToggleBell(ch) }) {
                                Icon(
                                    if (ch.notificationsEnabled) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = SaffronFlame
                                )
                            }
                        } else {
                            Button(
                                onClick = { onToggleSubscribe(ch) },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Subscribe") }
                        }
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("Videos", "Shorts", "Community", "About").forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }

            when (selectedTab) {
                0 -> items(channelVideos, key = { it.id }) { v ->
                    VideoFeedCard(
                        video = v,
                        onClick = { onOpenVideo(v) },
                        onChannelClick = {},
                        onToggleWatchLater = { onToggleWatchLater(v) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                1 -> items(channelShorts, key = { it.id }) { s ->
                    VideoFeedCard(
                        video = s,
                        onClick = { onOpenVideo(s) },
                        onChannelClick = {},
                        onToggleWatchLater = { onToggleWatchLater(s) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                2 -> {
                    if (ch.ownerUserId == currentUserId) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = postDraft,
                                    onValueChange = { postDraft = it },
                                    placeholder = { Text("Share an update with subscribers...") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    onCreatePost(ch, postDraft)
                                    postDraft = ""
                                }) { Text("Post") }
                            }
                        }
                    }
                    items(channelPosts, key = { it.id }) { post ->
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(post.channelName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(post.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                else -> item {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("About ${ch.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(ch.description, style = MaterialTheme.typography.bodyMedium)
                        Text("Category: ${ch.category}", style = MaterialTheme.typography.bodySmall)
                        if (ch.socialLinks.isNotBlank()) {
                            Text("Links: ${ch.socialLinks}", style = MaterialTheme.typography.bodySmall, color = SaffronFlame)
                        }
                    }
                }
            }
        }
    }

    pendingCropUri?.let { uri ->
        val ch = channel
        ImageCropDialog(
            sourceUri = uri,
            cropTarget = pendingCropTarget,
            onDismiss = { pendingCropUri = null },
            onCropped = { savedPath ->
                if (ch != null) {
                    val updated = when (pendingCropTarget) {
                        CropTarget.LOGO_CIRCLE -> ch.copy(avatarUri = savedPath)
                        CropTarget.BANNER_WIDE -> ch.copy(bannerUri = savedPath)
                    }
                    onUpdateChannel(updated)
                }
                pendingCropUri = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    onBack: () -> Unit,
    onCreateChannel: (
        name: String,
        handle: String,
        description: String,
        category: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology") }
    var socialLinks by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf("") }
    var bannerUri by remember { mutableStateOf("") }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCropTarget by remember { mutableStateOf(CropTarget.LOGO_CIRCLE) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            pendingCropTarget = CropTarget.LOGO_CIRCLE
            pendingCropUri = it
        }
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            pendingCropTarget = CropTarget.BANNER_WIDE
            pendingCropUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Your Channel") },
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
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Channel Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("create_channel_name")
                )
            }
            item {
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Unique Handle (e.g. @mychannel) *") },
                    modifier = Modifier.fillMaxWidth().testTag("create_channel_handle")
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Channel Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text("Category", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BharatTubeCatalog.categories.drop(1)) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = socialLinks,
                    onValueChange = { socialLinks = it },
                    label = { Text("Social / Website Links") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (avatarUri.isBlank()) "Channel Avatar" else "Avatar Selected ✓") }
                    OutlinedButton(
                        onClick = { bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (bannerUri.isBlank()) "Channel Banner" else "Banner Selected ✓") }
                }
            }
            item {
                Button(
                    onClick = {
                        onCreateChannel(name, handle, description, category, socialLinks, avatarUri, bannerUri)
                    },
                    enabled = name.isNotBlank() && handle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("submit_create_channel")
                ) {
                    Text("Launch Channel")
                }
            }
        }
    }

    pendingCropUri?.let { uri ->
        ImageCropDialog(
            sourceUri = uri,
            cropTarget = pendingCropTarget,
            onDismiss = { pendingCropUri = null },
            onCropped = { savedPath ->
                when (pendingCropTarget) {
                    CropTarget.LOGO_CIRCLE -> avatarUri = savedPath
                    CropTarget.BANNER_WIDE -> bannerUri = savedPath
                }
                pendingCropUri = null
            }
        )
    }
}
