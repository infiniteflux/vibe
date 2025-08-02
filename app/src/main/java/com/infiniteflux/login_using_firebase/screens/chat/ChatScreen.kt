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
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmode.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmode.Group



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
    // --- CHANGE 1: Fetch data when the screen first appears ---
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchUserGroups()
    }

    // --- CHANGE 2: Collect the list of groups from the ViewModel's StateFlow ---
    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start =16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Group Chats",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* TODO: Handle create new group */ }) {
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
            // --- CHANGE 3: Use the new 'groups' list from the StateFlow ---
            items(groups) { group ->
                ChatListItem(group = group, onClick = {
                    // This navigation logic remains the same as the new Group class also has a String id
                    navController.navigate("${AppRoutes.CHAT_GROUP_DETAILS}/${group.id}/${group.name}")
                })
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ChatListItem(group: Group, onClick: () -> Unit) { // <-- CHANGE 4: Parameter is now the new 'Group' data class
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- CHANGE 5: Use Coil's AsyncImage to load the avatar from a URL ---
        AsyncImage(
            model = group.groupAvatarUrl,
            contentDescription = group.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            // You can add a placeholder image here
            // placeholder = painterResource(id = R.drawable.placeholder_avatar)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(group.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            // --- CHANGE 6: Last message and unread count are removed for now ---
            // In a real app, you would add a `lastMessage` field to your Group document in Firestore
            // to display here efficiently.
            Text("Tap to open chat", color = Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(group.relatedEvent, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("11 Jul", fontSize = 12.sp, color = Color.Gray) // Date needs to be dynamic
            Spacer(modifier = Modifier.height(8.dp))
            // --- CHANGE 7: Display member count from the new data model ---
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
