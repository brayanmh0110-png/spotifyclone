package com.example.spotifyclone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Album
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * AlbumDetailScreen: Muestra todas las canciones que pertenecen a un álbum.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlbumDetailScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val albumSongs by musicViewModel.albumSongs.collectAsState()
    val currentAlbum by musicViewModel.albumActual.collectAsState()
    val userState by authViewModel.userState.collectAsState()
    val playlists by musicViewModel.playlists.collectAsState()

    // Si el álbum no cargó, mostramos uno temporal (Cargando...)
    val album = currentAlbum ?: Album(title = "Cargando...", artist = "...", coverUrl = "")

    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(userState.uid) {
        if (userState.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(userState.uid)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- PARTE VISUAL: PORTADA ---
            item { AlbumHeaderImage(album.coverUrl) }
            
            // --- PARTE VISUAL: INFO DEL ÁLBUM ---
            item { AlbumInfoSection(album) }
            
            // --- ACCIONES RÁPIDAS ---
            item {
                AlbumActionPanel(
                    onPlayAll = {
                        albumSongs.firstOrNull()?.let { musicViewModel.playSong(it, albumSongs) }
                    },
                    onShuffle = {
                        musicViewModel.alternarAleatorio()
                        albumSongs.randomOrNull()?.let { musicViewModel.playSong(it, albumSongs) }
                    }
                )
            }

            // --- LISTADO DE CANCIONES ---
            items(albumSongs) { song ->
                AlbumSongRow(
                    song = song,
                    onPlay = { 
                        musicViewModel.playSong(song, albumSongs)
                        musicViewModel.registrarReproduccion(userState.uid, song)
                    },
                    onAddToQueue = { musicViewModel.agregarALaCola(song) },
                    onAddToPlaylist = { songToAddToPlaylist = song }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Modal para elegir playlist
    if (songToAddToPlaylist != null) {
        PlaylistSelectionDialog(
            playlists = playlists,
            onPlaylistSelect = { pl ->
                musicViewModel.agregarCancionAPlaylist(userState.uid, pl.id, songToAddToPlaylist!!.id)
                songToAddToPlaylist = null
                Toast.makeText(context, "Añadido", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { songToAddToPlaylist = null }
        )
    }
}

/**
 * Componentes privados de diseño del álbum
 */

@Composable
fun AlbumHeaderImage(coverUrl: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp)
            .background(brush = Brush.verticalGradient(colors = listOf(Color.Gray.copy(alpha = 0.5f), Color.Black))),
        contentAlignment = Alignment.Center
    ) {
        Surface(modifier = Modifier.size(200.dp), shadowElevation = 12.dp, color = Color.DarkGray) {
            AsyncImage(model = coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun AlbumInfoSection(album: Album) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(album.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
            Text(album.artist, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Álbum • ${album.releaseDate}", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun AlbumActionPanel(onPlayAll: () -> Unit, onShuffle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { }) { Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray, modifier = Modifier.size(28.dp)) }
        IconButton(onClick = { }) { Icon(Icons.Default.DownloadForOffline, null, tint = Color.Gray, modifier = Modifier.size(28.dp)) }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onShuffle) { Icon(Icons.Default.Shuffle, null, tint = Color(0xFF1DB954), modifier = Modifier.size(28.dp)) }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1DB954)).clickable { onPlayAll() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp)) }
    }
}

@Composable
fun AlbumSongRow(song: Song, onPlay: () -> Unit, onAddToQueue: () -> Unit, onAddToPlaylist: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(song.artist, color = Color.Gray, fontSize = 12.sp)
        }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.Gray) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = Color(0xFF282828)) {
                DropdownMenuItem(text = { Text("Agregar a la cola", color = Color.White) }, onClick = { onAddToQueue(); showMenu = false })
                DropdownMenuItem(text = { Text("Agregar a playlist", color = Color.White) }, onClick = { onAddToPlaylist(); showMenu = false })
            }
        }
    }
}

@Composable
fun PlaylistSelectionDialog(playlists: List<com.example.spotifyclone.model.Playlist>, onPlaylistSelect: (com.example.spotifyclone.model.Playlist) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar a playlist", color = Color.White) },
        text = {
            LazyColumn {
                items(playlists) { pl ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onPlaylistSelect(pl) }.padding(vertical = 12.dp)) {
                        Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954))
                        Spacer(Modifier.width(12.dp))
                        Text(pl.name, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF282828)
    )
}
