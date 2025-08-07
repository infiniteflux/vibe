package com.infiniteflux.login_using_firebase.viewmodel
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.data.JoinedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.ListenerRegistration

class EventsViewModel : ViewModel() {

    private var eventsListener: ListenerRegistration? = null
    private var joinedEventsListener: ListenerRegistration? = null

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUserId get() = auth.currentUser?.uid


    // --- State for the list of all events ---
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    // --- State to hold the IDs of events the user has joined ---
    private val _joinedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val joinedEventIds: StateFlow<Set<String>> = _joinedEventIds

    init {
        // --- THE FIX 1: Fetch public events immediately when the ViewModel is created. ---
        // This will now work for both guests and logged-in users.
        fetchEvents()
    }

    fun initializeData() {
        fetchEvents()
        fetchJoinedEvents()
    }


    // --- NEW FUNCTION TO CREATE AN EVENT ---
    /**
     * Creates a new event document in the 'events' collection.
     */
    fun createEvent(
        title: String,
        location: String,
        date: String,
        category: String,
        description: String,
        host: String
    ) {
        // Basic validation
        if (title.isBlank() || location.isBlank() || date.isBlank() || category.isBlank() || description.isBlank() || host.isBlank()) {
            // In a real app, you'd want to show an error to the user
            return
        }

        val newEvent = Event(
            title = title,
            location = location,
            date = date,
            category = category,
            description = description,
            host = host,
            imageUrl = "https://avatars.githubusercontent.com/u/147044141?s=400&u=0848775e883324ec1bd028ca3a7bc3ded25a0f18&v=4" // Use a default placeholder image
        )

        // Add the new event to the 'events' collection
        db.collection("events").add(newEvent)
            .addOnSuccessListener {
                // You could add a success message or navigation here if needed
            }
            .addOnFailureListener {
                // Handle the error
            }
    }

    /**
     * Fetches all events from the 'events' collection.
     */
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

    /**
     * Fetches the IDs of all events the current user has joined.
     */
    fun fetchJoinedEvents() {
        joinedEventsListener?.remove()
        if (currentUserId == null) return

        joinedEventsListener=db.collection("users").document(currentUserId!!)
            .collection("joinedEvents")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                // We only need the document IDs, which are the event IDs
                _joinedEventIds.value = snapshots.documents.map { it.id }.toSet()
            }
    }

    /**
     * Finds a specific event from the already fetched list.
     */
    fun findEvent(eventId: String): Event? {
        return _events.value.find { it.id == eventId }
    }

    /**
     * Toggles the user's joined status for a specific event.
     */
    fun toggleJoinedStatus(eventId: String) {
        if (currentUserId == null) return

        val eventRef = db.collection("users").document(currentUserId!!)
            .collection("joinedEvents").document(eventId)

        if (_joinedEventIds.value.contains(eventId)) {
            // If the user has already joined, delete the document to "un-join"
            eventRef.delete()
        } else {
            // If the user has not joined, create the document to "join"
            eventRef.set(JoinedEvent(eventId = eventId))
        }
    }

    fun clearDataAndListeners() {
        joinedEventsListener?.remove()
        _joinedEventIds.value = emptySet()
    }
}
