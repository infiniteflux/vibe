package com.infiniteflux.login_using_firebase

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "!!! MyApplication onCreate CALLED !!!")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // THIS 'IF' STATEMENT IS THE CRITICAL FIX
        // It ensures this code only runs on Android 8.0 (Oreo) and newer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for Private Messages
            val privateMessagesChannel = NotificationChannel(
                "private_message",
                "Private Messages", // Changed back to plural for better UI in settings
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for direct messages."
            }

            // Channel for Group Messages
            val groupMessagesChannel = NotificationChannel(
                "group_message",
                "Group Messages", // Changed back to plural for better UI
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages in groups."
            }

            // Channel for New Connections
            val newConnectionsChannel = NotificationChannel(
                "new_connection",
                "New Connections", // Changed back to plural for better UI
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new friend connections or matches."
            }

            // Channel for New Events
            val newEventsChannel = NotificationChannel(
                "new_event",
                "New Events", // Changed back to plural for better UI
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new events and announcements."
            }

            // Register all the channels with the system
            notificationManager.createNotificationChannel(privateMessagesChannel)
            notificationManager.createNotificationChannel(groupMessagesChannel)
            notificationManager.createNotificationChannel(newConnectionsChannel)
            notificationManager.createNotificationChannel(newEventsChannel)
        }
    }
}

