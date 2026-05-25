package com.example.spotifyclone.repository

import com.example.spotifyclone.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow

class MusicRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // 1. Arquitectura de Firestore: Colecciones
    private val usersCollection = firestore.collection("users")
    private val songsCollection = firestore.collection("songs")
    private val genresCollection = firestore.collection("genres")
    private val playlistsCollection = firestore.collection("playlists")
    private val activityLogCollection = firestore.collection("activity_log")
    private val artistsCollection = firestore.collection("artists")
    private val albumsCollection = firestore.collection("albums")

    // Get all songs
    fun getSongs(): Flow<List<Song>> = flow {
        val snapshot = songsCollection.get().await()
        emit(snapshot.toObjects(Song::class.java))
    }

    // Get all artists
    fun getArtists(): Flow<List<Artist>> = flow {
        val snapshot = artistsCollection.get().await()
        emit(snapshot.toObjects(Artist::class.java))
    }

    // Get all albums
    fun getAlbums(): Flow<List<Album>> = flow {
        val snapshot = albumsCollection.get().await()
        emit(snapshot.toObjects(Album::class.java))
    }

    // Get songs by album
    suspend fun getSongsByAlbum(songIds: List<String>): List<Song> {
        if (songIds.isEmpty()) return emptyList()
        return songsCollection.whereIn("id", songIds).get().await().toObjects(Song::class.java)
    }

    // Seed data (Classic Music)
    suspend fun seedClassicalMusic() {
        val classicalSongs = listOf(
            Song("1", "Symphony No. 5", "Beethoven", "genre_classical", "url1", "cover1", "30:00"),
            Song("2", "The Four Seasons", "Vivaldi", "genre_classical", "url2", "cover2", "40:00"),
            Song("3", "Clair de Lune", "Debussy", "genre_classical", "url3", "cover3", "05:00"),
        )
        for (song in classicalSongs) {
            songsCollection.document(song.id).set(song).await()
        }
    }

    // 3. Lógica de Personalización: Filtro de Favoritos
    suspend fun getUserFavorites(userId: String): List<Song> {
        val userDoc = usersCollection.document(userId).get().await()
        val favoritesIds = userDoc.toObject(User::class.java)?.favorites ?: emptyList()
        
        if (favoritesIds.isEmpty()) return emptyList()
        
        return songsCollection.whereIn("id", favoritesIds).get().await().toObjects(Song::class.java)
    }

    // Toggle Favorite
    suspend fun toggleFavorite(userId: String, songId: String) {
        val userRef = usersCollection.document(userId)
        val userDoc = userRef.get().await()
        val favorites = userDoc.toObject(User::class.java)?.favorites ?: emptyList()

        if (favorites.contains(songId)) {
            userRef.update("favorites", FieldValue.arrayRemove(songId)).await()
            logActivity(userId, "Removed $songId from favorites")
        } else {
            userRef.update("favorites", FieldValue.arrayUnion(songId)).await()
            logActivity(userId, "Added $songId to favorites")
        }
    }

    // 3. Playlists Colaborativas: Lógica de edición
    suspend fun canEditPlaylist(userId: String, playlistId: String): Boolean {
        val playlistDoc = playlistsCollection.document(playlistId).get().await()
        val playlist = playlistDoc.toObject(Playlist::class.java) ?: return false
        
        return (playlist.ownerId == userId) || playlist.collaborators.contains(userId)
    }

    suspend fun updatePlaylist(userId: String, playlist: Playlist): Result<Unit> {
        return if (canEditPlaylist(userId, playlist.id)) {
            playlistsCollection.document(playlist.id).set(playlist).await()
            logActivity(userId, "Updated playlist ${playlist.id}")
            Result.success(Unit)
        } else {
            Result.failure(Exception("No permission to edit this playlist"))
        }
    }

    // Activity Log holaaaaa
    private suspend fun logActivity(userId: String, action: String) {
        val log = ActivityLog(userId = userId, action = action)
        activityLogCollection.add(log).await()
    }
    
    fun getGenres(): Flow<List<Genre>> = flow {
        val snapshot = genresCollection.get().await()
        emit(snapshot.toObjects(Genre::class.java))
    }
}
