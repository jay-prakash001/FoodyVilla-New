package com.jp.foodyvilla.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla.presentation.screens.MainScreen
import com.jp.foodyvilla.presentation.screens.account.ProfileScreen
import com.jp.foodyvilla.presentation.screens.cart.CartScreen
import com.jp.foodyvilla.presentation.screens.cart.DetailAddScreen
import com.jp.foodyvilla.presentation.screens.cart.PaymentScreen
import com.jp.foodyvilla.presentation.screens.contactUs.ContactUsScreen
import com.jp.foodyvilla.presentation.screens.detail.DetailScreen
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.screens.login.MobileLoginScreen
import com.jp.foodyvilla.presentation.screens.login.OtpVerificationScreen
import com.jp.foodyvilla.presentation.screens.menuOnline.OrderOnlineScreen
import com.jp.foodyvilla.presentation.screens.reviews.AddReviewScreen
import com.jp.foodyvilla.presentation.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodyVillaNavGraph() {
    val navController = rememberNavController()
    val homeViewModel = koinViewModel<HomeViewModel>()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val context = LocalContext.current
    val isLoggedIn = loginViewModel.isLoggedIn.collectAsStateWithLifecycle().value

    val homeState = homeViewModel.uiState.collectAsStateWithLifecycle().value


    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Screen.Splash
    )
    {
        composable<Screen.Splash> {
            SplashScreen(
                loginViewModel = loginViewModel, navController = navController
            )
        }


        composable<Screen.Login> {

            MobileLoginScreen(
                loginViewModel = loginViewModel,
                navController = navController,
                onGetOtp = {
                    loginViewModel.updateOtp("")
                    loginViewModel.login()
                })
        }

        composable<Screen.Otp> {
            val maskedPhone = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value
            OtpVerificationScreen(
                maskedPhone = maskedPhone.dropLast(4) + "****",
                loginViewModel = loginViewModel,
                navController = navController,
                onVerify = {
                    loginViewModel.login(otp = it)
//                    navController.navigate(Screen.Home)
                },
            ) {
                loginViewModel.updateOtp("")
                loginViewModel.login()
            }
        }

        composable<Screen.Home> {
            MainScreen(navController = navController, viewModel = homeViewModel)


        }

        composable<Screen.Detail> { backStack ->
            val detail: Screen.Detail = backStack.toRoute()
            DetailScreen(
                itemId = detail.itemId,
                onBack = { navController.popBackStack() },

                onItemClick = { navController.navigate(Screen.Detail(it)) },
                onCartClick = { navController.navigate(Screen.Cart) }, homeViewModel = homeViewModel
            )
        }

        composable<Screen.OnLineMenu> { backStack ->
            OrderOnlineScreen(onBackClick = { navController.navigateUp() })
        }
//
        composable<Screen.Cart> {
            CartScreen(
                onBack = { navController.popBackStack() },
                onBrowseMenu = { navController.navigate(Screen.Home) },
                onCheckoutOutlet = { outletId ->
                    navController.navigate(Screen.DetailAdd(outletId))
                },
                viewModel = homeViewModel,
                loginViewModel = loginViewModel
            )
        }

        composable<Screen.DetailAdd> { backStack ->
            val args: Screen.DetailAdd = backStack.toRoute()
            DetailAddScreen(
                outletId = args.outletId,
                onBack = { navController.popBackStack() },
                onProceedToPayment = { outletId ->
                    navController.navigate(Screen.Payment(outletId))
                },
                viewModel = homeViewModel,
                loginViewModel = loginViewModel
            )
        }

        composable<Screen.Payment> { backStack ->
            val args: Screen.Payment = backStack.toRoute()
            PaymentScreen(
                outletId = args.outletId,
                onBack = { navController.popBackStack() },
                onOrderSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                viewModel = homeViewModel
            )
        }



        composable<Screen.CustomerSupport> {
            ContactUsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = homeViewModel
            )
        }
        composable<Screen.AddReviews> { backStack ->
            val args: Screen.AddReviews = backStack.toRoute()
            AddReviewScreen(
                productId = args.productId,
                orderId = args.orderId,
                outletId = args.outletId,
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
        composable<Screen.Profile> {
            ProfileScreen(
                viewModel = loginViewModel,
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }


}
