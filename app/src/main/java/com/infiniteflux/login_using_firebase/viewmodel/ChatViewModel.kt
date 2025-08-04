package com.infiniteflux.login_using_firebase.viewmodel
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp // <-- 1. Import ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.Group
import com.infiniteflux.login_using_firebase.data.Message
import com.infiniteflux.login_using_firebase.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



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

    // --- 2. State to hold all users in the app ---
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    // --- 3. NEW FUNCTION: Fetch all users from the 'users' collection ---
    fun fetchAllUsers() {
        db.collection("users")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _allUsers.value = snapshots.documents.mapNotNull {
                    it.toObject(User::class.java)?.copy(id = it.id)
                }
            }
    }

    // --- 4. NEW FUNCTION: Add a user to a group's member list ---
    fun addMemberToGroup(groupId: String, userId: String) {
        if (groupId.isBlank() || userId.isBlank()) return

        val groupRef = db.collection("groups").document(groupId)
        // Use FieldValue.arrayUnion to safely add a new member without creating duplicates
        groupRef.update("memberIds", FieldValue.arrayUnion(userId))
    }


    fun createGroup(groupName: String, relatedEvent: String) {
        if (currentUserId == null || groupName.isBlank()) return

        // Create a new Group object
        val newGroup = Group(
            name = groupName,
            relatedEvent = relatedEvent,
            // Add the creator as the first member
            memberIds = listOf(currentUserId),
            // You can set a default avatar URL
            groupAvatarUrl = "https://avatars.githubusercontent.com/u/147044141?s=400&u=0848775e883324ec1bd028ca3a7bc3ded25a0f18&v=4"
        )

        // Add the new group to the 'groups' collection in Firestore
        db.collection("groups").add(newGroup)
        // You can add .addOnSuccessListener and .addOnFailureListener here for feedback
    }

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
