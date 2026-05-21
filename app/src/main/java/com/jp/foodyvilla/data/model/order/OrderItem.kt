package com.jp.foodyvilla.data.model.order

import com.jp.foodyvilla.data.model.OutletMenuItem
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: Long? = null,
    val created_at: String? = null,
    val order_id: String,
    val menu_item_id: Long,
    val qty: Long,
    val price_per_item: Double,
    val total_price: Double,
    val total_discount: Double = 0.0,
    val outlet_menu_items: OutletMenuItem? = null
)

@Serializable
data class OrderItemInsert(
    val order_id: String,
    val menu_item_id: Long,
    val qty: Long,
    val price_per_item: Double,
    val total_price: Double,
    val total_discount: Double = 0.0
)
