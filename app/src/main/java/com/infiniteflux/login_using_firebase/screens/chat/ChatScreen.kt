package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // <-- IMPORTANT: Import for Coil
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.Group
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel

/*
 * =====================================================================================
 * HOW TO USE THIS CODE
 * =====================================================================================
 * This code is adapted to work with the live Firebase data model.
 *
 * 1.  **Add Coil Dependency:** To load images from a URL, you need the Coil library.
 * Add this to your app-level `build.gradle.kts` file:
 * `implementation("io.coil-kt:coil-compose:2.6.0")`
 *
 * 2.  **Update Imports:** Make sure you are importing the new `Group` data class and
 * the new `ChatViewModel` as defined in the `group_chat_workflow_guide`.
 * =====================================================================================
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchUserGroups()
    }

    val groups by viewModel.groups.collectAsState()
    // 1. State to control the visibility of the dialog
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
                // 2. Update IconButton to show the dialog
                IconButton(onClick = { showCreateGroupDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(groups) { group ->
                ChatListItem(group = group, onClick = {
                    navController.navigate("${AppRoutes.CHAT_GROUP_DETAILS}/${group.id}/${group.name}")
                })
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }

    // 3. Conditionally display the dialog
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

// 4. A new Composable for the dialog
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
                enabled = groupName.isNotBlank() // Button is only enabled if name is not empty
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
private fun ChatListItem(group: Group, onClick: () -> Unit) {
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
            Text("Tap to open chat", color = Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(group.relatedEvent, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("11 Jul", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${group.memberIds.size} members", fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open Chat", tint = Color.Gray, modifier = Modifier.padding(start = 8.dp))
    }
}


@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    Login_Using_FirebaseTheme {
        // Preview will now show an empty list, which is expected as it's not connected to Firebase.
        ChatScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}
