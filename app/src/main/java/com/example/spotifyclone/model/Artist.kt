package com.example.spotifyclone.model

/**
 * Representa a un artista musical.
 */
data class Artist(
    val id: String = "",       // ID único (ej: art_karol_g)
    val name: String = "",     // Nombre artístico
    val imageUrl: String = ""  // Foto del artista
)
