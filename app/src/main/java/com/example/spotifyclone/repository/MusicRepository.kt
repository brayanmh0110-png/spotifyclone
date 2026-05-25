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

/**
 * Repositorio central de la aplicación.
 * Gestiona la comunicación entre la base de datos (Firebase Firestore)
 * y la API externa de música (iTunes).
 */
class MusicRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Definición de las colecciones de Firestore
    private val usersCollection = firestore.collection("users")
    private val songsCollection = firestore.collection("songs")
    private val genresCollection = firestore.collection("genres")
    private val playlistsCollection = firestore.collection("playlists")
    private val activityLogCollection = firestore.collection("activity_log")
    private val artistsCollection = firestore.collection("artists")
    private val albumsCollection = firestore.collection("albums")

    /**
     * Obtiene todas las canciones almacenadas en Firestore.
     * Retorna un Flow que emite la lista de canciones.
     */
    fun getSongs(): Flow<List<Song>> = flow {
        val snapshot = songsCollection.get().await()
        emit(snapshot.toObjects(Song::class.java))
    }

    /**
     * Obtiene la lista de artistas desde Firestore.
     */
    fun getArtists(): Flow<List<Artist>> = flow {
        val snapshot = artistsCollection.get().await()
        emit(snapshot.toObjects(Artist::class.java))
    }

    /**
     * Obtiene la lista de álbumes desde Firestore.
     */
    fun getAlbums(): Flow<List<Album>> = flow {
        val snapshot = albumsCollection.get().await()
        emit(snapshot.toObjects(Album::class.java))
    }

    /**
     * Filtra y obtiene canciones específicas según una lista de IDs (usado para Álbumes).
     */
    suspend fun getSongsByAlbum(songIds: List<String>): List<Song> {
        if (songIds.isEmpty()) return emptyList()
        return songsCollection.whereIn("id", songIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Obtiene las canciones favoritas del usuario actual consultando su ID en Firestore.
     */
    suspend fun getUserFavorites(userId: String): List<Song> {
        val userDoc = usersCollection.document(userId).get().await()
        val favoritesIds = userDoc.toObject(User::class.java)?.favorites ?: emptyList()
        
        if (favoritesIds.isEmpty()) return emptyList()
        
        return songsCollection.whereIn("id", favoritesIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Agrega o elimina una canción de la lista de favoritos del usuario.
     * Utiliza arrayUnion y arrayRemove de Firebase para mayor eficiencia.
     */
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

    /**
     * Verifica si un usuario tiene permisos para editar una playlist (si es dueño o colaborador).
     */
    suspend fun canEditPlaylist(userId: String, playlistId: String): Boolean {
        val playlistDoc = playlistsCollection.document(playlistId).get().await()
        val playlist = playlistDoc.toObject(Playlist::class.java) ?: return false
        
        return (playlist.ownerId == userId) || playlist.collaborators.contains(userId)
    }

    /**
     * Actualiza la información de una playlist en Firestore si el usuario tiene permisos.
     */
    suspend fun updatePlaylist(userId: String, playlist: Playlist): Result<Unit> {
        return if (canEditPlaylist(userId, playlist.id)) {
            playlistsCollection.document(playlist.id).set(playlist).await()
            logActivity(userId, "Updated playlist ${playlist.id}")
            Result.success(Unit)
        } else {
            Result.failure(Exception("No permission to edit this playlist"))
        }
    }

    /**
     * Registra una acción del usuario en la colección de logs para auditoría o analítica.
     */
    private suspend fun logActivity(userId: String, action: String) {
        val log = ActivityLog(userId = userId, action = action)
        activityLogCollection.add(log).await()
    }

    /**
     * Función maestra de carga de datos inicial (Seed).
     * 1. Limpia datos antiguos.
     * 2. Busca 50 canciones reales en la API de iTunes en paralelo.
     * 3. Crea automáticamente los géneros, artistas y álbumes vinculados en Firestore.
     */
    suspend fun seedFullProjectData() = withContext(Dispatchers.IO) {
        // Verificamos si ya hay datos para evitar sobreescritura innecesaria
        val existingSongs = songsCollection.limit(1).get().await()
        if (!existingSongs.isEmpty) return@withContext

        // Limpieza de colecciones principales
        val collections = listOf(songsCollection, albumsCollection, artistsCollection, genresCollection)
        for (collection in collections) {
            val snapshot = collection.get().await()
            for (doc in snapshot.documents) doc.reference.delete().await()
        }

        // Definición de géneros con imágenes reales de Unsplash
        val genres = listOf(
            Genre("gen_pop", "Pop Global", "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?w=500"),
            Genre("gen_rock_pe", "Rock Peruano", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500"),
            Genre("gen_reg", "Reggaetón Hits", "https://images.unsplash.com/photo-1621112904887-419379ce6824?w=500"),
            Genre("gen_salsa", "Salsa Brava", "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=500"),
            Genre("gen_cumbia", "Cumbia Peruana", "https://images.unsplash.com/photo-1594122230689-45899d9e6f69?w=500")
        )
        for (g in genres) genresCollection.document(g.id).set(g).await()

        // Lista de canciones a buscar
        val queries = listOf(
            "Billie Eilish Birds of a Feather", "Sabrina Carpenter Espresso", "Taylor Swift Cruel Summer", 
            "Dua Lipa Houdini", "Harry Styles As It Was", "Olivia Rodrigo Vampire",
            "The Weeknd Starboy", "Miley Cyrus Flowers", "Benson Boone Beautiful Things", "Post Malone Circles",
            "Fragil Avenida Larco", "Mar de Copas Mujer Noche", "Libido En esta habitacion",
            "Amen Dicen", "Miki Gonzalez Carreteras", "Los Mojarras Triciclo Peru",
            "Rio Lo peor de todo", "Pedro Suarez Vertiz Cuando la cama", "Libido Solo", "Mar de Copas Suna",
            "Karol G Si Antes Te Hubiera Conocido", "Bad Bunny Perro Negro", "Feid Luna",
            "Myke Towers Lala", "Bad Bunny Monaco", "Karol G Provenza",
            "Quevedo Columbia", "Rauw Alejandro Diluvio", "Feid Classy 101", "Karol G QLONA",
            "Willie Colon Idilio", "Marc Anthony Vivir Mi Vida", "Daniela Darcourt Probabilidad de Amor",
            "Oscar D Leon Lloraras", "Joe Arroyo La Rebelion", "Gilberto Santa Rosa Conteo Regresivo",
            "Tito Nieves Fabricando Fantasias", "Frankie Ruiz La Cura", "Grupo Niche Gotas de Lluvia", "Victor Manuelle He Tratado",
            "Grupo 5 Motor y Motivo", "Grupo Nectar El Arbolito", "Agua Marina Tu Amor Fue Una Mentira",
            "Corazon Serrano Muriendo de Amor", "Armonia 10 Cervecita", "Hermanos Yaipen Me Emborracho",
            "Los Mirlos Cariñito", "Chacalon Muchacho Provinciano", "Grupo 5 Que levante la mano", "Agua Marina Pasitos"
        )

        val albumSongsMap = mutableMapOf<String, MutableList<String>>()

        // Peticiones asíncronas para descargar la información de las 50 canciones en segundos
        queries.mapIndexed { index, query ->
            async {
                try {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://itunes.apple.com/search?term=$encoded&limit=1&entity=song"
                    val json = URL(url).readText()
                    
                    if (json.contains("trackName\":\"")) {
                        // Extracción manual de datos del JSON de iTunes
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
                        
                        // Guardado en Firestore
                        songsCollection.document(songId).set(song).await()
                        
                        // Agrupación para creación de álbumes
                        synchronized(albumSongsMap) {
                            albumSongsMap.getOrPut(genreId) { mutableListOf() }.add(songId)
                        }

                        // Creación automática del artista
                        val artId = "art_${artist.lowercase().replace(" ", "_")}"
                        artistsCollection.document(artId).set(Artist(artId, artist, cover)).await()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.awaitAll()

        // Generación de los álbumes "Top 10" basados en las canciones descargadas
        for ((genreId, songIds) in albumSongsMap) {
            val name = genres.find { it.id == genreId }?.name ?: "Mix"
            val albumId = "album_v2_$genreId"
            val album = Album(albumId, "Top 10 $name", "Varios", "https://picsum.photos/seed/$albumId/600/600", "2024", songIds)
            albumsCollection.document(albumId).set(album).await()
        }
    }
    
    /**
     * Obtiene la lista de géneros musicales.
     */
    fun getGenres(): Flow<List<Genre>> = flow {
        val snapshot = genresCollection.get().await()
        emit(snapshot.toObjects(Genre::class.java))
    }

    /**
     * Realiza una búsqueda en vivo en la API de iTunes basándose en el query del usuario.
     * Retorna una lista de objetos Song con audio y carátula real.
     */
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
