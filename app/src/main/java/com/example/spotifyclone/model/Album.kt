package com.example.spotifyclone.model

data class Album(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val coverUrl: String = "",
    val releaseDate: String = "",
    val songIds: List<String> = emptyList()
)
