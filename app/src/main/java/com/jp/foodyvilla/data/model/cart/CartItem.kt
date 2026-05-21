package com.jp.foodyvilla.data.model.cart

import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.data.model.Outlet
import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val id: Long = 0,
    val created_at: String? = null,
    val customer_id: Long,
    val outlet_id: Long,
    val menu_item_id: Long,
    val qty: Long = 1,
    val outlet_menu_items: OutletMenuItem? = null,
    val outlets: Outlet? = null
) {
    val totalPrice: Double get() = (outlet_menu_items?.price ?: 0.0) * qty
}

@Serializable
data class CartRequest(
    val customer_id: Long,
    val menu_item_id: Long,
    val outlet_id: Long,
    val qty: Int
)

@Serializable
data class CartUpdate(
    val qty: Int
)
