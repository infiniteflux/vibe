package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = ""
)

data class Group(
    val id: String = "",
    val name: String = "",
    val relatedEvent: String = "",
    val memberIds: List<String> = listOf(),
    val groupAvatarUrl: String = ""
)

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
)
