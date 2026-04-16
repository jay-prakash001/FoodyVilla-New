package com.jp.foodyvilla.presentation.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.FoodItem
import com.jp.foodyvilla.data.model.MockData
import com.jp.foodyvilla.data.repo.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = ""
) {
    val filteredItems: List<FoodItem>
        get() = if (selectedCategory == "") allItems
        else allItems.filter { it.category.contains( selectedCategory,true) }
}

class MenuViewModel(private val foodRepository: ProductRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            foodRepository.getProducts().collect { items ->
                _uiState.update {
                    it.copy(isLoading = false, allItems = items, categories = MockData.categories)
                }
            }
        }
    }

    fun selectCategory(name: String) = _uiState.update { it.copy(selectedCategory = if(name.contains("all",true)){""}else name) }
}
