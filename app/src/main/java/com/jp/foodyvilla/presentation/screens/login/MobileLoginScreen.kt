package com.jp.foodyvilla.presentation.screens.login

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla.R
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay
import java.util.Locale

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

    // Derived states for loading and button logic
    val isLoading = getOtpState is UiState.Loading
    val isButtonEnabled = mobileNumber.length == 10 && !isLoading

    // Handle side-effects cleanly
    LaunchedEffect(getOtpState) {
        when (getOtpState) {
            is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
            is UiState.Success<*> -> navController.navigate(Screen.Otp)
            else -> {}
        }
    }

    val bgDrawable = if (isDark) R.drawable.login_bg_dark else R.drawable.login_light_bg

    Box(modifier = Modifier.fillMaxSize()) {
        // LAYER 1: Fixed Full Screen Background Image (Completely unaffected by keyboard)
        Image(
            painter = painterResource(id = bgDrawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // LAYER 2: Semi-transparent Gradient Overlay
        val scrimColorBase = MaterialTheme.colorScheme.background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrimColorBase.copy(alpha = if (isDark) 0.3f else 0.15f),
                            scrimColorBase.copy(alpha = if (isDark) 0.85f else 0.8f)
                        )
                    )
                )
        )

        // LAYER 3: Scrollable Form Content with Keyboard Padding
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // App Logo at the top of the section
                Image(
                    painter = painterResource(R.drawable.logo_new),
                    contentDescription = "FoodyVilla Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Subtitle matching the provided image
                val headlineText = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = if (isDark) Color(0xFF4CAF50) else Color(0xFF006837),
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp
                        )
                    ) {
                        append("Good Food\n")
                    }
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp
                        )
                    ) {
                        append("Brings ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp
                        )
                    ) {
                        append("Good Mood")
                    }
                }

                Text(
                    text = headlineText,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fresh. Healthy. Delicious. Always Veg.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Interactive Form Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Phone Input Field Container
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f) else Color(0xFFF4F9F5),
                        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFA1D6B2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Smartphone,
                                contentDescription = null,
                                tint = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF00873D),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+91",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(Color(0xFFCCCCCC))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = mobileNumber,
                                onValueChange = {
                                    if (it.length <= 10 && it.all(Char::isDigit)) {
                                        loginViewModel.updatePhone(it)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !isLoading,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (mobileNumber.isEmpty()) {
                                            Text(
                                                text = "Enter your mobile number",
                                                style = TextStyle(
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    if (mobileNumber.isNotEmpty()) {
                        Text(
                            text = "${mobileNumber.length}/10",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mobileNumber.length == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Get OTP Button
                    Button(
                        onClick = { onGetOtp(mobileNumber) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isButtonEnabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722),
                            disabledContainerColor = Color(0xFFFF5722).copy(alpha = 0.4f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Get OTP",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Terms & Privacy Policy Footer
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
                            text = "Terms",
                            modifier = Modifier.clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://foodyvilla.github.io/Web/")
                                )
                                context.startActivity(intent)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = " & ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Privacy Policy",
                            modifier = Modifier.clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://foodyvilla.github.io/Web/")
                                )
                                context.startActivity(intent)
                            },
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
// SCREEN 2 — OTP Verification Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OtpVerificationScreen(
    maskedPhone: String = "+91 ***** *****",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val bgDrawable = if (isDark) R.drawable.login_bg_dark else R.drawable.login_light_bg

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
        delay(300)
        focusRequester.requestFocus()
    }

    // Handle Authentication UI States safely
    val context = LocalContext.current
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
            is UiState.Success<*> -> {
                focusManager.clearFocus()
                navController.navigate(Screen.Home) {
                    popUpTo(Screen.Login) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fixed Background Image (Doesn't shrink with keyboard)
        Image(
            painter = painterResource(id = bgDrawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay
        val scrimColorBase = MaterialTheme.colorScheme.background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrimColorBase.copy(alpha = if (isDark) 0.3f else 0.15f),
                            scrimColorBase.copy(alpha = if (isDark) 0.85f else 0.85f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Icon
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "OTP Icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Verify OTP",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter 6-digit code sent to $maskedPhone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // OTP Input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { focusRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = otpValue,
                            onValueChange = {
                                if (it.length <= otpLength && it.all(Char::isDigit)) {
                                    otpValue = it
                                    loginViewModel.updateOtp(it)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused }
                                .alpha(0f)
                                .size(1.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 0 until otpLength) {
                                val char = otpValue.getOrNull(i)?.toString() ?: ""
                                val isBoxFocused = isFocused && i == otpValue.length.coerceAtMost(otpLength - 1)

                                Surface(
                                    modifier = Modifier.size(46.dp, 56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isBoxFocused)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    border = BorderStroke(
                                        width = if (isBoxFocused) 2.dp else 1.dp,
                                        color = if (isBoxFocused)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = char,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Verify Button
                    Button(
                        onClick = { onVerify(otpValue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = isVerifyButtonEnabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Verify & Proceed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resend OTP Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (timerRunning) "Resend code in " else "Didn't receive code? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (timerRunning) {
                            Text(
                                text = String.format(Locale.getDefault(), "00:%02d", countdown),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Resend OTP",
                                modifier = Modifier.clickable {
                                    countdown = 54
                                    timerRunning = true
                                    onResendOtp()
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Change Number
                    Text(
                        text = "Edit Phone Number",
                        modifier = Modifier.clickable { navController.popBackStack() },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
