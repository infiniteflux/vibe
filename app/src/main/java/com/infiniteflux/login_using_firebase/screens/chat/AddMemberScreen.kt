package com.infiniteflux.login_using_firebase.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    navController: NavController,
    groupId: String,
    viewModel: ChatViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    val allUsers by viewModel.allUsers.collectAsState()
    val group by viewModel.groups.collectAsState().let { groupsState ->
        remember(groupsState) {
            derivedStateOf { groupsState.value.find { it.id == groupId } }
        }
    }

    // Filter out users who are already members
    val potentialMembers = allUsers.filter { user ->
        group?.memberIds?.contains(user.id) == false
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
                    text = "Add to ${group?.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(potentialMembers) { user ->
                MemberListItem(
                    member = user,
                    modifier = Modifier.clickable {
                        viewModel.addMemberToGroup(groupId, user.id)
                        // Go back to the previous screen after adding the member
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}