package com.example.spotifyclone.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.model.Album
import com.example.spotifyclone.model.Artist
import com.example.spotifyclone.model.Genre
import com.example.spotifyclone.model.Playlist
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.repository.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ModoRepeticion { NINGUNO, UNO, TODO }

/**
 * ViewModel central para la gestión de música y el estado del reproductor.
 * Utiliza StateFlow para exponer datos reactivos a la interfaz de usuario.
 */
class MusicViewModel : ViewModel() {
    private val repository = MusicRepository()

    // Estados de datos (Listas cargadas de Firestore)
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _albumSongs = MutableStateFlow<List<Song>>(emptyList())
    val albumSongs: StateFlow<List<Song>> = _albumSongs.asStateFlow()

    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    // Estados del Reproductor
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    // --- RF07: Shuffle y Repeat ---
    private val _esModoAleatorio = MutableStateFlow(false)
    val esModoAleatorio: StateFlow<Boolean> = _esModoAleatorio.asStateFlow()

    private val _modoRepeticion = MutableStateFlow(ModoRepeticion.NINGUNO)
    val modoRepeticion: StateFlow<ModoRepeticion> = _modoRepeticion.asStateFlow()

    // --- RF11/RF12: Playlists del usuario ---
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // --- RF14: Álbum seleccionado actualmente ---
    private val _albumActual = MutableStateFlow<Album?>(null)
    val albumActual: StateFlow<Album?> = _albumActual.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Inicia la reproducción de una canción.
     * @param song La canción a reproducir.
     * @param playlist La lista de canciones actual (opcional) para habilitar Siguiente/Anterior.
     */
    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (song.audioUrl.isEmpty()) return
        
        // Actualizamos la cola de reproducción actual
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (!_currentPlaylist.value.contains(song)) {
            _currentPlaylist.value = listOf(song)
        }

        // Liberar el reproductor anterior si existía
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.audioUrl)
                prepareAsync() // Carga el audio en segundo plano
                setOnPreparedListener {
                    start()
                    _currentSong.value = song
                    _isPlaying.value = true
                    _duration.value = duration.toFloat()
                    updateProgress() // Inicia el seguimiento de la barra de tiempo
                }
                setOnCompletionListener {
                    // Al terminar la canción, pasa automáticamente a la siguiente
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
     * Alterna entre Play y Pause en la canción actual.
     */
    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
                updateProgress()
            }
        }
    }

    /**
     * Salta a la siguiente canción respetando el modo de repetición y aleatorio.
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
     * Salta a la canción anterior en la cola de reproducción.
     */
    fun playPreviousSong() {
        val currentList = _currentPlaylist.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1])
        } else if (currentList.isNotEmpty()) {
            playSong(currentList.last()) // Va al final si estaba en la primera
        }
    }

    /**
     * Mueve el progreso de la canción a una posición específica (SeekBar).
     */
    fun seekTo(position: Float) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    /**
     * Actualiza el estado de la posición actual del audio cada segundo.
     */
    private fun updateProgress() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let {
                    _currentPosition.value = it.currentPosition.toFloat()
                }
                delay(1000)
            }
        }
    }

    /**
     * Detiene completamente la reproducción y limpia el estado del reproductor.
     * Útil para cuando el usuario cierra sesión.
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

    /**
     * Limpieza del reproductor cuando el ViewModel se destruye.
     */
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    init {
        // Al iniciar, cargamos los datos existentes y lanzamos el sembrado automático
        loadGenres()
        loadArtists()
        loadAlbums()
        loadSongs()
        
        viewModelScope.launch {
            seedData() 
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().collect {
                _genres.value = it
            }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            repository.getArtists().collect {
                _artists.value = it
            }
        }
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            repository.getAlbums().collect {
                _albums.value = it
            }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            repository.getSongs().collect {
                _songs.value = it
            }
        }
    }

    /**
     * Carga las canciones de un álbum específico.
     */
    fun loadSongsByAlbum(songIds: List<String>) {
        viewModelScope.launch {
            _albumSongs.value = repository.getSongsByAlbum(songIds)
        }
    }

    /**
     * Carga la lista de favoritos del usuario desde el repositorio.
     */
    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _favorites.value = repository.getUserFavorites(userId)
        }
    }

    /**
     * Agrega o quita de favoritos.
     */
    fun toggleFavorite(userId: String, songId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(userId, songId)
            loadFavorites(userId) // Refresca la lista local después del cambio
        }
    }

    /**
     * Realiza una búsqueda de canciones en tiempo real a través del repositorio.
     */
    fun searchSongs(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = repository.searchSongs(query)
            _isSearching.value = false
        }
    }

    /**
     * Ejecuta el proceso de sembrado de datos y recarga las listas una vez finalizado.
     */
    suspend fun seedData() {
        repository.seedFullProjectData()
        loadGenres()
        loadArtists()
        loadAlbums()
        loadSongs()
    }

    /**
     * Actualiza metadatos de una playlist.
     */
    fun updatePlaylist(userId: String, playlist: Playlist) {
        viewModelScope.launch {
            repository.updatePlaylist(userId, playlist)
        }
    }

    // --- RF07: Activa o desactiva el modo aleatorio ---
    fun alternarAleatorio() {
        _esModoAleatorio.value = !_esModoAleatorio.value
    }

    // --- RF07: Cambia el modo de repetición: Ninguno → Todo → Uno → Ninguno ---
    fun cambiarModoRepeticion() {
        _modoRepeticion.value = when (_modoRepeticion.value) {
            ModoRepeticion.NINGUNO -> ModoRepeticion.TODO
            ModoRepeticion.TODO   -> ModoRepeticion.UNO
            ModoRepeticion.UNO   -> ModoRepeticion.NINGUNO
        }
    }

    // --- RF08: Inserta una canción justo después de la que está sonando ---
    fun agregarALaCola(cancion: Song) {
        val lista = _currentPlaylist.value.toMutableList()
        if (lista.none { it.id == cancion.id }) {
            val indexActual = lista.indexOfFirst { it.id == _currentSong.value?.id }
            val insertarEn = if (indexActual >= 0) indexActual + 1 else lista.size
            lista.add(insertarEn, cancion)
            _currentPlaylist.value = lista
        }
    }

    // --- RF11: Carga las playlists del usuario desde Firestore ---
    fun cargarPlaylists(userId: String) {
        viewModelScope.launch {
            repository.getPlaylists(userId).collect { _playlists.value = it }
        }
    }

    // --- RF11: Crea una nueva playlist vacía con el nombre dado ---
    // onCreada recibe el id de la nueva playlist (útil para abrirla al instante).
    fun crearPlaylist(userId: String, nombre: String, onCreada: (String) -> Unit = {}) {
        viewModelScope.launch {
            val nuevoId = repository.crearPlaylist(userId, nombre)
            // Recargamos aquí mismo para asegurar que la nueva playlist ya esté
            // disponible antes de avisar y abrir su pantalla.
            repository.getPlaylists(userId).collect { _playlists.value = it }
            onCreada(nuevoId)
        }
    }

    // --- RF12: Agrega una canción a una playlist existente ---
    fun agregarCancionAPlaylist(userId: String, playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(userId, playlistId, songId)
            cargarPlaylists(userId)
        }
    }

    // --- RF12: Quita una canción de una playlist ---
    fun quitarCancionDePlaylist(userId: String, playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(userId, playlistId, songId)
            cargarPlaylists(userId)
        }
    }

    /**
     * Elimina una playlist completa de la base de datos.
     */
    fun eliminarPlaylist(userId: String, playlistId: String) {
        viewModelScope.launch {
            repository.eliminarPlaylist(userId, playlistId)
            cargarPlaylists(userId) // Refrescamos la lista local
        }
    }

    // --- RF14: Guarda el álbum seleccionado y carga sus canciones ---
    fun seleccionarAlbum(album: Album) {
        _albumActual.value = album
        loadSongsByAlbum(album.songIds)
    }
}
