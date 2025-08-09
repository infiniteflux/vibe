package com.infiniteflux.login_using_firebase.viewmodel

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

    // State for the user's name
    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName

    // State for the number of joined events
    private val _eventsCount = MutableStateFlow(0)
    val eventsCount: StateFlow<Int> = _eventsCount

    // State for the list of trending events
    private val _trendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val trendingEvents: StateFlow<List<Event>> = _trendingEvents

    // State to hold all event IDs and joined event IDs for accurate counting
    private val _allEventIds = MutableStateFlow<Set<String>>(emptySet())
    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())

    // Listeners
    private var userDataListener: ListenerRegistration? = null
    private var trendingEventsListener: ListenerRegistration? = null
    private var allEventsListener: ListenerRegistration? = null
    private var joinedEventsListenerForCount: ListenerRegistration? = null


    fun initializeData() {
        fetchUserData()
        fetchTrendingEvents()
        fetchAllEventIds()
        fetchJoinedEventsForCount()
    }

    private fun fetchUserData() {
        if (currentUserId != null) {
            userDataListener?.remove()
            userDataListener = db.collection("users").document(currentUserId!!)
                .addSnapshotListener { document, _ ->
                    if (document != null) {
                        _userName.value = document.getString("name") ?: "User"
                    }
                }
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
        _eventsCount.value = existingJoinedEvents.size
    }

    private fun fetchTrendingEvents() {
        trendingEventsListener?.remove()
        trendingEventsListener = db.collection("events")
            .orderBy("title", Query.Direction.DESCENDING)
            .limit(3)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    _trendingEvents.value = snapshots.documents.mapNotNull {
                        it.toObject(Event::class.java)?.copy(id = it.id)
                    }
                }
            }
    }

    fun clearDataAndListeners() {
        userDataListener?.remove()
        trendingEventsListener?.remove()
        allEventsListener?.remove()
        joinedEventsListenerForCount?.remove()
        _userName.value = "User"
        _eventsCount.value = 0
        _trendingEvents.value = emptyList()
        _allEventIds.value = emptySet()
        _joinedEventIds.value = emptySet()
    }
}
