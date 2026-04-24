package com.jp.foodyvilla.data.model.fcm

import kotlinx.serialization.Serializable

@Serializable
data class UserFcmUpdate(
    val fcm_token: String
)