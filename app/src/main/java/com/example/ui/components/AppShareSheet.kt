package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SaffronFlame

object BharatTubeShareHelper {
    fun buildShareLink(packageName: String): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    fun buildShareMessage(packageName: String): String =
        "BharatTube par videos, Shorts aur creator channels dekho! Abhi download karo: ${buildShareLink(packageName)}"

    fun shareToTargetApp(
        context: Context,
        appLabel: String,
        packageCandidates: List<String>,
        shareMessage: String
    ) {
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "BharatTube App")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        for (pkg in packageCandidates) {
            try {
                val targeted = Intent(baseIntent).apply {
                    setPackage(pkg)
                }
                context.startActivity(targeted)
                return
            } catch (_: ActivityNotFoundException) {
                // Try next package variant or fall back to Android share sheet
            }
        }

        Toast.makeText(
            context,
            "$appLabel not found on device — opening Share menu",
            Toast.LENGTH_SHORT
        ).show()
        openSystemShareSheet(context, shareMessage)
    }

    fun openSystemShareSheet(context: Context, shareMessage: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "BharatTube App")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        context.startActivity(Intent.createChooser(sendIntent, "App Share Karo - BharatTube"))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShareBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareLink = remember(context.packageName) {
        BharatTubeShareHelper.buildShareLink(context.packageName)
    }
    val shareMessage = remember(context.packageName) {
        BharatTubeShareHelper.buildShareMessage(context.packageName)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "App Share",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "App Share Karo - Dosto ko BharatTube bhejo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Generated Share Link Preview + Copy button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_share_link_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BharatTube Share Link",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaffronFlame,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = shareLink,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(shareMessage))
                            Toast.makeText(context, "Share link copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("app_share_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Share Link",
                            tint = SaffronFlame
                        )
                    }
                }
            }

            // Social App Targets: WhatsApp, Telegram, Instagram, Facebook
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SocialShareOption(
                    label = "WhatsApp",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    badgeColor = Color(0xFF25D366),
                    tag = "share_whatsapp",
                    onClick = {
                        BharatTubeShareHelper.shareToTargetApp(
                            context = context,
                            appLabel = "WhatsApp",
                            packageCandidates = listOf("com.whatsapp", "com.whatsapp.w4b"),
                            shareMessage = shareMessage
                        )
                    }
                )
                SocialShareOption(
                    label = "Telegram",
                    icon = Icons.AutoMirrored.Filled.Send,
                    badgeColor = Color(0xFF0088CC),
                    tag = "share_telegram",
                    onClick = {
                        BharatTubeShareHelper.shareToTargetApp(
                            context = context,
                            appLabel = "Telegram",
                            packageCandidates = listOf("org.telegram.messenger", "org.telegram.messenger.web"),
                            shareMessage = shareMessage
                        )
                    }
                )
                SocialShareOption(
                    label = "Instagram",
                    icon = Icons.Filled.CameraAlt,
                    badgeColor = Color(0xFFE1306C),
                    tag = "share_instagram",
                    onClick = {
                        BharatTubeShareHelper.shareToTargetApp(
                            context = context,
                            appLabel = "Instagram",
                            packageCandidates = listOf("com.instagram.android"),
                            shareMessage = shareMessage
                        )
                    }
                )
                SocialShareOption(
                    label = "Facebook",
                    icon = Icons.Filled.Public,
                    badgeColor = Color(0xFF1877F2),
                    tag = "share_facebook",
                    onClick = {
                        BharatTubeShareHelper.shareToTargetApp(
                            context = context,
                            appLabel = "Facebook",
                            packageCandidates = listOf("com.facebook.katana", "com.facebook.lite"),
                            shareMessage = shareMessage
                        )
                    }
                )
            }

            Button(
                onClick = {
                    BharatTubeShareHelper.openSystemShareSheet(context, shareMessage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_more_apps_button")
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("More Share Options", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SocialShareOption(
    label: String,
    icon: ImageVector,
    badgeColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Share on $label",
                tint = badgeColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
