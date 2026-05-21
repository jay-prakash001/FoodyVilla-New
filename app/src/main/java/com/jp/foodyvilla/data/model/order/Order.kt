package com.jp.foodyvilla.data.model.order

import com.jp.foodyvilla.data.model.Outlet
import kotlinx.serialization.Serializable

@Serializable
data class OrderModel(
    val id: String,
    val created_at: String = "",
    val outlet_id: Long = 0,
    val customer_id: Long? = null,
    val customer_name: String? = null,
    val phone: String? = null,
    val status: String = "pending",
    val order_type: String? = null,
    val address: String? = null,
    val delivery_lat: Double? = null,
    val delivery_long: Double? = null,
    val instruction: String? = null,
    val transaction_id: String? = null,
    val order_items: List<OrderItem> = emptyList(),
    val outlets: Outlet? = null
)

@Serializable
data class OrderInsert(
    val outlet_id: Long,
    val customer_id: Long,
    val customer_name: String,
    val phone: String,
    val status: String = "pending",
    val order_type: String? = null,
    val address: String? = null,
    val delivery_lat: Double? = null,
    val delivery_long: Double? = null,
    val instruction: String? = null,
    val transaction_id: String? = null
)

@Serializable
data class PaymentInsert(
    val order_id: String,
    val customer_id: Long,
    val razorpay_order_id: String?,
    val razorpay_payment_id: String?,
    val razorpay_signature: String?,
    val amount: Long,
    val amount_due: Long? = 0,
    val amount_refunded: Long? = 0,
    val currency: String = "INR",
    val payment_status: String,
    val payment_method: String? = null,
    val razorpay_response: String? = null, // Store as stringified JSON if needed
    val refund_id: String? = null,
    val refund_reason: String? = null,
    val refunded_at: String? = null,
    val error_code: String? = null,
    val error_description: String? = null
)
