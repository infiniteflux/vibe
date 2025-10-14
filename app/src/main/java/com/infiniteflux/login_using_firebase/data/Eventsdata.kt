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
    val host: String = "",
    val joinCount: Int = 0,
    val startTimestamp: Timestamp? = null,
    val durationHours: Int = 2,

)

//data class EventRating(
//    val ratings: Map<String, String> = emptyMap()
//)

data class Connection(
    val connectedAt: Timestamp? = null,
    val eventId: String = ""
)

data class JoinedEvent(
    val eventId: String = "",
    @ServerTimestamp
    val joinedAt: Timestamp? = null
)
