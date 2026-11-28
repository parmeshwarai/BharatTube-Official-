package com.example.ui.screens

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.PlaylistEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDetailsScreen(
    videoUri: String,
    initialIsShort: Boolean,
    initialTitle: String = "",
    initialDescription: String = "",
    initialThumbnailUri: String = "",
    initialCategory: String = "Entertainment",
    playlists: List<PlaylistEntity>,
    onBack: () -> Unit,
    onCreatePlaylist: (String, String, Boolean) -> Unit,
    onPublish: (
        title: String,
        description: String,
        videoUri: String,
        thumbnailUri: String,
        format: VideoFormat,
        visibility: VideoVisibility,
        category: String,
        hashtags: String,
        allowComments: Boolean,
        audienceType: String,
        playlistId: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var customThumbnailUri by remember(initialThumbnailUri) { mutableStateOf(initialThumbnailUri) }
    var format by remember(initialIsShort) {
        mutableStateOf(if (initialIsShort) VideoFormat.SHORT else VideoFormat.LONG)
    }
    var visibility by remember { mutableStateOf(VideoVisibility.PUBLIC) }
    var category by remember(initialCategory) { mutableStateOf(initialCategory.ifBlank { "Entertainment" }) }
    var audienceType by remember { mutableStateOf("NOT_FOR_KIDS") }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var allowComments by remember { mutableStateOf(true) }

    var showAudienceDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    var fileNameLabel by remember { mutableStateOf("video_bharattube.mp4") }
    var durationLabel by remember { mutableStateOf("0:30") }
    var resolutionLabel by remember { mutableStateOf("1080p") }
    var framePreview by remember { mutableStateOf<Bitmap?>(null) }

    val thumbPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customThumbnailUri = uri.toString()
        }
    }

    LaunchedEffect(videoUri) {
        if (videoUri.isBlank()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val parsed = runCatching { Uri.parse(videoUri) }.getOrNull() ?: return@withContext
            // Resolve display name
            val resolvedName = runCatching {
                if (parsed.scheme == "content") {
                    context.contentResolver.query(parsed, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
                } else {
                    parsed.lastPathSegment
                }
            }.getOrNull()
            if (!resolvedName.isNullOrBlank()) {
                fileNameLabel = resolvedName
            }

            // Resolve duration + frame + resolution
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, parsed)
                val durMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val durSec = (durMs / 1000L).toInt()
                if (durSec > 0) {
                    durationLabel = BharatTubeCatalog.formatDuration(durSec)
                }
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
                resolutionLabel = when {
                    height >= 2160 -> "4K"
                    height >= 1080 -> "1080p"
                    height >= 720 -> "720p"
                    else -> "${height}p"
                }
                framePreview = retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
            } catch (_: Exception) {
            } finally {
                runCatching { retriever.release() }
            }
        }
    }

    val selectedPlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }
    val audienceLabel = if (audienceType == "MADE_FOR_KIDS") "Yes, made for kids" else "Not for kids"

    Scaffold(
        containerColor = Color(0xFF101218),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "BharatTube",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("upload_details_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101218))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Upload Details",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Add details to your video before publishing",
                        color = Color(0xFFA2A7B8),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Thumbnail Preview Card + Metadata
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.76f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B1F2A))
                            .clickable {
                                thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .testTag("upload_details_thumbnail_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            customThumbnailUri.isNotBlank() -> {
                                AsyncImage(
                                    model = customThumbnailUri,
                                    contentDescription = "Custom Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            framePreview != null -> {
                                Image(
                                    bitmap = framePreview!!.asImageBitmap(),
                                    contentDescription = "Video Frame Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                BharatTubeThumbnail(
                                    thumbnailKey = "drawable:img_thumb_travel",
                                    contentDescription = "Video Preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Video Preview",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Thumbnail", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$fileNameLabel • $durationLabel • $resolutionLabel",
                        color = Color(0xFF9EA4B4),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Title Input
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = Color(0xFF2B2F3C), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "Add Title",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. My travel vlog in the mountains", color = Color(0xFF8E94A4)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1F222C),
                            unfocusedContainerColor = Color(0xFF1F222C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SaffronFlame,
                            unfocusedBorderColor = Color(0xFF2D313F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_details_title_input")
                    )
                }
            }

            // Description Input
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = Color(0xFF2B2F3C), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "Add Description",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe what your video is about...", color = Color(0xFF8E94A4)) },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1F222C),
                            unfocusedContainerColor = Color(0xFF1F222C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SaffronFlame,
                            unfocusedBorderColor = Color(0xFF2D313F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_details_description_input")
                    )
                }
            }

            // Format & Category Quick Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(VideoFormat.LONG to "Video", VideoFormat.SHORT to "Short").forEach { (f, label) ->
                            FilterChip(
                                selected = format == f,
                                onClick = { format = f },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronFlame,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1F222C),
                                    labelColor = Color(0xFFB4BAC8)
                                )
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BharatTubeCatalog.categories.drop(1)) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronFlame,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1F222C),
                                    labelColor = Color(0xFFB4BAC8)
                                )
                            )
                        }
                    }
                }
            }

            // Visibility Cards: Public | Private | Unlisted
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Visibility",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisibilityOptionCard(
                            title = "Public",
                            subtitle = "Visible to everyone",
                            selected = visibility == VideoVisibility.PUBLIC,
                            tag = "visibility_public",
                            modifier = Modifier.weight(1f),
                            onClick = { visibility = VideoVisibility.PUBLIC }
                        )
                        VisibilityOptionCard(
                            title = "Private",
                            subtitle = "Only you can view",
                            selected = visibility == VideoVisibility.PRIVATE,
                            tag = "visibility_private",
                            modifier = Modifier.weight(1f),
                            onClick = { visibility = VideoVisibility.PRIVATE }
                        )
                        VisibilityOptionCard(
                            title = "Unlisted",
                            subtitle = "Anyone with the link",
                            selected = visibility == VideoVisibility.UNLISTED,
                            tag = "visibility_unlisted",
                            modifier = Modifier.weight(1f),
                            onClick = { visibility = VideoVisibility.UNLISTED }
                        )
                    }
                }
            }

            // Audience Row
            item {
                DetailsActionRow(
                    title = "Audience",
                    trailingValue = audienceLabel,
                    tag = "upload_details_audience_row",
                    onClick = { showAudienceDialog = true }
                )
            }

            // Playlist Row
            item {
                DetailsActionRow(
                    title = "Playlist",
                    trailingValue = "Add to playlist • ${selectedPlaylist?.name ?: "None selected"}",
                    tag = "upload_details_playlist_row",
                    onClick = { showPlaylistDialog = true }
                )
            }

            // Comments Switch Row
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1D202A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Allow comments",
                            color = Color(0xFFB4BAC8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(10.dp))
                        Switch(
                            checked = allowComments,
                            onCheckedChange = { allowComments = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SaffronFlame
                            ),
                            modifier = Modifier.testTag("upload_details_comments_switch")
                        )
                    }
                }
            }

            // Primary Orange Upload Button
            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        onPublish(
                            title,
                            description,
                            videoUri,
                            customThumbnailUri,
                            format,
                            visibility,
                            category,
                            "#BharatTube #Creators",
                            allowComments,
                            audienceType,
                            selectedPlaylistId
                        )
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronFlame,
                        contentColor = Color.Black,
                        disabledContainerColor = SaffronFlame.copy(alpha = 0.4f),
                        disabledContentColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("upload_details_submit_button")
                ) {
                    Text(
                        text = "Upload",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showAudienceDialog) {
        AlertDialog(
            onDismissRequest = { showAudienceDialog = false },
            title = { Text("Select Audience") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            audienceType = "NOT_FOR_KIDS"
                            showAudienceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (audienceType == "NOT_FOR_KIDS") "✓ Not for kids" else "Not for kids")
                    }
                    OutlinedButton(
                        onClick = {
                            audienceType = "MADE_FOR_KIDS"
                            showAudienceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (audienceType == "MADE_FOR_KIDS") "✓ Yes, made for kids" else "Yes, made for kids")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudienceDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            selectedPlaylistId = null
                            showPlaylistDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedPlaylistId == null) "✓ None selected" else "None")
                    }
                    playlists.forEach { pl ->
                        OutlinedButton(
                            onClick = {
                                selectedPlaylistId = pl.id
                                showPlaylistDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedPlaylistId == pl.id) "✓ ${pl.name}" else pl.name)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("New Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistName.trim(), "", false)
                                newPlaylistName = ""
                            }
                        },
                        enabled = newPlaylistName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Playlist")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun VisibilityOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) SaffronFlame else Color(0xFF2D313F)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1D202A))
            .border(1.4.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (selected) SaffronFlame else Color(0xFF7B8192))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Text(
            text = subtitle,
            color = if (selected) SaffronFlame else Color(0xFF9EA4B4),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2
        )
    }
}

@Composable
private fun DetailsActionRow(
    title: String,
    trailingValue: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1D202A),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = trailingValue,
                color = Color(0xFFB4BAC8),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFB4BAC8)
            )
        }
    }
}
