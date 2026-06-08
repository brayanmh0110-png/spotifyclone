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
 * PlaylistScreen: Muestra las canciones de una playlist y permite reproducirlas o quitarlas.
 * El parámetro playlistId identifica cuál playlist mostrar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String,
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel,
    vistaModeloAutenticacion: AuthViewModel
) {
    val listaPlaylists by vistaModeloMusica.playlists.collectAsState()
    val listaCanciones by vistaModeloMusica.songs.collectAsState()
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()

    // Nos aseguramos de tener cargadas las playlists del usuario al abrir la pantalla
    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            vistaModeloMusica.cargarPlaylists(estadoUsuario.uid)
        }
    }

    val playlist = listaPlaylists.find { it.id == playlistId }
    val cancionesDePlaylist = listaCanciones.filter { playlist?.songsIds?.contains(it.id) == true }

    // Canciones que aún no están en la playlist: se ofrecen como recomendaciones
    val cancionesRecomendadas = listaCanciones.filter { playlist?.songsIds?.contains(it.id) != true }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: "Playlist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { rellenos ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenos)
        ) {
            // Cabecera con ícono genérico y botón para reproducir todo
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${cancionesDePlaylist.size} canciones",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            cancionesDePlaylist.firstOrNull()?.let {
                                vistaModeloMusica.playSong(it, cancionesDePlaylist)
                            }
                        },
                        enabled = cancionesDePlaylist.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Reproducir todo", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Estado vacío
            if (cancionesDePlaylist.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Esta playlist está vacía.\nAgrega canciones desde las recomendaciones de abajo.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(cancionesDePlaylist) { cancion ->
                    ItemCancionEnPlaylist(
                        cancion = cancion,
                        alPulsar = { vistaModeloMusica.playSong(cancion, cancionesDePlaylist) },
                        alEliminar = {
                            playlist?.let {
                                vistaModeloMusica.quitarCancionDePlaylist(
                                    estadoUsuario.uid,
                                    it.id,
                                    cancion.id
                                )
                            }
                        }
                    )
                }
            }

            // --- Sección: canciones recomendadas para agregar a esta playlist ---
            if (cancionesRecomendadas.isNotEmpty()) {
                item {
                    Text(
                        text = "Te recomendamos",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                items(cancionesRecomendadas) { cancion ->
                    ItemCancionRecomendada(
                        cancion = cancion,
                        alPulsar = { vistaModeloMusica.playSong(cancion, cancionesRecomendadas) },
                        alAgregar = {
                            playlist?.let {
                                vistaModeloMusica.agregarCancionAPlaylist(
                                    estadoUsuario.uid,
                                    it.id,
                                    cancion.id
                                )
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * Fila individual de una canción dentro de una playlist.
 * El botón rojo quita la canción de la playlist sin borrarla del sistema.
 */
@Composable
private fun ItemCancionEnPlaylist(
    cancion: Song,
    alPulsar: () -> Unit,
    alEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cancion.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        // Botón para quitar la canción de esta playlist
        IconButton(onClick = { alEliminar() }) {
            Icon(
                imageVector = Icons.Default.RemoveCircleOutline,
                contentDescription = "Quitar de playlist",
                tint = Color.Gray
            )
        }
    }
}

/**
 * Fila de una canción recomendada. El botón "+" la agrega a la playlist actual.
 */
@Composable
private fun ItemCancionRecomendada(
    cancion: Song,
    alPulsar: () -> Unit,
    alAgregar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cancion.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        // Botón para agregar la canción a esta playlist
        IconButton(onClick = { alAgregar() }) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "Agregar a la playlist",
                tint = Color(0xFF1DB954)
            )
        }
    }
}
