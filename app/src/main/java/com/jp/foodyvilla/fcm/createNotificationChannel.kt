package com.jp.foodyvilla.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

fun createNotificationChannel(context: Context) {

    val channel = NotificationChannel(
        "foodyvilla_channel",
        "FoodyVilla Notifications",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Food offers and order updates"
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}