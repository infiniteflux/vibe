package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.Group
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchUserGroups()
        viewModel.fetchGroupReadStatuses()
    }

    val groups by viewModel.groups.collectAsState()
    val readStatuses by viewModel.groupReadStatus.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Group Chats",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { showCreateGroupDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (groups.isEmpty()) {
                Text(
                    text = "No group chats found.\nTap the '+' to create one!",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(groups) { group ->
                        val lastReadTimestamp = readStatuses[group.id]?.lastReadTimestamp
                        val isUnread = (group.lastMessageTimestamp != null && lastReadTimestamp != null && group.lastMessageTimestamp > lastReadTimestamp)
                                || (group.lastMessageTimestamp != null && lastReadTimestamp == null)

                        ChatListItem(
                            group = group,
                            isUnread = isUnread,
                            onClick = {
                                viewModel.markGroupAsRead(group.id)
                                navController.navigate("${AppRoutes.CHAT_GROUP_DETAILS}/${group.id}/${group.name}")
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { groupName, relatedEvent ->
                viewModel.createGroup(groupName, relatedEvent)
                showCreateGroupDialog = false
            }
        )
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var relatedEvent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relatedEvent,
                    onValueChange = { relatedEvent = it },
                    label = { Text("Related Event (Optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(groupName, relatedEvent) },
                enabled = groupName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun ChatListItem(group: Group, isUnread: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = group.groupAvatarUrl,
            contentDescription = group.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(group.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val lastMessagePrefix = if (group.lastMessageSenderName.isNotBlank()) "${group.lastMessageSenderName}: " else ""
            Text(
                text = "$lastMessagePrefix${group.lastMessageText}",
                color = if (isUnread) MaterialTheme.colorScheme.onSurface else Color.Gray,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(group.relatedEvent, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(group.lastMessageTimestamp),
                fontSize = 12.sp,
                color = if (isUnread) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isUnread) {
                Badge(
                    modifier = Modifier.size(10.dp)
                )
            } else {
                Text("${group.memberIds.size} members", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open Chat", tint = Color.Gray, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val messageDate = timestamp.toDate()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val yesterday = calendar.time
    return when {
        messageDate.after(today) -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(messageDate)
        messageDate.after(yesterday) -> "Yesterday"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(messageDate)
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    Login_Using_FirebaseTheme {
        ChatScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}
