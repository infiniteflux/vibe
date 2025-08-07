package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.ListenerRegistration

class HomeViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    // State for the user's name
    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName

    // State for the number of joined events
    private val _eventsCount = MutableStateFlow(0)
    val eventsCount: StateFlow<Int> = _eventsCount

    // State for the list of trending events
    private val _trendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val trendingEvents: StateFlow<List<Event>> = _trendingEvents

    // Variables to hold the listener registrations
    private var userDataListener: ListenerRegistration? = null
    private var trendingEventsListener: ListenerRegistration? = null

    fun initializeData() {
        fetchUserData()
        fetchTrendingEvents()
    }

    fun fetchUserData() {
        if (currentUserId != null) {
            // --- THE FIX: Use addSnapshotListener for real-time updates ---
            // This will now listen for any changes to the user's document,
            // including name changes from the profile screen.
            userDataListener=db.collection("users").document(currentUserId!!)
                .addSnapshotListener { document, _ ->
                    if (document != null) {
                        _userName.value = document.getString("name") ?: "User"
                    }
                }

            // Fetch joined events count (this can remain the same as it's already a listener)
            db.collection("users").document(currentUserId!!)
                .collection("joinedEvents")
                .addSnapshotListener { snapshots, _ ->
                    _eventsCount.value = snapshots?.size() ?: 0
                }
        }
    }

    fun fetchTrendingEvents() {
        // For now, we'll just get the 3 most recently created events
        trendingEventsListener=db.collection("events")
            .orderBy("title", Query.Direction.DESCENDING) // Assuming newer events are added later
            .limit(3)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    _trendingEvents.value = snapshots.documents.mapNotNull {
                        it.toObject(Event::class.java)?.copy(id = it.id)
                    }
                }
            }
    }

    // New function to clear data and stop listening for changes
    fun clearDataAndListeners() {
        userDataListener?.remove()
        trendingEventsListener?.remove()
        _userName.value = "User"
        _eventsCount.value = 0
        _trendingEvents.value = emptyList()
    }
}
