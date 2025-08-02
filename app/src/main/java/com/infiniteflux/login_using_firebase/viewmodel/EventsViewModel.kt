package com.infiniteflux.login_using_firebase.viewmodel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.infiniteflux.login_using_firebase.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Updated Event data class with more details
data class Event(
    val id: Int,
    val title: String,
    val location: String,
    val date: String,
    val joinedCount: Int,
    val totalCount: Int,
    val imageRes: Int, // Using drawable resource for simplicity
    var isJoined: Boolean,
    val category: String,
    val isCurated: Boolean = true,
    val description: String,
    val host: String
)

// Updated dummy data for events
private val sampleEvents = listOf(
    Event(1, "Coffee & Connections", "Campus Café", "12/7/2025 at 2:30 PM", 18, 24, R.drawable.lazy, false, "Social", true, "The perfect opportunity to network with fellow students and professionals in a relaxed, cozy cafe setting. Grab a coffee and make some new connections!", "VIBE Community"),
    Event(2, "Taco Tuesday Fiesta", "El Mariachi Restaurant", "15/7/2025 at 11:30 PM", 12, 30, R.drawable.lazy, true, "Food", true, "Join us for a delicious Taco Tuesday! Enjoy authentic Mexican food, great music, and even better company. All-you-can-eat tacos for the first hour.", "El Mariachi"),
    Event(3, "Study Group Session", "Library Room 301", "18/7/2025 at 4:00 PM", 5, 15, R.drawable.lazy, false, "Study", false, "Need to cram for finals? Join our group study session for Computer Science 101. We'll have snacks and a teaching assistant on hand to answer questions.", "CS Department"),
    Event(4, "Weekend Movie Night", "Student Union Cinema", "20/7/2025 at 8:00 PM", 25, 50, R.drawable.lazy, false, "Social", false, "Unwind after a long week with a free movie night at the student cinema. We'll be showing a recent blockbuster. Popcorn is on us!", "Student Activities Board")
)

class EventsViewModel : ViewModel() {
    private val _events = MutableStateFlow(sampleEvents)
    val events: StateFlow<List<Event>> = _events

    fun findEvent(eventId: Int): Event? {
        return _events.value.find { it.id == eventId }
    }

    fun toggleJoinedStatus(eventId: Int) {
        val currentEvents = _events.value.toMutableList()
        val eventIndex = currentEvents.indexOfFirst { it.id == eventId }
        if (eventIndex != -1) {
            val oldEvent = currentEvents[eventIndex]
            val newEvent = oldEvent.copy(isJoined = !oldEvent.isJoined)
            currentEvents[eventIndex] = newEvent
            _events.value = currentEvents
        }
    }
}
