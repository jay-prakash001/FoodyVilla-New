package com.jp.foodyvilla.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla.presentation.screens.MainScreen
import com.jp.foodyvilla.presentation.screens.cart.CartScreen
import com.jp.foodyvilla.presentation.screens.detail.DetailScreen
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.menuOnline.OrderOnlineScreen
import com.jp.foodyvilla.presentation.screens.reviews.AddReviewScreen
import com.jp.foodyvilla.presentation.screens.splash.SplashScreen
import com.jp.foodyvilla.presentation.utils.RequestNotificationPermission
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodyVillaNavGraph() {
    val navController = rememberNavController()
    val homeViewModel = koinViewModel<HomeViewModel>()

    NavHost(modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            MainScreen(navController = navController, viewModel = homeViewModel)

        }

        composable<Screen.Detail> { backStack ->
            val detail: Screen.Detail = backStack.toRoute()
            DetailScreen(
                itemId = detail.itemId,
                onBack = { navController.popBackStack() },

                onItemClick = {navController.navigate(Screen.Detail(it))},
                onCartClick = { navController.navigate(Screen.Cart) }, homeViewModel = homeViewModel
            )
        }

        composable<Screen.OnLineMenu> { backStack ->
            OrderOnlineScreen(onBackClick = {navController.navigateUp()})
        }
//
        composable<Screen.Cart> {
            CartScreen(
                onBack = { navController.popBackStack() },
                onBrowseMenu = { navController.navigate(Screen.Home) }, viewModel = homeViewModel
            )
        }

        composable<Screen.AddReviews> {
            AddReviewScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }


//
//        composable<Screen.Login> {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate(Screen.Home) {
//                        popUpTo(Screen.Login) { inclusive = true }
//                    }
//                },
//                onNavigateToRegister = { navController.navigate(Screen.Register) }
//            )
//        }
//
//        composable<Screen.Register> {
//            RegisterScreen(
//                onRegisterSuccess = {
//                    navController.navigate(Screen.Home) {
//                        popUpTo(Screen.Register) { inclusive = true }
//                    }
//                },
//                onNavigateToLogin = { navController.popBackStack() }
//            )
//        }
//
//        composable<Screen.Profile> {
//            ProfileScreen(
//                onSignOut = {
//                    navController.navigate(Screen.Login) {
//                        popUpTo(0) { inclusive = true }
//                    }
//                }
//            )
//        }
    }
}
