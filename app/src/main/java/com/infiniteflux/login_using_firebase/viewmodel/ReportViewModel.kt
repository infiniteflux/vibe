package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.ConnectionForReport
import com.infiniteflux.login_using_firebase.data.Report
import com.infiniteflux.login_using_firebase.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReportViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUserId get() = auth.currentUser?.uid

    private val _connections = MutableStateFlow<List<ConnectionForReport>>(emptyList())
    val connections: StateFlow<List<ConnectionForReport>> = _connections

    fun initializeData() {
        fetchConnectionsForReporting()
    }

    private fun fetchConnectionsForReporting() {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId!!)
            .collection("connections")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener

                viewModelScope.launch {
                    val connectionList = snapshots.documents.mapNotNull { doc ->
                        val connectedUserId = doc.id
                        val userDoc = db.collection("users").document(connectedUserId).get().await()
                        val user = userDoc.toObject(User::class.java)
                        user?.let {
                            ConnectionForReport(userId = it.id, userName = it.name)
                        }
                    }
                    _connections.value = connectionList
                }
            }
    }

    fun submitReport(reportedUserId: String, reason: String, onSuccess: () -> Unit) {
        if (currentUserId == null || reason.isBlank() || reportedUserId.isBlank()) return

        val report = Report(
            reportedUserId = reportedUserId,
            reporterId = currentUserId!!,
            reason = reason
        )

        db.collection("reports").add(report).addOnSuccessListener {
            onSuccess()
        }
    }

    fun clearDataAndListeners() {
        _connections.value = emptyList()
    }
}
