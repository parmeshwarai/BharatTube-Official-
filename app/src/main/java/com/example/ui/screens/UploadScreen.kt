package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.UploadStatus
import com.example.data.model.UploadTaskEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.SaffronFlame

@Composable
fun UploadScreen(
    uploads: List<UploadTaskEntity>,
    defaultLanguage: String,
    defaultCountry: String,
    onStartUpload: (
        title: String,
        description: String,
        videoUri: String,
        thumbnailUri: String,
        format: VideoFormat,
        visibility: VideoVisibility,
        category: String,
        hashtags: String,
        languageCode: String,
        countryCode: String
    ) -> Unit,
    onPauseUpload: (String) -> Unit,
    onResumeUpload: (String) -> Unit,
    onCancelUpload: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("#BharatTube #Creators") }
    var pickedVideoUri by remember { mutableStateOf("") }
    var pickedThumbUri by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(VideoFormat.LONG) }
    var visibility by remember { mutableStateOf(VideoVisibility.PUBLIC) }
    var category by remember { mutableStateOf("Technology") }
    var languageCode by remember(defaultLanguage) { mutableStateOf(defaultLanguage) }
    var copyrightConfirmed by remember { mutableStateOf(true) }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pickedVideoUri = it.toString() }
    }
    val thumbPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pickedThumbUri = it.toString() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Upload Video or Short", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Zero-permission Android Photo Picker with resumable chunked queue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            IntegrationDisclosureBanner(
                title = "Local Storage & Cloud Transcoding Pipeline",
                message = "Picked videos are processed and persisted locally on device. Connecting Cloud Storage & server-side HLS/4K transcoding enables global CDN playback."
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                    modifier = Modifier.weight(1f).testTag("pick_video_button")
                ) {
                    Icon(Icons.Filled.VideoFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (pickedVideoUri.isBlank()) "Pick Video" else "Video Ready ✓")
                }
                OutlinedButton(
                    onClick = { thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.weight(1f).testTag("pick_thumbnail_button")
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (pickedThumbUri.isBlank()) "Thumbnail" else "Thumb Ready ✓")
                }
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth().testTag("upload_title_input")
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Chapters") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text("Hashtags") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column {
                Text("Format", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(VideoFormat.LONG, VideoFormat.SHORT).forEach { f ->
                        FilterChip(
                            selected = format == f,
                            onClick = { format = f },
                            label = { Text(if (f == VideoFormat.LONG) "Long Video" else "Short / Reel") }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Visibility", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VideoVisibility.entries) { v ->
                        FilterChip(
                            selected = visibility == v,
                            onClick = { visibility = v },
                            label = { Text(v.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
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
                Spacer(Modifier.height(8.dp))
                Text("Language", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BharatTubeCatalog.initialLanguages.take(10)) { lang ->
                        FilterChip(
                            selected = languageCode == lang.code,
                            onClick = { languageCode = lang.code },
                            label = { Text(lang.englishName) }
                        )
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = copyrightConfirmed, onCheckedChange = { copyrightConfirmed = it })
                Text(
                    "I confirm this content uses original or properly licensed audio/video (Copyright Protection Check).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Button(
                onClick = {
                    onStartUpload(
                        title, description, pickedVideoUri, pickedThumbUri,
                        format, visibility, category, hashtags, languageCode, defaultCountry
                    )
                    if (title.isNotBlank()) title = ""
                },
                enabled = copyrightConfirmed && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("start_upload_button")
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Chunked Upload")
            }
        }

        if (uploads.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Upload Queue (${uploads.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(uploads, key = { it.id }) { task ->
                val pct = if (task.totalBytes > 0L) (task.uploadedBytes.toFloat() / task.totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
                val uploadedMb = task.uploadedBytes / (1024.0 * 1024.0)
                val totalMb = task.totalBytes / (1024.0 * 1024.0)
                val speedMb = task.speedBytesPerSec / (1024.0 * 1024.0)
                val remainingBytes = (task.totalBytes - task.uploadedBytes).coerceAtLeast(0L)
                val etaSec = if (task.speedBytesPerSec > 0) remainingBytes / task.speedBytesPerSec else 0L

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${task.status} • ${(pct * 100).toInt()}% • ${String.format("%.1f", uploadedMb)} / ${String.format("%.1f", totalMb)} MB",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (task.status == UploadStatus.COMPLETED.name) EmeraldVerified else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (task.status == UploadStatus.UPLOADING.name) {
                                    Text(
                                        "Speed: ${String.format("%.1f", speedMb)} MB/s • ETA: ${etaSec}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SaffronFlame
                                    )
                                }
                            }
                            if (task.status == UploadStatus.UPLOADING.name) {
                                IconButton(onClick = { onPauseUpload(task.id) }) {
                                    Icon(Icons.Filled.Pause, contentDescription = "Pause upload")
                                }
                            } else if (task.status == UploadStatus.PAUSED.name || task.status == UploadStatus.FAILED.name) {
                                IconButton(onClick = { onResumeUpload(task.id) }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Resume upload")
                                }
                            }
                            if (task.status != UploadStatus.COMPLETED.name && task.status != UploadStatus.CANCELLED.name) {
                                IconButton(onClick = { onCancelUpload(task.id) }) {
                                    Icon(Icons.Filled.Cancel, contentDescription = "Cancel upload")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (task.status == UploadStatus.COMPLETED.name) EmeraldVerified else SaffronFlame
                        )
                    }
                }
            }
        }
    }
}
