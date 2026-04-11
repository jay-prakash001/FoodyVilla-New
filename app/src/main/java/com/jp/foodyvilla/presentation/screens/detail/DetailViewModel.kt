package com.jp.foodyvilla.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.jp.foodyvilla.data.model.FoodItem
import com.jp.foodyvilla.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val item: FoodItem? = null,
    val quantity: Int = 1,
    val isWishlisted: Boolean = false,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val foodRepository: ProductRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
    }

    fun loadItem(itemId : Int) {
        viewModelScope.launch {
            foodRepository.getProductById(itemId).collect { item ->
                _uiState.update { it.copy(isLoading = false, item = item) }
            }
        }
    }

    fun increment() = _uiState.update { it.copy(quantity = it.quantity + 1) }
    fun decrement() = _uiState.update { it.copy(quantity = maxOf(1, it.quantity - 1)) }
    fun toggleWishlist() = _uiState.update { it.copy(isWishlisted = !it.isWishlisted) }
}
