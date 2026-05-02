package com.nomnom.data.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nomnom.data.service.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val masterKey = MasterKey.Builder(application)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        application,
        "nomnom_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Keys for persistence
    private val KEY_TOKEN         = "auth_token"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    private val KEY_USER_ID       = "user_id"
    private val KEY_EMAIL         = "user_email"
    private val KEY_USERNAME      = "user_username"
    private val KEY_AVATAR        = "user_avatar"
    private val KEY_PREMIUM       = "is_premium"

    // ── State ──────────────────────────────────────────────────────────────
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady: StateFlow<Boolean> = _isAuthReady

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _authToken = MutableStateFlow("")
    val authToken: StateFlow<String> = _authToken

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _avatarId = MutableStateFlow("")
    val avatarId: StateFlow<String> = _avatarId

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    // ── Init — restore session from secure storage ────────────────────────
    init {
        viewModelScope.launch {
            val token    = securePrefs.getString(KEY_TOKEN, "") ?: ""
            val uid      = securePrefs.getString(KEY_USER_ID, "") ?: ""
            val email    = securePrefs.getString(KEY_EMAIL, "") ?: ""
            
            if (token.isNotBlank() && uid.isNotBlank()) {
                _authToken.value  = token
                _userId.value     = uid
                _userEmail.value  = email
                _username.value   = securePrefs.getString(KEY_USERNAME, "") ?: ""
                _avatarId.value   = securePrefs.getString(KEY_AVATAR, "") ?: ""
                _isLoggedIn.value = true
                _isPremium.value  = securePrefs.getBoolean(KEY_PREMIUM, false)
                
                // Proactively refresh token to avoid mid-session expiry
                refreshToken()
                
                // Refresh profile from DB
                fetchProfile()
            }
            _isAuthReady.value = true
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                val (user, token, refreshToken) = withContext(Dispatchers.IO) {
                    SupabaseService.login(email.trim(), password)
                }
                saveSession(token, refreshToken, user.id, user.email)
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Signup ─────────────────────────────────────────────────────────────
    fun signup(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                val (user, token, refreshToken) = withContext(Dispatchers.IO) {
                    SupabaseService.signup(email.trim(), password)
                }
                saveSession(token, refreshToken, user.id, user.email)
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Signup failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** ── Token Refresh ───────────────────────────────────────────────────── */
    fun refreshToken() {
        val currentRefreshToken = securePrefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
        if (currentRefreshToken.isBlank()) {
            handleSessionExpired()
            return
        }

        viewModelScope.launch {
            try {
                val (newToken, newRefreshToken) = withContext(Dispatchers.IO) {
                    SupabaseService.refreshSession(currentRefreshToken)
                }
                saveSession(newToken, newRefreshToken, _userId.value, _userEmail.value)
            } catch (e: Exception) {
                handleSessionExpired()
            }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            securePrefs.edit().clear().apply()
            _authToken.value  = ""
            _userId.value     = ""
            _userEmail.value  = ""
            _username.value   = ""
            _avatarId.value   = ""
            _isLoggedIn.value = false
        }
    }

    // ── Profile Management ────────────────────────────────────────────────
    fun fetchProfile() {
        val uid = _userId.value
        if (uid.isBlank()) return
        
        viewModelScope.launch {
            try {
                val profile = withContext(Dispatchers.IO) {
                    SupabaseService.getProfile(uid)
                }
                profile?.let {
                    _username.value = it.username ?: ""
                    _avatarId.value = it.avatarId ?: ""
                    securePrefs.edit().apply {
                        putString(KEY_USERNAME, it.username)
                        putString(KEY_AVATAR, it.avatarId)
                    }.apply()
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun updateProfile(name: String, avatar: String) {
        val uid = _userId.value
        if (uid.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.updateProfile(uid, name, avatar, _authToken.value)
                }
                _username.value = name
                _avatarId.value = avatar
                securePrefs.edit().apply {
                    putString(KEY_USERNAME, name)
                    putString(KEY_AVATAR, avatar)
                }.apply()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.updateEmail(newEmail, _authToken.value)
                }
                _userEmail.value = newEmail
                securePrefs.edit().putString(KEY_EMAIL, newEmail).apply()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    SupabaseService.updatePassword(newPassword, _authToken.value)
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    fun setPremium(enabled: Boolean) {
        _isPremium.value = enabled
        securePrefs.edit().putBoolean(KEY_PREMIUM, enabled).apply()
    }

    fun handleSessionExpired() {
        viewModelScope.launch {
            securePrefs.edit().clear().apply()
            _authToken.value        = ""
            _userId.value           = ""
            _userEmail.value        = ""
            _isLoggedIn.value       = false
            _isSessionExpired.value = true
        }
    }

    fun clearSessionExpired() { _isSessionExpired.value = false }

    // ── Helpers ────────────────────────────────────────────────────────────
    private fun saveSession(token: String, refreshToken: String, uid: String, email: String) {
        _authToken.value = token
        _userId.value    = uid
        _userEmail.value = email
        
        securePrefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_ID, uid)
            putString(KEY_EMAIL, email)
        }.apply()
    }
}
