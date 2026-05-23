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

    private val _recommendedItems = MutableStateFlow<List<OutletMenuItem>>(emptyList())
    val recommendedItems = _recommendedItems.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            productRepo.getProductById(itemId).collect { item ->
                _uiState.update { it.copy(isLoading = false, item = item) }
                // Load reviews for this product
                if (item != null) {
                    loadReviews(item.product_id)
                    loadRecommended(item.product_catalog?.category_id ?: 0L, item.id)
                }
            }
        }
    }

    private fun loadRecommended(categoryId: Long, currentItemId: Long) {
        viewModelScope.launch {
            // Fetch items from the same category
            productRepo.getOutletsWithMenu().collect { outlets ->
                val recommended = outlets.flatMap { it.outlet_menu_items ?: emptyList() }
                    .filter { 
                        it.product_catalog?.category_id == categoryId && 
                        it.id != currentItemId 
                    }
                    .distinctBy { it.product_id }
                    .take(6)
                _recommendedItems.value = recommended
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
        if (quantity < 1) return
        _uiState.update { it.copy(quantity = quantity) }
    }

    fun toggleWishlist() = _uiState.update { it.copy(isWishlisted = !it.isWishlisted) }
}
