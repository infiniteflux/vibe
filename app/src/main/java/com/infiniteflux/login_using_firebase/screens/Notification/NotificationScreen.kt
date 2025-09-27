package com.infiniteflux.login_using_firebase.screens.Notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.infiniteflux.login_using_firebase.data.AppNotification
import com.infiniteflux.login_using_firebase.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeData()
    }

    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = {navController.navigateUp()}) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notifications", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No new notifications.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    val icon = when (notification.type) {
        "private_message", "group_message" -> Icons.Default.ChatBubble
        "new_connection" -> Icons.Default.Favorite
        "new_event" -> Icons.Default.Event
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = notification.type,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontWeight = FontWeight.Bold)
                Text(notification.body, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatNotificationTimestamp(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun formatNotificationTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
