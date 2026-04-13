package com.jp.foodyvilla.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NutritionalInfo(
    val protein: String = "",
    val energy: String = "",
    val carbs: String = "",
    val fat: String = ""
)
@Serializable
data class FoodItem(
    val id: Int = 0,
    val createdAt: String = "",           // timestamptz
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discount: Int = 0,                // int4
    val image: List<String> = emptyList(), // _jsonb (array)
    val category: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val prepTime: String = "",
    val review: List<Review> = emptyList(), // _jsonb (array)
    val nutritionalInfo: NutritionalInfo = NutritionalInfo(),
    val isVeg: Boolean = true,
    val isVegan: Boolean = false,
    val isBestSeller: Boolean = false
)



@Serializable
data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    val quantity: Int = 1
) {
    val totalPrice: Double get() = foodItem.price * quantity
}

@Serializable
data class Category(
    val id: String,
    val name: String,
    val emoji: String
)

enum class OrderStatus { PLACED, PREPARING, ON_THE_WAY, DELIVERED }

@Serializable
data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: String = OrderStatus.PLACED.name,
    val createdAt: String = "",
    val deliveryAddress: String = ""
)

@Serializable
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoURL: String = "",
    val phoneNumber: String = "",
    val address: String = ""
)

@Serializable
data class Review(
    val id: Int = 0,
    val userName: String = "",
    val title: String = "",
    val rating: Double = 0.0,
    val desc: String = "",
    val date: String = "",
    val foodItem: String = "",
    val img_url: List<String> = emptyList() // ✅ NEW
)

@Serializable
data class Offer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val discount: String = "",
    val code: String = "",
    val validUntil: String = "",
    val imageUrl: String = "",
    val color: Long = 0xFF_E53935
)
