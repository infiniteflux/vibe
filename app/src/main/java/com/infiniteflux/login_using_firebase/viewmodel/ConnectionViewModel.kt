package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.Connection
import com.infiniteflux.login_using_firebase.data.ConnectionInfo
import com.infiniteflux.login_using_firebase.data.Event
import com.infiniteflux.login_using_firebase.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ConnectionViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    // --- 2. StateFlow to hold the final list of connection info ---
    private val _connections = MutableStateFlow<List<ConnectionInfo>>(emptyList())
    val connections: StateFlow<List<ConnectionInfo>> = _connections

    private var connectionsListener: ListenerRegistration? = null

    fun initializeData() {
        fetchConnections()
    }

    private fun fetchConnections() {
        if (currentUserId == null) return
        connectionsListener?.remove()

        // --- 3. Listen for changes in the user's connections sub-collection ---
        connectionsListener = db.collection("users").document(currentUserId!!)
            .collection("connections")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener

                // Use a coroutine to handle the async calls for fetching details
                viewModelScope.launch {
                    val connectionInfos = snapshots.documents.mapNotNull { doc ->
                        val connection = doc.toObject(Connection::class.java)
                        val connectedUserId = doc.id

                        if (connection != null) {
                            // --- 4. Fetch the connected user's profile and the event details ---
                            val userDoc = db.collection("users").document(connectedUserId).get().await()
                            val eventDoc = db.collection("events").document(connection.eventId).get().await()

                            val user = userDoc.toObject(User::class.java)
                            val event = eventDoc.toObject(Event::class.java)

                            if (user != null && event != null) {
                                ConnectionInfo(
                                    userId = user.id,
                                    userName = user.name,
                                    userAvatarUrl = user.avatarUrl,
                                    fromEvent = "From ${event.title}",
                                    matchDate = connection.connectedAt?.toDate().toString() // You can format this date later
                                )
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }
                    _connections.value = connectionInfos
                }
            }
    }

    fun clearDataAndListeners() {
        connectionsListener?.remove()
        _connections.value = emptyList()
    }
}


