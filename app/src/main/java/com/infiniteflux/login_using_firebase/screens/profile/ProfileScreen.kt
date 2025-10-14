package com.infiniteflux.login_using_firebase.screens.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ConnectionViewModel
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import com.infiniteflux.login_using_firebase.viewmodel.HomeViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    eventsViewModel: EventsViewModel,
    homeViewModel: HomeViewModel,
    connectionViewModel: ConnectionViewModel
) {
    val user by viewModel.userProfile.collectAsState()
    val connectionCount by connectionViewModel.connectionCount.collectAsState()
    val context = LocalContext.current


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0C0C))
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(4.dp, Color.White),
                    modifier = Modifier.size(120.dp)
                ) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.name, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                Text(user.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate(AppRoutes.EDITPROFILE) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Edit Profile")
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(count = user.eventsCount, label = "Events", icon = Icons.Default.Event)
                StatCard(count = connectionCount, label = "Connections", icon = Icons.Default.People)
                StatCard(count = user.interestsCount, label = "Interests", icon = Icons.Default.Favorite)
            }
        }

        item {
            InfoCard(title = "About Me") {
                Box(
                    modifier = Modifier
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(user.aboutMe, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            // My Interests Section
            InfoCard(title = "My Interests") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(user.interests) { interest ->
                        AssistChip(onClick = { }, label = { Text(interest) })
                    }
                }
            }
        }

        item {
            // Action List Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ActionItem(icon = Icons.Default.People, text = "My Connections", onClick = {navController.navigate(
                    AppRoutes.CONNECTION)})
                ActionItem(icon = Icons.Default.Warning, text = "Wall of Shame", onClick = {navController.navigate(
                    AppRoutes.WALLOFSHAME)})
                ActionItem(icon = Icons.Default.Flag, text = "Report a User", onClick = {navController.navigate(
                    AppRoutes.REPORT_USER)})
                ActionItem(icon = Icons.Default.Palette, text = "Theme", trailingText = "Light", onClick = { Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()})
                ActionItem(icon = Icons.Default.Settings, text = "Settings", onClick = {Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()})
                ActionItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    text = "Logout",
                    color = Color.Red,
                    onClick = {
                        chatViewModel.clearDataAndListeners()
                        eventsViewModel.clearDataAndListeners()
                        homeViewModel.clearDataAndListeners()
                        viewModel.clearDataAndListeners()
                        connectionViewModel.clearDataAndListeners()
                        authViewModel.signout()
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(count: Int, label: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$count", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String, trailingText: String? = null, color: Color = Color.Unspecified, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = if(color != Color.Unspecified) color else LocalContentColor.current)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, modifier = Modifier.weight(1f), color = color, fontSize = 16.sp)
        if (trailingText != null) {
            Text(trailingText, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
    Divider()
}
