package com.nomnom.ui.screens.suggest

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.Recipe
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun SuggestScreen(
    authToken: String,
    recipeViewModel: RecipeViewModel
) {
    val recipes   by recipeViewModel.recipes.collectAsState()
    var suggested by remember { mutableStateOf<Recipe?>(null) }
    var flipped   by remember { mutableStateOf(false) }
    var spinning  by remember { mutableStateOf(false) }

    // Card flip angle
    val flipAngle by animateFloatAsState(
        targetValue   = if (flipped) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "cardFlip"
    )

    // Spinning ring for dice icon
    val spinAngle by animateFloatAsState(
        targetValue   = if (spinning) 1440f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label         = "diceSpinAngle"
    )

    val onRoll = {
        if (recipes.isNotEmpty() && !spinning) {
            spinning  = true
            flipped   = false
            suggested = null
        }
    }

    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NomNomNavy, NomNomBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            // Header
            Text(
                text      = "What should\nI eat? 🎲",
                style     = MaterialTheme.typography.displayMedium,
                color     = NomNomWhite,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Let NomNom pick for you",
                fontSize  = 14.sp,
                color     = NomNomTextSub,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // ── 3D flip card ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .graphicsLayer {
                        rotationY      = flipAngle
                        cameraDistance = 15f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRoll
                    )
            ) {
                if (flipAngle <= 90f) {
                    // Front face — prompt card
                    FrontCard(isSpinning = spinning, spinAngle = spinAngle)
                } else {
                    // Back face — recipe result (rotated to compensate)
                    Box(Modifier.graphicsLayer { rotationY = 180f }.fillMaxSize()) {
                        ResultCard(recipe = suggested)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Roll button ────────────────────────────────────────────────
            NomNomButton(
                onClick  = onRoll,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = !spinning
            ) {
                Icon(
                    Icons.Default.Casino,
                    null,
                    modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = spinAngle }
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (!flipped) "Suggest a Recipe" else "Try Another",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (recipes.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Add some recipes first!", color = NomNomTextSub, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }

    // Spin, pick, then flip
    LaunchedEffect(spinning) {
        if (spinning) {
            delay(800)
            suggested = if (recipes.isNotEmpty()) recipes.random() else null
            spinning  = false
            flipped   = true
        }
    }
}

@Composable
private fun FrontCard(isSpinning: Boolean, spinAngle: Float) {
    val infinite = rememberInfiniteTransition(label = "frontGlow")
    val glowAlpha by infinite.animateFloat(
        0.4f, 0.85f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        "glowAlpha"
    )
    Box(
        modifier        = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(NomNomSurface)
            .border(2.dp, NomNomRed.copy(alpha = if (isSpinning) 1f else glowAlpha), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val diceScale by rememberInfiniteTransition(label = "diceScale").animateFloat(
                1f, 1.15f,
                infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                "diceScale"
            )
            Icon(
                Icons.Default.Casino,
                null,
                tint     = NomNomRed,
                modifier = Modifier
                    .size(100.dp)
                    .scale(if (isSpinning) 1.2f else diceScale)
                    .graphicsLayer { rotationZ = spinAngle }
            )
            Spacer(Modifier.height(24.dp))
            Text("Tap to reveal", color = NomNomWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("your secret meal", color = NomNomTextSub, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ResultCard(recipe: Recipe?) {
    Box(
        modifier        = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(NomNomSurface)
            .border(1.dp, NomNomDivider, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (recipe == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("😔", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("No recipes yet!", color = NomNomWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Add some recipes first", color = NomNomTextSub, fontSize = 13.sp)
            }
        } else {
            // Recipe Background Hint
            Box(Modifier.fillMaxSize()) {
                if (!recipe.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().alpha(0.15f),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(
                    modifier            = Modifier.padding(24.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("TONIGHT'S SPECIAL 👨‍🍳", color = NomNomRed, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        recipe.title,
                        color      = NomNomWhite,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipe.prepTimeMinutes?.let { RecipeChip("⏱ ${it}m") }
                        recipe.cookTimeMinutes?.let { RecipeChip("🔥 ${it}m") }
                        recipe.servings?.let       { RecipeChip("🍽 $it") }
                    }
                    Spacer(Modifier.height(24.dp))
                    
                    NomNomButton(
                        onClick = { /* Navigate to detail - would need navigation here */ },
                        containerColor = NomNomSurface2,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("View Recipe", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
