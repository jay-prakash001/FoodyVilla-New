package com.jp.foodyvilla.presentation.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun HideSystemBars() {
    val context = LocalContext.current

    // Safely cast to Activity
    val activity = context as? Activity ?: return
    val window = activity.window
    val decorView = window.decorView

    LaunchedEffect(Unit) {
        // 1. Tell the window not to fit system windows (Enables Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, decorView)

        // 2. Hide both Status and Navigation bars
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // 3. Reveal temporarily on swipe without resizing the layout layout
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
