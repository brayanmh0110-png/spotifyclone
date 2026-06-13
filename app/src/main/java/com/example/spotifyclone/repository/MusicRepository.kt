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
 * Este es el repositorio principal de la aplicación.
 * Se encarga de dos cosas importantes:
 * 1. Hablar con la base de datos de Firebase (Firestore) para guardar y traer música.
 * 2. Hablar con la API de iTunes para buscar canciones reales de internet.
 */
class MusicRepository {
    // Conexión con la base de datos de Firebase (Firestore)
    private val firestore = FirebaseFirestore.getInstance()

    // Accesos directos a las "carpetas" (colecciones) de nuestra base de datos en la nube:
    private val usersCollection = firestore.collection("users")
    private val songsCollection = firestore.collection("songs")
    private val genresCollection = firestore.collection("genres")
    private val playlistsCollection = firestore.collection("playlists")
    private val activityLogCollection = firestore.collection("activity_log")
    private val artistsCollection = firestore.collection("artists")
    private val albumsCollection = firestore.collection("albums")

    /**
     * Trae todas las canciones que están guardadas en nuestra base de datos.
     * Devuelve un "Flow" que es como un canal de agua por donde llegan los datos.
     */
    fun getSongs(): Flow<List<Song>> = flow {
        // Pedimos los datos a la nube y esperamos a que lleguen
        val snapshot = songsCollection.get().await()
        // Convertimos los documentos de la nube en una lista de objetos Song para la app
        emit(snapshot.toObjects(Song::class.java))
    }

    /**
     * Trae la lista de artistas que tenemos registrados.
     */
    fun getArtists(): Flow<List<Artist>> = flow {
        val snapshot = artistsCollection.get().await()
        emit(snapshot.toObjects(Artist::class.java))
    }

    /**
     * Trae la lista de álbumes disponibles.
     */
    fun getAlbums(): Flow<List<Album>> = flow {
        val snapshot = albumsCollection.get().await()
        emit(snapshot.toObjects(Album::class.java))
    }

    /**
     * Busca y trae canciones específicas cuando le damos una lista de IDs.
     * Útil para cuando abrimos un álbum y queremos ver sus canciones.
     */
    suspend fun getSongsByAlbum(songIds: List<String>): List<Song> {
        if (songIds.isEmpty()) return emptyList()
        // Le pedimos a Firestore: "Dame las canciones que tengan estos IDs"
        return songsCollection.whereIn("id", songIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Obtiene las canciones que el usuario marcó con el corazón (Me gusta).
     */
    suspend fun getUserFavorites(userId: String): List<Song> {
        // 1. Buscamos la ficha del usuario
        val userDoc = usersCollection.document(userId).get().await()
        // 2. Extraemos la lista de IDs de canciones favoritas que tiene guardadas
        val favoritesIds = userDoc.toObject(User::class.java)?.favorites ?: emptyList()

        if (favoritesIds.isEmpty()) return emptyList()

        // 3. Traemos la información completa de esas canciones favoritas
        return songsCollection.whereIn("id", favoritesIds).get().await().toObjects(Song::class.java)
    }

    /**
     * Agrega o quita una canción de los favoritos del usuario.
     */
    suspend fun toggleFavorite(userId: String, songId: String) {
        val userRef = usersCollection.document(userId)
        val userDoc = userRef.get().await()
        val favorites = userDoc.toObject(User::class.java)?.favorites ?: emptyList()

        if (favorites.contains(songId)) {
            // Si ya era favorita, la quitamos usando arrayRemove (borra un elemento de la lista en la nube)
            userRef.update("favorites", FieldValue.arrayRemove(songId)).await()
            logActivity(userId, "Removed $songId from favorites")
        } else {
            // Si no era favorita, la agregamos usando arrayUnion (añade sin repetir)
            userRef.update("favorites", FieldValue.arrayUnion(songId)).await()
            logActivity(userId, "Added $songId to favorites")
        }
    }

    /**
     * Revisa si el usuario actual tiene permiso para cambiar cosas en una playlist.
     * Devuelve verdadero si el usuario es el dueño o es un colaborador invitado.
     */
    suspend fun canEditPlaylist(userId: String, playlistId: String): Boolean {
        val playlistDoc = playlistsCollection.document(playlistId).get().await()
        val playlist = playlistDoc.toObject(Playlist::class.java) ?: return false

        return (playlist.ownerId == userId) || playlist.collaborators.contains(userId)
    }

    /**
     * Guarda los cambios hechos en una playlist (ej: cambiar el nombre).
     */
    suspend fun updatePlaylist(userId: String, playlist: Playlist): Result<Unit> {
        return if (canEditPlaylist(userId, playlist.id)) {
            // Si tiene permiso, guardamos la playlist completa en la nube
            playlistsCollection.document(playlist.id).set(playlist).await()
            logActivity(userId, "Updated playlist ${playlist.id}")
            Result.success(Unit)
        } else {
            Result.failure(Exception("No permission to edit this playlist"))
        }
    }

    /**
     * Guarda una pequeña nota en la base de datos cada vez que el usuario hace algo importante.
     */
    private suspend fun logActivity(userId: String, action: String) {
        val log = ActivityLog(userId = userId, action = action)
        activityLogCollection.add(log).await()
    }

    /**
     * Obtiene el historial de actividad del usuario desde Firestore.
     */
    fun getActivityLogs(userId: String): Flow<List<ActivityLog>> = flow {
        val snapshot = activityLogCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(15)
            .get().await()
        emit(snapshot.toObjects(ActivityLog::class.java))
    }

    /**
     * FUNCIÓN MAESTRA (Seed): Se encarga de llenar la app de música real la primera vez.
     * 1. Borra los datos de prueba viejos.
     * 2. Busca 50 canciones famosas en iTunes de forma automática.
     * 3. Crea los géneros, artistas y álbumes vinculados en la base de datos.
     */
    suspend fun seedFullProjectData() = withContext(Dispatchers.IO) {
        // Primero verificamos si ya hay canciones para no repetir el proceso
        val existingSongs = songsCollection.limit(1).get().await()
        if (!existingSongs.isEmpty) return@withContext

        // Limpieza: Borramos lo que haya en las carpetas principales para empezar de cero
        val collections = listOf(songsCollection, albumsCollection, artistsCollection, genresCollection)
        for (collection in collections) {
            val snapshot = collection.get().await()
            for (doc in snapshot.documents) doc.reference.delete().await()
        }

        // Definimos los 5 géneros musicales con imágenes bonitas de internet
        val genres = listOf(
            Genre("gen_pop", "Pop Global", "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?w=500"),
            Genre("gen_rock_pe", "Rock Peruano", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500"),
            Genre("gen_reg", "Reggaetón Hits", "https://images.unsplash.com/photo-1621112904887-419379ce6824?w=500"),
            Genre("gen_salsa", "Salsa Brava", "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=500"),
            Genre("gen_cumbia", "Cumbia Peruana", "https://images.unsplash.com/photo-1594122230689-45899d9e6f69?w=500")
        )
        for (g in genres) genresCollection.document(g.id).set(g).await()

        // Lista de 50 canciones que queremos buscar en internet
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

        // Proceso paralelo: Buscamos todas las canciones al mismo tiempo para que sea rápido
        queries.mapIndexed { index, query ->
            async {
                try {
                    // Preparamos el nombre para internet
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://itunes.apple.com/search?term=$encoded&limit=1&entity=song"
                    // Descargamos la información de iTunes
                    val json = URL(url).readText()
                    
                    if (json.contains("trackName\":\"")) {
                        // Extraemos los datos: Título, Artista, Portada y el Audio
                        val title = json.substringAfter("trackName\":\"").substringBefore("\"")
                        val artist = json.substringAfter("artistName\":\"").substringBefore("\"")
                        val cover = json.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                        val audio = json.substringAfter("previewUrl\":\"").substringBefore("\"")
                        
                        // Asignamos un género según la posición en la lista (cada 10 canciones cambia de género)
                        val genreId = when {
                            index < 10 -> "gen_pop"
                            index < 20 -> "gen_rock_pe"
                            index < 30 -> "gen_reg"
                            index < 40 -> "gen_salsa"
                            else -> "gen_cumbia"
                        }

                        val songId = "song_v2_$index"
                        val song = Song(songId, title, artist, genreId, audio, cover, "00:30")
                        
                        // Guardamos la canción completa en nuestra base de datos
                        songsCollection.document(songId).set(song).await()

                        // Agrupamos la canción para crear un álbum después
                        synchronized(albumSongsMap) {
                            albumSongsMap.getOrPut(genreId) { mutableListOf() }.add(songId)
                        }

                        // Creamos automáticamente la ficha del artista
                        val artId = "art_${artist.lowercase().replace(" ", "_")}"
                        artistsCollection.document(artId).set(Artist(artId, artist, cover)).await()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.awaitAll()

        // Finalmente, creamos los álbumes "Top 10" usando las canciones que acabamos de descargar
        for ((genreId, songIds) in albumSongsMap) {
            val name = genres.find { it.id == genreId }?.name ?: "Mix"
            val albumId = "album_v2_$genreId"
            val album = Album(albumId, "Top 10 $name", "Varios", "https://picsum.photos/seed/$albumId/600/600", "2024", songIds)
            albumsCollection.document(albumId).set(album).await()
        }
    }

    /**
     * Trae la lista de géneros musicales de la nube.
     */
    fun getGenres(): Flow<List<Genre>> = flow {
        val snapshot = genresCollection.get().await()
        emit(snapshot.toObjects(Genre::class.java))
    }

    /**
     * Trae las playlists que le pertenecen al usuario que está usando la app.
     */
    fun getPlaylists(userId: String): Flow<List<Playlist>> = flow {
        // Buscamos playlists donde el "dueño" sea el usuario indicado
        val snapshot = playlistsCollection.whereEqualTo("ownerId", userId).get().await()
        emit(snapshot.toObjects(Playlist::class.java))
    }

    /**
     * Crea una playlist nueva y vacía.
     */
    suspend fun crearPlaylist(userId: String, nombre: String): String {
        // Generamos un ID único basado en el tiempo exacto en milisegundos
        val id = "pl_${System.currentTimeMillis()}"
        val playlist = Playlist(id = id, name = nombre, ownerId = userId)
        // Guardamos la playlist en la base de datos
        playlistsCollection.document(id).set(playlist).await()
        logActivity(userId, "Creó la playlist: $nombre")
        return id
    }

    /**
     * Agrega una canción a una playlist existente si tenemos permiso.
     */
    suspend fun addSongToPlaylist(userId: String, playlistId: String, songId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            // Usamos arrayUnion para añadir el ID de la canción a la lista de la playlist
            playlistsCollection.document(playlistId)
                .update("songsIds", FieldValue.arrayUnion(songId)).await()
        }
    }

    /**
     * Quita una canción de una playlist.
     */
    suspend fun removeSongFromPlaylist(userId: String, playlistId: String, songId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            // Usamos arrayRemove para quitar el ID de la canción de la lista
            playlistsCollection.document(playlistId)
                .update("songsIds", FieldValue.arrayRemove(songId)).await()
        }
    }

    /**
     * Elimina una playlist completa de la base de datos si el usuario tiene permiso.
     */
    suspend fun eliminarPlaylist(userId: String, playlistId: String) {
        if (canEditPlaylist(userId, playlistId)) {
            playlistsCollection.document(playlistId).delete().await()
            logActivity(userId, "Eliminó la playlist: $playlistId")
        }
    }

    /**
     * BUSCADOR EN VIVO: Busca canciones directamente en la API de iTunes.
     * Esto permite encontrar cualquier canción del mundo que esté en iTunes.
     */
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            // Consultamos a iTunes y pedimos 10 resultados
            val url = "https://itunes.apple.com/search?term=$encoded&limit=10&entity=song"
            val json = URL(url).readText()
            
            val songs = mutableListOf<Song>()
            // Dividimos el texto recibido para extraer cada canción manualmente
            val parts = json.split("{\"wrapperType\":\"track\"").drop(1)

            for (part in parts) {
                val title = part.substringAfter("trackName\":\"").substringBefore("\"")
                val artist = part.substringAfter("artistName\":\"").substringBefore("\"")
                val cover = part.substringAfter("artworkUrl100\":\"").substringBefore("\"").replace("100x100", "600x600")
                val audio = part.substringAfter("previewUrl\":\"").substringBefore("\"")
                val id = part.substringAfter("trackId\":").substringBefore(",")

                // Creamos una canción temporal para mostrar en los resultados de búsqueda
                songs.add(Song("search_$id", title, artist, "search", audio, cover, "00:30"))
            }
            songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
