package com.jp.foodyvilla.presentation.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewsUiState(val isLoading: Boolean = true, val reviews: List<Review> = emptyList())

class ReviewsViewModel(
//    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            reviewRepository.getReviews().collect { reviews ->
//                _uiState.update { it.copy(isLoading = false, reviews = reviews) }
//            }
//        }
//    }

    fun addReview(){
        viewModelScope.launch {

        }
    }
}
