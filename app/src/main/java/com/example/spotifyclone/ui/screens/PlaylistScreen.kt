package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * PlaylistScreen: Visualiza el contenido de una playlist creada por el usuario.
 * Permite reproducir las canciones, quitarlas y ver recomendaciones para añadir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String,
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val playlists by musicViewModel.playlists.collectAsState()
    val songs by musicViewModel.songs.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    // Sincronizamos las playlists al entrar
    LaunchedEffect(userState.uid) {
        if (userState.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(userState.uid)
        }
    }

    // Buscamos la playlist específica y sus canciones
    val playlist = playlists.find { it.id == playlistId }
    val playlistSongs = songs.filter { playlist?.songsIds?.contains(it.id) == true }
    
    // Filtramos canciones que NO están en la playlist para recomendarlas
    val recommendedSongs = songs.filter { playlist?.songsIds?.contains(it.id) != true }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = playlist?.name ?: "Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- CABECERA DE LA PLAYLIST ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(160.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954), modifier = Modifier.size(80.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("${playlistSongs.size} canciones", color = Color.Gray, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))

                    // Botón para empezar la música
                    Button(
                        onClick = {
                            playlistSongs.firstOrNull()?.let { musicViewModel.playSong(it, playlistSongs) }
                        },
                        enabled = playlistSongs.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Reproducir todo", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- LISTA DE CANCIONES ACTUALES ---
            if (playlistSongs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Esta playlist está vacía.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                    }
                }
            } else {
                items(playlistSongs) { song ->
                    PlaylistItem(
                        song = song,
                        onPlay = { 
                            musicViewModel.playSong(song, playlistSongs)
                            musicViewModel.registrarReproduccion(userState.uid, song)
                        },
                        onRemove = {
                            playlist?.let { musicViewModel.quitarCancionDePlaylist(userState.uid, it.id, song.id) }
                        }
                    )
                }
            }

            // --- SECCIÓN: RECOMENDACIONES ---
            if (recommendedSongs.isNotEmpty()) {
                item {
                    Text("Te recomendamos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
                items(recommendedSongs) { song ->
                    RecommendedItem(
                        song = song,
                        onPlay = { 
                            musicViewModel.playSong(song, recommendedSongs)
                            musicViewModel.registrarReproduccion(userState.uid, song)
                        },
                        onAdd = {
                            playlist?.let { musicViewModel.agregarCancionAPlaylist(userState.uid, it.id, song.id) }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * PlaylistItem: Fila de canción dentro de la playlist con botón de quitar.
 */
@Composable
private fun PlaylistItem(song: Song, onPlay: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(song.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Gray)
        }
    }
}

/**
 * RecommendedItem: Fila de canción con botón de añadir (+).
 */
@Composable
private fun RecommendedItem(song: Song, onPlay: () -> Unit, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(song.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        IconButton(onClick = onAdd) {
            Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF1DB954))
        }
    }
}
