package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.infiniteflux.login_using_firebase.viewmodel.WarningLevel

data class ReportedUser(
    val id: String, // Changed to String to match Firestore UID
    val name: String,
    val university: String,
    val totalReports: Int,
    val verifiedReports: Int,
    val lastIncidentDays: String,
    val reportedFor: List<String>,
    val avatarUrl: String, // Changed to String for URL
    val warningLevel: WarningLevel
)

data class Report(
    val reportedUserId: String = "",
    val reporterId: String = "",
    val reason: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val verified: Boolean = false // Field for admins to verify a report
)
