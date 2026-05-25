package com.example.spotifyclone.model

/**
 * Representa un género musical (Pop, Rock, etc.).
 */
data class Genre(
    val id: String = "",       // ID del género (ej: gen_pop)
    val name: String = "",     // Nombre legible del género
    val imageUrl: String = ""  // Imagen representativa para las tarjetas
)
