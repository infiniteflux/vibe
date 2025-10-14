package com.infiniteflux.login_using_firebase.screens.profile.WallOfShame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.data.ReportedUser
import com.infiniteflux.login_using_firebase.viewmodel.WallOfShameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallOfShameScreen(navController: NavController, viewModel: WallOfShameViewModel) {
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeData()
    }

    val reportedUsers by viewModel.reportedUsers.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Wall of Shame",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { /* TODO: Show info dialog */ }) {
                    Icon(Icons.Default.Info, contentDescription = "Information")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CommunitySafetyCard()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "${reportedUsers.size} users with safety concerns",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(reportedUsers) { user ->
                ReportedUserCard(user = user,navController)
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                SubmitReportCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CommunitySafetyCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = "Community Safety", tint = Color(0xFF00796B))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Community Safety", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                Text(
                    "Users listed here have received multiple verified reports. Exercise caution and report any inappropriate behavior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF004D40)
                )
            }
        }
    }
}

@Composable
fun ReportedUserCard(user: ReportedUser,navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, user.warningLevel.color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = user.name, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(user.university, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Chip(label = user.warningLevel.text, color = user.warningLevel.color)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ReportStat(value = "${user.totalReports}", label = "Total Reports")
                ReportStat(value = "${user.verifiedReports}", label = "Verified")
                ReportStat(value = user.lastIncidentDays, label = "Last Incident")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Reported for:", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                user.reportedFor.forEach { reason ->
                    AssistChip(
                        onClick = { },
                        label = { Text(reason) },
                        border = BorderStroke(1.dp, Color.Gray)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController.navigate(AppRoutes.REPORT_USER)},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Flag, contentDescription = "Report")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Report This User")
            }
        }
    }
}

@Composable
fun ReportStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun Chip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
        contentColor = Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SubmitReportCard() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Experienced inappropriate behavior?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Help keep our community safe by reporting users who violate our guidelines.", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO: Handle submit a report */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
            Icon(Icons.Default.Flag, contentDescription = "Submit Report")
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Submit a Report")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WallOfShameScreenPreview() {
    Login_Using_FirebaseTheme {
        WallOfShameScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}
