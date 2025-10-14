package com.infiniteflux.login_using_firebase.cloud

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.infiniteflux.login_using_firebase.R
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userDocRef = Firebase.firestore.collection("users").document(currentUser.uid)
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                  // Log success
                }
                .addOnFailureListener {
                    // Log failure or retry
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            val title = notification.title
            val body = notification.body
            val channelId = notification.channelId ?: "default_channel_id"

            if (title != null && body != null) {
                showNotification(title, body, channelId)
            }
        }
    }

    private fun showNotification(title: String, message: String, channelId: String) {

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(Random.nextInt(), builder.build())
        }
    }
}
