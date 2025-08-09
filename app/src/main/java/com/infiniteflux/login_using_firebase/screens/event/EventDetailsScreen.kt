package com.infiniteflux.login_using_firebase.screens.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.viewmodel.AuthState
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventsViewModel,
    authState: AuthState?,
    onLoginRequired: () -> Unit
) {
    val event = viewModel.findEvent(eventId)
    val joinedEventIds by viewModel.joinedEventIds.collectAsState()
    val isJoined = joinedEventIds.contains(event?.id)

    var showCongratsDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event not found!")
        }
        return
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Event Details",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        floatingActionButton = {
            // --- 1. Check if the event has ended ---
            val eventHasEnded = event.startTimestamp != null &&
                    (event.startTimestamp.seconds * 1000 + TimeUnit.HOURS.toMillis(event.durationHours.toLong())) < System.currentTimeMillis()

            // --- 2. Conditionally display the correct button ---
            if (isJoined && eventHasEnded) {
                // If user has joined AND the event is over, show "Rate Attendees"
                Button(
                    onClick = { navController.navigate("${AppRoutes.RATE_ATTENDEES}/${event.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Rate Attendees")
                }
            } else {
                // Otherwise, show the original "Join Event" button
                Button(
                    onClick = {
                        if (authState is AuthState.Guest) {
                            onLoginRequired()
                        } else {
                            if (!isJoined) {
                                viewModel.toggleJoinedStatus(event.id)
                                showCongratsDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !(authState !is AuthState.Guest && isJoined),
                    colors = if (isJoined) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.8f)
                        )
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    }
                ) {
                    if (isJoined) {
                        Icon(Icons.Default.Check, contentDescription = "Joined")
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("You've Joined This Event")
                    } else {
                        Text("Join Event")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoRow(icon = Icons.Default.CalendarToday, text = event.date)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(icon = Icons.Default.LocationOn, text = event.location)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "About this event",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Hosted by",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.host, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showCongratsDialog) {
        AlertDialog(
            onDismissRequest = { showCongratsDialog = false },
            title = { Text("Congratulations!") },
            text = { Text("You have successfully joined the event: ${event.title}") },
            confirmButton = {
                Button(onClick = { showCongratsDialog = false }) {
                    Text("Awesome!")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
