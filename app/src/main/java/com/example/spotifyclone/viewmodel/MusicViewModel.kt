package com.example.spotifyclone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.model.Genre
import com.example.spotifyclone.model.Playlist
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.repository.MusicRepository
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

    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    // Estado para el mini-reproductor
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun playSong(song: Song) {
        _currentSong.value = song
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    init {
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().collect {
                _genres.value = it
            }
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

    fun seedData() {
        viewModelScope.launch {
            repository.seedClassicalMusic()
        }
    }

    fun updatePlaylist(userId: String, playlist: Playlist) {
        viewModelScope.launch {
            repository.updatePlaylist(userId, playlist)
        }
    }
}
