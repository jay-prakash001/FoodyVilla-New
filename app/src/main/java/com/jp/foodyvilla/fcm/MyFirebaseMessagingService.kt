package com.jp.foodyvilla.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jp.foodyvilla.MainActivity
import com.jp.foodyvilla.R
import com.jp.foodyvilla.data.repo.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.net.URL

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

        Log.d("FCM", "Message received: $remoteMessage")

        val title = remoteMessage.data["title"] ?: "FoodyVilla"
        val body = remoteMessage.data["message"] ?: ""
        val imageUrl = remoteMessage.data["imageUrl"]
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        showNotification(title, body, imageUrl)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        title: String,
        message: String,
        imageUrl: String?
    ) {

        val channelId = "foodyvilla_channel"

        val intent = Intent(this, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "FoodyVilla Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Load image if available
        if (!imageUrl.isNullOrEmpty()) {

            try {

                val bitmap = BitmapFactory.decodeStream(
                    URL(imageUrl).openConnection().getInputStream()
                )

                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                )

                builder.setLargeIcon(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        NotificationManagerCompat.from(this)
            .notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
    }
}