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
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel,
    vistaModeloAutenticacion: AuthViewModel
) {
    // 1. Estados reactivos del reproductor
    val cancionActual by vistaModeloMusica.currentSong.collectAsState()
    val estaReproduciendo by vistaModeloMusica.isPlaying.collectAsState()
    val posicionActual by vistaModeloMusica.currentPosition.collectAsState()
    val duracionTotal by vistaModeloMusica.duration.collectAsState()
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()
    val listaFavoritos by vistaModeloMusica.favorites.collectAsState()
    val esModoAleatorio by vistaModeloMusica.esModoAleatorio.collectAsState()
    val modoRepeticion by vistaModeloMusica.modoRepeticion.collectAsState()
    val listaPlaylists by vistaModeloMusica.playlists.collectAsState()

    var menuExpandido by remember { mutableStateOf(false) }
    var cancionParaPlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            vistaModeloMusica.cargarPlaylists(estadoUsuario.uid)
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
            IconButton(onClick = { controladorNavegacion.popBackStack() }) {
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
                            cancionActual?.let { vistaModeloMusica.agregarALaCola(it) }
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
                cancionActual?.let { vistaModeloMusica.toggleFavorite(estadoUsuario.uid, it.id) }
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
            onValueChange = { vistaModeloMusica.seekTo(it) },
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
            IconButton(onClick = { vistaModeloMusica.alternarAleatorio() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (esModoAleatorio) Color(0xFF1DB954) else Color.LightGray
                )
            }

            IconButton(onClick = { vistaModeloMusica.playPreviousSong() }) {
                Icon(Icons.Default.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            // Botón central de Play/Pause
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White).clickable { vistaModeloMusica.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (estaReproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(40.dp))
            }

            IconButton(onClick = { vistaModeloMusica.playNextSong() }) {
                Icon(Icons.Default.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            // Botón Repetir: verde y con ícono diferente según el modo activo
            IconButton(onClick = { vistaModeloMusica.cambiarModoRepeticion() }) {
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
                                            vistaModeloMusica.agregarCancionAPlaylist(estadoUsuario.uid, playlist.id, it.id)
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
