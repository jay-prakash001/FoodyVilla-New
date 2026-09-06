package com.jp.foodyvilla.presentation.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.screens.home.CategoryChip
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.home.OutletHeader
import com.jp.foodyvilla.presentation.screens.home.QuantitySelector
import com.jp.foodyvilla.presentation.screens.home.VegDot
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onItemClick: (Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Column {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Search dishes, cuisines...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions { focusManager.clearFocus() }
                    )
                    
                    // Sticky Filters
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        // Category Filter
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(state.categories) { cat ->
                                CategoryChip(
                                    label = cat.name,
                                    emoji = cat.emoji,
                                    selected = state.selectedCategory == cat.id,
                                    onClick = { viewModel.selectCategory(cat.id) }
                                )
                            }
                        }
                        
                        // Outlet Filter
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                        if(state.recommendations.isNotEmpty()){
                                item {
                                    FilterChip(
                                        selected = state.selectedOutletId == -2L,
                                        onClick = { viewModel.selectOutlet(-2L) },
                                        label = { Text("🎊 Recommended for You") }
                                    )
                                }
                            }

                            item {
                                FilterChip(
                                    selected = state.selectedOutletId == -1L,
                                    onClick = { viewModel.selectOutlet(-1L) },
                                    label = { Text("All Outlets") }
                                )
                            }
                            items(state.outlets) { outlet ->
                                FilterChip(
                                    selected = state.selectedOutletId == outlet.id,
                                    onClick = { viewModel.selectOutlet(outlet.id) },
                                    label = { Text(outlet.name) },
                                    leadingIcon = {
                                        AsyncImage(
                                            model = outlet.logo_url,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 100.dp)
        ) {
            if (state.selectedOutletId == -2L) {
                // Recommendation Mode: Show all recommended items filtered by category/search
                val displayItems = state.recommendations.filter { item ->
                    val matchesCategory = state.selectedCategory == -1L ||
                            item.product_catalog?.category_id == state.selectedCategory

                    val matchesSearch = state.searchQuery.isBlank() ||
                            item.product_catalog?.name?.contains(state.searchQuery, true) == true

                    matchesCategory && matchesSearch
                }

                items(displayItems, key = { it.id }) { item ->
                    val outlet = state.outlets.find { it.id == item.outlet_id }
                    FoodListItem(
                        item = item,
                        outletLogo = outlet?.logo_url,
                        onAddToCart = { viewModel.updateCartItemQuantity(item) },
                        onClick = { onItemClick(item.id) },
                        homeViewModel = viewModel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                // Regular Outlet Mode: Group by outlet
                state.outlets.forEach { outlet ->
                    val matchesOutlet = state.selectedOutletId == -1L ||
                            outlet.id == state.selectedOutletId

                    if (matchesOutlet) {
                        val displayItems = outlet.outlet_menu_items?.filter { item ->
                            val matchesCategory = state.selectedCategory == -1L ||
                                    item.product_catalog?.category_id == state.selectedCategory

                            val matchesSearch = state.searchQuery.isBlank() ||
                                    item.product_catalog?.name?.contains(state.searchQuery, true) == true

                            matchesCategory && matchesSearch
                        } ?: emptyList()

                        if (displayItems.isNotEmpty()) {
                            item { OutletHeader(outlet) }

                            items(displayItems, key = { it.id }) { item ->
                                FoodListItem(
                                    item = item,
                                    outletLogo = outlet.logo_url,
                                    onAddToCart = { viewModel.updateCartItemQuantity(item) },
                                    onClick = { onItemClick(item.id) },
                                    homeViewModel = viewModel,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodListItem(
    item: OutletMenuItem,
    outletLogo: String?,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value
    val inCart = homeState.cartItems.any { it.menu_item_id == item.id }

    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = item.image.firstOrNull(),
                    contentDescription = item.product_catalog?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
                
                // Outlet Mini Logo
                if (outletLogo != null) {
                    AsyncImage(
                        model = outletLogo,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    VegDot(isVeg = item.product_catalog?.is_veg ?: true)
                    Text(
                        text = item.product_catalog?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                        Text("%.1f".format(item.rating), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text(
                    text = item.product_catalog?.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (item.discount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${item.price.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${item.discount}% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "₹${item.discountedPrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (item.is_free_delivery == true) {
                            Text(
                                text = "Free Delivery",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                  
                    if (!inCart) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
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
}
