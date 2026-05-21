package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Banner(
    val id: Long = 0,
    val created_at: String = "",
    val outlet_id: Long? = null,
    val title: String? = null,
    val img_url: String? = null,
    val display_order: Int = 0
)
