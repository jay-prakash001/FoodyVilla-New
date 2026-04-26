package com.jp.foodyvilla.presentation.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.utils.UiState

// ─── Data model matching your API response ────────────────────────────────────

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LoginViewModel,
    onSaveChanges: (UserProfile) -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.getUserProfile()
    }
    val userState = viewModel.user.collectAsStateWithLifecycle().value


    when (userState) {
        is UiState.Success -> {

            val userProfile = userState.data
            var name by remember { mutableStateOf(userProfile.name ?: "") }
            var email by remember { mutableStateOf(userProfile.email ?: "") }
            var phone by remember { mutableStateOf(userProfile.phone) }
            var address by remember { mutableStateOf(userProfile.address ?: "") }

            // UI state
            var showLogoutDialog by remember { mutableStateOf(false) }
            var isSaving by remember { mutableStateOf(false) }
            var saveSuccess by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }
            val scrollState = rememberScrollState()

            // FoodyVilla brand colors
            val brandOrange = MaterialTheme.colorScheme.primary
            val brandOrangeDark = MaterialTheme.colorScheme.primary
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "My Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            // Logout icon in top bar
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Save Changes Button
                            Button(
                                onClick = {
                                    isSaving = true
                                    val updated = userProfile.copy(
                                        name = name.trim().ifEmpty { null },
                                        email = email.trim().ifEmpty { null },
                                        phone = phone?.trim(),
                                        address = address.trim().ifEmpty { null }
                                    )
                                    onSaveChanges(updated)
                                    isSaving = false
                                    saveSuccess = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = brandOrange
                                ),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Save Changes",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            // Logout Button
                            OutlinedButton(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    // tint border to error color
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Out",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Avatar + Verification Badge ─────────────────────────────────
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(100.dp)
                    ) {
                        // Avatar Circle with gradient background
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(brandOrange, brandOrangeDark)
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = if (name.isNotBlank()) name.first().uppercase() else "U",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Edit camera icon
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change photo",
                                modifier = Modifier.padding(5.dp),
                                tint = brandOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name display (or placeholder)
                    Text(
                        text = if (name.isNotBlank()) name else "FoodyVilla User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Verification chip
                    if (userProfile.is_verified) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Verified", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                                labelColor = Color(0xFF2E7D32),
                                leadingIconContentColor = Color(0xFF2E7D32)
                            )
                        )
                    } else {
                        AssistChip(
                            onClick = {},
                            label = { Text("Not Verified", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.error,
                                leadingIconContentColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Form Card ────────────────────────────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = brandOrange
                            )

                            ProfileTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Full Name",
                                placeholder = "Enter your name",
                                leadingIcon = Icons.Outlined.Person,
                                keyboardType = KeyboardType.Text
                            )

                            ProfileTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                placeholder = "Enter your email",
                                leadingIcon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email
                            )

                            ProfileTextField(
                                value = phone ?: "",
                                onValueChange = { phone = it },
                                label = "Phone Number",
                                placeholder = "Enter phone number",
                                leadingIcon = Icons.Outlined.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Address Card ─────────────────────────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Delivery Address",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = brandOrange
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Address") },
                                placeholder = { Text("Enter your delivery address") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = brandOrange
                                    )
                                },
                                minLines = 3,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandOrange,
                                    focusedLabelColor = brandOrange,
                                    cursorColor = brandOrange
                                )
                            )

                            // Show lat/long if available
                            if (userProfile.lat != null && userProfile.long != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "GPS: ${userProfile.lat}, ${userProfile.long}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Account Info Card (read-only) ────────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Account Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = brandOrange
                            )

                            InfoRow(
                                icon = Icons.Outlined.Tag,
                                label = "User ID",
                                value = "#${userProfile.id}"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            InfoRow(
                                icon = Icons.Outlined.Key,
                                label = "Auth ID",
                                value = userProfile.auth_user_id.take(8) + "…"
                            )
                        }
                    }

                    // Bottom padding so content isn't hidden behind bottom bar
                    Spacer(modifier = Modifier.height(160.dp))
                }
            }

            // ── Logout Confirmation Dialog ────────────────────────────────────────────
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(
                            text = "Log Out?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to log out of FoodyVilla?",
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Log Out", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        else -> {}
    }
    // Editable state

}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType
) {
    val brandOrange = Color(0xFFFF6B35)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = brandOrange
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = brandOrange,
            focusedLabelColor = brandOrange,
            cursorColor = brandOrange
        )
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
// Uncomment to use in Android Studio Preview
/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    FoodyVillaTheme {
        ProfileScreen(
            userProfile = UserProfile(
                id = 8,
                authUserId = "291380ef-5573-4e6d-ac4b-d46233ebaf5c",
                name = null,
                email = null,
                phone = "+916264974771",
                address = null,
                lat = null,
                long = null,
                isVerified = false
            ),
            onSaveChanges = {},
            onLogout = {},
            onNavigateBack = {}
        )
    }
}
*/