package com.example.spotifyclone.model

import com.google.firebase.Timestamp

data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val action: String = "",
    val timestamp: Timestamp = Timestamp.now(),
)
