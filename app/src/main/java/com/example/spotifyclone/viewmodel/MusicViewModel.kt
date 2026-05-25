package com.example.spotifyclone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.model.Album
import com.example.spotifyclone.model.Artist
import com.example.spotifyclone.model.Genre
import com.example.spotifyclone.model.Playlist
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.repository.MusicRepository
import android.media.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {
    private val repository = MusicRepository()

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

    // Estado para el mini-reproductor
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (song.audioUrl.isEmpty()) return
        
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (!_currentPlaylist.value.contains(song)) {
            // Si se reproduce una canción suelta, la lista actual será solo esa canción
            _currentPlaylist.value = listOf(song)
        }

        // Detener canción actual si existe
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _currentSong.value = song
                    _isPlaying.value = true
                    _duration.value = duration.toFloat()
                    updateProgress()
                }
                setOnCompletionListener {
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

    fun playNextSong() {
        val currentList = _currentPlaylist.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            playSong(currentList[currentIndex + 1])
        } else if (currentList.isNotEmpty()) {
            // Si es la última, vuelve a la primera (opcional, estilo Spotify)
            playSong(currentList[0])
        }
    }

    fun playPreviousSong() {
        val currentList = _currentPlaylist.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1])
        } else if (currentList.isNotEmpty()) {
            // Si es la primera, va a la última
            playSong(currentList.last())
        }
    }

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

    fun seekTo(position: Float) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

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

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    init {
        // Primero cargamos lo que haya en Firestore para que la UI no esté vacía
        loadGenres()
        loadArtists()
        loadAlbums()
        loadSongs()
        
        // Luego, en segundo plano, verificamos y sembramos si es necesario
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

    fun loadSongsByAlbum(songIds: List<String>) {
        viewModelScope.launch {
            _albumSongs.value = repository.getSongsByAlbum(songIds)
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _favorites.value = repository.getUserFavorites(userId)
        }
    }

    fun toggleFavorite(userId: String, songId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(userId, songId)
            loadFavorites(userId) // Refresh
        }
    }

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

    suspend fun seedData() {
        repository.seedFullProjectData()
        // Después de sembrar, cargamos los datos reales a la UI
        loadGenres()
        loadArtists()
        loadAlbums()
        loadSongs()
    }

    fun updatePlaylist(userId: String, playlist: Playlist) {
        viewModelScope.launch {
            repository.updatePlaylist(userId, playlist)
        }
    }
}
