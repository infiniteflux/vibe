package com.infiniteflux.login_using_firebase.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.infiniteflux.login_using_firebase.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.ListenerRegistration

class ProfileViewModel : ViewModel() {

    private var profileListener: ListenerRegistration? = null
    private var joinedEventsListener: ListenerRegistration? = null

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId get() = auth.currentUser?.uid

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    fun initializeData() {
        fetchUserProfile()
    }

    // --- 3. NEW FUNCTION TO UPLOAD THE IMAGE AND UPDATE THE PROFILE ---
    fun uploadProfileImage(imageUri: Uri) {
        if (currentUserId == null) return

        // Create a reference to the location where the image will be stored
        val storageRef = storage.reference.child("profile_images/$currentUserId.jpg")

        // Upload the file
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // After upload is successful, get the public download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Save the new URL to the user's document in Firestore
                    db.collection("users").document(currentUserId!!)
                        .update("avatarUrl", downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                // Handle any errors during upload
            }
    }

    // --- NEW FUNCTION TO UPDATE THE USER'S PROFILE ---
    fun updateUserProfile(newName: String, newAboutMe: String, newInterests: List<String>) {
        if (currentUserId == null) return

        val userDocRef = db.collection("users").document(currentUserId!!)
        val updates = mapOf(
            "name" to newName,
            "aboutMe" to newAboutMe,
            "interests" to newInterests
        )

        userDocRef.update(updates)
            .addOnSuccessListener {
                // Optionally provide success feedback
            }
            .addOnFailureListener {
                // Optionally provide error feedback
            }
    }

    fun fetchUserProfile() {
        if (currentUserId == null) return
        _userProfile.value = _userProfile.value.copy(email = auth.currentUser?.email ?: "")

        profileListener=db.collection("users").document(currentUserId!!)
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

        joinedEventsListener=db.collection("users").document(currentUserId!!)
            .collection("joinedEvents")
            .addSnapshotListener { snapshots, _ ->
                _userProfile.value = _userProfile.value.copy(eventsCount = snapshots?.size() ?: 0)
            }
    }

    fun clearDataAndListeners() {
        profileListener?.remove()
        joinedEventsListener?.remove()
        _userProfile.value = UserProfile() // Reset to default state
    }
}
