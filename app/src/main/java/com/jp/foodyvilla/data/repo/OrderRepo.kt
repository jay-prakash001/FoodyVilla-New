package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.data.model.order.*
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import com.jp.foodyvilla.data.model.fcm.NotifyOutletRequest
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.*

class OrderRepository(
    private val supabase: SupabaseClient
) {

    private suspend fun getCustomerId(): Long? {
        val authId = supabase.auth.currentUserOrNull()?.id ?: return null
        val user = supabase.postgrest["users"]
            .select { filter { eq("auth_user_id", authId) } }
            .decodeSingleOrNull<UserProfile>()
        return user?.id?.toLong()
    }

    fun placeOrder(
        outletId: Long,
        cartItems: List<CartItem>,
        address: String,
        phone: String,
        customerName: String,
        instruction: String? = null,
        lat: Double? = null,
        long: Double? = null,
        orderType: String? = null,
        transactionId: String? = null
    ): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val customerId = getCustomerId() ?: throw Exception("User not found")

            // 1. Create Order
            val orderInsertData = OrderInsert(
                outlet_id = outletId,
                customer_id = customerId,
                customer_name = customerName,
                phone = phone,
                status = "pending",
                order_type = orderType?.lowercase(),
                address = address,
                delivery_lat = lat,
                delivery_long = long,
                instruction = instruction,
                transaction_id = transactionId
            )

            val order = supabase.postgrest["orders"]
                .insert(orderInsertData) { select() }
                .decodeSingle<OrderModel>()

            // 2. Create Order Items
            val orderItems = cartItems.filter { it.outlet_id == outletId }.map {
                OrderItemInsert(
                    order_id = order.id,
                    menu_item_id = it.menu_item_id,
                    qty = it.qty,
                    price_per_item = it.outlet_menu_items?.price ?: 0.0,
                    total_price = it.totalPrice
                )
            }

            supabase.postgrest["order_items"].insert(orderItems)

            // 3. Clear Cart for this outlet
            supabase.postgrest["cart"].delete {
                filter {
                    eq("customer_id", customerId)
                    eq("outlet_id", outletId)
                }
            }

            emit(UiState.Success(order.id))

            // 4. Notify Outlet
            try {
                val notifyRequest = NotifyOutletRequest(
                    outletId = outletId,
                    title = "New Order Received from $customerName",
                    description = cartItems.filter { it.outlet_id == outletId }.map {
                        "${it.outlet_menu_items?.product_catalog?.name} x ${it.qty}"
                    },
                    imageUrl = cartItems.firstOrNull { it.outlet_id == outletId }?.outlet_menu_items?.image?.firstOrNull()
                )
                supabase.functions.invoke("notify_outlet", notifyRequest)
            } catch (e: Exception) {
                // Don't fail the whole order if notification fails
            }
        } catch (e: Exception) {
            emit(UiState.Error(Exception(e.message ?: "Order failed")))
        }
    }

    fun savePayment(
        orderId: String,
        razorpayOrderId: String?,
        razorpayPaymentId: String?,
        razorpaySignature: String?,
        amount: Long,
        status: String,
        method: String? = null,
        errorCode: String? = null,
        errorDescription: String? = null,
        razorpayResponse: String? = null
    ): Flow<UiState<Boolean>> = flow {
        try {
            val customerId = getCustomerId() ?: throw Exception("User not found")
            val paymentData = PaymentInsert(
                order_id = orderId,
                customer_id = customerId,
                razorpay_order_id = razorpayOrderId,
                razorpay_payment_id = razorpayPaymentId,
                razorpay_signature = razorpaySignature,
                amount = amount,
                amount_due = 0,
                amount_refunded = 0,
                payment_status = status,
                payment_method = method,
                currency = "INR",
                error_code = errorCode,
                error_description = errorDescription,
                razorpay_response = razorpayResponse
            )

            supabase.postgrest["payments"].insert(paymentData)
            emit(UiState.Success(true))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    fun cancelOrder(orderId: String, outletId: Long, customerName: String, productNames: List<String>, imageUrl: String?): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            supabase.postgrest["orders"]
                .update(mapOf("status" to "cancelled")) {
                    filter { eq("id", orderId) }
                }
            emit(UiState.Success("Order cancelled"))

            // Notify Outlet of Cancellation
            try {
                val notifyRequest = NotifyOutletRequest(
                    outletId = outletId,
                    title = "Order Cancelled by $customerName",
                    description = productNames,
                    imageUrl = imageUrl
                )
                supabase.functions.invoke("notify_outlet", notifyRequest)
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    fun observeOrders(): Flow<UiState<List<OrderModel>>> = flow {
        val customerId = getCustomerId() ?: return@flow
        emit(UiState.Loading)

        // 1. Initial fetch
        val initialOrders = try {
            supabase.postgrest["orders"]
                .select(Columns.raw("*, order_items(*, outlet_menu_items(*, product_catalog(*))), outlets(*)")) {
                    filter { eq("customer_id", customerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<OrderModel>()
        } catch (e: Exception) {
            emit(UiState.Error(e))
            return@flow
        }
        emit(UiState.Success(initialOrders))

        // 2. Realtime listener
        val channel = supabase.realtime.channel("orders_channel")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "orders"
        }

        channel.subscribe()

        // Combine initial fetch with real-time changes
        changeFlow.collect { action ->
            try {
                val updatedOrders = supabase.postgrest["orders"]
                    .select(Columns.raw("*, order_items(*, outlet_menu_items(*, product_catalog(*))), outlets(*)")) {
                        filter { eq("customer_id", customerId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<OrderModel>()
                emit(UiState.Success(updatedOrders))
            } catch (e: Exception) {
                // Ignore errors during re-fetch to keep the stream alive
            }
        }
    }
}
