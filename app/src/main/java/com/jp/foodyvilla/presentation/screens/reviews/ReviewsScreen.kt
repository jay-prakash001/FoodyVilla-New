package com.jp.foodyvilla.presentation.screens.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.Review
import com.jp.foodyvilla.presentation.screens.home.RatingChip
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel = koinViewModel(),
    onAddReviewClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Customer Reviews",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        // Removed FAB as requested
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.reviews.isEmpty()) {
            // Empty State UI
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reviews yet. Share your experience after ordering!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ⭐ Overall Summary Card
                val avg = state.reviews.map { it.rating }.average()
                item {
                    SummaryCard(avg = avg, totalReviews = state.reviews.size)
                }

                items(state.reviews, key = { it.id }) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(avg: Double, totalReviews: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(avg),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$totalReviews reviews",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (5 downTo 1).forEach { star ->
                    StarRow(star.toDouble(), 100) // Mock progress for now
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // User Name or Title
                Text(
                    text = review.title ?: "Anonymous",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                RatingChip(rating = review.rating.toDouble())
            }
            
            Text(
                text = review.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!review.img_url.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    review.img_url.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            Text(
                text = review.created_at.substringBefore("T"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun StarRow(star: Double, progress: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = star.toInt().toString(), style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(
            progress = { 0.5f }, // Mock
            modifier = Modifier.width(100.dp).height(6.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
