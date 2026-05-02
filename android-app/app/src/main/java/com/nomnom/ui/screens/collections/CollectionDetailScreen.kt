package com.nomnom.ui.screens.collections

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.Recipe
import com.nomnom.data.model.RecipeCollection
import com.nomnom.data.viewmodel.CollectionViewModel
import com.nomnom.ui.components.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// Helper for high-quality food photography URLs
private fun collectionImageUrl(name: String): String {
    val keywords = listOf("healthy", "salad", "pasta", "steak", "ramen", "dessert", "stew", "sushi", "taco", "soup")
    val keyword = keywords.firstOrNull { name.contains(it, ignoreCase = true) } ?: "food"
    
    val photoId = when (keyword) {
        "healthy" -> "photo-1512621776951-a57141f2eefd"
        "salad"   -> "photo-1512621776951-a57141f2eefd"
        "pasta"   -> "photo-1473093226795-af9932fe5856"
        "steak"   -> "photo-1546241072-48010ad2862c"
        "ramen"   -> "photo-1569718212165-3a8278d5f624"
        "dessert" -> "photo-1563729784474-d77dbb933a9e"
        "stew"    -> "photo-1547592166-23ac45744acd"
        "sushi"   -> "photo-1579871494447-9811cf80d66c"
        "taco"    -> "photo-1565299585323-38d6b0865b47"
        "soup"    -> "photo-1547592166-23ac45744acd"
        else      -> "photo-1504674900247-0877df9cc836" 
    }
    return "https://images.unsplash.com/$photoId?q=80&w=800&auto=format&fit=crop"
}

// Helper for premium 'Liquid Mesh' fallback gradients
private fun detailGradient(name: String): Brush {
    val palettes = listOf(
        listOf(Color(0xFF4A148C), Color(0xFF7B1FA2), Color(0xFFE1BEE7)), // Lavender Silk
        listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFFBBDEFB)), // Ocean Breeze
        listOf(Color(0xFF1B5E20), Color(0xFF388E3C), Color(0xFFC8E6C9)), // Fresh Sage
        listOf(Color(0xFFE65100), Color(0xFFF57C00), Color(0xFFFFE0B2)), // Saffron Sunset
        listOf(Color(0xFF01579B), Color(0xFF0288D1), Color(0xFFB3E5FC)), // Sky Glass
        listOf(Color(0xFF310000), Color(0xFF880E4F), Color(0xFFFF4081)), // Liquid Cherry
        listOf(Color(0xFF33691E), Color(0xFF689F38), Color(0xFFDCEDC8)), // Mossy Morning
        listOf(Color(0xFFBF360C), Color(0xFFE64A19), Color(0xFFFFCCBC))  // Terracotta
    )
    val pair = palettes[name.hashCode().mod(palettes.size).let { if (it < 0) it + palettes.size else it }]
    return Brush.linearGradient(
        0.0f to pair[0],
        0.6f to pair[1],
        1.0f to pair[2],
        start = androidx.compose.ui.geometry.Offset.Zero,
        end   = androidx.compose.ui.geometry.Offset.Infinite
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collection: RecipeCollection,
    allRecipes: List<Recipe>,
    viewModel: CollectionViewModel,
    onBack: () -> Unit,
    onAddRecipe: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val collectionRecipeIds by viewModel.collectionRecipeIds.collectAsState()
    val isLoading           by viewModel.isLoading.collectAsState()
    val errorMessage        by viewModel.errorMessage.collectAsState()
    val haptic              = LocalHapticFeedback.current
    var showAddSheet        by remember { mutableStateOf(false) }
    var showRenameDialog    by remember { mutableStateOf(false) }
    var newName             by remember { mutableStateOf(collection.name) }
    val snackbarHostState   = remember { SnackbarHostState() }

    // Use derivedStateOf for instant updates when collectionRecipeIds changes
    val recipesInCollection by remember(allRecipes, collectionRecipeIds) {
        derivedStateOf { allRecipes.filter { it.id in collectionRecipeIds } }
    }
    val recipesNotInCollection by remember(allRecipes, collectionRecipeIds) {
        derivedStateOf { allRecipes.filter { it.id !in collectionRecipeIds } }
    }

    LaunchedEffect(collection.id) {
        viewModel.fetchCollectionRecipeIds(collection.id)
    }

    // Error handling with Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCollectionRecipeIds() }
    }

    Scaffold(
        containerColor = CSBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // Slightly taller for more impact
                    .background(detailGradient(collection.name)) // Fallback
            ) {
                // CINEMATIC BACKDROP
                AsyncImage(
                    model = collectionImageUrl(collection.name),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // CINEMATIC SCRIM
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                        
                        Row {
                            IconButton(
                                onClick = { showRenameDialog = true },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, "Rename", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            IconButton(
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); showAddSheet = true },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.25f), CircleShape)
                            ) {
                                Icon(Icons.Default.PlaylistAdd, "Add", tint = Color.White)
                            }
                        }
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    Text(
                        collection.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontFamily = NotoSerif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${recipesInCollection.size} CURATED RECIPES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->

        if (isLoading && recipesInCollection.isEmpty()) {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(top = 240.dp).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) { SkeletonCard() }
            }
        } else if (recipesInCollection.isEmpty()) {
            // Premium Empty State
            Box(
                Modifier.fillMaxSize().padding(top = 240.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(40.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = CSSage.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌿", fontSize = 56.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Empty Sanctuary",
                        style  = MaterialTheme.typography.headlineSmall,
                        color  = CSOnBackground,
                        fontFamily = NotoSerif
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This collection is waiting for your favorite recipes. Tap below to start adding.",
                        color     = CSOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    NomNomButton(
                        onClick  = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); showAddSheet = true },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.PlaylistAdd, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add from Library", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(top = 230.dp),
                contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(recipesInCollection, key = { _, r -> r.id }) { index, recipe ->
                    AnimatedListItem(index = index) {
                        CollectionRecipeRow(
                            recipe    = recipe,
                            onTap     = { onRecipeClick(recipe) },
                            onRemove  = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.removeRecipeFromCollection(collection.id, recipe.id) 
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Collection", fontFamily = NotoSerif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a new name for this collection.", color = CSOnSurfaceVariant, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    NomNomTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = "e.g. Healthy Favorites",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.renameCollection(collection.id, newName)
                        showRenameDialog = false
                    }
                }) {
                    Text("Save", color = CSSage, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = CSOutline)
                }
            },
            containerColor = CSSurfaceLowest,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showAddSheet) {
        AddToCollectionSheet(
            recipes      = recipesNotInCollection,
            totalInLibrary = allRecipes.size,
            onAdd        = { recipe ->
                viewModel.addRecipeToCollection(collection.id, recipe.id)
            },
            onAddRecipeClick = {
                showAddSheet = false
                onAddRecipe()
            },
            onDismiss    = { showAddSheet = false }
        )
    }
}

@Composable
private fun CollectionRecipeRow(
    recipe: Recipe,
    onTap: () -> Unit,
    onRemove: () -> Unit
) {
    NomNomCard(onClick = onTap) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecipeThumbnail(recipe.imageUrl, size = 52.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    color      = CSOnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    maxLines   = 1
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(12.dp), tint = CSOutline)
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.cookTimeMinutes ?: 0}m", color = CSOnSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.RestaurantMenu, null, modifier = Modifier.size(12.dp), tint = CSOutline)
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.ingredients.size} ingr", color = CSOnSurfaceVariant, fontSize = 12.sp)
                }
            }
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.background(CSError.copy(alpha = 0.1f), CircleShape).size(32.dp)
            ) {
                Icon(Icons.Default.Close, "Remove", tint = CSError, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToCollectionSheet(
    recipes: List<Recipe>,
    totalInLibrary: Int,
    onAdd: (Recipe) -> Unit,
    onAddRecipeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val addedThisSession = remember { mutableStateListOf<String>() }
    val haptic           = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = CSSurfaceLowest,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = CSOutline) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text(
                "Add to Collection",
                style    = MaterialTheme.typography.headlineSmall,
                color    = CSOnSurface,
                fontFamily = NotoSerif,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Select recipes from your library to include in this category.",
                style = MaterialTheme.typography.bodyMedium,
                color = CSOnSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            if (totalInLibrary == 0) {
                // SPECIAL CASE: Library is completely empty
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍳", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Your library is empty",
                            fontWeight = FontWeight.Bold,
                            color = CSOnSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Add your first recipe to start organizing.",
                            color = CSOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        NomNomButton(onClick = onAddRecipeClick) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create New Recipe")
                        }
                    }
                }
            } else if (recipes.isEmpty() && addedThisSession.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "All caught up!",
                            fontWeight = FontWeight.Bold,
                            color = CSOnSurface
                        )
                        Text(
                            "Every recipe in your library is already here.",
                            color = CSOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        val isAdded = recipe.id in addedThisSession
                        NomNomCard(
                            onClick         = { /* Card click disabled to prioritize button */ },
                            containerColor  = if (isAdded) CSSage.copy(alpha = 0.05f) else CSSurfaceLow
                        ) {
                            Row(
                                modifier          = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RecipeThumbnail(recipe.imageUrl, size = 48.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        recipe.title,
                                        color      = CSOnSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 15.sp,
                                        maxLines   = 1
                                    )
                                }
                                
                                if (isAdded) {
                                    Surface(
                                        color = CSSage.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, null, tint = CSSage, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Added", color = CSSage, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onAdd(recipe)
                                            addedThisSession.add(recipe.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CSSage),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
