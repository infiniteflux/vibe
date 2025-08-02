package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmode.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmode.Message

// IMPORTANT: Make sure you are importing the new data class and ViewModel


/*
 * =====================================================================================
 * HOW TO USE THIS CODE
 * =====================================================================================
 * This screen is now connected to the live Firebase backend.
 *
 * 1.  **Navigation:** When you navigate to this screen from your group list, you must
 * pass the `groupId` (String) and `groupName` (String).
 * Example: `navController.navigate("chat_group_details/${group.id}/${group.name}")`
 *
 * 2.  **ViewModel:** It uses the same shared `ChatViewModel` instance.
 * =====================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    navController: NavController,
    groupId: String, // <-- CHANGE 1: groupId is now a String
    groupName: String, // Pass the group name for the top bar
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }

    // --- CHANGE 2: Start listening for messages for this specific group ---
    LaunchedEffect(key1 = groupId) {
        viewModel.listenForMessages(groupId)
    }

    // --- CHANGE 3: Collect the live list of messages and the current user's ID ---
    val messages by viewModel.messages.collectAsState()
    val currentUserId = viewModel.currentUserId // Assuming you've exposed this from your ViewModel
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(groupName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Show group info */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Group Info")
                }
            }
        },
        bottomBar = {
            MessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    // --- CHANGE 4: Wire up the send button ---
                    // Assuming "You" as the sender name for now. In a real app, you'd fetch the user's name.
                    viewModel.sendMessage(groupId, messageText, "You")
                    messageText = "" // Clear the input field
                    keyboardController?.hide()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true // Shows the newest messages at the bottom
        ) {
            // --- CHANGE 5: Use the new 'messages' list ---
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId
                )
            }
        }
    }
}

// --- CHANGE 6: A new, better Composable for displaying chat messages ---
@Composable
fun MessageBubble(message: Message, isFromCurrentUser: Boolean) {
    val alignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromCurrentUser) 64.dp else 0.dp,
                end = if (isFromCurrentUser) 0.dp else 64.dp
            ),
        contentAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = backgroundColor,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Only show the sender's name if it's not the current user
                if (!isFromCurrentUser) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Composable
fun MessageInput(value: String, onValueChange: (String) -> Unit, onSendClick: () -> Unit) {
    Surface(tonalElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Ensures input field is above navigation bar
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = false,
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank(), // Disable button if input is empty
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupChatScreenPreview() {
    Login_Using_FirebaseTheme {
        GroupChatScreen(navController = rememberNavController(), groupId = "1", groupName = "Coffee Crew", viewModel = viewModel())
    }
}
