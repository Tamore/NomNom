package com.nomnom.ui.screens.recipe

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.*
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: String,
    authToken: String,
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val errorMessage   by viewModel.errorMessage.collectAsState()

    var title       by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.title ?: "") }
    var prepTime    by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.prepTimeMinutes?.toString() ?: "") }
    var cookTime    by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.cookTimeMinutes?.toString() ?: "") }
    var servings    by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.servings?.toString() ?: "") }
    var notes       by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.notes ?: "") }
    var ingredients by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.ingredients ?: listOf("")) }
    var steps       by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.steps ?: listOf("")) }
    var imageUrl    by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.imageUrl ?: "") }
    var selectedTags by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.tags?.toSet() ?: emptySet()) }
    var saveError   by remember { mutableStateOf<String?>(null) }
    val haptic      = LocalHapticFeedback.current

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) { saveError = errorMessage; viewModel.clearError() }
    }

    if (selectedRecipe == null) {
        Box(Modifier.fillMaxSize().background(CSSurface), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CSSage)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope      = rememberCoroutineScope()
    val stepTitles = listOf("Title", "Ingredients", "Steps", "Details")

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
        val finalNotes = notes.ifBlank { null }
        viewModel.updateRecipe(
            id              = recipeId,
            title           = title.trim(),
            ingredients     = ingredients.filter { it.isNotBlank() },
            steps           = steps.filter { it.isNotBlank() },
            sourceUrl       = null,
            prepTimeMinutes = prepTime.toIntOrNull(),
            cookTimeMinutes = cookTime.toIntOrNull(),
            servings        = servings.toIntOrNull(),
            notes           = finalNotes,
            imageUrl        = imageUrl.ifBlank { null },
            tags            = selectedTags.toList()
        )
        onSaved()
    }

    Scaffold(
        containerColor = CSSurface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Edit Recipe", style = MaterialTheme.typography.titleLarge, color = CSOnSurface)
                        Text(stepTitles[pagerState.currentPage], fontSize = 12.sp, color = CSOnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = CSOnSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CSSurfaceLowest)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth().background(CSSurfaceLowest).padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                WizardStepIndicator(totalSteps = 4, currentStep = pagerState.currentPage)
                Text("${pagerState.currentPage + 1}/4", color = CSOnSurfaceVariant, fontSize = 12.sp)
            }

            AnimatedVisibility(visible = saveError != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(CSSurfaceVariant).padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = CSError, modifier = Modifier.size(16.dp))
                    Text(saveError ?: "", color = CSError, fontSize = 13.sp)
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f), userScrollEnabled = false) { page ->
                when (page) {
                    0 -> TitlePage(title = title, onTitleChange = { title = it })
                    1 -> IngredientsPage(ingredients = ingredients, onUpdate = { ingredients = it })
                    2 -> StepsPage(steps = steps, onUpdate = { steps = it })
                        3 -> DetailsPage(
                            prepTime = prepTime, onPrepChange = { prepTime = it },
                            cookTime = cookTime, onCookChange = { cookTime = it },
                            servings = servings, onServChange = { servings = it },
                            notes = notes, onNotesChange = { notes = it },
                            imageUrl = imageUrl, onImageChange = { imageUrl = it },
                            selectedTags = selectedTags,
                            onTagToggle = { tag -> selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag }
                        )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(CSSurfaceLowest).padding(horizontal = 20.dp, vertical = 14.dp),
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
                        if (pagerState.currentPage < 3) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        else save()
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CSOnSage, strokeWidth = 2.dp)
                    else if (pagerState.currentPage < 3) {
                        Text("Next", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                    } else Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
