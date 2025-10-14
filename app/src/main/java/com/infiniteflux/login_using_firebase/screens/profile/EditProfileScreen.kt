package com.infiniteflux.login_using_firebase.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf(userProfile.name) }
    var aboutMe by remember { mutableStateOf(userProfile.aboutMe) }
    var interests by remember { mutableStateOf(userProfile.interests) }
    var newInterestText by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfileImage(it)
        }
    }

    LaunchedEffect(userProfile) {
        if (name != userProfile.name) name = userProfile.name
        if (aboutMe != userProfile.aboutMe) aboutMe = userProfile.aboutMe
        if (interests != userProfile.interests) interests = userProfile.interests
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Change photo")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = aboutMe,
                onValueChange = { aboutMe = it },
                label = { Text("About Me") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            // (Interests section remains the same)
            Text("Your Interests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(interest) },
                            trailingIcon = {
                                IconButton(
                                    modifier = Modifier.size(18.dp),
                                    onClick = { interests = interests - interest }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove interest")
                                }
                            }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newInterestText,
                    onValueChange = { newInterestText = it },
                    label = { Text("Add an interest") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newInterestText.isNotBlank()) {
                        interests = interests + newInterestText
                        newInterestText = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add interest")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.updateUserProfile(name, aboutMe, interests)
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

