package com.jp.foodyvilla.presentation.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView

import androidx.compose.runtime.SideEffect


@Composable
fun HideSystemBars() {
    val context = LocalContext.current
    val view = LocalView.current

    // SideEffect runs every time the composition is successful
    // ensuring the bars stay hidden even if something else tries to show them
    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)

        // This is the core logic to hide bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}