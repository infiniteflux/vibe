package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.Message
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/*
 * =====================================================================================
 * NOTE:
 * This update requires your AuthViewModel to provide the current user's name.
 * You can add the following to your AuthViewModel:
 *
 * private val _currentUserName = MutableLiveData<String>()
 * val currentUserName: LiveData<String> = _currentUserName
 *
 * private fun fetchUserName(uid: String) {
 * db.collection("users").document(uid).get().addOnSuccessListener {
 * _currentUserName.value = it.getString("name") ?: "User"
 * }
 * }
 *
 * // Call fetchUserName() when checkAuthState() confirms an authenticated user.
 * =====================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    navController: NavController,
    groupId: String,
    groupName: String,
    viewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel() // Add AuthViewModel
) {
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = groupId) {
        viewModel.listenForMessages(groupId)
    }

    val messages by viewModel.messages.collectAsState()
    val currentUserId = viewModel.currentUserId
    val senderName by authViewModel.currentUserName.observeAsState("You")
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(groupName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.navigate("${AppRoutes.GROUP_INFO}/$groupId") }) {
                    Icon(Icons.Default.Info, contentDescription = "Group Info")
                }
            }
        },
        bottomBar = {
            MessageInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    viewModel.sendMessage(groupId, messageText, senderName)
                    messageText = ""
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
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId
                )
            }
        }
    }
}

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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
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
                .navigationBarsPadding()
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
                maxLines = 5 // Allow for slightly longer messages
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank(),
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
@Composable
private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val messageDate = timestamp.toDate()
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(messageDate)
}

@Preview(showBackground = true)
@Composable
fun GroupChatScreenPreview() {
    Login_Using_FirebaseTheme {
        GroupChatScreen(navController = rememberNavController(), groupId = "1", groupName = "Coffee Crew")
    }
}
