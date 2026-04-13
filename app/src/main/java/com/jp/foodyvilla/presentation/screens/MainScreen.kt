package com.jp.foodyvilla.presentation.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla.presentation.components.FoodyVillaNavBar
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.screens.home.HomeScreen
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.menu.MenuScreen
import com.jp.foodyvilla.presentation.screens.offers.OffersScreen
import com.jp.foodyvilla.presentation.screens.reviews.ReviewsScreen
import org.koin.androidx.compose.koinViewModel


@Composable
fun MainScreen(modifier: Modifier = Modifier, navController: NavController, viewModel: HomeViewModel = koinViewModel()) {
    val selectedPage = viewModel.selectedPage.collectAsStateWithLifecycle().value


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FoodyVillaNavBar(
                selectedPage = selectedPage,
                onPageChange = { viewModel.updateSelectedPage(it) },
                modifier = modifier
                    .fillMaxWidth()
                    .height(84.dp)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding()) ) {

            // MenuWebViewScreen stays alive in the background — never recomposed away


            // Overlay the other pages on top, just hide them when not needed
            when (selectedPage) {
                0 -> HomeScreen({ itemId ->
                    navController.navigate(Screen.Detail(itemId))
                }, {}, viewModel)

                1 -> MenuScreen(navController = navController,onItemClick = { navController.navigate(Screen.Detail(it)) })
//                1-> OrderOnlineScreen({ selectedPage = 0})
                2 -> OffersScreen()
                3 -> ReviewsScreen(){
                    navController.navigate(Screen.AddReviews)
                }
//                3 -> ContactUsScreen()
                else -> HomeScreen({}, {},viewModel)

            }
        }
    }
}