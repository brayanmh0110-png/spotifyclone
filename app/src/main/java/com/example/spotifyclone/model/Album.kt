package com.example.spotifyclone.model

/**
 * Representa un álbum musical.
 */
data class Album(
    val id: String = "",           // ID único del álbum
    val title: String = "",        // Nombre del álbum
    val artist: String = "",       // Artista principal o "Varios"
    val coverUrl: String = "",     // Imagen de portada del álbum
    val releaseDate: String = "",  // Año de lanzamiento
    val songIds: List<String> = emptyList() // Lista de IDs de canciones que lo componen
)
