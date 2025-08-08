package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Event(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val description: String = "",
    val host: String = ""
)

data class JoinedEvent(
    val eventId: String = "",
    @ServerTimestamp
    val joinedAt: Timestamp? = null
)
