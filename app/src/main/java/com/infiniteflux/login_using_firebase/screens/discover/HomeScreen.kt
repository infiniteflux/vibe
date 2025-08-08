package com.infiniteflux.login_using_firebase.screens.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    authViewModel: AuthViewModel // 1. Add AuthViewModel
) {
    val userName by viewModel.userName.collectAsState()
    val eventsCount by viewModel.eventsCount.collectAsState()
    val trendingEvents by viewModel.trendingEvents.collectAsState()

    // --- 2. Observe the user's role and add state for the dialog ---
    val userRole by authViewModel.userRole.observeAsState("user")
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopBar(userName = userName)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StatsSection(eventsCount = eventsCount)
            Spacer(modifier = Modifier.height(24.dp))
            TrendingEventsSection(navController = navController, trendingEvents = trendingEvents)
            Spacer(modifier = Modifier.height(24.dp))
            // --- 3. Pass the role and dialog trigger to the QuickActionsSection ---
            QuickActionsSection(
                navController = navController,
                userRole = userRole,
                onPermissionDenied = { showPermissionDeniedDialog = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
            StaySafeSection()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- 4. Add the permission denied dialog ---
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Feature for Creators Only") },
            text = { Text("Creating new events is currently limited to users with the 'creator' role.") },
            confirmButton = {
                Button(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun TopBar(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Hey $userName! 👋",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Ready to vibe with new people?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color(0xFFE91E63),
                    contentColor = Color.White
                ) { Text("2") }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun StatsSection(eventsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatCard(value = eventsCount.toString(), "Events", Icons.Default.Event)
        StatCard("12", "Connections", Icons.Default.People)
        StatCard("0", "This Week", Icons.AutoMirrored.Filled.TrendingUp)
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun TrendingEventsSection(navController: NavController, trendingEvents: List<Event>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🔥 Trending Events",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = { navController.navigate(AppRoutes.EVENTS) }) {
                Text("See All", color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (trendingEvents.isNotEmpty()) {
            TrendingEventCard(event = trendingEvents.first())
        } else {
            Text("Loading trending events...", color = Color.Gray)
        }
    }
}

@Composable
fun TrendingEventCard(event: Event) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    event.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// --- 5. Update the QuickActionsSection function signature ---
@Composable
fun QuickActionsSection(
    navController: NavController,
    userRole: String,
    onPermissionDenied: () -> Unit
) {
    Column {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // --- 6. Update the onClick logic for the "Create Event" card ---
            QuickActionCard(
                onClickAction = {
                    if (userRole == "creator") {
                        navController.navigate(AppRoutes.CREATE_EVENT)
                    } else {
                        onPermissionDenied()
                    }
                },
                "Create Event",
                Icons.Default.AddCircle,
                Color(0xFF4CAF50)
            )
            QuickActionCard(onClickAction = { navController.navigate(AppRoutes.CHATS) }, "Group Chats", Icons.Outlined.Group, Color(0xFF2196F3))
            QuickActionCard(onClickAction = { navController.navigate(AppRoutes.CONNECTION) }, "Connections", Icons.Default.Favorite, Color(0xFFE91E63))
        }
    }
}

@Composable
fun QuickActionCard(onClickAction: () -> Unit, label: String, icon: ImageVector, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .height(100.dp)
            .width(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().clickable(onClick = onClickAction),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StaySafeSection() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Safety Shield",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .height(105.dp)
            ) {
                Text(
                    text = "Stay Safe Out There!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Always meet in public places and trust your instincts. Report any concerns immediately. If you ever feel unsafe, don't hesitate to contact local authorities. Your safety is the top priority. Be aware of your surroundings and let a friend know where you are going.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}
