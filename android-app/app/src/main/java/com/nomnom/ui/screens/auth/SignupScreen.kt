package com.nomnom.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
fun SignupScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }
    val haptic          = LocalHapticFeedback.current

    // ── Animated background ───────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "signupBg")
    val animColor by infiniteTransition.animateColor(
        initialValue  = CSSurface,
        targetValue   = CSSurfaceLowest,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "signupBg"
    )
    val orbY by infiniteTransition.animateFloat(
        initialValue  = -50f,
        targetValue   = 50f,
        animationSpec = infiniteRepeatable(tween(5500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "orbY"
    )

    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animColor, CSSurface, CSSurfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 100.dp, y = orbY.dp)
                .clip(CircleShape)
                .background(CSSage.copy(alpha = 0.5f))
                .blur(70.dp)
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Box(
                modifier        = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CSSage.copy(alpha = 0.12f))
                    .border(1.5.dp, CSSage.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍳", fontSize = 36.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row {
                Text("Nom", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = CSOnSurface)
                Text("Nom", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = CSSage)
            }
            Text(
                "Create your free account",
                fontSize  = 14.sp,
                color     = CSOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(36.dp))

            // ── Glass card ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CSSurfaceLowest.copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(CSSage.copy(alpha = 0.25f), CSOutline.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text("Get Started", style = MaterialTheme.typography.titleLarge, color = CSOnSurface)
                    Text("Sign up to save your recipes", fontSize = 13.sp, color = CSOnSurfaceVariant)
                    Spacer(Modifier.height(24.dp))

                    NomNomTextField(
                        value         = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label         = "Email",
                        placeholder   = "you@example.com",
                        leadingIcon   = { Icon(Icons.Default.Email, null, tint = CSSage, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    NomNomTextField(
                        value                = password,
                        onValueChange        = { password = it; viewModel.clearError() },
                        label                = "Password",
                        placeholder          = "Min. 8 characters",
                        leadingIcon          = { Icon(Icons.Default.Lock, null, tint = CSSage, modifier = Modifier.size(20.dp)) },
                        trailingIcon         = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CSOnSurfaceVariant)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier             = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    NomNomTextField(
                        value                = confirmPassword,
                        onValueChange        = { confirmPassword = it; viewModel.clearError() },
                        label                = "Confirm Password",
                        placeholder          = "Repeat password",
                        leadingIcon          = { Icon(Icons.Default.Lock, null, tint = CSSage, modifier = Modifier.size(20.dp)) },
                        trailingIcon         = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CSOnSurfaceVariant)
                            }
                        },
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier             = Modifier.fillMaxWidth()
                    )

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
                        onClick  = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.signup(email, password, confirmPassword) },
                        enabled  = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = CSOnSage, strokeWidth = 2.dp)
                        } else {
                            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = CSOnSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = CSSage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
