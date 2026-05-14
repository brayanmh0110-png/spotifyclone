package com.example.spotifyclone.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val favorites: List<String> = emptyList()
)
