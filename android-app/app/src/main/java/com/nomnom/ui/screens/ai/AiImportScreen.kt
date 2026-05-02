package com.nomnom.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.AiExtractedRecipe
import com.nomnom.data.viewmodel.AiImportViewModel
import com.nomnom.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImportScreen(
    viewModel: AiImportViewModel,
    authToken: String,
    onBack: () -> Unit,
    onUseRecipe: (AiExtractedRecipe) -> Unit,
    onEditRecipe: (AiExtractedRecipe) -> Unit = onUseRecipe  // defaults to same as use
) {
    var inputText       by remember { mutableStateOf("") }
    val isLoading       by viewModel.isLoading.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()
    val extractedRecipe by viewModel.extractedRecipe.collectAsState()

    var expandIngredients by remember { mutableStateOf(false) }
    var expandSteps       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.authToken = authToken }
    // Reset expand state when new recipe extracted
    LaunchedEffect(extractedRecipe) { expandIngredients = false; expandSteps = false }

    // Sparkle animation
    val sparkTransition = rememberInfiniteTransition(label = "sparkle")
    val sparkRotation by sparkTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1800, easing = LinearEasing)),
        "sparkRot"
    )
    val sparkScale by sparkTransition.animateFloat(
        0.85f, 1.2f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        "sparkScale"
    )
    val buttonPulse by sparkTransition.animateFloat(
        1f, 1.04f,
        infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        "btnPulse"
    )

    Scaffold(
        containerColor = NomNomNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint     = NomNomGold,
                                modifier = Modifier.size(22.dp).scale(sparkScale).graphicsLayer { rotationZ = sparkRotation }
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, tint = NomNomGold, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("AI Recipe Import", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = NomNomWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NomNomSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header card ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NomNomNavyDeep, NomNomGoldSurface, NomNomSurface)
                        )
                    )
                    .border(1.dp, NomNomGold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨", fontSize = 22.sp)
                        Text("Paste a recipe URL or text", color = NomNomWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Gemini AI extracts the title, ingredients, steps, and more automatically.",
                        color      = NomNomTextSub,
                        fontSize   = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Input ─────────────────────────────────────────────────────
            NomNomTextField(
                value         = inputText,
                onValueChange = {
                    inputText = it
                    if (viewModel.extractedRecipe.value != null) viewModel.clearRecipe()
                },
                placeholder   = "Paste a recipe URL (e.g. allrecipes.com/...) or paste the full recipe text here…",
                singleLine    = false,
                maxLines      = 8,
                modifier      = Modifier.fillMaxWidth().height(160.dp)
            )

            // ── Extract button ─────────────────────────────────────────────
            val isOffline by viewModel.isOffline.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (isLoading) buttonPulse else 1f)
            ) {
                NomNomButton(
                    onClick         = { viewModel.extractRecipe(inputText) },
                    enabled         = inputText.isNotBlank() && !isLoading && !isOffline,
                    containerColor  = if (isOffline) NomNomNavyLighter else (if (isLoading) NomNomGold.copy(alpha = 0.8f) else NomNomGold),
                    modifier        = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            null,
                            tint     = NomNomBg,
                            modifier = Modifier.size(20.dp).scale(sparkScale).graphicsLayer { rotationZ = sparkRotation }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Extracting with Gemini…", fontWeight = FontWeight.Bold, color = NomNomBg)
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, tint = if (isOffline) NomNomTextSub else NomNomBg, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isOffline) "Offline" else "Extract Recipe", fontWeight = FontWeight.Bold, color = if (isOffline) NomNomTextSub else NomNomBg, fontSize = 16.sp)
                    }
                }
            }

            if (isOffline) {
                Text(
                    "You need an internet connection to use AI Import.",
                    color = NomNomGold.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Error ─────────────────────────────────────────────────────
            AnimatedVisibility(visible = errorMessage != null) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NomNomErrorSurface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = NomNomError, modifier = Modifier.size(16.dp))
                    Text(errorMessage ?: "", color = NomNomError, fontSize = 13.sp, modifier = Modifier.weight(1f))
                }
            }

            // ── Result preview ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = extractedRecipe != null,
                enter   = fadeIn(tween(300)) + slideInVertically { it / 3 }
            ) {
                extractedRecipe?.let { recipe ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(NomNomSurface)
                            .border(1.dp, NomNomSuccess.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Success header
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = NomNomSuccess, modifier = Modifier.size(20.dp))
                            Text("Recipe Extracted!", color = NomNomSuccess, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        HorizontalDivider(color = NomNomDivider)

                        Text(recipe.title.ifBlank { "Untitled Recipe" }, color = NomNomWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GoldChip("🥕 ${recipe.ingredients.size} ingredients")
                            GoldChip("📋 ${recipe.steps.size} steps")
                            recipe.servings?.let { GoldChip("🍽 $it") }
                        }

                        // Collapsible ingredients
                        CollapsibleSection(
                            title     = "Ingredients",
                            expanded  = expandIngredients,
                            onToggle  = { expandIngredients = !expandIngredients },
                            preview   = recipe.ingredients.take(3).joinToString(", ") +
                                        if (recipe.ingredients.size > 3) "…" else ""
                        ) {
                            recipe.ingredients.forEach { ing ->
                                Text("• $ing", color = NomNomTextSub, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }

                        // Collapsible steps
                        CollapsibleSection(
                            title     = "Steps",
                            expanded  = expandSteps,
                            onToggle  = { expandSteps = !expandSteps },
                            preview   = recipe.steps.take(2).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString(" · ") +
                                        if (recipe.steps.size > 2) "…" else ""
                        ) {
                            recipe.steps.forEachIndexed { i, step ->
                                Text("${i + 1}. $step", color = NomNomTextSub, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                            }
                        }

                        HorizontalDivider(color = NomNomDivider)

                        // Action buttons
                        NomNomButton(
                            onClick  = { onUseRecipe(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Use This Recipe →", fontWeight = FontWeight.Bold, color = NomNomWhite, fontSize = 15.sp)
                        }
                        OutlinedButton(
                            onClick  = { onEditRecipe(recipe) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = BorderStroke(1.dp, NomNomDivider),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NomNomTextSub)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Edit Before Saving", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    preview: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = NomNomWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = NomNomTextSub,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = !expanded) {
            Text(preview, color = NomNomTextDim, fontSize = 12.sp, maxLines = 2)
        }
        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 6.dp), content = content)
        }
    }
}
