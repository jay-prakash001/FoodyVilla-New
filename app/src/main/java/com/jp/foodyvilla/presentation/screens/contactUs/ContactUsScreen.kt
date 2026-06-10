package com.jp.foodyvilla.presentation.screens.contactUs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel

// ─── CONSTANTS ─────────────────────────────────────────────────────────────
const val PHONE_NUMBER = "7067371183"
private const val EMAIL_ADDRESS = "foodyvilla.in@gmail.com"
private const val WHATSAPP_URL = "https://wa.me/917067371183"
private const val WEBSITE_URL = "https://www.foodyvilla.in"
const val MAP_LAT = 20.348417
const val MAP_LNG = 81.959333
const val MAP_LABEL = "Foodyvilla+Nagri+Chhattisgarh"
private const val INSTAGRAM_URL = "https://instagram.com/mrfoodyvilla"
private const val HERO_IMAGE_URL = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1000&auto=format&fit=crop"

// ─── DATA MODELS ────────────────────────────────────────────────────────────
data class ContactTile(
    val icon: ImageVector,
    val label: String,
    val subtitle: String,
    val color: Color,
    val action: (Context) -> Unit
)

data class HourEntry(val day: String, val time: String, val isToday: Boolean = false)

// ─── MAIN SCREEN ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val homeState by viewModel.uiState.collectAsStateWithLifecycle()

    val tiles = remember {
        listOf(
            ContactTile(Icons.Rounded.Call, "Call Us", "+91 $PHONE_NUMBER", Color(0xFF4CAF50)) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$PHONE_NUMBER")))
            },
            ContactTile(Icons.Rounded.Chat, "WhatsApp", "Instant Chat", Color(0xFF25D366)) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL)))
            },
            ContactTile(Icons.Rounded.Email, "Email Us", "Support Mail", Color(0xFFE53935)) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$EMAIL_ADDRESS")
                    putExtra(Intent.EXTRA_SUBJECT, "Inquiry – Foodyvilla App")
                })
            },
            ContactTile(Icons.Rounded.Language, "Website", "Visit Portal", Color(0xFF1976D2)) { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
            }
        )
    }

    val hours = listOf(
        HourEntry("Mon - Sat", "11:30 AM – 10:30 PM", true),
        HourEntry("Sunday", "11:30 AM – 11:00 PM")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Get in Touch", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                   IconButton(onClick = {
                       try {
                           context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL)))
                       } catch (e: Exception) {
                       }
                   }) {
                       Icon(Icons.Rounded.CameraAlt, contentDescription = "Instagram")
                   }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
//            HeroHeader()

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .offset(y = (-30).dp)
            ) {
                ContactQuickActions(tiles, context)

                if (homeState.outlets.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    OutletsSection(homeState.outlets, context)
                }

                Spacer(Modifier.height(24.dp))
                LocationCard(context)

                Spacer(Modifier.height(24.dp))
                OpeningHoursCard(hours)

                Spacer(Modifier.height(24.dp))
                SocialConnect(context)

                Spacer(Modifier.height(40.dp))
                Footer()
            }
        }
    }
}

@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = HERO_IMAGE_URL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 100f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 45.dp)
        ) {
            Text(
                "Foodyvilla",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                "Premium Indian & Chinese Cuisine",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ContactQuickActions(tiles: List<ContactTile>, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tiles.forEach { tile ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { tile.action(context) }
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tile.color.copy(alpha = 0.1f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(tile.icon, null, tint = tile.color, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tile.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OutletsSection(outlets: List<Outlet>, context: Context) {
    Column {
        Text(
            "Our Outlets",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(outlets) { outlet ->
                OutletContactCard(outlet, context)
            }
        }
    }
}

@Composable
private fun OutletContactCard(outlet: Outlet, context: Context) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable {
                outlet.phone?.let {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(120.dp)) {
                AsyncImage(
                    model = outlet.banner_url ?: outlet.logo_url,
                    contentDescription = outlet.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (outlet.is_active) "Open" else "Closed",
                        color = if (outlet.is_active) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = outlet.logo_url,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        outlet.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    outlet.address ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val uri = Uri.parse("geo:${outlet.lat},${outlet.lng}?q=${outlet.lat},${outlet.lng}(${outlet.name})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Rounded.Directions, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Directions", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    IconButton(
                        onClick = {
                             outlet.phone?.let {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                            }
                        },
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Call, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCard(context: Context) {
    Column {
        Text(
            "Main Office",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Map,
                        null,
                        modifier = Modifier.size(60.dp).graphicsLayer(alpha = 0.2f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Text("Open in Google Maps", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Column(Modifier.padding(20.dp)) {
                    Text("Nagri Main Road", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Text("Dhamtari, Chhattisgarh - 493778", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:$MAP_LAT,$MAP_LNG?q=$MAP_LAT,$MAP_LNG($MAP_LABEL)")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Directions, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Navigation")
                    }
                }
            }
        }
    }
}

@Composable
private fun OpeningHoursCard(hours: List<HourEntry>) {
    Column {
        Text(
            "Service Hours",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(20.dp)) {
                hours.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Schedule,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = if (entry.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                entry.day,
                                fontWeight = if (entry.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text(
                            entry.time,
                            fontWeight = if (entry.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (entry.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (index < hours.size - 1) HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SocialConnect(context: Context) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(0.4f),
        shape = RoundedCornerShape(24.dp),
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Craving something delicious? Check out Foodyvilla! $WEBSITE_URL")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Spread the Word"))
        }
    ) {
        Row(
            Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ThumbUp, null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Love our food?", fontWeight = FontWeight.Bold)
                Text("Share your experience with friends", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ArrowForwardIos, null, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun Footer() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Thank you for choosing Foodyvilla",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Crafted with ❤️ in Chhattisgarh",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "App Version 1.0.8",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline.copy(0.5f)
        )
    }
}