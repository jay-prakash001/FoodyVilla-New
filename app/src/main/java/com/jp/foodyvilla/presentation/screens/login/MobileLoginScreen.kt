package com.jp.foodyvilla.presentation.screens.login

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla.presentation.navigation.Screen
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 1 — Welcome / Mobile Login Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MobileLoginScreen(
    loginViewModel: LoginViewModel,
    navController: NavController,
    onGetOtp: (String) -> Unit = {}
) {
    var mobileNumber = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value

    val getOtpState = loginViewModel.getOtpState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    when(getOtpState){
        is UiState.Error -> {
            Toast.makeText(context,  "Try Again After Sometime...", Toast.LENGTH_SHORT).show()
        }
        UiState.Loading -> {
            Toast.makeText(context, "Sending OTP...", Toast.LENGTH_SHORT).show()

        }
        is UiState.Success<*> ->{
            navController.navigate(Screen.Otp)
        }
        else -> {}
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Brand Icon ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Brand",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Heading ─────────────────────────────────────────────────────
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your mobile number to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Mobile Number Label ─────────────────────────────────────────
            Text(
                text = "MOBILE NUMBER",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // ── Phone Input Row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Country Code Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "+91",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Number field
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) loginViewModel.updatePhone(it) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "000-000-0000",
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Get OTP Button ──────────────────────────────────────────────
            Button(
                onClick = { onGetOtp(mobileNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = mobileNumber.length == 10,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Get OTP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Terms ───────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    "By continuing, you agree to our ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    "Terms of Service",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " and ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Privacy Policy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2 — OTP Verification Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OtpVerificationScreen(
    maskedPhone: String = "+91 **********",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val otpLength = 6

    var otp by remember { mutableStateOf("") }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }

    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value
    // ── Countdown timer ─────────────────────────────
    var countdown by remember { mutableIntStateOf(54) }
    var timerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            timerRunning = false
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    val context = LocalContext.current
    when(loginState){
        is UiState.Error -> {
            Toast.makeText(context,  "Try Again After Sometime...", Toast.LENGTH_SHORT).show()
        }
        UiState.Loading -> {
            Toast.makeText(context, "Verifying...", Toast.LENGTH_SHORT).show()

        }
        is UiState.Success<*> ->{
            navController.navigate(Screen.Home)
        }
        else -> {}
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Icon ───────────────────────────────────
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Title ──────────────────────────────────
            Text(
                text = "Verify your number",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sent to $maskedPhone",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── OTP Boxes ──────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpLength) { index ->

                    val char = otp.getOrNull(index)?.toString() ?: ""

                    BasicTextField(
                        value = char,
                        onValueChange = { input ->

                            if (input.length <= 1 && input.all { it.isDigit() }) {

                                val newOtp = StringBuilder(otp)

                                if (input.isNotEmpty()) {
                                    if (otp.length > index) {
                                        newOtp.setCharAt(index, input[0])
                                    } else {
                                        newOtp.insert(index, input)
                                    }
                                } else if (otp.length > index) {
                                    newOtp.deleteCharAt(index)
                                }

                                otp = newOtp.toString()

                                // 👉 Move forward
                                if (input.isNotEmpty() && index < otpLength - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }

                                // 👉 Move back on delete
                                if (input.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }

                                // 👉 Auto submit
                                if (otp.length == otpLength) {
                                    onVerify(otp)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .focusRequester(focusRequesters[index])
                            .border(
                                width = 2.dp,
                                color = if (char.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                if (char.isNotEmpty())
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(14.dp)
                            ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Verify Button ──────────────────────────
            Button(
                onClick = { onVerify(otp) },
                enabled = otp.length == otpLength,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Verify & Proceed",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Timer ──────────────────────────────────
            if (timerRunning) {
                val formatted = String.format("%02d:%02d", countdown / 60, countdown % 60)

                Text(
                    text = "Resend in $formatted",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Resend ─────────────────────────────────
            TextButton(
                onClick = {
                    if (!timerRunning) {
                        onResendOtp()
                        countdown = 54
                        timerRunning = true
                        otp = ""
                        focusRequesters[0].requestFocus()
                    }
                },
                enabled = !timerRunning
            ) {
                Text("Resend OTP")
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
