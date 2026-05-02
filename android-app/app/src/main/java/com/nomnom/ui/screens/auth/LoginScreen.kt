package com.nomnom.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.viewmodel.AuthViewModel
import com.nomnom.ui.components.NomNomButton
import com.nomnom.ui.components.NomNomTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit
) {
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val haptic       = LocalHapticFeedback.current

    // ── Animated gradient background ─────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "loginBg")
    val animColor1 by infiniteTransition.animateColor(
        initialValue  = CSSurface,
        targetValue   = CSSurfaceLowest,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "bgColor1"
    )
    val animColor2 by infiniteTransition.animateColor(
        initialValue  = CSSurfaceVariant,
        targetValue   = CSSurface,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "bgColor2"
    )
    val orb1Y by infiniteTransition.animateFloat(
        initialValue  = -60f,
        targetValue   = 60f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "orb1Y"
    )
    val orb2X by infiniteTransition.animateFloat(
        initialValue  = -40f,
        targetValue   = 40f,
        animationSpec = infiniteRepeatable(tween(6200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "orb2X"
    )
    // animated gradient colours
    val gradStart = animColor1
    val gradEnd   = animColor2

    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animColor1, animColor2, CSSurface))),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glow orbs (blurred circles)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = orb1Y.dp)
                .clip(CircleShape)
                .background(CSSage.copy(alpha = 0.08f))
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = orb2X.dp, y = 120.dp)
                .clip(CircleShape)
                .background(CSSurfaceVariant.copy(alpha = 0.6f))
                .blur(60.dp)
        )

        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Wordmark ─────────────────────────────────────────────────
            Box(
                modifier        = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(CSSage.copy(alpha = 0.15f))
                    .border(1.5.dp, CSSage.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍳", fontSize = 40.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Text(
                    text       = "Nom",
                    fontSize   = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = CSOnSurface
                )
                Text(
                    text       = "Nom",
                    fontSize   = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = CSSage
                )
            }
            Text(
                text      = "Your smart recipe companion",
                fontSize  = 14.sp,
                color     = CSOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(40.dp))

            // ── Glassmorphism form card ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CSSurfaceLowest.copy(alpha = 0.85f))
                    .border(
                        width  = 1.dp,
                        brush  = Brush.verticalGradient(
                            listOf(CSSage.copy(alpha = 0.3f), CSOutline.copy(alpha = 0.4f))
                        ),
                        shape  = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text       = "Welcome Back",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = CSOnSurface
                    )
                    Text(
                        text     = "Sign in to your account",
                        fontSize = 13.sp,
                        color    = CSOnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(Modifier.height(24.dp))

                    NomNomTextField(
                        value         = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label         = "Email",
                        placeholder   = "you@example.com",
                        leadingIcon   = {
                            Icon(Icons.Default.Email, null, tint = CSSage, modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    NomNomTextField(
                        value                = password,
                        onValueChange        = { password = it; viewModel.clearError() },
                        label                = "Password",
                        placeholder          = "••••••••",
                        leadingIcon          = {
                            Icon(Icons.Default.Lock, null, tint = CSSage, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon         = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = CSOnSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier             = Modifier.fillMaxWidth()
                    )

                    // Inline error
                    if (errorMessage != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CSSurfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, null, tint = CSError, modifier = Modifier.size(16.dp))
                            Text(errorMessage!!, color = CSError, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    NomNomButton(
                        onClick  = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.login(email, password) },
                        enabled  = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = CSOnSage,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigate to Signup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = CSOnSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = onNavigateToSignup) {
                    Text("Sign Up", color = CSSage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
