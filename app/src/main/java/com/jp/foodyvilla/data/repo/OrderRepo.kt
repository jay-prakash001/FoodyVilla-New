package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.data.model.order.OrderInsert
import com.jp.foodyvilla.data.model.order.OrderItem
import com.jp.foodyvilla.data.model.order.OrderItemInsert
import com.jp.foodyvilla.data.model.order.OrderModel
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class OrderRepository(
    private val supabase: SupabaseClient
) {

    // 🔐 Get customer_id from token
    private suspend fun getCustomerId(): Int? {
        val authId = supabase.auth.currentUserOrNull()?.id ?: return null

        val user = supabase.postgrest["users"]
            .select {
                filter { eq("auth_user_id", authId) }
            }
            .decodeSingleOrNull<UserProfile>()

        return user?.id
    }

    // 🚀 PLACE ORDER
    fun placeOrder(
        cartItems: List<CartItem>,
        address: String,
        phone: String,
        customerName: String,
        instruction: String? = null,
        lat: Double? = null,
        long: Double? = null,
        orderType: String? = null
    ): Flow<UiState<String>> = flow {

        emit(UiState.Loading)

        try {
            val customerId = getCustomerId()
                ?: throw Exception("User not found")

            // 1️⃣ Insert Order — typed class, no mapOf()
            val order = supabase.postgrest["orders"]
                .insert(
                    OrderInsert(
                        customer_id = customerId,
                        status = "PLACED",
                        address = address,
                        phone = phone,
                        customer_name = customerName,
                        order_type = orderType,
                        instruction = instruction,
                        delivery_lat = lat,
                        delivery_long = long
                    )
                ){select()}
                .decodeSingle<OrderModel>()

            // 2️⃣ Insert Order Items — typed class, no mapOf()
            val orderItems = cartItems.map {
                OrderItemInsert(
                    order_id = order.id,
                    productid = it.product_id,
                    qty = it.qty ?: 1,
                    price_per_item = it.products?.price ?: 0.0,
                    total_price = (it.products?.price ?: 0.0) * (it.qty ?: 1),
                    total_discount = 0.0
                )
            }

            supabase.postgrest["order_items"].insert(orderItems)

            // 3️⃣ Clear Cart
            supabase.postgrest["cart"]
                .delete {
                    filter { eq("customer_id", customerId) }
                }

            emit(UiState.Success(order.id))

        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Order failed")))
        }
    }

    // ⚡ REALTIME ORDERS — updated for supabase-kt 3.x
    fun observeOrders(): Flow<List<OrderModel>> = callbackFlow {

        val authId = supabase.auth.currentUserOrNull()?.id
        if (authId == null) { trySend(emptyList()); close(); return@callbackFlow }

        val user = supabase.postgrest["users"]
            .select { filter { eq("auth_user_id", authId) } }
            .decodeSingleOrNull<UserProfile>()

        val customerId = user?.id
        if (customerId == null) { trySend(emptyList()); close(); return@callbackFlow }

        // ✅ 3.x: use supabase.realtime.channel()
        val channel = supabase.realtime.channel("orders-channel")

        // ✅ 3.x: postgresChangeFlow BEFORE subscribe()
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "orders"
        }.onEach {
            val orders = supabase.postgrest["orders"]
                .select {
                    filter { eq("customer_id", customerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<OrderModel>()
            trySend(orders)
        }.launchIn(this)

        // ✅ 3.x: subscribe AFTER setting up flows
        channel.subscribe()

        // Initial load
        val initial = supabase.postgrest["orders"]
            .select {
                filter { eq("customer_id", customerId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<OrderModel>()
        trySend(initial)
        awaitClose {
            launch {
                channel.unsubscribe()
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    // ⚡ REALTIME ORDER ITEMS — updated for supabase-kt 3.x
    fun observeOrderItems(orderId: String): Flow<List<OrderItem>> = callbackFlow {

        val channel = supabase.realtime.channel("order-items-$orderId")

        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "order_items"
        }.onEach {
            val items = supabase.postgrest["order_items"]
                .select { filter { eq("order_id", orderId) } }
                .decodeList<OrderItem>()
            trySend(items)
        }.launchIn(this)

        channel.subscribe()

        // Initial load
        val initial = supabase.postgrest["order_items"]
            .select { filter { eq("order_id", orderId) } }
            .decodeList<OrderItem>()
        trySend(initial)

        awaitClose {
            launch {
                channel.unsubscribe()
                supabase.realtime.removeChannel(channel)
            }
        }
    }
}