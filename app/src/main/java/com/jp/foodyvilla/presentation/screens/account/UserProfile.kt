package com.jp.foodyvilla.presentation.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val logoutState by viewModel.logoutState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            snackbarHostState.showSnackbar("Profile updated successfully")
        } else if (updateState is UiState.Error) {
            snackbarHostState.showSnackbar((updateState as UiState.Error).exception?.message ?: "Error updating profile")
        }
    }

    LaunchedEffect(logoutState) {
        if (logoutState is UiState.Success) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (userState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val user = (userState as UiState.Success).data
                ProfileContent(
                    user = user,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding),
                    updateLoading = updateState is UiState.Loading
                )
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load profile")
                        Button(onClick = { viewModel.getUserProfile() }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ProfileContent(
    user: UserProfile,
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier,
    updateLoading: Boolean
) {
    var name by remember(user.name) { mutableStateOf(user.name ?: "") }
    var email by remember(user.email) { mutableStateOf(user.email ?: "") }
    var phone by remember(user.phone) { mutableStateOf(user.phone ?: "") }
    var address by remember(user.address) { mutableStateOf(user.address ?: "") }

    val locationState by viewModel.locationState.collectAsStateWithLifecycle()

    // Update address when location is fetched
    LaunchedEffect(user.address) {
        address = user.address ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Initials Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (name.firstOrNull()?.toString() ?: user.phone?.lastOrNull()?.toString() ?: "U").uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = name.ifBlank { "Welcome!" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Default.Person
                )
                ProfileField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
                ProfileField(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    enabled = false // Usually phone is verified and not directly editable
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivery Address",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { viewModel.fetchCurrentLocation() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Use Current Location", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        trailingIcon = {
                           if (locationState is UiState.Loading) {
                               CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                           }
                        },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        Button(
            onClick = {
                val updatedUser = user.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    address = address
                )
                viewModel.updateProfile(updatedUser)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !updateLoading && name.isNotBlank()
        ) {
            if (updateLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Update Profile", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isSingleLine: Boolean = true,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = isSingleLine,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
