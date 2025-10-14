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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val privateMessagesChannel = NotificationChannel(
                "private_message",
                "Private Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for direct messages."
            }

            val groupMessagesChannel = NotificationChannel(
                "group_message",
                "Group Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages in groups."
            }

            val newConnectionsChannel = NotificationChannel(
                "new_connection",
                "New Connections",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new friend connections or matches."
            }

            val newEventsChannel = NotificationChannel(
                "new_event",
                "New Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new events and announcements."
            }

            notificationManager.createNotificationChannel(privateMessagesChannel)
            notificationManager.createNotificationChannel(groupMessagesChannel)
            notificationManager.createNotificationChannel(newConnectionsChannel)
            notificationManager.createNotificationChannel(newEventsChannel)
        }
    }
}

