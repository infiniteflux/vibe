package com.infiniteflux.login_using_firebase.screens.event

import androidx.compose.animation.AnimatedContent
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
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.User
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import com.infiniteflux.login_using_firebase.viewmodel.MatchResult
import kotlinx.coroutines.launch

data class AttendeeRatingState(
    val user: User,
    var hasBeenRated: Boolean = false,
    var matchResult: MatchResult = MatchResult.Pending
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAttendeesScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventsViewModel = viewModel()
) {
    var attendeesState by remember { mutableStateOf<List<AttendeeRatingState>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // --- THE FIX: Fetch attendees AND the list of users you've already rated ---
    LaunchedEffect(key1 = eventId) {
        viewModel.getMyRatedUsersForEvent(eventId) { ratedUserIds ->
            viewModel.getAttendees(eventId) { allAttendees ->
                // Filter out the users who have already been rated
                val unratedAttendees = allAttendees.filter { it.id !in ratedUserIds }
                attendeesState = unratedAttendees.map { AttendeeRatingState(user = it) }
            }
        }
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rate Attendees",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        if (attendeesState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No other attendees to rate.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(attendeesState, key = { it.user.id }) { attendee ->
                    AttendeeCard(
                        attendeeState = attendee,
                        onRate = { rating ->
                            coroutineScope.launch {
                                val result = viewModel.submitRating(eventId, attendee.user.id, rating)
                                val updatedList = attendeesState.map {
                                    if (it.user.id == attendee.user.id) {
                                        it.copy(hasBeenRated = true, matchResult = result)
                                    } else {
                                        it
                                    }
                                }
                                attendeesState = updatedList
                            }
                        },
                        onSeeConnectionsClicked = {
                            navController.navigate(AppRoutes.PROFILE)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendeeState: AttendeeRatingState,
    onRate: (String) -> Unit,
    onSeeConnectionsClicked: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = attendeeState.user.avatarUrl,
                    contentDescription = attendeeState.user.name,
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(attendeeState.user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(attendeeState.user.aboutMe, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(targetState = attendeeState.hasBeenRated, label = "RatingStateAnimation") { hasBeenRated ->
                if (!hasBeenRated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { onRate("Spark") }) { Text("Spark") }
                        Button(onClick = { onRate("Normal") }) { Text("Normal") }
                        Button(onClick = { onRate("Fine") }) { Text("Fine") }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        when (attendeeState.matchResult) {
                            is MatchResult.Match -> {
                                Text("You both matched a vibe!", fontWeight = FontWeight.Bold)
                                Button(onClick = onSeeConnectionsClicked) {
                                    Text("See Connections")
                                }
                            }
                            is MatchResult.NoMatch -> {
                                Text("Sorry, you both don't have the same vibe.")
                            }
                            is MatchResult.Pending -> {
                                Text("Vibe sent! Waiting for them to respond.")
                            }
                        }
                    }
                }
            }
        }
    }
}
