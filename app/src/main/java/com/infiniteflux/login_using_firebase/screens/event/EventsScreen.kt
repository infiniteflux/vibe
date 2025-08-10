package com.infiniteflux.login_using_firebase.screens.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.viewmodel.AuthState
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel

@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel,
    authState: AuthState?,
    onLoginRequired: () -> Unit,
    authViewModel: AuthViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val events by viewModel.events.collectAsState()
    val joinedEventIds by viewModel.joinedEventIds.collectAsState()
    var eventToJoin by remember { mutableStateOf<Event?>(null) }
    var eventJustJoined by remember { mutableStateOf<Event?>(null) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }

    val userRole by authViewModel.userRole.observeAsState("user")

    val filteredEvents = events.filter {
        (it.title.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategory == "All" || it.category == selectedCategory)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DiscoverTopSection()
        SearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it }
        )
        FilterChips(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredEvents, key = { it.id }) { event ->
                val isJoined = joinedEventIds.contains(event.id)

                EventCard(
                    event = event,
                    isJoined = isJoined,
                    userRole = userRole,
                    onCardClicked = {
                        navController.navigate("${AppRoutes.EVENT_DETAILS}/${event.id}")
                    },
                    onJoinClicked = {
                        if (authState is AuthState.Guest) {
                            onLoginRequired()
                        } else {
                            eventToJoin = event
                        }
                    },
                    onDeleteClicked = {
                        eventToDelete = event
                    }
                )
            }
        }
    }

    if (eventToJoin != null) {
        AlertDialog(
            onDismissRequest = { eventToJoin = null },
            title = { Text("Join Event?") },
            text = { Text("Are you sure you want to join '${eventToJoin!!.title}'?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.toggleJoinedStatus(eventToJoin!!.id)
                    eventJustJoined = eventToJoin
                    eventToJoin = null
                }) {
                    Text("Yes, Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToJoin = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (eventJustJoined != null) {
        AlertDialog(
            onDismissRequest = { eventJustJoined = null },
            title = { Text("Congratulations!") },
            text = { Text("You have successfully joined the event: ${eventJustJoined!!.title}") },
            confirmButton = {
                Button(onClick = { eventJustJoined = null }) {
                    Text("Awesome!")
                }
            }
        )
    }

    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to permanently delete '${eventToDelete!!.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(eventToDelete!!.id) {
                            eventToDelete = null // Close the dialog on success
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
fun DiscoverTopSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Discover Events",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Find your next adventure",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search events...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Social", "Food", "Study")
    val icons = listOf(Icons.Default.Apps, Icons.Default.People, Icons.Default.Fastfood, Icons.Default.Book)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = "$category category",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}


@Composable
fun EventCard(
    event: Event,
    isJoined: Boolean,
    userRole: String,
    onCardClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClicked)
    ) {
        Column {
            Box(modifier = Modifier.height(150.dp)) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                // --- THE FIX: Added the missing Text and Icon rows back ---
                Text(event.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // --- END FIX ---

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (userRole == "creator") {
                        IconButton(onClick = onDeleteClicked) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = onJoinClicked,
                        shape = RoundedCornerShape(50),
                        enabled = !isJoined,
                        colors = if (isJoined) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.8f)
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        if (isJoined) {
                            Icon(Icons.Default.Check, contentDescription = "Joined", modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        }
                        Text(if (isJoined) "Joined" else "Join", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
