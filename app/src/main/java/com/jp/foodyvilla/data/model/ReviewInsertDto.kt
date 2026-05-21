package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewInsertDto(
    val user_name: String,
    val comment: String,
    val rating: Int,
    val img_url: List<String>,
    val product_id: Long? = null
)