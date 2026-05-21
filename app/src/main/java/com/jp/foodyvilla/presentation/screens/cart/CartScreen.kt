package com.jp.foodyvilla.presentation.screens.cart

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.utils.UiState
import com.razorpay.Checkout
import kotlinx.coroutines.launch
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onBrowseMenu: () -> Unit,
    onCheckoutOutlet: (Long) -> Unit, // New navigation callback
    viewModel: HomeViewModel,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

        val groupedItems = remember(state.cartItems) {
            state.cartItems.groupBy { it.outlet_id }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedItems.forEach { (outletId, items) ->
                val outlet = items.firstOrNull()?.outlets
                item {
                    OutletCartGroup(
                        outletName = outlet?.name ?: "Unknown Outlet",
                        items = items,
                        onCheckout = { onCheckoutOutlet(outletId) },
                        onIncrement = { cartItem ->
                            viewModel.updateCartItemQuantity(cartItem.outlet_menu_items!!, (cartItem.qty + 1).toInt())
                        },
                        onDecrement = { cartItem ->
                            viewModel.updateCartItemQuantity(cartItem.outlet_menu_items!!, (cartItem.qty - 1).toInt())
                        },
                        onRemove = { cartItem ->
                            viewModel.removeFromCart(cartItem.menu_item_id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OutletCartGroup(
    outletName: String,
    items: List<CartItem>,
    onCheckout: () -> Unit,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = outletName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${items.size} Items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            items.forEach { cartItem ->
                CartItemRow(
                    cartItem = cartItem,
                    onIncrement = { onIncrement(cartItem) },
                    onDecrement = { onDecrement(cartItem) },
                    onRemove = { onRemove(cartItem) }
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))
            val total = items.sumOf { it.totalPrice }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "₹${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Button(
                    onClick = onCheckout,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Checkout")
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cartItem.outlet_menu_items?.image?.firstOrNull() ?: "",
            contentDescription = cartItem.outlet_menu_items?.product_catalog?.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .padding(4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cartItem.outlet_menu_items?.product_catalog?.name ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                "₹${cartItem.outlet_menu_items?.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        QuantitySelector(
            quantity = cartItem.qty.toInt(),
            onDecrement = onDecrement,
            onIncrement = onIncrement
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


fun initiatePayment(
    context: Context,
    name: String,
    email: String = "",
    contact: String,
    amount: String,
) {

    try {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_ShBw7mlCM6gT6y") // ✅ dummy test key

        val options = JSONObject().apply {
            put("name", "FoodyVilla") // App name
            put("description", "Online Order")
            put("currency", "INR")
            put("amount", amount) // ₹499.00 (amount in paise)
            put("theme.color", "#E23744")

            put("prefill", JSONObject().apply {
                put("name", name)
                put("email", email)
                put("contact", contact)
            })


        }

        checkout.open(context as Activity, options)
    } catch (e: Exception) {
        println("Payment Error $e")
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
                model = cartItem.outlet_menu_items?.image?.firstOrNull() ?: "",
                contentDescription = cartItem.outlet_menu_items?.product_catalog?.name,
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
                    VegDot(isVeg = cartItem.outlet_menu_items?.product_catalog?.is_veg ?: false)
                    Text(
                        cartItem.outlet_menu_items?.product_catalog?.name ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
                Text(
                    "₹${cartItem.outlet_menu_items?.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                QuantitySelector(
                    quantity = cartItem.qty.toInt(),
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

