package com.jp.foodyvilla.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(1800)
        onSplashComplete()
    }


    FoodyVillaSplash()
}



@Composable
fun FoodyVillaSplash() {
    val color = MaterialTheme.colorScheme
    val inf = rememberInfiniteTransition(label = "splash")

    val ring1Scale by inf.animateFloat(0.5f, 1.9f, infiniteRepeatable(tween(1700, easing = EaseOut), RepeatMode.Restart), label = "r1s")
    val ring1Alpha by inf.animateFloat(0.4f, 0f,   infiniteRepeatable(tween(1700, easing = EaseOut), RepeatMode.Restart), label = "r1a")
    val ring2Scale by inf.animateFloat(0.5f, 1.9f, infiniteRepeatable(tween(1700, easing = EaseOut, delayMillis = 600), RepeatMode.Restart), label = "r2s")
    val ring2Alpha by inf.animateFloat(0.4f, 0f,   infiniteRepeatable(tween(1700, easing = EaseOut, delayMillis = 600), RepeatMode.Restart), label = "r2a")
    val dotRot     by inf.animateFloat(0f, 360f,   infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "dot")
    val textAlpha  by inf.animateFloat(0.45f, 1f,  infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse), label = "txt")

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(190.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val c     = Offset(size.width / 2, size.height / 2)
                    val baseR = size.minDimension * 0.27f
                    drawCircle(color.primaryContainer.copy(ring1Alpha), baseR * ring1Scale, c)
                    drawCircle(color.onPrimaryContainer.copy(ring2Alpha),  baseR * ring2Scale, c)

                    val orbitR = baseR * 0.96f
                    val angle  = Math.toRadians(dotRot.toDouble())
                    val dp     = Offset(c.x + orbitR * cos(angle).toFloat(), c.y + orbitR * sin(angle).toFloat())
                    drawCircle(color.primaryContainer, 7.dp.toPx(), dp)
                    drawCircle(Color.White.copy(0.5f), 3.dp.toPx(), dp)
                    drawCircle(color.onPrimaryContainer, orbitR, c, style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                }

                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(color.primaryContainer.copy(0.38f), color.surfaceContainer.copy(0.7f)))
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(listOf(Color.White.copy(0.4f), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) { Text("🍽️", fontSize = 40.sp) }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                "FoodyVilla",
                fontSize      = 34.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = color.onPrimaryContainer,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Getting your menu ready",
                fontSize      = 13.sp,
                color         = color.onPrimary.copy(alpha = textAlpha),
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(52.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(0, 280, 560).forEach { delay ->
                    val a by rememberInfiniteTransition(label = "d$delay").animateFloat(
                        0.18f, 1f,
                        infiniteRepeatable(tween(550, delayMillis = delay, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "da"
                    )
                    Box(Modifier.size(7.dp).clip(CircleShape).background(color.onPrimaryContainer.copy(a)))
                }
            }
        }
    }
}

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme
    val inf = rememberInfiniteTransition(label = "bg")
    val shift by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "shift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(color.primaryContainer, color.primary)))

        val b1x = size.width * (0.78f + sin(shift * 2 * PI.toFloat()) * 0.08f)
        val b1y = size.height * (0.15f + cos(shift * 2 * PI.toFloat()) * 0.06f)
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(color.primary.copy(0.28f), Color.Transparent),
                center = Offset(b1x, b1y), radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(b1x, b1y)
        )

        val b2x = size.width * (0.18f + cos(shift * 2 * PI.toFloat()) * 0.07f)
        val b2y = size.height * (0.78f + sin(shift * 2 * PI.toFloat() + 1f) * 0.08f)
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(color.primaryContainer.copy(0.18f), Color.Transparent),
                center = Offset(b2x, b2y), radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(b2x, b2y)
        )

        val step = 48.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(Color.White.copy(0.022f), Offset(x, 0f), Offset(x, size.height), 0.5f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(Color.White.copy(0.022f), Offset(0f, y), Offset(size.width, y), 0.5f)
            y += step
        }
    }
}