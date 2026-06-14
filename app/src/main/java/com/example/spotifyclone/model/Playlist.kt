package com.example.spotifyclone.model

/**
 * Representa una lista de reproducción personalizada creada por un usuario.
 */
data class Playlist(
    val id: String = "",         // ID de la playlist
    val name: String = "",       // Nombre dado por el usuario
    val ownerId: String = "",    // UID del creador (quien puede editarla)
    val coverUrl: String = "",   // Portada (opcional)
    val songsIds: List<String> = emptyList(), // Lista de canciones guardadas
    val collaborators: List<String> = emptyList() // Otros usuarios que pueden añadir música
)
