package com.infiniteflux.login_using_firebase.viewmodel
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.Group
import com.infiniteflux.login_using_firebase.data.GroupReadStatus
import com.infiniteflux.login_using_firebase.data.Message
import com.infiniteflux.login_using_firebase.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.ktx.storage
import com.infiniteflux.login_using_firebase.data.ChatRoom
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


// --- The ViewModel Class ---
class ChatViewModel : ViewModel() {
    // --- 2. Store your listeners in variables ---
    private var groupsListener: ListenerRegistration? = null
    private var readStatusListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null


    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    val currentUserId get() = auth.currentUser?.uid

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers


    private val _groupReadStatus = MutableStateFlow<Map<String, GroupReadStatus>>(emptyMap())
    val groupReadStatus: StateFlow<Map<String, GroupReadStatus>> = _groupReadStatus

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun initializeData() {
        fetchUserGroups()
        fetchGroupReadStatuses()
    }

    fun fetchAllUsers() {
        db.collection("users")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                _allUsers.value = snapshots.documents.mapNotNull {
                    it.toObject(User::class.java)?.copy(id = it.id)
                }
            }
    }


    fun addMemberToGroup(groupId: String, userId: String) {
        if (groupId.isBlank() || userId.isBlank()) return

        val groupRef = db.collection("groups").document(groupId)
        groupRef.update("memberIds", FieldValue.arrayUnion(userId))
    }


    fun createGroup(
        groupName: String,
        relatedEvent: String,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        if (currentUserId == null || groupName.isBlank()) return

        // If no image is selected, use a default placeholder and create the group
        if (imageUri == null) {
            val newGroup = Group(
                name = groupName,
                relatedEvent = relatedEvent,
                memberIds = listOf(currentUserId!!),
                groupAvatarUrl = "https://placehold.co/100"
            )
            db.collection("groups").add(newGroup).addOnSuccessListener { onSuccess() }
            return
        }

        // If an image is selected, upload it first
        val imageFileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("group_avatars/$imageFileName.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // After upload, get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val newGroup = Group(
                        name = groupName,
                        relatedEvent = relatedEvent,
                        memberIds = listOf(currentUserId!!),
                        groupAvatarUrl = downloadUrl.toString()
                    )
                    db.collection("groups").add(newGroup).addOnSuccessListener { onSuccess() }
                }
            }
            .addOnFailureListener {
                // Handle upload failure
            }
    }

    fun fetchUserGroups() {
        _isLoading.value = true
        if (currentUserId == null) {
            _isLoading.value = false
            return
        }
        groupsListener = db.collection("groups")
            .whereArrayContains("memberIds", currentUserId!!)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ChatViewModel", "Error fetching groups: ", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    _groups.value = snapshots.documents.mapNotNull {
                        it.toObject(Group::class.java)?.copy(id = it.id)
                    }
                }
                _isLoading.value = false
            }
    }


    fun listenForMessages(groupId: String) {
        messagesListener=db.collection("groups").document(groupId)
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
        val message = Message(
            text = text,
            senderId = currentUserId!!,
            senderName = senderName
        )
        val groupRef = db.collection("groups").document(groupId)
        groupRef.collection("messages").add(message)
        groupRef.update(
            "lastMessageText", text,
            "lastMessageSenderName", senderName,
            "lastMessageTimestamp", FieldValue.serverTimestamp() // Use server timestamp
        )
        markGroupAsRead(groupId)
    }

    fun markGroupAsRead(groupId: String) {
        if (currentUserId == null) return

        val readStatusRef = db.collection("users").document(currentUserId!!)
            .collection("groupReadStatus").document(groupId)
        readStatusRef.set(mapOf("lastReadTimestamp" to FieldValue.serverTimestamp()))
    }

    fun fetchGroupReadStatuses() {
        if (currentUserId == null) return

        readStatusListener=db.collection("users").document(currentUserId!!)
            .collection("groupReadStatus")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener
                val statusMap = snapshots.documents.associate { doc ->
                    doc.id to (doc.toObject(GroupReadStatus::class.java) ?: GroupReadStatus())
                }
                _groupReadStatus.value = statusMap
            }
    }

    // private chat started from here ..
    fun getOrCreateChatRoom(otherUserId: String, onComplete: (String) -> Unit) {
        if (currentUserId == null) return

        val chatRoomId = if (currentUserId!! > otherUserId) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }

        val chatRoomRef = db.collection("chats").document(chatRoomId)

        viewModelScope.launch {
            try {
                val document = chatRoomRef.get().await()
                if (!document.exists()) {
                    val newChatRoom = ChatRoom(
                        id = chatRoomId,
                        participants = listOf(currentUserId!!, otherUserId)
                    )
                    chatRoomRef.set(newChatRoom).await()
                }
                onComplete(chatRoomId)
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    fun listenForPrivateChatMessages(chatRoomId: String) {
        messagesListener = db.collection("chats").document(chatRoomId)
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
    fun sendPrivateMessage(chatRoomId: String, text: String, senderName: String) {
        if (currentUserId == null || text.isBlank()) return
        val message = Message(
            text = text,
            senderId = currentUserId!!,
            senderName = senderName
        )
        db.collection("chats").document(chatRoomId).collection("messages").add(message)
    }


    // --- 4. Add a function to clear data and remove listeners ---
    fun clearDataAndListeners() {
        groupsListener?.remove()
        readStatusListener?.remove()
        messagesListener?.remove()
        _groups.value = emptyList()
        _groupReadStatus.value = emptyMap()
        _messages.value = emptyList()
    }

}
