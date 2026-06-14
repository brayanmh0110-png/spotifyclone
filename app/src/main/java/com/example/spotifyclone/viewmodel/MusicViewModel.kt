package com.example.spotifyclone.viewmodel

import android.media.MediaPlayer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.model.*
import com.example.spotifyclone.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// Enum para controlar los modos de repetición de la música
enum class ModoRepeticion { NINGUNO, UNO, TODO }

/**
 * MusicViewModel: El "cerebro" musical de la aplicación.
 * Gestiona el estado de la reproducción, las listas de canciones, favoritos y la lógica de Firebase.
 * Mantiene la interfaz de usuario sincronizada con lo que está sonando.
 */
class MusicViewModel : ViewModel() {
    // Conexión con el repositorio de datos
    private val repository = MusicRepository()

    // --- ESTADOS REACTIVOS (StateFlow) ---
    // Usamos _variable (privada) para modificar y variable (pública) para que la UI la lea.

    // Lista global de todas las canciones cargadas
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    // Géneros musicales para la exploración
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    // Artistas disponibles en la app
    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    // Álbumes disponibles
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    // Canciones del álbum seleccionado actualmente
    private val _albumSongs = MutableStateFlow<List<Song>>(emptyList())
    val albumSongs: StateFlow<List<Song>> = _albumSongs.asStateFlow()

    // Canciones del artista seleccionado
    private val _artistSongs = MutableStateFlow<List<Song>>(emptyList())
    val artistSongs: StateFlow<List<Song>> = _artistSongs.asStateFlow()

    // Artista que se está visualizando en detalle
    private val _artistaActual = MutableStateFlow<Artist?>(null)
    val artistaActual: StateFlow<Artist?> = _artistaActual.asStateFlow()

    // Lista de canciones favoritas del usuario (con "Me gusta")
    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    // Resultados de la búsqueda activa
    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    // Indica si se está realizando una búsqueda en la API
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // --- ESTADO DEL REPRODUCTOR ---
    
    // Cola de reproducción actual (lista de canciones que están sonando o vendrán después)
    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    // La canción que está sonando en este preciso momento
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    // Estado del botón Play/Pause
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Posición actual de la canción en milisegundos (para la barra de progreso)
    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()

    // Duración total de la canción actual
    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    // Configuración de reproducción aleatoria
    private val _esModoAleatorio = MutableStateFlow(false)
    val esModoAleatorio: StateFlow<Boolean> = _esModoAleatorio.asStateFlow()

    // Configuración del modo de repetición
    private val _modoRepeticion = MutableStateFlow(ModoRepeticion.NINGUNO)
    val modoRepeticion: StateFlow<ModoRepeticion> = _modoRepeticion.asStateFlow()

    // Playlists creadas por el usuario
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Color dominante de la portada (para diseño dinámico)
    private val _dominantColor = MutableStateFlow(0xFF503691.toInt())
    val dominantColor: StateFlow<Int> = _dominantColor.asStateFlow()

    // Historial de actividad (acciones recientes del usuario)
    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    // Álbum visualizado actualmente
    private val _albumActual = MutableStateFlow<Album?>(null)
    val albumActual: StateFlow<Album?> = _albumActual.asStateFlow()

    // El objeto MediaPlayer real de Android que emite el sonido
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Inicia la reproducción de una canción.
     * @param song La canción elegida.
     * @param playlist La lista de donde viene esa canción para poder saltar a la siguiente.
     */
    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (song.audioUrl.isEmpty()) return
        
        // Actualizamos la cola de reproducción
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (!_currentPlaylist.value.contains(song)) {
            _currentPlaylist.value = listOf(song)
        }

        // Limpiamos el reproductor anterior si existía para evitar solapamientos
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.audioUrl) // Cargamos el link de internet de la canción
                prepareAsync() // Preparamos la canción sin bloquear la app
                setOnPreparedListener {
                    start() // ¡Empieza a sonar!
                    _currentSong.value = song
                    _isPlaying.value = true
                    _duration.value = duration.toFloat()
                    updateProgress() // Iniciamos el hilo que mueve la barra de progreso
                    updateDominantColor(song.coverUrl) // Analizamos los colores de la portada
                }
                setOnCompletionListener {
                    // Cuando termina la canción, pasamos a la siguiente automáticamente
                    playNextSong()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    /**
     * Alterna entre Pausa y Reproducción.
     */
    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
                updateProgress() // Retomamos la barra de progreso
            }
        }
    }

    /**
     * Lógica compleja para saltar a la siguiente canción considerando:
     * - Modo Repetir 1: Vuelve a empezar la misma.
     * - Modo Aleatorio: Elige una al azar de la lista.
     * - Modo Normal: Sigue el orden de la lista.
     */
    fun playNextSong() {
        val listaActual = _currentPlaylist.value
        val indexActual = listaActual.indexOfFirst { it.id == _currentSong.value?.id }

        val siguienteCancion: Song? = when (_modoRepeticion.value) {
            ModoRepeticion.UNO -> _currentSong.value
            ModoRepeticion.TODO -> {
                if (_esModoAleatorio.value) {
                    listaActual.filter { it.id != _currentSong.value?.id }.randomOrNull()
                } else {
                    listaActual.getOrNull(indexActual + 1) ?: listaActual.firstOrNull()
                }
            }
            ModoRepeticion.NINGUNO -> {
                if (_esModoAleatorio.value) {
                    listaActual.filter { it.id != _currentSong.value?.id }.randomOrNull()
                } else {
                    listaActual.getOrNull(indexActual + 1)
                }
            }
        }
        siguienteCancion?.let { playSong(it) }
    }

    /**
     * Regresa a la canción anterior en la lista.
     */
    fun playPreviousSong() {
        val currentList = _currentPlaylist.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1])
        } else if (currentList.isNotEmpty()) {
            playSong(currentList.last())
        }
    }

    /**
     * Permite al usuario mover la barra de progreso (Slider) a un punto específico.
     */
    fun seekTo(position: Float) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    /**
     * Actualiza el estado de la posición actual del audio de forma fluida.
     */
    private fun updateProgress() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let {
                    _currentPosition.value = it.currentPosition.toFloat()
                }
                delay(100) // 10fps para que la barra se mueva suave (cada 100ms)
            }
        }
    }

    /**
     * Detiene la música y limpia todo. Se usa al cerrar sesión.
     */
    fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _currentSong.value = null
            _isPlaying.value = false
            _currentPosition.value = 0f
        }
    }

    // Al cerrar el ViewModel, liberamos el reproductor para no gastar batería
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    init {
        // Al iniciar la app, cargamos todos los datos iniciales desde Firebase
        loadGenres()
        loadArtists()
        loadAlbums()
        loadSongs()
        
        viewModelScope.launch {
            seedData() // Función de emergencia para llenar la base de datos si está vacía
        }
    }

    // --- FUNCIONES DE CARGA DESDE EL REPOSITORIO ---

    private fun loadGenres() {
        viewModelScope.launch { repository.getGenres().collect { _genres.value = it } }
    }

    private fun loadArtists() {
        viewModelScope.launch { repository.getArtists().collect { _artists.value = it } }
    }

    private fun loadAlbums() {
        viewModelScope.launch { repository.getAlbums().collect { _albums.value = it } }
    }

    private fun loadSongs() {
        viewModelScope.launch { repository.getSongs().collect { _songs.value = it } }
    }

    /**
     * Extrae el color dominante de la portada de la canción para el diseño dinámico.
     */
    private fun updateDominantColor(imageUrl: String) {
        if (imageUrl.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(input)
                
                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val color = palette.getDominantColor(0xFF503691.toInt())
                    _dominantColor.value = color
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Carga las canciones de un álbum por sus IDs.
     */
    fun loadSongsByAlbum(songIds: List<String>) {
        viewModelScope.launch {
            _albumSongs.value = repository.getSongsByAlbum(songIds)
        }
    }

    /**
     * Carga los favoritos del usuario.
     */
    fun loadFavorites(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _favorites.value = repository.getUserFavorites(userId)
        }
    }

    /**
     * Agrega o quita una canción de los favoritos del usuario.
     */
    fun toggleFavorite(userId: String, song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(userId, song)
            loadFavorites(userId)
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    /**
     * Realiza una búsqueda de canciones con un "debounce" de 500ms para no saturar la API.
     */
    fun searchSongs(query: String) {
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(500) // Espera un poco a que el usuario termine de escribir
            _searchResults.value = repository.searchSongs(query)
            _isSearching.value = false
        }
    }

    /**
     * Si la base de datos está vacía, descarga música de iTunes y llena Firebase.
     */
    suspend fun seedData() {
        repository.seedFullProjectData()
    }

    /**
     * Cambia entre modo normal y aleatorio.
     */
    fun alternarAleatorio() {
        _esModoAleatorio.value = !_esModoAleatorio.value
    }

    /**
     * Cicla entre los modos de repetición (Ninguno -> Uno -> Todos).
     */
    fun cambiarModoRepeticion() {
        _modoRepeticion.value = when (_modoRepeticion.value) {
            ModoRepeticion.NINGUNO -> ModoRepeticion.TODO
            ModoRepeticion.TODO -> ModoRepeticion.UNO
            ModoRepeticion.UNO -> ModoRepeticion.NINGUNO
        }
    }

    /**
     * Añade una canción al final de la cola de reproducción actual.
     */
    fun agregarALaCola(song: Song) {
        val current = _currentPlaylist.value.toMutableList()
        if (!current.contains(song)) {
            current.add(song)
            _currentPlaylist.value = current
        }
    }

    /**
     * Cambia el orden de una canción en la cola.
     */
    fun reordenarCola(from: Int, to: Int) {
        val list = _currentPlaylist.value.toMutableList()
        if (from in list.indices && to in list.indices) {
            val item = list.removeAt(from)
            list.add(to, item)
            _currentPlaylist.value = list
        }
    }

    /**
     * Quita una canción de la cola.
     */
    fun quitarDeCola(songId: String) {
        _currentPlaylist.value = _currentPlaylist.value.filter { it.id != songId }
    }

    /**
     * Registra que una canción ha sido escuchada en el historial de Firebase.
     */
    fun registrarReproduccion(userId: String, song: Song) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.logActivity(userId, "Escuchó: ${song.title} - ${song.artist}")
        }
    }

    /**
     * Escucha cambios en las playlists del usuario.
     */
    fun cargarPlaylists(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.getPlaylists(userId).collect {
                _playlists.value = it
            }
        }
    }

    /**
     * Escucha el historial de actividad.
     */
    fun cargarHistorial(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.getActivityLogs(userId).collect {
                _activityLogs.value = it
            }
        }
    }

    /**
     * Crea una playlist nueva en Firebase.
     */
    fun crearPlaylist(userId: String, nombre: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.crearPlaylist(userId, nombre)
            onSuccess(id)
        }
    }

    /**
     * Añade una canción a una playlist existente.
     */
    fun agregarCancionAPlaylist(userId: String, playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(userId, playlistId, songId)
        }
    }

    /**
     * Quita una canción de una playlist.
     */
    fun quitarCancionDePlaylist(userId: String, playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(userId, playlistId, songId)
        }
    }

    /**
     * Elimina una playlist completa de la base de datos.
     */
    fun eliminarPlaylist(userId: String, playlistId: String) {
        viewModelScope.launch {
            repository.eliminarPlaylist(userId, playlistId)
        }
    }

    /**
     * Selecciona un artista para mostrar sus detalles.
     */
    fun seleccionarArtista(artista: Artist) {
        _artistaActual.value = artista
        _artistSongs.value = _songs.value.filter { it.artist == artista.name }
    }

    /**
     * Selecciona un álbum para mostrar sus detalles.
     */
    fun seleccionarAlbum(album: Album) {
        _albumActual.value = album
        loadSongsByAlbum(album.songIds)
    }
}
