package com.jp.foodyvilla.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.Review
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.home.RatingChip
import com.jp.foodyvilla.presentation.utils.isOutletOpen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Long,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: DetailViewModel = koinViewModel(parameters = { parametersOf(itemId) }),
    homeViewModel: HomeViewModel,
) {
    val primaryRed = MaterialTheme.colorScheme.primary
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val recommended by viewModel.recommendedItems.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val item = state.item

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FoodyVilla", fontWeight = FontWeight.Black, color = primaryRed) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::toggleWishlist) {
                        Icon(if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = primaryRed)
                    }
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = { if(homeState.cartItems.isNotEmpty()) Badge { Text(homeState.cartItems.size.toString()) } }) {
                            Icon(Icons.Default.ShoppingCart, null, tint = primaryRed)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (item != null) {
                Surface(shadowElevation = 12.dp) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val cartItem = homeState.cartItems.firstOrNull { it.menu_item_id == item.id }
                        val inCart = cartItem != null
                        val isOpen = isOutletOpen(item.outlets?.opens_at, item.outlets?.closes_at)
                        
                        val currentQuantity = if (inCart) cartItem!!.qty.toInt() else state.quantity

                        QuantitySelector(
                            quantity = currentQuantity,
                            onDecrement = { 
                                if (inCart) homeViewModel.updateCartItemQuantity(item, currentQuantity - 1)
                                else viewModel.updateQuantity(state.quantity - 1)
                            },
                            onIncrement = { 
                                if (inCart) homeViewModel.updateCartItemQuantity(item, currentQuantity + 1)
                                else viewModel.updateQuantity(state.quantity + 1)
                            }
                        )

                        Spacer(Modifier.width(16.dp))
                        
                        Button(
                            onClick = { 
                                if (inCart) onCartClick()
                                else homeViewModel.updateCartItemQuantity(item, state.quantity)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inCart) Color(0xFF4CAF50) else if (isOpen) primaryRed else Color.Gray
                            ),
                            enabled = isOpen || inCart
                        ) {
                            Text(
                                if (!isOpen && !inCart) "Closed Now"
                                else if (inCart) "Go to Cart" 
                                else "Add to Cart • ₹${"%.2f".format(item.discountedPrice * state.quantity)}"
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading || item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = primaryRed) }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(320.dp)) {
                    FoodImageSlider(images = item.image)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface), startY = 500f)))
                }
            }
            item {
                Column(Modifier.offset(y = (-24).dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).padding(20.dp)) {
                    // Outlet Info
                    if (item.outlets != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.outlets.logo_url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    item.outlets.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    item.outlets.address ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Text(item.product_catalog?.name ?: "", style = MaterialTheme.typography.headlineMedium)
                    
                    if (item.discount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "₹${item.price.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Surface(color = Color(0xFFE53935).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    "${item.discount}% OFF",
                                    color = Color(0xFFE53935),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${item.discountedPrice}", style = MaterialTheme.typography.headlineSmall.copy(color = primaryRed))
                        if (item.is_free_delivery == true) {
                            Spacer(Modifier.width(12.dp))
                            Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text("FREE DELIVERY", color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    if (item.handling_charges != null && item.handling_charges!! > 0) {
                        Text("+ ₹${item.handling_charges} Handling Charges", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(item.product_catalog?.description ?: "")
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Reviews Section
                    Text("Customer Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (reviews.isEmpty()) {
                        Text("No reviews yet. Be the first to review!", modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        reviews.forEach { review ->
                            ReviewItem(review)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Recommended Products
                    if (recommended.isNotEmpty()) {
                        Text("Recommended for You", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        recommended.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { recItem ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        com.jp.foodyvilla.presentation.screens.home.FoodGridCard(
                                            item = recItem,
                                            onAddToCart = { homeViewModel.updateCartItemQuantity(recItem) },
                                            onClick = { onItemClick(recItem.id) },
                                            homeViewModel = homeViewModel
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.title ?: "Anonymous", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            RatingChip(rating = review.rating.toDouble())
        }
        Text(review.description ?: "", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        if (!review.img_url.isNullOrEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(review.img_url!!) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun FoodImageSlider(images: List<String>) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })
    Box {
        androidx.compose.foundation.pager.HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(model = images[page], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
            repeat(images.size) { index ->
                Box(Modifier.padding(2.dp).size(if (pagerState.currentPage == index) 8.dp else 6.dp).clip(CircleShape).background(if (pagerState.currentPage == index) Color.White else Color.Gray))
            }
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(horizontal = 4.dp)) {
        IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, null) }
        Text(quantity.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, null) }
    }
}
