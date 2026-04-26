package com.example.spotifyclone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spotifyclone.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _registeredUsers = mutableListOf<User>()

    fun onEmailChange(email: String) {
        _userState.value = _userState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _userState.value = _userState.value.copy(password = password)
    }

    fun register(): Boolean {
        val currentUser = _userState.value
        if (currentUser.email.isNotBlank() && currentUser.password.isNotBlank()) {
            _registeredUsers.add(currentUser)
            return true
        }
        return false
    }

    fun login(): Boolean {
        val currentUser = _userState.value
        return _registeredUsers.any { it.email == currentUser.email && it.password == currentUser.password }
    }
}
