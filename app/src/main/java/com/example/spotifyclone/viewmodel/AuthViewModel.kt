package com.example.spotifyclone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.data.UserPreferencesRepository
import com.example.spotifyclone.model.User
import com.example.spotifyclone.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository()
    private val userPrefs = UserPreferencesRepository(application)

    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val savedUid = userPrefs.userUid.first()
            _isLoggedIn.value = savedUid != null && authRepository.isUserLoggedIn()
        }
    }

    fun onNameChange(name: String) {
        _userState.value = _userState.value.copy(name = name)
    }

    fun onEmailChange(email: String) {
        _userState.value = _userState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        // We don't store password in the User model anymore, 
        // using a separate state for UI if needed, or just temporary
    }

    // Validation and Registration
    fun register(password: String) {
        val user = _userState.value
        if (!authRepository.validateEmail(user.email)) {
            _error.value = "Email inválido"
            return
        }
        if (!authRepository.validatePassword(password)) {
            _error.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {
            val result = authRepository.register(user.name, user.email, password)
            if (result.isSuccess) {
                val uid = result.getOrThrow()
                userPrefs.saveUserSession(uid, user.email)
                _isLoggedIn.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun login(password: String) {
        val user = _userState.value
        viewModelScope.launch {
            val result = authRepository.login(user.email, password)
            if (result.isSuccess) {
                val uid = result.getOrThrow()
                userPrefs.saveUserSession(uid, user.email)
                _isLoggedIn.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userPrefs.clearUserSession()
            _isLoggedIn.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
