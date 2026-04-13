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

class HomeViewModel(private val offerRepo: OfferRepo, private val productRepo: ProductRepo) :
    ViewModel() {

    private val _selectedPage = MutableStateFlow(0)
    val selectedPage = _selectedPage.asStateFlow()
    fun updateSelectedPage(page: Int) {
        _selectedPage.value = page
    }


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
