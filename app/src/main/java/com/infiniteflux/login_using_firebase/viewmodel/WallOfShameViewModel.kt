package com.infiniteflux.login_using_firebase.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.infiniteflux.login_using_firebase.R

// Data class for a reported user
data class ReportedUser(
    val id: Int,
    val name: String,
    val university: String,
    val totalReports: Int,
    val verifiedReports: Int,
    val lastIncidentDays: String,
    val reportedFor: List<String>,
    val avatarRes: Int?, // Nullable for users without an avatar
    val warningLevel: WarningLevel
)

enum class WarningLevel(val text: String, val color: Color) {
    DANGER("Danger", Color(0xFFE91E63)),
    WARNING("Warning", Color(0xFFFFA000))
}

// Dummy data for reported users
private val dummyReportedUsers = listOf(
    ReportedUser(
        id = 1,
        name = "John D.",
        university = "University of California",
        totalReports = 5,
        verifiedReports = 3,
        lastIncidentDays = "22d",
        reportedFor = listOf("Harassment", "Inappropriate Behavior"),
        avatarRes = null, // No avatar for this user
        warningLevel = WarningLevel.DANGER
    ),
    ReportedUser(
        id = 2,
        name = "Mike S.",
        university = "Stanford University",
        totalReports = 3,
        verifiedReports = 2,
        lastIncidentDays = "23d",
        reportedFor = listOf("Fake Profile", "Safety Concern"),
        avatarRes = R.drawable.lazy, // Make sure you have this drawable
        warningLevel = WarningLevel.WARNING
    )
)

class WallOfShameViewModel : ViewModel() {
    val reportedUsers = dummyReportedUsers
    // In a real app, this data would be fetched from a secure backend
}
