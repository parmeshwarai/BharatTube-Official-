package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.data.model.ChannelEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    activeChannel: ChannelEntity?,
    onBack: () -> Unit,
    onPublishPost: (ChannelEntity, String) -> Unit
) {
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Create Post", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeChannel == null) {
                Text(
                    text = "Create a channel in Profile first to publish a Community Post.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activeChannel.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(activeChannel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${activeChannel.handle} • Public Post", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("Share an update, question, or poll with your BharatTube subscribers...") },
                    minLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_input")
                )

                Button(
                    onClick = {
                        onPublishPost(activeChannel, postText)
                        onBack()
                    },
                    enabled = postText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_post_submit")
                ) {
                    Text("Publish Post", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
