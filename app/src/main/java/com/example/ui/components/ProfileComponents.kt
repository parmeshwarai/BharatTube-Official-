package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChannelLinkItem(
    val label: String,
    val url: String
)

object ProfileFormatters {
    fun formatRelativeDate(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        val diff = (nowMs - timestampMs).coerceAtLeast(0L)
        val minutes = diff / 60_000L
        val hours = diff / 3_600_000L
        val days = diff / 86_400_000L
        return when {
            minutes < 1L -> "Just now"
            minutes < 60L -> "$minutes min ago"
            hours < 24L -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            days < 7L -> if (days == 1L) "1 day ago" else "$days days ago"
            days < 30L -> {
                val weeks = (days / 7L).coerceAtLeast(1L)
                if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
            }
            days < 365L -> {
                val months = (days / 30L).coerceAtLeast(1L)
                if (months == 1L) "1 month ago" else "$months months ago"
            }
            else -> {
                val years = (days / 365L).coerceAtLeast(1L)
                if (years == 1L) "1 year ago" else "$years years ago"
            }
        }
    }

    fun formatJoinedDate(timestampMs: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestampMs))
    }

    fun parseSocialLinks(raw: String): List<ChannelLinkItem> {
        if (raw.isBlank()) return emptyList()
        return raw.split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { entry ->
                if ("|" in entry) {
                    val parts = entry.split("|", limit = 2)
                    val label = parts[0].trim().ifBlank { inferLinkLabel(parts[1]) }
                    val url = parts[1].trim()
                    ChannelLinkItem(label, url)
                } else {
                    ChannelLinkItem(inferLinkLabel(entry), entry)
                }
            }
    }

    fun serializeSocialLinks(items: List<ChannelLinkItem>): String {
        return items
            .filter { it.url.isNotBlank() }
            .joinToString("\n") { "${it.label.trim().ifBlank { inferLinkLabel(it.url) }}|${it.url.trim()}" }
    }

    fun inferLinkLabel(url: String): String {
        val lower = url.lowercase()
        return when {
            "instagram" in lower -> "Instagram"
            "x.com" in lower || "twitter" in lower -> "X (Twitter)"
            "facebook" in lower || "fb.com" in lower -> "Facebook"
            "t.me" in lower || "telegram" in lower -> "Telegram"
            else -> "Website"
        }
    }
}

@Composable
fun ChannelLogoAvatar(
    avatarUri: String,
    contentDescription: String,
    size: Dp = 76.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .background(Color(0xFF1D1B22)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri.isNotBlank()) {
            BharatTubeThumbnail(
                thumbnailKey = avatarUri,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFE0D2), Color(0xFFF6A4A1).copy(alpha = 0.92f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                BharatTubeLogoMark(modifier = Modifier.size(size * 0.62f))
            }
        }
    }
}

@Composable
fun OwnerCrownControlIcon(
    modifier: Modifier = Modifier,
    tint: Color = com.example.ui.theme.AmberGold
) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Golden Crown sitting atop a control ring
        val crownPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.24f, h * 0.44f)
            lineTo(w * 0.20f, h * 0.16f)
            lineTo(w * 0.38f, h * 0.30f)
            lineTo(cx, h * 0.10f)
            lineTo(w * 0.62f, h * 0.30f)
            lineTo(w * 0.80f, h * 0.16f)
            lineTo(w * 0.76f, h * 0.44f)
            close()
        }
        drawPath(crownPath, color = tint)

        // Small crown base band + control gear ring below
        val cy = h * 0.68f
        val r = w * 0.22f
        drawCircle(
            color = tint,
            radius = r,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
        )
        drawCircle(
            color = tint,
            radius = w * 0.07f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )
    }
}

@Composable
fun SwitchOrCreateAccountDialog(
    currentUser: UserProfileEntity?,
    allUsers: List<UserProfileEntity>,
    defaultCountry: String,
    defaultLanguage: String,
    onDismiss: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onCreateLocalAccount: (
        username: String,
        email: String,
        phone: String,
        countryCode: String,
        languageCode: String,
        isMinor: Boolean
    ) -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var isMinor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showCreateForm) "Create BharatTube Account" else "Switch Account",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!showCreateForm) {
                    allUsers.forEach { acc ->
                        val isActive = currentUser?.uid == acc.uid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) SaffronFlame.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSwitchUser(acc.uid)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelLogoAvatar(
                                    avatarUri = acc.avatarUri,
                                    contentDescription = acc.username,
                                    size = 40.dp,
                                    borderWidth = 1.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${acc.handle}${if (acc.isAdmin) " • Admin" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isActive) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = EmeraldVerified)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showCreateForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Account")
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMinor, onCheckedChange = { isMinor = it })
                        Text(
                            "Minor account (enforces chat safety)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (showCreateForm) {
                Button(
                    onClick = {
                        onCreateLocalAccount(newName, newEmail, newPhone, defaultCountry, defaultLanguage, isMinor)
                        onDismiss()
                    },
                    enabled = newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                ) {
                    Text("Create")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Done") }
            }
        },
        dismissButton = {
            if (showCreateForm) {
                TextButton(onClick = { showCreateForm = false }) { Text("Back") }
            }
        }
    )
}
