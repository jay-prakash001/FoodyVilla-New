package com.jp.foodyvilla.data.model.fcm

import kotlinx.serialization.Serializable

@Serializable
data class NotifyOutletRequest(
    val outletId: Long,
    val title: String,
    val description: List<String>,
    val imageUrl: String? = null
)
