package com.jp.foodyvilla.presentation.screens.login


import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jp.foodyvilla.R
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.isSystemInDarkTheme


@Composable
fun MobileLoginScreen(
    loginViewModel: LoginViewModel,
    navController: NavController,
    onGetOtp: (String) -> Unit = {}
) {
    val mobileNumber = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value
    val getOtpState = loginViewModel.getOtpState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val isDark = isSystemInDarkTheme()

    // Derived states for crisp loading and button logic
    val isLoading = getOtpState is UiState.Loading
    val isButtonEnabled = mobileNumber.length == 10 && !isLoading

    // Handle side-effects cleanly
    when (getOtpState) {
        is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
        is UiState.Success<*> -> navController.navigate(Screen.Otp)
        else -> {}
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ── LAYER 1: Full Screen Background Image (with Blur) ───────────
            Image(
                painter = painterResource(id = R.drawable.loginbg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (isDark) 2.dp else 4.dp)
                ,
                contentScale = ContentScale.Crop
            )

            // ── LAYER 2: Semi-transparent Gradient Overlay ───────────────────
            val scrimColorBase = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scrimColorBase.copy(alpha = if (isDark) 0.2f else 0.1f),
                                scrimColorBase.copy(alpha = if (isDark) 0.85f else 0.92f)
                            )
                        )
                    )
            )

            // ── LAYER 3: Scrollable UI Content ───────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Top Brand Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_new),
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(160.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Food. Joy. Delivered.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                // Interactive Form Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Let's get you in! 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your number to order delicious food",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Phone Input Card ─────────────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.85f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Mobile Number",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    enabled = !isLoading, // Disables clicking chips during API call
                                    label = {
                                        Text(
                                            text = "🇮🇳 +91",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    },
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                OutlinedTextField(
                                    value = mobileNumber,
                                    onValueChange = {
                                        if (it.length <= 10 && it.all(Char::isDigit))
                                            loginViewModel.updatePhone(it)
                                    },
                                    enabled = !isLoading, // Freeze inputs when loading
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    placeholder = {
                                        Text(
                                            "00000 00000",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    )
                                )
                            }

                            AnimatedVisibility(
                                visible = mobileNumber.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = "${mobileNumber.length}/10",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (mobileNumber.length == 10)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Get OTP Button (With Embedded Async Loading Spinner) ───
                    Button(
                        onClick = { onGetOtp(mobileNumber) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        enabled = isButtonEnabled, // Prevents accidental duplicate network requests
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.LocalDining,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Get OTP & Order Now",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Terms & Privacy Policy Footer ─────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            "By continuing, you agree to our ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Terms",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " & ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Privacy Policy",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}





// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2 — OTP Verification Screen  (Restaurant Theme · Material 3)
// ─────────────────────────────────────────────────────────────────────────────





@Composable
fun OtpVerificationScreen(
    maskedPhone: String = "+91 ***** *****",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val otpLength = 6
    var otpValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value

    var countdown by remember { mutableIntStateOf(54) }
    var timerRunning by remember { mutableStateOf(true) }

    val isLoading = loginState is UiState.Loading
    val isVerifyButtonEnabled = otpValue.length == otpLength && !isLoading

    // Countdown Timer Logic
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            timerRunning = false
        }
    }

    // Auto-focus keyboard on screen entry
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle Authentication UI States safely
    val context = LocalContext.current
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
            is UiState.Success<*> -> {
                focusManager.clearFocus()
                navController.navigate(Screen.Home)
            }
            else -> {}
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        // The outer Box centers the main Column vertically and horizontally within the viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Lottie Animation Inside a Balanced Circle ─────────────────
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.delivery_service)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.fillMaxSize().padding(10.dp) // Sized perfectly to fit inside the circle frame
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Almost there! 🍽️",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Code sent to $maskedPhone",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Enter the 6-digit OTP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your food is just one step away",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Interactive OTP Container Layout ───────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 1: Fully Invisible Native Text Input Area
                    BasicTextField(
                        value = otpValue,
                        onValueChange = { newValue ->
                            val cleanValues = newValue.filter { it.isDigit() }.take(otpLength)
                            otpValue = cleanValues

                            if (cleanValues.length == otpLength) {
                                onVerify(cleanValues)
                            }
                        },
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                            .alpha(0.01f),
                        decorationBox = { it() }
                    )

                    // Layer 2: Visual Presentation Display Grid View
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        repeat(otpLength) { index ->
                            val char = otpValue.getOrNull(index)?.toString() ?: ""

                            val isBoxFocused = isFocused && (index == otpValue.length || (index == otpLength - 1 && otpValue.length == otpLength))
                            val hasValue = char.isNotEmpty()

                            val containerColor = when {
                                isLoading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                isBoxFocused || hasValue -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                            val borderColor = when {
                                isLoading -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                isBoxFocused || hasValue -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            }
                            val borderWidth = if (isBoxFocused || hasValue && !isLoading) 2.dp else 1.dp

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(containerColor, RoundedCornerShape(14.dp))
                                    .border(borderWidth, borderColor, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = if (hasValue && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { otpValue.length / otpLength.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (otpValue.length == otpLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Verify Submit Button with Async Spinner ───────────────────
                Button(
                    onClick = { onVerify(otpValue) },
                    enabled = isVerifyButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Verify & Start Ordering",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Subtle Material Timer Display View ────────────────────
                AnimatedVisibility(
                    visible = timerRunning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resend available in ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                countdown / 60,
                                countdown % 60
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Resend Code Interface Controller ───────────────────────
                TextButton(
                    onClick = {
                        if (!timerRunning && !isLoading) {
                            onResendOtp()
                            countdown = 54
                            timerRunning = true
                            otpValue = ""
                            focusRequester.requestFocus()
                        }
                    },
                    enabled = !timerRunning && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Resend OTP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!timerRunning && !isLoading)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun OtpVerificationScreen1(
    maskedPhone: String = "+91 ***** *****",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val otpLength = 6
    var otpValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value
    val isDark = isSystemInDarkTheme()

    var countdown by remember { mutableIntStateOf(54)  }
    var timerRunning by remember { mutableStateOf(true) }

    // Unified Reactive UI States
    val isLoading = loginState is UiState.Loading
    val isVerifyButtonEnabled = otpValue.length == otpLength && !isLoading

    // Countdown Timer Logic
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            timerRunning = false
        }
    }

    // Auto-focus keyboard on screen entry
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle Authentication UI States safely
    val context = LocalContext.current
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
            is UiState.Success<*> -> {
                focusManager.clearFocus()
                navController.navigate(Screen.Home)
            }
            else -> {}
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ── LAYER 1: Full Screen Background Image (with Blur) ───────────
//            Image(
//                painter = painterResource(id = R.drawable.loginbg),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .blur(if (isDark) 16.dp else 10.dp),
//                contentScale = ContentScale.Crop
//            )

            // ── LAYER 2: Theme-Aware Gradient Scrim Overlay ───────────────────
            val scrimColorBase = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scrimColorBase.copy(alpha = if (isDark) 0.4f else 0.5f),
                                scrimColorBase.copy(alpha = if (isDark) 0.85f else 0.92f)
                            )
                        )
                    )
            )

            // ── LAYER 3: Scrollable UI Content ───────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Hero Lottie Banner Section ───────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.delivery_service)
                        )
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Almost there! 🍽️",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Code sent to $maskedPhone",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent
                        )
                    )
                }

                // ── Form Entry Content ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter the 6-digit OTP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your food is just one step away",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Interactive OTP Container Layout ─────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Layer 1: Fully Invisible Native Text Input Area
                        BasicTextField(
                            value = otpValue,
                            onValueChange = { newValue ->
                                val cleanValues = newValue.filter { it.isDigit() }.take(otpLength)
                                otpValue = cleanValues

                                if (cleanValues.length == otpLength) {
                                    onVerify(cleanValues)
                                }
                            },
                            enabled = !isLoading, // Disables text extraction operations during processing
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused }
                                .alpha(0.01f),
                            decorationBox = { it() }
                        )

                        // Layer 2: Visual Presentation Display Grid View
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            repeat(otpLength) { index ->
                                val char = otpValue.getOrNull(index)?.toString() ?: ""

                                val isBoxFocused = isFocused && (index == otpValue.length || (index == otpLength - 1 && otpValue.length == otpLength))
                                val hasValue = char.isNotEmpty()

                                val containerColor = when {
                                    isLoading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    isBoxFocused || hasValue -> MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).copy(alpha = 0.9f)
                                    else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.7f)
                                }
                                val borderColor = when {
                                    isLoading -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    isBoxFocused || hasValue -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                }
                                val borderWidth = if (isBoxFocused || hasValue && !isLoading) 2.dp else 1.dp

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(containerColor, RoundedCornerShape(14.dp))
                                        .border(borderWidth, borderColor, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = if (hasValue && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium inline character length completion tracking progress bar
                    LinearProgressIndicator(
                        progress = { otpValue.length / otpLength.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (otpValue.length == otpLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // ── Verify Submit Button with Async Spinner ───────────────────
                    Button(
                        onClick = { onVerify(otpValue) },
                        enabled = isVerifyButtonEnabled, // Safety lock against multiple execution triggers
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Verify & Start Ordering",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Subtle Material Timer Display View ────────────────────
                    AnimatedVisibility(
                        visible = timerRunning,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Resend available in ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    countdown / 60,
                                    countdown % 60
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Resend Code Interface Controller ───────────────────────
                    TextButton(
                        onClick = {
                            if (!timerRunning && !isLoading) {
                                onResendOtp()
                                countdown = 54
                                timerRunning = true
                                otpValue = ""
                                focusRequester.requestFocus()
                            }
                        },
                        enabled = !timerRunning && !isLoading, // Block code requests if system is already executing a verify operation
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Resend OTP",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!timerRunning && !isLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun OtpVerificationScreen0(
    maskedPhone: String = "+91 ***** *****",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val otpLength = 6
    var otpValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value

    var countdown by remember { mutableIntStateOf(54) }
    var timerRunning by remember { mutableStateOf(true) }

    // Countdown Timer Logic
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            timerRunning = false
        }
    }

    // Auto-focus keyboard on screen entry
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle Authentication UI States
    val context = LocalContext.current
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Error -> Toast.makeText(
                context,
                "Try Again After Sometime…",
                Toast.LENGTH_SHORT
            ).show()

            UiState.Loading -> Toast.makeText(context, "Verifying…", Toast.LENGTH_SHORT).show()
            is UiState.Success<*> -> {
                focusManager.clearFocus()
                navController.navigate(Screen.Home)
            }

            else -> {}
        }
    }

    val timerProgress = countdown / 54f

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Hero Banner ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── Lottie Animation Container ───────────────────────────

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.delivery_service)
                        )
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Almost there! 🍽️",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Code sent to $maskedPhone",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Form Entry Content ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Enter the 6-digit OTP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your food is just one step away",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Interactive OTP Container Layout ───────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 1: Fully Invisible Native Text Input Area for Keyboard Paste & Autofill Focus
                    BasicTextField(
                        value = otpValue,
                        onValueChange = { newValue ->
                            val cleanValues = newValue.filter { it.isDigit() }.take(otpLength)
                            otpValue = cleanValues

                            if (cleanValues.length == otpLength) {
                                onVerify(cleanValues)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                            .alpha(0.01f),
                        decorationBox = { it() }
                    )

                    // Layer 2: Visual Presentation Display Grid View
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        repeat(otpLength) { index ->
                            val char = otpValue.getOrNull(index)?.toString() ?: ""

                            // Highlight calculation logic
                            val isBoxFocused =
                                isFocused && (index == otpValue.length || (index == otpLength - 1 && otpValue.length == otpLength))
                            val hasValue = char.isNotEmpty()

                            val containerColor = when {
                                isBoxFocused || hasValue -> MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.25f
                                )

                                else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                            }
                            val borderColor = when {
                                isBoxFocused || hasValue -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                            }
                            val borderWidth = if (isBoxFocused || hasValue) 2.dp else 1.dp

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(containerColor, RoundedCornerShape(12.dp))
                                    .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = if (hasValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom sleek verification step loading line
                LinearProgressIndicator(
                    progress = { otpValue.length / otpLength.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.4f) // Shrunk to look like a premium premium tracker indicator element
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ── Verify Submit Button ─────────────────────────────────────
                Button(
                    onClick = { onVerify(otpValue) },
                    enabled = otpValue.length == otpLength,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Verify & Start Ordering",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Subtle Material Timer Display View ────────────────────────
                AnimatedVisibility(
                    visible = timerRunning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resend available in ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                countdown / 60,
                                countdown % 60
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Resend Code Interface Controller ───────────────────────────
                TextButton(
                    onClick = {
                        if (!timerRunning) {
                            onResendOtp()
                            countdown = 54
                            timerRunning = true
                            otpValue = ""
                            focusRequester.requestFocus()
                        }
                    },
                    enabled = !timerRunning,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Resend OTP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!timerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.4f
                            )
                        )
                    )
                }
            }
        }
    }
}

