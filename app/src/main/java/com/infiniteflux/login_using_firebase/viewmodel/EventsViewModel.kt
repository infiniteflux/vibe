package com.infiniteflux.login_using_firebase.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.data.JoinedEvent
import com.infiniteflux.login_using_firebase.data.User
import com.infiniteflux.login_using_firebase.data.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await
import java.util.Date

class EventsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val currentUserId get() = auth.currentUser?.uid

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val joinedEventIds: StateFlow<Set<String>> = _joinedEventIds

    private var eventsListener: ListenerRegistration? = null
    private var joinedEventsListener: ListenerRegistration? = null

    init {
        fetchEvents()
    }

    fun initializeData() {
        fetchJoinedEvents()
    }

    fun getAttendees(eventId: String, onResult: (List<User>) -> Unit) {
        if (currentUserId == null) {
            onResult(emptyList())
            return
        }

        db.collectionGroup("joinedEvents").whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }
                val userIds = snapshot.documents.map { it.reference.parent.parent!!.id }

                db.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                    .addOnSuccessListener { userSnapshot ->
                        val users = userSnapshot.toObjects(User::class.java)
                        onResult(users.filter { it.id != currentUserId })
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("EventsViewModel", "Error getting attendees: ", exception)
                onResult(emptyList())
            }
    }

    suspend fun submitRating(eventId: String, ratedUserId: String, rating: String): MatchResult {
        if (currentUserId == null) return MatchResult.NoMatch

        val currentUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(currentUserId!!)

        val ratingData = mapOf("ratings" to mapOf(ratedUserId to rating))
        currentUserRatingRef.set(ratingData, SetOptions.merge()).await()
        return checkForMutualConnection(eventId, ratedUserId, rating)
    }

    private suspend fun checkForMutualConnection(eventId: String, otherUserId: String, myRating: String): MatchResult {
        if (currentUserId == null) return MatchResult.NoMatch

        val otherUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(otherUserId)

        return try {
            val document = otherUserRatingRef.get().await()
            if (!document.exists()) {
                return MatchResult.Pending
            }

            val theirRatings = document.get("ratings") as? Map<String, String> ?: emptyMap()
            val theirRatingForMe = theirRatings[currentUserId!!]

            when (theirRatingForMe) {
                myRating -> {
                    Log.d("EventsViewModel", "Mutual match found! Creating connection.")
                    val connection = Connection(eventId = eventId)
                    db.collection("users").document(currentUserId!!).collection("connections").document(otherUserId).set(connection).await()
                    db.collection("users").document(otherUserId).collection("connections").document(currentUserId!!).set(connection).await()
                    MatchResult.Match
                }
                null -> {
                    MatchResult.Pending
                }
                else -> {
                    MatchResult.NoMatch
                }
            }
        } catch (e: Exception) {
            Log.e("EventsViewModel", "Error checking for mutual connection", e)
            MatchResult.NoMatch
        }
    }

    fun createEvent(
        title: String,
        location: String,
        dateString: String,
        category: String,
        description: String,
        host: String,
        durationHours: Int,
        imageUri: Uri?,
        eventStartDate: Date,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || location.isBlank() || dateString.isBlank() || category.isBlank() || description.isBlank() || host.isBlank()) {
            return
        }

        val createEventWithUrl = { imageUrl: String ->
            val newEvent = Event(
                title = title,
                location = location,
                date = dateString,
                category = category,
                description = description,
                host = host,
                imageUrl = imageUrl,
                durationHours = durationHours,
                startTimestamp = Timestamp(eventStartDate)
            )
            db.collection("events").add(newEvent).addOnSuccessListener { onSuccess() }
        }

        if (imageUri == null) {
            createEventWithUrl("https://placehold.co/600x400")
            return
        }

        val imageFileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("event_images/$imageFileName.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    createEventWithUrl(downloadUrl.toString())
                }
            }
    }

    fun fetchEvents() {
        eventsListener?.remove()
        eventsListener = db.collection("events")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _events.value = snapshots.documents.mapNotNull {
                    it.toObject(Event::class.java)?.copy(id = it.id)
                }
            }
    }

    fun fetchJoinedEvents() {
        joinedEventsListener?.remove()
        if (currentUserId == null) return
        joinedEventsListener = db.collection("users").document(currentUserId!!)
            .collection("joinedEvents")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _joinedEventIds.value = snapshots.documents.map { it.id }.toSet()
            }
    }

    fun findEvent(eventId: String): Event? {
        return _events.value.find { it.id == eventId }
    }

    fun toggleJoinedStatus(eventId: String) {
        if (currentUserId == null) return
        val eventRef = db.collection("users").document(currentUserId!!)
            .collection("joinedEvents").document(eventId)
        if (_joinedEventIds.value.contains(eventId)) {
            eventRef.delete()
        } else {
            eventRef.set(JoinedEvent(eventId = eventId))
        }
    }

    fun clearDataAndListeners() {
        joinedEventsListener?.remove()
        _joinedEventIds.value = emptySet()
    }
    fun getMyRatedUsersForEvent(eventId: String, onResult: (Set<String>) -> Unit) {
        if (currentUserId == null) {
            onResult(emptySet())
            return
        }
        db.collection("events").document(eventId)
            .collection("ratings").document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val ratingsMap = document.get("ratings") as? Map<String, String> ?: emptyMap()
                    onResult(ratingsMap.keys)
                } else {
                    onResult(emptySet())
                }
            }
    }
    fun deleteEvent(eventId: String, onSuccess: () -> Unit) {
        db.collection("events").document(eventId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
            }
    }
}

sealed class MatchResult {
    object Match : MatchResult()
    object NoMatch : MatchResult()
    object Pending : MatchResult()
}
