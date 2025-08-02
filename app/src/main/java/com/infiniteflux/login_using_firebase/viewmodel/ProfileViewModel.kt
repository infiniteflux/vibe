package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.ViewModel
import com.infiniteflux.login_using_firebase.R

// Data class for user profile information
data class UserProfile(
    val name: String,
    val email: String,
    val avatarRes: Int, // Using a drawable resource for the avatar
    val eventsCount: Int,
    val connectionsCount: Int,
    val interestsCount: Int,
    val aboutMe: String,
    val interests: List<String>
)

// Dummy data for the user profile
private val dummyUserProfile = UserProfile(
    name = "Dev User",
    email = "dev@example.com",
    avatarRes = R.drawable.lazy, // You'll need to add a placeholder avatar image
    eventsCount = 5,
    connectionsCount = 12,
    interestsCount = 3,
    aboutMe = "Development user for testing the app",
    interests = listOf("Technology", "Music", "Sports")
)

class ProfileViewModel : ViewModel() {
    val userProfile = dummyUserProfile
    // In a real app, this data would be fetched from a repository or database
}
