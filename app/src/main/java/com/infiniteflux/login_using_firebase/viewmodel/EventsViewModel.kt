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
    private val storage = Firebase.storage // 1. Get a reference to Storage
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

        // This query requires a Firestore Index. If it fails, check your Logcat for a URL
        // that will automatically create the index for you.
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
                // This will print the error in your logs if the index is missing.
                Log.e("EventsViewModel", "Error getting attendees: ", exception)
                onResult(emptyList())
            }
    }


    // --- 2. UPDATED: submitRating is now a suspend function that returns a MatchResult ---
    suspend fun submitRating(eventId: String, ratedUserId: String, rating: String): MatchResult {
        if (currentUserId == null) return MatchResult.NoMatch

        val currentUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(currentUserId!!)

        val ratingData = mapOf("ratings" to mapOf(ratedUserId to rating))

        // Use await() to make the function wait until the upload is complete
        currentUserRatingRef.set(ratingData, SetOptions.merge()).await()

        // After saving, check for a mutual match and return the result
        return checkForMutualConnection(eventId, ratedUserId, rating)
    }

    private suspend fun checkForMutualConnection(eventId: String, otherUserId: String, myRating: String): MatchResult {
        if (currentUserId == null) return MatchResult.NoMatch

        val otherUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(otherUserId)

        return try {
            val document = otherUserRatingRef.get().await()
            if (!document.exists()) {
                // Case 1: They haven't rated anyone yet.
                return MatchResult.Pending
            }

            val theirRatings = document.get("ratings") as? Map<String, String> ?: emptyMap()
            val theirRatingForMe = theirRatings[currentUserId!!]

            when (theirRatingForMe) {
                myRating -> {
                    // Case 2: It's a match! Create the connection.
                    Log.d("EventsViewModel", "Mutual match found! Creating connection.")
                    val connection = Connection(eventId = eventId)
                    db.collection("users").document(currentUserId!!).collection("connections").document(otherUserId).set(connection).await()
                    db.collection("users").document(otherUserId).collection("connections").document(currentUserId!!).set(connection).await()
                    MatchResult.Match
                }
                null -> {
                    // Case 3: They have rated others, but not me yet.
                    MatchResult.Pending
                }
                else -> {
                    // Case 4: They rated me, but it's a different rating. No connection.
                    MatchResult.NoMatch
                }
            }
        } catch (e: Exception) {
            Log.e("EventsViewModel", "Error checking for mutual connection", e)
            MatchResult.NoMatch // Handle any errors
        }
    }


    // --- 2. UPDATED createEvent function to handle image upload ---
    fun createEvent(
        title: String,
        location: String,
        dateString: String, // Keep the formatted string for display
        category: String,
        description: String,
        host: String,
        durationHours: Int,
        imageUri: Uri?,
        eventStartDate: Date, // The actual start date and time
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || location.isBlank() || dateString.isBlank() || category.isBlank() || description.isBlank() || host.isBlank()) {
            return
        }

        val createEventWithUrl = { imageUrl: String ->
            val newEvent = Event(
                title = title,
                location = location,
                date = dateString, // Save the formatted string
                category = category,
                description = description,
                host = host,
                imageUrl = imageUrl,
                durationHours = durationHours,
                // Convert the user-selected Date into a Firebase Timestamp
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

    // --- NEW FUNCTION to get users already rated by the current user ---
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
                onSuccess() // This is to navigate back after deletion
            }
            .addOnFailureListener {
                // Handle any errors, like showing a toast
            }
    }
}

sealed class MatchResult {
    object Match : MatchResult()
    object NoMatch : MatchResult()
    object Pending : MatchResult() // The other user hasn't rated yet
}
