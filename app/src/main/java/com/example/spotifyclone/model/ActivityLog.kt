package com.example.spotifyclone.model

import com.google.firebase.Timestamp

/**
 * Registra una acción realizada por el usuario.
 * Se usa para mostrar el historial de "Recientes".
 */
data class ActivityLog(
    val userId: String = "",       // Quién hizo la acción
    val action: String = "",       // Qué hizo (Ej: "Escuchó: Birds of a Feather")
    val timestamp: Timestamp = Timestamp.now() // Cuándo lo hizo (fecha automática de Firebase)
)
