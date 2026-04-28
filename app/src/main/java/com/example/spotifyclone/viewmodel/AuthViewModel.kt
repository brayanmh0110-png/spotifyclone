package com.example.spotifyclone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spotifyclone.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel: El "Cerebro" de la lógica de usuario.
 * Se encarga de mantener el estado de los datos (emails, passwords) de forma independiente a la UI.
 * Esto permite que si la pantalla gira o cambia, los datos no se pierdan.
 */
class AuthViewModel : ViewModel() {
    
    // MutableStateFlow: Un contenedor de datos que "fluye". 
    // Cuando el valor cambia, todas las pantallas conectadas se actualizan automáticamente.
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    // Lista temporal en memoria para simular una base de datos de usuarios registrados.
    private val _registeredUsers = mutableListOf<User>()

    // Función para actualizar el email en el estado global.
    fun onEmailChange(email: String) {
        _userState.value = _userState.value.copy(email = email)
    }

    // Función para actualizar la contraseña en el estado global.
    fun onPasswordChange(password: String) {
        _userState.value = _userState.value.copy(password = password)
    }

    /**
     * Lógica de Registro:
     * Verifica que los campos no estén vacíos y guarda al usuario en la lista temporal.
     */
    fun register(): Boolean {
        val currentUser = _userState.value
        if (currentUser.email.isNotBlank() && currentUser.password.isNotBlank()) {
            _registeredUsers.add(currentUser)
            return true
        }
        return false
    }

    /**
     * Lógica de Login:
     * Compara las credenciales ingresadas con los usuarios guardados en la lista.
     */
    fun login(): Boolean {
        val currentUser = _userState.value
        return _registeredUsers.any { it.email == currentUser.email && it.password == currentUser.password }
    }
}
