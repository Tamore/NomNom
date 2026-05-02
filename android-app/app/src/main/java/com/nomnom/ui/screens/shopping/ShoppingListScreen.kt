package com.nomnom.ui.screens.shopping

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.Recipe
import com.nomnom.data.viewmodel.ShoppingItem
import com.nomnom.data.viewmodel.ShoppingViewModel
import com.nomnom.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel,
    recipes: List<Recipe>
) {
    val items        by viewModel.items.collectAsState()
    var showPicker   by remember { mutableStateOf(false) }
    val checkedCount = items.count { it.checked }

    val grouped = items.groupBy { it.category }

    Scaffold(
        containerColor = NomNomNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shopping List", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
                        AnimatedContent(
                            targetState = "${items.size} items · $checkedCount checked",
                            label       = "shoppingCount"
                        ) { txt ->
                            Text(txt, fontSize = 12.sp, color = NomNomTextSub)
                        }
                    }
                },
                actions = {
                    if (checkedCount > 0) {
                        IconButton(onClick = { viewModel.clearChecked() }) {
                            Icon(Icons.Default.CheckCircle, "Clear checked", tint = NomNomSuccess)
                        }
                    }
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(Icons.Default.DeleteSweep, "Clear all", tint = NomNomTextSub)
                        }
                    }
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Default.PlaylistAdd, "Add from recipe", tint = NomNomRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NomNomSurface)
            )
        }
    ) { padding ->

        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(32.dp)
                ) {
                    Text("🛒", fontSize = 72.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Your list is empty", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add ingredients from recipes or type one manually below",
                        color     = NomNomTextSub,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    QuickAddRow(onAdd = { viewModel.addItem(it) })
                    
                    Spacer(Modifier.height(16.dp))
                    Text("— or —", color = NomNomTextSub, fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    NomNomButton(onClick = { showPicker = true }, modifier = Modifier.width(200.dp)) {
                        Icon(Icons.Default.PlaylistAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add from Recipe", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(padding),
                contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "quick_add") {
                    QuickAddRow(onAdd = { viewModel.addItem(it) })
                    Spacer(Modifier.height(12.dp))
                }

                grouped.forEach { (category, categoryItems) ->
                    item(key = "cat_$category") {
                        Text(
                            category,
                            color      = NomNomTextSub,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item    = item,
                            onToggle = { viewModel.toggleItem(item.id) }
                        )
                    }
                    item(key = "div_$category") { Spacer(Modifier.height(8.dp)) }
                }

                item {
                    // Progress bar
                    Spacer(Modifier.height(8.dp))
                    Column {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress", color = NomNomTextSub, fontSize = 12.sp)
                            Text(
                                "$checkedCount / ${items.size}",
                                color      = NomNomWhite,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        val progress by animateFloatAsState(
                            if (items.isEmpty()) 0f else checkedCount.toFloat() / items.size,
                            tween(600),
                            label = "shoppingProgress"
                        )
                        LinearProgressIndicator(
                            progress        = { progress },
                            modifier        = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color           = NomNomSuccess,
                            trackColor      = NomNomSurface2
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Recipe picker bottom sheet ──────────────────────────────────────────
    if (showPicker && recipes.isNotEmpty()) {
        RecipePickerSheet(
            recipes  = recipes,
            onSelect = { recipe ->
                viewModel.addIngredients(recipe.ingredients, recipe.title)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, onToggle: () -> Unit) {
    val textAlpha by animateFloatAsState(if (item.checked) 0.38f else 1f, label = "itemAlpha")
    val checkColor by animateColorAsState(
        if (item.checked) NomNomSuccess else NomNomDivider, tween(200), label = "itemCheck"
    )

    NomNomCard(
        onClick         = onToggle,
        containerColor  = NomNomSurface
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox circle
            Box(
                modifier        = Modifier
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (item.checked) NomNomSuccess else Color.Transparent)
                    .border(1.5.dp, checkColor, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.checked) Text("✓", color = NomNomWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text           = item.name,
                color          = NomNomWhite.copy(alpha = textAlpha),
                fontSize       = 15.sp,
                modifier       = Modifier.weight(1f),
                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipePickerSheet(
    recipes: List<Recipe>,
    onSelect: (Recipe) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = NomNomSurface2,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = NomNomDivider) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Add ingredients from…",
                style    = MaterialTheme.typography.titleMedium,
                color    = NomNomWhite,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            recipes.forEach { recipe ->
                NomNomCard(
                    onClick  = { onSelect(recipe) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RecipeThumbnail(recipe.imageUrl, size = 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(recipe.title, color = NomNomWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("${recipe.ingredients.size} ingredients", color = NomNomTextSub, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.Add, null, tint = NomNomRed)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun QuickAddRow(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NomNomSurface)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Add item manually...", color = NomNomTextSub, fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NomNomRed,
                focusedTextColor = NomNomWhite,
                unfocusedTextColor = NomNomWhite
            ),
            singleLine = true
        )
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = "Add",
                tint = if (text.isNotBlank()) NomNomRed else NomNomDivider
            )
        }
    }
}
