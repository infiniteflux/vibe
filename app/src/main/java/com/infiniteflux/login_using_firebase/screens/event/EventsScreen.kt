package com.infiniteflux.login_using_firebase.screens.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.sharedComponents.BottomNavigationBar
import com.infiniteflux.login_using_firebase.viewmodel.Event
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel

@Composable
fun EventsScreen(navController: NavController, viewModel: EventsViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val events by viewModel.events.collectAsState()

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
                EventCard(
                    event = event,
                    onCardClicked = {
                        navController.navigate("${AppRoutes.EVENT_DETAILS}/${event.id}")
                    }
                )
            }
        }
        BottomNavigationBar(navController=NavController(context = LocalContext.current))
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

@Composable
fun FilterChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Social", "Food", "Study")
    val icons = listOf(Icons.Default.Apps, Icons.Default.People, Icons.Default.Fastfood, Icons.Default.Book)

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
fun EventCard(event: Event, onCardClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClicked)
    ) {
        Column {
            Box(modifier = Modifier.height(150.dp)) {
                Image(
                    painter = painterResource(id = event.imageRes),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (event.isCurated) {
                    Text(
                        text = "✨ VIBE Curated",
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${event.joinedCount}/${event.totalCount} joined", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { /* The whole card is now clickable */ },
                        enabled = false, // Disable direct clicks on the button
                        shape = RoundedCornerShape(50),
                        colors = if (event.isJoined) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                disabledContainerColor = Color.LightGray.copy(alpha = 0.8f)
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    ) {
                        if (event.isJoined) {
                            Icon(Icons.Default.Check, contentDescription = "Joined", modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        }
                        Text(if (event.isJoined) "Joined" else "Join", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun DiscoverEventsScreenPreview() {
//    VibeWithMeTheme {
//        val navController = rememberNavController()
//        val viewModel = EventsViewModel()
//        DiscoverEventsScreen(navController = navController, viewModel = viewModel)
//    }
//}
