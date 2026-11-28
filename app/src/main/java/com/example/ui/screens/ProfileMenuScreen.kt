package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import com.example.data.model.ChannelEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.SwitchOrCreateAccountDialog
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuScreen(
    user: UserProfileEntity?,
    activeChannel: ChannelEntity?,
    allUsers: List<UserProfileEntity>,
    prefs: UserPreferences,
    onBack: () -> Unit,
    onManageAccount: () -> Unit,
    onOpenYourChannel: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenYourVideos: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenWatchLater: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelpFeedback: () -> Unit,
    onOpenCreatorStudio: () -> Unit,
    onOpenChhotuAi: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenAdvertiser: () -> Unit,
    onOpenSuperAdmin: () -> Unit,
    onOpenChhotuAdmin: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onCreateLocalAccount: (String, String, String, String, String, Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var showSwitchAccountDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val displayName = activeChannel?.name ?: user?.username ?: "BharatTube Creator"
    val displayHandle = activeChannel?.handle ?: user?.handle ?: "@bharattube"
    val avatarUri = activeChannel?.avatarUri?.takeIf { it.isNotBlank() } ?: user?.avatarUri.orEmpty()

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_menu_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Profile Card (Logo, Name, Handle, Manage Your Account)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161822),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Logo with warm gradient ring (Orange Lotus fallback)
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(listOf(SaffronFlame, LotusCoral, Color(0xFF5D76FF))),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF212432)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri.isNotBlank()) {
                                BharatTubeThumbnail(
                                    thumbnailKey = avatarUri,
                                    contentDescription = displayName,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                BharatTubeLogoMark(modifier = Modifier.size(44.dp))
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prefs.isSignedOut) "Signed Out" else displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (prefs.isSignedOut) "Tap Switch Account to sign in" else displayHandle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA0A5B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onManageAccount,
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(1.dp, Color(0xFF3A3F52)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("manage_your_account_button")
                            ) {
                                Text(
                                    text = "Manage Your Account",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Main 9-Item Profile Menu Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161822),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileMenuRow(
                            icon = Icons.Filled.SwapHoriz,
                            label = "Switch Account",
                            tag = "menu_switch_account",
                            onClick = { showSwitchAccountDialog = true }
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.AccountCircle,
                            label = "Your Channel",
                            tag = "menu_your_channel",
                            onClick = onOpenYourChannel
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.History,
                            label = "History",
                            tag = "menu_history",
                            onClick = onOpenHistory
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.VideoLibrary,
                            label = "Your Videos",
                            tag = "menu_your_videos",
                            onClick = onOpenYourVideos
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.Download,
                            label = "Downloads",
                            tag = "menu_downloads",
                            onClick = onOpenDownloads
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.WatchLater,
                            label = "Watch Later",
                            tag = "menu_watch_later",
                            onClick = onOpenWatchLater
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.Settings,
                            label = "Settings",
                            tag = "menu_settings",
                            onClick = onOpenSettings
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            label = "Help and Feedback",
                            tag = "menu_help_feedback",
                            onClick = onOpenHelpFeedback
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = if (prefs.isSignedOut) "Sign In" else "Sign Out",
                            tint = if (prefs.isSignedOut) SaffronFlame else LotusCoral,
                            labelColor = if (prefs.isSignedOut) SaffronFlame else LotusCoral,
                            tag = "menu_sign_out",
                            onClick = {
                                if (prefs.isSignedOut) {
                                    showSwitchAccountDialog = true
                                } else {
                                    showSignOutConfirm = true
                                }
                            }
                        )
                    }
                }
            }

            // Creator & Admin Tools (keeps all platform features accessible cleanly)
            item {
                Text(
                    text = "Creator & Platform Tools",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFA0A5B8),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161822),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileMenuRow(
                            icon = Icons.Filled.AccountBalanceWallet,
                            label = "Creator Studio",
                            tint = SaffronFlame,
                            tag = "menu_creator_studio",
                            onClick = onOpenCreatorStudio
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.SmartToy,
                            label = "Chhotu AI Assistant",
                            tint = SaffronFlame,
                            tag = "menu_chhotu_ai",
                            onClick = onOpenChhotuAi
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            label = "Messages & Community Chat",
                            tag = "menu_chat",
                            onClick = onOpenChat
                        )
                        MenuDivider()
                        ProfileMenuRow(
                            icon = Icons.Filled.Campaign,
                            label = "Advertiser Dashboard",
                            tag = "menu_advertiser",
                            onClick = onOpenAdvertiser
                        )
                        if (user?.isAdmin == true) {
                            MenuDivider()
                            ProfileMenuRow(
                                icon = Icons.Filled.AdminPanelSettings,
                                label = "Super Admin Dashboard",
                                tint = SaffronFlame,
                                tag = "menu_super_admin",
                                onClick = onOpenSuperAdmin
                            )
                            MenuDivider()
                            ProfileMenuRow(
                                icon = Icons.Filled.Security,
                                label = "Chhotu AI Control Center",
                                tint = SaffronFlame,
                                tag = "menu_chhotu_admin",
                                onClick = onOpenChhotuAdmin
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSwitchAccountDialog) {
        SwitchOrCreateAccountDialog(
            currentUser = user,
            allUsers = allUsers,
            defaultCountry = prefs.preferredCountry,
            defaultLanguage = prefs.preferredLanguage,
            onDismiss = { showSwitchAccountDialog = false },
            onSwitchUser = onSwitchUser,
            onCreateLocalAccount = onCreateLocalAccount
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out of BharatTube?") },
            text = { Text("Your channels, videos, and local history remain safely saved on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LotusCoral)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    tint: Color = Color(0xFFC5CAD8),
    labelColor: Color = Color.White,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF6B7185),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.06f),
        thickness = 0.8.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
