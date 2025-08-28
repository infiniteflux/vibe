package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Report(
    val reportedUserId: String = "",
    val reporterId: String = "",
    val reason: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val verified: Boolean = false // Field for admins to verify a report
)

data class ConnectionForReport(
    val userId: String,
    val userName: String
)
