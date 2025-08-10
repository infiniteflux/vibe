package com.infiniteflux.login_using_firebase.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    // State properties
    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName

    private val _eventsCount = MutableStateFlow(0)
    val eventsCount: StateFlow<Int> = _eventsCount

    private val _trendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val trendingEvents: StateFlow<List<Event>> = _trendingEvents

    private val _isLoadingTrending = MutableStateFlow(true)
    val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending

    private val _allEventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())

    // Listeners
    private var userDataListener: ListenerRegistration? = null
    private var trendingEventsListener: ListenerRegistration? = null
    private var allEventsListener: ListenerRegistration? = null
    private var joinedEventsListenerForCount: ListenerRegistration? = null

    init {}

    fun initializeData() {
        Log.d("HomeViewModel", "Initializing data for user: $currentUserId")
        fetchUserData()
        fetchAllEventIds()
        fetchJoinedEventsForCount()
    }

    private fun fetchUserData() {
        userDataListener?.remove()
        if (currentUserId != null) {
            Log.d("HomeViewModel", "Fetching user data...")
            userDataListener = db.collection("users").document(currentUserId!!)
                .addSnapshotListener { document, _ ->
                    if (document != null) {
                        _userName.value = document.getString("name") ?: "User"
                        Log.d("HomeViewModel", "User name updated: ${_userName.value}")
                    }
                }
        }
    }

    private fun fetchAllEventIds() {
        allEventsListener?.remove()
        Log.d("HomeViewModel", "Fetching all event IDs...")
        allEventsListener = db.collection("events").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                _allEventIds.value = snapshots.documents.map { it.id }.toSet()
                Log.d("HomeViewModel", "All event IDs updated: ${_allEventIds.value.size} events found.")
                updateJoinedEventsCount()
                fetchTrendingEvents()
            }
        }
    }

    private fun fetchJoinedEventsForCount() {
        joinedEventsListenerForCount?.remove()
        if (currentUserId != null) {
            Log.d("HomeViewModel", "Fetching joined event IDs for count...")
            joinedEventsListenerForCount = db.collection("users").document(currentUserId!!)
                .collection("joinedEvents")
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        _joinedEventIds.value = snapshots.documents.map { it.id }.toSet()
                        Log.d("HomeViewModel", "Joined event IDs updated: ${_joinedEventIds.value.size} events joined.")
                        updateJoinedEventsCount()
                    }
                }
        }
    }

    private fun updateJoinedEventsCount() {
        val existingJoinedEvents = _joinedEventIds.value.intersect(_allEventIds.value)
        _eventsCount.value = existingJoinedEvents.size
        Log.d("HomeViewModel", "Recalculated joined events count: ${_eventsCount.value}")
    }

    // --- THE FIX: Updated the query to sort by joinCount ---
    private fun fetchTrendingEvents() {
        _isLoadingTrending.value = true
        trendingEventsListener?.remove()
        Log.d("HomeViewModel", "Fetching trending events...")
        trendingEventsListener = db.collection("events")
            // 1. Filter for events that haven't started yet.
            .whereGreaterThan("startTimestamp", com.google.firebase.Timestamp.now())
            // 2. Sort by the number of people who have joined, most popular first.
            .orderBy("joinCount", Query.Direction.DESCENDING)
            // 3. Get the top 3 most popular upcoming events.
            .limit(3)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // This will log an error if you are missing the necessary index.
                    Log.e("HomeViewModel", "Error fetching trending events", error)
                    _isLoadingTrending.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _trendingEvents.value = snapshots.documents.mapNotNull {
                        it.toObject(Event::class.java)?.copy(id = it.id)
                    }
                    Log.d("HomeViewModel", "Trending events updated: ${_trendingEvents.value.size} events found.")
                }
                _isLoadingTrending.value = false
            }
    }

    fun clearDataAndListeners() {
        Log.d("HomeViewModel", "Clearing all data and listeners.")
        userDataListener?.remove()
        trendingEventsListener?.remove()
        allEventsListener?.remove()
        joinedEventsListenerForCount?.remove()
        _userName.value = "User"
        _eventsCount.value = 0
        _trendingEvents.value = emptyList()
        _allEventIds.value = emptySet()
        _joinedEventIds.value = emptySet()
        _isLoadingTrending.value = true
    }
}
