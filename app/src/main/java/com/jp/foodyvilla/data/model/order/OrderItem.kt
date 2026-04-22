package com.jp.foodyvilla.data.model.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: Int? = null,
    val order_id: String,
    val productid: Int,
    val qty: Int,
    val price_per_item: Double,
    val total_price: Double,
    val total_discount: Double? = 0.0
)