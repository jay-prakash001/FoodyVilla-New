package com.jp.foodyvilla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.jp.foodyvilla.presentation.navigation.FoodyVillaNavGraph
import com.jp.foodyvilla.presentation.screens.login.GoogleSignInScreen
import com.jp.foodyvilla.presentation.utils.HideSystemBars
import com.jp.foodyvilla.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
//                HideSystemBars()
                FoodyVillaNavGraph()
//            val context = LocalContext.current
//                GoogleSignInScreen(context)

            }
        }
    }
}
