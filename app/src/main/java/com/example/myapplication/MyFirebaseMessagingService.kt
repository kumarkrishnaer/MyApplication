package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM_RECEIVED", "Message received")
        Log.d("FCM_RECEIVED", "Data = ${remoteMessage.data}")
        Log.d("FCM_RECEIVED", "Title = ${remoteMessage.notification?.title}")
        Log.d("FCM_RECEIVED", "Body = ${remoteMessage.notification?.body}")

        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "New Message"

        val message = remoteMessage.data["message"]
            ?: remoteMessage.notification?.body
            ?: "FCM received"

        showNotification(title, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New token: $token")
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "admin_channel"

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Admin Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}