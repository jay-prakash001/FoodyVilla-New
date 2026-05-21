package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.data.model.cart.CartRequest
import com.jp.foodyvilla.data.model.cart.CartUpdate
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CartRepository(
    private val supabase: SupabaseClient
)
{

    // 🔐 Get auth user id from token
    private suspend fun getAuthUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }

    // 🧩 Map auth user → users table → customer_id
    private suspend fun getCustomerId(): Long? {

        val authId = getAuthUserId() ?: return null

        val user = supabase.postgrest["users"]
            .select {
                filter {
                    eq("auth_user_id", authId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        return user?.id?.toLong()
    }

    // ➕ Add to cart
    fun addToCart(menuItemId: Long, outletId: Long, qty: Int = 1): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            println("Adding to cart: customerId=$customerId, menuItemId=$menuItemId, outletId=$outletId, qty=$qty")

            val existing = supabase.postgrest["cart"]
                .select {
                    filter {
                        eq("customer_id", customerId)
                        eq("menu_item_id", menuItemId)
                        eq("outlet_id", outletId)
                    }
                }
                .decodeList<CartItem>()

            if (existing.isEmpty()) {
                if (qty > 0) {
                    supabase.postgrest["cart"].insert(
                        CartRequest(
                            customer_id = customerId,
                            menu_item_id = menuItemId,
                            outlet_id = outletId,
                            qty = qty
                        )
                    )
                    println("Inserted new cart item")
                }
            } else {
                val cartItem = existing.first()
                if (qty <= 0) {
                    supabase.postgrest["cart"]
                        .delete {
                            filter {
                                eq("id", cartItem.id)
                            }
                        }
                    println("Deleted cart item")
                } else {
                    supabase.postgrest["cart"]
                        .update(
                            CartUpdate(qty = qty)
                        ) {
                            filter {
                                eq("id", cartItem.id)
                            }
                        }
                    println("Updated cart item qty to $qty")
                }
            }

            emit(UiState.Success("Cart updated"))

        } catch (e: Exception) {
            e.printStackTrace()
            println("Cart Update Error: ${e.message}")
            emit(UiState.Error(Exception(e.message ?: "Unknown error")))
        }
    }

    // 📥 Get cart items
    fun getCartItems(): Flow<UiState<List<CartItem>>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            val items = supabase.postgrest["cart"]
                .select(
                    Columns.raw(
                        """
                        *,
                        outlet_menu_items (*, product_catalog (*)),
                        outlets (*)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("customer_id", customerId)
                    }
                }
                .decodeList<CartItem>()

            emit(UiState.Success(items))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Failed to load cart")))
        }
    }

    // ❌ Remove item
    fun removeFromCart(cartId: Long): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            supabase.postgrest["cart"]
                .delete {
                    filter {
                        eq("id", cartId)
                    }
                }

            emit(UiState.Success("Removed from cart"))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Delete failed")))
        }
    }

    // 🧹 Clear cart
    fun clearCart(): Flow<UiState<String>> = flow {
        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            supabase.postgrest["cart"]
                .delete {
                    filter {
                        eq("customer_id", customerId)
                    }
                }

            emit(UiState.Success("Cart cleared"))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Clear failed")))
        }
    }
}