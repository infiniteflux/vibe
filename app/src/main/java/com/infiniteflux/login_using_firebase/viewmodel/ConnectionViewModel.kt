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
import java.text.SimpleDateFormat
import java.util.Locale


class ConnectionViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    private val _connections = MutableStateFlow<List<ConnectionInfo>>(emptyList())
    val connections: StateFlow<List<ConnectionInfo>> = _connections

    private var connectionsListener: ListenerRegistration? = null

    fun initializeData() {
        fetchConnections()
    }
    // --- NEW FUNCTION to delete a connection for both users ---
    fun deleteConnection(otherUserId: String) {
        if (currentUserId == null) return

        // Delete the connection from the current user's profile
        db.collection("users").document(currentUserId!!)
            .collection("connections").document(otherUserId).delete()

        // Delete the connection from the other user's profile
        db.collection("users").document(otherUserId)
            .collection("connections").document(currentUserId!!).delete()
    }


    private fun fetchConnections() {
        if (currentUserId == null) return
        connectionsListener?.remove()

        connectionsListener = db.collection("users").document(currentUserId!!)
            .collection("connections")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener

                viewModelScope.launch {
                    val connectionInfos = snapshots.documents.mapNotNull { doc ->
                        val connection = doc.toObject(Connection::class.java)
                        val connectedUserId = doc.id

                        if (connection != null) {
                            val userDoc = db.collection("users").document(connectedUserId).get().await()
                            val eventDoc = db.collection("events").document(connection.eventId).get().await()

                            val user = userDoc.toObject(User::class.java)
                            val event = eventDoc.toObject(Event::class.java)

                            if (user != null) {
                                // --- THE FIX ---
                                // If the event is found, use its title.
                                // If not (because it was deleted), use a default message.
                                val eventName = event?.title ?: "a past event"

                                ConnectionInfo(
                                    userId = user.id,
                                    userName = user.name,
                                    userAvatarUrl = user.avatarUrl,
                                    fromEvent = "From $eventName",
                                    matchDate = formatTimestamp(connection.connectedAt)
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

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val date = timestamp.toDate()
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }

    fun clearDataAndListeners() {
        connectionsListener?.remove()
        _connections.value = emptyList()
    }
}