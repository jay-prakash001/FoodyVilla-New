package com.jp.foodyvilla.data.repo

import android.content.Context
import android.net.Uri
import com.jp.foodyvilla.data.model.Review
import com.jp.foodyvilla.data.model.ReviewInsertDto
import com.jp.foodyvilla.data.model.ReviewRequest
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.data.utils.compressImage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ReviewRepository(
    private val supabase: SupabaseClient
) {

    private suspend fun getCustomerId(): Long? {
        val authId = supabase.auth.currentUserOrNull()?.id ?: return null
        val user = supabase.postgrest["users"]
            .select { filter { eq("auth_user_id", authId) } }
            .decodeSingleOrNull<UserProfile>()
        return user?.id?.toLong()
    }

    // ----------------------
    // GET REVIEWS
    // ----------------------
    fun getReviews(): Flow<List<Review>> = flow {
        val result = supabase.from("reviews")
            .select() {
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Review>()

        emit(result)
    }

    fun getProductReviews(productId: Long): Flow<List<Review>> = flow {
        try {
            val res = supabase.from("reviews")
                .select {
                    filter {
                        eq("menu_item_id", productId)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<Review>()
            emit(res)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // ----------------------
    // UPLOAD IMAGES
    // ----------------------
    suspend fun uploadImages(
        context: Context,
        uris: List<Uri>
    ): List<String> {
        return uris.map { uri ->
            val compressed = compressImage(context, uri)
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "reviews/$fileName"

            supabase.storage.from("review").upload(
                path = path,
                data = compressed
            )

            supabase.storage.from("review")
                .publicUrl(path)
        }
    }

    // ----------------------
    // INSERT REVIEW
    // ----------------------
    suspend fun insertReview(request: ReviewRequest) {
        val customerId = getCustomerId() ?: throw Exception("User not found")

        val dto = ReviewInsertDto(
            customer_id = customerId,
            review_type = request.reviewType,
            order_id = request.orderId,
            menu_item_id = request.menuItemId,
            outlet_id = request.outletId,
            rating = request.rating,
            title = request.customerName,
            description = request.desc,
            img_url = request.imageUrls
        )

        supabase.from("reviews").insert(dto)
    }
}
