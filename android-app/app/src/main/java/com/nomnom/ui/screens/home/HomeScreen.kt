package com.nomnom.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.model.AiExtractedRecipe
import com.nomnom.data.viewmodel.*
import com.nomnom.ui.screens.ai.AiImportScreen
import com.nomnom.ui.screens.collections.CollectionDetailScreen
import com.nomnom.ui.screens.collections.CollectionsScreen
import com.nomnom.ui.screens.recipe.*
import com.nomnom.ui.screens.shopping.ShoppingListScreen
import com.nomnom.ui.screens.suggest.SuggestScreen

// ── Tab definitions ────────────────────────────────────────────────────────
sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Recipes     : BottomTab("recipe_list",  "Recipes",     Icons.Default.RamenDining)
    object Collections : BottomTab("collections",  "Collections", Icons.Default.CollectionsBookmark)
    object Shopping    : BottomTab("shopping",     "Shop",        Icons.Default.ShoppingCart)
    object Suggest     : BottomTab("suggest",      "Suggest",     Icons.Default.Shuffle)
    object Profile     : BottomTab("profile",      "Profile",     Icons.Default.Person)
}

private val TABS = listOf(BottomTab.Recipes, BottomTab.Collections, BottomTab.Shopping, BottomTab.Suggest, BottomTab.Profile)

object HomeRoutes {
    const val RECIPE_LIST        = "recipe_list"
    const val RECIPE_DETAIL       = "recipe_detail/{recipeId}"
    const val ADD_RECIPE          = "add_recipe"
    const val EDIT_RECIPE         = "edit_recipe/{recipeId}"
    const val COLLECTIONS         = "collections"
    const val COLLECTION_DETAIL   = "collection_detail/{collectionId}"
    const val SUGGEST             = "suggest"
    const val AI_IMPORT           = "ai_import"
    const val COOK_MODE           = "cook_mode/{recipeId}"
    const val SHOPPING            = "shopping"
    const val PROFILE             = "profile"
}

@Composable
fun HomeScreen(authViewModel: AuthViewModel) {
    val authToken by authViewModel.authToken.collectAsState()
    val userId    by authViewModel.userId.collectAsState()

    val recipeViewModel: RecipeViewModel         = viewModel()
    val collectionViewModel: CollectionViewModel = viewModel()
    val aiImportViewModel: AiImportViewModel     = viewModel()
    val shoppingViewModel: ShoppingViewModel     = viewModel()

    // Wire auth into recipe VM so 401s trigger auto-logout
    LaunchedEffect(Unit) { recipeViewModel.authViewModel = authViewModel }

    var aiPrefill by remember { mutableStateOf<AiExtractedRecipe?>(null) }
    // Track the selected collection for detail navigation
    var selectedCollection by remember { mutableStateOf<com.nomnom.data.model.RecipeCollection?>(null) }

    val navController        = rememberNavController()
    val navBackStackEntry    by navController.currentBackStackEntryAsState()
    val currentDest          = navBackStackEntry?.destination

    val tabRoutes = TABS.map { it.route }.toSet()
    val showBottomBar = currentDest?.route in tabRoutes
    val recipes by recipeViewModel.recipes.collectAsState()
    val isOffline by recipeViewModel.isOffline.collectAsState()

    Scaffold(
        containerColor = CSLinen, // Main background
        bottomBar = {
            AnimatedVisibility(
                visible     = showBottomBar,
                enter       = slideInVertically { it } + fadeIn(),
                exit        = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar(
                    containerColor = CSSurfaceLowest,
                    tonalElevation = 0.dp
                ) {
                    TABS.forEach { tab ->
                        val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                        val iconScale by animateFloatAsState(
                            targetValue   = if (selected) 1.15f else 1f,
                            animationSpec = spring(Spring.DampingRatioMediumBouncy),
                            label         = "navIcon${tab.route}"
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    modifier           = Modifier.scale(iconScale)
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = CSSage,
                                selectedTextColor   = CSSage,
                                unselectedIconColor = CSOnSurfaceVariant.copy(alpha = 0.5f),
                                unselectedTextColor = CSOnSurfaceVariant.copy(alpha = 0.5f),
                                indicatorColor      = CSSage.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // ── Offline Banner ──────────────────────────────────────────
            AnimatedVisibility(
                visible = isOffline,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color    = CSErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.Center
                    ) {
                        Icon(Icons.Default.WifiOff, null, tint = CSOnErrorContainer, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "You are currently offline",
                            color      = CSOnErrorContainer,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── App Navigation ──────────────────────────────────────────
            NavHost(
                navController    = navController,
                startDestination = HomeRoutes.RECIPE_LIST,
                modifier         = Modifier.weight(1f)
            ) {
                // ── Recipes ──────────────────────────────────────────────────
                composable(HomeRoutes.RECIPE_LIST) {
                    RecipeListScreen(
                        authToken  = authToken,
                        userId     = userId,
                        viewModel  = recipeViewModel,
                        authViewModel = authViewModel,
                        onLogout   = { authViewModel.logout() },
                        onRecipeClick = { recipe ->
                            recipeViewModel.selectRecipe(recipe)
                            navController.navigate("recipe_detail/${recipe.id}")
                        },
                        onAddClick    = { navController.navigate(HomeRoutes.ADD_RECIPE) },
                        onImportClick = { navController.navigate(HomeRoutes.AI_IMPORT) }
                    )
                }

                composable(HomeRoutes.RECIPE_DETAIL) { back ->
                    val recipeId = back.arguments?.getString("recipeId") ?: return@composable
                    RecipeDetailScreen(
                        recipeId  = recipeId,
                        authToken = authToken,
                        userId    = userId ?: "",
                        viewModel = recipeViewModel,
                        collectionViewModel = collectionViewModel,
                        onBack    = { navController.popBackStack() },
                        onEdit    = { navController.navigate("edit_recipe/$recipeId") },
                        onDeleted = { navController.popBackStack() },
                        onCookMode = { navController.navigate("cook_mode/$it") }
                    )
                }

                composable(HomeRoutes.ADD_RECIPE) {
                    AddRecipeScreen(
                        authToken = authToken,
                        userId    = userId,
                        viewModel = recipeViewModel,
                        prefill   = aiPrefill,
                        onBack    = { aiPrefill = null; navController.popBackStack() },
                        onSaved   = { aiPrefill = null; navController.popBackStack() }
                    )
                }

                composable(HomeRoutes.EDIT_RECIPE) { back ->
                    val recipeId = back.arguments?.getString("recipeId") ?: return@composable
                    EditRecipeScreen(
                        recipeId  = recipeId,
                        authToken = authToken,
                        viewModel = recipeViewModel,
                        onBack    = { navController.popBackStack() },
                        onSaved   = { navController.popBackStack() }
                    )
                }

                // ── Cook Mode ─────────────────────────────────────────────────
                composable(HomeRoutes.COOK_MODE) { back ->
                    val recipeId = back.arguments?.getString("recipeId") ?: return@composable
                    CookModeScreen(
                        recipeId  = recipeId,
                        viewModel = recipeViewModel,
                        onExit    = { navController.popBackStack() }
                    )
                }

                // ── AI Import ─────────────────────────────────────────────────
                composable(HomeRoutes.AI_IMPORT) {
                    AiImportScreen(
                        viewModel   = aiImportViewModel,
                        authToken   = authToken,
                        onBack      = { aiImportViewModel.clearRecipe(); navController.popBackStack() },
                        onUseRecipe = { extracted ->
                            aiPrefill = extracted
                            aiImportViewModel.clearRecipe()
                            navController.navigate(HomeRoutes.ADD_RECIPE)
                        },
                        onEditRecipe = { extracted ->
                            aiPrefill = extracted
                            aiImportViewModel.clearRecipe()
                            navController.navigate(HomeRoutes.ADD_RECIPE)
                        }
                    )
                }

                // ── Collections ───────────────────────────────────────────────
                composable(HomeRoutes.COLLECTIONS) {
                    CollectionsScreen(
                        authToken           = authToken,
                        userId              = userId,
                        collectionViewModel = collectionViewModel,
                        onCollectionClick   = { collection ->
                            selectedCollection = collection
                            navController.navigate("collection_detail/${collection.id}")
                        }
                    )
                }

                composable(HomeRoutes.COLLECTION_DETAIL) {
                    val collection = selectedCollection
                    if (collection != null) {
                        CollectionDetailScreen(
                            collection      = collection,
                            allRecipes      = recipes,
                            viewModel       = collectionViewModel,
                            onBack          = { navController.popBackStack() },
                            onAddRecipe     = { navController.navigate(HomeRoutes.ADD_RECIPE) },
                            onRecipeClick   = { recipe ->
                                recipeViewModel.selectRecipe(recipe)
                                navController.navigate("recipe_detail/${recipe.id}")
                            }
                        )
                    } else {
                        navController.popBackStack()
                    }
                }

                // ── Shopping ──────────────────────────────────────────────────
                composable(HomeRoutes.SHOPPING) {
                    ShoppingListScreen(
                        viewModel = shoppingViewModel,
                        recipes   = recipes
                    )
                }

                // ── Suggest ───────────────────────────────────────────────────
                composable(HomeRoutes.SUGGEST) {
                    SuggestScreen(
                        authToken       = authToken,
                        recipeViewModel = recipeViewModel
                    )
                }

                // ── Profile ───────────────────────────────────────────────────
                composable(HomeRoutes.PROFILE) {
                    com.nomnom.ui.screens.profile.ProfileScreen(
                        authViewModel   = authViewModel,
                        recipeViewModel = recipeViewModel,
                        onLogout        = { /* Handled by AuthViewModel */ }
                    )
                }
            }
        }
    }
}
