package com.nomnom.data.viewmodel

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nomnom.data.model.Recipe
import com.nomnom.data.service.RecipeInput
import com.nomnom.data.service.SupabaseService
import com.nomnom.util.NetworkObserver
import com.nomnom.util.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RecipeViewModel(application: android.app.Application) : AndroidViewModel(application) {

    // Injected by HomeScreen so we can trigger auto-logout on 401
    var authViewModel: AuthViewModel? = null

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    val recipeStats = _recipes.map { list ->
        mapOf(
            "total" to list.size,
            "ingredients" to list.sumOf { it.ingredients.size },
            "avg_prep" to (if (list.isNotEmpty()) list.mapNotNull { it.prepTimeMinutes }.let { if (it.isEmpty()) 0 else it.average().toInt() } else 0)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    private val networkObserver = NetworkObserver(application)

    var authToken: String = ""
    var userId: String = ""

    init {
        viewModelScope.launch {
            networkObserver.observe().collectLatest { status ->
                _isOffline.value = (status == NetworkStatus.Unavailable)
                if (status == NetworkStatus.Available && _recipes.value.isEmpty() && authToken.isNotBlank()) {
                    fetchRecipes()
                }
            }
        }
    }
    
    fun fetchRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fetchedRecipes = withContext(Dispatchers.IO) {
                    SupabaseService.fetchRecipes(userId, authToken)
                }
                _recipes.value = fetchedRecipes
            } catch (e: Exception) {
                if (e.isJwtExpired()) authViewModel?.handleSessionExpired()
                else if (e is UnknownHostException || e is SocketTimeoutException) {
                    _isOffline.value = true
                    _errorMessage.value = "No internet connection"
                }
                else _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun fetchRecipe(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val recipe = withContext(Dispatchers.IO) {
                    SupabaseService.fetchRecipe(id, authToken)
                }
                _selectedRecipe.value = recipe
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createRecipe(
        title: String,
        ingredients: List<String>,
        steps: List<String>,
        sourceUrl: String? = null,
        sourceType: String? = null,
        prepTimeMinutes: Int? = null,
        cookTimeMinutes: Int? = null,
        servings: Int? = null,
        notes: String? = null,
        imageUrl: String? = null,
        tags: List<String> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val input = RecipeInput(
                title = title, ingredients = ingredients, steps = steps,
                sourceUrl = sourceUrl, sourceType = sourceType,
                prepTimeMinutes = prepTimeMinutes, cookTimeMinutes = cookTimeMinutes,
                servings = servings, notes = notes, imageUrl = imageUrl,
                tags = tags
            )
            try {
                val newRecipe = withContext(Dispatchers.IO) {
                    SupabaseService.createRecipe(input, userId, authToken)
                }
                _recipes.value = listOf(newRecipe) + _recipes.value
                onSuccess()
            } catch (e: Exception) {
                if (e.isJwtExpired()) authViewModel?.handleSessionExpired()
                else _errorMessage.value = "Failed to create recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateRecipe(
        id: String,
        title: String,
        ingredients: List<String>,
        steps: List<String>,
        sourceUrl: String? = null,
        sourceType: String? = null,
        prepTimeMinutes: Int? = null,
        cookTimeMinutes: Int? = null,
        servings: Int? = null,
        notes: String? = null,
        imageUrl: String? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val input = RecipeInput(
                title = title, ingredients = ingredients, steps = steps,
                sourceUrl = sourceUrl, sourceType = sourceType,
                prepTimeMinutes = prepTimeMinutes, cookTimeMinutes = cookTimeMinutes,
                servings = servings, notes = notes, imageUrl = imageUrl,
                tags = tags
            )
            try {
                val updatedRecipe = withContext(Dispatchers.IO) {
                    SupabaseService.updateRecipe(id, input, authToken)
                }
                val index = _recipes.value.indexOfFirst { it.id == id }
                if (index >= 0) {
                    val updatedList = _recipes.value.toMutableList()
                    updatedList[index] = updatedRecipe
                    _recipes.value = updatedList
                }
                _selectedRecipe.value = updatedRecipe
            } catch (e: Exception) {
                if (e.isJwtExpired()) authViewModel?.handleSessionExpired()
                else _errorMessage.value = "Failed to update recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.deleteRecipe(id, authToken)
                }
                _recipes.value = _recipes.value.filter { it.id != id }
                _selectedRecipe.value = null
            } catch (e: Exception) {
                if (e.isJwtExpired()) authViewModel?.handleSessionExpired()
                else _errorMessage.value = "Failed to delete recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectRecipe(recipe: com.nomnom.data.model.Recipe) {
        _selectedRecipe.value = recipe
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

/** Returns true if the exception is a 401 JWT expired Supabase error */
private fun Exception.isJwtExpired(): Boolean {
    val msg = message?.lowercase() ?: return false
    return msg.contains("401") || msg.contains("jwt expired") || msg.contains("not authenticated")
}
