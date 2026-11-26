package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame

@Composable
fun ProfileScreen(
    user: UserProfileEntity?,
    allUsers: List<UserProfileEntity>,
    myChannels: List<ChannelEntity>,
    prefs: UserPreferences,
    onCreateChannel: () -> Unit,
    onOpenChannel: (String) -> Unit,
    onOpenCreatorStudio: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAdvertiser: () -> Unit,
    onOpenSuperAdmin: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenSupport: () -> Unit,
    onUpdateProfile: (UserProfileEntity) -> Unit,
    onCreateLocalAccount: (String, String, String, String, String, Boolean) -> Unit,
    onSwitchUser: (String) -> Unit,
    onToggleRestrictedMode: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectCountry: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(SaffronFlame, LotusCoral))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.username?.take(1) ?: "B").uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.username ?: "Loading...",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (user?.isAdmin == true) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(color = EmeraldVerified.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "ADMIN",
                                            color = EmeraldVerified,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${user?.handle ?: ""} • ID: ${user?.uid ?: ""} • ${prefs.preferredCountry}/${prefs.preferredLanguage.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showEditDialog = true }, modifier = Modifier.testTag("edit_profile_button")) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                        }
                    }
                    if (!user?.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(user!!.bio, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onCreateChannel,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                            modifier = Modifier.weight(1f).testTag("profile_create_channel_button")
                        ) {
                            Icon(Icons.Filled.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create Channel")
                        }
                        OutlinedButton(
                            onClick = { showAccountDialog = true },
                            modifier = Modifier.weight(1f).testTag("switch_account_button")
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Accounts")
                        }
                    }
                }
            }
        }

        // Auth Setup Disclosure
        item {
            IntegrationDisclosureBanner(
                title = "Authentication & Cloud Sync Status",
                message = "Currently running on local device persistence (Room + DataStore). Enable Firebase Auth / Google OAuth 2.0, Apple Sign-In, or OTP gateway credentials in Cloud Console to enable cross-device cloud login."
            )
        }

        // My Channels
        if (myChannels.isNotEmpty()) {
            item {
                Text("My Channels (${myChannels.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myChannels.forEach { ch ->
                        Surface(
                            onClick = { onOpenChannel(ch.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ch.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${ch.handle} • ${BharatTubeCatalog.formatCompactCount(ch.subscriberCount)} subscribers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Platform Hub Navigation
        item {
            Text("Creator, Business & Platform Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            ProfileNavTile(
                icon = Icons.Filled.AccountBalanceWallet,
                title = "Creator Studio & Monetization",
                subtitle = "Analytics, 50/50 revenue sharing & ₹1,000 India payout",
                tag = "nav_creator_studio",
                onClick = onOpenCreatorStudio
            )
        }
        item {
            ProfileNavTile(
                icon = Icons.Filled.VideoLibrary,
                title = "Watch History, Watch Later & Playlists",
                subtitle = "Continue watching, pause/clear history, custom playlists",
                tag = "nav_library",
                onClick = onOpenLibrary
            )
        }
        item {
            ProfileNavTile(
                icon = Icons.Filled.Campaign,
                title = "Advertiser Dashboard",
                subtitle = "Create campaigns, country/language targeting & Meta test status",
                tag = "nav_advertiser",
                onClick = onOpenAdvertiser
            )
        }
        if (user?.isAdmin == true) {
            item {
                ProfileNavTile(
                    icon = Icons.Filled.AdminPanelSettings,
                    title = "Super Admin Dashboard",
                    subtitle = "Global ON/OFF switches, 50:50 split rules, bans, audit logs",
                    tag = "nav_super_admin",
                    onClick = onOpenSuperAdmin
                )
            }
        }
        item {
            ProfileNavTile(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Messages & Community Chat",
                subtitle = "Direct & group chat with minor-safety protections",
                tag = "nav_chat",
                onClick = onOpenChat
            )
        }
        item {
            ProfileNavTile(
                icon = Icons.Filled.Language,
                title = "Country & Language (${prefs.preferredCountry} • ${prefs.preferredLanguage.uppercase()})",
                subtitle = "20 initial languages, local currency & regional feeds",
                tag = "nav_region",
                onClick = { showRegionDialog = true }
            )
        }
        item {
            ProfileNavTile(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help, Copyright Appeals & Support Tickets",
                subtitle = "FAQ, account recovery, payment & copyright support",
                tag = "nav_support",
                onClick = onOpenSupport
            )
        }

        // Safety & Privacy Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = SaffronFlame)
                        Spacer(Modifier.width(8.dp))
                        Text("Safety & Security", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Restricted Mode", style = MaterialTheme.typography.bodyMedium)
                            Text("Filters mature/age-restricted titles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = prefs.restrictedMode, onCheckedChange = { onToggleRestrictedMode() })
                    }
                    if (user != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Two-Factor Authentication (2FA)", style = MaterialTheme.typography.bodyMedium)
                                Text("Protects admin & payout settings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = user.twoFactorEnabled,
                                onCheckedChange = { onUpdateProfile(user.copy(twoFactorEnabled = it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && user != null) {
        var name by remember { mutableStateOf(user.username) }
        var bio by remember { mutableStateOf(user.bio) }
        var upi by remember { mutableStateOf(user.upiId) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") })
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })
                    OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI ID (for India Payouts)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(user.copy(username = name.ifBlank { user.username }, bio = bio, upiId = upi))
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAccountDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var isMinor by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Switch or Create Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allUsers.forEach { acc ->
                        OutlinedButton(
                            onClick = {
                                onSwitchUser(acc.uid)
                                showAccountDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${acc.username} (${acc.handle})${if (acc.isAdmin) " ★ Admin" else ""}")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Create Local Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Full Name *") })
                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email") })
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Mobile Number") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMinor, onCheckedChange = { isMinor = it })
                        Text("Minor account (enforces chat/monetization safety)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateLocalAccount(newName, newEmail, newPhone, prefs.preferredCountry, prefs.preferredLanguage, isMinor)
                        showAccountDialog = false
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAccountDialog = false }) { Text("Close") } }
        )
    }

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            title = { Text("Country & Language") },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { Text("Country & Currency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                    items(BharatTubeCatalog.countries.size) { i ->
                        val c = BharatTubeCatalog.countries[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCountry(c.code) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (prefs.preferredCountry == c.code) "✓ " else ""}${c.name} (${c.currencyCode} ${c.currencySymbol.trim()})",
                                color = if (prefs.preferredCountry == c.code) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Language (20 Initial)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(BharatTubeCatalog.initialLanguages.size) { i ->
                        val l = BharatTubeCatalog.initialLanguages[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLanguage(l.code) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (prefs.preferredLanguage == l.code) "✓ " else ""}${l.nativeName} — ${l.englishName}",
                                color = if (prefs.preferredLanguage == l.code) SaffronFlame else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRegionDialog = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun ProfileNavTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag(tag)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaffronFlame.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = SaffronFlame)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
