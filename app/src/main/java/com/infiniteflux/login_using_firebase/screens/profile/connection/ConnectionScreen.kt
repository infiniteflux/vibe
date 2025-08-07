package com.infiniteflux.login_using_firebase.screens.profile.connection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.Connection
import com.infiniteflux.login_using_firebase.viewmodel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(navController: NavController, viewModel: ConnectionViewModel) {
    Scaffold(
        topBar = {
            // FIX: Replaced TopAppBar with a custom Row inside the topBar slot
            // to have full control over its appearance and padding.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp), // Minimal padding for alignment
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your Connections", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    ) { innerPadding ->
        // The LazyColumn respects the padding calculated by the Scaffold for our custom top bar.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    "Active Connections",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            items(viewModel.connections) { connection ->
                ConnectionCard(connection = connection, onChatClick = {
                    // TODO: Navigate to the correct private chat
                }, onDeleteClick = {
                    // TODO: Handle delete connection
                })
            }
        }
    }
}
@Composable
fun ConnectionCard(connection: Connection, onChatClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = { /* No action */ },
                    label = { Text(connection.matchType) },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Text(connection.matchDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = connection.avatarRes),
                    contentDescription = connection.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(connection.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(connection.fromEvent, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You both rated each other as a friend connection!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Start Chat")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Start Chat")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Connection", tint = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectionsScreenPreview() {
    Login_Using_FirebaseTheme {
        ConnectionScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}
