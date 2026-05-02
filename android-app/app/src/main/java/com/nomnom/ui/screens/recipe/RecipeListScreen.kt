package com.nomnom.ui.screens.recipe

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.Recipe
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Restaurant
import com.nomnom.data.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CATEGORIES = listOf("All", "Healthy", "One Pot Meal", "Continental", "Indian", "Starter", "Main Course", "Dessert")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit,
    onImportClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: RecipeViewModel,
    authViewModel: AuthViewModel,
    authToken: String,
    userId: String
) {
    val recipes      by viewModel.recipes.collectAsState()
    val isPremium    by authViewModel.isPremium.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val haptic       = LocalHapticFeedback.current
    val scope        = rememberCoroutineScope()

    var searchQuery  by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("All") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.authToken = authToken
        viewModel.userId    = userId
        viewModel.fetchRecipes()
    }

    val filteredRecipes = remember(recipes, searchQuery, activeCategory) {
        recipes.filter { r ->
            val q = searchQuery.trim()
            val matchSearch = q.isBlank() ||
                r.title.contains(q, ignoreCase = true) ||
                r.ingredients.any { it.contains(q, ignoreCase = true) } ||
                r.notes?.contains(q, ignoreCase = true) == true
            
            val matchCategory = if (activeCategory == "All") true
                else r.tags.contains(activeCategory) || r.title.contains(activeCategory, ignoreCase = true)
            
            matchSearch && matchCategory
        }
    }

    // FAB scale animation
    val fabInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val fabPressed by fabInteraction.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        if (fabPressed) 0.92f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "fabScale"
    )
    val fabRotation by animateFloatAsState(
        if (fabPressed) 45f else 0f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "fabRot"
    )

    Scaffold(
        containerColor = CSLinen,
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onAddClick() },
                modifier           = Modifier.scale(fabScale),
                shape              = RoundedCornerShape(16.dp),
                containerColor     = CSSage,
                contentColor       = CSOnSage,
                interactionSource  = fabInteraction
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add Recipe",
                    modifier = Modifier.graphicsLayer { rotationZ = fabRotation }
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = {
                isRefreshing = true
                scope.launch {
                    viewModel.fetchRecipes()
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            Text(
                text       = "Recipes",
                style      = MaterialTheme.typography.displaySmall,
                color      = CSOnBackground,
                fontFamily = NotoSerif,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))
            // ── Search bar ─────────────────────────────────────────────────
            NomNomTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                placeholder   = "Search recipes, ingredients…",
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = CSOutline) },
                trailingIcon  = if (searchQuery.isNotBlank()) ({
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, null, tint = CSOutline, modifier = Modifier.size(18.dp))
                    }
                }) else null,
                singleLine    = true
            )

            // ── Filter chips ───────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATEGORIES.forEach { category ->
                    val icon = when (category) {
                        "Healthy"      -> "🥗 "
                        "One Pot Meal" -> "🍲 "
                        "Continental"  -> "🍕 "
                        "Indian"       -> "🍛 "
                        "Starter"      -> "🍢 "
                        "Main Course"  -> "🥘 "
                        "Dessert"      -> "🍰 "
                        else           -> ""
                    }
                    TagChip(
                        text     = "$icon$category",
                        selected = activeCategory == category,
                        onClick  = { activeCategory = category }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── AI Import Banner ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CSSurfaceLowest)
                    .border(1.dp, CSGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onImportClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(12.dp)
                ) {
                    val sparkle = rememberInfiniteTransition(label = "sparkle")
                    val sparkleScale by sparkle.animateFloat(
                        1f, 1.25f,
                        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        "sparkleScale"
                    )
                    Text("✨", fontSize = 22.sp, modifier = Modifier.scale(sparkleScale))
                    Column(Modifier.weight(1f)) {
                        Text("Import with AI", color = CSGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Paste a URL or text — Gemini fills the form", color = CSOnSurfaceVariant.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = CSGold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Content ────────────────────────────────────────────────────
            val isOffline by viewModel.isOffline.collectAsState()

            when {
                isOffline && recipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Text("☁️", fontSize = 72.sp, modifier = Modifier.alpha(0.3f))
                            Spacer(Modifier.height(16.dp))
                            Text("No Connection", style = MaterialTheme.typography.titleLarge, color = CSOnSurface)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You are currently offline. Connect to the internet to view your recipes, or Go Premium for offline access!",
                                color     = CSOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize  = 14.sp
                            )
                        }
                    }
                }

                isLoading && recipes.isEmpty() -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(4) { SkeletonCard() }
                    }
                }

                recipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Text("🍳", fontSize = 72.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No Recipes Yet",
                                style  = MaterialTheme.typography.titleLarge,
                                color  = CSOnSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap the + button to add your first recipe",
                                color     = CSOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize  = 14.sp
                            )
                        }
                    }
                }

                filteredRecipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No results for \"$searchQuery\"",
                                color     = CSOnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(filteredRecipes, key = { _, r -> r.id }) { index, recipe ->
                            AnimatedListItem(index = index) {
                                RecipeListItem(
                                    recipe = recipe, 
                                    onClick = { onRecipeClick(recipe) },
                                    isPremium = isPremium
                                )
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                    }
                }
            }

            if (errorMessage != null) {
                LaunchedEffect(errorMessage) { viewModel.clearError() }
            }
        }
        } // end PullToRefreshBox
    }
}

@Composable
fun RecipeListItem(recipe: Recipe, onClick: () -> Unit, isPremium: Boolean = false) {
    NomNomCard(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (recipe.imageUrl != null) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(CSSurfaceContainer, CSSurfaceLow)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍳", fontSize = 48.sp, modifier = Modifier.alpha(0.2f))
                    }
                }
                
                // Global/Premium Badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (recipe.isGlobal) {
                        Surface(
                            color = CSSage.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "GLOBAL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (isPremium) {
                        Surface(
                            color = CSGold.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "PRO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val category = recipe.tags.firstOrNull() ?: "RECIPE"
                Text(
                    text = category.uppercase(),
                    color = CSGold,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CSOnSurface,
                    fontFamily = NotoSerif,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule, 
                        null, 
                        tint = CSOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    val totalTime = (recipe.prepTimeMinutes ?: 0) + (recipe.cookTimeMinutes ?: 0)
                    Text(
                        text = "${totalTime} mins",
                        color = CSOnSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (recipe.servings != null) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Restaurant, 
                            null, 
                            tint = CSOnSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${recipe.servings} serving",
                            color = CSOnSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Legacy alias used elsewhere
@Composable
fun RecipeMetadataChip(label: String, icon: String) = RecipeChip(label)
