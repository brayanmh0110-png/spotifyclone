package com.example.spotifyclone.model

/**
 * Representa una canción individual dentro del sistema.
 * Es compatible con Firebase Firestore (campos coinciden con los de la nube).
 */
data class Song(
    val id: String = "",         // ID único (Ej: itunes_12345)
    val title: String = "",      // Título de la canción
    val artist: String = "",     // Nombre del artista/banda
    val genreId: String = "",    // Categoría a la que pertenece
    val audioUrl: String = "",   // Link directo al archivo de sonido (.mp3)
    val coverUrl: String = "",   // Link a la imagen de portada
    val duration: String = ""    // Duración formateada (Ej: 03:45)
)
