package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

data class ReviewRequest(
    val customerName: String, // Maps to 'title' in DB or used locally
    val rating: Int,
    val desc: String, // Maps to 'description' in DB
    val imageUrls: List<String>,
    val reviewType: String = "product",
    val orderId: String? = null,
    val menuItemId: Long? = null,
    val outletId: Long? = null
)
