package com.jp.foodyvilla.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jp.foodyvilla.presentation.components.FoodyVillaNavBar
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.screens.home.HomeScreen
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.menu.MenuScreen
import com.jp.foodyvilla.presentation.screens.offers.OffersScreen
import com.jp.foodyvilla.presentation.screens.orders.OrderHistoryScreen
import com.jp.foodyvilla.presentation.screens.reviews.ReviewsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        // Handle result if needed
    }

    LaunchedEffect(Unit) {
        viewModel.getCartItems()
    }

    val selectedPage = viewModel.selectedPage.collectAsStateWithLifecycle().value
    val homeState = viewModel.uiState.collectAsStateWithLifecycle().value
    val titles = listOf(
        "Good day, Foodie 👋",
        "Order Menu",
        "Offers",
        "Reviews",
        "Contact Us"
    )
    val title = titles.getOrElse(selectedPage) { "Foody Villa" }

    Scaffold(
        containerColor = colors.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectedPage != 0) {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.CustomerSupport) }) {
                            Icon(
                                Icons.Default.SupportAgent,
                                contentDescription = "Customer Support",
                                tint = colors.onSurface
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Cart) }) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = colors.onSurface
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Profile) }) {
                            Icon(
                                Icons.Default.Person3,
                                contentDescription = "Profile",
                                tint = colors.onSurface
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ZomatoCartBar(
                    cartItemCount = homeState.cartItems.size,
                    totalPrice = homeState.cartItems.sumOf { it.totalPrice ?: 0.0 },
                    items = homeState.cartItems.mapNotNull {
                        it.outlet_menu_items?.image?.firstOrNull()
                    },
                    onClick = { navController.navigate(Screen.Cart) }
                )
                FoodyVillaNavBar(
                    selectedPage = selectedPage,
                    onPageChange = { viewModel.updateSelectedPage(it) },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(84.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (selectedPage != 0) innerPadding.calculateTopPadding() else 0.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedPage) {
                0 -> HomeScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail(itemId))
                    },
                    viewModel = viewModel,
                    onMenuClick = {
                        viewModel.updateSelectedPage(1)
                    },
                    onSupportClick = { navController.navigate(Screen.CustomerSupport) },
                    onCartClick = { navController.navigate(Screen.Cart) },
                    onProfileClick = { navController.navigate(Screen.Profile) }
                )

                1 -> MenuScreen(
                    viewModel = viewModel,
                    onItemClick = { navController.navigate(Screen.Detail(it)) }
                )

                2 -> OffersScreen()
                3 -> ReviewsScreen {
                    navController.navigate(Screen.AddReviews())
                }

                4 -> OrderHistoryScreen(viewModel = viewModel) { productId ->
                    navController.navigate(Screen.AddReviews(productId = productId))
                }

                else -> HomeScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail(itemId))
                    },
                    viewModel = viewModel,
                    onMenuClick = {
                        viewModel.updateSelectedPage(1)
                    },
                    onSupportClick = { navController.navigate(Screen.CustomerSupport) },
                    onCartClick = { navController.navigate(Screen.Cart) },
                    onProfileClick = { navController.navigate(Screen.Profile) }
                )
            }
        }
    }
}

@Composable
fun ZomatoCartBar(
    cartItemCount: Int,
    totalPrice: Double,
    items: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val barColor = colors.primary
    val contentColor = colors.onPrimary

    AnimatedVisibility(
        visible = cartItemCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(20.dp),
                color = barColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.height(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.take(3).forEachIndexed { index, imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .offset(x = (-30 * index).dp)
                                    .zIndex((5 - index).toFloat())
                                    .border(2.dp, barColor, CircleShape)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    // Center — "View Cart"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "View Cart",
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.3.sp
                        )
                    }

                    // Right — price + arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹%.0f".format(totalPrice),
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
