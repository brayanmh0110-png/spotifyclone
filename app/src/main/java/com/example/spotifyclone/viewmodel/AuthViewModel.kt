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

/**
 * AuthViewModel: Gestiona todo lo relacionado con el usuario.
 * Controla el registro, inicio de sesión, perfiles y persistencia de la sesión local.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository()
    private val userPrefs = UserPreferencesRepository(application)

    // --- ESTADOS REACTIVOS ---
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Al encender la app, revisamos si ya había un usuario logueado en el teléfono
        checkSession()
    }

    /**
     * Revisa la sesión guardada localmente (DataStore).
     */
    private fun checkSession() {
        viewModelScope.launch {
            val uid = userPrefs.userUid.first()
            if (uid != null) {
                _isLoggedIn.value = true
                fetchUserProfile(uid) // Si hay sesión, traemos los datos de Firebase
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * Trae los datos actualizados del usuario (Nombre, Foto, Favoritos) desde Firebase.
     */
    fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            val user = authRepository.getUserProfile(uid)
            if (user != null) {
                _userState.value = user
            }
        }
    }

    // Funciones para manejar cambios en los campos de texto
    fun onNameChange(newName: String) { _userState.value = _userState.value.copy(name = newName) }
    fun onEmailChange(newEmail: String) { _userState.value = _userState.value.copy(email = newEmail) }

    /**
     * Registra un nuevo usuario en Firebase Auth y Firestore.
     */
    fun register(password: String) {
        viewModelScope.launch {
            _error.value = null
            authRepository.register(_userState.value.name, _userState.value.email, password)
                .onSuccess { uid ->
                    fetchUserProfile(uid)
                    userPrefs.saveUserSession(uid, _userState.value.email)
                    _isLoggedIn.value = true
                }
                .onFailure { e ->
                    _error.value = mapExceptionToMessage(e)
                }
        }
    }

    /**
     * Inicia sesión con correo y contraseña.
     */
    fun login(password: String) {
        viewModelScope.launch {
            _error.value = null
            authRepository.login(_userState.value.email, password)
                .onSuccess { uid ->
                    fetchUserProfile(uid)
                    userPrefs.saveUserSession(uid, _userState.value.email)
                    _isLoggedIn.value = true
                }
                .onFailure { e ->
                    _error.value = mapExceptionToMessage(e)
                }
        }
    }

    /**
     * Cierra la sesión y limpia los datos del teléfono.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userPrefs.clearUserSession()
            _isLoggedIn.value = false
            _userState.value = User()
        }
    }

    /**
     * Elimina permanentemente la cuenta del usuario.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount(_userState.value.uid).onSuccess {
                logout()
            }.onFailure { e ->
                _error.value = "No se pudo eliminar: ${e.message}"
            }
        }
    }

    /**
     * Actualiza la foto de perfil en Firebase.
     */
    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            authRepository.updateProfilePicture(_userState.value.uid, uri).onSuccess {
                _userState.value = _userState.value.copy(photoUrl = uri)
            }
        }
    }

    /**
     * Actualiza el nombre mostrado en Firebase.
     */
    fun updateName(newName: String) {
        viewModelScope.launch {
            authRepository.updateUserName(_userState.value.uid, newName).onSuccess {
                _userState.value = _userState.value.copy(name = newName)
            }
        }
    }

    private fun mapExceptionToMessage(e: Throwable?): String {
        return e?.message ?: "Ocurrió un error inesperado"
    }

    fun clearError() {
        _error.value = null
    }
}
