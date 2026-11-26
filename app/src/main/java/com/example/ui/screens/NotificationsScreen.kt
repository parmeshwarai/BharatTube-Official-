package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NotificationEntity
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Read")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                IntegrationDisclosureBanner(
                    title = "In-App Notification Center Active",
                    message = "Remote push notifications require Firebase Cloud Messaging (FCM) configuration. All subscriber, earnings, payout, upload, and copyright alerts appear here in real time."
                )
            }
            if (notifications.isEmpty()) {
                item {
                    Text("No notifications yet.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (notif.isRead) MaterialTheme.colorScheme.surface else SaffronFlame.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = if (notif.isRead) MaterialTheme.colorScheme.onSurfaceVariant else SaffronFlame
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.category, style = MaterialTheme.typography.labelSmall, color = SaffronFlame)
                                Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(notif.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
