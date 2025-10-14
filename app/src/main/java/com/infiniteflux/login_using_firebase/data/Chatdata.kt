package com.infiniteflux.login_using_firebase.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val aboutMe: String = "",
    val interests: List<String> = emptyList(),
    val role: String = "user",
    val isBanned: Boolean = false,
    val university: String = ""
)

data class Group(
    val id: String = "",
    val name: String = "",
    val relatedEvent: String = "",
    val memberIds: List<String> = listOf(),
    val groupAvatarUrl: String = "",
    val lastMessageText: String = "No messages yet.",
    @ServerTimestamp
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSenderName: String = ""
)

data class GroupReadStatus(
    @ServerTimestamp
    val lastReadTimestamp: Timestamp? = null
)

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
)

data class ChatRoom(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessageText: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSenderName: String = ""
)

