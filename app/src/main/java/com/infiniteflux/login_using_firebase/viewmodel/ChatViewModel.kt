package com.infiniteflux.login_using_firebase.viewmode
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp // <-- 1. Import ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// --- Data Classes for Chat ---

data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = ""
)

data class Group(
    val id: String = "",
    val name: String = "",
    val relatedEvent: String = "",
    val memberIds: List<String> = listOf(),
    val groupAvatarUrl: String = ""
)

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    // --- 2. Add the @ServerTimestamp annotation ---
    // This tells Firestore to automatically manage this field.
    @ServerTimestamp
    val timestamp: Timestamp? = null
)


// --- The ViewModel Class ---
class ChatViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    val currentUserId = auth.currentUser?.uid

    // --- State for the Group List Screen ---
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    // --- State for the Chat Screen ---
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun fetchUserGroups() {
        if (currentUserId == null) return

        db.collection("groups")
            .whereArrayContains("memberIds", currentUserId)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _groups.value = snapshots.documents.mapNotNull {
                    it.toObject(Group::class.java)?.copy(id = it.id)
                }
            }
    }

    fun listenForMessages(groupId: String) {
        db.collection("groups").document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _messages.value = snapshots.documents.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                }
            }
    }

    fun sendMessage(groupId: String, text: String, senderName: String) {
        if (currentUserId == null || text.isBlank()) return

        // --- 3. Remove the timestamp from the message object ---
        // Firestore will now add it automatically on the server.
        val message = Message(
            text = text,
            senderId = currentUserId,
            senderName = senderName
            // The timestamp is now handled by the @ServerTimestamp annotation
        )

        db.collection("groups").document(groupId)
            .collection("messages")
            .add(message)
    }
}
