package com.infiniteflux.login_using_firebase.viewmodel
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.data.JoinedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

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

    // --- 2. UPDATED createEvent function to handle image upload ---
    fun createEvent(
        title: String,
        location: String,
        date: String,
        category: String,
        description: String,
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
                description = description, host = host, imageUrl = "https://placehold.co/600x400"
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
                        title = title, location = location, date = date, category = category,
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
