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

    // This is called when a new FCM token is generated or refreshed.
    // We save this token to the user's profile in Firestore.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userDocRef = Firebase.firestore.collection("users").document(currentUser.uid)
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                    // Optional: Log success
                }
                .addOnFailureListener {
                    // Optional: Log failure or retry
                }
        }
    }

    // This is called when a message is received while the app is in the foreground.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            val title = notification.title
            val body = notification.body
            // Key Change: Read the channel ID from the incoming notification payload.
            // Fallback to a default ID if it's somehow missing.
            val channelId = notification.channelId ?: "default_channel_id"

            if (title != null && body != null) {
                showNotification(title, body, channelId)
            }
        }
    }

    private fun showNotification(title: String, message: String, channelId: String) {
        // IMPORTANT: This function now ASSUMES the channel with the given ID
        // has already been created in your Application class at app startup.

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon) // Replace with your icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Use high priority for chat/social apps
            .setAutoCancel(true) // Dismisses the notification when tapped

        with(NotificationManagerCompat.from(this)) {
            // Permission check for Android 13 (API 33) and above
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // If permission is not granted, you cannot post the notification.
                // You should request this permission from the user in your UI.
                return
            }
            // Use a random integer for the notification ID to show multiple notifications
            notify(Random.nextInt(), builder.build())
        }
    }
}
