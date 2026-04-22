package com.jp.foodyvilla.presentation.navigation

import android.widget.Button
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jp.foodyvilla.presentation.screens.MainScreen
import com.jp.foodyvilla.presentation.screens.cart.CartScreen
import com.jp.foodyvilla.presentation.screens.detail.DetailScreen
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.screens.login.MobileLoginScreen
import com.jp.foodyvilla.presentation.screens.login.OtpVerificationScreen
import com.jp.foodyvilla.presentation.screens.menuOnline.OrderOnlineScreen
import com.jp.foodyvilla.presentation.screens.reviews.AddReviewScreen
import com.jp.foodyvilla.presentation.screens.splash.SplashScreen
import io.github.jan.supabase.SupabaseClient
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
                    onSplashComplete = {
                        if (isLoggedIn) {
                            navController.navigate(Screen.Home) {
                                popUpTo(Screen.Splash) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Login) {
                                popUpTo(Screen.Splash) { inclusive = true }
                            }
//                        Toast.make/Text(context, "Unable to login", Toast.LENGTH_SHORT).show()
                        }


                    }
                )
            }


            composable<Screen.Login> {

                MobileLoginScreen(loginViewModel = loginViewModel,
                    navController = navController,
                    onGetOtp =  {
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
