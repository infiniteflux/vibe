package com.infiniteflux.login_using_firebase.screens.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: EventsViewModel
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf("") }

    // --- 1. State management for Date and Time Pickers ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val datePickerState = rememberDatePickerState(
        // Prevent selecting dates before today
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000 // Allow today
            }
        }
    )
    val timePickerState = rememberTimePickerState()

    // Formatted date and time string for the UI
    val formattedDateTime = remember(selectedDate, selectedTime) {
        if (selectedDate != null && selectedTime != null) {
            val calendar = Calendar.getInstance().apply {
                time = selectedDate!!
                set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                set(Calendar.MINUTE, selectedTime!!.second)
            }
            SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(calendar.time)
        } else {
            ""
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val isFormValid by remember(title, location, formattedDateTime, category, description, host, durationHours) {
        derivedStateOf {
            title.isNotBlank() && location.isNotBlank() && formattedDateTime.isNotBlank() &&
                    category.isNotBlank() && description.isNotBlank() && host.isNotBlank() &&
                    durationHours.isNotBlank()
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Event") },
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
            Text("Event Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // (Image Picker UI remains the same)
            Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }, /*...*/) { /*...*/ }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

            // --- 2. Read-only TextField to launch the Date Picker ---
            OutlinedTextField(
                value = formattedDateTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date and Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) {
                            showDatePicker = true
                            focusManager.clearFocus(force = true)
                        }
                    }
            )

            OutlinedTextField(value = durationHours, onValueChange = { durationHours = it }, label = { Text("Duration (in hours)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            CategoryDropdown(selectedCategory = category, onCategorySelected = { category = it })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Hosted By") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.createEvent(
                        title = title, location = location, date = formattedDateTime, category = category,
                        description = description, host = host, durationHours = durationHours.toIntOrNull() ?: 2,
                        imageUri = imageUri
                    ) {
                        navController.navigateUp()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publish Event")
            }
        }
    }

    // --- 3. Date Picker Dialog ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    selectedDate = datePickerState.selectedDateMillis?.let { Date(it) }
                    // Show the time picker right after a date is confirmed
                    if (selectedDate != null) {
                        showTimePicker = true
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- 4. Time Picker Dialog ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                selectedTime = Pair(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        )
    }
}

// A custom composable for the TimePickerDialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: @Composable () -> Unit = { Text("Select Time") }
) {
    val timePickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                // Here you would typically update your state with timePickerState.hour and timePickerState.minute
                onConfirm()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Social", "Food", "Study")
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {}, // Input is read-only
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor() // This connects the TextField to the dropdown menu
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
