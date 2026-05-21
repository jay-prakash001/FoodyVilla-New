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
data class ProductCatalog(
    val id: Long = 0,
    val created_at: String = "",
    val name: String = "",
    val description: String = "",

    val category_id: Long? = null,

    val is_veg: Boolean = true,
    val is_vegan: Boolean = false,
    val is_bestseller: Boolean = false,

    val nutritional_info: NutritionalInfo = NutritionalInfo(),

    val prep_time: String = "",

    val review: List<Review>? = emptyList()
)

@Serializable
data class OutletMenuItem(
    val id: Long = 0,
    val created_at: String = "",
    val outlet_id: Long = 0,
    val product_id: Long = 0,
    val image: List<String> = emptyList(),
    val price: Double = 0.0,
    val discount: Long = 0,
    val is_available: Boolean = true,
    val is_out_of_stock: Boolean = false,
    val rating: Float = 0f,
    val reviews_count: Int = 0,
    val product_catalog: ProductCatalog? = null
)

@Serializable
data class Outlet(
    val id: Long = 0,
    val created_at: String = "",
    val name: String = "",
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val logo_url: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radius_km: Double = 5.0,
    val is_active: Boolean = true,
    val opens_at: String? = null,
    val closes_at: String? = null,
    val fcm_tokens: List<String>? = emptyList(),
    val banner_url: String? = null,
    val razor_pay_key: String? = "rzp_test_ShBw7mlCM6gT6y",
    val outlet_menu_items: List<OutletMenuItem>? = emptyList()
)

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val emoji: String
)

@Serializable
data class Review(
    val id: Long = 0,
    val created_at: String = "",
    val user_id: Long? = null,
    val product_id: Long? = null,
    val user_name: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val img_url: List<String> = emptyList()
)

// Legacy compatibility or replacement
@Serializable
data class FoodItem(
    val id: Int = 0,
    val createdAt: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discount: Int = 0,
    val image: List<String> = emptyList(),
    val category: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val prepTime: String = "",
    val review: List<Review> = emptyList(),
    val nutritionalInfo: NutritionalInfo = NutritionalInfo(),
    val isVeg: Boolean = true,
    val isVegan: Boolean = false,
    val isBestSeller: Boolean = false,
    val outlet_id: Long? = null
)
