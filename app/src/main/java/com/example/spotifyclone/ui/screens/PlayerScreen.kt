package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.ModoRepeticion
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.navigation.Screen

/**
 * PlayerScreen: Pantalla completa del reproductor.
 * Es el componente más visual de la app, con degradados dinámicos y controles avanzados.
 */
@Composable
fun PlayerScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    // --- OBTENCIÓN DE ESTADOS ---
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cancionActual by musicViewModel.currentSong.collectAsState()
    val estaReproduciendo by musicViewModel.isPlaying.collectAsState()
    val posicionActual by musicViewModel.currentPosition.collectAsState()
    val duracionTotal by musicViewModel.duration.collectAsState()
    val estadoUsuario by authViewModel.userState.collectAsState()
    val listaFavoritos by musicViewModel.favorites.collectAsState()
    val esModoAleatorio by musicViewModel.esModoAleatorio.collectAsState()
    val modoRepeticion by musicViewModel.modoRepeticion.collectAsState()
    val listaPlaylists by musicViewModel.playlists.collectAsState()
    
    // Obtener el color dinámico basado en la portada
    val dominantColorInt by musicViewModel.dominantColor.collectAsState()
    val colorFondo = Color(dominantColorInt)

    var menuExpandido by remember { mutableStateOf(false) }
    var cancionParaPlaylist by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Cargamos las playlists al entrar para el diálogo de "Agregar a playlist"
    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(estadoUsuario.uid)
        }
    }

    // Si por alguna razón no hay canción seleccionada, no mostramos nada
    if (cancionActual == null) return

    val esFavorita = listaFavoritos.any { it.id == cancionActual?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(colorFondo, Color.Black)))
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        
        // --- CABECERA (Header) ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.KeyboardArrowDown, "Cerrar", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("REPRODUCIENDO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { menuExpandido = true }) {
                    Icon(Icons.Default.MoreVert, "Opciones", tint = Color.White)
                }
                DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }, containerColor = Color(0xFF282828)) {
                    // Opción: Compartir
                    DropdownMenuItem(
                        text = { Text("Compartir canción", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Share, null, tint = Color.White) },
                        onClick = {
                            cancionActual?.let {
                                clipboardManager.setText(AnnotatedString("¡Escucha esta canción! ${it.title} - ${it.artist}"))
                                Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }
                            menuExpandido = false
                        }
                    )
                    // Opción: Cola
                    DropdownMenuItem(
                        text = { Text("Ver cola de reproducción", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PlaylistPlay, null, tint = Color.White) },
                        onClick = {
                            navController.navigate(Screen.Queue.route)
                            menuExpandido = false
                        }
                    )
                    // Opción: Agregar a Playlist
                    DropdownMenuItem(
                        text = { Text("Agregar a playlist", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = Color.White) },
                        onClick = {
                            cancionParaPlaylist = true
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // --- PORTADA DEL ÁLBUM ---
        AsyncImage(
            model = cancionActual?.coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(48.dp))

        // --- TÍTULO Y BOTÓN DE LIKE ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cancionActual?.title ?: "", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(cancionActual?.artist ?: "", color = Color.LightGray, fontSize = 18.sp, maxLines = 1)
            }
            IconButton(onClick = { 
                cancionActual?.let { 
                    musicViewModel.toggleFavorite(estadoUsuario.uid, it)
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

        // --- BARRA DE PROGRESO (Slider) ---
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

        // --- CONTROLES DE REPRODUCCIÓN (Play, Skip, Shuffle, Repeat) ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            // Botón Aleatorio
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

            // Botón CENTRAL Play/Pause
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White).clickable { musicViewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (estaReproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(40.dp))
            }

            IconButton(onClick = { musicViewModel.playNextSong() }) {
                Icon(Icons.Default.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(48.dp))
            }

            // Botón Repetir
            IconButton(onClick = { musicViewModel.cambiarModoRepeticion() }) {
                Icon(
                    imageVector = if (modoRepeticion == ModoRepeticion.UNO) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repetir",
                    tint = if (modoRepeticion == ModoRepeticion.NINGUNO) Color.LightGray else Color(0xFF1DB954)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // --- SECCIÓN DE LETRAS (Lyrics) ---
        LyricsSection(cancionActual?.title ?: "", colorFondo)

        Spacer(Modifier.height(48.dp))
    }

    // --- DIÁLOGO PARA AGREGAR A PLAYLIST ---
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
 * LyricsSection: Muestra la caja de letras debajo del reproductor.
 */
@Composable
fun LyricsSection(songTitle: String, dominantColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = dominantColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Letras", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Disfruta de la música de $songTitle\n\n" +
                "Las letras sincronizadas no están disponibles para esta canción en la versión de desarrollo.\n\n" +
                "¡Canta a todo pulmón!",
                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* Compartir letras */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("COMPARTIR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Convierte milisegundos en formato mm:ss (Ej: 185000 -> 03:05).
 */
private fun formatearTiempo(milisegundos: Int): String {
    val totalSegundos = milisegundos / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    return "%d:%02d".format(minutos, segundos)
}
