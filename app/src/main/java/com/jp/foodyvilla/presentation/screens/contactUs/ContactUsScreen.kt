//package com.jp.foodyvilla.presentation.screens
//



package com.jp.foodyvilla.presentation.screens.contactUs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  CONSTANTS
// ─────────────────────────────────────────────────────────────────────────────
const val PHONE_NUMBER   = "7067371183"
private const val EMAIL_ADDRESS  = "foodyvilla.in@gmail.com"
private const val WHATSAPP_URL   = "https://wa.me/917067371183"
private const val WEBSITE_URL    = "https://www.foodyvilla.in"
const val MAP_LAT        = 20.348417
const val MAP_LNG        = 81.959333
const val MAP_LABEL      = "Foodyvilla+Nagri+Chhattisgarh"


// ─────────────────────────────────────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────────────────────────────────────

data class ContactTile(
    val icon          : ImageVector,
    val label         : String,
    val subtitle      : String,
    val gradientStart : Color,
    val gradientEnd   : Color,
    val action        : (Context) -> Unit
)

data class HourEntry(
    val day    : String,
    val time   : String,
    val isToday: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit = {}) {
    val context = LocalContext.current

    val tiles = listOf(
        ContactTile(
            icon          = Icons.Rounded.Call,
            label         = "Call Us",
            subtitle      = "+91 $PHONE_NUMBER",
            gradientStart = GradCallStart,
            gradientEnd   = GradCallEnd,
            action        = { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$PHONE_NUMBER")))
            }
        ),
        ContactTile(
            icon          = Icons.Rounded.Email,
            label         = "Email Us",
            subtitle      = "Drop us a mail",
            gradientStart = GradEmailStart,
            gradientEnd   = GradEmailEnd,
            action        = { ctx ->
                ctx.startActivity(
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$EMAIL_ADDRESS")
                        putExtra(Intent.EXTRA_SUBJECT, "Inquiry – Foodyvilla")
                    }
                )
            }
        ),
        ContactTile(
            icon          = Icons.Rounded.Chat,
            label         = "WhatsApp",
            subtitle      = "Chat with us",
            gradientStart = GradWhatsAppStart,
            gradientEnd   = GradWhatsAppEnd,
            action        = { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL)))
            }
        ),
        ContactTile(
            icon          = Icons.Rounded.Language,
            label         = "Website",
            subtitle      = "foodyvilla.in",
            gradientStart = GradWebStart,
            gradientEnd   = GradWebEnd,
            action        = { ctx ->
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
            }
        )
    )

    val hours = listOf(
        HourEntry("Monday",    "11:30 AM – 10:30 PM"),
        HourEntry("Tuesday",   "11:30 AM – 10:30 PM"),
        HourEntry("Wednesday", "11:30 AM – 10:30 PM"),
        HourEntry("Thursday",  "11:30 AM – 10:30 PM"),
        HourEntry("Friday",    "11:30 AM – 10:30 PM"),
        HourEntry("Saturday",  "11:30 AM – 10:30 PM"),
        HourEntry("Sunday",    "11:30 AM – 10:30 PM")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Contact Us",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            HeroBanner()
            Spacer(Modifier.height(24.dp))

            SectionLabel("Get In Touch", Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            ContactTilesGrid(tiles = tiles, context = context)

            Spacer(Modifier.height(20.dp))
            ShareStrip(context, Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(24.dp))
            SectionLabel("Our Location", Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            LocationCard(context, Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(24.dp))
            SectionLabel("Opening Hours", Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            HoursCard(hours, Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(32.dp))
            FooterStrip()
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HERO BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Decorative background orbs
        Box(
            Modifier
                .size(160.dp)
                .offset(x = 230.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.06f))
        )
        Box(
            Modifier
                .size(90.dp)
                .offset(x = 280.dp, y = 50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.04f))
        )

        Column {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text       = "🍛  Indian & Chinese Cuisine",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("🥘", fontSize = 28.sp) }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text  = "Foodyvilla",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text  = "Nagri, Chhattisgarh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text       = "Where every bite is a celebration of\nflavour, spice & tradition.",
                style      = MaterialTheme.typography.bodySmall,
                color      = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("4.8 ★" to "Rating", "80+" to "Dishes", "5K+" to "Orders").forEach { (value, label) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                            Text(label, style = MaterialTheme.typography.labelSmall,  color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SECTION LABEL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CONTACT TILES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ContactTilesGrid(tiles: List<ContactTile>, context: Context) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { tile ->
                    ContactTileCard(tile, Modifier.weight(1f)) { tile.action(context) }
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ContactTileCard(tile: ContactTile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick        = onClick,
        modifier       = modifier.shadow(
            elevation    = 4.dp,
            shape        = RoundedCornerShape(20.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            spotColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        shape          = RoundedCornerShape(20.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Colorful gradient icon — always vibrant regardless of theme
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(tile.gradientStart, tile.gradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(tile.icon, contentDescription = tile.label, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(tile.label,    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(tile.subtitle, style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Gradient action pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.linearGradient(listOf(tile.gradientStart, tile.gradientEnd)))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("Tap", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SHARE STRIP
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShareStrip(context: Context, modifier: Modifier = Modifier) {
    Surface(
        shape          = RoundedCornerShape(18.dp),
        color          = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 0.dp,
        modifier       = modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Love Foodyvilla?",           style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Share with friends & family", style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.70f))
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                onClick = {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT,
                                    "🍛 Try Foodyvilla in Nagri, CG! " +
                                            "Fresh Indian & Chinese daily. " +
                                            "Visit: $WEBSITE_URL | Call: +91$PHONE_NUMBER")
                            }, "Share Foodyvilla via"
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Share, "Share", tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LOCATION CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LocationCard(context: Context, modifier: Modifier = Modifier) {
    Surface(
        shape          = RoundedCornerShape(20.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier       = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Map placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Nagri, Chhattisgarh",       style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("20°20'54.3\"N  81°57'33.6\"E", style = MaterialTheme.typography.labelSmall,  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f))
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Foodyvilla — Nagri Branch",                          style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Nagri, Dhamtari District, Chhattisgarh, India",      style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🍽 Dine-in", "🛍 Pick up", "🛵 Delivery").forEach { tag ->
                    Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick  = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("geo:$MAP_LAT,$MAP_LNG?q=$MAP_LAT,$MAP_LNG($MAP_LABEL)"))
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Rounded.Directions, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Get Directions", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HOURS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HoursCard(hours: List<HourEntry>, modifier: Modifier = Modifier) {
    Surface(
        shape          = RoundedCornerShape(20.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier       = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            hours.forEachIndexed { idx, entry ->
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(8.dp).clip(CircleShape).background(
                                if (entry.isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Text(
                            text       = entry.day,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = if (entry.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (entry.isToday) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (entry.isToday) {
                            Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("Today", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text(
                        text       = entry.time,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (entry.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (idx < hours.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FOOTER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FooterStrip() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
        Spacer(Modifier.height(16.dp))
        Text("Made with ❤️ by Foodyvilla, Nagri CG", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Privacy Policy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary,  fontWeight = FontWeight.SemiBold)
            Text("Terms of Use",  style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,    fontWeight = FontWeight.SemiBold)
            Text("v1.0.0",        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FOODYVILLA BRAND PALETTE
// ─────────────────────────────────────────────────────────────────────────────

// Primary  – warm saffron-orange (food brand anchor)
val Orange10  = Color(0xFF3A0D00)
val Orange20  = Color(0xFF5E1A00)
val Orange30  = Color(0xFF8A2E00)
val Orange40  = Color(0xFFB84400)   // light primary
val Orange80  = Color(0xFFFFB596)
val Orange90  = Color(0xFFFFDBCE)
val Orange95  = Color(0xFFFFEDE6)
val Orange99  = Color(0xFFFFFBFF)

// Secondary – turmeric-gold
val Gold10    = Color(0xFF2C1A00)
val Gold20    = Color(0xFF4A2F00)
val Gold30    = Color(0xFF6D4700)
val Gold40    = Color(0xFF915F00)
val Gold80    = Color(0xFFE9BE6A)
val Gold90    = Color(0xFFFFDEA0)
val Gold95    = Color(0xFFFFEFCF)
val Gold99    = Color(0xFFFFFBFF)

// Tertiary  – herb-green (fresh, natural)
val Green10   = Color(0xFF002111)
val Green20   = Color(0xFF003820)
val Green30   = Color(0xFF005230)
val Green40   = Color(0xFF006D40)
val Green80   = Color(0xFF6FDBA1)
val Green90   = Color(0xFF8DF7BB)
val Green95   = Color(0xFFCDFFE3)
val Green99   = Color(0xFFF5FFF5)

// Error
val Red10     = Color(0xFF410002)
val Red20     = Color(0xFF690005)
val Red40     = Color(0xFFBA1A1A)
val Red80     = Color(0xFFFFB4AB)
val Red90     = Color(0xFFFFDAD6)

// Neutral
val Neutral10 = Color(0xFF1A110E)   // darkest surface
val Neutral20 = Color(0xFF2E1F1A)
val Neutral30 = Color(0xFF44312B)
val Neutral40 = Color(0xFF5C4339)
val Neutral50 = Color(0xFF765A50)   // medium
val Neutral60 = Color(0xFF917367)
val Neutral70 = Color(0xFFAD8D80)
val Neutral80 = Color(0xFFCBA89A)
val Neutral90 = Color(0xFFE8C4B5)
val Neutral95 = Color(0xFFF7DDD1)
val Neutral99 = Color(0xFFFFFBFF)

// Neutral Variant
val NeutralVar10 = Color(0xFF1F140F)
val NeutralVar20 = Color(0xFF352823)
val NeutralVar30 = Color(0xFF4C3D38)
val NeutralVar40 = Color(0xFF65554F)
val NeutralVar50 = Color(0xFF7F6D67)
val NeutralVar60 = Color(0xFF9A8680)
val NeutralVar70 = Color(0xFFB6A09A)
val NeutralVar80 = Color(0xFFD2BBB4)
val NeutralVar90 = Color(0xFFEFD8D0)
val NeutralVar95 = Color(0xFFFDE7DF)
val NeutralVar99 = Color(0xFFFFFBFF)

// ── Colorful fixed icon gradient stops (tile-specific, theme-agnostic) ────────
val GradCallStart     = Color(0xFFFF6B35)
val GradCallEnd       = Color(0xFFFF9A70)
val GradEmailStart    = Color(0xFF7C4DFF)
val GradEmailEnd      = Color(0xFFAA90FF)
val GradWhatsAppStart = Color(0xFF00C853)
val GradWhatsAppEnd   = Color(0xFF69F0AE)
val GradWebStart      = Color(0xFF0091EA)
val GradWebEnd        = Color(0xFF64B5F6)

// ─────────────────────────────────────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────


//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//
//// ─────────────────────────────────────────────────────────────────────────────
////  RESTAURANT CONSTANTS
//// ─────────────────────────────────────────────────────────────────────────────
//
// const val PHONE_NUMBER  = "7067371183"
//private const val EMAIL_ADDRESS = "foodyvilla.in@gmail.com"
//private const val WHATSAPP_URL  = "https://wa.me/917067371183"
//private const val MAP_LAT       = 20.348417
//private const val MAP_LNG       = 81.959333
//private const val MAP_LABEL     = "Foodyvilla+Nagri+Chhattisgarh"
//
//// ─────────────────────────────────────────────────────────────────────────────
////  DATA MODELS
//// ─────────────────────────────────────────────────────────────────────────────
//
//data class ContactAction(
//    val icon        : ImageVector,
//    val label       : String,
//    val subtitle    : String,
//    val isPrimary   : Boolean = false,
//    val isSecondary : Boolean = false,
//    val action      : (Context) -> Unit
//)
//
//data class HourEntry(
//    val days   : String,
//    val time   : String,
//    val isToday: Boolean = false
//)
//
//data class SocialLink(
//    val icon : ImageVector,
//    val label: String,
//    val url  : String
//)
//
//// ─────────────────────────────────────────────────────────────────────────────
////  MAIN SCREEN
//// ─────────────────────────────────────────────────────────────────────────────
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ContactUsScreen(onNavigateBack: () -> Unit = {}) {
//    val context           = LocalContext.current
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope             = rememberCoroutineScope()
//
//    val contactActions = listOf(
//        ContactAction(
//            icon      = Icons.Filled.Call,
//            label     = "Call Us",
//            subtitle  = "+91 $PHONE_NUMBER",
//            isPrimary = true,
//            action    = { ctx ->
//                ctx.startActivity(
//                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$PHONE_NUMBER"))
//                )
//            }
//        ),
//        ContactAction(
//            icon        = Icons.Filled.Email,
//            label       = "Email Us",
//            subtitle    = EMAIL_ADDRESS,
//            isSecondary = true,
//            action      = { ctx ->
//                ctx.startActivity(
//                    Intent(Intent.ACTION_SENDTO).apply {
//                        data = Uri.parse("mailto:$EMAIL_ADDRESS")
//                        putExtra(Intent.EXTRA_SUBJECT, "Inquiry – Foodyvilla")
//                    }
//                )
//            }
//        ),
//        ContactAction(
//            icon     = Icons.Filled.Chat,
//            label    = "WhatsApp",
//            subtitle = "Chat with us",
//            action   = { ctx ->
//                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL)))
//            }
//        ),
//        ContactAction(
//            icon     = Icons.Filled.HeadsetMic,
//            label    = "Live Support",
//            subtitle = "In-app chat",
//            action   = {
//                scope.launch { snackbarHostState.showSnackbar("Opening live support…") }
//            }
//        )
//    )
//
//    val hours = listOf(
//        HourEntry("Monday",    "11:30 AM – 10:30 PM"),
//        HourEntry("Tuesday",   "11:30 AM – 10:30 PM"),
//        HourEntry("Wednesday", "11:30 AM – 10:30 PM"),
//        HourEntry("Thursday",  "11:30 AM – 10:30 PM"),
//        HourEntry("Friday",    "11:30 AM – 10:30 PM"),
//        HourEntry("Saturday",  "11:30 AM – 10:30 PM", ),
//        HourEntry("Sunday",    "11:30 AM – 10:30 PM"),
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Contact Us", fontWeight = FontWeight.SemiBold) },
//
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor             = MaterialTheme.colorScheme.primary,
//                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
//                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            )
//        },
//        snackbarHost   = { SnackbarHost(snackbarHostState) },
//        containerColor = MaterialTheme.colorScheme.background
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(paddingValues)
//        ) {
//            HeroBanner()
//            Spacer(Modifier.height(16.dp))
//
//            SectionCard("About Foodyvilla") { AboutSection() }
//            Spacer(Modifier.height(12.dp))
//
//            SectionCard("Get In Touch") {
//                ContactActionsGrid(actions = contactActions, onAction = { it.action(context) })
//                Spacer(Modifier.height(12.dp))
//                ShareButton {
//                    context.startActivity(
//                        Intent.createChooser(
//                            Intent(Intent.ACTION_SEND).apply {
//                                type = "text/plain"
//                                putExtra(
//                                    Intent.EXTRA_TEXT,
//                                    "🍛 Craving Indian & Chinese? Try Foodyvilla in Nagri, " +
//                                            "Chhattisgarh! Fresh, flavourful dishes every day. " +
//                                            "Call: +91$PHONE_NUMBER | Email: $EMAIL_ADDRESS"
//                                )
//                            },
//                            "Share Foodyvilla via"
//                        )
//                    )
//                }
//            }
//            Spacer(Modifier.height(12.dp))
//
//            SectionCard("Our Location") {
//                LocationSection {
//                    context.startActivity(
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("geo:$MAP_LAT,$MAP_LNG?q=$MAP_LAT,$MAP_LNG($MAP_LABEL)")
//                        )
//                    )
//                }
//            }
//            Spacer(Modifier.height(12.dp))
//
//            SectionCard("Opening Hours") { OpeningHours(hours) }
//            Spacer(Modifier.height(12.dp))
//
//            FooterNote()
//            Spacer(Modifier.height(28.dp))
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  HERO BANNER
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun HeroBanner() {
//    val gradientColors = listOf(
//        MaterialTheme.colorScheme.primary,
//        MaterialTheme.colorScheme.primaryContainer,
////        MaterialTheme.colorScheme.tertiary
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Brush.linearGradient(colors = gradientColors))
//            .padding(24.dp)
//    ) {
//        Column {
//            Surface(
//                shape = RoundedCornerShape(20.dp),
//                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
//            ) {
//                Text(
//                    text          = "🍛  Indian & Chinese Cuisine",
//                    color         = MaterialTheme.colorScheme.onTertiaryContainer,
//                    fontSize      = 11.sp,
//                    fontWeight    = FontWeight.Medium,
//                    letterSpacing = 0.5.sp,
//                    modifier      = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
//                )
//            }
//
//            Spacer(Modifier.height(14.dp))
//
//            Text(
//                text       = "Foodyvilla",
//                fontSize   = 36.sp,
//                fontWeight = FontWeight.Bold,
//                color      = MaterialTheme.colorScheme.onPrimary,
//                lineHeight = 40.sp
//            )
//            Text(
//                text       = "Nagri, Chhattisgarh",
//                fontSize   = 13.sp,
//                color      = MaterialTheme.colorScheme.tertiary,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(Modifier.height(10.dp))
//
//            Text(
//                text       = "Where every bite is a celebration of\nflavour, spice & tradition.",
//                fontSize   = 13.sp,
//                color      = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
//                lineHeight = 20.sp,
//                fontWeight = FontWeight.Light
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .size(56.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("🥘", fontSize = 26.sp)
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  SECTION CARD WRAPPER
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun SectionCard(
//    title   : String,
//    modifier: Modifier = Modifier,
//    content : @Composable ColumnScope.() -> Unit
//) {
//    Card(
//        modifier  = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape     = RoundedCornerShape(16.dp),
//        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text          = title.uppercase(),
//                style         = MaterialTheme.typography.labelSmall,
//                color         = MaterialTheme.colorScheme.primary,
//                letterSpacing = 1.2.sp,
//                fontWeight    = FontWeight.SemiBold
//            )
//            Spacer(Modifier.height(12.dp))
//            content()
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  ABOUT SECTION
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun AboutSection() {
//    Text(
//        text = "Foodyvilla is your go-to destination for authentic Indian curries, " +
//                "aromatic biryanis, sizzling tandoori delights, and flavour-packed " +
//                "Chinese dishes — all crafted daily with fresh ingredients and " +
//                "time-honoured recipes. Order online, reserve a table, or explore " +
//                "our full menu right from the app.",
//        style      = MaterialTheme.typography.bodyMedium,
//        color      = MaterialTheme.colorScheme.onSurfaceVariant,
//        lineHeight = 22.sp
//    )
//
//    Spacer(Modifier.height(14.dp))
//
//    // Alternate stat chips: primary / secondary / primary
//    val stats = listOf(
//        Triple("4.8 ★", "Food Rating", false),
//        Triple("80+",   "Menu Items", true),
//        Triple("5K+",   "Orders",     false)
//    )
//
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(10.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        stats.forEach { (num, label, isSecondary) ->
//            Surface(
//                modifier = Modifier.weight(1f),
//                shape    = RoundedCornerShape(12.dp),
//                color    = if (isSecondary) MaterialTheme.colorScheme.secondaryContainer
//                else             MaterialTheme.colorScheme.primaryContainer
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(vertical = 12.dp)
//                ) {
//                    Text(
//                        text       = num,
//                        fontSize   = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color      = if (isSecondary) MaterialTheme.colorScheme.onSecondaryContainer
//                        else             MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//                    Text(
//                        text       = label,
//                        fontSize   = 11.sp,
//                        color      = if (isSecondary) MaterialTheme.colorScheme.secondary
//                        else             MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  CONTACT ACTIONS GRID
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun ContactActionsGrid(
//    actions : List<ContactAction>,
//    onAction: (ContactAction) -> Unit
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
//        actions.chunked(2).forEach { rowActions ->
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                rowActions.forEach { action ->
//                    ContactActionButton(
//                        action   = action,
//                        modifier = Modifier.weight(1f),
//                        onClick  = { onAction(action) }
//                    )
//                }
//                if (rowActions.size < 2) Spacer(Modifier.weight(1f))
//            }
//        }
//    }
//}
//
//@Composable
//fun ContactActionButton(
//    action  : ContactAction,
//    modifier: Modifier = Modifier,
//    onClick : () -> Unit
//) {
//    val containerColor = when {
//        action.isPrimary   -> MaterialTheme.colorScheme.primary
//        action.isSecondary -> MaterialTheme.colorScheme.secondary
//        else               -> MaterialTheme.colorScheme.surfaceVariant
//    }
//    val contentColor = when {
//        action.isPrimary   -> MaterialTheme.colorScheme.onPrimary
//        action.isSecondary -> MaterialTheme.colorScheme.onSecondary
//        else               -> MaterialTheme.colorScheme.onSurfaceVariant
//    }
//    val subColor = contentColor.copy(alpha = 0.7f)
//    val iconBg   = contentColor.copy(alpha = 0.14f)
//
//    Surface(
//        onClick        = onClick,
//        modifier       = modifier,
//        shape          = RoundedCornerShape(14.dp),
//        color          = containerColor,
//        tonalElevation = 0.dp
//    ) {
//        Column(
//            modifier            = Modifier.padding(14.dp),
//            verticalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(iconBg),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector        = action.icon,
//                    contentDescription = action.label,
//                    tint               = contentColor,
//                    modifier           = Modifier.size(18.dp)
//                )
//            }
//            Text(text = action.label,    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = contentColor)
//            Text(text = action.subtitle, fontSize = 11.sp, color = subColor)
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  SHARE BUTTON
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun ShareButton(onClick: () -> Unit) {
//    OutlinedButton(
//        onClick  = onClick,
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(12.dp),
//        colors   = ButtonDefaults.outlinedButtonColors(
//            contentColor = MaterialTheme.colorScheme.secondary
//        ),
//        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
//    ) {
//        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
//        Spacer(Modifier.width(8.dp))
//        Text("Share This App", fontWeight = FontWeight.Medium, fontSize = 14.sp)
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  LOCATION SECTION
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun LocationSection(onDirectionsClick: () -> Unit) {
//    // Map placeholder
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(130.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .background(MaterialTheme.colorScheme.primaryContainer),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Icon(
//                imageVector        = Icons.Filled.LocationOn,
//                contentDescription = null,
//                tint               = MaterialTheme.colorScheme.secondary,
//                modifier           = Modifier.size(42.dp)
//            )
//            Text(
//                text       = "Nagri, Chhattisgarh, India",
//                fontSize   = 12.sp,
//                color      = MaterialTheme.colorScheme.onPrimaryContainer,
//                fontWeight = FontWeight.Medium
//            )
//            Text(
//                text     = "20°20'54.3\"N  81°57'33.6\"E",
//                fontSize = 10.sp,
//                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
//            )
//        }
//    }
//
//    Spacer(Modifier.height(12.dp))
//
//    Text(
//        text       = "Foodyvilla — Nagri Branch",
//        style      = MaterialTheme.typography.titleSmall,
//        fontWeight = FontWeight.SemiBold,
//        color      = MaterialTheme.colorScheme.onSurface
//    )
//    Spacer(Modifier.height(2.dp))
//    Text(
//        text       = "Nagri, Dhamtari District\nChhattisgarh, India",
//        style      = MaterialTheme.typography.bodySmall,
//        color      = MaterialTheme.colorScheme.onSurfaceVariant,
//        lineHeight = 20.sp
//    )
//
//    Spacer(Modifier.height(10.dp))
//
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalAlignment     = Alignment.CenterVertically
//    ) {
//        listOf("Dine-in", "Pick up", "Delivery").forEach { label ->
//            Surface(
//                shape = RoundedCornerShape(6.dp),
//                color = MaterialTheme.colorScheme.secondaryContainer
//            ) {
//                Text(
//                    text       = label,
//                    fontSize   = 12.sp,
//                    color      = MaterialTheme.colorScheme.onSecondaryContainer,
//                    fontWeight = FontWeight.Medium,
//                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
//        }
//    }
//
//    Spacer(Modifier.height(12.dp))
//
//    FilledTonalButton(
//        onClick  = onDirectionsClick,
//        modifier = Modifier.fillMaxWidth(),
//        shape    = RoundedCornerShape(12.dp),
//        colors   = ButtonDefaults.filledTonalButtonColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
//            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
//        )
//    ) {
//        Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
//        Spacer(Modifier.width(8.dp))
//        Text("Get Directions", fontWeight = FontWeight.Medium)
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  OPENING HOURS
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun OpeningHours(hours: List<HourEntry>) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        hours.forEachIndexed { index, entry ->
//            Row(
//                modifier              = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment     = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text       = entry.days,
//                        style      = MaterialTheme.typography.bodyMedium,
//                        color      = MaterialTheme.colorScheme.onSurface,
//                        fontWeight = if (entry.isToday) FontWeight.SemiBold else FontWeight.Normal
//                    )
//
//                }
//                Text(
//                    text       = entry.time,
//                    style      = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    color      = if (entry.isToday) MaterialTheme.colorScheme.primary
//                    else               MaterialTheme.colorScheme.onSurface
//                )
//            }
//            if (index < hours.lastIndex) {
//                HorizontalDivider(
//                    color     = MaterialTheme.colorScheme.outlineVariant,
//                    thickness = 0.5.dp
//                )
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  SOCIAL LINKS
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun SocialLinksRow(links: List<SocialLink>, onLinkClick: (String) -> Unit) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(10.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        links.forEach { link ->
//            Surface(
//                onClick        = { onLinkClick(link.url) },
//                modifier       = Modifier.weight(1f),
//                shape          = RoundedCornerShape(12.dp),
//                color          = MaterialTheme.colorScheme.surfaceVariant,
//                tonalElevation = 0.dp
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(vertical = 12.dp)
//                ) {
//                    Icon(
//                        imageVector        = link.icon,
//                        contentDescription = link.label,
//                        tint               = MaterialTheme.colorScheme.primary,
//                        modifier           = Modifier.size(22.dp)
//                    )
//                    Spacer(Modifier.height(4.dp))
//                    Text(
//                        text       = link.label,
//                        fontSize   = 10.sp,
//                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
//                        fontWeight = FontWeight.Medium,
//                        textAlign  = TextAlign.Center
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  FOOTER
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun FooterNote() {
//    Column(
//        modifier            = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        HorizontalDivider(
//            color     = MaterialTheme.colorScheme.outlineVariant,
//            thickness = 0.5.dp
//        )
//        Spacer(Modifier.height(16.dp))
//        Text(
//            text      = "Made with ❤️ by Foodyvilla, Nagri CG",
//            fontSize  = 12.sp,
//            color     = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = TextAlign.Center
//        )
//        Spacer(Modifier.height(4.dp))
//        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//            Text(
//                text       = "Privacy Policy",
//                fontSize   = 11.sp,
//                color      = MaterialTheme.colorScheme.secondary,
//                fontWeight = FontWeight.Medium
//            )
//            Text(
//                text       = "Terms of Use",
//                fontSize   = 11.sp,
//                color      = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.Medium
//            )
//            Text(
//                text  = "v1.0.0",
//                fontSize = 11.sp,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  PREVIEW
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Preview(showBackground = true, showSystemUi = true, name = "Foodyvilla Contact Us")
//@Composable
//fun ContactUsScreenPreview() {
//    ContactUsScreen()
//}