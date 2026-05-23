package com.jp.foodyvilla.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.jp.foodyvilla.data.model.Banner
import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.MockData
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.data.model.order.OrderModel
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.data.repo.CartRepository
import com.jp.foodyvilla.data.repo.LocationRepository
import com.jp.foodyvilla.data.repo.OfferRepo
import com.jp.foodyvilla.data.repo.OrderRepository
import com.jp.foodyvilla.data.repo.ProductRepo
import com.jp.foodyvilla.presentation.utils.UiState
import com.jp.foodyvilla.presentation.utils.isOutletOpen
import kotlinx.coroutines.Job
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
    val outlets: List<Outlet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: Long = -1L,
    val selectedOutletId: Long = -1L,
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val allItems: List<OutletMenuItem>
        get() = outlets.flatMap { it.outlet_menu_items ?: emptyList() }

    val filteredItems: List<OutletMenuItem>
        get() {
            val query = searchQuery.trim()

            return allItems.filter { item ->
                val matchesSearch = query.isBlank() ||
                        item.product_catalog?.name?.contains(query, true) == true ||
                        item.product_catalog?.description?.contains(query, true) == true

                val matchesCategory = selectedCategory == -1L ||
                        item.product_catalog?.category_id == selectedCategory

                val matchesOutlet = selectedOutletId == -1L ||
                        item.outlet_id == selectedOutletId

                matchesSearch && matchesCategory && matchesOutlet
            }
        }
}

data class OrderUiState(
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val instructions: String = "",
    val orderType: String = "Delivery",
    val lat: Double = 0.0,
    val long: Double = 0.0
)

class HomeViewModel(
    private val offerRepo: OfferRepo,
    private val productRepo: ProductRepo,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _selectedPage = MutableStateFlow(0)
    val selectedPage = _selectedPage.asStateFlow()
    fun updateSelectedPage(page: Int) {
        _selectedPage.value = page
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _orderState = MutableStateFlow(OrderUiState())
    val orderState: StateFlow<OrderUiState> = _orderState

    fun updateCustomerName(value: String) {
        _orderState.update { it.copy(customerName = value) }
    }

    fun updatePhone(value: String) {
        val formattedPhone = if (value.length == 10 && value.all { it.isDigit() }) {
            "+91$value"
        } else {
            value
        }
        _orderState.update { it.copy(phone = formattedPhone) }
    }

    fun updateAddress(value: String) {
        _orderState.update { it.copy(address = value) }
    }

    fun updateInstructions(value: String) {
        _orderState.update { it.copy(instructions = value) }
    }

    fun updateOrderType(value: String) {
        _orderState.update { it.copy(orderType = value) }
    }

    fun updateCustomerDetailsForOrder(user: UserProfile) {
        viewModelScope.launch {
            _orderState.update {
                it.copy(
                    customerName = user.name ?: "",
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    lat = user.lat ?: 0.0,
                    long = user.long ?: 0.0
                )
            }
        }
    }

    private val _locationState = MutableStateFlow<UiState<Pair<Double, Double>>>(UiState.Idle)
    val locationState = _locationState.asStateFlow()

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = UiState.Loading
            val result = locationRepository.fetchLocation()
            result.onSuccess { location ->
                _locationState.value = UiState.Success(location)
                val addressResult = locationRepository.getAddressFromLocation(
                    latitude = location.first,
                    longitude = location.second
                )
                val address = addressResult.getOrNull() ?: ""
                _orderState.update { state ->
                    state.copy(address = address, lat = location.first, long = location.second)
                }
            }
            result.onFailure { exception ->
                _locationState.value = UiState.Error(Exception(exception))
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationRepository.hasLocationPermission()
    fun hasNotificationPermission(): Boolean = locationRepository.hasNotificationPermission()
    fun isGpsEnabled(): Boolean = locationRepository.isGpsEnabled()
    
    val fcm = FirebaseMessaging.getInstance()

    init {
        loadData()
        fcm.subscribeToTopic("offers")
        fcm.subscribeToTopic("banners")
    }

    private val _orderHistoryState = MutableStateFlow<UiState<List<OrderModel>>>(UiState.Idle)
    val orderHistoryState = _orderHistoryState.asStateFlow()
    private var orderListenerJob: Job? = null

    fun getOrderedItems() {
        if (orderListenerJob != null && orderListenerJob?.isActive == true) return
        orderListenerJob = viewModelScope.launch {
            orderRepository.observeOrders().collectLatest {
                _orderHistoryState.value = it
                if (it is UiState.Success) {
                    updatePurchasedProducts(it.data)
                }
            }
        }
    }

    fun getCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { res ->
                if (res is UiState.Success) {
                    _uiState.update { it.copy(cartItems = res.data) }
                    // Re-calculate orders that can be reviewed
                }
            }
        }
    }

    private val _purchasedProductIds = MutableStateFlow<Set<Long>>(emptySet())
    val purchasedProductIds = _purchasedProductIds.asStateFlow()

    private fun updatePurchasedProducts(orders: List<OrderModel>) {
        val ids = orders
            .filter { it.status.lowercase() == "delivered" || it.status.lowercase() == "completed" }
            .flatMap { it.order_items }
            .map { it.menu_item_id }
            .toSet()
        _purchasedProductIds.value = ids
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Launch parallel data loading
            launch {
                offerRepo.getBanners().collect { items ->
                    _uiState.update { it.copy(banners = items) }
                }
            }
            
            launch {
                productRepo.getCategories().collect { cats ->
                    // Add "All" category at the beginning
                    // Create a dummy category for "All" with ID -1
                    val allCategory = Category(id = -1L, name = "All", emoji = "🍽️")
                    _uiState.update { it.copy(categories = listOf(allCategory) + cats) }
                }
            }

            getOutlets()
            getCartItems()
        }
    }

    // 🚀 PLACE ORDER & PAYMENT
    private val _paymentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val paymentState = _paymentState.asStateFlow()
    private var pendingOutletId: Long? = null
    private var lastProcessedPaymentId: String? = null
    
    fun setPendingOutletId(id: Long) {
        pendingOutletId = id
        _paymentState.value = UiState.Idle
    }
    
    fun resetPaymentState() {
        _paymentState.value = UiState.Idle
    }

    fun onPaymentSuccess(
        razorpayPaymentId: String?,
        razorpayOrderId: String?,
        razorpaySignature: String?
    ) {
        if (razorpayPaymentId != null && razorpayPaymentId == lastProcessedPaymentId) return
        lastProcessedPaymentId = razorpayPaymentId

        viewModelScope.launch {
            _paymentState.value = UiState.Loading
            pendingOutletId?.let { outletId ->
                orderRepository.placeOrder(
                    outletId = outletId,
                    cartItems = uiState.value.cartItems,
                    address = _orderState.value.address,
                    phone = _orderState.value.phone,
                    customerName = _orderState.value.customerName,
                    instruction = _orderState.value.instructions,
                    orderType = _orderState.value.orderType,
                    transactionId = razorpayPaymentId,
                    lat = _orderState.value.lat,
                    long = _orderState.value.long
                ).collectLatest { state ->
                    when (state) {
                        is UiState.Success -> {
                            val orderId = state.data
                            val amount = uiState.value.cartItems
                                .filter { it.outlet_id == outletId }
                                .sumOf { it.totalPrice }
                                
                            orderRepository.savePayment(
                                orderId = orderId,
                                razorpayOrderId = razorpayOrderId,
                                razorpayPaymentId = razorpayPaymentId,
                                razorpaySignature = razorpaySignature,
                                amount = amount.toLong(),
                                status = "captured"
                            ).collectLatest { }

                            getCartItems()
                            _paymentState.value = UiState.Success("Order placed successfully")
                            getOrderedItems()
                        }
                        is UiState.Error -> {
                            _paymentState.value = state
                            lastProcessedPaymentId = null
                        }
                        else -> {}
                    }
                }
            }
            pendingOutletId = null
        }
    }

    fun onPaymentError(errorCode: Int, errorDescription: String) {
        _paymentState.value = UiState.Error(Exception(errorDescription), errorDescription)
        pendingOutletId = null
    }

    fun updateCartItemQuantity(item: OutletMenuItem, quantity: Int = 1) {
        if (quantity == 0) {
            _uiState.update { state ->
                state.copy(cartItems = state.cartItems.filter { it.menu_item_id != item.id })
            }
        } else {
            _uiState.update { state ->
                val exists = state.cartItems.any { it.menu_item_id == item.id }
                val updatedList = if (exists) {
                    state.cartItems.map {
                        if (it.menu_item_id == item.id) it.copy(qty = quantity.toLong()) else it
                    }
                } else {
                    val outlet = state.outlets.find { it.id == item.outlet_id }
                    state.cartItems + CartItem(
                        id = 0,
                        outlet_menu_items = item,
                        qty = quantity.toLong(),
                        customer_id = 0,
                        menu_item_id = item.id,
                        outlet_id = item.outlet_id,
                        outlets = outlet
                    )
                }
                state.copy(cartItems = updatedList)
            }
        }

        viewModelScope.launch {
            cartRepository.addToCart(item.id, item.outlet_id, quantity).collectLatest { }
        }
    }

    fun removeFromCart(menuItemId: Long) {
        viewModelScope.launch {
            val item = _uiState.value.cartItems.find { it.menu_item_id == menuItemId }
            if (item != null) {
                cartRepository.addToCart(menuItemId, item.outlet_id, 0).collectLatest { res ->
                    if (res is UiState.Success) {
                        _uiState.update {
                            it.copy(cartItems = it.cartItems.filter { it.menu_item_id != menuItemId })
                        }
                    }
                }
            }
        }
    }

    fun getOutlets() {
        viewModelScope.launch {
            try {
                productRepo.getOutletsWithMenu().collect { items ->
                    _uiState.update { it.copy(isLoading = false, outlets = items) }
                }
            } catch (e: Exception) { }
        }
    }

    fun cancelOrder(order: OrderModel) {
        viewModelScope.launch {
            val productNames = order.order_items.map { 
                "${it.outlet_menu_items?.product_catalog?.name} x ${it.qty}" 
            }
            val imageUrl = order.order_items.firstOrNull()?.outlet_menu_items?.image?.firstOrNull()
            
            orderRepository.cancelOrder(
                orderId = order.id,
                outletId = order.outlet_id,
                customerName = order.customer_name ?: "Customer",
                productNames = productNames,
                imageUrl = imageUrl
            ).collectLatest { state ->
                if (state is UiState.Success) {
                    getOrderedItems()
                }
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
    }

    fun selectOutlet(outletId: Long) {
        _uiState.update { it.copy(selectedOutletId = outletId) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
