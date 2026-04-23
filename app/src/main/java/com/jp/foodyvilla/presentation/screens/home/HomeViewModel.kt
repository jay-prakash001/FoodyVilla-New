package com.jp.foodyvilla.presentation.screens.home

import Banner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.FoodItem
import com.jp.foodyvilla.data.model.MockData
import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.data.repo.CartRepository
import com.jp.foodyvilla.data.repo.LocationRepository
import com.jp.foodyvilla.data.repo.OfferRepo
import com.jp.foodyvilla.data.repo.OrderRepository
import com.jp.foodyvilla.data.repo.ProductRepo
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val bestSellers: List<FoodItem>
        get() = allItems.filter { it.isBestSeller }

    val filteredItems: List<FoodItem>
        get() {
            val query = searchQuery.trim()

            return allItems.filter { item ->

                val matchesSearch =
                    query.isBlank() ||
                            item.name.contains(query, true) ||
                            item.description.contains(query, true) ||
                            item.category.contains(query, true) ||
                            item.prepTime.contains(query, true) ||
                            item.price.toString().contains(query) ||
                            item.rating.toString().contains(query) ||
                            (query.equals("veg", true) && item.isVeg) ||
                            (query.equals("vegan", true) && item.isVegan)

                val matchesCategory =
                    selectedCategory == "all" ||
                            item.category.equals(selectedCategory, true) ||
                            // 🔥 ALSO allow category match via search
                            item.name.contains(selectedCategory, true) ||
                            item.description.contains(selectedCategory, true)

                matchesSearch && matchesCategory
            }
        }

    val popularItems: List<FoodItem>
        get() = allItems.filter { !it.isBestSeller }
}

class HomeViewModel(private val offerRepo: OfferRepo, private val productRepo: ProductRepo, private val cartRepository: CartRepository, private val orderRepository: OrderRepository,private val locationRepository: LocationRepository) :
    ViewModel() {


    private val _selectedPage = MutableStateFlow(0)
    val selectedPage = _selectedPage.asStateFlow()
    fun updateSelectedPage(page: Int) {
        _selectedPage.value = page
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val fcm = FirebaseMessaging.getInstance()



    init {
        loadData()
        fcm.subscribeToTopic("offers")
        fcm.token
            .addOnSuccessListener { token ->
                println("FCM Token: $token")
            }
            .addOnFailureListener {
                println("Failed to get FCM token")
            }

    }

    fun getOrderedItems(){
        viewModelScope.launch {
            orderRepository.observeOrders().collectLatest {
                println("orders : $it")

            }

        }
    }

    fun getCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest{res->

                println(" cart items : $res")
                if(res is UiState.Success){
                    _uiState.value = _uiState.value.copy(cartItems = res.data)

                }
            }
        }
    }
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            offerRepo.getBanners().collect { items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        banners = items,
                        categories = MockData.categories
                    )
                }
            }

            getProduct()
            getCartItems()
            getOrderedItems()
        }
    }
    fun getCurrentLocation(): Flow<UiState<Pair<Double, Double>>> = flow {
        emit(UiState.Loading)

        try {
            val location = locationRepository.getCurrentLocation()

            if (location != null) {
                emit(UiState.Success(location))
            } else {
                emit(UiState.Error(Exception("Location not available")))
            }

        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }


    // ─────────────────────────────────────────
// 🚀 PLACE ORDER
// ─────────────────────────────────────────
    fun placeOrder(
        address: String,
        phone: String,
        customerName: String,
        instruction: String? = null,
        orderType : String?= null,
        lat: Double? = null,
        long: Double? = null,
    ) {
        val cartItems = _uiState.value.cartItems

        if (cartItems.isEmpty()) {
//            _uiState.update { it.copy(orderError = "Cart is empty") }
            return
        }

        viewModelScope.launch {

            getCurrentLocation().collectLatest {
                println("Location $it")
                if(it is UiState.Success){
                    orderRepository.placeOrder(
                        cartItems = cartItems,
                        address = address,
                        phone = phone,
                        customerName = customerName,
                        instruction = instruction,
                        orderType = orderType,
                        lat = it.data.first,
                        long = it.data.second
                    ).collectLatest { state ->

                        println("Order Place State $state")
//                when (state) {
//                    is UiState.Loading -> {
//                        _uiState.update {
//                            it.copy(
//                                isPlacingOrder = true,
//                                orderError = null,
//                                orderSuccess = null
//                            )
//                        }
//                    }
//
//                    is UiState.Success -> {
//                        _uiState.update {
//                            it.copy(
//                                isPlacingOrder = false,
//                                orderSuccess = state.data,  // orderId
//                                cartItems = emptyList()     // clear cart locally
//                            )
//                        }
//                    }
//
//                    is UiState.Error -> {
//                        _uiState.update {
//                            it.copy(
////                                isPlacingOrder = false,
////                                orderError = state.exception.message ?: "Order failed"
//                            )
//                        }
//                    }

                    }

                }
            }
        }
    }
    // ─────────────────────────────────────────
// 🧹 CLEAR ORDER STATE (call after showing snackbar/dialog)
// ─────────────────────────────────────────
    fun clearOrderState() {
//        _uiState.update {
////            it.copy(
////                orderSuccess = null,
////                orderError = null
////            )
//        }
    }

    fun updateCartItemQuantity(item: FoodItem, quantity: Int = 1) {
        if (quantity == 0) {
            _uiState.update { it ->
                it.copy(cartItems = it.cartItems.filter { it.products?.id != item.id })
            }
        }else{
            _uiState.update {
                it.copy(cartItems = it.cartItems.filter { it.products?.id != item.id } + CartItem(
                    id = item.id,
                    products = item,
                    qty = quantity,
                    customer_id =  0,
                    product_id =item.id
                ))
            }
        }

        viewModelScope.launch {
            cartRepository.addToCart(item.id, quantity).collectLatest {
                println("update cart $it")
            }
        }


    }

    fun getTotalCartValue(): Double {
        return uiState.value.cartItems.sumOf { it.totalPrice ?: 0.0 }
    }

    fun removeFromCart(itemId: Int) {
        viewModelScope.launch {
            cartRepository.addToCart(itemId, 0).collectLatest { res->
                println("update cart $res")
                if(res is UiState.Success){
                    _uiState.update {
                        it.copy(cartItems = it.cartItems.filter { it.products?.id != itemId })
                    }
                }
            }
        }


    }


    fun generateWhatsAppMessage(
        state: HomeUiState,
        name: String,
        phone: String,
        address: String,
        type: String,
        instructions: String
    ): String {

//        val orderId = System.currentTimeMillis().toString().takeLast(6)
//
//        val itemsText = state.cartItems.joinToString("\n") {
//            "${it.foodItem.name} x${it.quantity} = ₹${"%.2f".format(it.foodItem.price * it.quantity)}"
//        }
//
//        val total = state.cartItems.sumOf {
//            it.foodItem.price * it.quantity
//        }

        return """
🧾 *New Order*

📦 Order ID: orderId
👤 Name: $name
📞 Phone: $phone
🍽 Type: $type

📍 Address: ${if (type == "Delivery") address else "N/A"}

🛒 *Items:*
itemsText

💰 *Total: *

📝 Instructions:
$instructions
""".trimIndent()
    }
    fun getProduct() {
        viewModelScope.launch {
            try {
                productRepo.getProducts().collect { items ->

                    println("Items : $items")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allItems = items
                        )
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
