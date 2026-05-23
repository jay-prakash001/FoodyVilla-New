package com.jp.foodyvilla.presentation.screens.cart

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAddScreen(
    outletId: Long,
    onBack: () -> Unit,
    onProceedToPayment: (outletId: Long) -> Unit,
    viewModel: HomeViewModel,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val userState by loginViewModel.user.collectAsStateWithLifecycle()

    // Prefill user details once they are loaded
    LaunchedEffect(userState) {
        if (userState is UiState.Success) {
            val user = (userState as UiState.Success).data
            if (orderState.customerName.isBlank()) viewModel.updateCustomerName(user.name ?: "")
            if (orderState.phone.isBlank()) viewModel.updatePhone(user.phone ?: "")
            if (orderState.address.isBlank()) viewModel.updateAddress(user.address ?: "")
        }
    }

    val outletItems = remember(state.cartItems, outletId) {
        state.cartItems.filter { it.outlet_id == outletId }
    }
    
    val outlet = outletItems.firstOrNull()?.outlets

    var nameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }

    val nameError = if (nameTouched && orderState.customerName.isBlank()) "Name is required" else null
    val phoneError = when {
        phoneTouched && orderState.phone.isBlank() -> "Phone number is required"
        phoneTouched && !orderState.phone.replace("+", "").matches(Regex("^[0-9]{10,13}$")) -> "Enter a valid phone number (10-13 digits)"
        else -> null
    }
    val addressError = if (orderState.orderType == "Delivery" && addressTouched && orderState.address.isBlank()) "Address is required for delivery" else null

    val isFormValid = orderState.customerName.isNotBlank() 
            && orderState.phone.replace("+", "").matches(Regex("^[0-9]{10,13}$"))
            && (orderState.orderType != "Delivery" || orderState.address.isNotBlank())

    var showLocationRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { it }
        if (isGranted) {
            if (viewModel.isGpsEnabled()) viewModel.fetchCurrentLocation()
            else context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            Toast.makeText(context, "Location permission is required to fetch address", Toast.LENGTH_SHORT).show()
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Location Permission") },
            text = { Text("We need your location to automatically fill your delivery address for a better experience.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationRationale = false
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Deny")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { if (isFormValid) onProceedToPayment(outletId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Proceed to Payment", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Ordering from: ${outlet?.name ?: "Outlet"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Order Type", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Delivery", "Pickup", "Dine-In").forEach { type ->
                                FilterChip(
                                    selected = orderState.orderType == type,
                                    onClick = { viewModel.updateOrderType(type) },
                                    label = { Text(type) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = orderState.customerName,
                            onValueChange = { viewModel.updateCustomerName(it); nameTouched = true },
                            label = { Text("Name *") },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = orderState.phone,
                            onValueChange = { viewModel.updatePhone(it); phoneTouched = true },
                            label = { Text("Phone *") },
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (orderState.orderType == "Delivery") {
                            OutlinedTextField(
                                value = orderState.address,
                                onValueChange = { viewModel.updateAddress(it); addressTouched = true },
                                label = { Text("Address *") },
                                isError = addressError != null,
                                supportingText = { addressError?.let { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2
                            )
                            
                            Button(
                                onClick = {
                                    when {
                                        viewModel.hasLocationPermission() -> {
                                            if (viewModel.isGpsEnabled()) viewModel.fetchCurrentLocation()
                                            else context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                        }
                                        else -> {
                                            showLocationRationale = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.MyLocation, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Use Current Location")
                            }
                        }

                        OutlinedTextField(
                            value = orderState.instructions,
                            onValueChange = { viewModel.updateInstructions(it) },
                            label = { Text("Instructions (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )
                    }
                }
            }
            
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bill Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        outletItems.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.outlet_menu_items?.product_catalog?.name} x ${item.qty}", style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${"%.2f".format((item.outlet_menu_items?.price ?: 0.0) * item.qty)}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if ((item.outlet_menu_items?.discount ?: 0) > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Discount (${item.outlet_menu_items?.discount}% off)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                        Text("-₹${"%.2f".format(((item.outlet_menu_items?.price ?: 0.0) - (item.outlet_menu_items?.discountedPrice ?: 0.0)) * item.qty)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                    }
                                }
                            }
                        }
                        
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        
                        val subtotal = outletItems.sumOf { (it.outlet_menu_items?.price ?: 0.0) * it.qty }
                        val totalDiscount = outletItems.sumOf { ((it.outlet_menu_items?.price ?: 0.0) - (it.outlet_menu_items?.discountedPrice ?: 0.0)) * it.qty }
                        val handling = outletItems.sumOf { (it.outlet_menu_items?.handling_charges ?: 0.0) * it.qty }
                        val delivery = outletItems.maxOfOrNull { it.outlet_menu_items?.delivery_charges ?: 0.0 } ?: 0.0
                        val grandTotal = subtotal - totalDiscount + handling + delivery

                        CheckoutPriceRow("Item Total (Original)", subtotal)
                        if (totalDiscount > 0) CheckoutPriceRow("Total Discount", totalDiscount, isDiscount = true)
                        if (handling > 0) CheckoutPriceRow("Handling Charges", handling)
                        if (delivery > 0) CheckoutPriceRow("Delivery Charges", delivery) 
                        
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text("₹${"%.2f".format(grandTotal)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutPriceRow(label: String, amount: Double, isFree: Boolean = false, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (isFree) "FREE" else if (isDiscount) "-₹${"%.2f".format(amount)}" else "₹${"%.2f".format(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isFree || isDiscount) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}
