package com.nomnom.ui.screens.recipe

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.AiExtractedRecipe
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*
import kotlinx.coroutines.launch
import sh.calvin.reorderable.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

private val QUICK_TAGS = listOf("Quick", "Healthy", "Budget", "Vegetarian", "Spicy", "Breakfast", "Dessert")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RecipeViewModel,
    authToken: String = "",
    userId: String = "",
    prefill: AiExtractedRecipe? = null
) {
    val haptic = LocalHapticFeedback.current
    var title       by remember { mutableStateOf(prefill?.title ?: "") }
    var ingredients by remember { mutableStateOf(prefill?.ingredients ?: listOf("")) }
    var steps       by remember { mutableStateOf(prefill?.steps ?: listOf("")) }
    var prepTime    by remember { mutableStateOf(prefill?.prepTimeMinutes?.toString() ?: "") }
    var cookTime    by remember { mutableStateOf(prefill?.cookTimeMinutes?.toString() ?: "") }
    var servings    by remember { mutableStateOf(prefill?.servings?.toString() ?: "") }
    var notes       by remember { mutableStateOf(prefill?.notes ?: "") }
    var imageUrl    by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var saveError    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) { saveError = errorMessage; viewModel.clearError() }
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope      = rememberCoroutineScope()

    val stepTitles = listOf("Title", "Ingredients", "Steps", "Details")
    val stepIcons  = listOf(Icons.Default.DriveFileRenameOutline, Icons.Default.FormatListBulleted,
                             Icons.Default.ListAlt, Icons.Default.Tune)

    fun canAdvance(page: Int) = when (page) {
        0 -> title.isNotBlank()
        1 -> ingredients.any { it.isNotBlank() }
        2 -> steps.any { it.isNotBlank() }
        else -> true
    }

    fun save() {
        if (title.isBlank()) {
            saveError = "Title is required"
            scope.launch { pagerState.animateScrollToPage(0) }
            return
        }
        if (title.length > 100) {
            saveError = "Title is too long (max 100 chars)"
            scope.launch { pagerState.animateScrollToPage(0) }
            return
        }
        if (ingredients.none { it.isNotBlank() }) {
            saveError = "At least one ingredient is required"
            scope.launch { pagerState.animateScrollToPage(1) }
            return
        }
        if (steps.none { it.isNotBlank() }) {
            saveError = "At least one step is required"
            scope.launch { pagerState.animateScrollToPage(2) }
            return
        }
        if (notes.length > 2000) {
            saveError = "Notes are too long (max 2000 chars)"
            scope.launch { pagerState.animateScrollToPage(3) }
            return
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.authToken = authToken
        viewModel.userId    = userId
        saveError           = null
        val finalNotes = notes.ifBlank { null }
        viewModel.createRecipe(
            title           = title.trim(),
            ingredients     = ingredients.filter { it.isNotBlank() },
            steps           = steps.filter { it.isNotBlank() },
            sourceUrl       = null,
            sourceType      = null,
            prepTimeMinutes = prepTime.toIntOrNull(),
            cookTimeMinutes = cookTime.toIntOrNull(),
            servings        = servings.toIntOrNull(),
            notes           = finalNotes,
            imageUrl        = imageUrl.ifBlank { null },
            tags            = selectedTags.toList(),
            onSuccess       = { onSaved() }
        )
    }

    Scaffold(
        containerColor = CSSurface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Recipe", style = MaterialTheme.typography.titleLarge, color = CSOnSurface)
                        Text(stepTitles[pagerState.currentPage], fontSize = 12.sp, color = CSOnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = CSOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CSSurfaceLowest)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Step indicator ─────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(CSSurfaceLowest)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                WizardStepIndicator(totalSteps = 4, currentStep = pagerState.currentPage)
                Text(
                    "${pagerState.currentPage + 1}/4",
                    color      = CSOnSurfaceVariant,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Error banner ───────────────────────────────────────────────
            AnimatedVisibility(visible = saveError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CSSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = CSError, modifier = Modifier.size(16.dp))
                    Text(saveError ?: "", color = CSError, fontSize = 13.sp)
                }
            }

            // ── Pager ──────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> TitlePage(title = title, onTitleChange = { title = it })
                    1 -> IngredientsPage(ingredients = ingredients, onUpdate = { ingredients = it })
                    2 -> StepsPage(steps = steps, onUpdate = { steps = it })
                        3 -> DetailsPage(
                            prepTime    = prepTime, onPrepChange = { prepTime = it },
                            cookTime    = cookTime, onCookChange = { cookTime = it },
                            servings    = servings, onServChange = { servings = it },
                            notes       = notes, onNotesChange = { notes = it },
                            imageUrl    = imageUrl, onImageChange = { imageUrl = it },
                            selectedTags = selectedTags, onTagToggle = { tag ->
                                selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                            }
                        )
                }
            }

            // ── Navigation buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CSSurfaceLowest)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, CSOutline),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = CSOnSurface)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Back")
                    }
                }

                NomNomButton(
                    onClick  = {
                        if (pagerState.currentPage < 3) {
                            if (canAdvance(pagerState.currentPage)) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        } else save()
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CSOnSage, strokeWidth = 2.dp)
                    } else if (pagerState.currentPage < 3) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save Recipe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ── Page composables ──────────────────────────────────────────────────────────

@Composable
internal fun TitlePage(title: String, onTitleChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("What's the recipe called?", style = MaterialTheme.typography.titleMedium, color = CSOnSurface)
        NomNomTextField(
            value         = title,
            onValueChange = onTitleChange,
            placeholder   = "e.g. Chicken Biryani",
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun IngredientsPage(ingredients: List<String>, onUpdate: (List<String>) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onUpdate(ingredients.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }.toList())
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LazyColumn(
        state           = lazyListState,
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Add ingredients", style = MaterialTheme.typography.titleMedium, color = CSOnSurface) }
        items(ingredients, key = { it }) { ing ->
            val idx = ingredients.indexOf(ing)
            ReorderableItem(reorderState, key = ing) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(CSSurface, RoundedCornerShape(8.dp)).padding(elevation)
                ) {
                    Icon(
                        Icons.Default.DragHandle, 
                        null, 
                        tint = CSOutline, 
                        modifier = Modifier.draggableHandle().padding(8.dp)
                    )
                    Text("${idx + 1}.", color = CSOnSurfaceVariant, fontSize = 14.sp, modifier = Modifier.width(26.dp))
                    NomNomTextField(
                        value         = ing,
                        onValueChange = { newValue -> onUpdate(ingredients.toMutableList().also { it[idx] = newValue }) },
                        placeholder   = "e.g. 2 cups flour",
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    if (ingredients.size > 1) {
                        IconButton(onClick = { onUpdate(ingredients.toMutableList().also { it.removeAt(idx) }) }) {
                            Icon(Icons.Default.Delete, null, tint = CSError.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        item {
            TextButton(onClick = { onUpdate(ingredients.toMutableList().also { it.add("") }) }) {
                Icon(Icons.Default.Add, null, tint = CSSage)
                Spacer(Modifier.width(4.dp))
                Text("Add Ingredient", color = CSSage)
            }
        }
    }
}

@Composable
internal fun StepsPage(steps: List<String>, onUpdate: (List<String>) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onUpdate(steps.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }.toList())
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LazyColumn(
        state           = lazyListState,
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Add steps", style = MaterialTheme.typography.titleMedium, color = CSOnSurface) }
        items(steps, key = { it }) { step ->
            val idx = steps.indexOf(step)
            ReorderableItem(reorderState, key = step) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.background(CSSurface, RoundedCornerShape(8.dp)).padding(elevation)
                ) {
                    Icon(
                        Icons.Default.DragHandle, 
                        null, 
                        tint = CSOutline, 
                        modifier = Modifier.draggableHandle().padding(top = 18.dp, start = 8.dp, end = 8.dp)
                    )
                    Box(
                        modifier        = Modifier.padding(top = 14.dp).size(26.dp)
                            .background(CSSage, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${idx + 1}", color = CSOnSage, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    NomNomTextField(
                        value         = step,
                        onValueChange = { new -> onUpdate(steps.toMutableList().also { it[idx] = new }) },
                        placeholder   = "Describe step ${idx + 1}…",
                        singleLine    = false,
                        maxLines      = 4,
                        modifier      = Modifier.weight(1f)
                    )
                    if (steps.size > 1) {
                        IconButton(onClick = { onUpdate(steps.toMutableList().also { it.removeAt(idx) }) }) {
                            Icon(Icons.Default.Delete, null, tint = CSError.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        item {
            TextButton(onClick = { onUpdate(steps.toMutableList().also { it.add("") }) }) {
                Icon(Icons.Default.Add, null, tint = CSSage)
                Spacer(Modifier.width(4.dp))
                Text("Add Step", color = CSSage)
            }
        }
    }
}

@Composable
internal fun DetailsPage(
    prepTime: String, onPrepChange: (String) -> Unit,
    cookTime: String, onCookChange: (String) -> Unit,
    servings: String, onServChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    imageUrl: String, onImageChange: (String) -> Unit,
    selectedTags: Set<String>, onTagToggle: (String) -> Unit
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Details & Tags", style = MaterialTheme.typography.titleMedium, color = CSOnSurface) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NomNomTextField(value = prepTime, onValueChange = onPrepChange, label = "Prep (min)", placeholder = "15",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                NomNomTextField(value = cookTime, onValueChange = onCookChange, label = "Cook (min)", placeholder = "30",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                NomNomTextField(value = servings, onValueChange = onServChange, label = "Servings", placeholder = "4",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Image URL (Supabase)", color = CSOnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            NomNomTextField(
                value         = imageUrl,
                onValueChange = onImageChange,
                placeholder   = "https://.../storage/v1/object/public/recipes/image.jpg",
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("Tags", color = CSOnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QUICK_TAGS.forEach { tag ->
                    TagChip(text = tag, selected = tag in selectedTags, onClick = { onTagToggle(tag) })
                }
            }
        }

        item {
            Text("Notes (optional)", color = CSOnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            NomNomTextField(
                value         = notes,
                onValueChange = onNotesChange,
                placeholder   = "Any tips, variations, or personal touches…",
                singleLine    = false,
                maxLines      = 5,
                modifier      = Modifier.fillMaxWidth().height(120.dp)
            )
        }
    }
}
