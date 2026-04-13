package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewInsertDto(
    val title: String,
    val desc: String,
    val rating: Int,
    val img_url: List<String>
)