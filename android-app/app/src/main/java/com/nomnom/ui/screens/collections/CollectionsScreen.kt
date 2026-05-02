package com.nomnom.ui.screens.collections

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.*
import com.nomnom.data.viewmodel.CollectionViewModel
import com.nomnom.ui.components.*
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Restaurant

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun collectionImageUrl(name: String): String {
    val keywords = listOf("healthy", "salad", "pasta", "steak", "ramen", "dessert", "stew", "sushi", "taco", "soup")
    val keyword = keywords.firstOrNull { name.contains(it, ignoreCase = true) } ?: "food"
    
    // Using high-quality curated Unsplash IDs for a premium look
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
        else      -> "photo-1504674900247-0877df9cc836" // Generic luxury food
    }
    return "https://images.unsplash.com/$photoId?q=80&w=600&auto=format&fit=crop"
}

private fun collectionGradient(name: String): Brush {
    val palettes = listOf(
        listOf(Color(0xFF4A148C), Color(0xFF7B1FA2), Color(0xFFE1BEE7)), // Lavender Silk
        listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFFBBDEFB)), // Ocean Breeze
        listOf(Color(0xFF1B5E20), Color(0xFF388E3C), Color(0xFFC8E6C9)), // Fresh Sage
        listOf(Color(0xFFE65100), Color(0xFFF57C00), Color(0xFFFFE0B2)), // Saffron Sunset
        listOf(Color(0xFF01579B), Color(0xFF0288D1), Color(0xFFB3E5FC)), // Sky Glass
        listOf(Color(0xFF310000), Color(0xFF880E4F), Color(0xFFFF4081)), // Liquid Cherry (Fixed)
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

private val collectionEmojis = listOf("🥗", "🍕", "🍝", "🥩", "🍜", "🧁", "🥘", "🍱", "🌮", "🍲")
private fun collectionEmoji(name: String): String {
    val idx = name.hashCode().mod(collectionEmojis.size).let { if (it < 0) it + collectionEmojis.size else it }
    return collectionEmojis[idx]
}
// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    authToken: String,
    userId: String,
    collectionViewModel: CollectionViewModel,
    onCollectionClick: (com.nomnom.data.model.RecipeCollection) -> Unit = {}
) {
    val collections  by collectionViewModel.collections.collectAsState()
    val isLoading    by collectionViewModel.isLoading.collectAsState()
    val haptic       = LocalHapticFeedback.current
    val scope        = rememberCoroutineScope()

    var searchQuery   by remember { mutableStateOf("") }
    var showCreate    by remember { mutableStateOf(false) }
    var newName       by remember { mutableStateOf("") }
    var newDesc       by remember { mutableStateOf("") }
    var deleteTarget  by remember { mutableStateOf<String?>(null) }
    var renameTarget  by remember { mutableStateOf<com.nomnom.data.model.RecipeCollection?>(null) }
    var renameValue   by remember { mutableStateOf("") }
    var isRefreshing  by remember { mutableStateOf(false) }

    val filteredCollections = remember(collections, searchQuery) {
        if (searchQuery.isBlank()) collections
        else collections.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        collectionViewModel.authToken = authToken
        collectionViewModel.userId    = userId
        collectionViewModel.fetchCollections()
    }

    Scaffold(
        containerColor = CSBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(CSSurfaceLowest)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "NomNom",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = CSSage,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "My Collections",
                            style      = MaterialTheme.typography.displaySmall,
                            color      = CSOnBackground,
                            fontFamily = NotoSerif
                        )
                    }
                    
                    IconButton(
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); showCreate = true },
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .background(CSSage, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Rounded.Add, "New Collection", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your personalized sanctuary of curated recipes and culinary inspirations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CSOnSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Modern Search Bar
                NomNomTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = "Search your collections...",
                    leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = CSOutline) },
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = {
                isRefreshing = true
                scope.launch {
                    collectionViewModel.fetchCollections()
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(padding)
        ) {
            when {
                isLoading && collections.isEmpty() -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement   = Arrangement.spacedBy(20.dp),
                        contentPadding  = PaddingValues(top = 16.dp)
                    ) {
                        items(3) { 
                            SkeletonCard(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(24.dp))
                            ) 
                        }
                    }
                }

                filteredCollections.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Text(if (searchQuery.isEmpty()) "📂" else "🔍", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isEmpty()) "No Collections Yet" else "No Results Found",
                                style      = MaterialTheme.typography.titleLarge,
                                color      = CSOnSurface,
                                fontFamily = NotoSerif
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isEmpty()) "Organize your recipes into beautiful groups." else "Try a different search term.",
                                color      = CSOnSurfaceVariant,
                                textAlign  = TextAlign.Center,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement   = Arrangement.spacedBy(20.dp),
                        contentPadding  = PaddingValues(top = 16.dp, bottom = 100.dp)
                    ) {
                        itemsIndexed(filteredCollections, key = { _, c -> c.id }) { index, collection ->
                            AnimatedListItem(index = index) {
                                PremiumCollectionCard(
                                    collection = collection,
                                    onClick    = { onCollectionClick(collection) },
                                    onDelete   = { deleteTarget = collection.id },
                                    onRename   = {
                                        renameTarget = collection
                                        renameValue  = collection.name
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false; newName = ""; newDesc = "" },
            containerColor   = CSSurfaceLowest,
            title            = { Text("New Collection", fontFamily = NotoSerif, fontWeight = FontWeight.Bold) },
            text             = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NomNomTextField(
                        value         = newName,
                        onValueChange = { newName = it },
                        label         = "Collection Name",
                        placeholder   = "e.g. Weekend Brunch",
                        modifier      = Modifier.fillMaxWidth()
                    )
                    NomNomTextField(
                        value         = newDesc,
                        onValueChange = { newDesc = it },
                        label         = "Description",
                        placeholder   = "Short note (optional)",
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = false
                    )
                }
            },
            confirmButton = {
                NomNomButton(
                    onClick = { collectionViewModel.createCollection(newName, newDesc); showCreate = false },
                    enabled = newName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel", color = CSOnSurfaceVariant) }
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = CSSurfaceLowest,
            title            = { Text("Delete Collection?", fontFamily = NotoSerif) },
            text             = { Text("Are you sure? Recipes inside will remain in your library.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                NomNomButton(
                    onClick = { collectionViewModel.deleteCollection(deleteTarget!!); deleteTarget = null },
                    containerColor = CSError
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = CSOnSurfaceVariant) }
            }
        )
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor   = CSSurfaceLowest,
            title            = { Text("Rename Collection", fontFamily = NotoSerif) },
            text             = {
                NomNomTextField(
                    value         = renameValue,
                    onValueChange = { renameValue = it },
                    placeholder   = "New name",
                    modifier      = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                NomNomButton(
                    onClick = { collectionViewModel.renameCollection(renameTarget!!.id, renameValue); renameTarget = null },
                    enabled = renameValue.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = CSOnSurfaceVariant) }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PremiumCollectionCard(
    collection: com.nomnom.data.model.RecipeCollection,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val gradient = collectionGradient(collection.name)
    val emoji    = collectionEmoji(collection.name)
    val haptic   = LocalHapticFeedback.current

    // Extract the primary color for the shadow
    val primaryColor = remember(collection.name) {
        val palettes = listOf(
            Color(0xFF6A20A0), Color(0xFF2060A0), Color(0xFF208060), 
            Color(0xFF804020), Color(0xFF4020A0), Color(0xFF802040), 
            Color(0xFF408020), Color(0xFF806020)
        )
        palettes[collection.name.hashCode().mod(palettes.size).let { if (it < 0) it + palettes.size else it }]
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(
                elevation = 12.dp,
                shape     = RoundedCornerShape(24.dp),
                spotColor = primaryColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick     = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
                onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); showMenu = true }
            )
    ) {
        // CINEMATIC BACKDROP
        AsyncImage(
            model = collectionImageUrl(collection.name),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // LUXURY OVERLAY: Deep Cinematic Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.1f),
                        0.6f to Color.Black.copy(alpha = 0.3f),
                        1f to Color.Black.copy(alpha = 0.8f)
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title & Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    collection.name,
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Color.White,
                    fontFamily = NotoSerif,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "COLLECTION", // Could fetch count here if available
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            // Menu Button at the bottom right
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded         = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor   = CSSurfaceLowest,
            modifier         = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            DropdownMenuItem(
                text    = { Text("Rename", color = CSOnSurface, fontWeight = FontWeight.Medium) },
                onClick = { showMenu = false; onRename() },
                leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = CSSage) }
            )
            Divider(color = CSOutline.copy(alpha = 0.1f))
            DropdownMenuItem(
                text    = { Text("Delete", color = CSError, fontWeight = FontWeight.Medium) },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = CSError) }
            )
        }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        CSSurfaceLowest,
        CSSurfaceLowest.copy(alpha = 0.5f),
        CSSurfaceLowest,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start  = androidx.compose.ui.geometry.Offset.Zero,
        end    = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
    )
}
