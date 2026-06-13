package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.ModoRepeticion
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * PlayerScreen: Pantalla completa del reproductor.
 * Se abre con una animación hacia arriba y muestra todos los controles de la canción actual.
 */
@Composable
fun PlayerScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    // 1. Estados reactivos del reproductor
    val context = LocalContext.current
    val cancionActual by musicViewModel.currentSong.collectAsState()
    val estaReproduciendo by musicViewModel.isPlaying.collectAsState()
    val posicionActual by musicViewModel.currentPosition.collectAsState()
    val duracionTotal by musicViewModel.duration.collectAsState()
    val estadoUsuario by authViewModel.userState.collectAsState()
    val listaFavoritos by musicViewModel.favorites.collectAsState()
    val esModoAleatorio by musicViewModel.esModoAleatorio.collectAsState()
    val modoRepeticion by musicViewModel.modoRepeticion.collectAsState()
    val listaPlaylists by musicViewModel.playlists.collectAsState()

    var menuExpandido by remember { mutableStateOf(false) }
    var cancionParaPlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(estadoUsuario.uid)
        }
    }

    // Si no hay canción, no dibujamos nada
    if (cancionActual == null) return

    val esFavorita = listaFavoritos.any { it.id == cancionActual?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF503691), Color.Black)))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera: Botón de bajar y Título de contexto
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.KeyboardArrowDown, "Cerrar", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("REPRODUCIENDO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { menuExpandido = true }) {
                    Icon(Icons.Default.MoreVert, "Opciones", tint = Color.White)
                }
                DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                    DropdownMenuItem(
                        text = { Text("Agregar a la cola") },
                        leadingIcon = { Icon(Icons.Default.QueueMusic, null) },
                        onClick = {
                            cancionActual?.let { 
                                musicViewModel.agregarALaCola(it)
                                Toast.makeText(context, "Agregado a la cola", Toast.LENGTH_SHORT).show()
                            }
                            menuExpandido = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Agregar a playlist") },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) },
                        onClick = {
                            cancionParaPlaylist = true
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Portada del álbum en grande
        AsyncImage(
            model = cancionActual?.coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(48.dp))

        // Info de Canción y Botón de Like
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cancionActual?.title ?: "", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(cancionActual?.artist ?: "", color = Color.LightGray, fontSize = 18.sp)
            }
            IconButton(onClick = { 
                cancionActual?.let { 
                    musicViewModel.toggleFavorite(estadoUsuario.uid, it.id)
                    val msg = if (esFavorita) "Eliminado de favoritos" else "Agregado a favoritos"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (esFavorita) Color(0xFF1DB954) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- BARRA DE PROGRESO (SLIDER) ---
        Slider(
            value = posicionActual,
            onValueChange = { musicViewModel.seekTo(it) },
            valueRange = 0f..duracionTotal.coerceAtLeast(1f),
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatearTiempo(posicionActual.toInt()), color = Color.LightGray, fontSize = 12.sp)
            Text(formatearTiempo(duracionTotal.toInt()), color = Color.LightGray, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        // --- CONTROLES DE REPRODUCCIÓN ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            // Botón Aleatorio: verde cuando está activo
            IconButton(onClick = { musicViewModel.alternarAleatorio() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (esModoAleatorio) Color(0xFF1DB954) else Color.LightGray
                )
            }

            IconButton(onClick = { musicViewModel.playPreviousSong() }) {
                Icon(Icons.Default.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            // Botón central de Play/Pause
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White).clickable { musicViewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (estaReproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(40.dp))
            }

            IconButton(onClick = { musicViewModel.playNextSong() }) {
                Icon(Icons.Default.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            // Botón Repetir: verde y con ícono diferente según el modo activo
            IconButton(onClick = { musicViewModel.cambiarModoRepeticion() }) {
                Icon(
                    imageVector = if (modoRepeticion == ModoRepeticion.UNO) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repetir",
                    tint = if (modoRepeticion == ModoRepeticion.NINGUNO) Color.LightGray else Color(0xFF1DB954)
                )
            }
        }
    }

    // Diálogo para agregar la canción actual a una playlist
    if (cancionParaPlaylist) {
        AlertDialog(
            onDismissRequest = { cancionParaPlaylist = false },
            title = { Text("Agregar a playlist", color = Color.White) },
            text = {
                if (listaPlaylists.isEmpty()) {
                    Text("No tienes playlists aún.\nCrea una desde Tu Biblioteca.", color = Color.Gray)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(listaPlaylists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cancionActual?.let {
                                            musicViewModel.agregarCancionAPlaylist(estadoUsuario.uid, playlist.id, it.id)
                                            Toast.makeText(context, "Agregado a ${playlist.name}", Toast.LENGTH_SHORT).show()
                                        }
                                        cancionParaPlaylist = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954))
                                Spacer(Modifier.width(12.dp))
                                Text(playlist.name, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { cancionParaPlaylist = false }) {
                    Text("Cerrar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

/**
 * Convierte milisegundos en formato mm:ss para mostrar en pantalla.
 */
private fun formatearTiempo(milisegundos: Int): String {
    val totalSegundos = milisegundos / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    return "%d:%02d".format(minutos, segundos)
}
