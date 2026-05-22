package com.jp.foodyvilla.presentation.screens.reviews

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    productId: Long = 0L,
    orderId: String? = null,
    outletId: Long? = null,
    viewModel: ReviewsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.addState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addImage(it) }
    }

    // ✅ Auto back on success
    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetAddState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Review ✍️") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 🧾 FORM CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 👤 Name (Pre-filled or Product Context)
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Review Title / Name") },
                        placeholder = { Text("e.g. Delicious Burger!") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // ⭐ Rating
                    Column {
                        Text("Rating", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { viewModel.onRatingChange(star) }) {
                                    Icon(
                                        imageVector = if (star <= state.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA000)
                                    )
                                }
                            }
                        }
                    }

                    // 💬 Description
                    OutlinedTextField(
                        value = state.desc,
                        onValueChange = viewModel::onDescChange,
                        label = { Text("Write your review...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 🖼️ IMAGE SECTION
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Photos", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable { launcher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.padding(24.dp))
                            }
                        }
                        items(state.images) { uri ->
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.removeImage(uri) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // 🚀 SUBMIT BUTTON
            Button(
                onClick = { 
                    viewModel.submit(
                        context = context,
                        productId = if (productId != 0L) productId else null,
                        orderId = orderId,
                        outletId = outletId,
                        reviewType = when {
                            orderId != null -> "order"
                            productId != 0L -> "product"
                            else -> "outlet"
                        }
                    ) 
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading && state.rating > 0
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Submit Review", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
