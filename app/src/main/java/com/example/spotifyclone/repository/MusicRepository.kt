package com.example.spotifyclone.repository

import com.example.spotifyclone.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.net.URL
import java.net.URLEncoder

/**
 * MusicRepository: El puente entre la app y las fuentes de datos externas.
 * Maneja la lógica de Firebase Firestore y la API de búsqueda de iTunes.
 */
class MusicRepository {
    // Instancia de la base de datos de Firebase
    private val firestore = FirebaseFirestore.getInstance()

    // Referencias a las colecciones (tablas) de nuestra base de datos en la nube
    private val usersCollection = firestore.collection("users")
    private val songsCollection = firestore.collection("songs")
    private val genresCollection = firestore.collection("genres")
    private val playlistsCollection = firestore.collection("playlists")
    private val activityLogCollection = firestore.collection("activity_log")
    private val artistsCollection = firestore.collection("artists")
    private val albumsCollection = firestore.collection("albums")

    /**
     * Trae todas las canciones de la colección global.
     */
    fun getSongs(): Flow<List<Song>> = flow {
        val snapshot = songsCollection.get().await()
        emit(snapshot.toObjects(Song::class.java))
    }

    /**
     * Trae todos los artistas registrados.
     */
    fun getArtists(): Flow<List<Artist>> = flow {
        val snapshot = artistsCollection.get().await()
        emit(snapshot.toObjects(Artist::class.java))
    }

    /**
     * Trae todos los álbumes.
     */
    fun getAlbums(): Flow<List<Album>> = flow {
        val snapshot = albumsCollection.get().await()
        emit(snapshot.toObjects(Album::class.java))
    }

    /**
     * Trae la información de canciones específicas basadas en sus IDs.
     */
    suspend fun getSongsByAlbum(songIds: List<String>): List<Song> {
        if (songIds.isEmpty()) return emptyList()
        return songsCollection.whereIn("id", songIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Obtiene la lista completa de canciones que un usuario marcó como favoritas.
     */
    suspend fun getUserFavorites(userId: String): List<Song> {
        val userDoc = usersCollection.document(userId).get().await()
        val favoritesIds = userDoc.toObject(User::class.java)?.favorites ?: emptyList()

        if (favoritesIds.isEmpty()) return emptyList()

        // Buscamos los detalles de cada canción en la colección global
        return songsCollection.whereIn("id", favoritesIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Agrega o quita una canción de los favoritos del usuario.
     * También asegura que la canción de iTunes quede guardada en nuestra base de datos.
     */
    suspend fun toggleFavorite(userId: String, song: Song) {
        // Guardamos la canción en la base de datos por si no existía (ej: de una búsqueda)
        songsCollection.document(song.id).set(song).await()

        val userRef = usersCollection.document(userId)
        val userDoc = userRef.get().await()
        val favorites = userDoc.toObject(User::class.java)?.favorites ?: emptyList()

        if (favorites.contains(song.id)) {
            // Eliminar de la lista de favoritos del usuario
            userRef.update("favorites", FieldValue.arrayRemove(song.id)).await()
            logActivity(userId, "Removió de favoritos: ${song.title}")
        } else {
            // Agregar a la lista de favoritos
            userRef.update("favorites", FieldValue.arrayUnion(song.id)).await()
            logActivity(userId, "Marcó como favorito: ${song.title}")
        }
    }

    /**
     * Verifica si el usuario es dueño o colaborador de una playlist.
     */
    suspend fun canEditPlaylist(userId: String, playlistId: String): Boolean {
        val playlistDoc = playlistsCollection.document(playlistId).get().await()
        val playlist = playlistDoc.toObject(Playlist::class.java) ?: return false
        return (playlist.ownerId == userId) || playlist.collaborators.contains(userId)
    }

    /**
     * Actualiza la información de una playlist (como el nombre).
     */
    suspend fun updatePlaylist(userId: String, playlist: Playlist): Result<Unit> {
        return if (canEditPlaylist(userId, playlist.id)) {
            playlistsCollection.document(playlist.id).set(playlist).await()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Sin permisos de edición"))
        }
    }

    /**
     * Registra una acción del usuario en la base de datos (Historial).
     */
    suspend fun logActivity(userId: String, action: String) {
        val log = ActivityLog(userId = userId, action = action)
        activityLogCollection.add(log).await()
    }

    /**
     * Obtiene el historial de actividad en tiempo real.
     */
    fun getActivityLogs(userId: String): Flow<List<ActivityLog>> = callbackFlow {
        val listener = activityLogCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ActivityLog::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Función de llenado inicial (Seeding): Descarga 50 canciones reales de iTunes
     * y configura la base de datos de Firebase si está vacía.
     */
    suspend fun seedFullProjectData() = withContext(Dispatchers.IO) {
        val existingSongs = songsCollection.limit(1).get().await()
        if (!existingSongs.isEmpty) return@withContext

        // Definimos géneros base
        val genres = listOf(
            Genre("gen_pop", "Pop Global", "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?w=500"),
            Genre("gen_rock_pe", "Rock Peruano", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500"),
            Genre("gen_reg", "Reggaetón Hits", "https://images.unsplash.com/photo-1621112904887-419379ce6824?w=500"),
            Genre("gen_salsa", "Salsa Brava", "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=500"),
            Genre("gen_cumbia", "Cumbia Peruana", "https://images.unsplash.com/photo-1594122230689-45899d9e6f69?w=500")
        )
        for (g in genres) genresCollection.document(g.id).set(g).await()

        val queries = listOf(
            "Billie Eilish", "Taylor Swift", "Mar de Copas", "Libido", "Bad Bunny", "Karol G", "Grupo 5"
        )

        val albumSongsMap = mutableMapOf<String, MutableList<String>>()

        // Buscamos canciones masivamente
        queries.mapIndexed { index, query ->
            async {
                try {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://itunes.apple.com/search?term=$encoded&limit=5&entity=song"
                    val json = URL(url).readText()
                    
                    val parts = json.split("{\"wrapperType\":\"track\"").drop(1)
                    for (part in parts) {
                        val title = part.substringAfter("trackName\":\"").substringBefore("\"")
                        val artist = part.substringAfter("artistName\":\"").substringBefore("\"")
                        val cover = part.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                        val audio = part.substringAfter("previewUrl\":\"").substringBefore("\"")
                        val trackId = part.substringAfter("trackId\":").substringBefore(",")

                        val songId = "song_$trackId"
                        val song = Song(songId, title, artist, "gen_pop", audio, cover, "00:30")
                        songsCollection.document(songId).set(song).await()

                        val artId = "art_${artist.lowercase().replace(" ", "_")}"
                        artistsCollection.document(artId).set(Artist(artId, artist, cover)).await()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }.awaitAll()
    }

    /**
     * Trae todos los géneros musicales.
     */
    fun getGenres(): Flow<List<Genre>> = flow {
        val snapshot = genresCollection.get().await()
        emit(snapshot.toObjects(Genre::class.java))
    }

    /**
     * Escucha las playlists del usuario en tiempo real.
     */
    fun getPlaylists(userId: String): Flow<List<Playlist>> = callbackFlow {
        val listener = playlistsCollection.whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Playlist::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Crea una nueva playlist.
     */
    suspend fun crearPlaylist(userId: String, nombre: String): String {
        val id = "pl_${System.currentTimeMillis()}"
        val playlist = Playlist(id = id, name = nombre, ownerId = userId)
        playlistsCollection.document(id).set(playlist).await()
        logActivity(userId, "Creó la playlist: $nombre")
        return id
    }

    /**
     * Agrega una canción a una playlist.
     */
    suspend fun addSongToPlaylist(userId: String, playlistId: String, songId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            playlistsCollection.document(playlistId)
                .update("songsIds", FieldValue.arrayUnion(songId)).await()
        }
    }

    /**
     * Remueve una canción de una playlist.
     */
    suspend fun removeSongFromPlaylist(userId: String, playlistId: String, songId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            playlistsCollection.document(playlistId)
                .update("songsIds", FieldValue.arrayRemove(songId)).await()
        }
    }

    /**
     * Elimina una playlist.
     */
    suspend fun eliminarPlaylist(userId: String, playlistId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            playlistsCollection.document(playlistId).delete().await()
        }
    }

    /**
     * BUSCADOR EXTERNO: Consulta la API de iTunes para encontrar canciones de cualquier artista.
     */
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encoded&limit=15&entity=song"
            val json = URL(url).readText()
            
            val songs = mutableListOf<Song>()
            val parts = json.split("{\"wrapperType\":\"track\"").drop(1)

            for (part in parts) {
                val title = part.substringAfter("trackName\":\"").substringBefore("\"")
                val artist = part.substringAfter("artistName\":\"").substringBefore("\"")
                val cover = part.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                val audio = part.substringAfter("previewUrl\":\"").substringBefore("\"")
                val id = part.substringAfter("trackId\":").substringBefore(",")

                songs.add(Song("itunes_$id", title, artist, "search", audio, cover, "00:30"))
            }
            songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
