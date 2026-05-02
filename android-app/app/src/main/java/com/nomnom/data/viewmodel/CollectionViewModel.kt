package com.nomnom.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nomnom.data.model.RecipeCollection
import com.nomnom.data.service.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> = _collections

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Set of recipe IDs currently in the selected collection
    private val _collectionRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val collectionRecipeIds: StateFlow<Set<String>> = _collectionRecipeIds

    var authToken: String = ""
    var userId: String    = ""

    // ── Fetch ──────────────────────────────────────────────────────────────
    fun fetchCollections() {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                _collections.value = withContext(Dispatchers.IO) {
                    SupabaseService.fetchCollections(authToken)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Could not load collections: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Create ─────────────────────────────────────────────────────────────
    fun createCollection(name: String, description: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                val newCollection = withContext(Dispatchers.IO) {
                    SupabaseService.createCollection(
                        name        = name.trim(),
                        description = description?.ifBlank { null },
                        userId      = userId,
                        token       = authToken
                    )
                }
                _collections.value = listOf(newCollection) + _collections.value
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    fun deleteCollection(id: String) {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.deleteCollection(id, authToken)
                }
                _collections.value = _collections.value.filter { it.id != id }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renameCollection(id: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.renameCollection(id, newName, authToken)
                }
                _collections.value = _collections.value.map {
                    if (it.id == id) it.copy(name = newName) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    // ── Collection recipe membership ────────────────────────────────────────

    fun fetchCollectionRecipeIds(collectionId: String) {
        viewModelScope.launch {
            try {
                val ids = withContext(Dispatchers.IO) {
                    SupabaseService.fetchCollectionRecipeIds(collectionId, authToken)
                }
                _collectionRecipeIds.value = ids.toSet()
            } catch (e: Exception) {
                _errorMessage.value = "Could not load collection recipes: ${e.message}"
            }
        }
    }

    fun addRecipeToCollection(collectionId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.addRecipeToCollection(collectionId, recipeId, authToken)
                }
                _collectionRecipeIds.value = _collectionRecipeIds.value + recipeId
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add recipe: ${e.message}"
            }
        }
    }

    fun removeRecipeFromCollection(collectionId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.removeRecipeFromCollection(collectionId, recipeId, authToken)
                }
                _collectionRecipeIds.value = _collectionRecipeIds.value - recipeId
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove recipe: ${e.message}"
            }
        }
    }

    fun clearCollectionRecipeIds() {
        _collectionRecipeIds.value = emptySet()
    }
}
