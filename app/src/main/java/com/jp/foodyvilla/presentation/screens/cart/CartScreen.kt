package com.jp.foodyvilla.presentation.screens.cart

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.cart.CartItem
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.home.QuantitySelector
import com.jp.foodyvilla.presentation.screens.home.VegDot


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onBrowseMenu: () -> Unit,
    viewModel: HomeViewModel
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getCartItems()
    }



    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf("Delivery") }

    // Track whether user has touched each field (so errors don't show before interaction)
    var nameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }


    // Validation
    val nameError = if (nameTouched && customerName.isBlank()) "Name is required" else null
    val phoneError = when {
        phoneTouched && phone.isBlank() -> "Phone number is required"
        phoneTouched && !phone.matches(Regex("^[+]?[0-9]{7,15}$")) -> "Enter a valid phone number"
        else -> null
    }
    val addressError =
        if (orderType == "Delivery" && addressTouched && address.isBlank()) "Address is required for delivery" else null

    val isFormValid = customerName.isNotBlank()
            && phone.matches(Regex("^[+]?[0-9]{7,15}$"))
            && (orderType != "Delivery" || address.isNotBlank())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (state.cartItems.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {
//                            // Force-touch all fields to surface any remaining errors
//                            nameTouched = true
//                            phoneTouched = true
//                            addressTouched = true
//
//                            if (!isFormValid) return@Button
//
//                            val message = viewModel.generateWhatsAppMessage(
//                                state, customerName, phone, address, orderType, instructions
//                            )
//                            val url = "https://wa.me/917067371183?text=${
//                                URLEncoder.encode(message, "UTF-8")
//                            }"
//                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                            viewModel.placeOrder(address = address, customerName = customerName, phone = phone, orderType = orderType, instruction = instructions)

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(
                            "Place Order • ₹${"%.2f".format(viewModel.getTotalCartValue())}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isFormValid) Color.White
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    ) { padding ->

        if (state.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add something delicious!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onBrowseMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Browse Menu", color = Color.White)
                    }
                }
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.cartItems, key = { it.products!!.id }) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onIncrement = {

                        viewModel.updateCartItemQuantity(cartItem.products!!, cartItem.qty + 1)
                    },
                    onDecrement = {
                        viewModel.updateCartItemQuantity(cartItem.products!!, cartItem.qty - 1)
                    },
                    onRemove = { viewModel.removeFromCart(cartItem.products!!.id) }
                )
            }

            // Order details form
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Order Details", style = MaterialTheme.typography.titleLarge)

                        // Order Type chips — placed first so address field
                        // appears/disappears before the user reaches it
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Delivery", "Pickup", "Dine-In").forEach { type ->
                                FilterChip(
                                    selected = orderType == type,
                                    onClick = {
                                        orderType = type
                                        // Reset address touched state when switching away from Delivery
                                        if (type != "Delivery") addressTouched = false
                                    },
                                    label = { Text(type) }
                                )
                            }
                        }

                        // Customer Name
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                nameTouched = true
                            },
                            label = { Text("Customer Name *") },
                            isError = nameError != null,
                            supportingText = {
                                if (nameError != null) {
                                    Text(nameError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneTouched = true
                            },
                            label = { Text("Phone Number *") },
                            isError = phoneError != null,
                            supportingText = {
                                if (phoneError != null) {
                                    Text(phoneError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Address — required only for Delivery
                        if (orderType == "Delivery") {
                            OutlinedTextField(
                                value = address,
                                onValueChange = {
                                    address = it
                                    addressTouched = true
                                },
                                label = { Text("Delivery Address *") },
                                isError = addressError != null,
                                supportingText = {
                                    if (addressError != null) {
                                        Text(addressError, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                minLines = 2,
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Special Instructions — optional
                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = { Text("Special Instructions") },
                            placeholder = { Text("Optional — e.g. extra spicy, no onions…") },
                            leadingIcon = {
                                Icon(Icons.Default.EditNote, contentDescription = null)
                            },
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Order summary
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Order Summary", style = MaterialTheme.typography.titleLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "₹${"%.2f".format(viewModel.getTotalCartValue())}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cartItem.products!!.image[0],
                contentDescription = cartItem.products!!.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegDot(isVeg = cartItem.products?.isVeg ?: false)
                    Text(
                        cartItem.products?.name ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
                Text(
                    "₹${cartItem.products?.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                QuantitySelector(
                    quantity = cartItem.qty,
                    onDecrement = onDecrement,
                    onIncrement = onIncrement
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "₹${"%.2f".format(cartItem.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}
