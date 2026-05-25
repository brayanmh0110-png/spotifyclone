package com.example.spotifyclone.model

/**
 * Datos del usuario autenticado en Firebase.
 */
data class User(
    val uid: String = "",         // ID único de Firebase Auth
    val email: String = "",       // Correo electrónico
    val name: String = "",        // Nombre mostrado
    val favorites: List<String> = emptyList() // Lista de IDs de canciones con "Like"
)
