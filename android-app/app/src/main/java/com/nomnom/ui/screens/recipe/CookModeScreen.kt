package com.nomnom.ui.screens.recipe

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun CookModeScreen(
    recipeId: String,
    viewModel: RecipeViewModel,
    onExit: () -> Unit,
    onCooked: () -> Unit = {}
) {
    val recipe by viewModel.selectedRecipe.collectAsState()
    val steps  = recipe?.steps ?: emptyList()

    var currentStep  by remember { mutableStateOf(0) }
    var showComplete by remember { mutableStateOf(false) }
    val haptic       = LocalHapticFeedback.current

    // ── Wake lock — keeps screen on while cooking ─────────────────────────
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    if (showComplete) {
        CookCompleteScreen(
            recipeName = recipe?.title ?: "",
            onDone     = { onCooked(); onExit() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CSSurface)
    ) {
        if (steps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No steps found.", color = CSOnSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Progress dots ─────────────────────────────────────────
                WizardStepIndicator(
                    totalSteps  = steps.size,
                    currentStep = currentStep,
                    modifier    = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Step ${currentStep + 1} of ${steps.size}",
                    color    = CSOnSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(Modifier.weight(0.5f))

                // ── Step text ─────────────────────────────────────────────
                AnimatedContent(
                    targetState  = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label        = "stepContent"
                ) { stepIdx ->
                    val stepText = steps.getOrElse(stepIdx) { "" }
                    Box(
                        modifier        = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CSSurfaceLowest)
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier        = Modifier
                                    .size(44.dp)
                                    .background(CSSage, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${stepIdx + 1}",
                                    color      = CSOnSage,
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text       = stepText,
                                color      = CSOnSurface,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign  = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                            // Timer chip — parse first time mention in step
                            val timerMs = remember(stepText) { parseTimerFromStep(stepText) }
                            if (timerMs != null) {
                                Spacer(Modifier.height(24.dp))
                                StepTimerWidget(totalMs = timerMs)
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // ── Navigation buttons ────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick  = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = BorderStroke(1.dp, CSOutline),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = CSOnSurface)
                        ) {
                            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                    }

                    NomNomButton(
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (currentStep < steps.lastIndex) {
                                currentStep++
                            } else {
                                showComplete = true
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentStep > 0) 1f else 1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            if (currentStep < steps.lastIndex) "Next Step" else "🎉 Done!",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        if (currentStep < steps.lastIndex) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // ── Exit button ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(CSSurfaceLowest.copy(alpha = 0.7f))
                .clickable(onClick = onExit),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, "Exit Cook Mode", tint = CSOnSurface, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Per-step timer widget
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepTimerWidget(totalMs: Long) {
    var remaining by remember(totalMs) { mutableStateOf(totalMs) }
    var running   by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0L) {
            delay(1000L)
            remaining -= 1000L
            if (remaining <= 0L) { remaining = 0L; running = false }
        }
    }

    Row(
        modifier          = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(CSSurface)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TimerRing(totalMs = totalMs, remainingMs = remaining, running = running, size = 64.dp)

        Column {
            Text("Timer", color = CSOnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!running && remaining > 0L) {
                    IconButton(
                        onClick  = { running = true },
                        modifier = Modifier
                            .size(34.dp)
                            .background(CSSage, CircleShape)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start", tint = CSOnSage, modifier = Modifier.size(18.dp))
                    }
                } else if (running) {
                    IconButton(
                        onClick  = { running = false },
                        modifier = Modifier
                            .size(34.dp)
                            .background(CSSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Pause, "Pause", tint = CSOnSurface, modifier = Modifier.size(18.dp))
                    }
                }
                if (remaining < totalMs) {
                    IconButton(
                        onClick  = { remaining = totalMs; running = false },
                        modifier = Modifier
                            .size(34.dp)
                            .background(CSSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, "Reset", tint = CSOnSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Parse first time mention from step text
// ─────────────────────────────────────────────────────────────────────────────

private fun parseTimerFromStep(text: String): Long? {
    val regex = Regex("""(\d+(?:\.\d+)?)\s*(hour|hr|minute|min|second|sec)s?""", RegexOption.IGNORE_CASE)
    val match = regex.find(text) ?: return null
    val amount = match.groupValues[1].toDoubleOrNull() ?: return null
    return when (match.groupValues[2].lowercase()) {
        "hour", "hr" -> (amount * 3_600_000).toLong()
        "minute", "min" -> (amount * 60_000).toLong()
        "second", "sec" -> (amount * 1_000).toLong()
        else -> null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Completion / confetti screen
// ─────────────────────────────────────────────────────────────────────────────

private data class ConfettiPiece(
    val x: Float, val y: Float, val color: Color,
    val size: Float, val speedX: Float, val speedY: Float, val rotation: Float
)

@Composable
private fun CookCompleteScreen(recipeName: String, onDone: () -> Unit) {
    val colors = listOf(CSSage, CSTerracotta, CSOutline, Color(0xFF60CFFF), Color(0xFFFF8C69))
    val pieces = remember {
        List(60) {
            ConfettiPiece(
                x        = Random.nextFloat(),
                y        = Random.nextFloat() * -0.3f,
                color    = colors.random(),
                size     = Random.nextFloat() * 12f + 6f,
                speedX   = (Random.nextFloat() - 0.5f) * 2f,
                speedY   = Random.nextFloat() * 3f + 1.5f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val infinite = rememberInfiniteTransition(label = "confetti")
    val time by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)),
        "confettiTime"
    )

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); entered = true }

    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(CSSurface),
        contentAlignment = Alignment.Center
    ) {
        // Confetti canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            pieces.forEach { p ->
                val cx = (p.x + p.speedX * time * 0.4f).mod(1f) * size.width
                val cy = (p.y + p.speedY * time * 0.5f).mod(1.3f) * size.height
                rotate(p.rotation + time * 360f, pivot = Offset(cx, cy)) {
                    drawRect(
                        color    = p.color,
                        topLeft  = Offset(cx - p.size / 2, cy - p.size / 2),
                        size     = androidx.compose.ui.geometry.Size(p.size, p.size * 0.5f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible      = entered,
            enter        = fadeIn(tween(500)) + scaleIn(spring(Spring.DampingRatioLowBouncy))
        ) {
            Column(
                modifier            = Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CSSurfaceLowest.copy(alpha = 0.95f))
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 72.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "You made it!",
                    style      = MaterialTheme.typography.displayMedium,
                    color      = CSOnSurface,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    recipeName,
                    color     = CSSage,
                    fontSize  = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                NomNomButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done 🙌", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
