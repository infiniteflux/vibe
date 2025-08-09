package com.infiniteflux.login_using_firebase.screens.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.data.User
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAttendeesScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventsViewModel = viewModel()
) {
    var attendees by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(key1 = eventId) {
        viewModel.getAttendees(eventId) { users ->
            attendees = users
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Attendees") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (attendees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No other attendees to rate.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(attendees) { user ->
                    AttendeeCard(
                        user = user,
                        onRate = { rating ->
                            viewModel.submitRating(eventId, user.id, rating)
                            // Remove the user from the list after rating to prevent re-rating
                            attendees = attendees - user
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(user: User, onRate: (String) -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = user.name,
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(user.aboutMe, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onRate("Spark") }) { Text("Spark") }
                Button(onClick = { onRate("Normal") }) { Text("Normal") }
                Button(onClick = { onRate("Fine") }) { Text("Fine") }
            }
        }
    }
}

