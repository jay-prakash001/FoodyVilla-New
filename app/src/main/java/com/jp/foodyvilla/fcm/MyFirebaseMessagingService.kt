package com.jp.foodyvilla.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jp.foodyvilla.data.repo.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val userRepository: UserRepository by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepository.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to update token", e)
            }
        }

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