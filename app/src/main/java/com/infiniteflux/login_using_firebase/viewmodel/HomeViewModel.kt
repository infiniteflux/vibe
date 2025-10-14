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
import java.util.Date

class HomeViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    // State properties
    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName

    private val _eventsCount = MutableStateFlow(0)
    val eventsCount: MutableStateFlow<Int> = _eventsCount


    private val _trendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val trendingEvents: StateFlow<List<Event>> = _trendingEvents

    private val _isLoadingTrending = MutableStateFlow(true)
    val isLoadingTrending: StateFlow<Boolean> = _isLoadingTrending

    private val _allEventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())

    // Listeners
    private var userDataListener: ListenerRegistration? = null
    private var allEventsListener: ListenerRegistration? = null
    private var joinedEventsListenerForCount: ListenerRegistration? = null

    init {}

    fun initializeData() {
        Log.d("HomeViewModel", "Initializing data for user: $currentUserId")
        fetchUserData()
        fetchAllEvents()
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
                    }
                }
        }
    }

    private fun fetchAllEvents() {
        _isLoadingTrending.value = true
        allEventsListener?.remove()
        Log.d("HomeViewModel", "Setting up main events listener...")
        allEventsListener = db.collection("events")
            .orderBy("startTimestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Error fetching all events", error)
                    _isLoadingTrending.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val allEvents = snapshots.documents.mapNotNull {
                        it.toObject(Event::class.java)?.copy(id = it.id)
                    }
                    Log.d("HomeViewModel", "All events updated: ${allEvents.size} events found.")

                    _allEventIds.value = allEvents.map { it.id }.toSet()
                    updateJoinedEventsCount()

                    _trendingEvents.value = allEvents
                        .filter { it.startTimestamp != null && it.startTimestamp.toDate().after(Date()) }
                        .sortedByDescending { it.joinCount }
                        .take(3)
                    Log.d("HomeViewModel", "Trending events updated: ${_trendingEvents.value.size} events found.")
                }
                _isLoadingTrending.value = false
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



    fun clearDataAndListeners() {
        Log.d("HomeViewModel", "Clearing all data and listeners.")
        userDataListener?.remove()
        allEventsListener?.remove()
        joinedEventsListenerForCount?.remove()
        _userName.value = "User"
        _eventsCount.value = 0
        _trendingEvents.value = emptyList()
        _joinedEventIds.value = emptySet()
        _isLoadingTrending.value = true
    }
}
