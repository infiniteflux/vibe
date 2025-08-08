package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
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
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    navController: NavController,
    groupId: String,
    viewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    val allUsers by viewModel.allUsers.collectAsState()
    // Find the current group from the list of groups
    val group by viewModel.groups.collectAsState().let { groupsState ->
        remember(groupsState) {
            derivedStateOf { groupsState.value.find { it.id == groupId } }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), // Your desired padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = group?.name ?: "Group Info",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(onClick = {
                // Navigate to the Add Member screen
                navController.navigate("${AppRoutes.ADD_MEMBER_TO_GROUP}/$groupId")
            }) {
                Text("Add Member")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Members", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            val members = group?.memberIds?.mapNotNull { memberId ->
                allUsers.find { it.id == memberId }
            } ?: emptyList()

            LazyColumn {
                items(members) { member ->
                    MemberListItem(member = member)
                }
            }
        }
    }
}

@Composable
fun MemberListItem(member: User, modifier: Modifier = Modifier) { // Added modifier parameter
    Row(
        modifier = modifier // Applied modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.avatarUrl,
            contentDescription = member.name,
            modifier = Modifier.size(40.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(member.name, fontWeight = FontWeight.SemiBold)
    }
}
