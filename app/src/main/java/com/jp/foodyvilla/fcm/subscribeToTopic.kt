package com.jp.foodyvilla.fcm

import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

fun subscribeToTopic(topic: String) {
    FirebaseMessaging.getInstance()
        .subscribeToTopic(topic)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
            }
        }
}