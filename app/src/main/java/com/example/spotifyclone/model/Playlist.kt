package com.example.spotifyclone.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val collaborators: List<String> = emptyList(),
    val songsIds: List<String> = emptyList()
)
