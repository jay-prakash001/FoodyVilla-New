package com.jp.foodyvilla.data.model

data class ReviewRequest(
    val customerName: String,
    val rating: Int,
    val desc: String,
    val imageUrls: List<String>
)