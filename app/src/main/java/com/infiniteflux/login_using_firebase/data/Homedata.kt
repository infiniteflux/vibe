package com.infiniteflux.login_using_firebase.data

data class UserProfile(
    val name: String = "Loading...",
    val email: String = "",
    val avatarUrl: String = "",
    val eventsCount: Int = 0,
    val interestsCount: Int = 0,
    val aboutMe: String = "No bio yet.",
    val interests: List<String> = emptyList()
)
