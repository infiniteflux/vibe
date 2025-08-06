package com.infiniteflux.login_using_firebase.data

data class UserProfile(
    val name: String = "Loading...",
    val email: String = "",
    val avatarUrl: String = "",
    val eventsCount: Int = 0,
    val connectionsCount: Int = 0, // Placeholder for a future feature
    val interestsCount: Int = 0,
    val aboutMe: String = "",
    val interests: List<String> = emptyList()
)
