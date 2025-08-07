package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.ViewModel
import com.infiniteflux.login_using_firebase.R

// Data class for a user connection
data class Connection(
    val id: Int,
    val name: String,
    val fromEvent: String,
    val matchType: String,
    val matchDate: String,
    val avatarRes: Int
)

// Dummy data for connections
private val dummyConnections = listOf(
    Connection(
        id = 1,
        name = "Sarah Chen",
        fromEvent = "From Coffee & Connections",
        matchType = "Friend Match",
        matchDate = "11/7/2025",
        avatarRes = R.drawable.lazy // Make sure you have this drawable
    )
    // You can add more connections here
)

class ConnectionViewModel : ViewModel() {
    val connections = dummyConnections
    // In a real app, you would fetch this from a database or API
}
