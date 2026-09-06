package com.jp.foodyvilla.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home),
    BottomNavItem("Menu", Icons.Filled.Menu),
    BottomNavItem("Offers", Icons.Outlined.LocalOffer),
    BottomNavItem("Review", Icons.Filled.Reviews),
    BottomNavItem("Orders", Icons.Filled.ShoppingBag),
)

@Composable
fun FoodyVillaNavBar(
    selectedPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme

    // Store tab positions to animate the sliding pill
    val tabPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

    val targetX = tabPositions[selectedPage]?.first ?: 0f
    val targetWidth = tabPositions[selectedPage]?.second ?: 0f

    val pillX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "pillX"
    )

    val pillWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "pillWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Standard BottomBar height
            .padding(horizontal = 24.dp, vertical = 12.dp) // Floating effect
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Sliding Selection Pill
        if (pillWidth > 0f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(pillX.toInt(), 0) }
                    .width(with(density) { pillWidth.toDp() })
                    .fillMaxHeight()
                    .padding(4.dp) // Inner spacing so pill doesn't touch edges
                    .background(colors.primary, RoundedCornerShape(20.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavTab(
                    item = item,
                    selected = selectedPage == index,
                    onClick = { onPageChange(index) },
                    modifier = Modifier.weight(1f),
                    onMeasured = { x, w -> tabPositions[index] = Pair(x, w) }
                )
            }
        }
    }
}

@Composable
private fun NavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasured: (x: Float, width: Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // Animate content color for a smooth transition
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.7f),
        label = "color"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                onMeasured(coords.positionInParent().x, coords.size.width.toFloat())
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = item.label,
                style = if (selected)
                    MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
