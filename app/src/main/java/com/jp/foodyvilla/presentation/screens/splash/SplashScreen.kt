package com.jp.foodyvilla.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla.R
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val isLoggedIn = loginViewModel.isLoggedIn.collectAsStateWithLifecycle().value

    var splashFinished by remember {
        mutableStateOf(false)
    }

    // Minimum splash duration
    LaunchedEffect(Unit) {
        delay(1800)
        splashFinished = true
    }

    // Navigate only after BOTH:
    // 1. splash completed
    // 2. auth state received
    LaunchedEffect(isLoggedIn, splashFinished) {
        if (!splashFinished) return@LaunchedEffect

        when (isLoggedIn) {
            is UiState.Success -> {
                if (isLoggedIn.data) {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Splash) {
                            inclusive = true
                        }
                    }
                } else {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Splash) {
                            inclusive = true
                        }
                    }
                }
            }

            is UiState.Error -> {
                navController.navigate(Screen.Login) {
                    popUpTo(Screen.Splash) {
                        inclusive = true
                    }
                }
            }

            else -> Unit
        }
    }

    NewSplashScreen()
}

@Composable
fun NewSplashScreen() {
    val isDark = isSystemInDarkTheme()
    val splashBg = if (isDark) R.drawable.dark_splash_bg else R.drawable.light_splash_bg

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = splashBg),
            contentDescription = "Splash Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SplashScreen0() {
    /* entry scale + fade */
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    /* plate gentle rocking */
    val infiniteT = rememberInfiniteTransition(label = "splash")
    val plateRock by infiniteT.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "plateRock"
    )

    /* loading bar */
    var barTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { delay(400); barTarget = 1f }
    val barProg by animateFloatAsState(
        targetValue = barTarget,
        animationSpec = tween(2_000, easing = FastOutSlowInEasing),
        label = "barProg"
    )

    Box(
        Modifier.fillMaxSize().background(Color(0xFFB7131A)),
        contentAlignment = Alignment.Center
    ) {
        /* decorative pulsing circles */
        PulsingCircle(340.dp, (-90).dp, (-200).dp, delay = 0)
        PulsingCircle(240.dp, 120.dp, 200.dp, delay = 600)
        PulsingCircle(150.dp, (-60).dp, 180.dp, delay = 1_200)

        /* logo block */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(logoScale).graphicsLayer(alpha = logoAlpha)
        ) {
            /* plate circle */
            Box(
                Modifier.size(200.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_new),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.75f),
                    modifier = Modifier.size(180.dp).graphicsLayer(rotationZ = plateRock)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "TASTE THE DIFFERENCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = White.copy(alpha = 0.75f),
                    letterSpacing = 3.sp
                )
            )

            Spacer(Modifier.height(60.dp))

            /* loading bar */
            Box(
                Modifier.width(160.dp).height(3.dp).clip(CircleShape).background(White.copy(alpha = 0.2f))
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(barProg).clip(CircleShape).background(
                        White
                    )
                )
            }
        }
    }
}

@Composable
private fun PulsingCircle(size: Dp, offsetX: Dp, offsetY: Dp, delay: Int) {
    val inf = rememberInfiniteTransition(label = "circle$delay")
    val sc by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(2_000, delayMillis = delay, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sc$delay"
    )
    val al by inf.animateFloat(
        1f, 0.5f,
        infiniteRepeatable(tween(2_000, delayMillis = delay, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "al$delay"
    )
    Box(
        Modifier
            .size(size)
            .offset(offsetX, offsetY)
            .scale(sc)
            .graphicsLayer(alpha = al)
            .clip(CircleShape)
            .background(Color.White.copy(.2f))
    )
}
