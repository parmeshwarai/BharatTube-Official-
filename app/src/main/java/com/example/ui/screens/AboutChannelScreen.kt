package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.components.ProfileFormatters
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutChannelScreen(
    channel: ChannelEntity?,
    channelVideos: List<VideoEntity>,
    countryCode: String,
    onBack: () -> Unit,
    onReportChannel: (targetType: String, targetId: String, targetTitle: String, reason: String, details: String) -> Unit
) {
    if (channel == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Channel information unavailable")
            }
        }
        return
    }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val channelUrl = "https://bharattube.com/${channel.handle}"
    val totalViews = remember(channelVideos) { channelVideos.sumOf { it.viewCount } }
    val formattedViews = remember(totalViews) { NumberFormat.getNumberInstance(Locale.US).format(totalViews) }
    val joinedDate = remember(channel.createdAt) { ProfileFormatters.formatJoinedDate(channel.createdAt) }
    val countryName = remember(countryCode) {
        BharatTubeCatalog.countries.firstOrNull { it.code == countryCode }?.name ?: "India"
    }
    val parsedLinks = remember(channel.socialLinks) {
        ProfileFormatters.parseSocialLinks(channel.socialLinks)
    }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("about_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy channel URL") },
                                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    clipboard.setText(AnnotatedString(channelUrl))
                                    Toast.makeText(context, "Channel URL copied!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share channel") },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Check out ${channel.name} (${channel.handle}) on BharatTube: $channelUrl")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Channel"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report channel", color = LotusCoral) },
                                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = LotusCoral) },
                                onClick = {
                                    showOverflowMenu = false
                                    showReportDialog = true
                                }
                            )
                        }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161822),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top Channel Identity + Copy URL pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelLogoAvatar(
                                avatarUri = channel.avatarUri,
                                contentDescription = channel.name,
                                size = 60.dp,
                                borderWidth = 2.dp,
                                borderColor = SaffronFlame
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = channel.handle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA0A5B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = {
                                    clipboard.setText(AnnotatedString(channelUrl))
                                    Toast.makeText(context, "Channel URL copied!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF282B36),
                                modifier = Modifier.testTag("about_copy_url_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy URL", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Text(
                            text = "${BharatTubeCatalog.formatCompactCount(channel.subscriberCount)} subscribers • ${channelVideos.size} videos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = channel.description.ifBlank {
                                    "${channel.name} creates original videos, Shorts, and live streams on BharatTube."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD5D9E6)
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Joined date, Total views, Location
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AboutInfoRow(icon = Icons.Filled.CalendarToday, text = "Joined $joinedDate")
                            AboutInfoRow(icon = Icons.Filled.Visibility, text = "$formattedViews total views")
                            AboutInfoRow(icon = Icons.Filled.LocationOn, text = "Location • $countryName")
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Links
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Links",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (parsedLinks.isEmpty()) {
                                Text(
                                    text = "No external links added yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8B91A6)
                                )
                            } else {
                                parsedLinks.forEach { link ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val safeUrl = if (link.url.startsWith("http://") || link.url.startsWith("https://")) {
                                                    link.url
                                                } else {
                                                    "https://${link.url}"
                                                }
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
                                                }.onFailure {
                                                    clipboard.setText(AnnotatedString(link.url))
                                                    Toast.makeText(context, "Copied ${link.url}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (link.label.contains("Website", true)) Icons.Filled.Language else Icons.Filled.Public,
                                            contentDescription = link.label,
                                            tint = Color(0xFFA0A5B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "${link.label} • ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = link.url,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF7FA6FF),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Report channel button
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Surface(
                                onClick = { showReportDialog = true },
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFF282B36),
                                modifier = Modifier.testTag("about_report_channel_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Report channel",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFC5CAD8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Report channel",
                                        tint = Color(0xFFC5CAD8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("Spam or Misleading") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report ${channel.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Spam or Misleading", "Harassment", "Copyright Infringement", "Impersonation").forEach { r ->
                        FilterChip(selected = reason == r, onClick = { reason = r }, label = { Text(r) })
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Additional details (optional)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportChannel("CHANNEL", channel.id, channel.name, reason, details)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AboutInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFA0A5B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
