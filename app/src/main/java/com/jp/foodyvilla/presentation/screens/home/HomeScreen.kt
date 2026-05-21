package com.jp.foodyvilla.presentation.screens.home

import com.jp.foodyvilla.data.model.Banner
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.utils.RequestNotificationPermission
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onItemClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    // Real-time notification permission tracking
    var isNotificationEnabled by remember { mutableStateOf(viewModel.hasNotificationPermission()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationEnabled = viewModel.hasNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(containerColor = colors.background) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Scaffold
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero section
            item(span = { GridItemSpan(2) }) {
                HeroSection(searchQuery = state.searchQuery, onSearchChange = viewModel::onSearchQueryChange)
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
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Flat list of all products
            if (state.filteredItems.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Products for You",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                items(state.filteredItems) { item ->
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
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
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
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(18.dp))
                )
                RatingChip(rating = item.rating.toDouble(), modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                Text(item.product_catalog?.name ?: "", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${item.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
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
                        onDecrement = { homeViewModel.updateCartItemQuantity(item, (cartItem?.qty ?: 0).toInt() - 1) },
                        onIncrement = { homeViewModel.updateCartItemQuantity(item, (cartItem?.qty ?: 0).toInt() + 1) }
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
            .background(colors.primaryContainer, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(20.dp)
    ) {
        Column {
            Text("Order Delicious\nFood Today", style = MaterialTheme.typography.displayMedium, color = colors.onPrimaryContainer)
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
fun CategorySection(categories: List<com.jp.foodyvilla.data.model.Category>, selectedCategoryId: Long, onCategorySelect: (Long) -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text("Categories", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { cat ->
                CategoryChip(label = cat.name, emoji = cat.emoji, selected = selectedCategoryId == cat.id, onClick = { onCategorySelect(cat.id) })
            }
        }
    }
}

@Composable
fun OutletHeader(outlet: Outlet) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = outlet.logo_url,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(outlet.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(outlet.address ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(18.dp))
                )
                RatingChip(rating = item.rating.toDouble(), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                Text(item.product_catalog?.name ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, modifier = Modifier.weight(1f))
            }
            Text(item.product_catalog?.description ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("₹${item.price}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                if (!inCart) {
                    Button(onClick = onAddToCart, shape = RoundedCornerShape(12.dp)) {
                        Text("Add")
                    }
                } else {
                    val cartItem = homeState.cartItems.find { it.menu_item_id == item.id }
                    QuantitySelector(
                        quantity = cartItem?.qty?.toInt() ?: 0,
                        onDecrement = { homeViewModel.updateCartItemQuantity(item, (cartItem?.qty ?: 0).toInt() - 1) },
                        onIncrement = { homeViewModel.updateCartItemQuantity(item, (cartItem?.qty ?: 0).toInt() + 1) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.width(72.dp).height(88.dp)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 24.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor, maxLines = 1)
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
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(180.dp).padding(8.dp)) { page ->
        AsyncImage(model = banners[page].img_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
    }
}

@Composable
fun RatingChip(rating: Double, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.9f)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000), modifier = Modifier.size(12.dp))
            Text("%.1f".format(rating), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VegDot(isVeg: Boolean) {
    val color = if (isVeg) Color(0xFF43A047) else Color.Red
    Box(Modifier.size(14.dp).border(1.5.dp, color, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
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
