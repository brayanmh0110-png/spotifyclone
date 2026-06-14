package com.example.spotifyclone.model

/**
 * Representa una categoría musical (Ej: Rock, Pop, Reggaeton).
 */
data class Genre(
    val id: String = "",       // ID del género (Ej: gen_pop)
    val name: String = "",     // Nombre visible (Ej: Pop Global)
    val imageUrl: String = ""  // Imagen decorativa para la sección de búsqueda
)
