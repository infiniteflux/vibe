package com.infiniteflux.login_using_firebase.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val user = viewModel.userProfile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8EAF6)) // A light lavender background
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(4.dp, Color.White),
                    modifier = Modifier.size(120.dp)
                ) {
                    Image(
                        painter = painterResource(id = user.avatarRes),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.name, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                Text(user.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* TODO: Handle edit profile */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Edit Profile")
                }
            }
        }

        item {
            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(count = user.eventsCount, label = "Events", icon = Icons.Default.Event)
                StatCard(count = user.connectionsCount, label = "Connections", icon = Icons.Default.People)
                StatCard(count = user.interestsCount, label = "Interests", icon = Icons.Default.Favorite)
            }
        }

        item {
            // About Me Section
            InfoCard(title = "About Me") {
                Text(user.aboutMe, style = MaterialTheme.typography.bodyLarge)
            }
        }

        item {
            // My Interests Section
            InfoCard(title = "My Interests") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    user.interests.forEach { interest ->
                        AssistChip(onClick = { }, label = { Text(interest) })
                    }
                }
            }
        }

        item {
            // Action List Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ActionItem(icon = Icons.Default.People, text = "My Connections", onClick = {})
                ActionItem(icon = Icons.Default.Warning, text = "Wall of Shame", onClick = {})
                ActionItem(icon = Icons.Default.Flag, text = "Report a User", onClick = {})
                ActionItem(icon = Icons.Default.Palette, text = "Theme", trailingText = "Light", onClick = {})
                ActionItem(icon = Icons.Default.Settings, text = "Settings", onClick = {})
                ActionItem(icon = Icons.Default.Logout, text = "Logout", color = Color.Red, onClick = {
                    // Navigate to login and clear the back stack
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
     Login_Using_FirebaseTheme{
        ProfileScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}
