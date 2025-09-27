package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class AppNotification(

    @DocumentId
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "", // e.g., 'private_message', 'group_message', 'new_connection'
    val relatedId: String = "" // e.g., chatId, eventId, connectionId
)