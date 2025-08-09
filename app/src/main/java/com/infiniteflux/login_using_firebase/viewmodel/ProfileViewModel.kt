package com.infiniteflux.login_using_firebase.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "Loading...",
    val email: String = "",
    val avatarUrl: String = "",
    val eventsCount: Int = 0,
    val connectionsCount: Int = 12,
    val interestsCount: Int = 0,
    val aboutMe: String = "No bio yet.",
    val interests: List<String> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId get() = auth.currentUser?.uid

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    // State to hold all event IDs and joined event IDs for accurate counting
    private val _allEventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())

    // Listeners
    private var profileListener: ListenerRegistration? = null
    private var allEventsListener: ListenerRegistration? = null
    private var joinedEventsListenerForCount: ListenerRegistration? = null

    fun initializeData() {
        fetchUserProfile()
        fetchAllEventIds()
        fetchJoinedEventsForCount()
    }

    fun uploadProfileImage(imageUri: Uri) {
        if (currentUserId == null) return
        val storageRef = storage.reference.child("profile_images/$currentUserId.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("users").document(currentUserId!!)
                        .update("avatarUrl", downloadUrl.toString())
                }
            }
    }

    fun updateUserProfile(newName: String, newAboutMe: String, newInterests: List<String>) {
        if (currentUserId == null) return
        val userDocRef = db.collection("users").document(currentUserId!!)
        val updates = mapOf(
            "name" to newName,
            "aboutMe" to newAboutMe,
            "interests" to newInterests
        )
        userDocRef.update(updates)
    }

    private fun fetchUserProfile() {
        if (currentUserId == null) return
        _userProfile.value = _userProfile.value.copy(email = auth.currentUser?.email ?: "")

        profileListener = db.collection("users").document(currentUserId!!)
            .addSnapshotListener { document, _ ->
                if (document == null) return@addSnapshotListener
                val name = document.getString("name") ?: "No Name"
                val avatarUrl = document.getString("avatarUrl") ?: ""
                val aboutMe = document.getString("aboutMe") ?: "Tell us about yourself!"
                val interests = document.get("interests") as? List<String> ?: emptyList()
                _userProfile.value = _userProfile.value.copy(
                    name = name,
                    avatarUrl = avatarUrl,
                    aboutMe = aboutMe,
                    interests = interests,
                    interestsCount = interests.size
                )
            }
    }

    private fun fetchAllEventIds() {
        allEventsListener?.remove()
        allEventsListener = db.collection("events").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                _allEventIds.value = snapshots.documents.map { it.id }.toSet()
                updateJoinedEventsCount()
            }
        }
    }

    private fun fetchJoinedEventsForCount() {
        if (currentUserId != null) {
            joinedEventsListenerForCount?.remove()
            joinedEventsListenerForCount = db.collection("users").document(currentUserId!!)
                .collection("joinedEvents")
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        _joinedEventIds.value = snapshots.documents.map { it.id }.toSet()
                        updateJoinedEventsCount()
                    }
                }
        }
    }

    private fun updateJoinedEventsCount() {
        val existingJoinedEvents = _joinedEventIds.value.intersect(_allEventIds.value)
        _userProfile.value = _userProfile.value.copy(eventsCount = existingJoinedEvents.size)
    }

    fun clearDataAndListeners() {
        profileListener?.remove()
        allEventsListener?.remove()
        joinedEventsListenerForCount?.remove()
        _userProfile.value = UserProfile() // Reset to default state
        _allEventIds.value = emptySet()
        _joinedEventIds.value = emptySet()
    }
}
