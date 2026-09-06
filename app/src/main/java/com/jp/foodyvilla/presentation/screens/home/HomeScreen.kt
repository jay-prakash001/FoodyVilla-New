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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
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
import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onItemClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel,
    onSupportClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = 120.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Compact Top App Bar
                item(span = { GridItemSpan(2) }) {
                    CompactHeader(
                        onSupportClick = onSupportClick,
                        onCartClick = onCartClick,
                        onProfileClick = onProfileClick,
                        cartItemCount = state.cartItems.size
                    )
                }

                // 2. Search Bar
                item(span = { GridItemSpan(2) }) {
                    ModernSearchBar(
                        searchQuery = state.searchQuery,
                        onSearchChange = viewModel::onSearchQueryChange
                    )
                }

                // 3. Promotional Banner Carousel
                item(span = { GridItemSpan(2) }) {
                    PromotionalBannerSlider(banners = state.banners)
                }

                // 4. Categories Section
                item(span = { GridItemSpan(2) }) {
                    CompactCategorySection(
                        categories = state.categories,
                        selectedCategoryId = state.selectedCategory,
                        onCategorySelect = viewModel::selectCategory,
                        onSeeAllClick = onMenuClick
                    )
                }

                // Permission Banners if active
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

                // 5. Recommended for You Section
                val itemsToShow = state.filteredItems
                val sectionHeaderTitle = when {
                    state.searchQuery.isNotBlank() -> "Search Results"
                    state.selectedCategory != -1L -> {
                        state.categories.find { it.id == state.selectedCategory }?.name ?: "Filtered Products"
                    }
                    state.recommendations.isNotEmpty() -> "Recommended for You"
                    else -> "Products for You"
                }

                if (itemsToShow.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sectionHeaderTitle,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            )
                            Row(
                                modifier = Modifier.clickable { onMenuClick() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "See all",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "See all",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    items(itemsToShow) { item ->
                        ModernFoodCard(
                            item = item,
                            onAddToCart = { viewModel.updateCartItemQuantity(item) },
                            onClick = { onItemClick(item.id) },
                            homeViewModel = viewModel
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surface.copy(alpha = 0.3f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = colors.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Getting your nearest outlet...",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT 1: COMPACT TOP APP BAR
// ─────────────────────────────────────────────────────────────

@Composable
fun CompactHeader(
    onSupportClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    cartItemCount: Int
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good day 👋",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "What would you like to eat?",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Support Icon
            HeaderIconButton(
                icon = Icons.Outlined.Headset,
                contentDescription = "Support",
                onClick = onSupportClick
            )

            // Cart Icon with badge
            Box {
                HeaderIconButton(
                    icon = Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart",
                    onClick = onCartClick
                )
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(18.dp)
                            .background(colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            color = colors.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Profile Icon
            HeaderIconButton(
                icon = Icons.Outlined.Person,
                contentDescription = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = colors.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT 2: MODERN SEARCH BAR
// ─────────────────────────────────────────────────────────────

@Composable
fun ModernSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerHigh,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = colors.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search dishes, cuisines...",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
                    .background(colors.outlineVariant)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = colors.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT 3: PROMOTIONAL BANNER SLIDER
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromotionalBannerSlider(banners: List<Banner>) {
    if (banners.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val pagerState = rememberPagerState { banners.size }
    LaunchedEffect(banners) {
        while (true) {
            delay(3500)
            if (banners.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
        ) { page ->
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = banners[page].img_url,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (banners.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .background(
                                color = if (isSelected) colors.primary else colors.outlineVariant,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT 4: CATEGORIES SECTION
// ─────────────────────────────────────────────────────────────

@Composable
fun CompactCategorySection(
    categories: List<Category>,
    selectedCategoryId: Long,
    onCategorySelect: (Long) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            )
            Row(
                modifier = Modifier.clickable { onSeeAllClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See all",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "See all",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategoryId == category.id
                CompactCategoryCard(
                    label = category.name,
                    emoji = category.emoji,
                    selected = isSelected,
                    onClick = { onCategorySelect(category.id) }
                )
            }
        }
    }
}

@Composable
fun CompactCategoryCard(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (selected) colors.primary else colors.surface
    val textColor = if (selected) colors.onPrimary else colors.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .width(78.dp)
            .height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT 5: RECOMMENDED FOR YOU / PRODUCT CARDS
// ─────────────────────────────────────────────────────────────

@Composable
fun ModernFoodCard(
    item: OutletMenuItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val cartItem = homeState.cartItems.find { it.menu_item_id == item.id }
    val inCart = cartItem != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating Badge (Top Right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface.copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "%.1f".format(item.rating.toDouble()),
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        )
                    }
                }

                // Veg / Non-Veg Indicator (Bottom Left of Image)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(colors.surface.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .padding(3.dp)
                ) {
                    VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dish Name
            Text(
                text = item.product_catalog?.name ?: "",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            val desc = item.product_catalog?.description ?: ""
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Column {
                    Text(
                        text = "₹${item.discountedPrice.toInt()}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                    if (item.discount > 0) {
                        Text(
                            text = "₹${item.price.toInt()}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                }

                // Add Button / Quantity Selector
                if (!inCart) {
                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Add",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    CompactQuantitySelector(
                        quantity = cartItem?.qty?.toInt() ?: 1,
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
fun CompactQuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.primaryContainer,
        border = BorderStroke(1.dp, colors.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = colors.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                text = quantity.toString(),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = colors.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPATIBILITY & HELPER COMPOSABLES FOR OTHER SCREENS
// ─────────────────────────────────────────────────────────────

@Composable
fun QuantitySelector(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    CompactQuantitySelector(
        quantity = quantity,
        onDecrement = onDecrement,
        onIncrement = onIncrement
    )
}

@Composable
fun RatingChip(rating: Double, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFB800),
                modifier = Modifier.size(12.dp)
            )
            Text(
                "%.1f".format(rating),
                color = colors.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChip(label: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    CompactCategoryCard(label = label, emoji = emoji, selected = selected, onClick = onClick)
}

@Composable
fun OutletHeader(outlet: Outlet) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                style = MaterialTheme.typography.titleMedium,
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
fun FoodGridCard(
    item: OutletMenuItem,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    ModernFoodCard(
        item = item,
        onAddToCart = onAddToCart,
        onClick = onClick,
        homeViewModel = homeViewModel,
        modifier = modifier
    )
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
fun VegDot(isVeg: Boolean) {
    val color = if (isVeg) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Box(
        Modifier
            .size(14.dp)
            .border(1.5.dp, color, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun LocationPermissionBanner(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
                    "Please enable location in settings to see nearby outlets.",
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
fun NotificationPermissionBanner(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
