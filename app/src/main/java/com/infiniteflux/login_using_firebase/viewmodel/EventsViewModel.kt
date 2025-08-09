package com.infiniteflux.login_using_firebase.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
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


    fun submitRating(eventId: String, ratedUserId: String, rating: String) {
        if (currentUserId == null) return

        val currentUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(currentUserId!!)

        // This creates a map named "ratings" which contains another map of the user's ratings.
        // SetOptions.merge() ensures we don't overwrite other ratings in the same document.
        val ratingData = mapOf("ratings" to mapOf(ratedUserId to rating))
        currentUserRatingRef.set(ratingData, SetOptions.merge())
            .addOnSuccessListener {
                // After saving, check for a mutual match
                checkForMutualConnection(eventId, ratedUserId, rating)
            }
    }

    private fun checkForMutualConnection(eventId: String, otherUserId: String, myRating: String) {
        if (currentUserId == null) return

        val otherUserRatingRef = db.collection("events").document(eventId)
            .collection("ratings").document(otherUserId)

        otherUserRatingRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Directly access the nested map
                val theirRatings = document.get("ratings") as? Map<String, String> ?: emptyMap()
                val theirRatingForMe = theirRatings[currentUserId!!]

                if (theirRatingForMe != null && theirRatingForMe == myRating) {
                    val connection = Connection(eventId = eventId)

                    db.collection("users").document(currentUserId!!)
                        .collection("connections").document(otherUserId).set(connection)

                    db.collection("users").document(otherUserId)
                        .collection("connections").document(currentUserId!!).set(connection)
                }
            }
        }
    }



    // --- 2. UPDATED createEvent function to handle image upload ---
    fun createEvent(
        title: String,
        location: String,
        date: String,
        category: String,
        description: String,
        durationHours: Int,
        host: String,
        imageUri: Uri?, // Pass the selected image URI
        onSuccess: () -> Unit // Callback to run on success
    ) {
        if (title.isBlank() || location.isBlank() || date.isBlank() || category.isBlank() || description.isBlank() || host.isBlank()) {
            return
        }

        // If no image is selected, use a placeholder and create the event directly
        if (imageUri == null) {
            val newEvent = Event(
                title = title, location = location, date = date, category = category,
                description = description, host = host, durationHours = durationHours, imageUrl = "https://placehold.co/600x400"
            )
            db.collection("events").add(newEvent).addOnSuccessListener { onSuccess() }
            return
        }

        // If an image is selected, upload it first
        val imageFileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("event_images/$imageFileName.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // After upload, get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Now create the event with the correct image URL
                    val newEvent = Event(
                        title = title, location = location, date = date, category = category, durationHours = durationHours,
                        description = description, host = host, imageUrl = downloadUrl.toString()
                    )
                    db.collection("events").add(newEvent).addOnSuccessListener { onSuccess() }
                }
            }
            .addOnFailureListener {
                // Handle upload failure
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
}
