package com.jp.foodyvilla.presentation.screens.home

import Banner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.FoodItem
import com.jp.foodyvilla.data.model.MockData
import com.jp.foodyvilla.data.repo.OfferRepo
import com.jp.foodyvilla.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val bestSellers: List<FoodItem>
        get() = allItems.filter { it.isBestSeller }

    val filteredItems: List<FoodItem>
        get() {
            val byCategory = if (selectedCategory == "all") allItems
            else allItems.filter { it.category == selectedCategory }
            return if (searchQuery.isBlank()) byCategory
            else byCategory.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    val popularItems: List<FoodItem>
        get() = allItems.filter { !it.isBestSeller }
}

class HomeViewModel(private val offerRepo: OfferRepo, private val productRepo: ProductRepo) :
    ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()

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
        }
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
