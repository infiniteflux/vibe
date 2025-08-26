package com.infiniteflux.login_using_firebase.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.ReportedUser
import com.infiniteflux.login_using_firebase.data.Report
import com.infiniteflux.login_using_firebase.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

enum class WarningLevel(val text: String, val color: Color) {
    DANGER("Danger", Color(0xFFE91E63)),
    WARNING("Warning", Color(0xFFFFA000))
}


class WallOfShameViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _reportedUsers = MutableStateFlow<List<ReportedUser>>(emptyList())
    val reportedUsers: StateFlow<List<ReportedUser>> = _reportedUsers

    private var bannedUsersListener: ListenerRegistration? = null

    fun initializeData() {
        fetchWallOfShame()
    }

    private fun fetchWallOfShame() {
        bannedUsersListener?.remove()
        bannedUsersListener = db.collection("users")
            .whereEqualTo("isBanned", true)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener

                viewModelScope.launch {
                    val bannedUserProfiles = snapshots.toObjects(User::class.java)

                    val reportedUsersList = bannedUserProfiles.mapNotNull { user ->
                        val reports = db.collection("reports")
                            .whereEqualTo("reportedUserId", user.id)
                            .get().await().toObjects(Report::class.java)

                        if (reports.isNotEmpty()) {
                            val totalReports = reports.size
                            val verifiedReports = reports.count { it.verified }
                            val lastIncidentTimestamp = reports.maxOfOrNull { it.timestamp!! }?.toDate()?.time ?: 0
                            val daysSinceLastIncident = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastIncidentTimestamp)
                            val reasons = reports.map { it.reason }.distinct()
                            val warningLevel = if (verifiedReports >= 3) WarningLevel.DANGER else WarningLevel.WARNING

                            ReportedUser(
                                id = user.id,
                                name = user.name,
                                university = user.university,
                                totalReports = totalReports,
                                verifiedReports = verifiedReports,
                                lastIncidentDays = "${daysSinceLastIncident}d",
                                reportedFor = reasons,
                                avatarUrl = user.avatarUrl,
                                warningLevel = warningLevel
                            )
                        } else {
                            null
                        }
                    }
                    _reportedUsers.value = reportedUsersList
                }
            }
    }

    fun clearDataAndListeners() {
        bannedUsersListener?.remove()
        _reportedUsers.value = emptyList()
    }
}
