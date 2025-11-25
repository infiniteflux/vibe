package com.infiniteflux.login_using_firebase.data

import com.infiniteflux.login_using_firebase.viewmodel.WarningLevel

data class ReportedUser(
    val id: String,
    val name: String,
    val university: String,
    val totalReports: Int,
    val verifiedReports: Int,
    val lastIncidentDays: String,
    val reportedFor: List<String>,
    val avatarUrl: String,
    val warningLevel: WarningLevel
)

