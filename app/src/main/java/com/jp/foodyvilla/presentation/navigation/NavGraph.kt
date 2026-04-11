package com.jp.foodyvilla.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla.presentation.screens.MainScreen
import com.jp.foodyvilla.presentation.screens.detail.DetailScreen
import com.jp.foodyvilla.presentation.screens.home.HomeScreen

import com.jp.foodyvilla.presentation.screens.splash.SplashScreen

@Composable
fun FoodyVillaNavGraph() {
    val navController = rememberNavController()
    NavHost(
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
            MainScreen(navController  = navController)
//            HomeScreen(
//                onItemClick = { itemId -> navController.navigate(Screen.Detail(itemId)) },
//                onCartClick = { navController.navigate(Screen.Cart) }
//            )
//
        }
//
        composable<Screen.Detail> { backStack ->
            val detail: Screen.Detail = backStack.toRoute()
            DetailScreen(
                itemId = detail.itemId,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart) }
            )
        }
//
//        composable<Screen.Cart> {
//            CartScreen(
//                onBack = { navController.popBackStack() },
//                onBrowseMenu = { navController.navigate(Screen.Home) }
//            )
//        }
//
//        composable<Screen.Menu> {
//            MenuScreen(
//                onItemClick = { itemId -> navController.navigate(Screen.Detail(itemId)) }
//            )
//        }
//
//        composable<Screen.Offers> {
//            OffersScreen()
//        }
//
//        composable<Screen.Reviews> {
//            ReviewsScreen()
//        }
//
//        composable<Screen.Contact> {
//            ContactScreen(onBack = { navController.popBackStack() })
//        }
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
