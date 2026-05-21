package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OfferFood(
    val id: String,
    val created_at: String = "",
    val outlet_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val linked_url: String? = null,
    val expires_at: String? = null
)
