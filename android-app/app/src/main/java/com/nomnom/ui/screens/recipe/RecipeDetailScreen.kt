package com.nomnom.ui.screens.recipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.Recipe
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*

private enum class DetailTab { Ingredients, Directions, Notes }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    authToken: String,
    userId: String,
    viewModel: RecipeViewModel,
    collectionViewModel: com.nomnom.data.viewmodel.CollectionViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onCookMode: (String) -> Unit = {}
) {
    val recipe       by viewModel.selectedRecipe.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val collections  by collectionViewModel.collections.collectAsState()
    
    var showCollectionPicker by remember { mutableStateOf(false) }
    val context      = LocalContext.current
    val haptic       = LocalHapticFeedback.current
    var showDelete   by remember { mutableStateOf(false) }
    var selectedTab  by remember { mutableStateOf(DetailTab.Ingredients) }
    var servings     by remember { mutableStateOf(recipe?.servings ?: 2) }
    val checkStates  = remember(recipe?.ingredients) {
        mutableStateMapOf<Int, Boolean>()
    }

    LaunchedEffect(recipeId) {
        if (recipe?.id != recipeId) {
            viewModel.authToken = authToken
            viewModel.fetchRecipe(recipeId)
        }
    }

    LaunchedEffect(recipe) {
        recipe?.servings?.let { servings = it }
    }

    val scrollState = rememberScrollState()

    // Parallax offset for hero
    val heroParallax by remember {
        derivedStateOf { scrollState.value * 0.35f }
    }

    Scaffold(
        containerColor = CSSurface,
    ) { _ ->

        if (isLoading || recipe == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CSSage)
            }
            return@Scaffold
        }

        val r = recipe!!
        val scaleFactor = if ((r.servings ?: 1) > 0) servings.toFloat() / (r.servings ?: servings).toFloat() else 1f

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // ── Hero zone ─────────────────────────────────────────────
                RecipeHeroBox(
                    imageUrl = r.imageUrl,
                    height   = 280.dp,
                    modifier = Modifier.graphicsLayer { translationY = -heroParallax }
                )

                // ── Floating content card ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-28).dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(CSSurface)
                        .padding(top = 24.dp, start = 20.dp, end = 20.dp)
                ) {
                    Column {
                        // Title
                        Text(
                            text  = r.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = CSOnSurface
                        )

                        Spacer(Modifier.height(12.dp))

                        // Meta pills row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            r.prepTimeMinutes?.let { RecipeChip("⏱ Prep ${it}m") }
                            r.cookTimeMinutes?.let { RecipeChip("🔥 Cook ${it}m") }
                            
                            Spacer(Modifier.weight(1f))
                            
                            // Save to Collection
                            IconButton(
                                onClick = { 
                                    collectionViewModel.authToken = authToken
                                    collectionViewModel.userId = userId
                                    collectionViewModel.fetchCollections()
                                    showCollectionPicker = true 
                                }, 
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.BookmarkBorder, "Save", tint = CSGold)
                            }

                            if (r.userId == userId) {
                                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, "Edit", tint = CSSage)
                                }
                                IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, "Delete", tint = CSError.copy(alpha = 0.6f))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Servings scaler + Share
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Servings
                            Row(
                                verticalAlignment   = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier            = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CSSurfaceLowest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (servings > 1) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); servings-- } },
                                    modifier = Modifier.size(24.dp)
                                ) { Icon(Icons.Default.Remove, null, tint = CSSage) }

                                Text("$servings", fontWeight = FontWeight.Bold, color = CSOnSurface)

                                IconButton(
                                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); servings++ },
                                    modifier = Modifier.size(24.dp)
                                ) { Icon(Icons.Default.Add, null, tint = CSSage) }

                                Spacer(Modifier.width(4.dp))
                                Text("servings", fontSize = 12.sp, color = CSOnSurfaceVariant)
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    shareRecipe(context, r)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(CSSage.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Share, "Share", tint = CSSage)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Tab row ───────────────────────────────────────
                        val tabs = listOf(DetailTab.Ingredients, DetailTab.Directions) +
                                if (r.notes.isNullOrBlank()) emptyList() else listOf(DetailTab.Notes)

                        val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
                        TabRow(
                            selectedTabIndex = selectedIndex,
                            containerColor   = Color.Transparent,
                            contentColor     = CSSage,
                            indicator        = { positions ->
                                if (selectedIndex < positions.size) {
                                    Box(
                                        Modifier
                                            .tabIndicatorOffset(positions[selectedIndex])
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(CSSage)
                                    )
                                }
                            },
                            divider = { HorizontalDivider(color = CSOutline) }
                        ) {
                            tabs.forEach { tab ->
                                val label = when (tab) {
                                    DetailTab.Ingredients -> "Ingredients"
                                    DetailTab.Directions  -> "Directions"
                                    DetailTab.Notes       -> "Notes"
                                }
                                Tab(
                                    selected = selectedTab == tab,
                                    onClick  = { selectedTab = tab },
                                    text     = {
                                        Text(
                                            label,
                                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                            fontSize   = 14.sp
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Tab content ───────────────────────────────────
                        AnimatedContent(
                            targetState   = selectedTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally { if (targetState > initialState) it / 8 else -it / 8 }) togetherWith
                                (fadeOut(tween(150)) + slideOutHorizontally { if (targetState > initialState) -it / 8 else it / 8 })
                            },
                            label         = "tabContent"
                        ) { tab ->
                            when (tab) {
                                DetailTab.Ingredients -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        r.ingredients.forEachIndexed { idx, ing ->
                                            val scaled = if (scaleFactor != 1f) scaleIngredient(ing, scaleFactor) else ing
                                            IngredientCheckRow(
                                                text           = scaled,
                                                checked        = checkStates[idx] == true,
                                                onCheckedChange = { checkStates[idx] = it; if (it) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                                            )
                                            if (idx < r.ingredients.lastIndex) {
                                                HorizontalDivider(color = CSOutline.copy(alpha = 0.5f), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }

                                DetailTab.Directions -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        r.steps.forEachIndexed { idx, step ->
                                            StepCard(number = idx + 1, text = step)
                                        }
                                    }
                                }

                                DetailTab.Notes -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CSSurfaceLowest)
                                            .padding(16.dp)
                                    ) {
                                        Text(r.notes ?: "", color = CSOnSurfaceVariant, fontSize = 15.sp, lineHeight = 22.sp)
                                    }
                                }
                            }
                        }

                        // Source URL
                        if (!r.sourceUrl.isNullOrBlank()) {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier          = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CSSurfaceLowest)
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.sourceUrl)))
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Link, null, tint = CSSage, modifier = Modifier.size(16.dp))
                                Text("View Original Recipe", color = CSSage, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline)
                            }
                        }

                        Spacer(Modifier.height(100.dp))
                    }
                }
            }

            // ── Floating back button ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CSSurfaceLowest.copy(alpha = 0.85f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = CSOnSurface, modifier = Modifier.size(20.dp))
            }

            // ── Cook Mode FAB ─────────────────────────────────────────────
            Box(
                modifier        = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                NomNomButton(
                    onClick  = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCookMode(recipeId) 
                    },
                    modifier = Modifier.width(220.dp)
                ) {
                    Text("🍳", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Cook Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // ── Delete dialog ─────────────────────────────────────────────────────
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor   = CSSurfaceLowest,
            title            = { Text("Delete Recipe?", color = CSOnSurface, fontWeight = FontWeight.Bold) },
            text             = { Text("This cannot be undone.", color = CSOnSurfaceVariant) },
            confirmButton    = {
                NomNomButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteRecipe(recipeId)
                        showDelete = false
                        onDeleted() 
                    },
                    containerColor = CSError
                ) { Text("Delete", color = CSOnError) }
            },
            dismissButton    = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel", color = CSOnSurfaceVariant) }
            }
        )
    }

    // ── Collection Picker dialog ──────────────────────────────────────────
    if (showCollectionPicker) {
        AlertDialog(
            onDismissRequest = { showCollectionPicker = false },
            containerColor   = CSSurfaceLowest,
            title            = { Text("Save to Collection", color = CSOnSurface, fontWeight = FontWeight.Bold) },
            text             = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (collections.isEmpty()) {
                        Text("You don't have any collections yet.", color = CSOnSurfaceVariant)
                    } else {
                        collections.forEach { coll ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        collectionViewModel.addRecipeToCollection(coll.id, recipeId)
                                        showCollectionPicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, null, tint = CSSage, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(coll.name, color = CSOnSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCollectionPicker = false }) {
                    Text("Close", color = CSOnSurfaceVariant)
                }
            }
        )
    }
}

private fun shareRecipe(context: Context, r: Recipe) {
    val shareText = buildString {
        append("🍴 ${r.title} 🍴\n\n")
        append("Ingredients:\n")
        r.ingredients.forEach { append("• $it\n") }
        append("\nDirections:\n")
        r.steps.forEachIndexed { i, step -> append("${i + 1}. $step\n") }
        append("\nShared from NomNom Recipe Manager 🍳")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Recipe"))
}

private fun scaleIngredient(text: String, factor: Float): String {
    val regex = Regex("^(\\d+(?:[./]\\d+)?)\\s*")
    val match = regex.find(text) ?: return text
    val raw = match.groupValues[1]
    val num = if ('/' in raw) {
        val parts = raw.split('/')
        parts[0].toFloatOrNull()?.div(parts[1].toFloatOrNull() ?: 1f) ?: 0f
    } else raw.toFloatOrNull() ?: 0f
    val scaled  = num * factor
    val rounded = if (scaled == scaled.toLong().toFloat()) scaled.toLong().toString()
                  else "%.1f".format(scaled)
    return "$rounded ${text.substring(match.range.last + 1)}"
}
