package com.jp.foodyvilla.presentation.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.jp.foodyvilla.data.model.Banner
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onItemClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    
    // Real-time permission and location tracking
    var isNotificationEnabled by remember { mutableStateOf(viewModel.hasNotificationPermission()) }
    var isLocationEnabled by remember { mutableStateOf(viewModel.hasLocationPermission()) }
    var isGpsEnabled by remember { mutableStateOf(viewModel.isGpsEnabled()) }
    
    var showLocationRationale by remember { mutableStateOf(false) }
    var locationPermanentlyDenied by remember { mutableStateOf(false) }

    var showNotificationRationale by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isGpsEnabled = true
            viewModel.fetchCurrentLocation(force = true)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationEnabled = granted
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        isLocationEnabled = granted
        if (granted) {
            locationPermanentlyDenied = false
            viewModel.checkLocationSettings(
                onSuccess = {
                    isGpsEnabled = true
                    viewModel.fetchCurrentLocation(force = true)
                },
                onFailure = { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                            gpsLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) { }
                    }
                }
            )
        } else {
            val activity = context.findActivity()
            if (activity != null) {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                if (showRationale) {
                    showLocationRationale = true
                    locationPermanentlyDenied = false
                } else {
                    locationPermanentlyDenied = true
                    showLocationRationale = false
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationEnabled = viewModel.hasNotificationPermission()
                
                val currentLocEnabled = viewModel.hasLocationPermission()
                val currentGpsEnabled = viewModel.isGpsEnabled()

                if (currentLocEnabled) {
                    locationPermanentlyDenied = false
                    showLocationRationale = false
                }

                if (currentLocEnabled != isLocationEnabled || currentGpsEnabled != isGpsEnabled) {
                    isLocationEnabled = currentLocEnabled
                    isGpsEnabled = currentGpsEnabled
                    if (currentLocEnabled && currentGpsEnabled) {
                        viewModel.fetchCurrentLocation(force = true)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (state.allItems.isEmpty()) {
            viewModel.loadData()
        }

        // Handle Notification Permission Rationale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!viewModel.hasNotificationPermission()) {
                val activity = context.findActivity()
                if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationRationale = true
                } else {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        delay(1500)

        // Handle Location Permission Rationale
        if (!viewModel.hasLocationPermission()) {
            val activity = context.findActivity()
            val showRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION) 
            } ?: false
            
            if (showRationale) {
                showLocationRationale = true
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            viewModel.checkLocationSettings(
                onSuccess = {
                    isGpsEnabled = true
                    viewModel.fetchCurrentLocation()
                },
                onFailure = { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                            gpsLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) { }
                    }
                }
            )
        }
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationRationale = false },
            title = { Text("Notifications Needed") },
            text = { Text("FoodyVilla would like to send you notifications about your order status and exclusive offers.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationRationale = false }) {
                    Text("No thanks")
                }
            }
        )
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Location Access Needed") },
            text = { Text("FoodyVilla needs your location to show the nearest outlets and food recommendations for you.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationRationale = false
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(containerColor = colors.background) { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {


        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp),
            modifier = Modifier.fillMaxSize()
        )
        {
            // Hero section
            item(span = { GridItemSpan(2) }) {
                HeroSection(
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::onSearchQueryChange
                )
            }

            // Banners
            item(span = { GridItemSpan(2) }) {
                BannerSlider(banners = state.banners)
            }

            // Categories
            item(span = { GridItemSpan(2) }) {
                CategorySection(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategory,
                    onCategorySelect = viewModel::selectCategory
                )
            }

            // Notification Permission Banner (Now below categories)
            if (!isNotificationEnabled) {
                item(span = { GridItemSpan(2) }) {
                    NotificationPermissionBanner(
                        onEnable = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Location Permission Banner (If permanently denied)
            if (locationPermanentlyDenied && !isLocationEnabled) {
                item(span = { GridItemSpan(2) }) {
                    LocationPermissionBanner(
                        onEnable = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Flat list of all products
            val itemsToShow = state.recommendations.ifEmpty { state.filteredItems }
            if (itemsToShow.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        if (state.recommendations.isNotEmpty()) "Recommended for You" else "Products for You",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                items(itemsToShow) { item ->
                    FoodGridCard(
                        item = item,
                        onAddToCart = { viewModel.updateCartItemQuantity(item) },
                        onClick = { onItemClick(item.id) },
                        homeViewModel = viewModel,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }


            if (state.isLoading || (locationState is UiState.Loading && state.recommendations.isEmpty())) {

                Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(0.2f)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

                    CircularProgressIndicator(color = colors.primary)

                    Text("Getting your nearest outlet...", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
                }

            }
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun LocationPermissionBanner(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Location Access Required",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Please enable location in settings to see nearby outlets and get recommendations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onEnable) {
                Text("Settings")
            }
        }
    }
}

@Composable
fun FoodGridCard(
    item: OutletMenuItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val inCart = homeState.cartItems.any { it.menu_item_id == item.id }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                RatingChip(
                    rating = item.rating.toDouble(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )

                // Outlet Mini Logo
                val outlet = homeState.outlets.find { it.id == item.outlet_id }
                if (outlet?.logo_url != null) {
                    AsyncImage(
                        model = outlet.logo_url,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                if (item.is_free_delivery == true) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "FREE DELIVERY",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                Text(
                    item.product_catalog?.name ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${item.discountedPrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (item.discount > 0) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "₹${item.price.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                    if (item.discount > 0) {
                        Text(
                            text = "${item.discount}% OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF43A047),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!inCart) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onAddToCart() },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                } else {
                    val cartItem = homeState.cartItems.find { it.menu_item_id == item.id }
                    QuantitySelector(
                        quantity = cartItem?.qty?.toInt() ?: 0,
                        onDecrement = {
                            homeViewModel.updateCartItemQuantity(
                                item,
                                (cartItem?.qty ?: 0).toInt() - 1
                            )
                        },
                        onIncrement = {
                            homeViewModel.updateCartItemQuantity(
                                item,
                                (cartItem?.qty ?: 0).toInt() + 1
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionBanner(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Enable Notifications",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Get real-time updates about your orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onEnable) {
                Text("Enable")
            }
        }
    }
}

@Composable
fun HeroSection(searchQuery: String, onSearchChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colors.primaryContainer,
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                "Order Delicious\nFood Today",
                style = MaterialTheme.typography.displayMedium,
                color = colors.onPrimaryContainer
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search dishes, cuisines...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
fun CategorySection(
    categories: List<com.jp.foodyvilla.data.model.Category>,
    selectedCategoryId: Long,
    onCategorySelect: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            "Categories",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                CategoryChip(
                    label = cat.name,
                    emoji = cat.emoji,
                    selected = selectedCategoryId == cat.id,
                    onClick = { onCategorySelect(cat.id) })
            }
        }
    }
}

@Composable
fun OutletHeader(outlet: Outlet) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = outlet.logo_url,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                outlet.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                outlet.address ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FoodCard(
    item: OutletMenuItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val inCart = homeState.cartItems.any { it.menu_item_id == item.id }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                RatingChip(
                    rating = item.rating.toDouble(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Outlet Mini Logo
                val outlet = homeState.outlets.find { it.id == item.outlet_id }
                if (outlet?.logo_url != null) {
                    AsyncImage(
                        model = outlet.logo_url,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                if (item.is_free_delivery == true) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "FREE DELIVERY",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                Text(
                    item.product_catalog?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                item.product_catalog?.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (item.discount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "₹${item.price.toInt()}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${item.discount}% OFF",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        "₹${item.discountedPrice.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!inCart) {
                    Button(onClick = onAddToCart, shape = RoundedCornerShape(12.dp)) {
                        Text("Add")
                    }
                } else {
                    val cartItem = homeState.cartItems.find { it.menu_item_id == item.id }
                    QuantitySelector(
                        quantity = cartItem?.qty?.toInt() ?: 0,
                        onDecrement = {
                            homeViewModel.updateCartItemQuantity(
                                item,
                                (cartItem?.qty ?: 0).toInt() - 1
                            )
                        },
                        onIncrement = {
                            homeViewModel.updateCartItemQuantity(
                                item,
                                (cartItem?.qty ?: 0).toInt() + 1
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .width(72.dp)
            .height(88.dp)
            .border(1.dp,if (selected) Color.Transparent else MaterialTheme.colorScheme.onSurface,RoundedCornerShape(16.dp))
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSlider(banners: List<Banner>) {
    val pagerState = rememberPagerState { banners.size }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (banners.isNotEmpty()) pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
    ) { page ->
        AsyncImage(
            model = banners[page].img_url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun RatingChip(rating: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(12.dp)
            )
            Text(
                "%.1f".format(rating),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VegDot(isVeg: Boolean) {
    val color = if (isVeg) Color(0xFF43A047) else Color.Red
    Box(
        Modifier
            .size(14.dp)
            .border(1.5.dp, color, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier
            .size(7.dp)
            .background(color, CircleShape))
    }
}

@Composable
fun QuantitySelector(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, null) }
        Text(quantity.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, null) }
    }
}
