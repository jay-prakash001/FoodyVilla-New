package com.jp.foodyvilla.presentation.test

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// ─── Models ──────────────────────────────────────────────────────────────────

data class ProductOrder(
    val orderId: String,
    val productId: String,
    val productName: String,
    val amount: Int,
    val currency: String = "INR",
    val userEmail: String,
    val userPhone: String,
    val userName: String
)

data class PaymentResult(
    val razorpayPaymentId: String,
    val razorpayOrderId: String,
    val razorpaySignature: String,
    val productOrder: ProductOrder,
    val status: PaymentStatus,
    val errorCode: Int? = null,
    val errorDescription: String? = null
)

enum class PaymentStatus { SUCCESS, FAILED }

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class CheckoutUiState {
    data object Idle : CheckoutUiState()
    data object CreatingOrder : CheckoutUiState()
    data class ReadyToLaunchPayment(
        val razorpayOrderId: String,
        val productOrder: ProductOrder
    ) : CheckoutUiState()
    data class PaymentSuccess(val paymentResult: PaymentResult) : CheckoutUiState()
    data class PaymentFailed(val errorCode: Int, val errorDescription: String) : CheckoutUiState()
}
