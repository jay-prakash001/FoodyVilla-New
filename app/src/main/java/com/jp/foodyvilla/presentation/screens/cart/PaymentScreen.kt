package com.jp.foodyvilla.presentation.screens.cart

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.utils.UiState
import com.razorpay.Checkout
import org.json.JSONObject

@Composable
fun PaymentScreen(
    outletId: Long,
    onBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()
    
    val outletItems = remember(state.cartItems, outletId) {
        state.cartItems.filter { it.outlet_id == outletId }
    }
    val outlet = outletItems.firstOrNull()?.outlets
    val totalAmount = outletItems.sumOf { it.totalPrice }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    var paymentErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is UiState.Success -> {
                showSuccessDialog = true
            }
            is UiState.Error -> {
                paymentErrorMessage = (paymentState as UiState.Error).msg
                showFailureDialog = true
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        if (outlet != null) {
            viewModel.setPendingOutletId(outletId)
            startRazorpay(
                activity = context as Activity,
                razorpayKey = outlet.razor_pay_key ?: "",
                amount = (totalAmount * 100).toLong(),
                name = orderState.customerName,
                contact = orderState.phone,
                email = "", // Optional
                onSuccess = { _ ->
                    // Handled via MainActivity -> ViewModel
                },
                onFailure = { _, message ->
                    paymentErrorMessage = message
                    showFailureDialog = true
                }
            )
        }
    }

    // Semi-transparent overlay while processing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (paymentState is UiState.Loading) "Processing Order..." else "Redirecting to Payment...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Please do not close the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showSuccessDialog) {
        PaymentStatusDialog(
            isSuccess = true,
            title = "Order Placed!",
            message = "Your order has been placed successfully. You can track it in your order history.",
            onDismiss = {
                showSuccessDialog = false
                viewModel.resetPaymentState()
                onOrderSuccess() // This will now just navigate back/refresh as per requirement
            }
        )
    }

    if (showFailureDialog) {
        PaymentStatusDialog(
            isSuccess = false,
            title = "Payment Failed",
            message = paymentErrorMessage.ifBlank { "Something went wrong during the transaction." },
            onDismiss = {
                showFailureDialog = false
                viewModel.resetPaymentState()
                onBack() // Navigate back to cart to allow retry
            }
        )
    }
}

@Composable
fun PaymentStatusDialog(
    isSuccess: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Translucent background
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.1f) 
                                else Color(0xFFF44336).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    ) {
                        Text("Okay", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

fun startRazorpay(
    activity: Activity,
    razorpayKey: String,
    amount: Long,
    name: String,
    contact: String,
    email: String,
    onSuccess: (String) -> Unit,
    onFailure: (Int, String) -> Unit
) {
    val checkout = Checkout()
    checkout.setKeyID(razorpayKey)
    
    try {
        val options = JSONObject().apply {
            put("name", "FoodyVilla")
            put("description", "Outlet Order")
            put("currency", "INR")
            put("amount", amount)
            put("theme.color", "#E23744")
            put("prefill", JSONObject().apply {
                put("name", name)
                put("contact", contact)
                put("email", email)
            })
        }
        checkout.open(activity, options)
    } catch (e: Exception) {
        onFailure(-1, e.message ?: "Error starting Razorpay")
    }
}
