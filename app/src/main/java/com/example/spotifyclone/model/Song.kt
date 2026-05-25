package com.example.spotifyclone.model

/**
 * Representa una canción en el sistema.
 */
data class Song(
    val id: String = "",         // ID único de la canción
    val title: String = "",      // Título de la pista
    val artist: String = "",     // Nombre del artista
    val genreId: String = "",    // ID del género al que pertenece
    val audioUrl: String = "",   // Enlace directo al archivo MP3 (iTunes/Drive)
    val coverUrl: String = "",   // Enlace a la imagen de la portada
    val duration: String = ""    // Duración formateada (ej: 0:30)
)
