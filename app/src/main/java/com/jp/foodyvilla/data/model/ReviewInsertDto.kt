package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewInsertDto(
    val customer_id: Long,
    val review_type: String,
    val order_id: String? = null,
    val menu_item_id: Long? = null,
    val outlet_id: Long? = null,
    val rating: Int,
    val title: String? = null,
    val description: String? = null,
    val img_url: List<String>? = emptyList()
)
