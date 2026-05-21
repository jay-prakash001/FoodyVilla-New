package com.jp.foodyvilla.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.Review
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.home.RatingChip
import com.jp.foodyvilla.presentation.screens.home.VegDot
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val item = state.item

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(homeState.cartItems) {
        val cartItem = homeState.cartItems.firstOrNull { it.menu_item_id == itemId }
        viewModel.updateQuantity(cartItem?.qty?.toInt() ?: 1)
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
                        QuantitySelector(
                            quantity = state.quantity,
                            onDecrement = { viewModel.updateQuantity(state.quantity - 1) },
                            onIncrement = { viewModel.updateQuantity(state.quantity + 1) }
                        )
                        Spacer(Modifier.width(16.dp))
                        val inCart = homeState.cartItems.any { it.menu_item_id == item.id }
                        Button(
                            onClick = { 
                                if (inCart) onCartClick()
                                else homeViewModel.updateCartItemQuantity(item, state.quantity)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                        ) {
                            Text(if (inCart) "Go to Cart" else "Add to Cart • ₹${"%.2f".format(item.price * state.quantity)}")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading || item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = primaryRed) }
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
                    Text(item.product_catalog?.name ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text("₹${item.price}", style = MaterialTheme.typography.headlineSmall.copy(color = primaryRed))
                    Spacer(Modifier.height(12.dp))
                    Text(item.product_catalog?.description ?: "")
                    Spacer(Modifier.height(20.dp))
                    
                    if (item.product_catalog?.nutritional_info != null) {
                        Text("Nutritional Info", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val info = item.product_catalog.nutritional_info
                            listOf("Protein" to info.protein, "Energy" to info.energy, "Carbs" to info.carbs, "Fat" to info.fat).forEach { (label, value) ->
                                NutritionChip(label, value, Modifier.weight(1f))
                            }
                        }
                    }
                    
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
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.user_name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            RatingChip(rating = review.rating.toDouble())
        }
        Text(review.comment, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        if (review.img_url.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(review.img_url) { url ->
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
fun NutritionChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun FoodImageSlider(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
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
