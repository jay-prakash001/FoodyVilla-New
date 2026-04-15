package com.jp.foodyvilla.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage



class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("Notification Received $remoteMessage")
        remoteMessage.notification?.let {
            Log.d("FCM", "Message: ${it.body}")
        }

        val title = remoteMessage.data["title"] ?: ""
        val body = remoteMessage.data["message"] ?: ""
        val imageUrl = remoteMessage.data["imageUrl"]

//        val db = DatabaseProvider.getDatabase(applicationContext)

//        CoroutineScope(Dispatchers.IO).launch {
//
//            db.notificationDao().insertNotification(
//                NotificationEntity(
//                    title = title,
//                    message = body,
//                    imageUrl = imageUrl
//                )
//            )
//        }
    }
}