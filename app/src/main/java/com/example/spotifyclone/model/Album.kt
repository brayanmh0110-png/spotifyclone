package com.example.spotifyclone.model

/**
 * Representa una colección de canciones (Álbum o Mix).
 */
data class Album(
    val id: String = "",         // ID único del álbum
    val title: String = "",      // Nombre del disco
    val artist: String = "",     // Autor principal
    val coverUrl: String = "",   // Imagen del álbum
    val releaseDate: String = "",// Año de lanzamiento
    val songIds: List<String> = emptyList() // IDs de las canciones que lo componen
)
