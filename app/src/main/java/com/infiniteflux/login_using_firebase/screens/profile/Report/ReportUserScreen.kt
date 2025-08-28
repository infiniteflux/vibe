package com.infiniteflux.login_using_firebase.screens.profile.Report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.infiniteflux.login_using_firebase.data.ConnectionForReport
import com.infiniteflux.login_using_firebase.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportUserScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.initializeData()
    }

    val connections by viewModel.connections.collectAsState()
    var selectedUser by remember { mutableStateOf<ConnectionForReport?>(null) }
    var reason by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isFormValid by remember(selectedUser, reason) {
        derivedStateOf { selectedUser != null && reason.isNotBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report a User") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select User to Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            UserDropdown(
                connections = connections,
                selectedUser = selectedUser,
                onUserSelected = { selectedUser = it }
            )

            Text("Reason for Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Please provide details...") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.submitReport(selectedUser!!.userId, reason) {
                        showSuccessDialog = true
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Report")
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text("Report Submitted") },
            text = { Text("Thank you for helping keep our community safe. Your report has been submitted for review.") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDropdown(
    connections: List<ConnectionForReport>,
    selectedUser: ConnectionForReport?,
    onUserSelected: (ConnectionForReport) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedUser?.userName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select a user") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            connections.forEach { connection ->
                DropdownMenuItem(
                    text = { Text(connection.userName) },
                    onClick = {
                        onUserSelected(connection)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
