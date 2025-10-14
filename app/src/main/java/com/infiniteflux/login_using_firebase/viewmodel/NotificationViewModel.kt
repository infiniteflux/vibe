package com.infiniteflux.login_using_firebase.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class NotificationViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val currentUserId get() = auth.currentUser?.uid

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private var notificationsListener: ListenerRegistration? = null

    fun initializeData() {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        if (currentUserId == null) return

        notificationsListener?.remove()
        notificationsListener = db.collection("users").document(currentUserId!!)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("NotificationViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _notifications.value = snapshots.documents.mapNotNull {
                        it.toObject(AppNotification::class.java)?.copy(id = it.id)
                    }
                }
            }
    }

    fun clearDataAndListeners() {
        notificationsListener?.remove()
        _notifications.value = emptyList()
    }
}
