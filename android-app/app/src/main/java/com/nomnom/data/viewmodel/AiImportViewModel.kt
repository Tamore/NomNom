package com.nomnom.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nomnom.data.model.AiExtractedRecipe
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

class AiImportViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _extractedRecipe = MutableStateFlow<AiExtractedRecipe?>(null)
    val extractedRecipe: StateFlow<AiExtractedRecipe?> = _extractedRecipe

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    private val networkObserver = NetworkObserver(application)

    var authToken: String = ""

    init {
        viewModelScope.launch {
            networkObserver.observe().collectLatest { status ->
                _isOffline.value = (status == NetworkStatus.Unavailable)
            }
        }
    }

    fun extractRecipe(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _extractedRecipe.value = null
            try {
                val recipe = withContext(Dispatchers.IO) {
                    SupabaseService.extractRecipe(input.trim(), authToken)
                }
                _extractedRecipe.value = recipe
            } catch (e: Exception) {
                if (e is UnknownHostException || e is SocketTimeoutException) {
                    _errorMessage.value = "No internet connection. Please check your network."
                } else {
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearRecipe() { _extractedRecipe.value = null }
    fun clearError()  { _errorMessage.value = null }
}
