package com.example.spotifyclone.repository

import com.example.spotifyclone.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.net.URL
import java.net.URLEncoder

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

    suspend fun seedFullProjectData() = withContext(Dispatchers.IO) {
        // 1. Verificamos si ya hay canciones para no repetir el proceso pesado
        val existingSongs = songsCollection.limit(1).get().await()
        if (!existingSongs.isEmpty) return@withContext

        // 2. LIMPIEZA INICIAL (Por si acaso quedó algo de rastro)
        val collections = listOf(songsCollection, albumsCollection, artistsCollection, genresCollection)
        for (collection in collections) {
            val snapshot = collection.get().await()
            for (doc in snapshot.documents) doc.reference.delete().await()
        }

        // 3. GÉNEROS
        val genres = listOf(
            Genre("gen_pop", "Pop Global", "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?w=500"),
            Genre("gen_rock_pe", "Rock Peruano", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500"),
            Genre("gen_reg", "Reggaetón Hits", "https://images.unsplash.com/photo-1621112904887-419379ce6824?w=500"),
            Genre("gen_salsa", "Salsa Brava", "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=500"),
            Genre("gen_cumbia", "Cumbia Peruana", "https://images.unsplash.com/photo-1594122230689-45899d9e6f69?w=500")
        )
        for (g in genres) genresCollection.document(g.id).set(g).await()

        // 4. BÚSQUEDA MASIVA (50 CANCIONES)
        val queries = listOf(
            // Pop
            "Billie Eilish Birds of a Feather", "Sabrina Carpenter Espresso", "Taylor Swift Cruel Summer", 
            "Dua Lipa Houdini", "Harry Styles As It Was", "Olivia Rodrigo Vampire",
            "The Weeknd Starboy", "Miley Cyrus Flowers", "Benson Boone Beautiful Things", "Post Malone Circles",
            // Rock PE
            "Fragil Avenida Larco", "Mar de Copas Mujer Noche", "Libido En esta habitacion",
            "Amen Dicen", "Miki Gonzalez Carreteras", "Los Mojarras Triciclo Peru",
            "Rio Lo peor de todo", "Pedro Suarez Vertiz Cuando la cama", "Libido Solo", "Mar de Copas Suna",
            // Reggaeton
            "Karol G Si Antes Te Hubiera Conocido", "Bad Bunny Perro Negro", "Feid Luna",
            "Myke Towers Lala", "Bad Bunny Monaco", "Karol G Provenza",
            "Quevedo Columbia", "Rauw Alejandro Diluvio", "Feid Classy 101", "Karol G QLONA",
            // Salsa
            "Willie Colon Idilio", "Marc Anthony Vivir Mi Vida", "Daniela Darcourt Probabilidad de Amor",
            "Oscar D Leon Lloraras", "Joe Arroyo La Rebelion", "Gilberto Santa Rosa Conteo Regresivo",
            "Tito Nieves Fabricando Fantasias", "Frankie Ruiz La Cura", "Grupo Niche Gotas de Lluvia", "Victor Manuelle He Tratado",
            // Cumbia
            "Grupo 5 Motor y Motivo", "Grupo Nectar El Arbolito", "Agua Marina Tu Amor Fue Una Mentira",
            "Corazon Serrano Muriendo de Amor", "Armonia 10 Cervecita", "Hermanos Yaipen Me Emborracho",
            "Los Mirlos Cariñito", "Chacalon Muchacho Provinciano", "Grupo 5 Que levante la mano", "Agua Marina Pasitos"
        )

        val albumSongsMap = mutableMapOf<String, MutableList<String>>()

        // Ejecutamos las búsquedas en paralelo para que sea súper rápido (no 110 minutos)
        queries.mapIndexed { index, query ->
            async {
                try {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://itunes.apple.com/search?term=$encoded&limit=1&entity=song"
                    val json = URL(url).readText()
                    
                    if (json.contains("trackName\":\"")) {
                        val title = json.substringAfter("trackName\":\"").substringBefore("\"")
                        val artist = json.substringAfter("artistName\":\"").substringBefore("\"")
                        val cover = json.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                        val audio = json.substringAfter("previewUrl\":\"").substringBefore("\"")
                        
                        val genreId = when {
                            index < 10 -> "gen_pop"
                            index < 20 -> "gen_rock_pe"
                            index < 30 -> "gen_reg"
                            index < 40 -> "gen_salsa"
                            else -> "gen_cumbia"
                        }

                        val songId = "song_v2_$index"
                        val song = Song(songId, title, artist, genreId, audio, cover, "00:30")
                        
                        songsCollection.document(songId).set(song).await()
                        
                        synchronized(albumSongsMap) {
                            albumSongsMap.getOrPut(genreId) { mutableListOf() }.add(songId)
                        }

                        val artId = "art_${artist.lowercase().replace(" ", "_")}"
                        artistsCollection.document(artId).set(Artist(artId, artist, cover)).await()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.awaitAll()

        // 5. ÁLBUMES
        for ((genreId, songIds) in albumSongsMap) {
            val name = genres.find { it.id == genreId }?.name ?: "Mix"
            val albumId = "album_v2_$genreId"
            val album = Album(albumId, "Top 10 $name", "Varios", "https://picsum.photos/seed/$albumId/600/600", "2024", songIds)
            albumsCollection.document(albumId).set(album).await()
        }
    }
    
    fun getGenres(): Flow<List<Genre>> = flow {
        val snapshot = genresCollection.get().await()
        emit(snapshot.toObjects(Genre::class.java))
    }

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encoded&limit=10&entity=song"
            val json = URL(url).readText()
            
            val songs = mutableListOf<Song>()
            val parts = json.split("{\"wrapperType\":\"track\"").drop(1)
            
            for (part in parts) {
                val title = part.substringAfter("trackName\":\"").substringBefore("\"")
                val artist = part.substringAfter("artistName\":\"").substringBefore("\"")
                val cover = part.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                val audio = part.substringAfter("previewUrl\":\"").substringBefore("\"")
                val id = part.substringAfter("trackId\":").substringBefore(",")

                songs.add(Song("search_$id", title, artist, "search", audio, cover, "00:30"))
            }
            songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
