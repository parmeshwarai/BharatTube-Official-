package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessageEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeVideoPlayer
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    videoId: String,
    observeVideo: (String) -> Flow<VideoEntity?>,
    user: UserProfileEntity?,
    messages: List<ChatMessageEntity>,
    onBack: () -> Unit,
    onSendLiveChat: (String, String, Boolean, String) -> Unit,
    onReportStream: (VideoEntity) -> Unit
) {
    val stream by observeVideo(videoId).collectAsStateWithLifecycle(initialValue = null)
    var chatDraft by remember { mutableStateOf("") }
    val roomId = "live_$videoId"
    val roomMessages = messages.filter { it.roomId == roomId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stream?.channelName ?: "Live Stream") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    stream?.let {
                        IconButton(onClick = { onReportStream(it) }) {
                            Icon(Icons.Filled.Flag, contentDescription = "Report stream")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val s = stream
        if (s == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Live stream not found")
            }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BharatTubeVideoPlayer(
                videoUri = s.videoUri,
                initialPositionMs = 0L,
                captionsText = s.captionsText,
                qualityLabel = "1080p",
                onSavePosition = {},
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = LotusCoral, shape = RoundedCornerShape(6.dp)) {
                        Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Visibility, contentDescription = null, tint = SaffronFlame)
                    Spacer(Modifier.width(4.dp))
                    Text("${s.liveViewerCount} watching", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(4.dp))
                Text(s.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (user?.isMinor == true) {
                IntegrationDisclosureBanner(
                    title = "Minor Safety Active",
                    message = "Live chat participation and creator monetization are restricted for minor accounts.",
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roomMessages, key = { it.id }) { msg ->
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(msg.senderName, style = MaterialTheme.typography.labelMedium, color = SaffronFlame, fontWeight = FontWeight.Bold)
                            Text(msg.text, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatDraft,
                    onValueChange = { chatDraft = it },
                    placeholder = { Text("Say something in live chat...") },
                    enabled = user?.isMinor != true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onSendLiveChat(roomId, s.title, true, chatDraft)
                        chatDraft = ""
                    },
                    enabled = user?.isMinor != true && chatDraft.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send live chat", tint = SaffronFlame)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: UserProfileEntity?,
    messages: List<ChatMessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (String, String, Boolean, String, Int) -> Unit,
    onReportChat: (String, String) -> Unit
) {
    val rooms = listOf(
        Triple("room_creators_hub", "India Creators Guild", true),
        Triple("room_dm_kabir", "Kabir Expeditions", false)
    )
    var activeRoom by remember { mutableStateOf(rooms.first()) }
    var draft by remember { mutableStateOf("") }
    val roomMessages = messages.filter { it.roomId == activeRoom.first }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BharatTube Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onReportChat(activeRoom.first, activeRoom.second) }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report conversation")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rooms) { room ->
                    FilterChip(
                        selected = activeRoom.first == room.first,
                        onClick = { activeRoom = room },
                        label = { Text(if (room.third) "# ${room.second}" else room.second) }
                    )
                }
            }
            if (user?.isMinor == true) {
                IntegrationDisclosureBanner(
                    title = "Minor Protection Policy",
                    message = "Private and group messaging are disabled for accounts marked as minors.",
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roomMessages, key = { it.id }) { msg ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (msg.senderId == user?.uid) SaffronFlame.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(msg.senderName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            if (msg.voiceDurationSec > 0) {
                                Text("Voice message (${msg.voiceDurationSec}s)", style = MaterialTheme.typography.bodySmall, color = SaffronFlame)
                            }
                            if (msg.text.isNotBlank()) {
                                Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Message ${activeRoom.second}...") },
                    enabled = user?.isMinor != true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onSendMessage(activeRoom.first, activeRoom.second, activeRoom.third, "Voice clip note", 8)
                    },
                    enabled = user?.isMinor != true
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Send voice message")
                }
                IconButton(
                    onClick = {
                        onSendMessage(activeRoom.first, activeRoom.second, activeRoom.third, draft, 0)
                        draft = ""
                    },
                    enabled = user?.isMinor != true && draft.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = SaffronFlame)
                }
            }
        }
    }
}
