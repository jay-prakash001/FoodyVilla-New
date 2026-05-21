package com.jp.foodyvilla.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.model.Review
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.data.repo.CartRepository
import com.jp.foodyvilla.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val item: OutletMenuItem? = null,
    val quantity: Int = 1,
    val isWishlisted: Boolean = false,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val productRepo: ProductRepo,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews = _reviews.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            productRepo.getProductById(itemId).collect { item ->
                _uiState.update { it.copy(isLoading = false, item = item) }
                // Load reviews for this product
                loadReviews(item?.product_id ?: 0L)
            }
        }
    }

    private fun loadReviews(productId: Long) {
        viewModelScope.launch {
            // Filter reviews by product_id
            productRepo.getProductReviews(productId).collect { items ->
                _reviews.value = items
            }
        }
    }

    fun updateQuantity(quantity: Int) {
        _uiState.update { it.copy(quantity = quantity) }
        viewModelScope.launch {
            val item = _uiState.value.item ?: return@launch
            cartRepository.addToCart(item.id, item.outlet_id, quantity).collectLatest { 
                println("Details Screen item quantity changed $it") 
            }
        }
    }

    fun toggleWishlist() = _uiState.update { it.copy(isWishlisted = !it.isWishlisted) }
}
