package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class AppNotification(

    @DocumentId
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "",
    val relatedId: String = ""
)