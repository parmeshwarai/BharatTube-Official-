package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChannelEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ChannelLinkItem
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.CropTarget
import com.example.ui.components.ImageCropDialog
import com.example.ui.components.ProfileFormatters
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChannelScreen(
    channel: ChannelEntity?,
    onBack: () -> Unit,
    onSave: (
        channel: ChannelEntity,
        name: String,
        handle: String,
        description: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String,
        onSuccess: () -> Unit
    ) -> Unit
) {
    if (channel == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit profile") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Create a channel first to edit profile details.")
            }
        }
        return
    }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var name by remember(channel.id) { mutableStateOf(channel.name) }
    var handleInput by remember(channel.id) { mutableStateOf(channel.handle.removePrefix("@")) }
    var description by remember(channel.id) { mutableStateOf(channel.description) }
    var avatarUri by remember(channel.id) { mutableStateOf(channel.avatarUri) }
    var bannerUri by remember(channel.id) { mutableStateOf(channel.bannerUri) }
    val links = remember(channel.id) {
        mutableStateListOf<ChannelLinkItem>().apply {
            addAll(ProfileFormatters.parseSocialLinks(channel.socialLinks))
        }
    }

    var showAddLinkDialog by remember { mutableStateOf(false) }
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

    val cleanHandleSlug = handleInput.trim().removePrefix("@").replace(Regex("[^a-zA-Z0-9_.]"), "")
    val displayHandle = if (cleanHandleSlug.isNotBlank()) "@$cleanHandleSlug" else "@channel"
    val channelUrl = "bharattube.com/$displayHandle"

    val saveAction = {
        onSave(
            channel,
            name,
            displayHandle,
            description,
            ProfileFormatters.serializeSocialLinks(links),
            avatarUri,
            bannerUri,
            onBack
        )
    }

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF151821),
        unfocusedContainerColor = Color(0xFF151821),
        focusedBorderColor = SaffronFlame,
        unfocusedBorderColor = Color(0xFF2B3040),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
    )

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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("edit_channel_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = saveAction,
                        enabled = name.isNotBlank() && cleanHandleSlug.length >= 3,
                        modifier = Modifier.testTag("edit_channel_top_save")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save profile", tint = SaffronFlame)
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Edit profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Banner + Overlapping Circular Channel Logo
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(164.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .testTag("edit_banner_preview")
                        ) {
                            BharatTubeThumbnail(
                                thumbnailKey = bannerUri.ifBlank { "drawable:img_hero_creators" },
                                contentDescription = "Channel Banner",
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0x33000000), Color.Transparent, Color(0x77000000))
                                        )
                                    )
                            )
                            Surface(
                                onClick = {
                                    bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Change Banner",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.72f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .testTag("edit_banner_pill_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Edit banner", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(48.dp))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(96.dp)
                    ) {
                        ChannelLogoAvatar(
                            avatarUri = avatarUri,
                            contentDescription = "Channel Logo",
                            size = 96.dp,
                            borderWidth = 3.dp,
                            borderColor = Color(0xFF0E1016),
                            modifier = Modifier
                                .clickable {
                                    logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .testTag("edit_logo_avatar")
                        )
                        Surface(
                            onClick = {
                                logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = CircleShape,
                            color = SaffronFlame,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .border(2.dp, Color(0xFF0E1016), CircleShape)
                                .testTag("edit_logo_camera_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Change profile photo", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Change profile photo",
                        color = SaffronFlame,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Channel Name
            item {
                Text("Channel Name", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Enter channel name") },
                    singleLine = true,
                    colors = darkFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_channel_name_input")
                )
            }

            // Handle
            item {
                Text("Handle", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = handleInput,
                    onValueChange = { raw ->
                        handleInput = raw.removePrefix("@").replace(Regex("[^a-zA-Z0-9_.]"), "").take(28)
                    },
                    prefix = { Text("@", color = SaffronFlame, fontWeight = FontWeight.Bold) },
                    supportingText = {
                        Text(
                            "This is how people will find you on BharatTube",
                            color = Color(0xFF8B91A6)
                        )
                    },
                    singleLine = true,
                    colors = darkFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_channel_handle_input")
                )
            }

            // Channel URL + Copy
            item {
                Text("Channel URL", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF151821),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2B3040), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = channelUrl,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString("https://$channelUrl"))
                                Toast.makeText(context, "Channel URL copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_channel_url_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Channel URL", tint = Color(0xFFC5CAD8))
                        }
                    }
                }
            }

            // Description + 500 chars counter
            item {
                Text("Description", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 500) description = it },
                    placeholder = { Text("Tell viewers about your channel...") },
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "${description.length} / 500 characters",
                                color = Color(0xFF8B91A6)
                            )
                        }
                    },
                    colors = darkFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_channel_description_input")
                )
            }

            // Links
            item {
                Text("Links", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA0A5B8))
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF151821),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2B3040), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SaffronFlame.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.label.contains("Website", true)) Icons.Filled.Language else Icons.Filled.Public,
                                        contentDescription = item.label,
                                        tint = SaffronFlame,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.url, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.label, color = Color(0xFF8B91A6), style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { links.removeAt(index) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove link", tint = LotusCoral, modifier = Modifier.size(18.dp))
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF8B91A6))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { showAddLinkDialog = true },
                            modifier = Modifier.testTag("add_channel_link_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add link", color = SaffronFlame, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Bottom Save CTA
            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = saveAction,
                    enabled = name.isNotBlank() && cleanHandleSlug.length >= 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronFlame,
                        contentColor = Color(0xFF1A0E07)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_channel_edit_button")
                ) {
                    Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showAddLinkDialog) {
        var linkLabel by remember { mutableStateOf("Website") }
        var linkUrl by remember { mutableStateOf("") }
        val presets = listOf("Website", "Instagram", "X (Twitter)", "Facebook", "Telegram")
        AlertDialog(
            onDismissRequest = { showAddLinkDialog = false },
            title = { Text("Add Channel Link") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        presets.take(3).forEach { label ->
                            FilterChip(
                                selected = linkLabel == label,
                                onClick = { linkLabel = label },
                                label = { Text(label) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        presets.drop(3).forEach { label ->
                            FilterChip(
                                selected = linkLabel == label,
                                onClick = { linkLabel = label },
                                label = { Text(label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("URL or Handle") },
                        placeholder = { Text("e.g. instagram.com/yourhandle") },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (linkUrl.isNotBlank()) {
                            links.add(ChannelLinkItem(linkLabel, linkUrl.trim()))
                        }
                        showAddLinkDialog = false
                    },
                    enabled = linkUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLinkDialog = false }) { Text("Cancel") }
            }
        )
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
